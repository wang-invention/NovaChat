<template>
  <view class="register-page">
    <view class="register-header" :style="{ marginTop: statusBarHeight + 'px' }">
      <view class="back-btn" @click="goBack" role="button" tabindex="0" @keydown.enter="goBack" aria-label="返回">
        <svg-icon class="back-icon" icon="<path d='M15 18L9 12L15 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
      </view>
      <text class="header-title">注册</text>
      <view class="header-placeholder"></view>
    </view>

    <view class="register-content">
      <view class="step-indicator">
        <view class="step-item" :class="{ active: currentStep === 1 }">
          <view class="step-circle">1</view>
          <text class="step-label">填写信息</text>
        </view>
        <view class="step-line" :class="{ active: currentStep > 1 }"></view>
        <view class="step-item" :class="{ active: currentStep === 2 }">
          <view class="step-circle">2</view>
          <text class="step-label">完成注册</text>
        </view>
      </view>

      <view v-if="currentStep === 1" class="step-panel">
        <view class="form-title">创建账号</view>
        <view class="form-subtitle">请填写以下信息完成注册</view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <svg-icon class="prefix-icon" icon="<path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <circle cx='12' cy='7' r='4' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
            </view>
            <input
              class="input-field"
              type="text"
              maxlength="32"
              placeholder="请输入用户名（字母开头，4-32位）"
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
              <svg-icon class="prefix-icon" icon="<rect x='3' y='11' width='18' height='11' rx='2' ry='2' stroke='currentColor' stroke-width='2'/>
                <path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2'/>" size="40" />
            </view>
            <input
              class="input-field"
              :password="!showPassword"
              maxlength="32"
              placeholder="请输入密码（8-32位）"
              placeholder-class="placeholder"
              v-model="form.password"
              @focus="onFocus('password')"
              @blur="onBlur('password')"
            />
            <view class="toggle-btn" @click="showPassword = !showPassword" role="button" tabindex="0" @keydown.enter="showPassword = !showPassword">
              <svg-icon class="toggle-icon" icon="<path d='M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <line x1='1' y1='1' x2='23' y2='23' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
              <svg-icon class="toggle-icon" icon="<path d='M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <circle cx='12' cy='12' r='3' stroke='currentColor' stroke-width='2'/>" />
            </view>
          </view>
          <text class="error-tip" v-if="errors.password">{{ errors.password }}</text>
        </view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <svg-icon class="prefix-icon" icon="<rect x='3' y='11' width='18' height='11' rx='2' ry='2' stroke='currentColor' stroke-width='2'/>
                <path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2'/>" size="40" />
            </view>
            <input
              class="input-field"
              :password="!showConfirmPwd"
              maxlength="32"
              placeholder="请再次输入密码"
              placeholder-class="placeholder"
              v-model="form.confirmPwd"
              @focus="onFocus('confirmPwd')"
              @blur="onBlur('confirmPwd')"
            />
            <view class="toggle-btn" @click="showConfirmPwd = !showConfirmPwd" role="button" tabindex="0" @keydown.enter="showConfirmPwd = !showConfirmPwd">
              <svg-icon class="toggle-icon" icon="<path d='M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <line x1='1' y1='1' x2='23' y2='23' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
              <svg-icon class="toggle-icon" icon="<path d='M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <circle cx='12' cy='12' r='3' stroke='currentColor' stroke-width='2'/>" />
            </view>
          </view>
          <text class="error-tip" v-if="errors.confirmPwd">{{ errors.confirmPwd }}</text>
        </view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <text class="prefix-text">+86</text>
              <view class="prefix-divider"></view>
            </view>
            <input
              class="input-field"
              type="number"
              maxlength="11"
              placeholder="请输入手机号（可选）"
              placeholder-class="placeholder"
              v-model="form.phone"
              @focus="onFocus('phone')"
              @blur="onBlur('phone')"
            />
          </view>
          <text class="error-tip" v-if="errors.phone">{{ errors.phone }}</text>
        </view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <svg-icon class="prefix-icon" icon="<path d='M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <polyline points='22,6 12,13 2,6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
            </view>
            <input
              class="input-field"
              type="text"
              maxlength="64"
              placeholder="请输入邮箱（可选）"
              placeholder-class="placeholder"
              v-model="form.email"
              @focus="onFocus('email')"
              @blur="onBlur('email')"
            />
          </view>
          <text class="error-tip" v-if="errors.email">{{ errors.email }}</text>
        </view>

        <view class="input-group">
          <view class="input-row">
            <view class="input-prefix">
              <svg-icon class="prefix-icon" icon="<path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
                <circle cx='12' cy='7' r='4' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" />
            </view>
            <input
              class="input-field"
              type="text"
              maxlength="64"
              placeholder="请输入昵称（可选）"
              placeholder-class="placeholder"
              v-model="form.nickname"
              @focus="onFocus('nickname')"
              @blur="onBlur('nickname')"
            />
          </view>
          <text class="error-tip" v-if="errors.nickname">{{ errors.nickname }}</text>
        </view>

        <view class="protocol-row" @click="agreeProtocol = !agreeProtocol" role="checkbox" :aria-checked="agreeProtocol" tabindex="0" @keydown.enter="agreeProtocol = !agreeProtocol">
          <view class="protocol-checkbox" :class="{ checked: agreeProtocol }">
            <svg-icon class="check-icon" icon="<polyline points='20 6 9 17 4 12' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'/>" />
          </view>
          <text class="protocol-text">
            我已阅读并同意
            <text class="protocol-link" @click.stop="openProtocol('user')">《用户协议》</text>
            和
            <text class="protocol-link" @click.stop="openProtocol('privacy')">《隐私政策》</text>
          </text>
        </view>

        <view class="btn-container">
          <view
            class="submit-btn"
            :class="{ disabled: !canSubmit, loading: loading }"
            @click="handleSubmit"
            role="button"
            tabindex="0"
            @keydown.enter="handleSubmit"
          >
            <text class="btn-text">完成注册</text>
          </view>
        </view>
      </view>

      <view v-if="currentStep === 2" class="step-panel">
        <view class="success-container">
          <view class="success-icon-wrap">
            <svg-icon class="success-icon" icon="<polyline points='20 6 9 17 4 12' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'/>" />
          </view>
          <text class="success-title">注册成功</text>
          <text class="success-subtitle">欢迎加入 ChatNova</text>
        </view>

        <view class="info-card">
          <view class="info-row">
            <text class="info-label">用户名</text>
            <text class="info-value">{{ form.username }}</text>
          </view>
        </view>

        <view class="btn-container">
          <view class="submit-btn success-btn" @click="goLogin" role="button" tabindex="0" @keydown.enter="goLogin">
            <text class="btn-text">立即登录</text>
          </view>
        </view>

        <view class="skip-link" @click="goHome" role="button" tabindex="0" @keydown.enter="goHome">
          <text>先看看再说</text>
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
import { onLoad } from "@dcloudio/uni-app";
import { register } from "@/api/user";

