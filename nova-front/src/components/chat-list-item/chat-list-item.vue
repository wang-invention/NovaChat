<template>
  <view 
    class="chat-list-item" 
    :class="{ 'has-border': hasBorder }"
    @click="handleClick"
    role="button"
    tabindex="0"
    @keydown.enter="handleClick"
  >
    <!-- 左侧头像 -->
    <view class="avatar-wrap">
      <image 
        class="avatar" 
        :src="data.avatar" 
        mode="aspectFill"
        v-if="data.avatarType === 'single' && data.avatar"
      />
      <view class="avatar-placeholder" v-else-if="data.avatarType === 'single' && !data.avatar">
        <text class="avatar-placeholder-text">{{ (data.name || '?')[0] }}</text>
      </view>
      <view class="group-avatar" v-else-if="data.avatarType === 'group'">
        <image 
          v-for="(img, idx) in data.avatarList.slice(0, 4)" 
          :key="idx"
          class="group-avatar-img"
          :src="img"
          mode="aspectFill"
        />
      </view>
      <view class="icon-avatar" :style="{ backgroundColor: data.iconBg }" v-else-if="data.avatarType === 'icon'">
        <svg-icon class="icon-svg" v-if="data.iconName === 'robot'" icon="<rect x='3' y='11' width='18' height='10' rx='2' stroke='currentColor' stroke-width='2'/>
          <path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2'/>
          <circle cx='9' cy='16' r='1' fill='currentColor'/>
          <circle cx='15' cy='16' r='1' fill='currentColor'/>" size="40" />
        <svg-icon class="icon-svg" v-else-if="data.iconName === 'file'" icon="<path d='M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>
          <path d='M14 2V8H20' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>" size="40" />
        <svg-icon class="icon-svg" v-else-if="data.iconName === 'subscription'" icon="<rect x='3' y='4' width='18' height='16' rx='2' stroke='currentColor' stroke-width='2'/>
          <path d='M8 11H16' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>
          <path d='M8 15H13' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="40" />
        <text class="icon-text" v-else>{{ data.iconText }}</text>
      </view>
      <!-- 未读角标 -->
      <view class="unread-badge" v-if="data.unread > 0">
        <text class="unread-text">{{ data.unread > 99 ? '99+' : data.unread }}</text>
      </view>
    </view>
    
    <!-- 中间内容 -->
    <view class="content-wrap">
      <view class="content-top">
        <text class="contact-name">{{ data.name }}</text>
        <text class="message-time">{{ data.time }}</text>
      </view>
      <text class="message-preview">{{ data.lastMessage }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({
      name: '',
      avatar: '',
      avatarType: 'single', // single, group, icon
      avatarList: [],
      iconBg: '#07C160',
      iconName: '',
      iconText: '',
      lastMessage: '',
      time: '',
      unread: 0
    })
  },
  hasBorder: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['click']);

const handleClick = () => {
  emit('click', props.data);
};
</script>

<style lang="scss" scoped>
.chat-list-item {
  display: flex;
  align-items: center;
  height: 160rpx;
  padding: 0 20rpx;
  background-color: #ffffff;
  transition: background-color 0.15s ease;
}

.chat-list-item:active {
  background-color: #f5f5f5;
}

.chat-list-item.has-border {
  border-bottom: 1rpx solid #e5e5e5;
}

/* 头像区域 */
.avatar-wrap {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  background-color: #f5f5f5;
}

.avatar-placeholder {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  background-color: #10aeff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder-text {
  font-size: 44rpx;
  font-weight: 600;
  color: #ffffff;
}

/* 群聊头像（四格） */
.group-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  overflow: hidden;
  display: flex;
  flex-wrap: wrap;
  background-color: #e5e5e5;
}

.group-avatar-img {
  width: 50rpx;
  height: 50rpx;
  object-fit: cover;
}

/* 图标头像 */
.icon-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg {
  width: 56rpx;
  height: 56rpx;
  color: #ffffff;
}

.icon-text {
  font-size: 40rpx;
  font-weight: 600;
  color: #ffffff;
}

/* 未读角标 */
.unread-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  background-color: #ff3b30;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #ffffff;
}

.unread-text {
  font-size: 22rpx;
  font-weight: 500;
  color: #ffffff;
}

/* 内容区域 */
.content-wrap {
  flex: 1;
  height: 100rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}

.content-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contact-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #111111;
  line-height: 1.4;
}

.message-time {
  font-size: 24rpx;
  color: #999999;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.message-preview {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
