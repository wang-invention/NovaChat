<template>
  <view class="tab-bar">
    <view
      class="tab-item"
      :class="{ active: current === 'chat' }"
      @click="switchTab('chat', '/pages/home/index')"
    >
      <view class="tab-icon-wrap">
        <svg-icon class="tab-icon" icon="<path d='M21 11.5C21.0034 12.8199 20.6951 14.1219 20.1 15.3C19.3944 16.7118 18.3098 17.8992 16.9674 18.7293C15.6251 19.5594 14.0782 19.9994 12.5 20C11.1801 20.0035 9.87812 19.6951 8.7 19.1L3 21L4.9 15.3C4.30493 14.1219 3.99656 12.8199 4 11.5C4.00061 9.92179 4.44061 8.37488 5.27072 7.03258C6.10083 5.69028 7.28825 4.6056 8.7 3.90003C9.87812 3.30496 11.1801 2.99659 12.5 3.00003H13C15.0843 3.11502 17.053 3.99479 18.5291 5.47089C20.0052 6.94699 20.885 8.91568 21 11V11.5Z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="44" color="#999999" />
        <view class="tab-badge" v-if="badge.chat > 0">
          <text class="tab-badge-text">{{ badge.chat > 99 ? '99+' : badge.chat }}</text>
        </view>
      </view>
      <text class="tab-text">聊天</text>
    </view>

    <view
      class="tab-item"
      :class="{ active: current === 'contacts' }"
      @click="switchTab('contacts', '/pages/contacts/index')"
    >
      <view class="tab-icon-wrap">
        <svg-icon class="tab-icon" icon="<path d='M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
          <circle cx='9' cy='7' r='4' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
          <path d='M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
          <path d='M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="44" color="#999999" />
        <view class="tab-badge" v-if="badge.contacts > 0">
          <text class="tab-badge-text">{{ badge.contacts > 99 ? '99+' : badge.contacts }}</text>
        </view>
      </view>
      <text class="tab-text">通讯录</text>
    </view>

    <view
      class="tab-item"
      :class="{ active: current === 'discover' }"
      @click="switchTab('discover', '/pages/discover/index')"
    >
      <view class="tab-icon-wrap">
        <svg-icon class="tab-icon" icon="<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='2'/>
          <path d='M2 12H22' stroke='currentColor' stroke-width='2'/>
          <path d='M12 2C14.5013 4.73835 15.9228 8.29203 16 12C15.9228 15.708 14.5013 19.2616 12 22C9.49872 19.2616 8.07725 15.708 8 12C8.07725 8.29203 9.49872 4.73835 12 2Z' stroke='currentColor' stroke-width='2'/>" size="44" color="#999999" />
        <view class="tab-badge" v-if="badge.discover > 0">
          <text class="tab-badge-text">{{ badge.discover > 99 ? '99+' : badge.discover }}</text>
        </view>
      </view>
      <text class="tab-text">发现</text>
    </view>

    <view
      class="tab-item"
      :class="{ active: current === 'me' }"
      @click="switchTab('me', '/pages/mine/index')"
    >
      <view class="tab-icon-wrap">
        <svg-icon class="tab-icon" icon="<path d='M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
          <circle cx='12' cy='7' r='4' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="44" color="#999999" />
        <view class="tab-badge" v-if="badge.me > 0">
          <text class="tab-badge-text">{{ badge.me > 99 ? '99+' : badge.me }}</text>
        </view>
      </view>
      <text class="tab-text">我的</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  current: {
    type: String,
    default: 'chat'
  },
  badge: {
    type: Object,
    default: () => ({
      chat: 0,
      contacts: 0,
      discover: 0,
      me: 0
    })
  }
});

function switchTab(tab, url) {
  if (props.current === tab) return;
  uni.reLaunch({ url });
}
</script>

<style lang="scss" scoped>
.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  background-color: #ffffff;
  border-top: 1rpx solid #e5e5e5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
}

.tab-item {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.tab-item:active {
  opacity: 0.7;
}

.tab-icon-wrap {
  position: relative;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-icon {
  width: 44rpx;
  height: 44rpx;
  color: #999999;
}

.tab-item.active .tab-icon {
  color: #07c160;
}

.tab-text {
  font-size: 22rpx;
  color: #999999;
}

.tab-item.active .tab-text {
  color: #07c160;
}

.tab-badge {
  position: absolute;
  top: -8rpx;
  right: -16rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 6rpx;
  background-color: #ff3b30;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-badge-text {
  font-size: 20rpx;
  color: #ffffff;
  line-height: 1;
}
</style>
