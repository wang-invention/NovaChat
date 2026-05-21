<template>
  <div class="moment-card">
    <div class="moment-left">
      <img v-if="moment.avatar && !imgError" :src="moment.avatar" class="moment-avatar" @error="imgError = true" alt="" @click="$emit('go-chat', moment.userId)">
      <div v-else class="moment-avatar-placeholder" @click="$emit('go-chat', moment.userId)">{{ (moment.nickname || '?')[0] }}</div>
    </div>

    <div class="moment-right">
      <div class="moment-header">
        <span class="moment-nickname" @click="$emit('go-chat', moment.userId)">{{ moment.nickname || moment.username }}</span>
        <button class="btn-more" @click="showMenu = !showMenu">
          <svg viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="5" r="2"/><circle cx="12" cy="12" r="2"/><circle cx="12" cy="19" r="2"/></svg>
        </button>
        <div v-if="showMenu && moment.userId === currentUserId" class="dropdown-menu">
          <button class="menu-item delete-item" @click="$emit('delete', moment.id); showMenu = false">删除</button>
          <button class="menu-item" @click="showMenu = false">取消</button>
        </div>
      </div>

      <div class="moment-content" v-if="moment.content">{{ moment.content }}</div>

      <div v-if="moment.images && moment.images.length" class="moment-images" :class="'grid-' + Math.min(moment.images.length, 9)">
        <img v-for="(img, idx) in moment.images" :key="idx" :src="img" class="moment-img" @click="previewIdx = idx" loading="lazy" alt="">
      </div>

      <div class="moment-meta">
        <span class="moment-time">{{ formatTime(moment.createTime) }}</span>
        <span v-if="!showActions" class="moment-action-trigger" @click="showActions = true">···</span>
      </div>

      <div v-if="showActions || moment.likeCount > 0 || moment.commentCount > 0" class="moment-interactions">
        <div v-if="showActions" class="action-bar">
          <button class="action-btn like-btn" :class="{ active: moment.liked }" @click="moment.liked ? $emit('unlike', moment.id) : $emit('like', moment.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            赞
          </button>
          <button class="action-btn comment-btn" @click="showCommentInput = !showCommentInput">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            评论
          </button>
        </div>

        <div v-if="moment.likeCount > 0 || moment.commentCount > 0" class="interaction-content">
          <div v-if="moment.likes && moment.likes.length" class="likes-section">
            <svg class="like-icon" viewBox="0 0 24 24" fill="#FF6B6B"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            <template v-for="(l, idx) in moment.likes" :key="l.userId">
              <span class="like-user" @click="$emit('go-chat', l.userId)">{{ l.nickname }}</span><template v-if="idx < moment.likes.length - 1">，</template>
            </template>
          </div>

          <div v-if="moment.comments && moment.comments.length" class="comments-section">
            <div v-for="c in moment.comments" :key="c.id" class="comment-row">
              <span class="comment-user" @click="$emit('go-chat', c.userId)">{{ c.nickname || c.username }}</span>
              <template v-if="c.replyToUserId">
                <span class="comment-reply-text">回复</span>
                <span class="comment-user" @click="$emit('go-chat', c.replyToUserId)">{{ c.replyToNickname || c.replyToUsername }}</span>
              </template>
              <span class="comment-colon">：</span>
              <span class="comment-body">{{ c.content }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="showCommentInput" class="comment-input-area">
        <input
          ref="commentInputRef"
          v-model="commentText"
          class="comment-input"
          placeholder="写评论..."
          @keydown.enter.prevent="doComment"
          @blur="onCommentBlur"
        >
      </div>
    </div>
  </div>

  <teleport to="body">
    <div v-if="previewIdx !== null" class="preview-overlay" @click.self="previewIdx = null">
      <button class="preview-close" @click="previewIdx = null">
        <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3"><path d="M18 6L6 18M6 6l12 12"/></svg>
      </button>
      <button v-if="previewIdx > 0" class="preview-nav prev" @click="previewIdx--">
        <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
      </button>
      <img :src="moment.images[previewIdx]" class="preview-image" @click.stop alt="">
      <button v-if="previewIdx < moment.images.length - 1" class="preview-nav next" @click="previewIdx++">
        <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
      </button>
      <div class="preview-counter">{{ previewIdx + 1 }} / {{ moment.images.length }}</div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  moment: { type: Object, required: true },
  currentUserId: { type: Number, default: null }
})

const emit = defineEmits(['like', 'unlike', 'delete', 'comment', 'go-chat'])

const imgError = ref(false)
const showMenu = ref(false)
const showActions = ref(false)
const showCommentInput = ref(false)
const commentText = ref('')
const previewIdx = ref(null)
const commentInputRef = ref(null)

function doComment() {
  if (!commentText.value.trim()) return
  emit('comment', { momentId: props.moment.id, content: commentText.value.trim() })
  commentText.value = ''
  showCommentInput.value = false
}

async function openComment() {
  showCommentInput.value = true
  await nextTick()
  commentInputRef.value?.focus()
}

