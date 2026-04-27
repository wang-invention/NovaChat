<template>
  <view class="mine-page">
    <!-- 顶部导航栏 - 固定 -->
    <view class="nav-bar">
      <text class="nav-title">我的</text>
      <view class="nav-right">
        <view class="icon-btn" @click="handleSearch">
          <svg class="icon-svg-small" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
        <view class="icon-btn" @click="handleMore">
          <svg class="icon-svg-small" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="1" fill="currentColor"/>
            <circle cx="19" cy="12" r="1" fill="currentColor"/>
            <circle cx="5" cy="12" r="1" fill="currentColor"/>
          </svg>
        </view>
      </view>
    </view>

    <!-- 我的页面内容 - 可滚动区域 -->
    <scroll-view class="mine-content" scroll-y refresher-enabled :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh">
      <!-- 用户信息卡片 -->
      <view class="user-card" @click="goToProfile">
        <view class="user-main">
          <image class="user-avatar" :src="userInfo.avatar || defaultAvatar" mode="aspectFill" @error="onAvatarError"/>
          <view class="user-info">
            <view class="user-name-row">
              <text class="user-name">{{ userInfo.nickname || userInfo.username || '未登录' }}</text>
            </view>
            <view class="user-tags" v-if="isLogin">
              <view class="user-tag vip">VIP</view>
              <view class="user-tag level">Lv.8</view>
            </view>
            <text class="user-id" v-if="isLogin">ID: {{ userInfo.id || '--' }}</text>
            <text class="user-id" v-else>点击登录</text>
          </view>
        </view>
        <view class="user-qrcode" v-if="isLogin">
          <svg class="qrcode-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="3" width="7" height="7" stroke="currentColor" stroke-width="2"/>
            <rect x="14" y="3" width="7" height="7" stroke="currentColor" stroke-width="2"/>
            <rect x="14" y="14" width="7" height="7" stroke="currentColor" stroke-width="2"/>
            <rect x="3" y="14" width="7" height="7" stroke="currentColor" stroke-width="2"/>
          </svg>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 支付 -->
      <view class="mine-section">
        <view class="mine-item">
          <view class="item-icon" style="background-color: #07C160;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="2" y="5" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
              <line x1="2" y1="10" x2="22" y2="10" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <text class="item-name">支付</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 收藏、朋友圈、卡包、表情 -->
      <view class="mine-section">
        <view class="mine-item">
          <view class="item-icon" style="background-color: #10AEFF;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">收藏</text>
          <text class="item-extra">128</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
        <view class="mine-item">
          <view class="item-icon" style="background-color: #FA9D3B;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="9" y1="9" x2="9.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="15" y1="9" x2="15.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">朋友圈</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
        <view class="mine-item">
          <view class="item-icon" style="background-color: #FF6B6B;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
              <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
              <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
              <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <text class="item-name">卡包</text>
          <text class="item-extra">5张优惠券</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
        <view class="mine-item">
          <view class="item-icon" style="background-color: #9C27B0;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="9" y1="9" x2="9.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="15" y1="9" x2="15.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">表情</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 设置 -->
      <view class="mine-section">
        <view class="mine-item" @click="goToSettings">
          <view class="item-icon" style="background-color: #607D8B;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">设置</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 更多功能 -->
      <view class="section-gap"></view>
      <view class="mine-section">
        <view class="mine-item" v-for="(item, index) in moreItems" :key="index" @click="handleMoreItem(item)">
          <view class="item-icon" :style="{ backgroundColor: item.color }">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" v-html="item.icon"></svg>
          </view>
          <text class="item-name">{{ item.name }}</text>
          <text class="item-extra" v-if="item.extra">{{ item.extra }}</text>
          <view class="item-arrow">
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="9 18 15 12 9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 底部退出按钮 -->
      <view class="section-gap" v-if="isLogin"></view>
      <view class="logout-section" v-if="isLogin">
        <view class="logout-btn" @click="handleLogout">
          <text class="logout-text">退出登录</text>
        </view>
      </view>

      <!-- 底部留白，防止被TabBar遮挡 -->
      <view class="bottom-padding"></view>
    </scroll-view>

    <!-- 底部 TabBar - 固定 -->
    <custom-tab-bar current="me" />
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import CustomTabBar from '@/components/custom-tab-bar/custom-tab-bar.vue';
import { getCurrentUser, logout } from '@/api/user';

// 响应式数据
const isLogin = ref(false);
const isRefreshing = ref(false);
const userInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  gender: 0,
  status: 1,
  lastLoginTime: null,
  createTime: null
});

// 默认头像
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default';

