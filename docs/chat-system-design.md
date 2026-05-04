# NovaChat 聊天系统设计与实现文档

## 一、整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 (UniApp + Vue3)                     │
│                                                               │
│  pages/chat/index.vue  ←→  api/chat.js  ←→  utils/message.js │
│       (聊天页面)            (API 层)          (数据模型层)      │
└──────────────────────────┬──────────────────────────────────┘
                           │ SSE / HTTP
                           │ vite proxy: /ai-api → :8000/api/v1
┌──────────────────────────▼──────────────────────────────────┐
│                  AI 服务 (FastAPI + Python)                    │
│                                                               │
│  main.py → api/v1/chat.py → services/ai_service.py           │
│  (入口)      (路由层)           (业务层 - Mock AI)              │
│                                                               │
│  models/schemas.py  ← 数据模型 (Pydantic)                     │
│  core/config.py     ← 配置管理                                 │
└──────────────────────────────────────────────────────────────┘
```

## 二、消息数据模型设计

### 2.1 消息类型枚举

```javascript
// src/utils/message.js
export const MSG_TYPE = {
  TEXT: "text",       // 文本消息
  IMAGE: "image",     // 图片消息
  EMOJI: "emoji",     // 大表情
  STICKER: "sticker", // 表情包（预留）
  SYSTEM: "system",   // 系统消息
};
```

**设计思路**：用枚举而非魔法字符串，避免拼写错误。`STICKER` 和 `EMOJI` 分开，因为大表情（如微信表情包）和小 Emoji 的渲染方式完全不同。

### 2.2 消息状态枚举

```javascript
export const MSG_STATUS = {
  SENDING: "sending",  // 发送中
  SENT: "sent",        // 发送成功
  FAILED: "failed",    // 发送失败
};
```

**设计思路**：三态模型覆盖了消息从发出到确认的完整生命周期。没有 `READ`（已读）状态，因为当前是 AI 对话场景，不需要已读回执。

### 2.3 消息对象结构

```javascript
{
  id: "msg_1714654321_1",   // 唯一 ID：时间戳 + 递增计数器
  role: "user",              // 角色：user / assistant / system
  type: "text",              // 消息类型：text / image / emoji / system
  content: "你好",            // 文本内容（图片消息为空）
  imageUrl: "",               // 图片 URL（仅 image 类型）
  status: "sent",             // 发送状态
  timestamp: 1714654321000,   // 毫秒时间戳
  recalled: false,            // 是否已撤回
  quoteId: null,              // 引用回复的消息 ID
  typing: false,              // 是否正在打字（仅 AI 流式回复时）
}
```

**设计思路**：
- `id` 用 `msg_` 前缀 + 时间戳 + 计数器，确保唯一性且可排序
- `role` 和 `type` 分离：role 决定消息在左侧还是右侧，type 决定渲染方式
- `quoteId` 存引用消息的 ID 而非内容，避免数据冗余，渲染时通过 `getQuoteMsg()` 查找
- `typing` 是临时状态，不持久化到存储

### 2.4 工厂函数

```javascript
createTextMsg(role, content, extra = {})   // 创建文本消息
createImageMsg(role, imageUrl, extra = {})  // 创建图片消息
createEmojiMsg(role, emojiKey, extra = {})  // 创建表情消息
createSystemMsg(content)                    // 创建系统消息
```

**设计思路**：工厂模式封装创建逻辑，自动填充 `id`、`timestamp`、`status` 等字段，`extra` 参数支持传入 `quoteId` 等扩展字段，避免每次手动拼对象。

## 三、核心功能实现

### 3.1 文字消息发送、接收、展示

**发送流程**：

```
用户输入 → sendTextMsg()
  ├─ 创建 user 消息 (status: sending)
  ├─ 推入 messages 数组
  ├─ 创建 assistant 消息 (typing: true, content: "")
  ├─ 调用 chatCompletionStream() 发起 SSE 请求
  │   ├─ onChunk: assistantMsg.content += chunk（逐字追加）
  │   ├─ onDone: typing = false, status = sent
  │   └─ onError: status = failed, 显示错误提示
  └─ user 消息 status → sent
