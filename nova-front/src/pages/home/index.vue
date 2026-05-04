<template>
  <view class="home-page">
    <view class="nav-bar">
      <text class="nav-title">ChatNova</text>
      <view class="nav-right">
        <view class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/><path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          <text class="search-placeholder">搜索</text>
        </view>
        <view class="add-btn">
          <svg class="add-icon" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 8V16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M8 12H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
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

    <custom-tab-bar current="chat" :badge="tabBadge" />
  </view>
</template>

<script>
import ChatListItem from '@/components/chat-list-item/chat-list-item.vue';
import CustomTabBar from '@/components/custom-tab-bar/custom-tab-bar.vue';
import { getConversations } from '@/api/im';
import { isLoggedIn } from '@/utils/auth';
import { connectWS, onWSMessage } from '@/utils/websocket';

export default {
  components: { ChatListItem, CustomTabBar },
  data() {
    return {
      chatList: [],
      loading: false,
      refreshing: false,
      removeWSHandler: null,
    };
  },
  computed: {
    tabBadge() {
      const total = this.chatList.reduce((sum, item) => sum + (item.unread || 0), 0);
      return { chat: total, contacts: 0, discover: 0, me: 0 };
    }
  },
  onShow() {
    if (isLoggedIn()) {
      this.loadConversations();
    }
  },
  onLoad() {
    if (isLoggedIn()) {
      const userInfo = uni.getStorageSync('userInfo');
      if (userInfo && userInfo.userId) {
        connectWS(userInfo.userId);
      }
      this.removeWSHandler = onWSMessage((data) => {
        if (data.type === 'chat_received') {
          this.loadConversations();
        }
      });
    }
  },
  onUnload() {
    if (this.removeWSHandler) this.removeWSHandler();
  },
  methods: {
    async loadConversations() {
      this.loading = true;
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
          time: this.formatConvTime(c.lastMessageTime),
          unread: c.unreadCount || 0,
          chatType: 'single',
          chatId: String(c.id),
          targetUserId: c.targetUserId,
          targetAvatar: c.targetAvatar || '',
          conversationId: c.id,
        }));

        this.chatList = [aiItem, ...convItems];
      } catch (e) {
        console.error('loadConversations failed:', e);
        this.chatList = [{
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
        this.loading = false;
        this.refreshing = false;
      }
    },
    onRefresh() {
      this.refreshing = true;
      this.loadConversations();
    },
    handleChatClick(data) {
      const params = [
        `name=${encodeURIComponent(data.name)}`,
        `chatType=${data.chatType || 'single'}`,
        `chatId=${data.chatId || ''}`,
      ];
      if (data.targetUserId) params.push(`targetUserId=${data.targetUserId}`);
      if (data.targetAvatar) params.push(`targetAvatar=${encodeURIComponent(data.targetAvatar)}`);
      if (data.conversationId) params.push(`conversationId=${data.conversationId}`);
      uni.navigateTo({ url: `/pages/chat/index?${params.join('&')}` });
    },
    formatConvTime(timeStr) {
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
    },
  },
};
</script>

<style lang="scss" scoped>
page { background-color: #f5f5f5; }
.home-page { height: 100vh; background-color: #f5f5f5; display: flex; flex-direction: column; }
.nav-bar { flex-shrink: 0; height: 88rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 20rpx; background-color: #ffffff; border-bottom: 1rpx solid #e5e5e5; }
.nav-title { font-size: 36rpx; font-weight: 600; color: #111111; margin-right: 20rpx; }
.nav-right { flex: 1; display: flex; align-items: center; justify-content: flex-end; gap: 20rpx; }
.search-box { flex: 1; height: 64rpx; background-color: #f5f5f5; border-radius: 16rpx; display: flex; align-items: center; padding: 0 20rpx; }
.search-icon { width: 32rpx; height: 32rpx; color: #999999; margin-right: 12rpx; }
.search-placeholder { font-size: 28rpx; color: #999999; }
.add-btn { width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background-color: #f5f5f5; }
.add-icon { width: 40rpx; height: 40rpx; color: #111111; }
.chat-list { flex: 1; background-color: #ffffff; overflow-y: auto; }
.empty-tip { text-align: center; padding: 100rpx 40rpx; font-size: 28rpx; color: #999; }
.tab-bar { flex-shrink: 0; }
</style>
