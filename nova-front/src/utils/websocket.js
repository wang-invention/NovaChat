/**
 * WebSocket 工具 - 单聊实时通信
 */

// #ifdef H5
const WS_BASE_URL = `ws://129.211.0.210:8087/ws/chat`;
// #endif
// #ifndef H5
const WS_BASE_URL = "ws://129.211.0.210:8087/ws/chat";
// #endif

let socketTask = null;
let isConnected = false;
let reconnectTimer = null;
let heartbeatTimer = null;
let messageHandlers = [];
let currentUserId = null;

const RECONNECT_DELAY = 3000;
const HEARTBEAT_INTERVAL = 30000;

export function connectWS(userId) {
  if (isConnected && currentUserId === userId) return;
  currentUserId = userId;
  disconnectWS();

  const url = `${WS_BASE_URL}?userId=${userId}`;

  // #ifdef H5
  socketTask = new WebSocket(url);

  socketTask.onopen = () => {
    isConnected = true;
    console.log("[WS] connected");
    startHeartbeat();
  };

  socketTask.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      messageHandlers.forEach((handler) => handler(data));
    } catch (e) {
      console.error("[WS] parse message error:", e);
    }
  };

  socketTask.onclose = () => {
    isConnected = false;
    console.log("[WS] closed");
    stopHeartbeat();
    scheduleReconnect();
  };

  socketTask.onerror = (err) => {
    console.error("[WS] error:", err);
    isConnected = false;
  };
  // #endif

  // #ifndef H5
  socketTask = uni.connectSocket({ url, complete: () => { } });

  socketTask.onOpen(() => {
    isConnected = true;
    console.log("[WS] connected");
    startHeartbeat();
  });

  socketTask.onMessage((res) => {
    try {
      const data = JSON.parse(res.data);
      messageHandlers.forEach((handler) => handler(data));
    } catch (e) {
      console.error("[WS] parse message error:", e);
    }
  });

  socketTask.onClose(() => {
    isConnected = false;
    console.log("[WS] closed");
    stopHeartbeat();
    scheduleReconnect();
  });

  socketTask.onError((err) => {
    console.error("[WS] error:", err);
    isConnected = false;
  });
  // #endif
}

export function disconnectWS() {
  stopHeartbeat();
  clearTimeout(reconnectTimer);
  reconnectTimer = null;

  if (socketTask) {
    try {
      // #ifdef H5
      socketTask.close();
      // #endif
      // #ifndef H5
      uni.closeSocket();
      // #endif
    } catch (e) { }
    socketTask = null;
  }
  isConnected = false;
}

export function sendWSMessage(data) {
  if (!isConnected || !socketTask) {
    console.warn("[WS] not connected, cannot send");
    return false;
  }

  const payload = typeof data === "string" ? data : JSON.stringify(data);

  // #ifdef H5
  socketTask.send(payload);
  return true;
  // #endif

  // #ifndef H5
  socketTask.send({ data: payload });
  return true;
  // #endif
}

export function onWSMessage(handler) {
  messageHandlers.push(handler);
  return () => {
    messageHandlers = messageHandlers.filter((h) => h !== handler);
  };
}

export function isWSConnected() {
  return isConnected;
}

function startHeartbeat() {
  stopHeartbeat();
  heartbeatTimer = setInterval(() => {
    if (isConnected) {
      sendWSMessage({ type: "ping" });
    }
  }, HEARTBEAT_INTERVAL);
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
}

function scheduleReconnect() {
  if (reconnectTimer) return;
  if (!currentUserId) return;
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    console.log("[WS] reconnecting...");
    connectWS(currentUserId);
  }, RECONNECT_DELAY);
}
