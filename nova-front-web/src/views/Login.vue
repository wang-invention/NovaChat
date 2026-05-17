<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1 class="auth-logo">NovaChat</h1>
          <p class="auth-subtitle">欢迎回来，请登录您的账号</p>
        </div>
        <form class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              <input v-model="form.username" type="text" class="form-input" placeholder="请输入用户名" maxlength="32" autocomplete="username">
            </div>
            <span class="form-error">{{ errors.username }}</span>
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              <input v-model="form.password" :type="showPwd ? 'text' : 'password'" class="form-input" placeholder="请输入密码" maxlength="32" autocomplete="current-password">
              <button type="button" class="toggle-pwd" :class="{ show: showPwd }" @click="showPwd = !showPwd">
                <svg class="eye-icon eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                <svg class="eye-icon eye-on" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              </button>
            </div>
            <span class="form-error">{{ errors.password }}</span>
          </div>
          <button type="submit" class="btn btn-primary btn-block" :class="{ loading }" :disabled="loading">
            <span class="btn-text">登录</span>
            <span class="btn-spinner"></span>
          </button>
          <p class="auth-footer">没有账号？<router-link to="/register" class="auth-link">立即注册</router-link></p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/user'
import { saveAuth } from '../utils/auth'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const errors = reactive({ username: '', password: '' })
const showPwd = ref(false)
const loading = ref(false)

async function handleLogin() {
  errors.username = ''
  errors.password = ''
  let valid = true
  if (!form.username) { errors.username = '请输入用户名'; valid = false }
  if (!form.password) { errors.password = '请输入密码'; valid = false }
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form.username, form.password)
    const loginData = res.data
    saveAuth(loginData.token, {
      id: loginData.userId,
      userId: loginData.userId,
      username: loginData.username,
      nickname: loginData.nickname,
      avatar: loginData.avatar
    })
    router.push('/')
  } catch (e) {
    errors.password = e.message
  } finally {
    loading.value = false
  }
}
</script>