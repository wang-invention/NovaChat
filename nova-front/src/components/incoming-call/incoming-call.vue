<template>
  <view class="incoming-call-overlay" v-if="visible" @click.stop>
    <view class="incoming-call-card">
      <view class="caller-info">
        <image v-if="callerAvatar" :src="callerAvatar" class="caller-avatar" mode="aspectFill" />
        <view v-else class="caller-avatar-placeholder">
          <text class="avatar-text">{{ callerName.charAt(0) }}</text>
        </view>
        <text class="caller-name">{{ callerName }}</text>
        <text class="caller-hint">邀请你语音通话</text>
      </view>

      <view class="incoming-actions">
        <view class="incoming-btn reject" @click="handleReject">
          <view class="btn-circle reject-circle">
            <svg-icon :icon="hangupIcon" size="44" />
          </view>
          <text class="btn-label">拒绝</text>
        </view>

        <view class="incoming-btn accept" @click="handleAccept">
          <view class="btn-circle accept-circle">
            <svg-icon :icon="acceptIcon" size="44" />
          </view>
          <text class="btn-label">接听</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { sendWSMessage } from "@/utils/websocket";

const props = defineProps({
  callId: { type: Number, default: null },
  callerId: { type: Number, default: null },
  callerName: { type: String, default: "" },
  callerAvatar: { type: String, default: "" },
});

const emit = defineEmits(["accept", "reject", "close"]);

const visible = ref(true);

const hangupIcon = "<path d='M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.42 19.42 0 0 1-3.33-2.67m-2.67-3.34a19.79 19.79 0 0 1-3.07-8.63A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><path d='M23 1L1 23' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>";
const acceptIcon = "<path d='M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z' fill='currentColor'/>";

function handleAccept() {
  visible.value = false;
  sendWSMessage({
    type: "accept",
    to: props.callerId,
    callId: props.callId,
  });
  emit("accept", {
    callId: props.callId,
    callerId: props.callerId,
    callerName: props.callerName,
    callerAvatar: props.callerAvatar,
  });
}

function handleReject() {
  visible.value = false;
  sendWSMessage({
    type: "reject",
    to: props.callerId,
    callId: props.callId,
  });
  emit("reject", { callId: props.callId });
  emit("close");
}
</script>

<style scoped>
.incoming-call-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.incoming-call-card {
  width: 560rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 60rpx 40rpx 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.caller-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 50rpx;
}

.caller-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  margin-bottom: 24rpx;
}

.caller-avatar-placeholder {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.avatar-text {
  font-size: 64rpx;
  color: #999;
  font-weight: 600;
}

.caller-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.caller-hint {
  font-size: 28rpx;
  color: #999;
}

.incoming-actions {
  display: flex;
  gap: 80rpx;
}

.incoming-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.btn-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reject-circle {
  background: #ff3b30;
  color: #fff;
}

.accept-circle {
  background: #07c160;
  color: #fff;
}

.btn-label {
  font-size: 26rpx;
  color: #666;
}
</style>