<template>
  <div class="chat-area" :style="{ background: chatBg || undefined }">
    <div class="chat-header">
      <div class="chat-header-info">
        <button class="btn-back" @click="$emit('back')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        </button>
        <img v-if="conversation?.avatar && !imgErrors['header']" :src="conversation.avatar" class="chat-avatar" @error="imgErrors['header'] = true" alt="">
        <div v-else class="chat-avatar-placeholder" :style="{ background: avatarColor(conversation?.name) }">{{ (conversation?.name || '?')[0] }}</div>
        <div class="chat-header-text">
          <span class="chat-name">{{ conversation?.name || '未知' }}</span>
          <span class="chat-status">{{ isStreaming ? 'AI 正在输入...' : (conversation?.isGroup ? '群聊' : '在线') }}</span>
        </div>
      </div>
      <div class="chat-header-actions">
        <button v-if="conversation?.isGroup" class="icon-btn" title="群聊信息" @click="$emit('group-info')">
          <svg viewBox="0 0 24 24" fill="currentColor"><circle cx="5" cy="12" r="2"/><circle cx="12" cy="12" r="2"/><circle cx="19" cy="12" r="2"/></svg>
        </button>
        <button class="icon-btn" title="语音通话" @click="$emit('call')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
        </button>
        <button class="icon-btn" title="聊天设置" @click="$emit('settings')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
        </button>
      </div>
    </div>

    <div class="msg-list" ref="msgList" @scroll="onScroll">
      <div v-if="loadingMore" class="msg-loading-more">加载中...</div>
      <template v-for="(msg, index) in messages" :key="msg.id">
        <div v-if="shouldShowTime(msg, index)" class="msg-time-divider">{{ formatTime(msg.createdAt) }}</div>
        <div class="msg-item" :class="msgClass(msg)" @contextmenu.prevent="onContextMenu($event, msg)">
          <img v-if="msgClass(msg) === 'self' && props.user?.avatar && !imgErrors['self_avatar']" :src="props.user.avatar" class="msg-avatar" @error="imgErrors['self_avatar'] = true" alt="">
          <div v-else-if="msgClass(msg) === 'self'" class="msg-avatar-placeholder">{{ (props.user?.nickname || props.user?.username || '?')[0] }}</div>
          <img v-else-if="msg.senderAvatar && !imgErrors['m_' + msg.id]" :src="msg.senderAvatar" class="msg-avatar" @error="imgErrors['m_' + msg.id] = true" alt="">
          <div v-else class="msg-avatar-placeholder" :style="{ background: avatarColor(msg.senderName) }">
            {{ (msg.senderName || '?')[0] }}
          </div>
          <div class="msg-bubble-wrap">
            <div v-if="msg.recalled" class="msg-bubble recalled">消息已撤回</div>
            <template v-else>
              <div v-if="msg.type === 'image'" class="msg-bubble" style="padding:4px;background:transparent;box-shadow:none;">
                <img :src="msg.content" class="msg-image" @click="previewImage(msg.content)" alt="">
              </div>
              <div v-else class="msg-bubble">
                {{ msg.content }}
                <div v-if="msg.typing" class="msg-typing"><span></span><span></span><span></span></div>
              </div>
            </template>
            <span v-if="msg.status === 'failed'" class="msg-status failed" @click="$emit('resend', msg)">发送失败，点击重试</span>
          </div>
        </div>
      </template>
    </div>

    <div v-if="contextMenu.show" class="msg-context-menu" :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }">
      <div class="ctx-item" @click="copyMsg">复制</div>
      <div v-if="canRecall" class="ctx-item danger" @click="recallMsg">撤回</div>
    </div>

    <div class="chat-input-area">
      <div class="chat-input-tools">
        <button class="tool-btn" title="表情" @click="showEmoji = !showEmoji; showPolish = false">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
        </button>
        <button class="tool-btn" title="图片" @click="$refs.imgInput.click()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
        </button>
        <input ref="imgInput" type="file" accept="image/*" style="display:none" @change="onImageSelect">
        <button class="tool-btn" title="AI 润色" @click="showPolish = !showPolish; showEmoji = false">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
        </button>
      </div>

      <div v-if="showEmoji" class="emoji-panel">
        <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
      </div>

      <div v-if="showPolish" class="polish-panel">
        <div class="polish-header">
          <span class="polish-title">AI 润色</span>
          <button class="polish-close" @click="showPolish = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div v-if="polishLoading" class="polish-loading">润色中...</div>
        <div v-else-if="polishResults.length === 0" class="polish-empty">输入文字后点击润色</div>
        <div v-else class="polish-results">
          <div v-for="(item, i) in polishResults" :key="i" class="polish-item" @click="applyPolish(item)">
            <span class="polish-label">{{ polishLabels[i] || '风格' + (i + 1) }}</span>
            <span class="polish-text">{{ typeof item === 'string' ? item : item.text }}</span>
          </div>
        </div>
      </div>

      <div class="chat-input-row">
        <textarea
          ref="msgInput"
          v-model="inputText"
          class="msg-input"
          placeholder="输入消息..."
          rows="1"
          @keydown.enter.exact.prevent="handleSend"
          @input="autoResize"
        ></textarea>
        <button class="btn-send" :disabled="!inputText.trim() && !isStreaming" @click="handleSend">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, reactive } from 'vue'
