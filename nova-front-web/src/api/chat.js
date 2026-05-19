import { getToken } from '../utils/auth'

const BASE = import.meta.env.VITE_API_BASE + '/api/chat'

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
export function put(path, body) { return request(path, { method: 'PUT', body: JSON.stringify(body) }) }
export function del(path) { return request(path, { method: 'DELETE' }) }

// ========== 聊天(ChatController) ==========

export function getConversations() {
  return get('/chat/conversations')
}

export function getConversationId(targetUserId) {
  return get('/chat/conversations/id?targetUserId=' + targetUserId)
}

export function getMessages(conversationId, lastMsgId, size) {
  let url = '/chat/messages?conversationId=' + conversationId
  if (lastMsgId) url += '&lastMsgId=' + lastMsgId
  if (size) url += '&size=' + size
  return get(url)
}

export function sendMessage(data) {
  return post('/chat/messages', data)
}

export function recallMessage(messageId) {
  return post('/chat/messages/' + messageId + '/recall', {})
}

export function deleteMessage(messageId) {
  return del('/chat/messages/' + messageId)
}

export function markRead(conversationId) {
  return post('/chat/conversations/' + conversationId + '/read', {})
}

// ========== 好友(FriendController) ==========

export function getFriends() {
  return get('/friend/list')
}

export function getFriendRequests() {
  return get('/friend/requests/pending')
}

export function getFriendRequestHistory() {
  return get('/friend/requests/history')
}

export function sendFriendRequest(receiverId, message) {
  return post('/friend/request', { receiverId, message })
}

export function acceptFriendRequest(requestId) {
  return post('/friend/request/' + requestId + '/accept', {})
}

export function rejectFriendRequest(requestId) {
  return post('/friend/request/' + requestId + '/reject', {})
}

export function isFriend(targetUserId) {
  return get('/friend/isFriend?targetUserId=' + targetUserId)
}

// ========== 群聊(GroupController) ==========

export function createGroup(data) {
  return post('/group/create', data)
}

export function updateGroup(groupId, data) {
  return put('/group/' + groupId, data)
}

export function getGroup(groupId) {
  return get('/group/' + groupId)
}

export function getMyGroups() {
  return get('/group/my')
}

export function getGroupMembers(groupId) {
  return get('/group/' + groupId + '/members')
}

export function addGroupMembers(groupId, data) {
  return post('/group/' + groupId + '/members', data)
}

export function removeGroupMember(groupId, userId) {
  return del('/group/' + groupId + '/members/' + userId)
}

export function leaveGroup(groupId) {
  return del('/group/' + groupId + '/leave')
}

export function changeMemberRole(groupId, userId, role) {
  return put('/group/' + groupId + '/members/' + userId + '/role?role=' + role, {})
}

export function dismissGroup(groupId) {
  return del('/group/' + groupId)
}

// ========== 通话(CallController) ==========

export function getCallRecords(page, size) {
  return get('/call/records?page=' + (page || 1) + '&size=' + (size || 20))
}

export function getCallRecord(callId) {
  return get('/call/records/' + callId)
}

// ========== 兼容旧接口(创建会话) ==========

export async function createConversation(peerId) {
  const res = await get('/chat/conversations/id?targetUserId=' + peerId)
  const id = res.data
  return { ...res, data: { id, conversationId: id } }
}