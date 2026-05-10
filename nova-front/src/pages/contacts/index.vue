<template>
  <view class="contacts-page">
    <view class="nav-bar" :style="{ marginTop: statusBarHeight + 'px' }">
      <text class="nav-title">通讯录</text>
      <view class="nav-right">
        <view class="search-box" @click="showSearch = true">
          <svg-icon class="search-icon" icon="<circle cx='11' cy='11' r='8' stroke='currentColor' stroke-width='2'/><path d='M21 21L16.65 16.65' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="32" color="#999999" />
          <text class="search-placeholder">搜索用户</text>
        </view>
        <view class="add-friend-btn" @click="openSearchForAddFriend">
          <svg-icon icon="<path d='M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><circle cx='9' cy='7' r='4' stroke='currentColor' stroke-width='2'/><path d='M20 8v6M17 11h6' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="40" color="#111111" />
        </view>
      </view>
    </view>

    <scroll-view class="contacts-list" scroll-y @refresherrefresh="onRefresh" :refresher-enabled="true" :refresher-triggered="refreshing">
      <view class="func-entries">
        <view class="func-item" @click="goFriendRequests">
          <view class="func-icon" style="background-color: #ff9500;">
            <svg-icon icon="<path d='M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><circle cx='9' cy='7' r='4' stroke='currentColor' stroke-width='2'/><path d='M20 8v6M17 11h6' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>" size="40" color="#ffffff" />
          </view>
          <text class="func-text">新的朋友</text>
          <view class="func-badge" v-if="pendingCount > 0">
            <text class="func-badge-text">{{ pendingCount > 99 ? '99+' : pendingCount }}</text>
          </view>
        </view>
      </view>

      <view class="section-title">我的好友</view>
      <view class="contact-item" v-for="user in friends" :key="user.friendId" @click="startChat(user)">
        <view class="contact-avatar-wrap">
          <image v-if="user.avatar" class="contact-avatar" :src="user.avatar" mode="aspectFill" />
          <view v-else class="contact-avatar-placeholder">
            <text class="avatar-text">{{ (user.nickname || user.username || '?')[0] }}</text>
          </view>
        </view>
        <view class="contact-info">
          <text class="contact-name">{{ user.remark || user.nickname || user.username }}</text>
          <text class="contact-id" v-if="user.remark">{{ user.nickname || user.username }}</text>
        </view>
        <view class="chat-action">
          <text class="chat-action-text">发消息</text>
        </view>
      </view>

      <view class="empty-tip" v-if="!loading && friends.length === 0">
        <text>暂无好友，去添加好友吧</text>
      </view>
    </scroll-view>

    <view class="search-mask" v-if="showSearch" @click="showSearch = false">
      <view class="search-panel" @click.stop>
        <view class="search-input-wrap">
          <input class="search-input" v-model="searchKeyword" placeholder="输入用户名/手机号搜索" confirm-type="search" @confirm="doSearch" />
          <view class="search-cancel" @click="showSearch = false"><text>取消</text></view>
        </view>
        <scroll-view class="search-results" scroll-y v-if="searchResults.length > 0">
          <view class="contact-item" v-for="user in searchResults" :key="user.id">
            <view class="contact-avatar-wrap">
              <image v-if="user.avatar" class="contact-avatar" :src="user.avatar" mode="aspectFill" />
              <view v-else class="contact-avatar-placeholder">
                <text class="avatar-text">{{ (user.nickname || user.username || '?')[0] }}</text>
              </view>
            </view>
            <view class="contact-info">
              <text class="contact-name">{{ user.nickname || user.username }}</text>
            </view>
            <view class="add-action" v-if="!user.isFriend" @click="openAddFriend(user)">
              <text class="add-action-text">加好友</text>
            </view>
            <view class="already-friend" v-else>
              <text class="already-friend-text">已添加</text>
            </view>
          </view>
        </scroll-view>
        <view class="search-empty" v-if="searchSearched && searchResults.length === 0">
          <text>未找到用户</text>
        </view>
      </view>
    </view>

    <view class="add-friend-mask" v-if="showAddFriend" @click="showAddFriend = false">
      <view class="add-friend-panel" @click.stop>
        <view class="add-friend-title">添加好友</view>
        <view class="add-friend-user" v-if="addFriendTarget">
          <view class="contact-avatar-wrap">
            <image v-if="addFriendTarget.avatar" class="contact-avatar" :src="addFriendTarget.avatar" mode="aspectFill" />
            <view v-else class="contact-avatar-placeholder">
              <text class="avatar-text">{{ (addFriendTarget.nickname || addFriendTarget.username || '?')[0] }}</text>
            </view>
          </view>
          <view class="contact-info">
            <text class="contact-name">{{ addFriendTarget.nickname || addFriendTarget.username }}</text>
          </view>
        </view>
        <input class="add-friend-msg" v-model="addFriendMsg" placeholder="我是你的好友" />
        <view class="add-friend-actions">
          <view class="add-friend-cancel" @click="showAddFriend = false"><text>取消</text></view>
          <view class="add-friend-confirm" @click="doAddFriend"><text>发送申请</text></view>
        </view>
      </view>
    </view>

    <my-tab-bar current="contacts" />
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import MyTabBar from '@/components/my-tab-bar/my-tab-bar.vue';
import { searchUsers, getFriendList, getPendingFriendRequests, sendFriendRequest, isFriend } from '@/api/im';
import { isLoggedIn } from '@/utils/auth';
import { onWSMessage } from '@/utils/websocket';

