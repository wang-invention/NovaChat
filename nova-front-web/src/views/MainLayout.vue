<template>
  <div class="main-page" :class="{ 'show-chat': showChat }">
    <!-- 左侧窄边栏 - 微信风格 -->
    <nav class="wechat-nav">
      <div class="nav-top">
        <div class="nav-avatar" @click="openProfile">
          <img v-if="user.avatar" :src="user.avatar" alt="">
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
        <DiscoverPanel v-else-if="activeTab === 'discover'" />
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div v-if="!currentChatId && !showProfile" class="chat-empty">
        <div class="empty-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        </div>
        <h2>NovaChat</h2>
        <p>选择一个会话开始聊天</p>
      </div>

      <ChatView
        v-if="currentChatId && !showProfile"
        :conversation="currentConversation"
        :messages="messages"
        :is-streaming="isStreaming"
        :streaming-msg-id="streamingMsgId"
        :chat-bg="chatBg"
        @send="sendMessage"
        @recall="recallMessage"
        @back="currentChatId = null"
        @settings="showChatSettings = !showChatSettings"
      />

      <ProfilePanel v-if="showProfile" @close="showProfile = false" @updated="onProfileUpdated" />

      <ChatSettingsPanel
        v-if="showChatSettings && currentChatId"
        :chat-bg="chatBg"
        @bg-change="chatBg = $event"
        @close="showChatSettings = false"
      />
    </main>

    <SearchModal v-if="showSearch" @close="showSearch = false" @chat="startChatWith" />
    <AddFriendModal v-if="showAddFriend" @close="showAddFriend = false" />

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
import { getConversations, getMessages, sendMessage as apiSendMessage, recallMessage as apiRecallMessage, getFriends, getFriendRequests, acceptFriendRequest, rejectFriendRequest, createConversation } from '../api/chat'
import { chatStream } from '../api/ai'
import ConversationList from '../components/ConversationList.vue'
import ContactList from '../components/ContactList.vue'
import DiscoverPanel from '../components/DiscoverPanel.vue'
import ChatView from '../components/ChatView.vue'
import ProfilePanel from '../components/ProfilePanel.vue'
import ChatSettingsPanel from '../components/ChatSettingsPanel.vue'
import SearchModal from '../components/SearchModal.vue'
import AddFriendModal from '../components/AddFriendModal.vue'
import CallOverlay from '../components/CallOverlay.vue'

const router = useRouter()
const user = ref(getUser() || {})
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
const chatBg = ref('')
const isStreaming = ref(false)
const streamingMsgId = ref('')
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
    conversations.value = (res.data || []).map(c => ({
      ...c,
      id: c.id || c.conversationId,
      name: c.name || c.peerName || c.peerNickname || '未知',
      avatar: c.avatar || c.peerAvatar || ''
    }))
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
  try {
    const res = await getMessages(currentChatId.value)
    messages.value = (res.data || []).map(m => ({
      ...m,
      id: m.id || m.messageId,
      senderId: m.senderId || m.fromUserId,
      senderName: m.senderName || m.fromUserName || '',
      content: m.content || '',
      type: m.type || m.messageType || 'text',
      createdAt: m.createdAt || m.createTime || Date.now()
    }))
    await nextTick()
    scrollToBottom()
  } catch {}
}

async function startChatWith(peer) {
  try {
    const res = await createConversation(peer.id || peer.userId)
    const conv = res.data
    conversations.value.unshift({
      ...conv,
      id: conv.id || conv.conversationId,
      name: peer.nickname || peer.username,
      avatar: peer.avatar || ''
    })
    selectChat(conv)
  } catch {
    const existing = conversations.value.find(c => c.peerId === (peer.id || peer.userId))
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
  const msg = {
    id: tempId,
    senderId: user.value.id,
    senderName: user.value.nickname || user.value.username,
    content: text,
    type: 'text',
    createdAt: Date.now(),
    status: 'sending'
  }
  messages.value.push(msg)
  await nextTick()
  scrollToBottom()

  try {
    const res = await apiSendMessage({ conversationId: currentChatId.value, content: text, type: 'text' })
    const idx = messages.value.findIndex(m => m.id === tempId)
    if (idx >= 0) {
      messages.value[idx] = { ...messages.value[idx], ...res.data, id: res.data.id || res.data.messageId, status: 'sent' }
    }
  } catch {
    const idx = messages.value.findIndex(m => m.id === tempId)
    if (idx >= 0) messages.value[idx].status = 'failed'
  }
}

async function sendAIMessage(text) {
  const userMsg = {
    id: 'ai_user_' + Date.now(),
    senderId: user.value.id,
    senderName: user.value.nickname || user.value.username,
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

function scrollToBottom() {
  nextTick(() => {
    const el = document.querySelector('.msg-list')
    if (el) el.scrollTop = el.scrollHeight
  })
}

function handleWsMessage(data) {
  if (data.type === 'message' || data.type === 'chat') {
    const msg = data.data || data
    const convId = msg.conversationId || data.conversationId
    if (convId === currentChatId.value) {
      const exists = messages.value.find(m => m.id === (msg.id || msg.messageId))
      if (!exists) {
        messages.value.push({
          ...msg,
          id: msg.id || msg.messageId,
          senderId: msg.senderId || msg.fromUserId,
          senderName: msg.senderName || msg.fromUserName || '',
          content: msg.content || '',
          type: msg.type || msg.messageType || 'text',
          createdAt: msg.createdAt || msg.createTime || Date.now()
        })
        scrollToBottom()
      }
    }
    loadConversations()
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
