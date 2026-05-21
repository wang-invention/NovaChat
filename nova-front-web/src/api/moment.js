import { getToken } from '../utils/auth'

const BASE = import.meta.env.VITE_API_BASE + '/api/moment'

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers['Authorization'] = 'Bearer ' + token

  const res = await fetch(BASE + path, { ...options, headers: { ...headers, ...options.headers } })
  if (res.status === 401) {
    const { clearAuth } = await import('../utils/auth')
    clearAuth()
    window.location.hash = '#/login'
    throw new Error('登录已过期')
  }
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || data.msg || '请求失败')
  return data
}

export function getTimeline(page = 1, size = 10) {
  return request('/moment/timeline?page=' + page + '&size=' + size)
}

export function publishMoment(content, images = []) {
  return request('/moment', { method: 'POST', body: JSON.stringify({ content, images }) })
}

export function deleteMoment(momentId) {
  return request('/moment/' + momentId, { method: 'DELETE' })
}

export function likeMoment(momentId) {
  return request('/moment/' + momentId + '/like', { method: 'POST' })
}

export function unlikeMoment(momentId) {
  return request('/moment/' + momentId + '/like', { method: 'DELETE' })
}

export function commentMoment(momentId, content, replyToUserId) {
  return request('/moment/' + momentId + '/comment', {
    method: 'POST',
    body: JSON.stringify({ content, replyToUserId: replyToUserId || null })
  })
}

export function deleteComment(commentId) {
  return request('/moment/comment/' + commentId, { method: 'DELETE' })
}