```

**关键代码**：

```javascript
function sendTextMsg() {
  const text = inputText.value.trim();
  if (!text || isStreaming.value) return;

  const msg = createTextMsg("user", text, { quoteId: quoteMsg.value?.id || null });
  quoteMsg.value = null;
  messages.value.push(msg);
  inputText.value = "";
  scrollToBottom();

  msg.status = MSG_STATUS.SENT;
  requestAIReply(msg);
}
```

**SSE 流式接收**（H5 环境）：

```javascript
// api/chat.js - chatCompletionStream()
fetch(url, { method: "POST", body, signal })
  .then(response => {
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) { onDone(); return; }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop();  // 保留不完整的行

        for (const line of lines) {
          if (!line.startsWith("data: ")) continue;
          const data = line.slice(6);
          if (data === "[DONE]") { onDone(); return; }
          const parsed = JSON.parse(data);
          const content = parsed.choices?.[0]?.delta?.content || "";
          if (content) onChunk(content);
        }
        read();  // 递归读取下一块
      });
    }
    read();
  });
```

**设计要点**：
- 使用 `ReadableStream` + `TextDecoder` 逐块读取
- `buffer` 机制处理 SSE 数据可能被截断的情况（一个 `data:` 行可能跨两个 chunk）
- 返回 `abort` 函数，用户返回时可以取消请求
- 小程序环境降级为普通请求（`uni.request` 不支持 SSE）

### 3.2 Emoji 表情面板

**实现方式**：内置 120 个常用 Emoji，8 列网格布局，点击插入到输入框。

```javascript
const emojiList = [
  "😀","😁","😂", ...  // 120 个常用 Emoji
];

function insertEmoji(emoji) {
  inputText.value += emoji;  // 追加到输入框
}

function toggleEmojiPanel() {
  showEmojiPanel.value = !showEmojiPanel.value;
}
```

**设计要点**：
- Emoji 作为文本消息发送（`type: "text"`），而非独立的 `emoji` 类型
- 面板高度固定 420rpx，内部 `scroll-view` 支持滚动
- 输入框获焦时自动关闭面板，避免遮挡

### 3.3 图片发送

**实现方式**：调用 `uni.chooseImage` 选择图片，创建 `image` 类型消息。

```javascript
function pickImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    sourceType: ["album", "camera"],
    success: (res) => {
      const tempUrl = res.tempFilePaths[0];
      const msg = createImageMsg("user", tempUrl, { quoteId: quoteMsg.value?.id || null });
      quoteMsg.value = null;
      messages.value.push(msg);
      scrollToBottom();
      msg.status = MSG_STATUS.SENT;
      requestAIReply(msg);
    },
  });
}
```

**渲染**：

```html
<image class="bubble-image" v-else-if="msg.type === 'image'"
  :src="msg.imageUrl" mode="widthFix"
  @click="previewImage(msg.imageUrl)" />
```

**设计要点**：
- `mode="widthFix"` 保持图片比例，宽度自适应
- 点击图片调用 `uni.previewImage` 全屏预览
- 发给 AI 时图片消息的 content 转换为 `[图片]` 文本

### 3.4 消息发送状态

**三态展示**：

| 状态 | 视觉表现 | 交互 |
|------|---------|------|
| `sending` | 旋转 spinner | 无 |
| `sent` | 无指示器 | 无 |
| `failed` | 红色感叹号 + "重发" | 点击重发 |

```html
<view class="status-row" v-if="!msg.recalled">
  <view class="status-sending" v-if="msg.status === 'sending'">
    <view class="spinner"></view>
  </view>
  <view class="status-failed" v-else-if="msg.status === 'failed'" @click.stop="retrySend(msg)">
    <svg>...</svg>
    <text class="failed-text">重发</text>
  </view>
</view>
```

**重发逻辑**：

```javascript
function retrySend(msg) {
  msg.status = MSG_STATUS.SENDING;
  requestAIReply(msg);
}
```

### 3.5 消息时间戳与排序

**时间戳显示规则**：相邻两条消息间隔超过 5 分钟时显示时间。

```javascript
export function shouldShowTimestamp(msgs, index) {
  if (index === 0) return true;
  const prev = msgs[index - 1];
  const curr = msgs[index];
  return curr.timestamp - prev.timestamp > 5 * 60 * 1000;
}
```

**时间格式化**：

```javascript
export function formatTime(ts) {
  // 今天 → "14:30"
  // 昨天 → "昨天 14:30"
  // 今年 → "5月2日 14:30"
  // 更早 → "2025/5/2 14:30"
}
```

**排序**：消息按 `timestamp` 升序排列，新消息追加到数组末尾，`scrollToBottom` 自动滚动到底部。

### 3.6 下拉加载历史记录

**实现方式**：`scroll-view` 的 `@scrolltoupper` 事件。

```html
<scroll-view @scrolltoupper="onScrollToUpper">
  <view class="load-more-tip" v-if="hasMoreHistory">加载中...</view>
  <!-- 消息列表 -->
