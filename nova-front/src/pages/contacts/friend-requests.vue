<template>
  <view class="friend-requests-page">
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none"><path d="M15 18l-6-6 6-6" stroke="#111" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </view>
      <text class="nav-title">新的朋友</text>
    </view>

    <scroll-view class="request-list" scroll-y @refresherrefresh="onRefresh" :refresher-enabled="true" :refresher-triggered="refreshing">
      <view class="request-item" v-for="req in requests" :key="req.id">
        <view class="request-avatar-wrap">
          <image v-if="req.fromAvatar" class="request-avatar" :src="req.fromAvatar" mode="aspectFill" />
          <view v-else class="request-avatar-placeholder">
            <text class="avatar-text">{{ (req.fromNickname || '?')[0] }}</text>
          </view>
        </view>
        <view class="request-info">
          <text class="request-name">{{ req.fromNickname || req.fromUsername || '用户' }}</text>
          <text class="request-msg">{{ req.message || '请求添加你为好友' }}</text>
        </view>
        <view class="request-actions" v-if="req.status === 0">
          <view class="btn-accept" @click="acceptReq(req.id)"><text>同意</text></view>
          <view class="btn-reject" @click="rejectReq(req.id)"><text>拒绝</text></view>
        </view>
        <view class="request-status" v-else-if="req.status === 1">
          <text class="status-accepted">已同意</text>
        </view>
        <view class="request-status" v-else-if="req.status === 2">
          <text class="status-rejected">已拒绝</text>
        </view>
      </view>

      <view class="empty-tip" v-if="!loading && requests.length === 0">
        <text>暂无好友申请</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getPendingFriendRequests, acceptFriendRequest, rejectFriendRequest } from '@/api/im';

export default {
  data() {
    return {
      requests: [],
      loading: false,
      refreshing: false,
    };
  },
  onShow() {
    this.loadRequests();
  },
  methods: {
    async loadRequests() {
      this.loading = true;
      try {
        const res = await getPendingFriendRequests();
        this.requests = res.data || [];
      } catch (e) {
        console.error('loadRequests failed:', e);
      } finally {
        this.loading = false;
        this.refreshing = false;
      }
    },
    onRefresh() {
      this.refreshing = true;
      this.loadRequests();
    },
    async acceptReq(requestId) {
      try {
        await acceptFriendRequest(requestId);
        uni.showToast({ title: '已添加好友', icon: 'success' });
        const req = this.requests.find((r) => r.id === requestId);
        if (req) req.status = 1;
      } catch (e) {
        console.error('acceptFriendRequest failed:', e);
      }
    },
    async rejectReq(requestId) {
      try {
        await rejectFriendRequest(requestId);
        uni.showToast({ title: '已拒绝', icon: 'none' });
        const req = this.requests.find((r) => r.id === requestId);
        if (req) req.status = 2;
      } catch (e) {
        console.error('rejectFriendRequest failed:', e);
      }
    },
    goBack() {
      uni.navigateBack();
    },
  },
};
</script>

<style lang="scss" scoped>
page { background-color: #f5f5f5; }
.friend-requests-page { height: 100vh; background-color: #f5f5f5; display: flex; flex-direction: column; }
.nav-bar { flex-shrink: 0; height: 88rpx; display: flex; align-items: center; padding: 0 20rpx; background-color: #ffffff; border-bottom: 1rpx solid #e5e5e5; }
.nav-back { width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; margin-right: 8rpx; }
.back-icon { width: 40rpx; height: 40rpx; }
.nav-title { font-size: 36rpx; font-weight: 600; color: #111; }

.request-list { flex: 1; background-color: #ffffff; }
.request-item { display: flex; align-items: center; min-height: 120rpx; padding: 16rpx 20rpx; border-bottom: 1rpx solid #e5e5e5; }
.request-avatar-wrap { margin-right: 24rpx; flex-shrink: 0; }
.request-avatar { width: 80rpx; height: 80rpx; border-radius: 12rpx; background-color: #f5f5f5; }
.request-avatar-placeholder { width: 80rpx; height: 80rpx; border-radius: 12rpx; background-color: #10aeff; display: flex; align-items: center; justify-content: center; }
.avatar-text { font-size: 36rpx; font-weight: 600; color: #ffffff; }
.request-info { flex: 1; display: flex; flex-direction: column; }
.request-name { font-size: 32rpx; color: #111; }
.request-msg { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.request-actions { display: flex; gap: 16rpx; flex-shrink: 0; }
.btn-accept { padding: 12rpx 28rpx; background-color: #07c160; border-radius: 12rpx; }
.btn-accept text { font-size: 24rpx; color: #ffffff; }
.btn-reject { padding: 12rpx 28rpx; background-color: #e5e5e5; border-radius: 12rpx; }
.btn-reject text { font-size: 24rpx; color: #666; }
.request-status { flex-shrink: 0; }
.status-accepted { font-size: 26rpx; color: #07c160; }
.status-rejected { font-size: 26rpx; color: #999; }
.empty-tip { text-align: center; padding: 100rpx 40rpx; font-size: 28rpx; color: #999; }
</style>
