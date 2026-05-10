<template>
  <view class="chat-page" :style="pageBgStyle">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <svg-icon class="nav-icon" icon="<path d='M15 18L9 12L15 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
      </view>
      <view class="nav-center">
        <text class="nav-title">{{ chatName }}</text>
      </view>
      <view class="nav-more" @click="toggleSettings">
        <svg-icon class="nav-icon" icon="<circle cx='12' cy='5' r='1.5' fill='currentColor'/><circle cx='12' cy='12' r='1.5' fill='currentColor'/><circle cx='12' cy='19' r='1.5' fill='currentColor'/>" />
      </view>
    </view>


    <scroll-view
      class="message-list"
      scroll-y
      :scroll-into-view="scrollToId"
      :scroll-with-animation="true"
      @scrolltoupper="onScrollToUpper"
    >
      <view class="load-more-tip" v-if="loadingMore"><text>加载中...</text></view>
      <view class="message-list-inner">
        <view class="welcome-tip" v-if="messages.length === 0 && chatType === 'ai'">
          <view class="welcome-icon">
            <svg-icon icon="<rect x='3' y='11' width='18' height='10' rx='2' stroke='currentColor' stroke-width='2'/><path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2'/><circle cx='9' cy='16' r='1' fill='currentColor'/><circle cx='15' cy='16' r='1' fill='currentColor'/>" size="40" />
          </view>
          <text class="welcome-text">你好！我是 Nova AI 助手，有什么可以帮你的吗？</text>
        </view>

        <view v-for="(msg, index) in messages" :key="msg.id">
          <view class="time-tip" v-if="shouldShowTimestamp(messages, index)">
            <text class="time-text">{{ formatTime(msg.timestamp) }}</text>
          </view>

          <view v-if="msg.type === 'system'" class="system-msg">
            <text class="system-text">{{ msg.content }}</text>
          </view>

          <view
            v-else
            :id="`msg-${msg.id}`"
            class="message-item"
            :class="[msg.role, { recalled: msg.recalled }]"
            @longpress="onMsgLongPress(msg)"
          >
            <template v-if="msg.role === 'other' || msg.role === 'assistant'">
              <view class="avatar other-avatar" v-if="chatType === 'ai'">
                <svg-icon icon="<rect x='3' y='11' width='18' height='10' rx='2' stroke='currentColor' stroke-width='2'/><path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2'/><circle cx='9' cy='16' r='1' fill='currentColor'/><circle cx='15' cy='16' r='1' fill='currentColor'/>" size="40" />
              </view>
              <image v-else class="avatar-img" :src="targetAvatar || ''" mode="aspectFill" />
              <view class="bubble-wrap">
                <view class="quote-bar" v-if="getQuoteMsg(msg)">
                  <text class="quote-text">{{ getQuoteMsg(msg).content || '[图片]' }}</text>
                </view>
                <view class="bubble other-bubble" v-if="!msg.recalled">
                  <text class="bubble-text" v-if="msg.type === 'text'">{{ msg.content }}</text>
                  <view class="image-wrap" v-else-if="msg.type === 'image'">
                    <image class="bubble-image" :src="msg.imageUrl" mode="widthFix" @click="previewImage(msg)" @longpress="onImageLongPress(msg)" />
                    <view class="upload-overlay" v-if="msg.uploading">
                      <text class="upload-text">上传中...</text>
                    </view>
                    <view class="failed-overlay" v-else-if="msg.status === 'failed'">
                      <text class="failed-text">上传失败</text>
                    </view>
                  </view>
                  <text class="bubble-emoji" v-else-if="msg.type === 'emoji'">{{ msg.content }}</text>
                  <view class="typing-cursor" v-if="msg.typing"></view>
                </view>
                <view class="recalled-tip" v-else><text>消息已撤回</text></view>
              </view>
            </template>

            <template v-else-if="msg.role === 'user'">
              <view class="bubble-wrap">
                <view class="quote-bar" v-if="getQuoteMsg(msg)">
                  <text class="quote-text">{{ getQuoteMsg(msg).content || '[图片]' }}</text>
                </view>
                <view class="bubble user-bubble" v-if="!msg.recalled">
                  <text class="bubble-text" v-if="msg.type === 'text'">{{ msg.content }}</text>
                  <view class="image-wrap" v-else-if="msg.type === 'image'">
                    <image class="bubble-image" :src="msg.imageUrl" mode="widthFix" @click="previewImage(msg)" @longpress="onImageLongPress(msg)" />
                    <view class="upload-overlay" v-if="msg.uploading">
                      <text class="upload-text">上传中...</text>
                    </view>
                    <view class="failed-overlay" v-else-if="msg.status === 'failed'">
                      <text class="failed-text">上传失败</text>
                    </view>
                  </view>
                  <text class="bubble-emoji" v-else-if="msg.type === 'emoji'">{{ msg.content }}</text>
                </view>
                <view class="recalled-tip" v-else><text>你撤回了一条消息</text></view>
                <view class="status-row" v-if="!msg.recalled">
                  <view class="status-sending" v-if="msg.status === 'sending'"><view class="spinner"></view></view>
                  <view class="status-failed" v-else-if="msg.status === 'failed'" @click.stop="retrySend(msg)">
                    <svg-icon icon="<circle cx='12' cy='12' r='10' stroke='#ff3b30' stroke-width='2'/><path d='M12 8V12L15 15' stroke='#ff3b30' stroke-width='2' stroke-linecap='round'/>" size="16" color="#ff3b30" />
                    <text class="failed-text">重发</text>
                  </view>
                </view>
              </view>
              <image v-if="myAvatar" class="avatar-img" :src="myAvatar" mode="aspectFill" />
              <view v-else class="avatar user-avatar">
                <svg-icon icon="<path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><circle cx='12' cy='7' r='4' stroke='currentColor' stroke-width='2'/>" />
              </view>
            </template>
          </view>
        </view>

        <view id="msg-bottom" class="scroll-bottom"></view>
      </view>
    </scroll-view>

    <view class="quote-bar-wrap" v-if="quoteMsg">
      <view class="quote-bar-inner">
        <text class="quote-label">回复：</text>
        <text class="quote-preview">{{ quoteMsg.content || '[图片]' }}</text>
        <view class="quote-close" @click="quoteMsg = null">
          <svg-icon icon="<path d='M18 6L6 18M6 6l12 12' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="28" />
        </view>
      </view>
    </view>

    <view class="input-area">
      <view class="input-toolbar">
        <view class="tool-btn" @click="toggleEmojiPanel">
          <svg-icon icon="<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='2'/><path d='M8 14s1.5 2 4 2 4-2 4-2' stroke='currentColor' stroke-width='2' stroke-linecap='round'/><circle cx='9' cy='9' r='1' fill='currentColor'/><circle cx='15' cy='9' r='1' fill='currentColor'/>" />
        </view>
        <view class="tool-btn" @click="pickImage">
          <svg-icon icon="<rect x='3' y='3' width='18' height='18' rx='2' stroke='currentColor' stroke-width='2'/><circle cx='8.5' cy='8.5' r='1.5' stroke='currentColor' stroke-width='2'/><path d='M21 15l-5-5L5 21' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
        </view>
      </view>
      <view class="input-row">
        <input class="msg-input" v-model="inputText" placeholder="输入消息..." placeholder-class="input-placeholder" :disabled="isStreaming" confirm-type="send" @confirm="sendTextMsg" @focus="onInputFocus" />
        <view class="send-btn" :class="{ active: canSend }" @click="sendTextMsg">
          <svg-icon class="send-icon" icon="<path d='M22 2L11 13' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><path d='M22 2L15 22L11 13L2 9L22 2Z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
        </view>
      </view>
    </view>

    <view class="emoji-panel" v-if="showEmojiPanel">
      <scroll-view class="emoji-scroll" scroll-y>
        <view class="emoji-grid">
          <view class="emoji-item" v-for="emoji in emojiList" :key="emoji" @click="insertEmoji(emoji)">
            <text class="emoji-char">{{ emoji }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="context-mask" v-if="contextMenu.visible" @click="closeContextMenu">
      <view class="context-menu" :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }" @click.stop>
        <view class="ctx-item" @click="doCopy" v-if="contextMenu.msg?.type === 'text'"><text>复制</text></view>
        <view class="ctx-item" @click="doQuote" v-if="contextMenu.msg?.type !== 'system'"><text>引用</text></view>
        <view class="ctx-item" @click="doForward" v-if="contextMenu.msg?.type !== 'system'"><text>转发</text></view>
        <view class="ctx-item" @click="doRecall" v-if="contextMenu.msg && canRecall(contextMenu.msg)"><text>撤回</text></view>
        <view class="ctx-item ctx-danger" @click="doDelete" v-if="contextMenu.msg?.type !== 'system'"><text>删除</text></view>
      </view>
    </view>

    <view class="settings-mask" v-if="showSettings" @click="showSettings = false">
      <view class="settings-panel" @click.stop>
        <view class="settings-title">聊天设置</view>
        <view class="settings-item" @click="changeBg('')"><text>默认背景</text><view class="radio-dot" :class="{ active: !chatBg }"></view></view>
        <view class="settings-item" @click="changeBg('#e8f5e9')"><text>浅绿</text><view class="radio-dot" :class="{ active: chatBg === '#e8f5e9' }"></view></view>
        <view class="settings-item" @click="changeBg('#e3f2fd')"><text>浅蓝</text><view class="radio-dot" :class="{ active: chatBg === '#e3f2fd' }"></view></view>
        <view class="settings-item" @click="changeBg('#fff3e0')"><text>浅橙</text><view class="radio-dot" :class="{ active: chatBg === '#fff3e0' }"></view></view>
        <view class="settings-item" @click="changeBg('#f3e5f5')"><text>浅紫</text><view class="radio-dot" :class="{ active: chatBg === '#f3e5f5' }"></view></view>
        <view class="settings-divider"></view>
        <view class="settings-item ctx-danger" @click="doClearHistory"><text>清空聊天记录</text></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { chatCompletionStream } from "@/api/chat";
