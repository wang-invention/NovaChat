# 多端登录详解：单 Token 校验 vs 多端登录

## 一、先理解你现在的方案（单 Token 校验）

### 1.1 现有流程

```
用户登录 → 服务端生成 JWT → 返回给客户端 → 客户端每次请求带上 Token → 网关校验 Token 签名/过期
```

### 1.2 问题

| 问题 | 说明 |
|------|------|
| **无法主动失效** | Token 签发后只要没过期就一直有效，无法手动登出 |
| **无法踢人** | 用户账号被盗后，无法远程让该账号在所有设备失效 |
| **无法限制多端** | 不能实现"同一账号只能同时在线 3 台设备" |
| **无设备感知** | 不知道用户当前登录在哪个设备 |

### 1.3 核心原因

JWT 是**自包含**的，签发后不依赖服务端存储。服务端只校验签名，不记录"这个 token 是不是有效"。

---

## 二、多端登录的核心思想

### 2.1 关键转变

```
之前：Token 有效 ↔ 签名正确 + 没过期
现在：Token 有效 ↔ 签名正确 + 没过期 + Redis 中会话存在
```

**增加一层 Redis 校验**：Token 本身没问题还不够，必须在 Redis 中有对应的会话记录。

### 2.2 三把 Key 的设计

```
user:login:session:{userId}:{deviceId}   → 会话详情（JSON）
user:login:token:{tokenId}               → "userId:deviceId"（用于反查）
user:login:devices:{userId}              → Set<deviceId>（用户所有设备）
```

### 2.3 形象理解

把 Redis 想象成一个**挂载在 JWT 上的开关**：

- JWT 告诉你"我是谁"
- Redis 告诉你"我的会话还在不在、能不能用"

---

## 三、登录流程对比

### 3.1 单 Token 校验（你现在的）

```java
// UserServiceImpl.login
String token = jwtService.issueToken(userId, username);
return new LoginVO(token);
```

### 3.2 多端登录（我们实现的）

```java
// UserServiceImpl.login
// 1. 生成 deviceId（从请求头 X-Device-Id 取，没有就 UUID）
String deviceId = StrUtil.blankToDefault(deviceIdHeader, IdUtil.fastSimpleUUID());

// 2. 生成 tokenId（UUID，作为 Redis key）
String tokenId = IdUtil.fastSimpleUUID();

// 3. 签发 JWT（包含 jti + deviceId）
String token = jwtService.issueToken(userId, username, tokenId, deviceId);

// 4. 单设备模式：先踢掉旧会话
if ("single".equals(loginMode)) {
    loginSessionService.removeAll(userId);
}

// 5. 写 Redis 三把 key
LoginSession session = LoginSession.builder()
        .userId(userId)
        .username(username)
        .deviceId(deviceId)
        .ip(loginIp)
        .loginAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().plusSeconds(ttlSeconds))
        .tokenId(tokenId)
        .build();
loginSessionService.save(session, ttlSeconds);
```

---

## 四、网关校验流程对比

### 4.1 单 Token 校验（你现在的）

```java
// AuthGlobalFilter
Claims claims = jwtService.parseClaims(token);  // JWT 验签
Long userId = jwtService.getUserId(claims);    // 取 userId
// 直接放行
```

### 4.2 多端登录（我们实现的）

```java
// AuthGlobalFilter
Claims claims = jwtService.parseClaims(token);  // JWT 验签
String tokenId = jwtService.getTokenId(claims); // 取 jti

// 新增：查 Redis
String redisKey = RedisKeys.token(tokenId);
Boolean exists = reactiveStringRedisTemplate.hasKey(redisKey);

if (!Boolean.TRUE.equals(exists)) {
    // Token 在 Redis 中不存在 = 已登出/被踢
    return writeUnauthorized(exchange, ResultCode.TOKEN_REVOKED, "Token 已失效");
}
// 存在才放行
```

---

## 五、登出流程对比

### 5.1 单 Token 校验

```
客户端删除本地 Token 即可，服务端什么都不用做
```

### 5.2 多端登录

