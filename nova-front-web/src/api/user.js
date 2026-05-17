import { post, put, upload } from './index'

export function login(username, password) {
  return post('/users/login', { username, password })
}

export function register(data) {
  return post('/users/register', data)
}

export function updateProfile(data) {
  return put('/users/profile', data)
}

export function uploadAvatar(file) {
  const fd = new FormData()
  fd.append('file', file)
  return upload('/users/upload/avatar', fd)
}

export function searchUser(keyword) {
  return get('/search?keyword=' + encodeURIComponent(keyword))
}