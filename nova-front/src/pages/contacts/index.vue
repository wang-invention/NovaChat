<template>
  <view class="contacts-page">
    <!-- 顶部导航栏 - 固定 -->
    <view class="nav-bar">
      <text class="nav-title">通讯录</text>
      <view class="nav-right">
        <view class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="search-placeholder">搜索</text>
        </view>
        <view class="add-btn">
          <svg class="add-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 12H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
      </view>
    </view>

    <!-- 通讯录列表 - 可滚动区域 -->
    <scroll-view class="contacts-list" scroll-y>
      <!-- 新的朋友 -->
      <view class="section-title">新的朋友</view>
      <view class="contact-item">
        <view class="contact-icon" style="background-color: #FA9D3B;">
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M20 8v6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M23 11h-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="contact-name">新的朋友</text>
        <view class="contact-badge" v-if="newFriendsCount > 0">
          <text class="badge-text">{{ newFriendsCount }}</text>
        </view>
      </view>

      <!-- 群聊 -->
      <view class="section-title">群聊</view>
      <view class="contact-item">
        <view class="contact-icon" style="background-color: #07C160;">
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="contact-name">群聊</text>
      </view>

      <!-- 标签 -->
      <view class="section-title">标签</view>
      <view class="contact-item">
        <view class="contact-icon" style="background-color: #10AEFF;">
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="7" y1="7" x2="7.01" y2="7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="contact-name">标签</text>
      </view>

      <!-- 公众号 -->
      <view class="section-title">公众号</view>
      <view class="contact-item">
        <view class="contact-icon" style="background-color: #FF6B6B;">
          <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="contact-name">公众号</text>
      </view>

      <!-- 我的好友 -->
      <view class="section-title">我的好友</view>
      <view class="contact-item" v-for="(item, index) in contacts" :key="index">
        <image class="contact-avatar" :src="item.avatar" mode="aspectFill"/>
        <view class="contact-info">
          <text class="contact-name">{{ item.name }}</text>
          <text class="contact-status" v-if="item.status">{{ item.status }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部 TabBar - 固定 -->
    <custom-tab-bar current="contacts" />
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
      newFriendsCount: 3,
      contacts: [
        { name: 'AI 助手', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=ai', status: '在线' },
        { name: '小美', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=meimei', status: '忙碌' },
        { name: '张三', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan' },
        { name: '李四', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', status: '在线' },
        { name: '王五', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu' },
        { name: '赵六', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu', status: '离开' },
        { name: '孙七', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunqi' },
        { name: '周八', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouba', status: '在线' },
        { name: '吴九', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=wujiu' },
        { name: '郑十', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengshi', status: '忙碌' },
        { name: '钱十一', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=qianshiyi' },
        { name: '冯十二', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=fengshier', status: '在线' },
        { name: '陈十三', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=chenshisan' },
        { name: '褚十四', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=chushisi' },
        { name: '卫十五', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=weishiwu', status: '在线' },
        { name: '蒋十六', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=jiangshiliu' },
        { name: '沈十七', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shenshiqi', status: '离开' },
        { name: '韩十八', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=hanshiba' },
        { name: '杨十九', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=yangshijiu', status: '在线' },
        { name: '朱二十', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhuershi' },
        { name: '秦二一', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=qineryi' },
        { name: '尤二二', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=youerer', status: '忙碌' },
        { name: '许二三', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xuersan' },
        { name: '何二四', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=heersi', status: '在线' },
        { name: '吕二五', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=lverwu' },
        { name: '施二六', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shierliu' },
        { name: '张二七', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangerqi', status: '在线' },
        { name: '孔二八', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=kongerba' },
        { name: '曹二九', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=caorerjiu', status: '离开' },
        { name: '严三十', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=yansanshi' }
      ]
    };
  }
};
</script>

<style lang="scss" scoped>
page {
  background-color: #f5f5f5;
}

.contacts-page {
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
  gap: 20rpx;
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

.add-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #f5f5f5;
}

.add-icon {
  width: 40rpx;
  height: 40rpx;
  color: #111111;
}

/* 通讯录列表 - 可滚动区域 */
.contacts-list {
  flex: 1;
  background-color: #ffffff;
  overflow-y: auto;
}

.section-title {
  height: 60rpx;
  line-height: 60rpx;
  padding: 0 20rpx;
  font-size: 24rpx;
  color: #666666;
  background-color: #f5f5f5;
}

.contact-item {
  display: flex;
  align-items: center;
  height: 120rpx;
  padding: 0 20rpx;
  border-bottom: 1rpx solid #e5e5e5;
}

.contact-icon {
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

.contact-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  margin-right: 24rpx;
  background-color: #f5f5f5;
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.contact-name {
  font-size: 32rpx;
  color: #111111;
}

.contact-status {
  font-size: 24rpx;
  color: #999999;
  margin-top: 4rpx;
}

/* 新的朋友角标 */
.contact-badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  background-color: #ff3b30;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-text {
  font-size: 22rpx;
  font-weight: 500;
  color: #ffffff;
}

/* 底部 TabBar - 固定 */
.tab-bar {
  flex-shrink: 0;
}
</style>
