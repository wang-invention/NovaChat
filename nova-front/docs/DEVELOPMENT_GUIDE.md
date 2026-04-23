# ChatNova 开发文档

> AI 聊天 · 社交 · 成长平台
> 技术栈：UniApp + Vue3 + Vite + Uni UI
> 更新日期：2026-04-22

---

## 一、项目结构

```
nova-front/
├── src/
│   ├── api/
│   │   └── user.js              # 用户相关API
│   ├── components/
│   │   └── cn-icon/             # 图标组件
│   ├── pages/
│   │   ├── login/index.vue      # 登录页
│   │   ├── register/index.vue   # 注册页
│   │   └── home/index.vue       # 首页
│   ├── utils/
│   │   └── request.js           # 请求封装
│   ├── App.vue                  # 应用根组件
│   ├── main.js                  # 应用入口
│   ├── manifest.json            # UniApp配置
│   ├── pages.json               # 路由配置
│   └── uni.scss                 # 全局样式变量
├── docs/                        # 开发文档
├── package.json
└── vite.config.js
```

---

## 二、页面说明

### 2.1 登录页 (pages/login/index.vue)

**功能特性：**
- 顶部导航栏：返回按钮 + 页面标题"登录"
- 手机号输入框（带+86前缀）
- 密码输入框（带眼睛显示/隐藏功能）
- 登录按钮（微信绿填充，输入为空时禁用变灰）
- 忘记密码链接（灰色文字，可点击跳转）
- 底部"没有账号？立即注册"链接（微信绿文字，可跳转注册页）
- 错误提示（手机号格式错误、密码错误时显示）

**设计规范（微信风格）：**
- 页面背景：`#EFEFF4`
- 卡片背景：`#FFFFFF`
- 圆角：卡片 `12rpx`，按钮 `8rpx`，输入框 `8rpx`
- 主色调：`#07C160`
- 标题：`32rpx` 加粗，正文：`28rpx`，辅助：`24rpx`
- 行高：`1.5`
- 页面左右边距：`40rpx`
- 导航栏高度：`88rpx`

**组件状态：**
| 状态 | 背景 | 文字 | 边框 | 阴影 |
|------|------|------|------|------|
| Default | `#07C160` | `#FFFFFF` | none | none |
| Hover | `#06AD56` | `#FFFFFF` | none | none |
| Active | `#059A4C` | `#FFFFFF` | none | none |
| Disabled | `#E5E5E5` | `#999999` | none | none |

---

### 2.2 注册页 (pages/register/index.vue)

**功能特性：**
- 3步骤流程：验证手机 → 设置密码 → 完成注册
- 顶部步骤条：当前步骤微信绿高亮，线条灰色
- 手机号输入框带+86前缀
- 验证码输入框带60秒倒计时获取按钮
- 密码输入框带显示/隐藏切换
- 密码强度检测（弱/中/强三级）
- 圆形勾选框协议确认
- 按钮状态联动（未勾选协议时禁用变灰）
- 可点击的《用户协议》《隐私政策》链接

**设计规范（微信风格）：**
- 页面背景：`#EFEFF4`
- 卡片背景：`#FFFFFF`
- 圆角：卡片 `16rpx`，按钮 `8rpx`，输入框 `8rpx`
- 主色调：`#07C160`
- 标题：`32rpx` 加粗，正文：`28rpx`，提示：`24rpx`
- 行高：`1.5`
- 页面左右边距：`40rpx`
- 输入框上下间距：`24rpx`
- 按钮距底部：`40rpx`

**步骤条样式：**
- 圆形直径：`56rpx`
- 默认状态：背景 `#E5E5E5`，文字 `#999999`
- 激活状态：背景 `#07C160`，文字 `#FFFFFF`
- 连接线宽度：`80rpx`，高度 `2rpx`

**协议勾选框：**
- 圆形直径：`36rpx`
- 边框：`2rpx solid #E5E5E5`
- 选中时：背景 `#07C160`，边框 `#07C160`

**组件状态：**
| 状态 | 背景 | 文字 | 边框 | 阴影 |
|------|------|------|------|------|
| Default | `#07C160` | `#FFFFFF` | none | none |
| Hover | `#06AD56` | `#FFFFFF` | none | none |
| Active | `#059A4C` | `#FFFFFF` | none | none |
| Disabled | `#E5E5E5` | `#999999` | none | none |

---

### 2.3 首页 (pages/home/index.vue)

**功能特性：**
- 顶部导航栏：App名称 + 搜索栏 + 加号按钮
- 聊天列表：复刻微信消息列表布局
- 消息项组件化封装（ChatListItem）
- 支持单聊头像、群聊四格头像、图标头像三种类型
- 未读消息红色角标（数字/红点）
- 下拉刷新、上拉加载更多
- 底部TabBar：聊天/通讯录/发现/我的
- Tab切换联动效果