const friends = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const pendingCount = ref(0);
const showSearch = ref(false);
const searchKeyword = ref('');
const searchResults = ref([]);
const searchSearched = ref(false);
const showAddFriend = ref(false);
const addFriendTarget = ref(null);
const addFriendMsg = ref('');
const statusBarHeight = ref(0);

let removeWSHandler = null;

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;
  removeWSHandler = onWSMessage((data) => {
    if (data.type === 'friend_request' || data.type === 'friend_accepted') {
      loadPendingCount();
      if (data.type === 'friend_accepted') {
        loadFriends();
      }
    }
  });
});

onUnmounted(() => {
  if (removeWSHandler) removeWSHandler();
});

onShow(() => {
  if (isLoggedIn()) {
    loadFriends();
    loadPendingCount();
  }
});

async function loadFriends() {
  loading.value = true;
  try {
    const res = await getFriendList();
    friends.value = res.data || [];
  } catch (e) {
    console.error('loadFriends failed:', e);
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

async function loadPendingCount() {
  try {
    const res = await getPendingFriendRequests();
    pendingCount.value = (res.data || []).length;
  } catch (e) {
    console.error('loadPendingCount failed:', e);
  }
}

function onRefresh() {
  refreshing.value = true;
  loadFriends();
  loadPendingCount();
}

function openSearchForAddFriend() {
  searchKeyword.value = '';
  searchResults.value = [];
  searchSearched.value = false;
  showSearch.value = true;
}

async function doSearch() {
  if (!searchKeyword.value.trim()) return;
  searchSearched.value = true;
  try {
    const res = await searchUsers(searchKeyword.value.trim());
    const me = uni.getStorageSync('userInfo');
    const myId = me?.userId;
    const users = (res.data || []).filter((u) => u.id !== myId);

    const results = [];
    for (const u of users) {
      try {
        const fr = await isFriend(u.id);
        u.isFriend = fr.data === true;
      } catch (e) {
        u.isFriend = false;
      }
      results.push(u);
    }
    searchResults.value = results;
  } catch (e) {
    console.error('searchUsers failed:', e);
  }
}

function openAddFriend(user) {
  addFriendTarget.value = user;
  addFriendMsg.value = '';
  showAddFriend.value = true;
}

async function doAddFriend() {
  if (!addFriendTarget.value) return;
  try {
    await sendFriendRequest({
      targetUserId: addFriendTarget.value.id,
      message: addFriendMsg.value || '我是你的好友',
    });
    uni.showToast({ title: '申请已发送', icon: 'success' });
    showAddFriend.value = false;
    showSearch.value = false;
  } catch (e) {
    console.error('sendFriendRequest failed:', e);
  }
}

function startChat(user) {
  uni.navigateTo({
    url: `/pages/chat/index?name=${encodeURIComponent(user.remark || user.nickname || user.username)}&chatType=single&targetUserId=${user.friendId}&targetAvatar=${encodeURIComponent(user.avatar || '')}`,
  });
}

function goFriendRequests() {
  uni.navigateTo({ url: '/pages/contacts/friend-requests' });
}
</script>

<style lang="scss" scoped>
page { background-color: #f5f5f5; }
.contacts-page { height: 100vh; background-color: #f5f5f5; display: flex; flex-direction: column; }
.nav-bar { flex-shrink: 0; height: 88rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 20rpx; background-color: #ffffff; border-bottom: 1rpx solid #e5e5e5; box-sizing: border-box; }
.nav-title { font-size: 36rpx; font-weight: 600; color: #111111; margin-right: 20rpx; }
.nav-right { flex: 1; display: flex; align-items: center; justify-content: flex-end; gap: 16rpx; }
.search-box { flex: 1; height: 64rpx; background-color: #f5f5f5; border-radius: 16rpx; display: flex; align-items: center; padding: 0 20rpx; }
.search-icon { width: 32rpx; height: 32rpx; color: #999999; margin-right: 12rpx; }
.search-placeholder { font-size: 28rpx; color: #999999; }
.add-friend-btn { width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; color: #07c160; }
.contacts-list { flex: 1; background-color: #ffffff; overflow-y: auto; }

.func-entries { padding: 0 20rpx; border-bottom: 1rpx solid #e5e5e5; }
.func-item { display: flex; align-items: center; height: 120rpx; position: relative; }
.func-icon { width: 80rpx; height: 80rpx; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; margin-right: 24rpx; }
.func-text { font-size: 32rpx; color: #111; }
.func-badge { position: absolute; top: 16rpx; left: 80rpx; min-width: 36rpx; height: 36rpx; padding: 0 8rpx; background: #ff3b30; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; }
.func-badge-text { font-size: 22rpx; color: #fff; }

.section-title { height: 60rpx; line-height: 60rpx; padding: 0 20rpx; font-size: 24rpx; color: #666; background-color: #f5f5f5; }
.contact-item { display: flex; align-items: center; height: 120rpx; padding: 0 20rpx; border-bottom: 1rpx solid #e5e5e5; }
.contact-avatar-wrap { margin-right: 24rpx; flex-shrink: 0; }
.contact-avatar { width: 80rpx; height: 80rpx; border-radius: 12rpx; background-color: #f5f5f5; }
.contact-avatar-placeholder { width: 80rpx; height: 80rpx; border-radius: 12rpx; background-color: #10aeff; display: flex; align-items: center; justify-content: center; }
.avatar-text { font-size: 36rpx; font-weight: 600; color: #ffffff; }
.contact-info { flex: 1; display: flex; flex-direction: column; }
.contact-name { font-size: 32rpx; color: #111; }
.contact-id { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.chat-action { padding: 12rpx 24rpx; background-color: #07c160; border-radius: 12rpx; }
.chat-action-text { font-size: 24rpx; color: #ffffff; }
.add-action { padding: 12rpx 24rpx; background-color: #10aeff; border-radius: 12rpx; }
.add-action-text { font-size: 24rpx; color: #ffffff; }
.already-friend { padding: 12rpx 24rpx; background-color: #e5e5e5; border-radius: 12rpx; }
.already-friend-text { font-size: 24rpx; color: #999; }
.empty-tip { text-align: center; padding: 100rpx 40rpx; font-size: 28rpx; color: #999; }

.search-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4); z-index: 100; display: flex; align-items: flex-start; justify-content: center; }
.search-panel { width: 90%; max-height: 80vh; background: #fff; border-radius: 16rpx; margin-top: 100rpx; overflow: hidden; }
.search-input-wrap { display: flex; align-items: center; padding: 20rpx; border-bottom: 1rpx solid #e5e5e5; }
.search-input { flex: 1; height: 64rpx; background: #f5f5f5; border-radius: 12rpx; padding: 0 20rpx; font-size: 28rpx; }
.search-cancel { margin-left: 16rpx; font-size: 28rpx; color: #10aeff; }
.search-results { max-height: 60vh; }
.search-empty { text-align: center; padding: 60rpx; font-size: 28rpx; color: #999; }

.add-friend-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4); z-index: 200; display: flex; align-items: center; justify-content: center; }
.add-friend-panel { width: 80%; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.add-friend-title { font-size: 34rpx; font-weight: 600; text-align: center; margin-bottom: 32rpx; }
.add-friend-user { display: flex; align-items: center; margin-bottom: 24rpx; }
.add-friend-msg { width: 100%; height: 72rpx; background: #f5f5f5; border-radius: 12rpx; padding: 0 20rpx; font-size: 28rpx; margin-bottom: 32rpx; }
.add-friend-actions { display: flex; gap: 24rpx; }
.add-friend-cancel { flex: 1; height: 80rpx; display: flex; align-items: center; justify-content: center; border-radius: 12rpx; background: #f5f5f5; font-size: 30rpx; color: #666; }
.add-friend-confirm { flex: 1; height: 80rpx; display: flex; align-items: center; justify-content: center; border-radius: 12rpx; background: #07c160; font-size: 30rpx; color: #fff; }
</style>
