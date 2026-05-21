<template>
  <div class="main-page" :class="{ 'show-chat': showChat }">
    <!-- 左侧窄边栏 - 微信风格 -->
    <nav class="wechat-nav">
      <div class="nav-top">
        <div class="nav-avatar" @click="openProfile">
          <img v-if="user.avatar && !imgErrors['nav']" :src="user.avatar" @error="imgErrors['nav'] = true" alt="">
          <div v-else :style="{ background: avatarBg }">{{ (user.nickname || user.username || '?')[0] }}</div>
        </div>
      </div>
      <div class="nav-mid">
        <button class="nav-item" :class="{ active: activeTab === 'chats' }" @click="activeTab = 'chats'" title="聊天">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          <span v-if="totalUnread" class="nav-badge">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
        </button>
        <button class="nav-item" :class="{ active: activeTab === 'contacts' }" @click="activeTab = 'contacts'" title="通讯录">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        </button>
        <button class="nav-item" :class="{ active: activeTab === 'discover' }" @click="activeTab = 'discover'" title="发现">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
        </button>
      </div>
      <div class="nav-bottom">
        <button class="nav-item" title="设置" @click="openProfile">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
        </button>
      </div>
    </nav>

    <!-- 中间栏 - 会话列表/通讯录/发现 -->
    <aside class="wechat-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">{{ sidebarTitle }}</h2>
        <div class="sidebar-actions">
          <button class="icon-btn" title="搜索用户" @click="showSearch = true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          </button>
          <button class="icon-btn" title="添加好友" @click="showAddFriend = true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
          </button>
          <button class="icon-btn" title="创建群聊" @click="showCreateGroup = true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/><line x1="21" y1="1" x2="21" y2="7"/><line x1="18" y1="4" x2="24" y2="4"/></svg>
          </button>
        </div>
      </div>

      <div class="sidebar-search">
        <div class="search-box">
          <svg class="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="searchQuery" type="text" :placeholder="searchPlaceholder" @input="onSearch">
        </div>
      </div>

      <div class="sidebar-content">
        <ConversationList v-if="activeTab === 'chats'" :conversations="filteredConversations" :active-id="currentChatId" @select="selectChat" />
        <ContactList v-else-if="activeTab === 'contacts'" :friends="friends" :requests="friendRequests" @chat="startChatWith" @accept="acceptRequest" @reject="rejectRequest" />
        <DiscoverPanel v-else-if="activeTab === 'discover'" @open-moments="showMoments = true" />
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div v-if="!currentChatId && !showProfile && !showMoments" class="chat-empty">
        <div class="empty-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        </div>
        <h2>NovaChat</h2>
        <p>选择一个会话开始聊天</p>
      </div>

      <div v-if="currentChatId && !showProfile && !showMoments" class="chat-wrapper" :class="{ 'with-panel': showGroupPanel && currentConversation?.isGroup }">
        <ChatView
          :conversation="currentConversation"
          :messages="messages"
          :is-streaming="isStreaming"
          :streaming-msg-id="streamingMsgId"
          :chat-bg="chatBg"
          :user="user"
          :loading-more="loadingMore"
          :has-more="hasMore"
          @send="sendMessage"
          @recall="recallMessage"
          @back="currentChatId = null"
          @settings="showChatSettings = !showChatSettings; showGroupPanel = false"
          @call="startCall"
          @group-info="openGroupPanel"
          @load-more="loadMoreMessages"
        />

        <GroupChatPanel
          v-if="showGroupPanel && currentConversation?.isGroup"
          :group-id="currentConversation.groupId || currentConversation.id"
          @close="showGroupPanel = false"
          @left="onGroupLeft"
          @updated="loadConversations"
        />
      </div>

      <ProfilePanel v-if="showProfile" @close="showProfile = false" @updated="onProfileUpdated" />

      <Moments
        v-if="showMoments"
        :current-user-id="user.id"
        :user-info="user"
        @back="showMoments = false"
        @go-chat="startChatWith"
      />

      <ChatSettingsPanel
        v-if="showChatSettings && currentChatId"
        :chat-bg="chatBg"
        @bg-change="chatBg = $event"
        @close="showChatSettings = false"
      />
    </main>

    <SearchModal v-if="showSearch" @close="showSearch = false" @chat="startChatWith" />
    <AddFriendModal v-if="showAddFriend" @close="showAddFriend = false" />
    <CreateGroupModal v-if="showCreateGroup" :friends="friends" @close="showCreateGroup = false" @created="onGroupCreated" />

    <CallOverlay
      v-if="callState.show"
      :state="callState"
      @hangup="hangupCall"
      @toggle-mute="toggleMute"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getUser, clearAuth } from '../utils/auth'
