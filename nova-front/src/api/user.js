/**
 * 用户相关接口
 */
import { http } from "@/utils/request";

export function loginByPassword(payload) {
  return http.post("/user/users/login", payload, { auth: false, showError: false });
}

export function register(payload) {
  return http.post("/user/users/register", payload, { auth: false, showError: false });
}

export function getProfile() {
  return http.get("/user/profile");
}
