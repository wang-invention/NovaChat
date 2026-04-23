# ChatNova 接口文档 · 总览

> 面向前端（UniApp / Web / 任意客户端）的接口对接说明。
>
> 所有业务接口默认从 **网关** 进入，域名 + 端口以实际部署为准。本地联调默认网关地址为 `http://localhost:8080`。

---

## 一、环境与基础地址

| 环境 | Base URL | 说明 |
| :-- | :-- | :-- |
| 本地开发 | `http://localhost:8080` | 启动 `nova-gateway` 后访问 |
| 联调 / 预发 | 由后端约定后补充 | — |
| 生产 | 由后端约定后补充 | — |

所有接口都以 `/api/{模块}` 为前缀，网关会 `StripPrefix=1` 后转发到下游服务。

- 用户模块前缀：`/api/user`
- 因此前端实际调用的是：`{BaseURL}/api/user/...`

---

## 二、通用请求规范

1. 请求方式：统一使用 `POST`（涉及写操作）或 `GET`（只读），登录/注册均为 `POST`。
2. 请求体：JSON，`Content-Type: application/json; charset=utf-8`。
3. 鉴权：除白名单外，所有请求必须在请求头携带 `Authorization: Bearer {token}`。
4. 白名单（无需登录即可访问）：
   - `POST /api/user/users/register`
   - `POST /api/user/users/login`
   - `GET  /api/user/user/health/**`
   - `GET  /actuator/**`
5. 登录态：Token 由登录接口下发，默认有效期 **7 天**（`604800` 秒），过期需重新登录。

### 请求头示例

```
POST /api/user/users/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

> 提示：`Authorization` 字段前缀必须是 `Bearer `（带一个空格）。登录、注册接口不需要携带。

---

## 三、通用响应结构

后端统一返回 `Result<T>` 结构，HTTP 状态码大多为 `200`，业务成功/失败由 `code` 判定。

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { },
  "timestamp": 1714034400000
}
```

| 字段 | 类型 | 说明 |
| :-- | :-- | :-- |
| `code` | number | 业务状态码，`200` 表示成功，其它为失败 |
| `message` | string | 人类可读提示语，可直接用于 Toast |
| `data` | object / array / null | 业务数据体，失败时通常为 `null` |
| `timestamp` | number | 服务器时间戳（毫秒） |

### 前端判定建议

```js
if (res.code === 200) {
  // 成功
} else {
  uni.showToast({ title: res.message || '请求失败', icon: 'none' })
}
```

---

## 四、全局业务状态码

| code | 说明 | 场景举例 |
| :-- | :-- | :-- |
| 200 | 操作成功 | 正常响应 |
| 400 | 参数错误 | 校验失败、JSON 格式错误、缺参 |
| 401 | 未登录或登录已过期 | 网关鉴权失败，需重新登录 |
| 403 | 无权限访问 / 账号封禁 | — |
| 404 | 请求资源不存在 | — |
| 405 | 请求方法不支持 | 用 GET 调用 POST 接口等 |
| 500 | 系统繁忙 | 未知异常兜底 |
| 503 | 服务暂不可用 | — |
| **1001** | 用户不存在 | 登录时用户名不存在 |
| **1002** | 用户已存在 | 注册时用户名 / 手机号 / 邮箱重复 |
| **1003** | 账号或密码错误 | 登录密码错误 |
| **1004** | Token 无效 | 网关解析 JWT 失败 |
| **1005** | Token 已过期 | 需要重新登录 |

> 前端建议：遇到 `401 / 1004 / 1005` 统一清空本地 Token 并跳转登录页。

---

## 五、接口清单（用户模块）

| 序号 | 名称 | 方法 | 路径 | 是否鉴权 | 文档 |
| :-- | :-- | :-- | :-- | :-- | :-- |
| 1 | 用户注册 | POST | `/api/user/users/register` | 否 | [`用户-注册.md`](./用户-注册.md) |
| 2 | 用户登录 | POST | `/api/user/users/login` | 否 | [`用户-登录.md`](./用户-登录.md) |

> 后续新增接口（找回密码、短信验证码登录、获取个人信息等）统一追加到此清单并新增 `.md` 文件。

---

## 六、前端对接建议（UniApp）

1. 在 `utils/request.js` 中封装 `uni.request`：
   - 统一拼接 Base URL
   - 自动注入 `Authorization: Bearer ${token}`
   - 统一处理 `code !== 200` 的 Toast
   - 统一处理 `401 / 1004 / 1005` 登出逻辑
2. Token 存储建议：`uni.setStorageSync('token', data.token)`；同时可存 `expiresAt` 以便本地提前预判过期。
3. 登录成功后推荐：
   ```js
   uni.setStorageSync('token', data.token)
   uni.setStorageSync('userInfo', {
     userId: data.userId,
     username: data.username,
     nickname: data.nickname,
     avatar: data.avatar,
   })
   uni.switchTab({ url: '/pages/home/index' })
   ```

---

## 七、变更记录

| 日期 | 版本 | 变更内容 | 负责人 |
| :-- | :-- | :-- | :-- |
| 2026-04-22 | v1.0.0 | 初版：注册、登录接口文档 | 后端 |
