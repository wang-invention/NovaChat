<template>
  <view class="login-page">
    <view class="login-header">
      <view class="back-btn" @click="goBack" role="button" tabindex="0" @keydown.enter="goBack" aria-label="返回">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
      <text class="header-title">登录</text>
      <view class="header-placeholder"></view>
    </view>

    <view class="login-content">
      <view class="login-panel">
        <view class="form-title">欢迎回来</view>
        <view class="form-subtitle">请登录您的账号</view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <svg class="prefix-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <input
              class="input-field"
              type="text"
              maxlength="32"
              placeholder="请输入用户名"
              placeholder-class="placeholder"
              v-model="form.username"
              @focus="onFocus('username')"
              @blur="onBlur('username')"
            />
          </view>
          <text class="error-tip" v-if="errors.username">{{ errors.username }}</text>
        </view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <svg class="prefix-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/>
              </svg>
            </view>
            <input
              class="input-field"
              :password="!showPassword"
              maxlength="20"
              placeholder="请输入密码"
              placeholder-class="placeholder"
              v-model="form.password"
              @focus="onFocus('password')"
              @blur="onBlur('password')"
            />
            <view class="toggle-btn" @click="showPassword = !showPassword" role="button" tabindex="0" @keydown.enter="showPassword = !showPassword">
              <svg v-if="showPassword" class="toggle-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else class="toggle-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
            </view>
          </view>
          <text class="error-tip" v-if="errors.password">{{ errors.password }}</text>
        </view>

        <view class="forget-row">
          <text class="forget-link" @click="handleForgetPwd" role="button" tabindex="0" @keydown.enter="handleForgetPwd">忘记密码？</text>
        </view>

        <view class="btn-container">
          <view
            class="submit-btn"
            :class="{ disabled: !canLogin, loading: loading }"
            @click="handleLogin"
            role="button"
            tabindex="0"
            @keydown.enter="handleLogin"
          >
            <text class="btn-text">登录</text>
          </view>
        </view>

        <view class="register-row">
          <text class="register-tip">没有账号？</text>
          <text class="register-link" @click="goRegister" role="button" tabindex="0" @keydown.enter="goRegister">立即注册</text>
        </view>
      </view>
    </view>

    <view class="protocol-modal" v-if="showProtocol" @click="closeProtocol" role="dialog" aria-modal="true">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ protocolTitle }}</text>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <text class="modal-text">{{ protocolContent }}</text>
        </scroll-view>
        <view class="modal-footer" @click="closeProtocol" role="button" tabindex="0" @keydown.enter="closeProtocol">
          <text>知道了</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from "vue";
import { loginByPassword } from "@/api/user";
import { getDeviceId, getDeviceType } from "@/utils/device";

const loading = ref(false);
const showPassword = ref(false);
const showProtocol = ref(false);
const protocolTitle = ref("");
const protocolContent = ref("");

const form = reactive({
  username: "",
  password: "",
});

const errors = reactive({
  username: "",
  password: "",
});

const validateUsername = (username) => {
  if (!username) {
    return "请输入用户名";
  }
  if (username.length < 4 || username.length > 32) {
    return "用户名长度需在 4~32 位";
  }
  const usernameRegex = /^[a-zA-Z][a-zA-Z0-9_]{3,31}$/;
  if (!usernameRegex.test(username)) {
    return "用户名需以字母开头，仅允许字母/数字/下划线";
  }
  return "";
};

const validatePassword = (password) => {
  if (!password) {
    return "请输入密码";
  }
  if (password.length < 8 || password.length > 32) {
    return "密码长度需在 8~32 位";
  }
  return "";
};

const onFocus = (field) => {};

const onBlur = (field) => {
  switch (field) {
    case "username":
      if (form.username) {
        errors.username = validateUsername(form.username);
      } else {
        errors.username = "";
      }
      break;
    case "password":
      if (form.password) {
        errors.password = validatePassword(form.password);
      } else {
        errors.password = "";
      }
      break;
  }
};

const canLogin = computed(() => {
  return (
    form.username.length >= 4 &&
    form.password.length >= 8 &&
    !errors.username &&
    !errors.password
  );
});

watch(
  () => form.username,
  (val) => {
    if (val.length >= 4) {
      errors.username = validateUsername(val);
    } else {
      errors.username = "";
    }
  }
);

watch(
  () => form.password,
  (val) => {
    if (val.length >= 8) {
      errors.password = validatePassword(val);
    } else {
      errors.password = "";
    }
  }
);