// 更多功能列表
const moreItems = [
  {
    name: '账号与安全',
    color: '#1ABC9C',
    icon: '<rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/>'
  },
  {
    name: '隐私',
    color: '#E74C3C',
    icon: '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>'
  },
  {
    name: '通用',
    color: '#3498DB',
    icon: '<circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    name: '帮助与反馈',
    color: '#F39C12',
    icon: '<circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    name: '关于',
    color: '#795548',
    extra: 'v1.0.0',
    icon: '<circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><line x1="12" y1="16" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="8" x2="12.01" y2="8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    name: '通知',
    color: '#9B59B6',
    icon: '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  }
];

// 检查登录状态
const checkLoginStatus = () => {
  const token = uni.getStorageSync('token');
  const storedUserInfo = uni.getStorageSync('userInfo');
  isLogin.value = !!token;

  if (storedUserInfo) {
    Object.assign(userInfo, storedUserInfo);
  }
};

// 获取用户信息
const fetchUserInfo = async () => {
  if (!isLogin.value) return;

  try {
    const res = await getCurrentUser();
    if (res.code === 200 && res.data) {
      Object.assign(userInfo, res.data);
      // 更新本地存储
      uni.setStorageSync('userInfo', {
        id: res.data.id,
        username: res.data.username,
        nickname: res.data.nickname,
        avatar: res.data.avatar
      });
    }
  } catch (err) {
    console.error('获取用户信息失败', err);
    // 如果是401错误，说明token已失效
    if (err.code === 401 || err.code === 1006) {
      handleTokenExpired();
    }
  }
};

// 处理token过期
const handleTokenExpired = () => {
  uni.removeStorageSync('token');
  uni.removeStorageSync('userInfo');
  uni.removeStorageSync('isLogin');
  isLogin.value = false;
  uni.showToast({
    title: '登录已过期，请重新登录',
    icon: 'none'
  });
};

// 下拉刷新
const onRefresh = async () => {
  isRefreshing.value = true;
  await fetchUserInfo();
  isRefreshing.value = false;
};

// 头像加载失败
const onAvatarError = () => {
  userInfo.avatar = defaultAvatar;
};

// 跳转到个人资料
const goToProfile = () => {
  if (!isLogin.value) {
    uni.navigateTo({ url: '/pages/login/index' });
    return;
  }
  uni.navigateTo({ url: '/pages/profile/index' });
};

// 跳转到设置
const goToSettings = () => {
  uni.navigateTo({ url: '/pages/settings/index' });
};

// 处理更多项点击
const handleMoreItem = (item) => {
  uni.showToast({
    title: `${item.name}功能开发中`,
    icon: 'none'
  });
};

// 搜索
const handleSearch = () => {
  uni.showToast({
    title: '搜索功能开发中',
    icon: 'none'
  });
};

// 更多
const handleMore = () => {
  uni.showToast({
    title: '更多功能开发中',
    icon: 'none'
  });
};

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用登出接口
          await logout();
        } catch (err) {
          console.error('登出接口调用失败', err);
        } finally {
          // 清除本地登录态
          uni.removeStorageSync('token');
          uni.removeStorageSync('userInfo');
          uni.removeStorageSync('isLogin');
          isLogin.value = false;

          // 重置用户信息
          Object.assign(userInfo, {
            id: null,
            username: '',
            nickname: '',
            avatar: '',
            phone: '',
            email: '',
            gender: 0,
            status: 1,
            lastLoginTime: null,
            createTime: null
          });

          uni.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    }
  });
};

// 页面显示时检查登录状态并获取用户信息
// 注意：UniApp 中 onShow 在页面每次显示时都会触发（包括首次加载）
// 所以不需要同时使用 onMounted
onShow(() => {
  checkLoginStatus();
  fetchUserInfo();
});
</script>

<style lang="scss" scoped>
page {
  background-color: #f5f5f5;
}

.mine-page {
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 - 固定 */
.nav-bar {
  flex-shrink: 0;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #e5e5e5;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #111111;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.icon-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg-small {
  width: 40rpx;
  height: 40rpx;
  color: #111111;
}

/* 我的页面内容 - 可滚动区域 */
.mine-content {
  flex: 1;
  background-color: #f5f5f5;
  overflow-y: auto;
}

/* 用户信息卡片 */
.user-card {
  background-color: #ffffff;
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-main {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  margin-right: 24rpx;
  background-color: #f0f0f0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #111111;
}

.user-tags {
  display: flex;
  gap: 12rpx;
}

.user-tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.user-tag.vip {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #ffffff;
}

.user-tag.level {
  background-color: #07C160;
  color: #ffffff;
}

.user-id {
  font-size: 26rpx;
  color: #999999;
}

.user-qrcode {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-icon {
  width: 48rpx;
  height: 48rpx;
  color: #999999;
}

/* 分隔线 */
.section-gap {
  height: 20rpx;
  background-color: #f5f5f5;
}

/* 功能列表 */
.mine-section {
  background-color: #ffffff;
}

.mine-item {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.mine-item:last-child {
  border-bottom: none;
}

.item-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.icon-svg {
  width: 40rpx;
  height: 40rpx;
  color: #ffffff;
}

.item-name {
  flex: 1;
  font-size: 32rpx;
  color: #111111;
}

.item-extra {
  font-size: 28rpx;
  color: #999999;
  margin-right: 16rpx;
}

.item-arrow {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  width: 32rpx;
  height: 32rpx;
  color: #cccccc;
}

/* 退出登录按钮 */
.logout-section {
  background-color: #ffffff;
  padding: 30rpx;
}

.logout-btn {
  background-color: #FF6B6B;
  border-radius: 12rpx;
  padding: 28rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 500;
}

/* 底部留白 */
.bottom-padding {
  height: 120rpx;
}
</style>
