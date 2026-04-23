# Day 7 · JWT / Token 体系 + 网关统一鉴权

> 目标：登录返回 JWT，网关统一校验 Token 并把 `userId / username` 透传下游；下游服务不再自己解析 Token。

---

## 一、最终产出

```
nova-common/
└── src/main/java/com/wang/novachat/common/
    ├── security/
    │   ├── JwtProperties.java       # nova.jwt.* 配置
    │   └── JwtService.java          # 签发 / 解析 / 校验 Token
    └── exception/GlobalExceptionHandler.java   # 加 @ConditionalOnClass(DispatcherServlet)

nova-user/
├── service/impl/UserServiceImpl.java           # 登录改用 JwtService 签发真 JWT
└── resources/application.yml                   # + nova.jwt.*

nova-gateway/
├── pom.xml                                      # + nova-common
├── GatewayApplication.java                      # scanBasePackages 包含 common
├── config/AuthProperties.java                   # nova.auth.white-list
├── filter/AuthGlobalFilter.java                 # WebFlux 全局鉴权过滤器
└── resources/application.yml                    # + nova.jwt / nova.auth
```

---

## 二、依赖与版本

父 pom 新增版本管理：

```xml
<jjwt.version>0.12.6</jjwt.version>
```

common 引入：

```xml
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><scope>runtime</scope></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><scope>runtime</scope></dependency>
```

> jjwt 0.12.x 是现代 API（`Jwts.builder().signWith(key, Jwts.SIG.HS256)`），与旧版 0.11.x 的静态 `SignatureAlgorithm.HS256` 不兼容，注意不要混用示例代码。

---

## 三、关键设计说明

### 3.1 `JwtService` 放在 common

JWT 的签发 / 解析逻辑在网关与业务服务之间必须 **完全一致**（共享密钥、相同的 claim 结构、相同的 issuer）。把它抽在 `nova-common.security` 里：

- **MVC 业务服务（nova-user）**：扫 `com.wang.novachat.common` → `JwtService` 作为 Bean 注入 `UserServiceImpl`，登录时调用 `issueToken`。
- **WebFlux 网关（nova-gateway）**：也扫 common → `JwtService` 同款 Bean 注入 `AuthGlobalFilter`，只调用 `parseClaims`。

一份代码两处复用，不可能出现 "网关签发的 Token，业务方解不开" 的经典 JWT 事故。

### 3.2 算法：HS256（对称）

- 密钥 `nova.jwt.secret` 长度必须 **≥ 32 字节**（256 bit）。`JwtService#init` 里做了强校验，配置不达标直接启动失败。
- 上线后建议切 **RS256/ES256（非对称）**：私钥只在签发服务（或 KMS）持有，公钥分发到所有校验端，密钥轮换更安全。Day7 先用 HS256 降低复杂度。

### 3.3 Claim 结构

```json
{
  "iss": "nova-chat",
  "sub": "1",
  "userId": 1,
  "username": "nova_demo",
  "iat": 1745208720,
  "exp": 1745813520
}
```

- `sub` 放 `userId` 字符串形式（JWT 规范 sub 是字符串）。
- 再冗余一份 `userId`（Number）+ `username`，避免网关每次都要字符串转 Long。
- 不塞 nickname / avatar：频繁变更的数据不应该进 Token，否则要等过期才能刷新。

### 3.4 `GlobalExceptionHandler` 的 `@ConditionalOnClass`

网关是 WebFlux，classpath 里没有 `spring-webmvc`，如果直接把 `@RestControllerAdvice` 风格的 `GlobalExceptionHandler` 扫进去会 ClassNotFound。

加一行就解决：

```java
@RestControllerAdvice
@ConditionalOnClass(DispatcherServlet.class)
public class GlobalExceptionHandler { ... }
```

`@ConditionalOnClass` 由 Spring Boot 用 ASM 读取元数据，classpath 不存在 `DispatcherServlet` 时 **连类都不会被加载**，而不是加载失败再回滚。所以网关可以放心 `scanBasePackages` 包含 common。

### 3.5 `AuthGlobalFilter`：网关鉴权核心

#### 3.5.1 执行顺序

```java
@Override public int getOrder() { return -100; }
```

`Ordered.HIGHEST_PRECEDENCE` 是 `Integer.MIN_VALUE`，Gateway 内置的 `RouteToRequestUrlFilter` 是 `10000` 左右。我们放 `-100`，在路由匹配之前执行，未登录请求在进入服务发现前就被拦截。

#### 3.5.2 白名单策略

```yaml
nova:
  auth:
    white-list:
      - /api/user/users/register
      - /api/user/users/login
      - /health/**
      - /actuator/**
```

- Ant 路径匹配（`AntPathMatcher`），`**` 匹配多级，`*` 匹配一级。
- 白名单路径是 **网关收到的原始 path**（StripPrefix 之前），不要用下游服务内部的路径。
- 调试期可以 `nova.auth.enabled=false` 一键关闭整个过滤器。

#### 3.5.3 透传用户身份

鉴权通过后，把用户信息放到请求头传给下游：

```
X-User-Id:   1
X-User-Name: nova_demo
```

下游服务 **不再自己解析 Token**，直接读这两个头即可（常量统一在 `CommonConstant`）。