</scroll-view>
```

**本地存储**：

```javascript
export function saveChatHistory(chatId, messages) {
  const data = messages.filter((m) => !m.typing);  // 不存临时状态
  uni.setStorageSync("chat_history_" + chatId, JSON.stringify(data));
}

export function loadChatHistory(chatId) {
  const raw = uni.getStorageSync("chat_history_" + chatId);
  return raw ? JSON.parse(raw) : [];
}
```

**设计要点**：
- 按 `chatId` 隔离不同会话的聊天记录
- `typing` 状态不持久化（页面重载后不应有打字光标）
- 页面 `onLoad` 时加载历史，`onUnmounted` 时保存

### 3.7 消息撤回与删除

**撤回规则**：2 分钟内可撤回。

```javascript
export const RECALL_TIMEOUT = 2 * 60 * 1000;

export function canRecall(msg) {
  if (msg.recalled || msg.role === "system") return false;
  return Date.now() - msg.timestamp < RECALL_TIMEOUT;
}
```

**撤回实现**：

```javascript
function doRecall() {
  const msg = contextMenu.value.msg;
  if (!msg || !canRecall(msg)) return;
  msg.recalled = true;           // 标记为已撤回
  saveChatHistory(chatId.value, messages.value);
  closeContextMenu();
}
```

**渲染**：撤回后显示灰色提示文字，而非删除消息。

```html
<view class="recalled-tip" v-else>
  <text>你撤回了一条消息</text>
</view>
```

**删除实现**：直接从数组移除。

```javascript
function doDelete() {
  const idx = messages.value.findIndex((m) => m.id === msg.id);
  if (idx !== -1) {
    messages.value.splice(idx, 1);
    saveChatHistory(chatId.value, messages.value);
  }
}
```

**设计要点**：撤回 ≠ 删除。撤回保留消息占位（显示"已撤回"），删除则彻底移除。

### 3.8 引用回复

**数据结构**：消息的 `quoteId` 字段指向被引用消息的 `id`。

```javascript
const msg = createTextMsg("user", text, { quoteId: quoteMsg.value?.id || null });
```

**渲染**：气泡上方显示引用条。

```html
<view class="quote-bar" v-if="getQuoteMsg(msg)">
  <text class="quote-text">{{ getQuoteMsg(msg).content }}</text>
</view>
```

**查找引用**：

```javascript
function getQuoteMsg(msg) {
  if (!msg.quoteId) return null;
  return messages.value.find((m) => m.id === msg.quoteId);
}
```

**输入区引用预览**：

```html
<view class="quote-bar-wrap" v-if="quoteMsg">
  <view class="quote-bar-inner">
    <text class="quote-label">回复：</text>
    <text class="quote-preview">{{ quoteMsg.content || '[图片]' }}</text>
    <view class="quote-close" @click="quoteMsg = null">×</view>
  </view>
</view>
```

### 3.9 复制与转发

**复制**：调用 `uni.setClipboardData` 写入剪贴板。

```javascript
function doCopy() {
  uni.setClipboardData({
    data: contextMenu.value.msg.content,
    success: () => { uni.showToast({ title: "已复制", icon: "success" }); },
  });
}
```

**转发**：当前实现为复制到剪贴板 + 提示"可粘贴转发"。完整转发需要联系人选择器，后续实现。

### 3.10 聊天背景设置

**实现方式**：5 种预设背景色，通过 CSS 变量动态切换。

```javascript
const chatBg = ref("");

