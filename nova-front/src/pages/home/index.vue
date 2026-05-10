<template>
  <view class="home-page">
    <view class="nav-bar" :style="{ marginTop: statusBarHeight + 'px' }">
      <text class="nav-title">ChatNova</text>
      <view class="nav-right">
        <view class="search-box">
          <svg-icon class="search-icon" icon="<circle cx='11' cy='11' r='8' stroke='currentColor' stroke-width='2'/><path d='M21 21L16.65 16.65' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="32" color="#999999" />
          <text class="search-placeholder">搜索</text>
        </view>
        <view class="add-btn">
          <svg-icon class="add-icon" icon="<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='2'/><path d='M12 8V16' stroke='currentColor' stroke-width='2' stroke-linecap='round'/><path d='M8 12H16' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="40" color="#111111" />
        </view>
      </view>
    </view>

    <scroll-view class="chat-list" scroll-y @refresherrefresh="onRefresh" :refresher-enabled="true" :refresher-triggered="refreshing">
      <chat-list-item
        v-for="(item, index) in chatList"
        :key="item.chatId || index"
        :data="item"
        :has-border="index < chatList.length - 1"
        @click="handleChatClick"
      />
      <view class="empty-tip" v-if="!loading && chatList.length === 0">
        <text>暂无会话，去通讯录找好友聊天吧</text>
      </view>
    </scroll-view>

    <my-tab-bar current="chat" :badge="tabBadge" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import ChatListItem from '@/components/chat-list-item/chat-list-item.vue';
import MyTabBar from '@/components/my-tab-bar/my-tab-bar.vue';
import { getConversations } from '@/api/im';
import { isLoggedIn } from '@/utils/auth';
import { connectWS, onWSMessage } from '@/utils/websocket';

const chatList = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const statusBarHeight = ref(0);
const navBarHeight = ref(0);

let removeWSHandler = null;

const tabBadge = computed(() => {
  const total = chatList.value.reduce((sum, item) => sum + (item.unread || 0), 0);
  return { chat: total, contacts: 0, discover: 0, me: 0 };
});

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;
  navBarHeight.value = statusBarHeight.value + 44;
  if (isLoggedIn()) {
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo && userInfo.userId) {
      connectWS(userInfo.userId);
    }
    removeWSHandler = onWSMessage((data) => {
      if (data.type === 'chat_received') {
        loadConversations();
      }
    });
  }
});

onUnmounted(() => {
  if (removeWSHandler) removeWSHandler();
});

onShow(() => {
  if (isLoggedIn()) {
    loadConversations();
  }
});

async function loadConversations() {
  loading.value = true;
  try {
    const res = await getConversations();
    const conversations = res.data || [];

    const aiItem = {
      name: 'AI 助手',
      avatarType: 'icon',
      iconBg: '#10AEFF',
      iconName: 'robot',
      lastMessage: '你好，我是你的AI助手',
      time: '',
      unread: 0,
      chatType: 'ai',
      chatId: 'ai_default',
    };

    const convItems = conversations.map((c) => ({
      name: c.targetNickname || `用户${c.targetUserId}`,
      avatarType: 'single',
      avatar: c.targetAvatar || '',
      lastMessage: c.lastMessage || '',
      time: formatConvTime(c.lastMessageTime),
      unread: c.unreadCount || 0,
      chatType: 'single',
      chatId: String(c.id),
      targetUserId: c.targetUserId,
      targetAvatar: c.targetAvatar || '',
      conversationId: c.id,
    }));

    chatList.value = [aiItem, ...convItems];
  } catch (e) {
    console.error('loadConversations failed:', e);
    chatList.value = [{
      name: 'AI 助手',
      avatarType: 'icon',
      iconBg: '#10AEFF',
      iconName: 'robot',
      lastMessage: '你好，我是你的AI助手',
      time: '',
      unread: 0,
      chatType: 'ai',
      chatId: 'ai_default',
    }];
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

function onRefresh() {
  refreshing.value = true;
  loadConversations();
}

function handleChatClick(data) {
  const params = [
    `name=${encodeURIComponent(data.name)}`,
    `chatType=${data.chatType || 'single'}`,
    `chatId=${data.chatId || ''}`,
  ];
  if (data.targetUserId) params.push(`targetUserId=${data.targetUserId}`);
  if (data.targetAvatar) params.push(`targetAvatar=${encodeURIComponent(data.targetAvatar)}`);
  if (data.conversationId) params.push(`conversationId=${data.conversationId}`);
  uni.navigateTo({ url: `/pages/chat/index?${params.join('&')}` });
}

function formatConvTime(timeStr) {
  if (!timeStr) return '';
  try {
    const d = new Date(timeStr);
    const now = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    const isToday = d.toDateString() === now.toDateString();
    if (isToday) return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (d.toDateString() === yesterday.toDateString()) return '昨天';
    if (d.getFullYear() === now.getFullYear()) return `${d.getMonth() + 1}月${d.getDate()}日`;
    return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()}`;
  } catch (e) {
    return '';
  }
}
</script>

<style lang="scss" scoped>
page { background-color: #f5f5f5; }
.home-page { height: 100vh; background-color: #f5f5f5; display: flex; flex-direction: column; }
.nav-bar { flex-shrink: 0; height: 88rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 20rpx; background-color: #ffffff; border-bottom: 1rpx solid #e5e5e5; box-sizing: border-box; }
.nav-title { font-size: 36rpx; font-weight: 600; color: #111111; margin-right: 20rpx; }
.nav-right { flex: 1; display: flex; align-items: center; justify-content: flex-end; gap: 20rpx; }
.search-box { flex: 1; height: 64rpx; background-color: #f5f5f5; border-radius: 16rpx; display: flex; align-items: center; padding: 0 20rpx; }
.search-icon { width: 32rpx; height: 32rpx; color: #999999; margin-right: 12rpx; }
.search-placeholder { font-size: 28rpx; color: #999999; }
.add-btn { width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background-color: #f5f5f5; }
.add-icon { width: 40rpx; height: 40rpx; color: #111111; }
.chat-list { flex: 1; background-color: #ffffff; overflow-y: auto; padding-bottom: 100rpx; }
.empty-tip { text-align: center; padding: 100rpx 40rpx; font-size: 28rpx; color: #999; }
</style>
