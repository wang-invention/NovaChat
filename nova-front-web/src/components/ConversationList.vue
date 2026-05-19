<template>
  <div>
    <div v-if="conversations.length === 0" style="text-align:center;padding:40px;color:var(--text-muted);font-size:14px;">
      暂无会话
    </div>
    <div
      v-for="conv in conversations"
      :key="conv.id"
      class="conv-item"
      :class="{ active: conv.id === activeId }"
      @click="$emit('select', conv)"
    >
      <img v-if="conv.avatar && !imgErrors[conv.id]" :src="conv.avatar" class="conv-avatar" @error="imgErrors[conv.id] = true" alt="">
      <div v-else class="conv-avatar-placeholder" :style="{ background: avatarColor(conv.name) }">
        {{ (conv.name || '?')[0] }}
      </div>
      <div class="conv-info">
        <div class="conv-name">
          {{ conv.name }}
          <span v-if="conv.isGroup" class="group-badge">群</span>
          <span v-else-if="conv.id === 'ai_assistant' || conv.type === 'ai'" class="ai-badge">AI</span>
        </div>
        <div class="conv-last-msg">{{ conv.lastMessage || conv.lastMsg || '' }}</div>
      </div>
      <div class="conv-meta">
        <span class="conv-time">{{ formatTime(conv.lastTime || conv.updatedAt) }}</span>
        <span v-if="conv.unreadCount" class="conv-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

defineProps({
  conversations: { type: Array, default: () => [] },
  activeId: { type: String, default: '' }
})

defineEmits(['select'])

const imgErrors = reactive({})

function avatarColor(name) {
  const colors = ['#07C160', '#10AEFF', '#FF6B6B', '#FFD93D', '#6C5CE7', '#00B894']
  const idx = (name || '?').charCodeAt(0) % colors.length
  return colors[idx]
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}
</script>