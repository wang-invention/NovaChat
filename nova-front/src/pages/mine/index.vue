<template>
  <view class="mine-page">
    <!-- 顶部导航栏 - 固定 -->
    <view class="nav-bar">
      <text class="nav-title">我的</text>
      <view class="nav-right">
        <view class="icon-btn">
          <svg class="icon-svg-small" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
        <view class="icon-btn">
          <svg class="icon-svg-small" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="1" fill="currentColor"/>
            <circle cx="19" cy="12" r="1" fill="currentColor"/>
            <circle cx="5" cy="12" r="1" fill="currentColor"/>
          </svg>
        </view>
      </view>
    </view>

    <!-- 我的页面内容 - 可滚动区域 -->
    <scroll-view class="mine-content" scroll-y>
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <view class="user-main">
          <image class="user-avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=me" mode="aspectFill"/>
          <view class="user-info">
            <view class="user-name-row">
              <text class="user-name">ChatNova</text>
            </view>
            <view class="user-tags">
              <view class="user-tag vip">VIP</view>
              <view class="user-tag level">Lv.8</view>
            </view>
            <text class="user-id">ID: 123456789</text>
          </view>
        </view>
        <view class="user-qrcode">
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
        <view class="mine-item">
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
        <view class="mine-item" v-for="(item, index) in moreItems" :key="index">
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
      <view class="section-gap"></view>
      <view class="logout-section">
        <view class="logout-btn">
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

<script>
import CustomTabBar from '@/components/custom-tab-bar/custom-tab-bar.vue';

export default {
  components: {
    CustomTabBar
  },
  data() {
    return {
      moreItems: [
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
      ]
    };
  }
};
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 20rpx;
  background-color: #ffffff;
}

.user-main {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 24rpx;
  margin-right: 24rpx;
  background-color: #f5f5f5;
  flex-shrink: 0;
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
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #111111;
}

.user-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.user-tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.user-tag.vip {
  background-color: #FFD700;
  color: #8B4513;
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
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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
  height: 110rpx;
  padding: 0 20rpx;
  border-bottom: 1rpx solid #e5e5e5;
}

.mine-item:last-child {
  border-bottom: none;
}

.item-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.icon-svg {
  width: 36rpx;
  height: 36rpx;
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
  margin-right: 12rpx;
}

.item-arrow {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.arrow-icon {
  width: 24rpx;
  height: 24rpx;
  color: #c8c8c8;
}

/* 退出登录 */
.logout-section {
  padding: 0 20rpx;
}

.logout-btn {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border-radius: 12rpx;
}

.logout-text {
  font-size: 32rpx;
  color: #ff3b30;
}

/* 底部留白 */
.bottom-padding {
  height: 40rpx;
}

/* 底部 TabBar - 固定 */
.tab-bar {
  flex-shrink: 0;
}
</style>
