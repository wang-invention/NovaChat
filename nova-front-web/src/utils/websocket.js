import { getToken, clearAuth } from '../utils/auth'

const WS_URL = import.meta.env.VITE_WS_URL

let ws = null
let connected = false
let heartbeatTimer = null
let reconnectTimer = null
const handlers = []

function handleAuthExpired() {
  stopHeartbeat()
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  ws = null
  clearAuth()
  window.location.hash = '#/login'
}

export function connect(userId) {
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return

  const token = getToken()
  if (!token) { handleAuthExpired(); return }

  const url = WS_URL + '?userId=' + userId + '&token=' + token

  try {
    ws = new WebSocket(url)
    ws.onopen = () => {
      connected = true
      startHeartbeat()
      if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
    }
    ws.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data)
        handlers.forEach(h => h(data))
      } catch { }
    }
    ws.onclose = () => {
      connected = false
      stopHeartbeat()
      if (!getToken()) { handleAuthExpired(); return }
      scheduleReconnect(userId)
    }
    ws.onerror = () => {
      connected = false
      if (!getToken()) { handleAuthExpired(); return }
    }
  } catch {
    scheduleReconnect(userId)
  }
}

export function disconnect() {
  stopHeartbeat()
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  if (ws) {
    ws.onclose = null
    ws.close()
    ws = null
  }
  connected = false
}

export function send(data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(data))
  }
}

export function onMessage(handler) {
  handlers.push(handler)
  return () => {
    const idx = handlers.indexOf(handler)
    if (idx >= 0) handlers.splice(idx, 1)
  }
}

export function isConnected() {
  return connected
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'ping' }))
    }
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) { clearInterval(heartbeatTimer); heartbeatTimer = null }
}

function scheduleReconnect(userId) {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connect(userId)
  }, 3000)
}