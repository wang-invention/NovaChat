import { getToken } from '../utils/auth'

const BASE = import.meta.env.VITE_API_BASE + '/api/chat/chat'

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers['Authorization'] = 'Bearer ' + token

  const res = await fetch(BASE + path, { ...options, headers: { ...headers, ...options.headers } })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || data.msg || '请求失败')
  return data
}

export function get(path) { return request(path) }
export function post(path, body) { return request(path, { method: 'POST', body: JSON.stringify(body) }) }
export function del(path) { return request(path, { method: 'DELETE' }) }

export function getConversations() {
  return get('/conversations')
}

export function getMessages(conversationId) {
  return get('/conversations/' + conversationId + '/messages')
}

export function sendMessage(data) {
  return post('/messages', data)
}

export function recallMessage(messageId) {
  return post('/messages/' + messageId + '/recall', {})
}

export function createConversation(peerId) {
  return post('/conversations', { peerId })
}

export function getFriends() {
  return get('/friends')
}

export function getFriendRequests() {
  return get('/friends/requests')
}

export function sendFriendRequest(receiverId, message) {
  return post('/friends/requests', { receiverId, message })
}

export function acceptFriendRequest(requestId) {
  return post('/friends/requests/' + requestId + '/accept', {})
}

export function rejectFriendRequest(requestId) {
  return post('/friends/requests/' + requestId + '/reject', {})
}