import { sendMessage, getMessages, recallMessage, deleteMessage, markRead, getConversationId } from "@/api/im";
import { uploadImage } from "@/api/user";
import { connectWS, sendWSMessage, onWSMessage } from "@/utils/websocket";
import { getStatusBarHeight } from "@/utils/safe-area";
import {
  MSG_TYPE, MSG_STATUS,
  createTextMsg, createImageMsg, createSystemMsg,
  canRecall, formatTime, shouldShowTimestamp,
  saveChatHistory, loadChatHistory, clearChatHistory,
  saveChatBg, loadChatBg,
} from "@/utils/message";

const statusBarHeight = ref(getStatusBarHeight());
const chatName = ref("");
const chatType = ref("ai");
const chatId = ref("ai_default");
const conversationId = ref(null);
const targetUserId = ref(null);
const targetAvatar = ref("");
const myAvatar = ref("");
const inputText = ref("");
const messages = ref([]);
const isStreaming = ref(false);
const scrollToId = ref("");
const showEmojiPanel = ref(false);
const showSettings = ref(false);
const chatBg = ref("");
const quoteMsg = ref(null);
const loadingMore = ref(false);
let abortFn = null;
let removeWSHandler = null;

const contextMenu = ref({ visible: false, msg: null, x: 0, y: 0 });