```java
// 单端登出：删除这个设备的会话
void logout(Long userId, String deviceId) {
    // 1. 查 tokenId
    LoginSession session = getByUserAndDevice(userId, deviceId);
    // 2. 删 token key
    delete(token:tokenId);
    // 3. 删 session key
    delete(session:userId:deviceId);
    // 4. 删 devices key
    delete(devices:userId);
}

// 全部登出：删除该用户所有会话
void logoutAll(Long userId) {
    // 找到该用户所有 session key
    Set<String> keys = keys("session:" + userId + ":*");
    for (String key : keys) {
        delete(key);
    }
    delete(devices:userId);
}
```

---

## 六、单设备 vs 多设备模式

### 6.1 单设备模式

```
用户 A 在手机登录 → 用户 A 在电脑登录 → 手机被踢下线
```

**实现**：新登录时先调用 `removeAll(userId)` 踢掉旧会话

```java
if ("single".equals(loginMode)) {
    loginSessionService.removeAll(userId);
}
```

### 6.2 多设备模式

```
用户 A 在手机登录 → 用户 A 在电脑登录 → 两者都保持登录
用户 A 在平板登录 → 达到上限（max-devices=3）→ 需要踢掉一个设备
```

**实现**：不踢旧会话，直接追加

---

## 七、为什么需要 jti（JWT ID）

### 7.1 问题

- JWT 太长，不适合直接当 Redis key
- JWT 内容不可变（签发后改不了）

### 7.2 解决

在 JWT 中插入一个 `jti` 字段（UUID），作为 Redis key：

```json
{
  "iss": "nova-chat",
  "sub": "1",
  "userId": 1,
  "jti": "550e8400-e29b-41d4-a716-446655440000",
  "deviceId": "device-001",
  "iat": 1745208720,
  "exp": 1745813520
}
```

Redis key = `user:login:token:{jti}` = `user:login:token:550e8400-...`

---

## 八、时序图

### 8.1 登录并主动登出

```
Client          nova-user           Redis            Gateway
  |                 |                 |                 |
  |---登录请求------>|                 |                 |
  |                 |---写session----->|                 |
  |                 |---写token------->|                 |
  |                 |---写devices----->|                 |
  |<--返回token-----|                 |                 |
  |                 |                 |                 |
  |---访问受保护接口---------------->|                 |
  |                 |                 |<--hasKey验证----|
  |                 |                 |---true--------->|
  |                 |                 |                 |--->下游服务
  |<--响应----------|                 |                 |
  |                 |                 |                 |
  |---登出请求------>|                 |                 |
  |                 |---deltoken----->|                 |
  |                 |---delsession--->|                 |
  |<--成功----------|                 |                 |
  |                 |                 |                 |
  |---访问(旧token)->|                 |                 |
  |                 |                 |<--hasKey-------|
  |                 |                 |---false/不存在->|
  |<--401失效-------|                 |                 |
```

### 8.2 同一用户多设备登录

```
用户A-手机        用户A-电脑        Redis            Gateway
    |                |                 |                 |
    |--登录(id=m1)->|                 |                 |
    |               |---写session--->|                 |
    |               |---写token----->|                 |
    |<--token1------|                 |                 |
    |               |--登录(id=m2)->|                 |
    |               |---写session--->|                 |
    |               |---写token----->|                 |
    |<--token2------|                 |                 |
    |               |                 |                 |
    |--访问(token1)->|                 |                 |
    |               |               |<--hasKey=true-->|
    |               |               |---true--------->|
    |<--200---------|                 |                 |
    |               |--访问(token2)->|                 |
    |               |               |<--hasKey=true-->|
    |               |<--200----------|                 |
```

---

## 九、关键代码解读

### 9.1 LoginSessionService#save

```java
public void save(LoginSession session, long ttlSeconds) {
    // 三把 key 的 TTL 都设为 JWT 过期时间
    // 这样 Redis 过期 = JWT 过期，天然对齐
    stringRedisTemplate.opsForValue().set(
        token:session.tokenId,  // tokenKey → sessionData
        ttlSeconds
    );
    stringRedisTemplate.opsForValue().set(
        session:userId:deviceId,  // sessionKey → sessionData
        ttlSeconds
    );
    stringRedisTemplate.opsForValue().set(
        devices:userId,  // devicesKey → deviceId
        ttlSeconds
    );
}
```

