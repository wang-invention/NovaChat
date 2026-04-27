# Day 9 · 用户信息接口 + 登录态验证

> 目标：完成 `/users/me` 获取当前用户信息接口，完善登录态验证链路。

---

## 一、接口说明

### 1.1 获取当前用户信息

```
GET /api/user/users/me
Authorization: Bearer <token>
```

**请求头**：

| 头 | 必须 | 说明 |
|----|------|------|
| Authorization | 是 | Bearer Token（JWT） |
| X-Device-Id | 否 | 设备 ID（用于标记当前设备） |

**响应**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "nova_demo",
    "nickname": "Nova Demo",
    "avatar": "https://cdn.novachat.example.com/avatar/default.png",
    "phone": "138****8888",
    "email": "nova@example.com",
    "gender": 1,
    "status": 1,
    "lastLoginTime": "2026-04-23T10:00:00",
    "createTime": "2026-04-01T08:00:00"
  },
  "timestamp": 1745404800000
}
```

---

## 二、登录态验证流程

### 2.1 完整链路

```
客户端                    网关                        业务服务
  |                        |                            |
  |--GET /users/me------->|                            |
  |  Authorization:       |                            |
  |  Bearer eyJhbGci...    |                            |
  |                        |--JWT 验签----------------->|
  |                        |<--验签通过-----------------||
  |                        |--Redis EXISTS token:{jti}->|
  |                        |<--true--------------------||
  |                        |--写头 X-User-Id=1-------->||
  |                        |   X-User-Name=nova_demo   |
  |                        |   X-Device-Id=device-001 |
  |                        |                            |---查询数据库
  |                        |                            |<--返回 UserVO
  |<--200 UserVO----------|                            |
```

### 2.2 网关做了什么

1. **JWT 验签**：确认 Token 是我们签发的、没被篡改
2. **Redis 会话校验**：确认 Token 对应的会话没有被主动登出/踢下线
3. **透传用户身份**：把 `userId / username / deviceId` 写入请求头传给下游

### 2.3 业务服务做了什么

1. 从 `X-User-Id` 头获取当前用户 ID
2. 查询数据库获取完整用户信息
3. 返回 `UserVO`（脱敏后，不包含 password）

---

## 三、关键代码

### 3.1 UserController

```java
/**
 * 获取当前登录用户的基本信息。
 * 登录态由网关在 JWT 验签 + Redis 会话校验通过后，注入 X-User-Id 头。
 */
@GetMapping("/me")
public Result<UserVO> getCurrentUser(HttpServletRequest request) {
    Long userId = requireUserId(request);
    return Result.success(userService.getCurrentUser(userId));
}
```

### 3.2 UserServiceImpl

```java
@Override
public UserVO getCurrentUser(Long userId) {
    if (userId == null) {
        throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
    }
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BusinessException(ResultCode.USER_NOT_EXIST);
    }
    return toVO(user);
}
```

### 3.3 为什么不需要再验证 Token

因为网关已经验证过了：

| 验证项 | 网关做的 | 业务服务做的 |
|--------|----------|--------------|
| Token 格式正确 | ✅ | 不需要 |
| Token 签名正确 | ✅ | 不需要 |
| Token 没过期 | ✅ | 不需要 |
| Token 没被登出 | ✅ | 不需要 |
| 用户存在 | ❌ | ✅ 查数据库 |

---

## 四、业务码说明

| 码 | 含义 | 触发条件 |
|----|------|----------|
| 200 | 成功 | 正常返回 |
| 401 | 未登录 | `X-User-Id` 头为空或格式错误 |
| 404 | 用户不存在 | 数据库查不到该用户 |

---

## 五、验证命令

```bash
# 1. 登录获取 token
TOKEN=$(curl -s -XPOST http://127.0.0.1:8080/api/user/users/login \
  -H "Content-Type: application/json" \
  -H "X-Device-Id: test-device-001" \
  -d '{"username":"nova_demo","password":"Passw0rd!"}' \
  | jq -r .data.token)

# 2. 获取当前用户信息
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer $TOKEN"

# 3. 不带 token → 401
curl http://127.0.0.1:8080/api/user/users/me

# 4. 带已登出的 token → 401 TOKEN_REVOKED
# 先登出，再用同一个 token 访问
curl -XPOST http://127.0.0.1:8080/api/user/users/logout \
  -H "Authorization: Bearer $TOKEN"
curl http://127.0.0.1:8080/api/user/users/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## 六、下一步（Day 10 预告）

- `@CurrentUser` 注解 + `HandlerMethodArgumentResolver`：直接在 Controller 方法参数中获取当前用户
- 用户信息编辑 `/users/profile`
- 修改密码 `/users/password`
- 头像上传
