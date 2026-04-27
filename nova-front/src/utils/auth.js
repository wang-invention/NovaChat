/**
 * 用户认证工具函数
 * 用于管理登录状态、token、用户信息等
 */

const TOKEN_KEY = 'token';
const USER_INFO_KEY = 'userInfo';
const LOGIN_STATUS_KEY = 'isLogin';

/**
 * 获取 token
 * @returns {string} token
 */
export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY) || '';
  } catch (e) {
    return '';
  }
}

/**
 * 设置 token
 * @param {string} token
 */
export function setToken(token) {
  try {
    uni.setStorageSync(TOKEN_KEY, token);
  } catch (e) {
    console.error('设置 token 失败', e);
  }
}

/**
 * 清除 token
 */
export function removeToken() {
  try {
    uni.removeStorageSync(TOKEN_KEY);
  } catch (e) {
    console.error('清除 token 失败', e);
  }
}

/**
 * 获取用户信息
 * @returns {Object|null}
 */
export function getUserInfo() {
  try {
    const userInfo = uni.getStorageSync(USER_INFO_KEY);
    return userInfo || null;
  } catch (e) {
    return null;
  }
}

/**
 * 设置用户信息
 * @param {Object} userInfo
 */
export function setUserInfo(userInfo) {
  try {
    uni.setStorageSync(USER_INFO_KEY, userInfo);
  } catch (e) {
    console.error('设置用户信息失败', e);
  }
}

/**
 * 清除用户信息
 */
export function removeUserInfo() {
  try {
    uni.removeStorageSync(USER_INFO_KEY);
  } catch (e) {
    console.error('清除用户信息失败', e);
  }
}

/**
 * 检查是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  try {
    const token = uni.getStorageSync(TOKEN_KEY);
    const isLogin = uni.getStorageSync(LOGIN_STATUS_KEY);
    return !!token && !!isLogin;
  } catch (e) {
    return false;
  }
}

/**
 * 设置登录状态
 * @param {boolean} status
 */
export function setLoginStatus(status) {
  try {
    uni.setStorageSync(LOGIN_STATUS_KEY, status);
  } catch (e) {
    console.error('设置登录状态失败', e);
  }
}

/**
 * 清除登录状态
 */
export function clearLoginStatus() {
  try {
    uni.removeStorageSync(LOGIN_STATUS_KEY);
  } catch (e) {
    console.error('清除登录状态失败', e);
  }
}

/**
 * 清除所有登录相关信息
 * 用于登出
 */
export function clearAuth() {
  removeToken();
  removeUserInfo();
  clearLoginStatus();
}

/**
 * 保存登录信息（登录成功后调用）
 * @param {string} token
 * @param {Object} userInfo
 */
export function saveLoginInfo(token, userInfo) {
  setToken(token);
  setUserInfo(userInfo);
  setLoginStatus(true);
}

/**
 * 需要登录才能访问
 * 如果未登录，跳转到登录页
 * @returns {boolean} 是否已登录
 */
export function requireAuth() {
  if (!isLoggedIn()) {
    uni.navigateTo({
      url: '/pages/login/index'
    });
    return false;
  }
  return true;
}

/**
 * 跳转到登录页
 * @param {string} redirect - 登录成功后重定向的页面（可选）
 */
export function goToLogin(redirect = '') {
  const url = redirect
    ? `/pages/login/index?redirect=${encodeURIComponent(redirect)}`
    : '/pages/login/index';
  uni.navigateTo({ url });
}
