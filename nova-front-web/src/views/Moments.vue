<template>
  <div class="moments-page">
    <div class="moments-cover" :style="{ backgroundImage: coverBg ? `url(${coverBg})` : '' }">
      <div class="cover-overlay"></div>
      <button class="btn-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <div class="cover-user-info">
        <img v-if="currentUser.avatar && !avatarError" :src="currentUser.avatar" class="cover-avatar" @error="avatarError = true" alt="">
        <div v-else class="cover-avatar-placeholder">{{ (currentUser.nickname || '我')[0] }}</div>
        <span class="cover-nickname">{{ currentUser.nickname || currentUser.username || '我' }}</span>
      </div>
    </div>

    <div class="moments-scroll" ref="scrollEl" @scroll="onScroll">
      <div v-if="moments.length === 0 && !loading" class="moments-empty">
        <div class="empty-icon">🌍</div>
        <p>暂无动态</p>
        <p class="empty-sub">发表第一条朋友圈吧</p>
      </div>

      <MomentCard
        v-for="m in moments"
        :key="m.id"
        :moment="m"
        :current-user-id="currentUserId"
        @like="doLike(m)"
        @unlike="doUnlike(m)"
        @delete="doDelete(m)"
        @comment="doComment"
        @go-chat="goChat"
      />

      <div v-if="loading" class="moments-loading">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-if="!hasMore && moments.length > 0" class="moments-end">— 已经到底了 —</div>
    </div>

    <div class="moments-toolbar">
      <button class="toolbar-btn camera-btn" @click="showEditor = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
      </button>
    </div>

    <teleport to="body">
      <div v-if="showEditor" class="modal-overlay" @click.self="showEditor = false">
        <div class="modal-card editor-card">
          <div class="modal-header">
            <button class="btn-cancel" @click="showEditor = false">取消</button>
            <h3>发表动态</h3>
            <button class="btn-publish-confirm" @click="doPublish" :disabled="!editorContent.trim() || publishing">
              {{ publishing ? '发布中...' : '发布' }}
            </button>
          </div>
          <div class="modal-body">
            <textarea
              v-model="editorContent"
              class="editor-textarea"
              placeholder="这一刻的想法..."
              rows="4"
            ></textarea>
            <div v-if="editorImages.length" class="editor-images">
              <div v-for="(img, idx) in editorImages" :key="idx" class="editor-img-wrap">
                <img :src="img" alt="">
                <button class="editor-img-del" @click="editorImages.splice(idx, 1)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3"><path d="M18 6L6 18M6 6l12 12"/></svg>
                </button>
              </div>
            </div>
            <div class="editor-toolbar">
              <label class="tool-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                <input type="file" accept="image/*" multiple @change="onImagesSelected" hidden>
              </label>
              <label class="tool-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </label>
              <span class="tool-location">📍 所在位置</span>
              <span class="tool-mention">@ 提醒谁看</span>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getTimeline, publishMoment, deleteMoment, likeMoment, unlikeMoment, commentMoment } from '../api/moment'
import MomentCard from '../components/MomentCard.vue'

const props = defineProps({
  currentUserId: { type: Number, default: null },
  userInfo: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['back', 'go-chat'])

const moments = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const scrollEl = ref(null)

const showEditor = ref(false)
const editorContent = ref('')
const editorImages = ref([])
const publishing = ref(false)

const avatarError = ref(false)
const coverBg = computed(() => {
  const covers = [
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800',
    'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800',
    'https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=800',
    'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800',
    'https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=800'
  ]
  return covers[props.currentUserId % covers.length]
})

const currentUser = computed(() => ({
  nickname: props.userInfo?.nickname || '',
  username: props.userInfo?.username || '',
  avatar: props.userInfo?.avatar || ''
}))

async function loadMoments(reset = false) {
  if (loading.value) return
  if (reset) { page.value = 1; hasMore.value = true; moments.value = [] }
  loading.value = true
  try {
    const res = await getTimeline(page.value, 10)
    const list = res.data || []
    if (reset) {
      moments.value = list
    } else {
      moments.value = [...moments.value, ...list]
    }
    hasMore.value = list.length >= 10
    page.value++
  } catch {
  } finally {
    loading.value = false
  }
}

function onScroll() {
  const el = scrollEl.value
  if (!el || !hasMore.value || loading.value) return
  if (el.scrollHeight - el.scrollTop - el.clientHeight < 100) {
    loadMoments()
  }
}

function doLike(m) {
  likeMoment(m.id).then(res => {
    const idx = moments.value.findIndex(x => x.id === m.id)
    if (idx >= 0 && res.data) moments.value[idx] = res.data
  })
}

function doUnlike(m) {
  unlikeMoment(m.id).then(res => {
    const idx = moments.value.findIndex(x => x.id === m.id)
    if (idx >= 0 && res.data) moments.value[idx] = res.data
  })
}

function doDelete(m) {
  if (!confirm('确定删除这条动态？')) return
  deleteMoment(m.id).then(() => {
    moments.value = moments.value.filter(x => x.id !== m.id)
  })
}

function doComment({ momentId, content }) {
  commentMoment(momentId, content).then(res => {
    const idx = moments.value.findIndex(x => x.id === momentId)
    if (idx >= 0 && res.data) {
      const m = moments.value[idx]
      const updated = { ...m, comments: [...m.comments, res.data], commentCount: m.commentCount + 1 }
      moments.value[idx] = updated
    }
  })
}

function goChat(userId) {
  emit('go-chat', { userId })
}

async function doPublish() {
  if (!editorContent.value.trim() || publishing.value) return
  publishing.value = true
  try {
    await publishMoment(editorContent.value.trim(), editorImages.value)
    showEditor.value = false
    editorContent.value = ''
    editorImages.value = []
    page.value = 1
    loadMoments(true)
  } catch {
  } finally {
    publishing.value = false
  }
}

function onImagesSelected(e) {
  const files = Array.from(e.target.files)
  files.forEach(f => {
    const reader = new FileReader()
    reader.onload = ev => {
      editorImages.value.push(ev.target.result)
    }
    reader.readAsDataURL(f)
  })
  e.target.value = ''
}

onMounted(() => loadMoments(true))
</script>

<style scoped>
.moments-page {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ededed;
  overflow: hidden;
}

.moments-cover {
  position: relative;
  height: 200px;
  background-size: cover;
  background-position: center;
  flex-shrink: 0;
}
.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.15), rgba(0,0,0,0.35));
}
.btn-back {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.btn-back svg { width: 16px; height: 16px; }

.cover-user-info {
  position: absolute;
  bottom: 12px;
  left: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 10;
}
.cover-avatar {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 2px solid #fff;
  object-fit: cover;
  box-shadow: 0 1px 6px rgba(0,0,0,0.25);
}
.cover-avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 2px solid #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 1px 6px rgba(0,0,0,0.25);
}
.cover-nickname {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0,0,0,0.4);
}

.moments-scroll {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
}
.moments-scroll::-webkit-scrollbar { width: 4px; }
.moments-scroll::-webkit-scrollbar-thumb { background: #ccc; border-radius: 2px; }

.moments-empty {
  text-align: center;
  padding: 50px 20px;
  color: #999;
}
.empty-icon { font-size: 36px; margin-bottom: 10px; }
.empty-sub { font-size: 13px; color: #bbb; margin-top: 4px; }

.moments-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #999;
  font-size: 13px;
}
.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-top-color: #07C160;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.moments-end {
  text-align: center;
  padding: 20px;
  color: #bbb;
  font-size: 12px;
}

.moments-toolbar {
  position: fixed;
  right: 16px;
  bottom: 60px;
  z-index: 50;
}
.toolbar-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.55);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  backdrop-filter: blur(4px);
  transition: all 0.2s;
}
.toolbar-btn:hover { background: rgba(0,0,0,0.7); transform: scale(1.05); }
.camera-btn svg { width: 18px; height: 18px; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.modal-card {
  width: 400px;
  max-height: 75vh;
  background: #fff;
  border-radius: 12px;
  animation: scaleIn 0.25s ease;
  overflow: hidden;
}
@keyframes scaleIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eee;
}
.btn-cancel {
  background: none;
  border: none;
  color: #333;
  font-size: 15px;
  padding: 4px 12px;
  cursor: pointer;
}
.modal-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}
.btn-publish-confirm {
  background: none;
  border: none;
  color: #07C160;
  font-size: 15px;
  font-weight: 500;
  padding: 4px 12px;
  cursor: pointer;
}
.btn-publish-confirm:disabled { color: #ccc; cursor: default; }

.modal-body { padding: 12px 16px; }

.editor-textarea {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  min-height: 80px;
  font-family: inherit;
}
.editor-textarea::placeholder { color: #bbb; }

.editor-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.editor-img-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 6px;
  overflow: hidden;
}
.editor-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.editor-img-del {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.editor-img-del svg { width: 10px; height: 10px; }

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.tool-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #576B95;
}
.tool-item svg { width: 22px; height: 22px; }
.tool-location,
.tool-mention {
  font-size: 13px;
  color: #576B95;
  cursor: pointer;
}
</style>