const statusBarHeight = ref(0);
const currentStep = ref(1);

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;
});
const loading = ref(false);
const showPassword = ref(false);
const showConfirmPwd = ref(false);
const agreeProtocol = ref(false);
const showProtocol = ref(false);
const protocolTitle = ref("");
const protocolContent = ref("");

const form = reactive({
  username: "",
  password: "",
  confirmPwd: "",
  phone: "",
  email: "",
  nickname: "",
});

const errors = reactive({
  username: "",
  password: "",
  confirmPwd: "",
  phone: "",
  email: "",
  nickname: "",
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

const validateConfirmPwd = (confirmPwd) => {
  if (!confirmPwd) {
    return "请再次输入密码";
  }
  if (confirmPwd !== form.password) {
    return "两次密码输入不一致";
  }
  return "";
};

const validatePhone = (phone) => {
  if (!phone) return "";
  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!phoneRegex.test(phone)) {
    return "手机号格式不正确";
  }
  return "";
};

const validateEmail = (email) => {
  if (!email) return "";
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return "邮箱格式不正确";
  }
  return "";
};

const validateNickname = (nickname) => {
  if (!nickname) return "";
  if (nickname.length > 64) {
    return "昵称最长 64 位";
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
    case "confirmPwd":
      if (form.confirmPwd) {
        errors.confirmPwd = validateConfirmPwd(form.confirmPwd);
      } else {
        errors.confirmPwd = "";
      }
      break;
    case "phone":
      errors.phone = validatePhone(form.phone);
      break;
    case "email":
      errors.email = validateEmail(form.email);
      break;
    case "nickname":
      errors.nickname = validateNickname(form.nickname);
      break;
  }
};

