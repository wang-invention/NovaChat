# Day 6 · 用户注册登录 + 密码加密

> 目标：打通 `nova-user` 的数据层 → 服务层 → 接口层，跑通 **注册 / 登录** 两个最核心的接口，密码走 BCrypt。

---

## 一、最终产出

```
nova-user/
├── pom.xml                                          # + mybatis-plus-boot3 + mysql-connector-j
├── src/main/resources/application.yml               # + 数据源 + MP 配置
└── src/main/java/com/wang/novachat/user/
    ├── UserApplication.java                         # 已扫 common 包
    ├── config/
    │   ├── MybatisPlusConfig.java                   # 分页 + 乐观锁 + @MapperScan
    │   └── MyMetaObjectHandler.java                 # 自动填充 create_time / update_time
    ├── controller/
    │   ├── HealthController.java
    │   └── UserController.java                      # POST /users/register、/users/login
    ├── service/
    │   ├── UserService.java
    │   └── impl/UserServiceImpl.java
    ├── mapper/UserMapper.java
    ├── entity/User.java                             # 对齐 nova_user.t_user
    ├── dto/
    │   ├── UserRegisterDTO.java
    │   └── UserLoginDTO.java
    └── vo/
        ├── UserVO.java
        └── LoginVO.java

nova-common/（新增两个工具）
└── src/main/java/com/wang/novachat/common/utils/
    ├── PasswordUtils.java                           # BCrypt 封装
    └── IpUtils.java                                 # 真实客户端 IP
```

---

## 二、依赖与配置

### 2.1 pom 新增

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

版本由父 pom 的 BOM 管理（`mybatis-plus 3.5.5`、`mysql-connector-j` 由 Spring Boot 3.2.5 管理）。

### 2.2 application.yml 关键项

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/nova_user?...Asia/Shanghai
    username: root
    password: root

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      update-strategy: not_null
  mapper-locations: classpath*:/mapper/**/*.xml
```

> 真实环境把数据源放到 **Nacos 配置** 中，别提交到仓库。Day6 为了方便本地跑通，先直接写在 `application.yml`。

---

## 三、关键设计说明

### 3.1 密码加密：BCrypt

- `PasswordUtils.encode(raw)` 用 `cn.hutool.crypto.digest.BCrypt`，工作因子 10，内置随机盐，同一明文每次加密结果都不一样。
- `PasswordUtils.matches(raw, hash)` 走 `BCrypt.checkpw`。**禁止** 用 `equals` 比较两段密文。
- 数据库 `t_user.password` 用 `VARCHAR(128)`，BCrypt hash 是 60 字节，完全够用，后续若切到 Argon2/Scrypt 也能直接装。
- 服务器存的只有 hash，无法反推明文 —— 即便 DB 被拖走，攻击者也要暴力爆破 BCrypt，每次尝试约 100ms，不现实。

### 3.2 用户名查询防枚举

登录接口在 "用户名不存在 / 密码错误" 两种情况下本可以返回不同提示，但这会被黑产用来 **枚举账号**。
所以本项目策略：

| 场景             | 真实语义           | 对外返回                                |
| ---------------- | ------------------ | --------------------------------------- |
| 用户名不存在     | USER_NOT_EXIST     | 当前 Day6 返回 "用户不存在"，Day7 收口时统一为 "账号或密码错误" |
| 密码错误         | PASSWORD_ERROR     | "账号或密码错误"                        |
| 账号被封禁       | FORBIDDEN          | "账号已被封禁"                          |

> 登录错误日志在后端照常打印（便于风控），只是不暴露给前端。

### 3.3 注册幂等

- 入库前先 `exists` 检查 username / phone / email。
- **并发场景**：两个请求同名同时穿过检查，兜底用 `DuplicateKeyException`（DB 层唯一索引）捕获后重新抛 `USER_ALREADY_EXIST`。
- 因此数据库层的 `uk_username / uk_phone / uk_email` **不可省**，应用层 check 只是优化，唯一约束才是底线。

### 3.4 `MetaObjectHandler` 双保险

```java
@Override public void insertFill(MetaObject m) { strictInsertFill(..., "createTime", ..., now); }
```

数据库 `DEFAULT CURRENT_TIMESTAMP` 也会填充时间，但：
- **主从复制** 如果用 Row 模式没事；如果用 Statement 模式主从时钟不一致就出问题。
- **跨时区** 统一由应用层用 `LocalDateTime.now()` 更可控（配合 yml 里 `serverTimezone=Asia/Shanghai`）。
- 应用端填 + DB 默认兜底 = 双保险。

### 3.5 MP 拦截器顺序

```java
interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());   // 1. 乐观锁
interceptor.addInnerInterceptor(new PaginationInnerInterceptor(MYSQL));   // 2. 分页
```

**顺序敏感**：乐观锁在前，分页在后。反了会导致分页查询时的 update 语句丢失 `version = version + 1`。

### 3.6 Token 先占位

Day6 不做 JWT。`LoginVO.token` 字段用 `IdUtils.simpleUUID()` 占位，过期时间 7 天。
Day7 会换成：
1. JwtUtils 签发 JWT（携带 userId、username、过期时间）
2. Gateway 全局过滤器统一校验
3. 下游服务通过 `X-User-Id` 头获取当前用户

---

## 四、接口约定

### 4.1 注册

```
POST /user/users/register               （Gateway 外部路径 /api/user/users/register）
Content-Type: application/json