const handleLogin = async () => {
  if (!canLogin.value || loading.value) return;

  errors.username = validateUsername(form.username);
  errors.password = validatePassword(form.password);

  if (errors.username || errors.password) return;

  loading.value = true;

  try {
    uni.showLoading({ title: "登录中...", mask: true });

    // 获取设备信息，用于多端登录管理
    const deviceId = getDeviceId();
    const deviceType = getDeviceType();

    const res = await loginByPassword({
      username: form.username,
      password: form.password,
      deviceId: deviceId,
      deviceType: deviceType,
    });

    const { data } = res;
    const token = data.token;
    const userInfo = {
      id: data.userId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
    };

    // 保存登录态
    uni.setStorageSync("token", token);
    uni.setStorageSync("userInfo", userInfo);
    uni.setStorageSync("isLogin", true);

    uni.hideLoading();
    uni.showToast({ title: "登录成功", icon: "success" });

    setTimeout(() => {
      uni.switchTab({
        url: "/pages/home/index",
        fail: () => {
          uni.reLaunch({ url: "/pages/home/index" });
        },
      });
    }, 400);
  } catch (err) {
    uni.hideLoading();
    // 处理特定错误码
    if (err.code === 1001) {
      uni.showToast({ title: "用户名或密码错误", icon: "none" });
    } else if (err.code === 1006) {
      uni.showToast({ title: "登录已失效，请重新登录", icon: "none" });
    } else {
      uni.showToast({ title: err.message || "登录失败，请重试", icon: "none" });
    }
  } finally {
    loading.value = false;
  }
};

const handleForgetPwd = () => {
  uni.showToast({ title: "忘记密码功能开发中", icon: "none" });
};

const goBack = () => {
  uni.navigateBack();
};

const goRegister = () => {
  uni.navigateTo({ url: "/pages/register/index" });
};

const closeProtocol = () => {
  showProtocol.value = false;
};

const openProtocol = (type) => {
  if (type === "user") {
    protocolTitle.value = "用户协议";
    protocolContent.value = `用户协议内容...
1. 服务条款
2. 用户权利与义务
3. 隐私保护
4. 知识产权
5. 免责声明`;
  } else {
    protocolTitle.value = "隐私政策";
    protocolContent.value = `隐私政策内容...
1. 信息收集
2. 信息使用
3. 信息共享
4. 信息安全
5. 用户权利`;
  }
  showProtocol.value = true;
};
</script>

<style lang="scss" scoped>
page {
  background-color: #efeff4;
}

.login-page {
  min-height: 100vh;
  background-color: #efeff4;
  display: flex;
  flex-direction: column;
}

.login-header {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #e5e5e5;
}

.back-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 40rpx;
  height: 40rpx;
  color: #1a1a1a;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.header-placeholder {
  width: 48rpx;
}

.login-content {
  flex: 1;
  padding: 40rpx 40rpx 0;
}

.login-panel {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 40rpx 32rpx;
}

.form-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111111;
  margin-bottom: 12rpx;
  line-height: 1.5;
}

.form-subtitle {
  font-size: 24rpx;
  color: #666666;
  margin-bottom: 40rpx;
  line-height: 1.5;
}

.input-group {
  margin-bottom: 24rpx;
}

.input-row {
  display: flex;
  align-items: center;
  height: 96rpx;
  background-color: #f7f7f7;
  border-radius: 8rpx;
  padding: 0 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
}

.input-row:focus-within {
  border-color: #07c160;
  background-color: #ffffff;
}

.input-prefix {
  display: flex;
  align-items: center;
  padding-right: 16rpx;
}

.prefix-text {
  font-size: 28rpx;
  color: #111111;
  font-weight: 500;
}

.prefix-divider {
  width: 2rpx;
  height: 32rpx;
  background-color: #e5e5e5;
  margin-left: 16rpx;
}

.prefix-icon {
  width: 36rpx;
  height: 36rpx;
  color: #999999;
}

.input-field {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #111111;
  background: transparent;
  border: none;
  outline: none;
  padding: 0 16rpx;
}

.placeholder {
  color: #cccccc;
}

.toggle-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.toggle-icon {
  width: 36rpx;
  height: 36rpx;
  color: #999999;
}

.error-tip {
  font-size: 24rpx;
  color: #fa5151;
  margin-top: 12rpx;
  padding-left: 8rpx;
}

.forget-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 8rpx;
  margin-bottom: 24rpx;
}

.forget-link {
  font-size: 24rpx;
  color: #666666;
}

.forget-link:active {
  opacity: 0.7;
}

.btn-container {
  margin-top: 16rpx;
}

.submit-btn {
  height: 96rpx;
  background-color: #07c160;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.submit-btn:active:not(.disabled) {
  background-color: #06ad56;
}

.submit-btn.disabled {
  background-color: #e5e5e5;
}

.submit-btn.loading {
  background-color: #07c160;
  opacity: 0.8;
}

.btn-text {
  font-size: 32rpx;
  font-weight: 500;
  color: #ffffff;
}

.submit-btn.disabled .btn-text {
  color: #999999;
}

.register-row {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40rpx;
  padding: 24rpx 0;
}

.register-tip {
  font-size: 24rpx;
  color: #666666;
}

.register-link {
  font-size: 24rpx;
  color: #07c160;
  margin-left: 8rpx;
}

.register-link:active {
  opacity: 0.7;
}

.protocol-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 600rpx;
  max-height: 80vh;
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 32rpx;
  border-bottom: 1rpx solid #e5e5e5;
  text-align: center;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111111;
}

.modal-body {
  padding: 32rpx;
  flex: 1;
  max-height: 500rpx;
}

.modal-text {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.8;
}

.modal-footer {
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #e5e5e5;
  text-align: center;
}

.modal-footer text {
  font-size: 32rpx;
  color: #07c160;
}

.modal-footer:active {
  background-color: #f7f7f7;
}
</style>