import { connect, disconnect, onMessage, send } from '../utils/websocket'
import { getConversations, getMessages, recallMessage as apiRecallMessage, getFriends, getFriendRequests, acceptFriendRequest, rejectFriendRequest, createConversation, markRead } from '../api/chat'
import { chatStream } from '../api/ai'
import ConversationList from '../components/ConversationList.vue'
import ContactList from '../components/ContactList.vue'
import DiscoverPanel from '../components/DiscoverPanel.vue'
import ChatView from '../components/ChatView.vue'
import ProfilePanel from '../components/ProfilePanel.vue'
import ChatSettingsPanel from '../components/ChatSettingsPanel.vue'
import SearchModal from '../components/SearchModal.vue'
import AddFriendModal from '../components/AddFriendModal.vue'
import CreateGroupModal from '../components/CreateGroupModal.vue'
import GroupChatPanel from '../components/GroupChatPanel.vue'
import Moments from './Moments.vue'
import CallOverlay from '../components/CallOverlay.vue'

const router = useRouter()
const user = ref(getUser() || {})
const imgErrors = reactive({})
const activeTab = ref('chats')
const searchQuery = ref('')
const conversations = ref([])
const currentChatId = ref(null)
const messages = ref([])
const friends = ref([])
const friendRequests = ref([])
const showProfile = ref(false)
const showChatSettings = ref(false)
const showSearch = ref(false)
const showAddFriend = ref(false)
const showCreateGroup = ref(false)
const showGroupPanel = ref(false)
const showMoments = ref(false)
const chatBg = ref('')
const isStreaming = ref(false)
const streamingMsgId = ref('')
const hasMore = ref(true)
const loadingMore = ref(false)
const PAGE_SIZE = 30
const showChat = ref(false)

const callState = reactive({
  show: false,
  status: 'calling',
  peerName: '',
  peerAvatar: '',
  duration: 0,
  muted: false
})

let streamAbort = null
let callTimer = null
let unsubWs = null

const avatarBg = computed(() => {
  const colors = ['#07C160', '#10AEFF', '#FF6B6B', '#FFD93D', '#6C5CE7', '#00B894']
  const idx = (user.value.username || '').charCodeAt(0) % colors.length
  return colors[idx]
})

const tabs = [
  { key: 'chats', label: '消息' },
  { key: 'contacts', label: '通讯录' },
  { key: 'discover', label: '发现' }
]

const sidebarTitle = computed(() => {
  if (activeTab.value === 'chats') return '消息'
  if (activeTab.value === 'contacts') return '通讯录'
  return '发现'
})

const totalUnread = computed(() => {
  return conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
})

const searchPlaceholder = computed(() => {
  if (activeTab.value === 'chats') return '搜索会话'
  if (activeTab.value === 'contacts') return '搜索好友'
  return '搜索'
})

const filteredConversations = computed(() => {
  if (!searchQuery.value) return conversations.value
  const q = searchQuery.value.toLowerCase()
  return conversations.value.filter(c => (c.name || c.peerName || '').toLowerCase().includes(q))
})

const currentConversation = computed(() => {
  return conversations.value.find(c => c.id === currentChatId.value || c.conversationId === currentChatId.value)
})

function onSearch() {}

