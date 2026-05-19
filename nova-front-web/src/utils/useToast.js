import { ref } from 'vue'

const list = ref([])
let idCounter = 0

function show(message, type = 'info', duration = 3000) {
  const id = ++idCounter
  const item = { id, message, type, visible: true }
  list.value.push(item)
  if (duration > 0) {
    setTimeout(() => { remove(id) }, duration)
  }
  return id
}

function remove(id) {
  const idx = list.value.findIndex(t => t.id === id)
  if (idx >= 0) list.value.splice(idx, 1)
}

export function useToast() {
  return {
    list,
    success(msg, dur) { return show(msg, 'success', dur) },
    error(msg, dur) { return show(msg, 'error', dur) },
    warning(msg, dur) { return show(msg, 'warning', dur) },
    info(msg, dur) { return show(msg, 'info', dur) },
    remove
  }
}