> ⚠️ 重要安全点：过滤器开头 **先把客户端塞进来的 `X-User-Id / X-User-Name` 主动剥掉**，否则攻击者可以绕过网关，直接伪造身份头命中走内网的下游服务：
> ```java
> ServerHttpRequest sanitized = request.mutate()
>         .headers(h -> {
>             h.remove(CommonConstant.HEADER_USER_ID);
>             h.remove(CommonConstant.HEADER_USERNAME);
>         })
>         .build();
> ```
> 生产环境下游服务也应该只接受内网 IP 访问，双保险。

#### 3.5.4 失败响应

校验失败统一返回 HTTP 401 + `Result` 结构：

```json
{ "code": 1004, "message": "Token 无效", "data": null, "timestamp": ... }
{ "code": 1005, "message": "Token 已过期", "data": null, "timestamp": ... }
{ "code": 401,  "message": "缺少 Token",  "data": null, "timestamp": ... }
```

过期与无效分两个业务码，前端可以根据 `1005` 引导用户刷新 Token。

---

## 四、接口验证

### 1. 启动顺序

```bash
# 1. 启动 Nacos
# 2. 启动业务服务
mvn -pl nova-common install -DskipTests
mvn -pl nova-user    spring-boot:run
mvn -pl nova-gateway spring-boot:run
```

### 2. 走网关登录（白名单）

```bash
curl -X POST http://127.0.0.1:8080/api/user/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nova_demo","password":"Passw0rd!"}'
```

响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "nova_demo",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJub3ZhLWNoYXQi...",
    "expiresAt": 1745813520000
  }
}
```

### 3. 不带 Token 访问受保护接口

```bash
curl -X POST http://127.0.0.1:8080/api/user/users/logout
# 401 { "code": 401, "message": "缺少 Token" }
```

### 4. 带 Token 访问

```bash
TOKEN="eyJ..."
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer $TOKEN"
# 网关解析成功 → 下游能从 X-User-Id 头拿到用户
```

### 5. 故意传错 Token

```bash
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer invalid.jwt.here"
# 401 { "code": 1004, "message": "Token 无效" }
```

### 6. 篡改 Token（验签必失败）

```bash
# 把合法 token 最后一段改一位
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer eyJhbGciOi...xxxTamperedSignature"
# 401 { "code": 1004, "message": "Token 无效" }
```

### 7. 验证客户端不能伪造 X-User-Id

```bash
curl http://127.0.0.1:8080/api/user/users/me \
  -H "X-User-Id: 99999"
# 401（因为没带合法 Token，网关不会放行，也不会让 X-User-Id 透传到下游）
```

---

## 五、踩坑清单

| 现象                                                         | 原因                                                                                   | 解决                                                                                     |
| ------------------------------------------------------------ | -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| Gateway 启动报 `ClassNotFoundException: DispatcherServlet`   | 网关 scan common 时加载了 `GlobalExceptionHandler`                                     | `GlobalExceptionHandler` 加 `@ConditionalOnClass(DispatcherServlet.class)`               |
| Token 签发成功但网关校验失败                                 | 网关与 nova-user 的 `nova.jwt.secret` 不一致                                           | 两侧共用同一值，推荐放 Nacos 共享配置                                                     |
| `nova.jwt.secret` 启动报 "长度不足 32 字节"                  | HS256 对密钥长度有要求                                                                 | 用至少 32 字节字符串，最好用 `SecureRandom` 生成 base64                                   |
| 白名单路径不生效                                             | Ant 路径写错（用了 `/api/user/users/login/` 带尾斜杠 / 少写 `**`）                     | 对照 `ServerHttpRequest.getURI().getPath()` 打印值；用 `AntPathMatcher` 在线工具调试     |
| 下游拿到的 X-User-Id 是客户端伪造的                          | 过滤器没有剥除客户端传入的这两个头                                                     | `AuthGlobalFilter` 开头先 `headers.remove`，再写入真实值                                  |
| jjwt 报 `io.jsonwebtoken.security.WeakKeyException`          | 密钥强度不够                                                                           | 密钥 ≥ 32 字节；或改成 RSA/ES256                                                         |
| 升级 jjwt 后编译不通过，找不到 `SignatureAlgorithm.HS256`    | 0.12.x 去掉了 `SignatureAlgorithm`，改用 `Jwts.SIG.HS256`                              | 按 0.12.x 新 API 写                                                                      |
| Gateway 响应体里中文乱码                                     | 没设置 Content-Type                                                                    | `response.getHeaders().setContentType(MediaType.APPLICATION_JSON)` + UTF-8 字节          |

---

## 六、Day 7 收尾 Checklist

- [x] common 新增 `JwtService + JwtProperties`，HS256 签发/校验跑通
- [x] `GlobalExceptionHandler` 加 `@ConditionalOnClass`，网关扫 common 不再炸
- [x] nova-user 登录接入真 JWT，`LoginVO.token` 从占位 UUID 改为 JWT
- [x] nova-gateway 接入 `AuthGlobalFilter`，白名单放行 / 解析 Token / 透传用户
- [x] 网关开头剥除客户端伪造的 `X-User-Id / X-User-Name`（防越权）
- [x] 过期 vs 无效分两个业务码（1005 / 1004），前端可单独处理

---

## 七、下一步（Day 8 预告）

- 下游接口从 `X-User-Id` 头拿当前用户（封装 `@CurrentUser` 注解 + `HandlerMethodArgumentResolver`）
- 引入 Redis：Token 黑名单 / 主动登出、同一用户多端踢下线
- 限流：网关 `RequestRateLimiter` + Redis，按 `userId` 维度
- 准备 AI 聊天模块 `nova-ai`：会话、消息表已经在 Day 4 schema 里，马上可以接 LangChain