async function loadConversations() {
  try {
    const res = await getConversations()
    conversations.value = (res.data || []).map(c => {
      const isGroup = c.conversationType === 'GROUP' || !!c.groupId
      return {
        ...c,
        id: c.id || c.conversationId,
        name: isGroup ? (c.groupName || c.name || '群聊') : (c.targetNickname || c.name || c.peerName || c.peerNickname || '未知'),
        avatar: isGroup ? (c.groupAvatar || c.avatar || '') : (c.targetAvatar || c.avatar || c.peerAvatar || ''),
        lastTime: c.lastMessageTime || c.lastTime || c.updatedAt,
        isGroup
      }
    })
    if (currentChatId.value) {
      const active = conversations.value.find(c => (c.id || c.conversationId) === currentChatId.value)
      if (active) active.unreadCount = 0
    }
  } catch {}
}

async function loadFriends() {
  try {
    const res = await getFriends()
    friends.value = res.data || []
  } catch {}
}

async function loadFriendRequests() {
  try {
    const res = await getFriendRequests()
    friendRequests.value = res.data || []
  } catch {}
}

async function selectChat(conv) {
  currentChatId.value = conv.id || conv.conversationId
  showChat.value = true
  showProfile.value = false
  showChatSettings.value = false
  showGroupPanel.value = false
  messages.value = []
  hasMore.value = true
  loadingMore.value = false

  const isGroup = conv.isGroup || conv.conversationType === 'GROUP' || !!conv.groupId
  if (isGroup && (conv.unreadCount || 0) > 0) {
    const target = conversations.value.find(c => (c.id || c.conversationId) === (conv.id || conv.conversationId))
    if (target) target.unreadCount = 0
  }

  try {
    const res = await getMessages(currentChatId.value, null, PAGE_SIZE)
    const raw = res.data || []
    messages.value = raw.map(m => ({
      ...m,
      id: m.id || m.messageId,
      senderId: m.senderId || m.fromUserId,
      senderName: m.senderName || m.fromUserName || '',
      senderAvatar: m.senderAvatar || '',
      content: m.content || '',
      type: m.type || m.messageType || 'text',
      createdAt: m.createdAt || m.createTime || Date.now()
    }))
    hasMore.value = raw.length >= PAGE_SIZE
    await nextTick()
    scrollToBottom()
    try {
      await markRead(currentChatId.value)
    } catch {}
    loadConversations()
  } catch {}
}

async function loadMoreMessages() {
  if (loadingMore.value || !hasMore.value || !currentChatId.value) return
  const oldest = messages.value[0]
  if (!oldest) return
  loadingMore.value = true
  const oldScrollHeight = document.querySelector('.msg-list')?.scrollHeight || 0
  try {
    const res = await getMessages(currentChatId.value, oldest.id, PAGE_SIZE)
    const raw = res.data || []
    if (raw.length === 0) {
      hasMore.value = false
      return
    }
    const older = raw.map(m => ({
      ...m,
      id: m.id || m.messageId,
      senderId: m.senderId || m.fromUserId,
      senderName: m.senderName || m.fromUserName || '',
      senderAvatar: m.senderAvatar || '',
      content: m.content || '',
      type: m.type || m.messageType || 'text',
      createdAt: m.createdAt || m.createTime || Date.now()
    }))
    messages.value.unshift(...older)
    hasMore.value = raw.length >= PAGE_SIZE
    await nextTick()
    const el = document.querySelector('.msg-list')
    if (el) el.scrollTop = el.scrollHeight - oldScrollHeight
  } catch {
    hasMore.value = false
  } finally {
    loadingMore.value = false
  }
}

async function startChatWith(peer) {
  try {
    const res = await createConversation(peer.friendId || peer.id || peer.userId)
    const conv = res.data
    conversations.value.unshift({
      ...conv,
      id: conv.id || conv.conversationId,
      name: peer.nickname || peer.username,
      avatar: peer.avatar || ''
    })
    selectChat(conv)
  } catch {
    const existing = conversations.value.find(c => c.peerId === (peer.friendId || peer.id || peer.userId))
    if (existing) selectChat(existing)
  }
}

