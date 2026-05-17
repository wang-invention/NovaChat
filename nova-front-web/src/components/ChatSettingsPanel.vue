<template>
  <div class="chat-settings-panel">
    <div class="chat-settings-header">
      <h3>聊天设置</h3>
      <button class="modal-close" @click="$emit('close')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
      </button>
    </div>
    <div class="chat-settings-body">
      <div class="settings-section">
        <span class="settings-label">聊天背景</span>
        <div class="bg-options">
          <div
            v-for="bg in bgList"
            :key="bg.value"
            class="bg-option"
            :class="{ active: chatBg === bg.value }"
            :style="{ background: bg.value || '#f5f5f5' }"
            @click="$emit('bgChange', bg.value)"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  chatBg: { type: String, default: '' }
})

defineEmits(['close', 'bgChange'])

const bgList = [
  { value: '' },
  { value: '#e8f5e9' },
  { value: '#e3f2fd' },
  { value: '#fff3e0' },
  { value: '#fce4ec' },
  { value: '#f3e5f5' }
]
</script>