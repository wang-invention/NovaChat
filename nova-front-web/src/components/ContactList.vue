<template>
  <div class="contact-list-wrap">
    <div class="contact-scroll" ref="scrollEl">
      <div v-if="requests.length > 0">
        <div class="contact-section-title">新的朋友</div>
        <div v-for="req in requests" :key="req.id" class="request-item">
          <img v-if="req.avatar && !imgErrors['req_' + req.id]" :src="req.avatar" class="request-avatar" @error="imgErrors['req_' + req.id] = true" alt="">
          <div v-else class="request-avatar-placeholder">{{ (req.senderName || '?')[0] }}</div>
          <div class="request-info">
            <div class="request-name">{{ req.senderName || req.senderNickname || '未知用户' }}</div>
            <div class="request-msg">{{ req.message || '请求添加您为好友' }}</div>
          </div>
          <div class="request-actions">
            <template v-if="req.status === 'pending'">
              <button class="btn-accept" @click="$emit('accept', req.id)">接受</button>
              <button class="btn-reject" @click="$emit('reject', req.id)">拒绝</button>
            </template>
            <span v-else class="request-status">{{ req.status === 'accepted' ? '已添加' : '已拒绝' }}</span>
          </div>
        </div>
      </div>

      <div v-if="groupedFriends.length === 0 && requests.length === 0" class="contact-empty">
        暂无好友
      </div>

      <div v-for="group in groupedFriends" :key="group.letter" :ref="el => setSectionRef(el, group.letter)" class="contact-group">
        <div class="contact-group-header">{{ group.letter }}</div>
        <div v-for="friend in group.list" :key="friend.friendId || friend.id || friend.userId" class="contact-item" @click="$emit('chat', friend)">
          <img v-if="(friend.avatar || friend.targetAvatar) && !imgErrors['f_' + (friend.friendId || friend.id || friend.userId)]"
               :src="friend.avatar || friend.targetAvatar" class="contact-avatar"
               @error="imgErrors['f_' + (friend.friendId || friend.id || friend.userId)] = true" alt="">
          <div v-else class="contact-avatar-placeholder" :style="{ background: avatarColor(friend.nickname || friend.username) }">
            {{ (friend.nickname || friend.username || '?')[0] }}
          </div>
          <div class="contact-info">
            <div class="contact-name">{{ friend.remark || friend.nickname || friend.username }}</div>
            <div class="contact-sub">{{ friend.signature || friend.bio || '' }}</div>
          </div>
          <span class="contact-action">发消息</span>
        </div>
      </div>
    </div>

    <div class="contact-index-bar" @touchstart.prevent="onIndexTouchStart" @touchmove.prevent="onIndexTouchMove" @touchend.prevent>
      <span
        v-for="letter in indexLetters"
        :key="letter"
        class="index-letter"
        :class="{ active: activeLetter === letter }"
        @click="scrollToLetter(letter)"
      >{{ letter }}</span>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'

const props = defineProps({
  friends: { type: Array, default: () => [] },
  requests: { type: Array, default: () => [] }
})

defineEmits(['chat', 'accept', 'reject'])

const imgErrors = reactive({})
const scrollEl = ref(null)
const sectionRefs = reactive({})
const activeLetter = ref('')

const groupedFriends = computed(() => {
  const map = {}
  for (const f of props.friends) {
    const letter = (f.initial || '#').toUpperCase()
    if (!map[letter]) map[letter] = []
    map[letter].push(f)
  }
  const letters = Object.keys(map).sort()
  return letters.map(letter => ({ letter, list: map[letter] }))
})

const indexLetters = computed(() => {
  const letters = groupedFriends.value.map(g => g.letter)
  if (!letters.includes('#')) letters.push('#')
  return letters.sort()
})

function setSectionRef(el, letter) {
  if (el) sectionRefs[letter] = el
}

function scrollToLetter(letter) {
  activeLetter.value = letter
  const el = sectionRefs[letter]
  if (el && scrollEl.value) {
    scrollEl.value.scrollTo({ top: el.offsetTop - 8, behavior: 'smooth' })
  }
}

function onIndexTouchStart(e) {
  const letter = getLetterFromTouch(e.touches[0])
  if (letter) scrollToLetter(letter)
}

function onIndexTouchMove(e) {
  const letter = getLetterFromTouch(e.touches[0])
  if (letter && letter !== activeLetter.value) scrollToLetter(letter)
}

function getLetterFromTouch(touch) {
  const el = document.querySelector('.contact-index-bar')
  if (!el) return null
  const rect = el.getBoundingClientRect()
  const y = touch.clientY - rect.top
  const h = rect.height / indexLetters.value.length
  const idx = Math.floor(y / h)
  return indexLetters.value[Math.min(Math.max(idx, 0), indexLetters.value.length - 1)]
}

function avatarColor(name) {
  const colors = ['#07C160', '#10AEFF', '#FF6B6B', '#FFD93D', '#6C5CE7', '#00B894']
  const idx = (name || '?').charCodeAt(0) % colors.length
  return colors[idx]
}
</script>

<style scoped>
.contact-list-wrap {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.contact-scroll {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

.contact-empty {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
  font-size: 14px;
}

.contact-section-title {
  padding: 6px 16px;
  font-size: 12px;
  color: #999;
  background: #f7f7f7;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}
.request-item:hover { background: #f9f9f9; }

.request-avatar { width: 40px; height: 40px; border-radius: 6px; object-fit: cover; }
.request-avatar-placeholder {
  width: 40px; height: 40px; border-radius: 6px;
  background: #07C160; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: bold; flex-shrink: 0;
}
.request-info { flex: 1; min-width: 0; }
.request-name { font-size: 14px; color: #333; font-weight: 500; }
.request-msg { font-size: 12px; color: #999; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.request-actions { display: flex; gap: 6px; flex-shrink: 0; }
.btn-accept {
  padding: 3px 10px; border-radius: 4px; border: none;
  background: #07C160; color: #fff; font-size: 12px; cursor: pointer;
}
.btn-reject {
  padding: 3px 10px; border-radius: 4px; border: 1px solid #ddd;
  background: #fff; color: #666; font-size: 12px; cursor: pointer;
}
.request-status { font-size: 12px; color: #999; }

.contact-group { }

.contact-group-header {
  padding: 6px 16px;
  font-size: 13px;
  color: #999;
  background: #f7f7f7;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 1;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
}
.contact-item:hover { background: #f9f9f9; }

.contact-avatar { width: 42px; height: 42px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
.contact-avatar-placeholder {
  width: 42px; height: 42px; border-radius: 6px;
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; font-weight: bold; flex-shrink: 0;
}
.contact-info { flex: 1; min-width: 0; }
.contact-name {
  font-size: 14px; color: #333; font-weight: 500;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.contact-sub {
  font-size: 12px; color: #999; margin-top: 2px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.contact-action {
  font-size: 12px; color: #07C160; flex-shrink: 0;
}

.contact-index-bar {
  position: absolute;
  right: 2px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  z-index: 10;
  touch-action: none;
}

.index-letter {
  font-size: 10px;
  color: #10AEFF;
  padding: 0 2px;
  cursor: pointer;
  user-select: none;
  line-height: 1.4;
  transition: color 0.1s;
}

.index-letter.active {
  color: #FF6B6B;
  font-weight: 700;
}
</style>
