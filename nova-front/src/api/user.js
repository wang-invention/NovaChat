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

/**
 * 更新用户基本信息
 * @param {Object} payload - 更新参数
 * @param {string} payload.nickname - 昵称
 * @param {string} payload.gender - 性别 0-未知 1-男 2-女
 * @param {string} payload.email - 邮箱
 * @param {string} payload.phone - 手机号
 */
export function updateUserProfile(payload) {
  return http.post("/user/users/profile", payload);
}

/**
 * 上传用户头像
 * @param {string} filePath - 头像文件路径
 */
export function uploadAvatar(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync("token") || "";
    uni.uploadFile({
      url: "http://129.211.0.210:8080/api/user/users/avatar",
      filePath: filePath,
      name: "file",
      header: {
        Authorization: `Bearer ${token}`,
      },
      success: (res) => {
        const data = JSON.parse(res.data || "{}");
        if (data.code === 200 || data.code === 0) {
          resolve(data);
        } else {
          uni.showToast({
            title: data.message || "上传失败",
            icon: "none",
          });
          reject(data);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: "上传失败，请稍后重试",
          icon: "none",
        });
        reject(err);
      },
    });
  });
}

/**
 * 上传聊天图片
 * @param {string} filePath - 图片文件路径
 */
export function uploadImage(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync("token") || "";
    uni.uploadFile({
      url: "http://129.211.0.210:8080/api/user/users/image",
      filePath: filePath,
      name: "file",
      header: {
        Authorization: `Bearer ${token}`,
      },
      success: (res) => {
        const data = JSON.parse(res.data || "{}");
        if (data.code === 200 || data.code === 0) {
          resolve(data);
        } else {
          uni.showToast({
            title: data.message || "上传失败",
            icon: "none",
          });
          reject(data);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: "上传失败，请稍后重试",
          icon: "none",
        });
        reject(err);
      },
    });
  });
}
