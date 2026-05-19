<template>
  <div class="toast-container">
    <transition-group name="toast">
      <div v-for="t in list" :key="t.id" class="toast" :class="'toast-' + t.type">
        <span class="toast-icon">
          <svg v-if="t.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6L9 17l-5-5"/></svg>
          <svg v-else-if="t.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          <svg v-else-if="t.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        </span>
        <span class="toast-msg">{{ t.message }}</span>
        <button class="toast-close" @click="$emit('remove', t.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
defineProps({ list: { type: Array, default: () => [] } })
defineEmits(['remove'])
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
  max-width: 380px;
}
.toast {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  font-size: 14px;
  color: #333;
  min-height: 44px;
}
.toast-success { border-left: 4px solid #07C160; }
.toast-error { border-left: 4px solid #ff6b6b; }
.toast-warning { border-left: 4px solid #FFD93D; }
.toast-info { border-left: 4px solid #10AEFF; }
.toast-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
}
.toast-success .toast-icon { color: #07C160; }
.toast-error .toast-icon { color: #ff6b6b; }
.toast-warning .toast-icon { color: #e6a23c; }
.toast-info .toast-icon { color: #10AEFF; }
.toast-msg { flex: 1; line-height: 1.4; word-break: break-word; }
.toast-close {
  flex-shrink: 0;
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0.45;
  padding: 2px;
  transition: opacity 0.2s;
}
.toast-close:hover { opacity: 1; }
.toast-close svg { width: 14px; height: 14px; }

.toast-enter-active {
  transition: all 0.25s ease-out;
}
.toast-leave-active {
  transition: all 0.2s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(40px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(40px);
}
</style>