const emojiList = [
  "😀","😁","😂","🤣","😃","😄","😅","😆","😉","😊",
  "😋","😎","😍","🥰","😘","😗","😙","😚","🙂","🤗",
  "🤔","😐","😑","😶","🙄","😏","😣","😥","😮","🤐",
  "😯","😪","😫","😴","😌","😛","😜","😝","🤤","😒",
  "😓","😔","😕","🙃","🤑","😲","🙁","😖","😞","😟",
  "😤","😢","😭","😦","😧","😨","😩","🤯","😬","😰",
  "😱","🥵","🥶","😳","🤪","😵","😡","😠","🤬","😷",
  "🤒","🤕","🤢","🤮","🥴","😇","🥳","🥺","🤠","🤡",
  "👍","👎","👌","✌️","🤞","🤟","🤘","🤙","👋","🤚",
  "✋","🖖","👏","🙌","🤝","🙏","💪","❤️","🔥","⭐",
  "🎉","🎊","💯","✅","❌","⚡","🌈","☀️","🌙","🎵",
];

const canSend = computed(() => inputText.value.trim() && !isStreaming.value);
const pageBgStyle = computed(() => chatBg.value ? { backgroundColor: chatBg.value } : {});

let loadCount = 0;

onLoad((options) => {
  loadCount++;
  console.log("[Chat] onLoad called, count=", loadCount, "options=", options);
  if (options.name) chatName.value = decodeURIComponent(options.name);
  if (options.chatType) chatType.value = options.chatType;
  if (options.chatId) chatId.value = options.chatId;
  if (options.targetUserId) targetUserId.value = Number(options.targetUserId);
  if (options.targetAvatar) targetAvatar.value = decodeURIComponent(options.targetAvatar);
  if (options.conversationId) conversationId.value = Number(options.conversationId);

  // 获取自己的头像
  const me = uni.getStorageSync("userInfo");
  myAvatar.value = me?.avatar || "";

  // 连接 WebSocket
  if (me?.id || me?.userId) {
    connectWS(me.id || me.userId);
  }

  chatBg.value = loadChatBg(chatId.value);

  if (chatType.value === "single") {
    loadServerMessages();
    if (conversationId.value) {
      markRead(conversationId.value).catch(() => {});
    }
  } else {
    const history = loadChatHistory(chatId.value);
    if (history.length > 0) {
      messages.value = history;
      nextTick(() => scrollToBottom());
    }
  }
});