{
  "username": "nova_demo",
  "password": "Passw0rd!",
  "nickname": "小星",
  "phone":    "13812345678",
  "email":    "demo@novachat.io"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "nova_demo",
    "nickname": "小星",
    "avatar": "https://cdn.novachat.example.com/avatar/default.png",
    "status": 1,
    "createTime": "2026-04-21T10:12:00"
  },
  "timestamp": 1745208720000
}
```

失败（用户名已存在）：

```json
{ "code": 1002, "message": "用户已存在", "data": null, "timestamp": ... }
```

### 4.2 登录

```
POST /user/users/login
Content-Type: application/json

{ "username": "nova_demo", "password": "Passw0rd!" }
```

成功响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "nova_demo",
    "nickname": "小星",
    "avatar": "https://cdn.novachat.example.com/avatar/default.png",
    "token": "7c6a180b36896a0a8c02787eeafb0e4c",
    "expiresAt": 1745813520000
  },
  "timestamp": 1745208720000
}
```

失败：

```json
{ "code": 1003, "message": "账号或密码错误", "data": null, "timestamp": ... }
```

---

## 五、验证步骤

### 1. 准备 MySQL

```sql
-- 确保 Day4 的建表 SQL 已执行
USE nova_user;
SELECT * FROM t_user;   -- 应为空表
```

### 2. 启动服务

```bash
mvn -pl nova-common install -DskipTests
mvn -pl nova-user spring-boot:run
```

### 3. curl 验证

```bash
# 注册
curl -X POST http://127.0.0.1:8081/user/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"nova_demo","password":"Passw0rd!","nickname":"小星"}'

# 登录
curl -X POST http://127.0.0.1:8081/user/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nova_demo","password":"Passw0rd!"}'

# 登录（错密码，应返回 1003）
curl -X POST http://127.0.0.1:8081/user/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nova_demo","password":"wrong_pwd"}'

# 参数校验（密码过短，应走 @Valid -> 全局异常）
curl -X POST http://127.0.0.1:8081/user/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ab","password":"1"}'
```

### 4. DB 验证

```sql
SELECT id, username, LEFT(password, 10) AS pwd_prefix, nickname, status, create_time
FROM nova_user.t_user;
-- password 应该以 $2a$10$ 开头（BCrypt 版本头）
```

---

## 六、踩坑清单

| 现象                                                         | 原因                                                                     | 解决                                                                                     |
| ------------------------------------------------------------ | ------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------- |
| `Invalid bound statement (not found)`                        | MyBatis 找不到 Mapper                                                    | `MybatisPlusConfig` 加 `@MapperScan("com.wang.novachat.user.mapper")`                     |
| `create_time / update_time` 为 null                          | `@TableField(fill = ...)` 只是声明，没实现 handler                       | 实现 `MetaObjectHandler` + 加 `@Component`                                               |
| 分页和乐观锁冲突（version 不递增）                           | MP 拦截器顺序错了                                                        | 先注册 `OptimisticLockerInnerInterceptor`，再注册 `PaginationInnerInterceptor`           |
| BCrypt 校验 `false` but 明文是对的                           | 老数据是明文密码                                                         | `PasswordUtils.matches` 已把格式异常吞掉返回 false；运维脚本批量转成 BCrypt               |
| Hikari 报 `Public Key Retrieval is not allowed`              | MySQL 8 默认安全                                                         | URL 加 `allowPublicKeyRetrieval=true`（开发库）或换 SSL（生产）                            |
| `date/time conversion` 异常                                  | 时区问题                                                                 | URL 加 `serverTimezone=Asia/Shanghai`；Entity 用 `LocalDateTime` 不要用老 `Date`         |
| 并发注册两条同名数据                                         | 只靠应用层 `exists` 检查                                                 | DB `uk_username` 唯一键 + 捕获 `DuplicateKeyException` 兜底                              |

---

## 七、Day 6 收尾 Checklist

- [x] nova-user 接入 MyBatis-Plus，连通 `nova_user.t_user`
- [x] `User` entity 对齐 Day4 schema（`@TableLogic` / `@Version` / 自动填充）
- [x] `PasswordUtils` 基于 BCrypt，加密 & 校验双向跑通
- [x] `UserController` POST /users/register、/users/login 走 `@Valid` + 全局异常
- [x] 登录错误不暴露账号是否存在（安全策略）
- [x] 占位 `token` 字段 & 过期时间，Day7 换成 JWT

---

## 八、下一步（Day 7 预告）

- 引入 JJWT，`JwtUtils.issueToken` / `verifyToken`
- `nova-gateway` 全局过滤器 `AuthGlobalFilter`：白名单放行 `/users/register /users/login`，其余请求强制校验 JWT
- 透传 `X-User-Id` 给下游，下游不再自己解析 token
- 登录错误文案收口为"账号或密码错误"（防枚举）
