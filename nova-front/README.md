# ChatNova

ChatNova 是一款「AI 聊天 · 社交 · 成长」平台的移动端应用，使用 **UniApp + Vue3 + Vite + SCSS** 技术栈构建，支持 H5、微信小程序、App 多端发布。

---

## 技术栈

- UniApp（`@dcloudio/*`）
- Vue 3（`<script setup>`）
- Vite 5
- SCSS（`lang="scss"` + 全局 `uni.scss` 变量）
- rpx 单位移动端适配

---

## 目录结构

```
nova-front/
├── index.html              # Vite 入口（H5）
├── package.json
├── vite.config.js          # 含 /api 代理
├── jsconfig.json           # @/* 路径别名
├── .gitignore
├── LOGIN_UI_STYLE_NOTES.md # UI 设计风格总结（必读）
└── src/
    ├── main.js             # createSSRApp 入口
    ├── App.vue             # 全局样式、生命周期
    ├── pages.json          # 路由 & tabBar
    ├── manifest.json       # 多端配置（H5 / 小程序 / App）
    ├── uni.scss            # 全局设计 Token
    ├── api/
    │   └── user.js         # 用户接口（含登录）
    ├── utils/
    │   └── request.js      # 统一请求封装（token、错误处理）
    └── pages/
        ├── login/index.vue # 登录页
        └── home/index.vue  # 首页（tabBar）
```

---

## 启动

```bash
# 安装依赖
npm install

# 运行（H5）
npm run dev:h5

# 运行（微信小程序，需 HBuilderX / 微信开发者工具）
npm run dev:mp-weixin

# 打包
npm run build:h5
npm run build:mp-weixin
```

H5 开发服务器默认端口：`5173`，并将 `/api` 代理到 `http://127.0.0.1:8080`（可在 `vite.config.js` 中修改）。

---

## 后端接口对接

登录接口位于 `src/api/user.js`：

```js
loginByPassword({ phone, password })
// POST /api/user/login
```

后端约定响应格式（二选一即通过）：

```json
{ "code": 0, "data": { "token": "xxx", "userInfo": { ... } } }
// 或
{ "success": true, "data": { "token": "xxx" } }
```

统一请求封装 `src/utils/request.js`：
- 自动拼接 `/api` 前缀
- 自动附带 `Authorization: Bearer <token>`
- 失败自动 Toast

---

## UI 设计规范

所有新增页面 **必须** 遵循 [`LOGIN_UI_STYLE_NOTES.md`](./LOGIN_UI_STYLE_NOTES.md) 中的设计体系：

- 主色 `#07C160`，背景 `#F7F8FA`
- 圆角：输入框 `20rpx`，卡片 `32rpx`，主按钮 `999rpx`
- 渐变按钮：`linear-gradient(135deg,#07C160,#10D876)`
- 文字：主 `#111`，次 `#666`，占位 `#BBB`
- 阴影柔和：`0 18rpx 48rpx rgba(17,17,17,.06)`
- 保持「高留白 + 强主按钮 + 弱化次操作」的信息层级

全局 SCSS 变量见 `src/uni.scss`（`$cn-primary`、`$cn-bg`、`$cn-radius-lg` 等）。

---

## 已实现页面

| 页面 | 路径 | 说明 |
| --- | --- | --- |
| 登录页 | `/pages/login/index` | 手机号 + 密码，含校验、密码显隐、按压动画、协议勾选 |
| 首页 | `/pages/home/index` | tabBar 首页，展示登录状态、退出登录 |

---

## 后续规划

- 注册页 / 验证码登录页 / 找回密码页（沿用同一视觉体系）
- 聊天列表、聊天详情（AI + 社交）
- 个人中心 / 成长体系
