import { get, post, put, upload } from './index'

export function login(username, password) {
  return post('/users/login', { username, password })
}

export function register(data) {
  return post('/users/register', data)
}

export function logout(deviceId) {
  return post('/users/logout', deviceId ? { deviceId } : {})
}

export function logoutAll() {
  return post('/users/logout-all', {})
}

export function getDevices() {
  return get('/users/devices')
}

export function kickDevice(deviceId) {
  return post('/users/kick?deviceId=' + deviceId, {})
}

export function getMyProfile() {
  return get('/users/me')
}

export function getUserById(userId) {
  return get('/users/' + userId)
}

export function updateProfile(data) {
  return post('/users/profile', data)
}

export function uploadAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  return upload('/users/avatar', fd)
}

export function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  return upload('/users/image', fd)
}

export function searchUser(keyword) {
  return get('/users/search?keyword=' + encodeURIComponent(keyword))
}