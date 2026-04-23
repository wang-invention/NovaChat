<template>
  <view class="discover-page">
    <!-- 顶部导航栏 - 固定 -->
    <view class="nav-bar">
      <text class="nav-title">发现</text>
      <view class="nav-right">
        <view class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="search-placeholder">搜索</text>
        </view>
      </view>
    </view>

    <!-- 发现列表 - 可滚动区域 -->
    <scroll-view class="discover-list" scroll-y>
      <!-- 朋友圈 -->
      <view class="discover-section">
        <view class="discover-item">
          <view class="item-icon" style="background-color: #07C160;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="9" y1="9" x2="9.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="15" y1="9" x2="15.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">朋友圈</text>
          <view class="item-extra">
            <image class="update-avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=update" mode="aspectFill"/>
            <view class="update-dot"></view>
          </view>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 扫一扫、摇一摇 -->
      <view class="discover-section">
        <view class="discover-item">
          <view class="item-icon" style="background-color: #10AEFF;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 7V5a2 2 0 0 1 2-2h2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M17 3h2a2 2 0 0 1 2 2v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M21 17v2a2 2 0 0 1-2 2h-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 21H5a2 2 0 0 1-2-2v-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">扫一扫</text>
        </view>
        <view class="discover-item">
          <view class="item-icon" style="background-color: #FA9D3B;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2v20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12h20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <text class="item-name">摇一摇</text>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 看一看、搜一搜 -->
      <view class="discover-section">
        <view class="discover-item">
          <view class="item-icon" style="background-color: #E91E63;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <polygon points="10 8 16 12 10 16 10 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">看一看</text>
        </view>
        <view class="discover-item">
          <view class="item-icon" style="background-color: #9C27B0;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">搜一搜</text>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 附近的人 -->
      <view class="discover-section">
        <view class="discover-item">
          <view class="item-icon" style="background-color: #FF6B6B;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">附近的人</text>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 购物、游戏 -->
      <view class="discover-section">
        <view class="discover-item">
          <view class="item-icon" style="background-color: #FF5722;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="9" cy="21" r="1" stroke="currentColor" stroke-width="2"/>
              <circle cx="20" cy="21" r="1" stroke="currentColor" stroke-width="2"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="item-name">购物</text>
        </view>
        <view class="discover-item">
          <view class="item-icon" style="background-color: #FF9800;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="6" y1="11" x2="10" y2="11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="14" y1="11" x2="18" y2="11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="8" y1="15" x2="16" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 2a10 10 0 0 1 10 10c0 5.523-4.477 10-10 10S2 17.523 2 12 6.477 2 12 2z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <text class="item-name">游戏</text>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="section-gap"></view>

      <!-- 小程序 -->
      <view class="discover-section">
        <view class="discover-item">
          <view class="item-icon" style="background-color: #607D8B;">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
              <line x1="8" y1="21" x2="16" y2="21" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="17" x2="12" y2="21" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <text class="item-name">小程序</text>
        </view>
      </view>

      <!-- 更多功能 -->
      <view class="section-gap"></view>
      <view class="discover-section">
        <view class="discover-item" v-for="(item, index) in moreItems" :key="index">
          <view class="item-icon" :style="{ backgroundColor: item.color }">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" v-html="item.icon"></svg>
          </view>
          <text class="item-name">{{ item.name }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部 TabBar - 固定 -->
    <custom-tab-bar current="discover" />
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
          name: '直播', 
          color: '#E91E63',
          icon: '<circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="3" fill="currentColor"/>'
        },
        { 
          name: '视频号', 
          color: '#673AB7',
          icon: '<rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18" stroke="currentColor" stroke-width="2"/><line x1="7" y1="2" x2="7" y2="22" stroke="currentColor" stroke-width="2"/><line x1="17" y1="2" x2="17" y2="22" stroke="currentColor" stroke-width="2"/><line x1="2" y1="12" x2="22" y2="12" stroke="currentColor" stroke-width="2"/><line x1="2" y1="7" x2="7" y2="7" stroke="currentColor" stroke-width="2"/><line x1="2" y1="17" x2="7" y2="17" stroke="currentColor" stroke-width="2"/><line x1="17" y1="17" x2="22" y2="17" stroke="currentColor" stroke-width="2"/><line x1="17" y1="7" x2="22" y2="7" stroke="currentColor" stroke-width="2"/>'
        },
        { 
          name: '音乐', 
          color: '#00BCD4',
          icon: '<path d="M9 18V5l12-2v13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="6" cy="18" r="3" stroke="currentColor" stroke-width="2"/><circle cx="18" cy="16" r="3" stroke="currentColor" stroke-width="2"/>'
        },
        { 
          name: '新闻', 
          color: '#3F51B5',
          icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="10 9 9 9 8 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
        },
        { 
          name: '运动', 
          color: '#4CAF50',
          icon: '<polyline points="23 6 13.5 15.5 8.5 10.5 1 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="17 6 23 6 23 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
        },
        { 
          name: '小说', 
          color: '#795548',
          icon: '<path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
        },
        { 
          name: '漫画', 
          color: '#FF9800',
          icon: '<rect x="3" y="3" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"/><circle cx="8.5" cy="8.5" r="1.5" stroke="currentColor" stroke-width="2"/><circle cx="15.5" cy="8.5" r="1.5" stroke="currentColor" stroke-width="2"/><path d="M9 16c1.5 1 3 1 4.5 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
        },
        { 
          name: '电影', 
          color: '#607D8B',
          icon: '<rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18" stroke="currentColor" stroke-width="2"/><line x1="7" y1="2" x2="7" y2="22" stroke="currentColor" stroke-width="2"/><line x1="17" y1="2" x2="17" y2="22" stroke="currentColor" stroke-width="2"/><line x1="2" y1="12" x2="22" y2="12" stroke="currentColor" stroke-width="2"/><line x1="2" y1="7" x2="7" y2="7" stroke="currentColor" stroke-width="2"/><line x1="2" y1="17" x2="7" y2="17" stroke="currentColor" stroke-width="2"/><line x1="17" y1="17" x2="22" y2="17" stroke="currentColor" stroke-width="2"/><line x1="17" y1="7" x2="22" y2="7" stroke="currentColor" stroke-width="2"/>'
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

.discover-page {
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
  margin-right: 20rpx;
}

.nav-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.search-box {
  flex: 1;
  height: 64rpx;
  background-color: #f5f5f5;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
}

.search-icon {
  width: 32rpx;
  height: 32rpx;
  color: #999999;
  margin-right: 12rpx;
}

.search-placeholder {
  font-size: 28rpx;
  color: #999999;
}

/* 发现列表 - 可滚动区域 */
.discover-list {
  flex: 1;
  background-color: #f5f5f5;
  overflow-y: auto;
}

.discover-section {
  background-color: #ffffff;
}

.discover-item {
  display: flex;
  align-items: center;
  height: 120rpx;
  padding: 0 20rpx;
  border-bottom: 1rpx solid #e5e5e5;
}

.discover-item:last-child {
  border-bottom: none;
}

.item-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.icon-svg {
  width: 48rpx;
  height: 48rpx;
  color: #ffffff;
}

.item-name {
  flex: 1;
  font-size: 32rpx;
  color: #111111;
}

.item-extra {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.update-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 8rpx;
  background-color: #f5f5f5;
}

.update-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: #ff3b30;
}

.section-gap {
  height: 20rpx;
  background-color: #f5f5f5;
}

/* 底部 TabBar - 固定 */
.tab-bar {
  flex-shrink: 0;
}
</style>
