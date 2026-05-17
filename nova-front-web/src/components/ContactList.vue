<template>
  <div>
    <div v-if="requests.length > 0">
      <div class="contact-section-title">新的朋友</div>
      <div v-for="req in requests" :key="req.id" class="request-item">
        <img v-if="req.avatar || req.senderAvatar" :src="req.avatar || req.senderAvatar" class="request-avatar" alt="">
        <div v-else class="request-avatar-placeholder">{{ (req.senderName || '?')[0] }}</div>
        <div class="request-info">
          <div class="request-name">{{ req.senderName || req.senderNickname || '未知用户' }}</div>
          <div class="request-msg">{{ req.message || '请求添加您为好友' }}</div>
        </div>
        <div class="request-actions">
          <template v-if="req.status === 'pending'">
            <button class="btn-accept" @click="$emit('accept', req.id)">接受</button>
            <button class="btn-reject" @click="$emit('reject', req.id)">拒绝</button>
          </template>
          <span v-else class="request-status">{{ req.status === 'accepted' ? '已添加' : '已拒绝' }}</span>
        </div>
      </div>
    </div>

    <div class="contact-section-title">好友列表</div>
    <div v-if="friends.length === 0" style="text-align:center;padding:40px;color:var(--text-muted);font-size:14px;">
      暂无好友
    </div>
    <div v-for="friend in friends" :key="friend.id || friend.userId" class="contact-item" @click="$emit('chat', friend)">
      <img v-if="friend.avatar" :src="friend.avatar" class="contact-avatar" alt="">
      <div v-else class="contact-avatar-placeholder">{{ (friend.nickname || friend.username || '?')[0] }}</div>
      <div class="contact-info">
        <div class="contact-name">{{ friend.nickname || friend.username }}</div>
        <div class="contact-sub">{{ friend.signature || friend.bio || '' }}</div>
      </div>
      <span class="contact-action">发消息</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  friends: { type: Array, default: () => [] },
  requests: { type: Array, default: () => [] }
})

defineEmits(['chat', 'accept', 'reject'])
</script>