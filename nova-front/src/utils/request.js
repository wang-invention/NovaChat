/**
 * ChatNova 统一请求封装
 * - 基于 uni.request
 * - 自动携带 token
 * - 统一错误处理与 Toast 提示
 * - 401 自动跳转登录页
 */

// #ifdef H5
const BASE_URL = "http://129.211.0.210:8080/api";
// #endif

// #ifndef H5
const BASE_URL = "http://129.211.0.210:8080/api";
// #endif

const TIMEOUT = 15000;

function getToken() {
  try {
    return uni.getStorageSync("token") || "";
  } catch (e) {
    return "";
  }
}

/**
 * 处理 401 未授权错误
 * 清除登录态并跳转到登录页
 */
function handleUnauthorized() {
  // 清除本地登录态
  uni.removeStorageSync("token");
  uni.removeStorageSync("userInfo");
  uni.removeStorageSync("isLogin");

  // 显示提示
  uni.showToast({
    title: "登录已过期，请重新登录",
    icon: "none",
    duration: 2000,
  });

  // 延迟跳转到登录页
  setTimeout(() => {
    uni.navigateTo({
      url: "/pages/login/index",
    });
  }, 1500);
}

export function request(options = {}) {
  const {
    url,
    method = "GET",
    data = {},
    header = {},
    auth = true,
    showError = true,
  } = options;

  return new Promise((resolve, reject) => {
    const finalHeader = {
      "Content-Type": "application/json",
      ...header,
    };

    // 添加设备ID头（用于多端登录管理）
    if (!finalHeader["X-Device-Id"]) {
      try {
        const deviceId = uni.getStorageSync("device_id");
        if (deviceId) {
          finalHeader["X-Device-Id"] = deviceId;
        }
      } catch (e) {
        // 忽略错误
      }
    }

    if (auth) {
      const token = getToken();
      if (token) finalHeader.Authorization = `Bearer ${token}`;
    }

    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: finalHeader,
      timeout: TIMEOUT,
      success: (res) => {
        const body = res.data || {};
        const ok = body.code === 0 || body.code === 200 || body.success === true || res.statusCode === 200;

        if (ok) {
          resolve(body);
        } else {
          // 处理 401 未授权
          if (res.statusCode === 401 || body.code === 401 || body.code === 1006) {
            handleUnauthorized();
          }

          if (showError) {
            uni.showToast({
              title: body.message || "请求失败",
              icon: "none",
            });
          }
          reject(body);
        }
      },
      fail: (err) => {
        if (showError) {
          uni.showToast({
            title: "网络异常，请稍后重试",
            icon: "none",
          });
        }
        reject(err);
      },
    });
  });
}

export const http = {
  get: (url, data, opts) => request({ url, method: "GET", data, ...opts }),
  post: (url, data, opts) => request({ url, method: "POST", data, ...opts }),
  put: (url, data, opts) => request({ url, method: "PUT", data, ...opts }),
  delete: (url, data, opts) => request({ url, method: "DELETE", data, ...opts }),
};
