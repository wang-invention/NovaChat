<template>
  <div class="call-overlay">
    <div class="call-bg"></div>
    <div class="call-content">
      <div class="call-peer-avatar">
        <img v-if="state.peerAvatar" :src="state.peerAvatar" alt="">
        <div v-else style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;background:rgba(255,255,255,0.1);">
          <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" style="width:40px;height:40px;"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </div>
      </div>
      <div class="call-peer-name">{{ state.peerName }}</div>
      <div class="call-status-text">
        {{ state.status === 'calling' ? '正在呼叫...' : state.status === 'incoming' ? '邀请你语音通话...' : state.status === 'connected' ? '通话中' : '' }}
      </div>
      <div v-if="state.status === 'connected'" class="call-duration">{{ formatDuration(state.duration) }}</div>
    </div>
    <div class="call-actions">
      <button class="call-action-btn" :class="{ muted: state.muted }" @click="$emit('toggleMute')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 5L6 9H2v6h4l5 4V5z"/><path v-if="state.muted" d="M23 9l-6 6m0-6l6 6"/></svg>
        <span>{{ state.muted ? '取消静音' : '静音' }}</span>
      </button>
      <button class="call-action-btn hangup" @click="$emit('hangup')">
        <svg viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M12 9c-1.6 0-3.15.25-4.6.72v3.1c0 .39-.23.74-.56.9-.98.49-1.87 1.12-2.66 1.85-.18.18-.43.28-.7.28-.28 0-.53-.11-.71-.29L.29 13.08c-.18-.17-.29-.42-.29-.7 0-.28.11-.53.29-.71C3.34 8.78 7.46 7 12 7s8.66 1.78 11.71 4.67c.18.18.29.43.29.71 0 .28-.11.53-.29.71l-2.48 2.48c-.18.18-.43.29-.71.29-.27 0-.52-.11-.7-.28-.79-.74-1.69-1.36-2.67-1.85-.33-.16-.56-.5-.56-.9v-3.1C15.15 9.25 13.6 9 12 9z"/></svg>
        <span>挂断</span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  state: { type: Object, required: true }
})

defineEmits(['hangup', 'toggleMute'])

function formatDuration(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0')
}
</script>