function onCommentBlur() {
  setTimeout(() => {
    if (!commentText.value.trim()) {
      showCommentInput.value = false
    }
  }, 200)
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  const y = d.getFullYear()
  const m = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const h = pad(d.getHours())
  const min = pad(d.getMinutes())
  if (y === now.getFullYear()) return m + '-' + day + ' ' + h + ':' + min
  return y + '-' + m + '-' + day + ' ' + h + ':' + min
}

function pad(v) { return v < 10 ? '0' + v : '' + v }
</script>

<style scoped>
.moment-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  position: relative;
}

.moment-left {
  flex-shrink: 0;
  padding-top: 2px;
}
.moment-avatar {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
  transition: opacity 0.2s;
}
.moment-avatar:hover { opacity: 0.85; }
.moment-avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: bold;
  cursor: pointer;
}

.moment-right { flex: 1; min-width: 0; }

.moment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  position: relative;
}
.moment-nickname {
  font-size: 15px;
  font-weight: 500;
  color: #576B95;
  cursor: pointer;
}
.moment-nickname:hover { text-decoration: underline; }

.btn-more {
  background: none;
  border: none;
  width: 20px;
  height: 20px;
  color: #b0b0b0;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-more svg { width: 16px; height: 16px; fill: currentColor; }

.dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  background: #4C4C4C;
  border-radius: 4px;
  overflow: hidden;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  min-width: 80px;
}
.menu-item {
  display: block;
  width: 100%;
  padding: 8px 14px;
  background: none;
  border: none;
  color: #fff;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}
.menu-item:hover { background: #555; }
.delete-item { color: #FF6B6B; }

.moment-content {
  font-size: 15px;
  color: #333;
  line-height: 1.65;
  word-break: break-all;
  margin-bottom: 8px;
}

.moment-images {
  display: grid;
  gap: 4px;
  margin-bottom: 8px;
  max-width: 290px;
}
.grid-1 { grid-template-columns: 1fr; max-width: 220px; }
.grid-2 { grid-template-columns: repeat(2, 1fr); }
.grid-3 { grid-template-columns: repeat(3, 1fr); }
.grid-4 { grid-template-columns: repeat(2, 1fr); }
.grid-5, .grid-6 { grid-template-columns: repeat(3, 1fr); }
.grid-7, .grid-8, .grid-9 { grid-template-columns: repeat(3, 1fr); }
.moment-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 3px;
  cursor: pointer;
  transition: transform 0.15s;
}
.moment-img:hover { transform: scale(1.03); }

.moment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.moment-time {
  font-size: 11px;
  color: #b0b0b0;
}
.moment-action-trigger {
  font-size: 14px;
  color: #b0b0b0;
  cursor: pointer;
  line-height: 1;
  letter-spacing: 1px;
  user-select: none;
}

.moment-interactions { margin-top: 4px; }

.action-bar {
  display: flex;
  gap: 20px;
  padding: 6px 0;
  border-top: 1px solid #f0f0f0;
  margin-bottom: 6px;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  font-size: 13px;
  color: #888;
  cursor: pointer;
  padding: 2px 0;
  transition: color 0.2s;
}
.action-btn svg { width: 16px; height: 16px; }
.action-btn:hover { color: #576B95; }
.like-btn.active { color: #FF6B6B; }
.like-btn.active svg { stroke: #FF6B6B; fill: #FF6B6B; }

.interaction-content {
  background: #f7f7f7;
  border-radius: 4px;
  padding: 8px 10px;
  font-size: 13px;
}

.likes-section {
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 1.6;
  color: #576B95;
}
.like-icon { width: 14px; height: 14px; flex-shrink: 0; margin-right: 2px; }
.like-user { cursor: pointer; }
.like-user:hover { text-decoration: underline; }

.comments-section { margin-top: 4px; }
.comment-row {
  line-height: 1.6;
  color: #333;
}
.comment-user {
  color: #576B95;
  cursor: pointer;
}
.comment-user:hover { text-decoration: underline; }
.comment-reply-text { color: #576B95; margin: 0 2px; }
.comment-colon { color: #333; }
.comment-body { color: #333; word-break: break-all; }

.comment-input-area {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
.comment-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}
.comment-input:focus { border-color: #07C160; }
.comment-input::placeholder { color: #bbb; }

.preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.92);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.preview-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: none;
  border: none;
  width: 36px;
  height: 36px;
  cursor: pointer;
  z-index: 10;
}
.preview-close svg { width: 22px; height: 22px; }

.preview-image {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 4px;
}

.preview-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255,255,255,0.15);
  border: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: background 0.2s;
  z-index: 10;
}
.preview-nav:hover { background: rgba(255,255,255,0.3); }
.preview-nav.prev { left: 20px; }
.preview-nav.next { right: 20px; }
.preview-nav svg { width: 24px; height: 24px; }

.preview-counter {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  background: rgba(0,0,0,0.4);
  padding: 4px 12px;
  border-radius: 12px;
}
</style>