async function sendMessage(text) {
  if (!text.trim() || !currentChatId.value) return

  if (currentChatId.value === 'ai_assistant') {
    await sendAIMessage(text)
    return
  }

  const tempId = 'temp_' + Date.now()
  const conv = currentConversation.value
  const isGroupChat = conv && (conv.isGroup || conv.conversationType === 'GROUP' || !!conv.groupId)
  const msg = {
    id: tempId,
    senderId: user.value.id,
    senderName: user.value.nickname || user.value.username,
    senderAvatar: user.value.avatar || '',
    content: text,
    type: 'text',
    createdAt: Date.now(),
    status: 'sending'
  }
  messages.value.push(msg)
  await nextTick()
  scrollToBottom()

  send({
    type: 'chat',
    to: isGroupChat ? null : (conv ? conv.targetUserId : null),
    groupId: isGroupChat ? (conv.groupId || conv.id) : null,
    content: text,
    msgType: 'text'
  })
}

function confirmSentMessage(savedMsg) {
  const msgId = savedMsg.id || savedMsg.messageId
  if (!msgId) return
  const idx = messages.value.findIndex(m => m.id === 'temp_' + savedMsg.tempId)
  if (idx < 0) {
    const tempIdx = messages.value.findIndex(m => m.status === 'sending' && m.content === savedMsg.content)
    if (tempIdx >= 0) {
      messages.value[tempIdx] = { ...messages.value[tempIdx], ...savedMsg, id: msgId, status: 'sent' }
    }
    return
  }
  messages.value[idx] = { ...messages.value[idx], ...savedMsg, id: msgId, status: 'sent' }
}

async function sendAIMessage(text) {
  const userMsg = {
    id: 'ai_user_' + Date.now(),
    senderId: user.value.id,
    senderName: user.value.nickname || user.value.username,
    senderAvatar: user.value.avatar || '',
    content: text,
    type: 'text',
    role: 'user',
    createdAt: Date.now()
  }
  messages.value.push(userMsg)

  const aiMsgId = 'ai_resp_' + Date.now()
  const aiMsg = {
    id: aiMsgId,
    senderId: 'ai_assistant',
    senderName: 'AI 助手',
    content: '',
    type: 'text',
    role: 'assistant',
    createdAt: Date.now(),
    typing: true
  }
  messages.value.push(aiMsg)

  isStreaming.value = true
  streamingMsgId.value = aiMsgId
  await nextTick()
  scrollToBottom()

  const historyMessages = messages.value
    .filter(m => m.role === 'user' || m.role === 'assistant')
    .slice(0, -1)
    .map(m => ({ role: m.role, content: m.content }))

  const controller = new AbortController()
  streamAbort = () => controller.abort()

  try {
    await chatStream(historyMessages, {
      signal: controller.signal,
      onChunk: (text) => {
        const idx = messages.value.findIndex(m => m.id === aiMsgId)
        if (idx >= 0) {
          messages.value[idx].content += text
          messages.value[idx].typing = false
        }
        scrollToBottom()
      },
      onDone: () => {
        isStreaming.value = false
        streamingMsgId.value = ''
        streamAbort = null
      },
      onError: () => {
        const idx = messages.value.findIndex(m => m.id === aiMsgId)
        if (idx >= 0) messages.value[idx].content = '抱歉，AI 服务暂时不可用。'
        isStreaming.value = false
        streamingMsgId.value = ''
        streamAbort = null
      }
    })
  } catch {
    isStreaming.value = false
    streamingMsgId.value = ''
    streamAbort = null
  }
}

async function recallMessage(msgId) {
  try {
    await apiRecallMessage(msgId)
    const idx = messages.value.findIndex(m => m.id === msgId)
    if (idx >= 0) messages.value[idx].recalled = true
  } catch {}
}

function openProfile() {
  showProfile.value = true
  currentChatId.value = null
  showChatSettings.value = false
}

function onProfileUpdated(updatedUser) {
  user.value = { ...user.value, ...updatedUser }
}

function openGroupPanel() {
  showGroupPanel.value = !showGroupPanel.value
  showChatSettings.value = false
}

