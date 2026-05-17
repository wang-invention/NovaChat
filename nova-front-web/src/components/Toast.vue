<template>
  <div :class="['toast', { show: visible, success: type === 'success' }]">{{ message }}</div>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
const message = ref('')
const type = ref('')
let timer = null

function show(msg, t) {
  message.value = msg
  type.value = t || ''
  visible.value = true
  clearTimeout(timer)
  timer = setTimeout(() => { visible.value = false }, 2000)
}

defineExpose({ show })
</script>