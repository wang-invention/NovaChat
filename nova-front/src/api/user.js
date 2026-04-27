/**
 * 用户相关接口
 */
import { http } from "@/utils/request";

/**
 * 用户登录
 * @param {Object} payload - 登录参数
 * @param {string} payload.username - 用户名
 * @param {string} payload.password - 密码
 * @param {string} payload.deviceId - 设备ID（可选，不传则后端生成）
 * @param {string} payload.deviceType - 设备类型（可选）
 */
export function loginByPassword(payload) {
  return http.post("/user/users/login", payload, { auth: false, showError: false });
}

/**
 * 用户注册
 * @param {Object} payload - 注册参数
 * @param {string} payload.username - 用户名
 * @param {string} payload.password - 密码
 * @param {string} payload.nickname - 昵称（可选）
 */
export function register(payload) {
  return http.post("/user/users/register", payload, { auth: false, showError: false });
}

/**
 * 获取当前登录用户信息
 * 需要登录态，会自动携带 token
 */
export function getCurrentUser() {
  return http.get("/user/users/me");
}

/**
 * 登出当前设备
 * 需要登录态
 */
export function logout() {
  return http.post("/user/users/logout");
}

/**
 * 登出所有设备
 * 需要登录态
 */
export function logoutAll() {
  return http.post("/user/users/logout-all");
}

/**
 * 获取用户登录设备列表
 * 需要登录态
 */
export function getLoginDevices() {
  return http.get("/user/users/devices");
}