const pageBgStyle = computed(() => {
  if (!chatBg.value) return {};
  return { backgroundColor: chatBg.value };
});
```

```html
<view class="chat-page" :style="pageBgStyle">
```

**持久化**：

```javascript
export function saveChatBg(chatId, bg) {
  uni.setStorageSync("chat_bg_" + chatId, bg);
}
```

### 3.11 清空聊天记录

```javascript
function doClearHistory() {
  uni.showModal({
    title: "确认清空",
    content: "清空后聊天记录将无法恢复，确定要清空吗？",
    success: (res) => {
      if (res.confirm) {
        messages.value = [];
        clearChatHistory(chatId.value);
        showSettings.value = false;
      }
    },
  });
}
```

**设计要点**：二次确认弹窗防止误操作。

## 四、长按上下文菜单

**触发**：`@longpress="onMsgLongPress(msg)"`

**菜单项**：

| 操作 | 条件 | 说明 |
|------|------|------|
| 复制 | `msg.type === 'text'` | 仅文本消息可复制 |
| 引用 | `msg.type !== 'system'` | 非系统消息都可引用 |
| 转发 | `msg.type !== 'system'` | 非系统消息都可转发 |
| 撤回 | `canRecall(msg)` | 2 分钟内可撤回 |
| 删除 | `msg.type !== 'system'` | 非系统消息都可删除 |

**实现**：

```javascript
function onMsgLongPress(msg) {
  if (msg.type === MSG_TYPE.SYSTEM) return;
  contextMenu.value = { visible: true, msg, x: 60, y: 200 };
}
```

**遮罩层**：点击遮罩关闭菜单。

```html
<view class="context-mask" v-if="contextMenu.visible" @click="closeContextMenu">
  <view class="context-menu" @click.stop>
    <!-- 菜单项 -->
  </view>
</view>
```

## 五、前后端通信协议

### 5.1 请求格式

```
POST /api/v1/chat/completions/stream
Content-Type: application/json

{
  "messages": [
    { "role": "user", "content": "你好" },
    { "role": "assistant", "content": "你好！我是 Nova AI 助手" },
    { "role": "user", "content": "帮助" }
  ],
  "stream": true
}
```

### 5.2 SSE 响应格式

```
data: {"id":"chat-xxx","object":"chat.completion.chunk","created":1714654321,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"content":"我可以帮你"},"finish_reason":null}]}

data: {"id":"chat-xxx","object":"chat.completion.chunk","created":1714654321,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"content":"：\n1. "},"finish_reason":null}]}

data: [DONE]
```

### 5.3 Vite 代理配置

```javascript
// vite.config.js
proxy: {
  "/ai-api": {
    target: "http://127.0.0.1:8000",
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/ai-api/, "/api/v1"),
  },
}
```

前端请求 `/ai-api/chat/completions/stream` → 代理到 `http://127.0.0.1:8000/api/v1/chat/completions/stream`

## 六、文件结构

```
nova-front/src/
├── api/
│   ├── chat.js          # AI 聊天 API（SSE 流式 + 普通请求）
│   └── user.js          # 用户 API
├── utils/
│   ├── message.js       # 消息数据模型、工厂函数、存储工具
│   ├── request.js       # 统一请求封装
│   ├── auth.js          # 认证工具
│   └── device.js        # 设备信息
├── pages/
│   ├── chat/
│   │   └── index.vue    # 聊天页面（全功能）
│   ├── home/
│   │   └── index.vue    # 首页（聊天列表）
│   ├── login/
│   │   └── index.vue    # 登录页
│   └── ...
└── pages.json           # 页面路由配置

nova-ai-service/
├── main.py              # FastAPI 入口
├── start.py             # 启动脚本
├── app/
│   ├── api/v1/
│   │   ├── chat.py      # 对话路由
│   │   └── health.py    # 健康检查
│   ├── services/
│   │   └── ai_service.py # AI 服务（Mock）
│   ├── models/
│   │   └── schemas.py   # Pydantic 数据模型
│   └── core/
│       └── config.py    # 配置管理
└── requirements.txt     # Python 依赖
```

## 七、后续扩展方向

1. **WebSocket 实时通信**：替换 SSE，支持双向通信（打字指示器、在线状态）
2. **消息已读回执**：增加 `read` 状态，需要后端配合
3. **表情包系统**：`STICKER` 类型 + 表情包商店
4. **文件/语音消息**：扩展 `MSG_TYPE`，增加文件上传接口
5. **消息搜索**：本地全文搜索 + 后端搜索 API
6. **多端同步**：聊天记录云端存储，WebSocket 推送
7. **真正的 AI 接入**：替换 `ai_service.py` 中的 Mock，接入 OpenAI / 国产大模型 API