function serverMsgToLocal(serverMsg, role) {
  return {
    id: `srv_${serverMsg.id}`,
    serverId: serverMsg.id,
    role: role,
    type: serverMsg.type || "text",
    content: serverMsg.content || "",
    imageUrl: serverMsg.thumbUrl || serverMsg.imageUrl || "",
    thumbUrl: serverMsg.thumbUrl || "",
    originUrl: serverMsg.originUrl || serverMsg.imageUrl || "",
    status: MSG_STATUS.SENT,
    timestamp: serverMsg.createTime ? new Date(serverMsg.createTime).getTime() : Date.now(),
    recalled: serverMsg.recalled === 1,
    quoteId: serverMsg.quoteId ? `srv_${serverMsg.quoteId}` : null,
  };
}

async function loadServerMessages() {
  try {
    console.log("[Chat] loadServerMessages called");
    if (!conversationId.value && targetUserId.value) {
      const res = await getConversationId(targetUserId.value);
      conversationId.value = res.data;
    }
    if (!conversationId.value) return;

    const res = await getMessages({ conversationId: conversationId.value, size: 50 });
    const serverMsgs = res.data || [];
    console.log("[Chat] server messages count:", serverMsgs.length);
    const me = uni.getStorageSync("userInfo");
    const myId = me?.id || me?.userId;

    messages.value = serverMsgs.map((m) => {
      const role = m.senderId === myId ? "user" : "other";
      return serverMsgToLocal(m, role);
    });
    nextTick(() => scrollToBottom());
  } catch (e) {
    console.error("loadServerMessages failed:", e);
  }
}

function scrollToBottom() {
  nextTick(() => {
    scrollToId.value = "";
    setTimeout(() => { scrollToId.value = "msg-bottom"; }, 50);
  });
}

function goBack() {
  if (abortFn) { abortFn(); abortFn = null; }
  if (chatType.value === "ai") {
    saveChatHistory(chatId.value, messages.value);
  }
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({ url: "/pages/home/index" });
  }
}

function toggleSettings() { showSettings.value = !showSettings.value; showEmojiPanel.value = false; }
function toggleEmojiPanel() { showEmojiPanel.value = !showEmojiPanel.value; }
function onInputFocus() { showEmojiPanel.value = false; }
function insertEmoji(emoji) { inputText.value += emoji; }

let isSending = false;

async function sendTextMsg() {
  const text = inputText.value.trim();
  if (!text || isStreaming.value || isSending) return;

  isSending = true;
  const msg = createTextMsg("user", text, { quoteId: quoteMsg.value?.id || null });
  quoteMsg.value = null;
  messages.value.push(msg);
  inputText.value = "";
  showEmojiPanel.value = false;
  scrollToBottom();

  try {
    if (chatType.value === "ai") {
      msg.status = MSG_STATUS.SENT;
      requestAIReply(msg);
    } else {
      await sendSingleChatMsg(msg);
    }
  } finally {
    isSending = false;
  }
}

function pickImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    sourceType: ["album", "camera"],
    success: (res) => {
      const tempUrl = res.tempFilePaths[0];
      const msg = createImageMsg("user", tempUrl, { quoteId: quoteMsg.value?.id || null });
      msg.status = MSG_STATUS.SENDING;
      msg.uploading = true;
      quoteMsg.value = null;
      messages.value.push(msg);
      scrollToBottom();

      uploadImage(tempUrl)
        .then((result) => {
          const thumbUrl = result.data?.thumbUrl || result.thumbUrl || result.data?.url || result.url;
          const originUrl = result.data?.url || result.url;
          msg.thumbUrl = thumbUrl;
          msg.imageUrl = thumbUrl;
          msg.originUrl = originUrl;
          msg.uploading = false;
          msg.status = MSG_STATUS.SENT;

          if (chatType.value === "ai") {
            requestAIReply(msg);
          } else {
            sendSingleChatMsg(msg);
          }
        })
        .catch((err) => {
          console.error('图片上传失败', err);
          msg.uploading = false;
          msg.status = MSG_STATUS.FAILED;
          uni.showToast({ title: "图片上传失败", icon: "none" });
        });
    },
    fail: () => {},
  });
}

async function sendSingleChatMsg(localMsg) {
  try {
    if (!targetUserId.value) {
      console.error("targetUserId is empty");
      localMsg.status = MSG_STATUS.FAILED;
      uni.showToast({ title: "发送失败：未找到接收人", icon: "none" });
      return;
    }

    // 直接通过 WebSocket 发送，后端会自动保存到数据库
    sendWSMessage({
      type: "chat",
      to: targetUserId.value,
      msgType: localMsg.type,
      content: localMsg.content,
      imageUrl: localMsg.thumbUrl || localMsg.imageUrl,
      originUrl: localMsg.originUrl,
      quoteId: localMsg.quoteId || null,
    });

    // 使用 Object.assign 触发响应式更新
    Object.assign(localMsg, { status: MSG_STATUS.SENT });
    console.log("Message status updated to:", localMsg.status);

    // 强制刷新视图
    nextTick(() => {
      messages.value = [...messages.value];
    });
  } catch (e) {
    console.error("sendSingleChatMsg failed:", e);
    localMsg.status = MSG_STATUS.FAILED;
    uni.showToast({ title: "发送失败", icon: "none" });
  }
}

function requestAIReply(userMsg) {
  const assistantMsg = createTextMsg("assistant", "", { typing: true });
  messages.value.push(assistantMsg);
  isStreaming.value = true;
  scrollToBottom();

  const history = messages.value
    .filter((m) => m.role === "user" || (m.role === "assistant" && m.content && !m.typing))
    .map((m) => ({ role: m.role, content: m.type === "image" ? "[图片]" : m.content }));

  abortFn = chatCompletionStream(
    { messages: history },
    {
      onChunk: (chunk) => { assistantMsg.content += chunk; scrollToBottom(); },
      onDone: () => {
        assistantMsg.typing = false;
        assistantMsg.status = MSG_STATUS.SENT;
        isStreaming.value = false;
        abortFn = null;
        scrollToBottom();
        saveChatHistory(chatId.value, messages.value);
      },
      onError: (err) => {
        console.error("Stream error:", err);
        assistantMsg.content = assistantMsg.content || "抱歉，发生了错误，请稍后重试。";
        assistantMsg.typing = false;
        assistantMsg.status = MSG_STATUS.SENT;
        isStreaming.value = false;
        abortFn = null;
        userMsg.status = MSG_STATUS.FAILED;
        uni.showToast({ title: "请求失败", icon: "none" });
      },
    }
  );
}

function retrySend(msg) {
  msg.status = MSG_STATUS.SENDING;
  if (chatType.value === "ai") {
    requestAIReply(msg);
  } else {
    sendSingleChatMsg(msg);
  }
}

function onMsgLongPress(msg) {
  if (msg.type === MSG_TYPE.SYSTEM) return;
  contextMenu.value = { visible: true, msg, x: 60, y: 200 };
}

function onImageLongPress(msg) {
  uni.showActionSheet({
    itemList: ['保存图片', '复制图片地址'],
    success: (res) => {
      if (res.tapIndex === 0) {
        downloadImage(msg);
      } else if (res.tapIndex === 1) {
        uni.setClipboardData({
          data: msg.originUrl || msg.imageUrl,
          success: () => uni.showToast({ title: '已复制', icon: 'success' })
        });
      }
    }
  });
}

