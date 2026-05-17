import { getToken, clearAuth } from '../utils/auth'

const BASE = import.meta.env.VITE_API_BASE + '/api/user'

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers['Authorization'] = 'Bearer ' + token

  const res = await fetch(BASE + path, { ...options, headers: { ...headers, ...options.headers } })
  const data = await res.json()

  if (res.status === 401) {
    clearAuth()
    window.location.hash = '#/login'
    throw new Error('登录已过期')
  }
  if (!res.ok) throw new Error(data.message || data.msg || '请求失败')
  if (data.code !== undefined && data.code !== 200) {
    throw new Error(data.message || data.msg || '请求失败')
  }
  return data
}

export function get(path) { return request(path) }
export function post(path, body) { return request(path, { method: 'POST', body: JSON.stringify(body) }) }
export function put(path, body) { return request(path, { method: 'PUT', body: JSON.stringify(body) }) }
export function del(path) { return request(path, { method: 'DELETE' }) }

export function upload(path, formData) {
  const headers = {}
  const token = getToken()
  if (token) headers['Authorization'] = 'Bearer ' + token
  return fetch(BASE + path, { method: 'POST', headers, body: formData }).then(r => r.json())
}