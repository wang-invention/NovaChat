# Day 8 · Redis 登录态 / 主动登出 / 多端踢下线

> 目标：登录会话写入 Redis，实现主动登出、全部设备踢下线；网关在 JWT 验签后二次查 Redis，确认会话未被主动注销。

---

## 一、最终产出

```
nova-common/
├── pom.xml                                   # + spring-boot-starter-data-redis
└── src/main/java/com/wang/novachat/common/
    ├── config/
    │   └── RedisConfig.java                  # RedisTemplate 序列化配置（新增）
    └── security/
        ├── LoginSession.java                 # 登录会话 POJO（新增）
        ├── RedisKeys.java                    # Redis Key 常量（新增）
        ├── LoginSessionService.java          # 会话 CRUD 服务（新增）
        └── JwtService.java                   # + jti / deviceId 支持（修改）

nova-user/
├── pom.xml                                   # + nova-common
├── service/impl/UserServiceImpl.java         # 登录写 Redis / 登出（修改）
├── controller/UserController.java             # + logout / logout-all（修改）
├── resources/
│   ├── bootstrap.yml                          # + 引入 nova-redis.yaml 共享配置
│   └── application.yml                        # + nova.login.* 配置

nova-gateway/
├── pom.xml                                   # + nova-common + spring-boot-starter-data-redis-reactive
├── config/
│   └── ReactiveRedisConfig.java              # ReactiveStringRedisTemplate（新增）
│                                                连接工厂由 Spring Boot 自动配置
├── filter/AuthGlobalFilter.java              # JWT 验签后查 Redis（修改）
└── resources/
    ├── bootstrap.yml                          # + 引入 nova-redis.yaml 共享配置
    └── application.yml                       # + nova.jwt / nova.auth 配置

docs/
└── nacos/
    └── nova-redis.yaml                       # Nacos 共享配置参考样例
```

---

## 二、依赖与版本

父 pom 新增：

```xml
<spring-data-redis.version>3.2.5</spring-data-redis.version>
<lettuce.version>6.3.5.RELEASE</lettuce.version>
```

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>${spring-data-redis.version}</version>
</dependency>
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>${lettuce.version}</version>
</dependency>
```

nova-common 引入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

nova-gateway 引入（响应式 Redis，支持 WebFlux 非阻塞）：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

> 注意：Redis 连接工厂由 Spring Boot 自动配置（`RedisAutoConfiguration` / `RedisReactiveAutoConfiguration`），根据 `spring.data.redis.*` 属性自动创建，不需要手动定义。

---

## 三、Redis Key 设计

| Key 模式 | 用途 | TTL |
|----------|------|-----|
| `user:login:session:{userId}:{deviceId}` | 单个会话详情 | = JWT 过期秒数 |
| `user:login:token:{tokenId}` | tokenId → 会话信息（网关反查） | = JWT 过期秒数 |
| `user:login:devices:{userId}` | 用户活跃设备集合 | = JWT 过期秒数 |

所有 key 的 TTL 与 JWT `exp` 保持一致，Redis 过期 = 登录失效，天然对齐。

---

## 四、LoginSession 数据结构

```java
{
    "userId": 1,
    "username": "nova_demo",
    "deviceId": "550e8400-e29b-41d4-a716-446655440000",
    "deviceType": "iOS",
    "ip": "192.168.1.100",
    "loginAt": "2026-04-23T10:00:00",
    "expireAt": "2026-04-30T10:00:00",
    "tokenId": "jti-uuid"
}
```

---

## 五、JWT Claim 结构升级

```json
{
  "iss": "nova-chat",
  "sub": "1",
  "userId": 1,
  "username": "nova_demo",
  "jti": "550e8400-e29b-41d4-a716-446655440000",
  "deviceId": "device-uuid",
  "iat": 1745208720,
  "exp": 1745813520
}
```

- `jti`（JWT ID）：UUID，作为 Redis 反查 key，不再用整段 JWT
- `deviceId`：设备唯一标识

---

## 六、核心流程

### 6.1 登录流程

```
1. 账号密码校验通过
2. 生成 deviceId（请求头 X-Device-Id 或 UUID）
3. 生成 tokenId（UUID，jti）
4. 签发 JWT（包含 jti + deviceId）
5. 单设备模式：先 removeAll(userId) 踢掉旧会话
6. 写 Redis 三把 key
7. 返回 token + expiresAt
```

### 6.2 网关鉴权流程

```
1. 白名单放行
2. JWT 验签（HS256）
3. 解析 jti / deviceId
4. 查 Redis：KEY EXISTS user:login:token:{jti}
   - 不存在 → 1006 Token 已失效
   - 存在 → 透传 X-User-Id / X-User-Name / X-Device-Id
5. Redis 查询失败 → 降级放行（避免 Redis 挂了全站不可用）
```

### 6.3 登出流程

```
单端登出（POST /users/logout）：
1. 从请求头获取 userId + deviceId
2. 删除 user:login:session:{userId}:{deviceId}
3. 删除 user:login:token:{tokenId}
4. 删除 user:login:devices:{userId}

