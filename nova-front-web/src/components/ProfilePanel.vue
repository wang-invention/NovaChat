<template>
  <div class="profile-panel">
    <div class="profile-header">
      <button class="btn-back" @click="$emit('close')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <h3>个人资料</h3>
    </div>
    <div class="profile-body">
      <div class="profile-avatar-section">
        <img v-if="user.avatar" :src="user.avatar" class="profile-avatar-img" alt="">
        <div v-else class="profile-avatar-img" :style="{ background: avatarBg }"></div>
        <div>
          <button class="btn btn-sm btn-primary" style="margin-top:4px;" @click="$refs.avatarInput.click()">更换头像</button>
          <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarChange">
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">昵称</label>
        <div class="input-wrap">
          <input v-model="form.nickname" type="text" class="form-input" placeholder="请输入昵称" maxlength="32">
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">性别</label>
        <div class="input-wrap">
          <select v-model="form.gender" class="form-input" style="appearance:auto;">
            <option value="">不设置</option>
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">手机号</label>
        <div class="input-wrap">
          <input v-model="form.phone" type="text" class="form-input" placeholder="请输入手机号" maxlength="11">
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">邮箱</label>
        <div class="input-wrap">
          <input v-model="form.email" type="email" class="form-input" placeholder="请输入邮箱" maxlength="64">
        </div>
      </div>

      <button class="btn btn-primary btn-block" :class="{ loading }" :disabled="loading" @click="handleSave" style="margin-top:8px;">
        <span class="btn-text">保存</span>
        <span class="btn-spinner"></span>
      </button>

      <button class="btn btn-block" style="color:var(--danger);margin-top:4px;" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUser, setUser, clearAuth } from '../utils/auth'
import { updateProfile, uploadAvatar } from '../api/user'

const emit = defineEmits(['close', 'updated'])
const router = useRouter()

const user = ref(getUser() || {})
const form = reactive({
  nickname: user.value.nickname || '',
  gender: user.value.gender || '',
  phone: user.value.phone || '',
  email: user.value.email || ''
})
const loading = ref(false)

const avatarBg = computed(() => {
  const colors = ['#07C160', '#10AEFF', '#FF6B6B', '#FFD93D', '#6C5CE7', '#00B894']
  const idx = (user.value.username || '').charCodeAt(0) % colors.length
  return colors[idx]
})

async function handleSave() {
  loading.value = true
  try {
    const res = await updateProfile({ ...form })
    const updated = res.data || res
    setUser({ ...user.value, ...updated })
    user.value = { ...user.value, ...updated }
    emit('updated', updated)
  } catch {
  } finally {
    loading.value = false
  }
}

async function onAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const res = await uploadAvatar(file)
    const avatar = res.data || res.url
    if (avatar) {
      setUser({ ...user.value, avatar })
      user.value.avatar = avatar
      emit('updated', { avatar })
    }
  } catch {}
  e.target.value = ''
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}
</script>