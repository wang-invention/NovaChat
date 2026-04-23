/**
 * ChatNova 统一请求封装
 * - 基于 uni.request
 * - 自动携带 token
 * - 统一错误处理与 Toast 提示
 */

// #ifdef H5
const BASE_URL = "/api";
// #endif

// #ifndef H5
const BASE_URL = "http://127.0.0.1:8080/api";
// #endif

const TIMEOUT = 15000;

function getToken() {
  try {
    return uni.getStorageSync("token") || "";
  } catch (e) {
    return "";
  }
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
        const ok = body.code === 0 || body.success === true || res.statusCode === 200;
        if (ok) {
          resolve(body);
        } else {
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
