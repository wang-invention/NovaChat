<template>
  <view class="profile-page">
    <view class="nav-bar" :style="{ marginTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <svg-icon class="back-icon" icon="<polyline points='15 18 9 12 15 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="24" />
      </view>
      <text class="nav-title">编辑个人信息</text>
      <view class="nav-right" @click="handleSave">
        <text class="save-btn" :class="{ disabled: saving }">保存</text>
      </view>
    </view>

    <scroll-view class="profile-content" scroll-y>
      <view class="avatar-section" @click="chooseAvatar">
        <text class="section-label">头像</text>
        <view class="avatar-wrap">
          <image class="avatar-img" :src="formData.avatar || defaultAvatar" mode="aspectFill" />
          <svg-icon class="arrow-icon" icon="<polyline points='9 18 15 12 9 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="20" />
        </view>
      </view>

      <view class="form-section">
        <view class="form-item">
          <text class="form-label">昵称</text>
          <input
            class="form-input"
            v-model="formData.nickname"
            placeholder="请输入昵称"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <text class="form-label">性别</text>
          <view class="gender-wrap" @click="showGenderPicker">
            <text class="gender-text">{{ genderText }}</text>
            <svg-icon class="arrow-icon" icon="<polyline points='9 18 15 12 9 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="20" />
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">手机号</text>
          <input
            class="form-input"
            v-model="formData.phone"
            placeholder="请输入手机号"
            type="number"
            maxlength="11"
          />
        </view>

        <view class="form-item">
          <text class="form-label">邮箱</text>
          <input
            class="form-input"
            v-model="formData.email"
            placeholder="请输入邮箱"
            type="text"
          />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { getCurrentUser, updateUserProfile, uploadAvatar } from '@/api/user';

const statusBarHeight = ref(0);
const saving = ref(false);
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default';

const formData = reactive({
  avatar: '',
  nickname: '',
  gender: 0,
  phone: '',
  email: '',
});

const genderText = computed(() => {
  switch (formData.gender) {
    case 1: return '男';
    case 2: return '女';
    default: return '未知';
  }
});

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  fetchUserInfo();
});

const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUser();
    if (res.code === 200 && res.data) {
      formData.avatar = res.data.avatar || '';
      formData.nickname = res.data.nickname || '';
      formData.gender = res.data.gender || 0;
      formData.phone = res.data.phone || '';
      formData.email = res.data.email || '';
    }
  } catch (err) {
    console.error('获取用户信息失败', err);
  }
};

const goBack = () => {
  uni.navigateBack();
};

const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0];
      uni.showLoading({ title: '上传中...' });

      try {
        const result = await uploadAvatar(tempFilePath);
        if (result.data) {
          formData.avatar = result.data.url || '';
          uni.showToast({ title: '头像上传成功', icon: 'success' });
        } else if (result.url) {
          formData.avatar = result.url;
          uni.showToast({ title: '头像上传成功', icon: 'success' });
        }
      } catch (err) {
        console.error('上传头像失败', err);
      } finally {
        uni.hideLoading();
      }
    },
  });
};

const showGenderPicker = () => {
  uni.showActionSheet({
    itemList: ['未知', '男', '女'],
    success: (res) => {
      formData.gender = res.tapIndex;
    },
  });
};

const handleSave = async () => {
  if (saving.value) return;

  if (!formData.nickname.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' });
    return;
  }

  saving.value = true;
  uni.showLoading({ title: '保存中...' });

  try {
    const payload = {
      nickname: formData.nickname.trim(),
      gender: formData.gender,
      phone: formData.phone.trim(),
      email: formData.email.trim(),
    };

    if (formData.avatar) {
      payload.avatar = formData.avatar;
    }

    const res = await updateUserProfile(payload);

    if (res.code === 200) {
      uni.setStorageSync('userInfo', {
        ...uni.getStorageSync('userInfo'),
        nickname: formData.nickname,
        avatar: formData.avatar,
      });

      uni.showToast({ title: '保存成功', icon: 'success' });

      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  } catch (err) {
    console.error('保存失败', err);
  } finally {
    saving.value = false;
    uni.hideLoading();
  }
};
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: #F7F8FA;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #eeeeee;
}

.nav-left,
.nav-right {
  width: 120rpx;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 34rpx;
  font-weight: 500;
  color: #333333;
}

.nav-right {
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  font-size: 32rpx;
  color: #07C160;
}

.save-btn.disabled {
  color: #999999;
}

.profile-content {
  height: calc(100vh - 88rpx);
}

.avatar-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  background-color: #ffffff;
  margin-top: 24rpx;
}

.section-label {
  font-size: 32rpx;
  color: #333333;
}

.avatar-wrap {
  display: flex;
  align-items: center;
}

.avatar-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
}

.arrow-icon {
  color: #cccccc;
}

.form-section {
  margin-top: 24rpx;
  background-color: #ffffff;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  width: 140rpx;
  font-size: 32rpx;
  color: #333333;
}

.form-input {
  flex: 1;
  font-size: 32rpx;
  color: #333333;
  text-align: right;
}

.form-input::placeholder {
  color: #999999;
}

.gender-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.gender-text {
  font-size: 32rpx;
  color: #333333;
  margin-right: 8rpx;
}
</style>