const canSubmit = computed(() => {
  return (
    form.username.length >= 4 &&
    form.password.length >= 8 &&
    form.confirmPwd.length >= 8 &&
    agreeProtocol.value &&
    !errors.username &&
    !errors.password &&
    !errors.confirmPwd &&
    !errors.phone &&
    !errors.email &&
    !errors.nickname
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
    errors.confirmPwd = "";
  }
);

watch(
  () => form.confirmPwd,
  (val) => {
    if (val.length >= 8) {
      errors.confirmPwd = validateConfirmPwd(val);
    } else {
      errors.confirmPwd = "";
    }
  }
);

watch(
  () => form.phone,
  (val) => {
    errors.phone = validatePhone(val);
  }
);

watch(
  () => form.email,
  (val) => {
    errors.email = validateEmail(val);
  }
);

const handleSubmit = async () => {
  if (!canSubmit.value || loading.value) return;

  errors.username = validateUsername(form.username);
  errors.password = validatePassword(form.password);
  errors.confirmPwd = validateConfirmPwd(form.confirmPwd);
  errors.phone = validatePhone(form.phone);
  errors.email = validateEmail(form.email);

  if (errors.username || errors.password || errors.confirmPwd || errors.phone || errors.email) {
    return;
  }

  loading.value = true;

  try {
    uni.showLoading({ title: "注册中...", mask: true });

    const payload = {
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username,
      phone: form.phone || undefined,
      email: form.email || undefined,
    };

    await register(payload);

    uni.hideLoading();
    currentStep.value = 2;
  } catch (err) {
    uni.hideLoading();
    uni.showToast({
      title: err.message || "注册失败，请重试",
      icon: "none",
      duration: 2000,
    });
  } finally {
    loading.value = false;
  }
};

const openProtocol = (type) => {
  if (type === "user") {
    protocolTitle.value = "用户协议";
    protocolContent.value = `用户协议 content...
1. 服务条款
2. 用户权利与义务
3. 隐私保护
4. 知识产权
5. 免责声明`;
  } else {
    protocolTitle.value = "隐私政策";
    protocolContent.value = `隐私政策 content...
1. 信息收集
2. 信息使用
3. 信息共享
4. 信息安全
5. 用户权利`;
  }
  showProtocol.value = true;
};

const closeProtocol = () => {
  showProtocol.value = false;
};

const goBack = () => {
  uni.navigateBack();
};

const goLogin = () => {
  uni.redirectTo({
    url: "/pages/login/index",
  });
};

const goHome = () => {
  uni.reLaunch({ url: "/pages/home/index" });
};

defineExpose({
  currentStep,
});
</script>

<style lang="scss" scoped>
page {
  background-color: #efeff4;
}

.register-page {
  min-height: 100vh;
  background-color: #efeff4;
  display: flex;
  flex-direction: column;
}

.register-header {
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

.register-content {
  flex: 1;
  padding: 40rpx 40rpx 0;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0 60rpx;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.step-circle {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background-color: #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 500;
  color: #999999;
  transition: all 0.3s ease;
}

.step-item.active .step-circle {
  background-color: #07c160;
  color: #ffffff;
}

.step-label {
  font-size: 24rpx;
  color: #999999;
  transition: color 0.3s ease;
}

.step-item.active .step-label {
  color: #07c160;
}

.step-line {
  width: 80rpx;
  height: 2rpx;
  background-color: #e5e5e5;
  margin: 0 16rpx;
  margin-bottom: 36rpx;
  transition: background-color 0.3s ease;
}

.step-line.active {
  background-color: #07c160;
}

.step-panel {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
  margin-bottom: 40rpx;
}

.form-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12rpx;
  line-height: 1.5;
}

.form-subtitle {
  font-size: 24rpx;
  color: #999999;
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
  color: #1a1a1a;
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
  color: #1a1a1a;
  background: transparent;
  border: none;
  outline: none;
  padding: 0 16rpx;
}

.placeholder {
  color: #cccccc;
}

.code-btn {
  padding: 12rpx 24rpx;
  background-color: #07c160;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #ffffff;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.code-btn:active {
  opacity: 0.8;
}

.code-btn.disabled {
  background-color: #e5e5e5;
  color: #999999;
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

.protocol-row {
  display: flex;
  align-items: flex-start;
  margin-top: 16rpx;
  padding: 24rpx 0;
}

.protocol-checkbox {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  margin-top: 2rpx;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.protocol-checkbox.checked {
  background-color: #07c160;
  border-color: #07c160;
}

.check-icon {
  width: 20rpx;
  height: 20rpx;
  color: #ffffff;
}

.protocol-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.6;
  flex: 1;
}

.protocol-link {
  color: #07c160;
}

.protocol-link:active {
  opacity: 0.7;
}

.btn-container {
  margin-top: 40rpx;
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

.strength-row {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  padding: 0 8rpx;
}

.strength-bars {
  display: flex;
  gap: 8rpx;
  margin-right: 16rpx;
}

.strength-bar {
  width: 64rpx;
  height: 8rpx;
  background-color: #e5e5e5;
  border-radius: 4rpx;
  transition: all 0.3s ease;
}

.strength-bar.active:nth-child(1) {
  background-color: #fa5151;
}

.strength-bar.active:nth-child(2) {
  background-color: #ffbe00;
}

.strength-bar.active:nth-child(3) {
  background-color: #07c160;
}

.strength-text {
  font-size: 24rpx;
  color: #999999;
}

.strength-text.weak {
  color: #fa5151;
}

.strength-text.medium {
  color: #ffbe00;
}

.strength-text.strong {
  color: #07c160;
}

.back-link {
  margin-top: 32rpx;
  text-align: center;
}

.back-link text {
  font-size: 26rpx;
  color: #07c160;
}

.back-link:active {
  opacity: 0.7;
}

.success-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 60rpx;
}

.success-icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: #07c160;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.success-icon {
  width: 60rpx;
  height: 60rpx;
  color: #ffffff;
}

.success-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.success-subtitle {
  font-size: 28rpx;
  color: #666666;
}

.info-card {
  background-color: #f7f7f7;
  border-radius: 8rpx;
  padding: 24rpx 32rpx;
  margin-bottom: 40rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 28rpx;
  color: #666666;
}

.info-value {
  font-size: 28rpx;
  color: #1a1a1a;
}

.success-btn {
  background-color: #07c160;
}

.skip-link {
  margin-top: 32rpx;
  text-align: center;
}

.skip-link text {
  font-size: 26rpx;
  color: #666666;
}

.skip-link:active {
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
  color: #1a1a1a;
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