全部登出（POST /users/logout-all）：
1. 获取该用户所有 session key
2. 删除所有 tokenId 对应的 key
3. 删除所有 session key
4. 删除 devices key
```

---

## 七、业务码

| 码 | 含义 | 场景 |
|----|------|------|
| 1004 | Token 无效 | JWT 验签失败 |
| 1005 | Token 已过期 | JWT 过期 |
| 1006 | Token 已失效 | Redis 中会话不存在（已主动登出 / 被踢） |
| 1007 | 其他设备登录 | 单设备模式下被踢（Day9 实现） |

---

## 八、配置项

### 8.1 Nacos 共享配置（推荐）

在 Nacos（public / NOVA_GROUP）新建 `nova-redis.yaml`，Redis 参数只维护一份：

```yaml
# docs/nacos/nova-redis.yaml（仓库内参考样例）
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: ${NOVA_REDIS_PASSWORD:}
      database: 0
      timeout: 3s
      client-type: lettuce
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
          max-wait: 1s
        shutdown-timeout: 100ms
```

两个服务的 `bootstrap.yml` 引入共享配置：

```yaml
spring:
  cloud:
    nacos:
      config:
        shared-configs:
          - data-id: nova-redis.yaml
            group: NOVA_GROUP
            refresh: true
```

> 密码通过 `NOVA_REDIS_PASSWORD` 环境变量注入，不进仓库。

### 8.2 nova-user 登录模式

```yaml
nova:
  login:
    mode: ${NOVA_LOGIN_MODE:single}  # single | multi
    max-devices: ${NOVA_LOGIN_MAX_DEVICES:3}
```

- `single`：新登录会踢掉旧会话
- `multi`：允许多设备登录，上限 `max-devices`

---

## 九、接口验证

### 1. 启动 Redis

```bash
docker run -d --name nova-redis \
  -p 6379:6379 \
  -v /data/redis:/data \
  redis:7-alpine redis-server --appendonly yes
```

### 2. 启动服务

```bash
mvn -pl nova-common install -DskipTests
mvn -pl nova-user spring-boot:run
mvn -pl nova-gateway spring-boot:run
```

### 3. 登录拿 token

```bash
TOKEN=$(curl -s -XPOST http://127.0.0.1:8080/api/user/users/login \
  -d '{"username":"nova_demo","password":"Passw0rd!"}' \
  -H 'Content-Type: application/json' \
  -H 'X-Device-Id: test-device-001' \
  | jq -r .data.token)
echo $TOKEN
```

### 4. 正常访问 → 200

```bash
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### 5. 登出

```bash
curl -XPOST http://127.0.0.1:8080/api/user/users/logout \
  -H "Authorization: Bearer $TOKEN"
```

### 6. 再访问 → 401 TOKEN_REVOKED

```bash
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer $TOKEN"
# 1006 Token 已失效
```

### 7. Redis 验证

```bash
redis-cli KEYS 'user:login:*'
redis-cli TTL user:login:token:<jti>
```

---

## 十、踩坑清单

| 现象 | 原因 | 解决 |
|------|------|------|
| WebFlux 吞吐骤降 | 网关用了同步 `RedisTemplate` 阻塞 event loop | 用 `ReactiveStringRedisTemplate` |
| Jackson 反序列化 LocalDateTime 报错 | 未注册 `JavaTimeModule` | `RedisConfig` 中 `objectMapper.registerModule(new JavaTimeModule())` |
| `GenericJackson2JsonRedisSerializer` 的 `@class` 字段让 value 膨胀 | 类型信息写入 Redis value | 改用固定类型 `Jackson2JsonRedisSerializer<LoginSession>` 或手动序列化 |
| 单设备模式下旧端被踢时无感知 | 没有独立业务码 | 新增 `1007 OTHER_DEVICE_LOGIN`，前端弹"您的账号在其他设备登录" |
| JWT 过期但 Redis 还有 | TTL 来源不一致 | 统一用 `jwtService.getExpireSeconds()` 作为 TTL |
| Redis 查询失败导致全站 401 | 没有降级逻辑 | `onErrorResume` 降级放行 |

---

## 十一、Day 8 收尾 Checklist

- [x] 父 pom 新增 `spring-data-redis` / `lettuce-core` 版本管理
- [x] nova-common 引入 `spring-boot-starter-data-redis`
- [x] nova-gateway 引入 `spring-boot-starter-data-redis-reactive`
- [x] `RedisConfig` 配置 `RedisTemplate<String, Object>` 序列化方式（连接工厂由 Spring Boot 自动配置）
- [x] `ReactiveRedisConfig` 配置 `ReactiveStringRedisTemplate`（连接工厂由 Spring Boot 自动配置）
- [x] `LoginSession` POJO
- [x] `RedisKeys` 集中 key 常量
- [x] `LoginSessionService` 会话 CRUD
- [x] `JwtService` 增加 `jti` / `deviceId` claim
- [x] `UserServiceImpl#login` 写 Redis 三把 key
- [x] `UserServiceImpl#logout` / `logoutAll`
- [x] `UserController` 新增 logout / logout-all 接口
- [x] `AuthGlobalFilter` JWT 验签后查 Redis
- [x] `ResultCode` 新增 `TOKEN_REVOKED` / `OTHER_DEVICE_LOGIN`
- [x] Nacos 共享配置 `docs/nacos/nova-redis.yaml`
- [x] `bootstrap.yml` 引入 `nova-redis.yaml` 共享配置

---

## 十二、下一步（Day 9 预告）

- 滑动续期：每次请求在网关里调用 `EXPIRE` 续 TTL
- Refresh Token：另建 `user:login:refresh:{userId}:{deviceId}`，TTL = 7 天
- 限流：`RequestRateLimiter` + Redis，按 `userId` 维度
- 登录安全：登录失败次数限制 / 设备异常检测