function closeContextMenu() { contextMenu.value.visible = false; }

function doCopy() {
  const msg = contextMenu.value.msg;
  if (!msg) return;
  uni.setClipboardData({ data: msg.content, success: () => { uni.showToast({ title: "已复制", icon: "success" }); } });
  closeContextMenu();
}

function doQuote() {
  const msg = contextMenu.value.msg;
  if (!msg) return;
  quoteMsg.value = msg;
  closeContextMenu();
}

function doForward() {
  const msg = contextMenu.value.msg;
  if (!msg) return;
  uni.setClipboardData({ data: msg.type === "image" ? "[图片]" : msg.content, success: () => { uni.showToast({ title: "已复制，可粘贴转发", icon: "none" }); } });
  closeContextMenu();
}

async function doRecall() {
  const msg = contextMenu.value.msg;
  if (!msg || !canRecall(msg)) return;

  if (chatType.value === "single" && msg.serverId) {
    try {
      await recallMessage(msg.serverId);
    } catch (e) {
      console.error("recallMessage failed:", e);
    }
  }
  msg.recalled = true;
  closeContextMenu();
  uni.showToast({ title: "已撤回", icon: "success" });
}

async function doDelete() {
  const msg = contextMenu.value.msg;
  if (!msg) return;

  if (chatType.value === "single" && msg.serverId) {
    try {
      await deleteMessage(msg.serverId);
    } catch (e) {
      console.error("deleteMessage failed:", e);
    }
  }
  const idx = messages.value.findIndex((m) => m.id === msg.id);
  if (idx !== -1) messages.value.splice(idx, 1);
  closeContextMenu();
}

function getQuoteMsg(msg) {
  if (!msg.quoteId) return null;
  return messages.value.find((m) => m.id === msg.quoteId);
}

function previewImage(msg) {
  const urls = msg.originUrl ? [msg.originUrl] : [msg.imageUrl];
  uni.previewImage({ urls: urls, current: urls[0] });
}

function downloadImage(msg) {
  const url = msg.originUrl || msg.imageUrl;
  if (!url) return;
  uni.showLoading({ title: '下载中...' });
  uni.download({
    url: url,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading();
            uni.showToast({ title: '保存成功', icon: 'success' });
          },
          fail: () => {
            uni.hideLoading();
            uni.showToast({ title: '保存失败', icon: 'none' });
          }
        });
      }
    },
    fail: () => {
      uni.hideLoading();
      uni.showToast({ title: '下载失败', icon: 'none' });
    }
  });
}

function onScrollToUpper() {
  if (chatType.value === "single" && conversationId.value) {
    loadMoreMessages();
  }
}

async function loadMoreMessages() {
  if (loadingMore.value) return;
  const firstMsg = messages.value.find((m) => m.serverId);
  if (!firstMsg) return;

  loadingMore.value = true;
  try {
    const res = await getMessages({ conversationId: conversationId.value, lastMsgId: firstMsg.serverId, size: 30 });
    const serverMsgs = res.data || [];
    const me = uni.getStorageSync("userInfo");
    const myId = me?.id || me?.userId;

    const olderMsgs = serverMsgs.map((m) => {
      const role = m.senderId === myId ? "user" : "other";
      return serverMsgToLocal(m, role);
    });
    messages.value = [...olderMsgs, ...messages.value];
  } catch (e) {
    console.error("loadMoreMessages failed:", e);
  } finally {
    loadingMore.value = false;
  }
}

function changeBg(bg) { chatBg.value = bg; saveChatBg(chatId.value, bg); }

function doClearHistory() {
  uni.showModal({
    title: "确认清空",
    content: "清空后聊天记录将无法恢复，确定要清空吗？",
    success: (res) => {
      if (res.confirm) {
        messages.value = [];
        if (chatType.value === "ai") clearChatHistory(chatId.value);
        showSettings.value = false;
        uni.showToast({ title: "已清空", icon: "success" });
      }
    },
  });
}

