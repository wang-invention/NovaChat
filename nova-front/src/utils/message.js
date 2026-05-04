/**
 * 消息数据模型与工具函数
 */

export const MSG_TYPE = {
  TEXT: "text",
  IMAGE: "image",
  EMOJI: "emoji",
  STICKER: "sticker",
  SYSTEM: "system",
};

export const MSG_STATUS = {
  SENDING: "sending",
  SENT: "sent",
  FAILED: "failed",
};

export const RECALL_TIMEOUT = 2 * 60 * 1000;

let _counter = 0;

export function genMsgId() {
  return `msg_${Date.now()}_${++_counter}`;
}

export function createTextMsg(role, content, extra = {}) {
  return {
    id: genMsgId(),
    role,
    type: MSG_TYPE.TEXT,
    content,
    status: role === "user" ? MSG_STATUS.SENDING : MSG_STATUS.SENT,
    timestamp: Date.now(),
    recalled: false,
    quoteId: null,
    ...extra,
  };
}

export function createImageMsg(role, imageUrl, extra = {}) {
  return {
    id: genMsgId(),
    role,
    type: MSG_TYPE.IMAGE,
    content: "",
    imageUrl,
    status: role === "user" ? MSG_STATUS.SENDING : MSG_STATUS.SENT,
    timestamp: Date.now(),
    recalled: false,
    quoteId: null,
    ...extra,
  };
}

export function createEmojiMsg(role, emojiKey, extra = {}) {
  return {
    id: genMsgId(),
    role,
    type: MSG_TYPE.EMOJI,
    content: emojiKey,
    status: role === "user" ? MSG_STATUS.SENDING : MSG_STATUS.SENT,
    timestamp: Date.now(),
    recalled: false,
    quoteId: null,
    ...extra,
  };
}

export function createSystemMsg(content) {
  return {
    id: genMsgId(),
    role: "system",
    type: MSG_TYPE.SYSTEM,
    content,
    status: MSG_STATUS.SENT,
    timestamp: Date.now(),
    recalled: false,
    quoteId: null,
  };
}

export function canRecall(msg) {
  if (msg.recalled || msg.role === "system") return false;
  return Date.now() - msg.timestamp < RECALL_TIMEOUT;
}

export function formatTime(ts) {
  const d = new Date(ts);
  const now = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  const time = `${pad(d.getHours())}:${pad(d.getMinutes())}`;

  const isToday =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate();

  if (isToday) return time;

  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  const isYesterday =
    d.getFullYear() === yesterday.getFullYear() &&
    d.getMonth() === yesterday.getMonth() &&
    d.getDate() === yesterday.getDate();

  if (isYesterday) return `昨天 ${time}`;

  const isThisYear = d.getFullYear() === now.getFullYear();
  if (isThisYear) return `${d.getMonth() + 1}月${d.getDate()}日 ${time}`;

  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${time}`;
}

export function shouldShowTimestamp(msgs, index) {
  if (index === 0) return true;
  const prev = msgs[index - 1];
  const curr = msgs[index];
  return curr.timestamp - prev.timestamp > 5 * 60 * 1000;
}

const CHAT_STORAGE_PREFIX = "chat_history_";
const CHAT_BG_PREFIX = "chat_bg_";

export function saveChatHistory(chatId, messages) {
  try {
    const data = messages.filter((m) => !m.typing);
    uni.setStorageSync(CHAT_STORAGE_PREFIX + chatId, JSON.stringify(data));
  } catch (e) {
    console.error("saveChatHistory failed", e);
  }
}

export function loadChatHistory(chatId) {
  try {
    const raw = uni.getStorageSync(CHAT_STORAGE_PREFIX + chatId);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    return [];
  }
}

export function clearChatHistory(chatId) {
  try {
    uni.removeStorageSync(CHAT_STORAGE_PREFIX + chatId);
  } catch (e) {
    console.error("clearChatHistory failed", e);
  }
}

export function saveChatBg(chatId, bg) {
  try {
    uni.setStorageSync(CHAT_BG_PREFIX + chatId, bg);
  } catch (e) {
    console.error("saveChatBg failed", e);
  }
}

export function loadChatBg(chatId) {
  try {
    return uni.getStorageSync(CHAT_BG_PREFIX + chatId) || "";
  } catch (e) {
    return "";
  }
}