### 9.2 网关 Redis 查询降级

```java
return reactiveStringRedisTemplate.hasKey(redisKey)
        .flatMap(exists -> {
            if (!Boolean.TRUE.equals(exists)) {
                return writeUnauthorized(exchange, TOKEN_REVOKED);
            }
            // 正常放行...
        })
        .onErrorResume(e -> {
            // Redis 挂了怎么办？
            // 降级放行：Token 签名没问题就放行，避免 Redis 故障导致全站不可用
            log.warn("Redis 查询失败，降级放行");
            return chain.filter(exchange);
        });
```

### 9.3 为什么要区分 TOKEN_REVOKED 和 TOKEN_EXPIRED

| 码 | 含义 | 前端处理 |
|----|------|----------|
| 1005 TOKEN_EXPIRED | JWT 本身过期了 | 引导用户重新登录 |
| 1006 TOKEN_REVOKED | JWT 有效但被主动登出/踢下线 | 提示"您的账号已退出登录" |

---

## 十、完整流程总结

```
┌─────────────────────────────────────────────────────────────────┐
│                            登录                                 │
├─────────────────────────────────────────────────────────────────┤
│  1. 账号密码校验                                                │
│  2. 生成 deviceId（请求头 / UUID）                              │
│  3. 生成 tokenId（UUID，作为 jti）                              │
│  4. 签发 JWT（包含 jti + deviceId）                             │
│  5. 单设备模式：removeAll 踢旧会话                               │
│  6. 写 Redis 三把 key（TTL = JWT 过期时间）                     │
│  7. 返回 token + expiresAt                                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         网关校验                                 │
├─────────────────────────────────────────────────────────────────┤
│  1. 白名单放行                                                  │
│  2. JWT 验签（签名 + 过期时间）                                  │
│  3. 解析 jti                                                    │
│  4. Redis EXISTS user:login:token:{jti}                        │
│     - 不存在 → 1006 Token 已失效                                │
│     - 存在 → 透传 X-User-Id / X-User-Name / X-Device-Id         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                           登出                                   │
├─────────────────────────────────────────────────────────────────┤
│  单端登出：                                                      │
│    DELETE user:login:session:{userId}:{deviceId}               │
│    DELETE user:login:token:{tokenId}                            │
│    DELETE user:login:devices:{userId}                           │
│                                                                 │
│  全部登出：                                                      │
│    DELETE user:login:session:{userId}:*  (所有设备)             │
│    DELETE user:login:token:*            (所有 token)            │
│    DELETE user:login:devices:{userId}                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 十一、Redis Key 设计总结

| Key | 存什么 | 用途 | TTL |
|-----|--------|------|-----|
| `user:login:session:{userId}:{deviceId}` | LoginSession JSON | 存会话详情 | JWT 过期秒数 |
| `user:login:token:{tokenId}` | "userId:deviceId" | 网关反查 | JWT 过期秒数 |
| `user:login:devices:{userId}` | deviceId | 快速查用户有哪些设备 | JWT 过期秒数 |

---

## 十二、常见问题

### Q1: 为什么不直接用 userId 当 key？

因为同一用户可能多端登录，需要区分不同设备。

### Q2: 为什么需要 token key？session key 不够吗？

不够。网关只知道 token，不知道 deviceId。token key 存 "userId:deviceId"，方便反查。

### Q3: Redis 挂了怎么办？

网关做了降级：Redis 查询失败时，只要 JWT 签名正确就放行。代价是短期内无法主动让 Token 失效。

### Q4: JWT 过期了 Redis 还没删怎么办？

TTL 统一用 `jwtService.getExpireSeconds()`，两者同时过期。如果发现不一致，以 Redis 为准（Redis 是实时过期的）。

### Q5: 多端登录上限怎么实现？

目前代码未实现完整版。完整版需要在 save 之前检查 `SCARD devices:{userId}`，超过上限返回错误或踢掉最早的设备。