onMounted(() => {
  console.log("[Chat] onMounted, removeWSHandler=", removeWSHandler);
  // 只在单聊时注册 WebSocket 消息监听
  if (chatType.value === "single" && !removeWSHandler) {
    console.log("[Chat] registering WS handler");
    removeWSHandler = onWSMessage((data) => {
      console.log("[Chat] WS message received:", data.type, data.data?.id);
      if (data.type === "chat_sent" && data.data) {
        // 更新本地消息的 serverId
        const msgData = data.data;
        const localMsg = messages.value.find(m => m.content === msgData.content && m.status === MSG_STATUS.SENT);
        if (localMsg) {
          localMsg.serverId = msgData.id;
        }
      } else if (data.type === "chat_received" && data.data) {
        const msgData = data.data;
        if (msgData.conversationId === conversationId.value) {
          // 如果是自己发送的消息，不重复添加
          const me = uni.getStorageSync("userInfo");
          const myId = me?.id || me?.userId;
          if (msgData.senderId === myId) {
            console.log("[Chat] ignoring self message");
            return;
          }
          console.log("[Chat] adding message to list");
          const msg = serverMsgToLocal(msgData, "other");
          messages.value.push(msg);
          scrollToBottom();
          markRead(conversationId.value).catch(() => {});
        }
      }
    });
  }
});

onUnmounted(() => {
  if (chatType.value === "ai") saveChatHistory(chatId.value, messages.value);
  if (removeWSHandler) {
    removeWSHandler();
    removeWSHandler = null;
  }
});
</script>