**设计规范（微信风格）：**
- 页面背景：`#F5F5F5`
- 卡片背景：`#FFFFFF`
- 主色调：`#07C160`
- 未读角标：`#FF3B30`（红色）
- 标题：`36rpx` 加粗
- 联系人名称：`32rpx` 加粗
- 消息内容：`28rpx`
- 时间/辅助文字：`24rpx`
- Tab文字：`22rpx`
- 圆角：搜索框 `16rpx`，头像 `12rpx`
- 列表项高度：`160rpx`
- 头像尺寸：`100rpx`
- 页面边距：`20rpx`

**组件结构：**
```
home-page
├── nav-bar (顶部导航栏)
│   ├── nav-title (ChatNova)
│   ├── search-box (搜索框)
│   └── add-btn (加号按钮)
├── chat-list (聊天列表)
│   └── chat-list-item (消息项组件)
│       ├── avatar-wrap (头像区域)
│       │   ├── avatar (单聊头像)
│       │   ├── group-avatar (群聊四格头像)
│       │   ├── icon-avatar (图标头像)
│       │   └── unread-badge (未读角标)
│       └── content-wrap (内容区域)
│           ├── contact-name (联系人名称)
│           ├── message-preview (消息预览)
│           └── message-time (消息时间)
└── tab-bar (底部导航栏)
    └── tab-item (4个标签)
        ├── tab-icon (图标)
        ├── tab-badge (未读角标)
        └── tab-text (文字)
```

**ChatListItem 组件 Props：**
| 属性 | 类型 | 说明 |
|------|------|------|
| data.name | String | 联系人/群组名称 |
| data.avatarType | String | 头像类型：single/group/icon |
| data.avatar | String | 单聊头像URL |
| data.avatarList | Array | 群聊头像URL数组（最多4个） |
| data.iconBg | String | 图标头像背景色 |
| data.iconName | String | 图标名称：robot/file/subscription |
| data.lastMessage | String | 最后一条消息 |
| data.time | String | 消息时间 |
| data.unread | Number | 未读消息数 |
| hasBorder | Boolean | 是否显示底部分割线 |

**TabBar 状态：**
| 状态 | 图标颜色 | 文字颜色 |
|------|----------|----------|
| 未选中 | `#999999` | `#999999` |
| 选中 | `#07C160` | `#07C160` |

---

## 三、设计令牌 (Design Tokens)

### 3.1 语义化颜色

```css
:root {
  /* 主色 */
  --color-primary: #07C160;
  --color-primary-hover: #06AD56;
  --color-primary-active: #059A4C;
  --color-primary-foreground: #FFFFFF;

  /* 背景 */
  --color-background: #F7F8FA;
  --color-foreground: #111111;
  --color-card: #FFFFFF;

  /* 边框 */
  --color-border: #E5E5E5;

  /* 状态环 */
  --color-ring: rgba(7, 193, 96, 0.2);

  /* 文字 */
  --color-text-secondary: #666666;
  --color-text-placeholder: #999999;

  /* 危险色 */
  --color-danger: #FA5151;
  --color-danger-hover: #E04848;
}
```

### 3.2 间距系统

```css
:root {
  --space-1: 0.25rem;   /* 4px */
  --space-2: 0.5rem;    /* 8px */
  --space-3: 0.75rem;   /* 12px */
  --space-4: 1rem;      /* 16px */
  --space-5: 1.25rem;   /* 20px */
  --space-6: 1.5rem;    /* 24px */
  --space-8: 2rem;      /* 32px */
  --space-10: 2.5rem;   /* 40px */
  --space-12: 3rem;     /* 48px */
}
```

### 3.3 字体系统

```css
:root {
  --font-size-xs: 0.75rem;    /* 12px */
  --font-size-sm: 0.875rem;   /* 14px */
  --font-size-base: 1rem;     /* 16px */
  --font-size-lg: 1.125rem;   /* 18px */
  --font-size-xl: 1.25rem;    /* 20px */
  --font-size-2xl: 1.5rem;    /* 24px */

  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;

  --line-height-tight: 1.25;
  --line-height-normal: 1.5;
  --line-height-relaxed: 1.75;
}
```

### 3.4 圆角系统

```css
:root {
  --radius-sm: 0.5rem;    /* 8px */
  --radius-md: 0.75rem;   /* 12px */
  --radius-lg: 1rem;      /* 16px */
  --radius-xl: 1.5rem;    /* 24px */
  --radius-full: 9999px;
}
```

### 3.5 阴影系统

```css
:root {
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}
```

### 3.6 过渡动画

```css
:root {
  --duration-fast: 150ms;
  --duration-normal: 250ms;
  --duration-slow: 350ms;

  --transition-fast: all var(--duration-fast) ease-in-out;
  --transition-normal: all var(--duration-normal) ease-in-out;
  --transition-slow: all var(--duration-slow) ease-in-out;
}
```

---

## 四、组件状态

### 4.1 按钮状态

