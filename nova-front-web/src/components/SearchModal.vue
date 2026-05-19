<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-card">
      <div class="modal-header">
        <h3>搜索用户</h3>
        <button class="modal-close" @click="$emit('close')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
        </button>
      </div>
      <div class="modal-body">
        <div class="input-wrap">
          <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="keyword" type="text" class="form-input" placeholder="输入用户名搜索" @keydown.enter="doSearch">
        </div>
        <button class="btn btn-primary btn-block btn-sm" :disabled="!keyword.trim() || searching" @click="doSearch">
          {{ searching ? '搜索中...' : '搜索' }}
        </button>
        <div v-if="results.length > 0" class="search-results">
          <div v-for="user in results" :key="user.id || user.userId" class="search-result-item">
            <img v-if="user.avatar && !imgErrors[user.id || user.userId]" :src="user.avatar" class="search-result-avatar" @error="imgErrors[user.id || user.userId] = true" alt="">
            <div v-else class="search-result-avatar-placeholder">{{ (user.nickname || user.username || '?')[0] }}</div>
            <div class="search-result-info">
              <div class="search-result-name">{{ user.nickname || user.username }}</div>
            </div>
            <span class="search-result-action" @click="startChat(user)">发消息</span>
          </div>
        </div>
        <div v-else-if="searched" style="text-align:center;padding:20px;color:var(--text-muted);font-size:13px;">
          未找到用户
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { searchUser } from '../api/user'

const emit = defineEmits(['close', 'chat'])

const keyword = ref('')
const results = ref([])
const searched = ref(false)
const searching = ref(false)
const imgErrors = reactive({})

async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  searched.value = false
  try {
    const res = await searchUser(keyword.value.trim())
    results.value = res.data || []
    searched.value = true
  } catch {
    results.value = []
    searched.value = true
  } finally {
    searching.value = false
  }
}

function startChat(user) {
  emit('chat', user)
  emit('close')
}
</script>