<style lang="scss" scoped>
page { background-color: #f5f5f5; }
.status-bar { background-color: #ffffff; }
.chat-page { height: 100vh; display: flex; flex-direction: column; background-color: #f5f5f5; position: relative; }
.nav-bar { flex-shrink: 0; height: 88rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; background-color: #ffffff; border-bottom: 1rpx solid #e5e5e5; z-index: 10; box-sizing: border-box; }
.nav-back, .nav-more { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; }
.nav-icon { width: 40rpx; height: 40rpx; color: #1a1a1a; }
.nav-center { display: flex; flex-direction: column; align-items: center; }
.nav-title { font-size: 32rpx; font-weight: 600; color: #1a1a1a; }
.message-list { flex: 1; overflow-y: auto; }
.message-list-inner { padding: 20rpx 24rpx; min-height: 100%; }
.scroll-bottom { height: 20rpx; }
.load-more-tip { text-align: center; padding: 16rpx; font-size: 24rpx; color: #999; }
.welcome-tip { display: flex; flex-direction: column; align-items: center; padding: 80rpx 40rpx; }
.welcome-icon { width: 120rpx; height: 120rpx; background-color: #e8f5e9; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 24rpx; svg { width: 64rpx; height: 64rpx; color: #07c160; } }
.welcome-text { font-size: 28rpx; color: #666; text-align: center; line-height: 1.6; }
.time-tip { text-align: center; margin: 24rpx 0 16rpx; }
.time-text { font-size: 22rpx; color: #999; background: rgba(0,0,0,0.04); padding: 4rpx 16rpx; border-radius: 8rpx; }
.system-msg { text-align: center; margin: 16rpx 0; }
.system-text { font-size: 24rpx; color: #999; }
.message-item { display: flex; align-items: flex-start; margin-bottom: 32rpx; &.user { justify-content: flex-end; } &.recalled { opacity: 0.6; } }
.avatar { width: 72rpx; height: 72rpx; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; svg { width: 40rpx; height: 40rpx; } }
.avatar-img { width: 72rpx; height: 72rpx; border-radius: 12rpx; flex-shrink: 0; background-color: #f0f0f0; }
.other-avatar { background-color: #e8f5e9; margin-right: 16rpx; svg { color: #07c160; } }
.user-avatar { background-color: #e3f2fd; margin-left: 16rpx; svg { color: #10aeff; } }
.message-item.user .avatar-img { margin-left: 16rpx; }
.message-item.other .avatar-img { margin-right: 16rpx; }
.bubble-wrap { max-width: 70%; min-width: 60rpx; }
.quote-bar { background: rgba(0,0,0,0.05); border-left: 4rpx solid #07c160; padding: 8rpx 16rpx; border-radius: 8rpx 8rpx 0 0; margin-bottom: -8rpx; }
.quote-text { font-size: 24rpx; color: #666; }
.bubble { padding: 20rpx 24rpx; border-radius: 16rpx; word-break: break-all; position: relative; }
.other-bubble { background-color: #ffffff; border-top-left-radius: 4rpx; }
.user-bubble { background-color: #95ec69; border-top-right-radius: 4rpx; }
.bubble-text { font-size: 30rpx; line-height: 1.6; color: #1a1a1a; }
.bubble-image { max-width: 400rpx; border-radius: 8rpx; }
.image-wrap { position: relative; display: inline-block; }
.upload-overlay, .failed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8rpx;
}
.upload-text, .failed-text { color: #fff; font-size: 24rpx; }
.failed-text { color: #ff3b30; }
.bubble-emoji { font-size: 64rpx; }
.typing-cursor { display: inline-block; width: 4rpx; height: 30rpx; background: #999; margin-left: 4rpx; animation: blink 0.8s infinite; vertical-align: middle; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
.recalled-tip { font-size: 24rpx; color: #999; text-align: center; padding: 8rpx; }
.status-row { display: flex; justify-content: flex-end; align-items: center; margin-top: 4rpx; }
.status-sending { display: flex; align-items: center; }
.spinner { width: 24rpx; height: 24rpx; border: 3rpx solid #ccc; border-top-color: #07c160; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.status-failed { display: flex; align-items: center; gap: 2rpx; }
.failed-text { font-size: 18rpx; color: #ff3b30; }
.quote-bar-wrap { flex-shrink: 0; padding: 12rpx 24rpx; background: #f5f5f5; border-top: 1rpx solid #e5e5e5; }
.quote-bar-inner { display: flex; align-items: center; background: #fff; border-radius: 8rpx; padding: 12rpx 16rpx; border-left: 6rpx solid #07c160; }
.quote-label { font-size: 24rpx; color: #07c160; margin-right: 8rpx; flex-shrink: 0; }
.quote-preview { font-size: 24rpx; color: #666; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.quote-close { margin-left: 12rpx; flex-shrink: 0; color: #999; }
.input-area { flex-shrink: 0; padding: 16rpx 24rpx; background: #ffffff; border-top: 1rpx solid #e5e5e5; display: flex; flex-direction: column; gap: 12rpx; }
.input-toolbar { display: flex; gap: 20rpx; }
.tool-btn { width: 80rpx; height: 80rpx; display: flex; align-items: center; justify-content: center; svg { width: 48rpx; height: 48rpx; color: #666; } }
.input-row { display: flex; align-items: center; gap: 16rpx; }
.msg-input { flex: 1; height: 72rpx; background: #f5f5f5; border-radius: 16rpx; padding: 0 24rpx; font-size: 30rpx; }
.send-btn { width: 80rpx; height: 80rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #cccccc; .send-icon { width: 40rpx; height: 40rpx; color: #ffffff; } &.active { background: #07c160; } }
.emoji-panel { flex-shrink: 0; height: 420rpx; background: #f5f5f5; border-top: 1rpx solid #e5e5e5; }
.emoji-scroll { height: 100%; }
.emoji-grid { display: flex; flex-wrap: wrap; padding: 16rpx; }
.emoji-item { width: 12.5%; height: 72rpx; display: flex; align-items: center; justify-content: center; }
.emoji-char { font-size: 44rpx; }
.context-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.3); z-index: 100; display: flex; align-items: center; justify-content: center; }
.context-menu { background: #fff; border-radius: 16rpx; padding: 8rpx 0; min-width: 200rpx; box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.15); }
.ctx-item { padding: 20rpx 32rpx; font-size: 28rpx; color: #1a1a1a; text-align: center; &:active { background: #f5f5f5; } }
.ctx-danger { color: #ff3b30; }
.settings-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.3); z-index: 100; display: flex; align-items: flex-end; justify-content: center; }
.settings-panel { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 32rpx; max-height: 70vh; }
.settings-title { font-size: 32rpx; font-weight: 600; text-align: center; margin-bottom: 32rpx; }
.settings-item { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #f0f0f0; font-size: 30rpx; }
.radio-dot { width: 36rpx; height: 36rpx; border-radius: 50%; border: 3rpx solid #ccc; &.active { border-color: #07c160; background: #07c160; position: relative; &::after { content: ''; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 16rpx; height: 16rpx; background: #fff; border-radius: 50%; } } }
.settings-divider { height: 1rpx; background: #e5e5e5; margin: 16rpx 0; }
</style>