| 状态 | 描述 | 视觉表现 |
|------|------|----------|
| Default | 默认状态 | 主色背景，白色文字 |
| Hover | 鼠标悬停 | 背景变深 10%，添加阴影 |
| Active | 鼠标按下 | 背景变深 15%，无阴影 |
| Focus | 键盘焦点 | 添加 2px ring |
| Disabled | 禁用状态 | 背景 `#A5D7B4`，禁止光标 |
| Loading | 加载状态 | 显示加载动画，禁止交互 |

### 4.2 输入框状态

| 状态 | 描述 | 视觉表现 |
|------|------|----------|
| Default | 默认状态 | 灰色边框 `#E5E5E5` |
| Hover | 鼠标悬停 | 边框变深 `#CCCCCC` |
| Focus | 获得焦点 | 主色边框，主色 ring |
| Error | 错误状态 | 红色边框，红色文字提示 |
| Disabled | 禁用状态 | 灰色背景，禁止光标 |

### 4.3 导航项状态

| 状态 | 描述 | 视觉表现 |
|------|------|----------|
| Default | 默认状态 | 灰色文字 `#999999` |
| Hover | 鼠标悬停 | 灰色背景 |
| Active | 选中状态 | 主色文字 `#07C160`，主色图标 |

---

## 五、路由配置

```json
{
  "pages": [
    {
      "path": "pages/login/index",
      "style": {
        "navigationStyle": "custom",
        "navigationBarTextStyle": "black",
        "backgroundColor": "#F7F8FA"
      }
    },
    {
      "path": "pages/register/index",
      "style": {
        "navigationStyle": "custom",
        "navigationBarTextStyle": "black",
        "backgroundColor": "#F7F8FA"
      }
    },
    {
      "path": "pages/home/index",
      "style": {
        "navigationBarTitleText": "ChatNova",
        "navigationBarBackgroundColor": "#FFFFFF",
        "navigationBarTextStyle": "black",
        "backgroundColor": "#F7F8FA"
      }
    }
  ],
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#07C160",
    "backgroundColor": "#FFFFFF",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "pages/home/index",
        "text": "首页"
      }
    ]
  }
}
```

---

## 六、API 层

### 6.1 请求封装 (utils/request.js)

基于 `uni.request` 的统一请求封装，提供以下功能：
- 自动携带 Token
- 统一错误处理
- Toast 错误提示
- Promise 化返回

```javascript
// 请求示例
import { http } from "@/utils/request";

// GET 请求
http.get("/user/profile");

// POST 请求（带参数）
http.post("/user/login", { phone: "xxx", password: "xxx" });

// 禁用 Token
http.post("/user/login", payload, { auth: false });

// 禁用错误提示
http.post("/user/login", payload, { showError: false });
```

### 6.2 用户 API (api/user.js)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| loginByPassword | POST /user/login | 密码登录 | 否 |
| getProfile | GET /user/profile | 获取用户信息 | 是 |

---

## 七、图标规范

### 7.1 图标类型
- 使用 SVG 内联图标
- 图标尺寸：`24x24` (默认)
- 图标颜色：`currentColor`（继承父元素颜色）

### 7.2 图标列表

| 页面 | 图标名称 | 用途 |
|------|----------|------|
| 登录 | phone | 手机号输入 |
| 登录 | lock | 密码输入 |
| 登录 | eye/eye-off | 密码显示/隐藏 |
| 注册 | sms | 验证码 |
| 注册 | user | 用户名 |
| 注册 | lock | 密码 |
| 首页 | home | 首页导航 |
| 首页 | contacts | 通讯录 |
| 首页 | discover | 发现 |
| 首页 | profile | 我的 |

---

## 七、可访问性规范

### 7.1 焦点管理
- 所有可交互元素可通过 Tab 键导航
- 使用 `tabindex="0"` 启用焦点
- 焦点状态显示 `2px` ring

### 7.2 ARIA 属性
- 按钮使用 `role="button"`
- 图标使用 `aria-label` 提供描述
- 表单输入使用 `aria-invalid` 标记错误

### 7.3 键盘支持
- Enter 键触发点击事件
- Escape 键关闭弹窗/取消操作

---

## 九、待完成功能

- [x] API 层对接（登录、注册接口）
- [ ] Vuex Store 用户状态管理
- [ ] JWT Token 存储与刷新
- [ ] 错误处理与提示
- [ ] 页面间导航守卫
- [ ] 消息列表功能开发
- [ ] 个人中心页面

---

## 九、开发命令

```bash
# H5 开发
npm run dev:h5

# 微信小程序
npm run dev:mp-weixin

# APP 开发
npm run dev:app

# H5 构建
npm run build:h5

# 微信小程序构建
npm run build:mp-weixin

# APP 构建
npm run build:app
```

---

## 十一、注意事项

1. **Node.js 版本**：需要 Node.js 15+ 以支持 `??=` 运算符
2. **HBuilderX**：建议使用 HBuilderX 运行和打包项目
3. **样式规范**：所有页面遵循 UI/UX Pro Max 设计规范
4. **微信风格**：主色调 `#07C160`，圆角风格柔和

---

*文档版本：v1.0.0*
*最后更新：2026-04-22*