function onGroupCreated(group) {
  showCreateGroup.value = false
  loadConversations().then(() => {
    const conv = conversations.value.find(c => c.groupId === group.id || String(c.groupId) === String(group.id))
    if (conv) {
      selectChat(conv)
    } else {
      conversations.value.unshift({
        id: 'group_' + group.id,
        groupId: group.id,
        name: group.name,
        avatar: group.avatar || '',
        isGroup: true,
        conversationType: 'GROUP',
        unreadCount: 0
      })
      selectChat(conversations.value[0])
    }
  })
}

function onGroupLeft(groupId) {
  currentChatId.value = null
  showGroupPanel.value = false
  loadConversations()
}

function scrollToBottom() {
  nextTick(() => {
    const el = document.querySelector('.msg-list')
    if (el) el.scrollTop = el.scrollHeight
  })
}

function handleWsMessage(data) {
  if (data.type === 'chat_sent') {
    confirmSentMessage(data.data || data)
    return
  }
  if (data.type === 'message' || data.type === 'chat' || data.type === 'chat_received') {
    const msg = data.data || data
    const msgConvId = String(msg.conversationId || data.conversationId || '')
    const curConvId = String(currentChatId.value || '')
    const msgSenderId = Number(msg.senderId || msg.fromUserId)
    const myUid = Number(user.value.id)
    const isSelf = !isNaN(msgSenderId) && !isNaN(myUid) && msgSenderId === myUid

    if (msgConvId && curConvId && (msgConvId === curConvId)) {
      const exists = messages.value.find(m => m.id === (msg.id || msg.messageId))
      if (!exists) {
        messages.value.push({
          ...msg,
          id: msg.id || msg.messageId,
          senderId: msg.senderId || msg.fromUserId,
          senderName: msg.senderName || msg.fromUserName || '',
          senderAvatar: msg.senderAvatar || '',
          content: msg.content || '',
          type: msg.type || msg.messageType || 'text',
          createdAt: msg.createdAt || msg.createTime || Date.now()
        })
        scrollToBottom()
      }
    } else if (!isSelf && msgConvId) {
      loadConversations()
    }
  } else if (data.type === 'recall' || data.type === 'message_recall') {
    const msgId = data.messageId || (data.data && data.data.messageId)
    if (msgId) {
      const idx = messages.value.findIndex(m => m.id === msgId)
      if (idx >= 0) messages.value[idx].recalled = true
    }
  } else if (data.type === 'friend_request') {
    loadFriendRequests()
  } else if (data.type === 'call_offer') {
    handleIncomingCall(data)
  } else if (data.type === 'call_answer') {
    callState.status = 'connected'
    startCallTimer()
  } else if (data.type === 'call_hangup' || data.type === 'call_reject') {
    endCall()
  } else if (data.type === 'call_ice') {
    handleCallIce(data)
  }
}

function handleIncomingCall(data) {
  callState.show = true
  callState.status = 'incoming'
  callState.peerName = data.callerName || '未知用户'
  callState.peerAvatar = data.callerAvatar || ''
}

function startCall() {
  const conv = currentConversation.value
  if (!conv) return
  callState.show = true
  callState.status = 'calling'
  callState.peerName = conv.name || '未知用户'
  callState.peerAvatar = conv.avatar || ''
  send({ type: 'call_offer', to: conv.targetUserId })
}

function handleCallIce(data) {}

function hangupCall() {
  send({ type: 'call_hangup', conversationId: currentChatId.value })
  endCall()
}

function endCall() {
  callState.show = false
  callState.status = 'idle'
  stopCallTimer()
}

function toggleMute() {
  callState.muted = !callState.muted
}

function startCallTimer() {
  stopCallTimer()
  callState.duration = 0
  callTimer = setInterval(() => { callState.duration++ }, 1000)
}

function stopCallTimer() {
  if (callTimer) { clearInterval(callTimer); callTimer = null }
}

onMounted(async () => {
  if (!user.value.id) {
    clearAuth()
    router.push('/login')
    return
  }
  connect(user.value.id)
  unsubWs = onMessage(handleWsMessage)
  await Promise.all([loadConversations(), loadFriends(), loadFriendRequests()])
})

onUnmounted(() => {
  disconnect()
  if (unsubWs) unsubWs()
  stopCallTimer()
  if (streamAbort) streamAbort()
})
</script>