import { polishMessage } from '../api/ai'

const props = defineProps({
  conversation: { type: Object, default: null },
  messages: { type: Array, default: () => [] },
  isStreaming: { type: Boolean, default: false },
  streamingMsgId: { type: String, default: '' },
  chatBg: { type: String, default: '' },
  user: { type: Object, default: () => ({}) },
  loadingMore: { type: Boolean, default: false },
  hasMore: { type: Boolean, default: true }
})

const emit = defineEmits(['send', 'recall', 'back', 'settings', 'call', 'resend', 'group-info', 'load-more'])

const inputText = ref('')
const msgInput = ref(null)
const msgList = ref(null)
const showEmoji = ref(false)
const showPolish = ref(false)
const imgErrors = reactive({})
const polishLoading = ref(false)
const polishResults = ref([])
const contextMenu = ref({ show: false, x: 0, y: 0, msg: null })

const polishLabels = ['温柔', '幽默', '高情商', '正式']

const emojis = [
  '😀','😂','🤣','😍','😘','😜','😎','🤩','🥳','😢',
  '😡','👍','👎','👏','🙌','💪','🤝','❤️','🔥','⭐',
  '🎉','🎂','🍰','☕','🍺','🎵','📷','💡','💰','🎁',
  '😊','😋','🤔','😴','🥺','😭','🤗','🙏','💀','👻',
  '🐶','🐱','🌹','🌸','🌈','☀️','🌙','⚡','💧','🎯'
]

const currentUserId = computed(() => {
  return props.user?.id || props.user?.userId || (() => {
    const u = JSON.parse(localStorage.getItem('nova_user') || '{}')
    return u.id || u.userId
  })()
})

function msgClass(msg) {
  if (msg.role === 'assistant') return 'assistant'
  if (msg.senderId === 'ai_assistant') return 'assistant'
  if (msg.senderId === currentUserId.value) return 'self'
  return 'other'
}

function avatarColor(name) {
  const colors = ['#07C160', '#10AEFF', '#FF6B6B', '#FFD93D', '#6C5CE7', '#00B894']
  const idx = (name || '?').charCodeAt(0) % colors.length
  return colors[idx]
}

const canRecall = computed(() => {
  const msg = contextMenu.value.msg
  if (!msg) return false
  const elapsed = Date.now() - (msg.createdAt || 0)
  return elapsed < 120000
})

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  emit('send', text)
  inputText.value = ''
  showEmoji.value = false
  showPolish.value = false
  nextTick(() => { if (msgInput.value) msgInput.value.style.height = 'auto' })
}

function insertEmoji(emoji) {
  inputText.value += emoji
  msgInput.value?.focus()
}

function autoResize() {
  const el = msgInput.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 100) + 'px'
}

function onContextMenu(e, msg) {
  contextMenu.value = { show: true, x: e.clientX, y: e.clientY, msg }
  setTimeout(() => { contextMenu.value.show = false }, 3000)
}

function copyMsg() {
  const msg = contextMenu.value.msg
  if (msg) navigator.clipboard.writeText(msg.content)
  contextMenu.value.show = false
}

function recallMsg() {
  const msg = contextMenu.value.msg
  if (msg) emit('recall', msg.id)
  contextMenu.value.show = false
}

function onScroll() {
  contextMenu.value.show = false
  if (props.hasMore && !props.loadingMore && msgList.value && msgList.value.scrollTop <= 10) {
    emit('load-more')
  }
}

function onImageSelect(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    emit('send', reader.result)
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

async function doPolish() {
  const text = inputText.value.trim()
  if (!text) return
  polishLoading.value = true
  polishResults.value = []
  try {
    const res = await polishMessage(text)
    polishResults.value = res.results || res.data || []
  } catch {
    polishResults.value = []
  } finally {
    polishLoading.value = false
  }
}

function applyPolish(item) {
  inputText.value = typeof item === 'string' ? item : (item.text || '')
  showPolish.value = false
  msgInput.value?.focus()
}

function previewImage(url) {
  window.open(url, '_blank')
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  const isToday = d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
  const isYesterday = d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate() - 1
  const timeStr = pad(d.getHours()) + ':' + pad(d.getMinutes())
  if (isToday) return timeStr
  if (isYesterday) return '昨天 ' + timeStr
  return pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + timeStr
}

function shouldShowTime(msg, index) {
  if (index === 0) return true
  const prev = props.messages[index - 1]
  if (!prev || !prev.createdAt || !msg.createdAt) return true
  const diff = new Date(msg.createdAt).getTime() - new Date(prev.createdAt).getTime()
  return diff > 5 * 60 * 1000
}

watch(showPolish, (val) => {
  if (val) doPolish()
})

defineExpose({ scrollToBottom: () => { if (msgList.value) msgList.value.scrollTop = msgList.value.scrollHeight } })
</script>