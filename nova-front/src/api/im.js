/**
 * 单聊相关接口
 */
import { http } from "@/utils/request";

export function sendMessage(data) {
  return http.post("/chat/chat/messages", data);
}

export function getConversations() {
  return http.get("/chat/chat/conversations");
}

export function getMessages(params) {
  return http.get("/chat/chat/messages", params);
}

export function recallMessage(messageId) {
  return http.post(`/chat/chat/messages/${messageId}/recall`);
}

export function deleteMessage(messageId) {
  return http.delete(`/chat/chat/messages/${messageId}`);
}

export function markRead(conversationId) {
  return http.post(`/chat/chat/conversations/${conversationId}/read`);
}

export function getConversationId(targetUserId) {
  return http.get("/chat/chat/conversations/id", { targetUserId });
}

export function searchUsers(keyword) {
  return http.get("/user/users/search", { keyword });
}

export function getUserById(userId) {
  return http.get(`/user/users/${userId}`);
}

export function sendFriendRequest(data) {
  return http.post("/chat/friend/request", data);
}

export function acceptFriendRequest(requestId) {
  return http.post(`/chat/friend/request/${requestId}/accept`);
}

export function rejectFriendRequest(requestId) {
  return http.post(`/chat/friend/request/${requestId}/reject`);
}

export function getPendingFriendRequests() {
  return http.get("/chat/friend/requests/pending");
}

export function getFriendList() {
  return http.get("/chat/friend/list");
}

export function isFriend(targetUserId) {
  return http.get("/chat/friend/isFriend", { targetUserId });
}
