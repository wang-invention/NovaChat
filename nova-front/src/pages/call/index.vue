<template>
  <view class="call-page">
    <view class="call-bg"></view>

    <view class="call-content">
      <view class="peer-avatar">
        <image v-if="peerAvatar" :src="peerAvatar" class="avatar-img" mode="aspectFill" />
        <view v-else class="avatar-placeholder">
          <text class="avatar-text">{{ peerName.charAt(0) }}</text>
        </view>
      </view>

      <text class="peer-name">{{ peerName }}</text>

      <text class="call-status">{{ statusText }}</text>

      <text class="call-duration" v-if="callState === 'ongoing'">{{ formatDuration(callDuration) }}</text>
    </view>

    <view class="call-actions">
      <view class="action-item" v-if="callState === 'ongoing'" @click="toggleMute">
        <view class="action-btn" :class="{ active: isMuted }">
          <svg-icon :icon="muteIcon" size="48" />
        </view>
        <text class="action-label">{{ isMuted ? '已静音' : '静音' }}</text>
      </view>

      <view class="action-item" v-if="callState === 'ongoing'" @click="toggleSpeaker">
        <view class="action-btn" :class="{ active: isSpeakerOn }">
          <svg-icon :icon="speakerIcon" size="48" />
        </view>
        <text class="action-label">{{ isSpeakerOn ? '免提中' : '免提' }}</text>
      </view>

      <view class="action-item" @click="doHangup">
        <view class="action-btn hangup-btn" :class="{ 'hangup-only': callState === 'ringing' }">
          <svg-icon :icon="hangupIcon" size="48" />
        </view>
        <text class="action-label">挂断</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import {
  createPeerConnection, startLocalStream, createOffer, createAnswer,
  setRemoteSdp, addIceCandidate, playRemoteStream, stopRemoteStream,
  toggleMute as rtcToggleMute, toggleSpeaker as rtcToggleSpeaker, hangup as rtcHangup,
  getConnectionState, requestMicrophonePermission,
} from "@/utils/webrtc";
import { sendWSMessage, onWSMessage } from "@/utils/websocket";

const callId = ref(null);
const role = ref("caller");
const peerId = ref(null);
const peerName = ref("");
const peerAvatar = ref("");
const callState = ref("ringing");
const callDuration = ref(0);
const isMuted = ref(false);
const isSpeakerOn = ref(false);
let durationTimer = null;
let removeWSHandler = null;
let pendingRemoteSdp = null;

const statusText = computed(() => {
  switch (callState.value) {
    case "ringing": return "等待对方接听...";
    case "incoming": return "邀请你语音通话";
    case "ongoing": return "通话中";
    case "ended": return "通话已结束";
    case "rejected": return "对方拒绝接听";
    case "busy": return "对方正忙";
    case "missed": return "无人接听";
    default: return "";
  }
});

const muteIcon = "<path d='M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z' fill='currentColor'/><path d='M19 10v2a7 7 0 0 1-14 0v-2' stroke='currentColor' stroke-width='2' stroke-linecap='round'/><path d='M12 19v4M8 23h8' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>";
const speakerIcon = "<path d='M11 5L6 9H2v6h4l5 4V5z' fill='currentColor'/><path d='M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>";
const hangupIcon = "<path d='M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.42 19.42 0 0 1-3.33-2.67m-2.67-3.34a19.79 19.79 0 0 1-3.07-8.63A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><path d='M23 1L1 23' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>";

onLoad((options) => {
  callId.value = options.callId ? Number(options.callId) : null;
  role.value = options.role || "caller";
  peerId.value = options.peerId ? Number(options.peerId) : null;
  peerName.value = decodeURIComponent(options.peerName || "对方");
  peerAvatar.value = decodeURIComponent(options.peerAvatar || "");

  if (role.value === "caller") {
    callState.value = "ringing";
    startCall();
  } else {
    callState.value = "ongoing";
    startDurationTimer();
  }

  removeWSHandler = onWSMessage(handleWSMessage);
});

onUnmounted(() => {
  cleanup();
});

function handleWSMessage(data) {
  const type = data.type;
  const payload = data.data || {};

  if (payload.callId !== callId.value) return;

  switch (type) {
    case "call_accepted":
      callState.value = "ongoing";
      startDurationTimer();
      break;
    case "call_rejected":
      callState.value = "rejected";
      setTimeout(() => closePage(), 2000);
      break;
    case "call_hangup":
      callState.value = "ended";
      stopDurationTimer();
      setTimeout(() => closePage(), 2000);
      break;
    case "call_ended":
      callState.value = "ended";
      stopDurationTimer();
      setTimeout(() => closePage(), 2000);
      break;
    case "call_busy":
      callState.value = "busy";
      setTimeout(() => closePage(), 2000);
      break;
    case "call_sdp":
      handleRemoteSdp(payload.sdp);
      break;
    case "call_ice":
      handleRemoteIce(payload);
      break;
  }
}

async function startCall() {
  try {
    const hasPermission = await requestMicrophonePermission();
    if (!hasPermission) {
      uni.showToast({ title: "麦克风权限未开启", icon: "none" });
      doHangup();
      return;
    }

    createPeerConnection({
      onRemoteStream: (stream) => playRemoteStream(stream),
      onIceCandidate: (candidate) => {
        sendWSMessage({
          type: "ice",
          to: peerId.value,
          callId: callId.value,
          candidate: candidate.candidate,
          sdpMid: candidate.sdpMid,
          sdpMLineIndex: candidate.sdpMLineIndex,
        });
      },
      onConnectionState: (state) => {
        if (state === "failed" || state === "disconnected") {
          callState.value = "ended";
          stopDurationTimer();
        }
      },
    });

    await startLocalStream();
    const offer = await createOffer();
    sendWSMessage({
      type: "sdp",
      to: peerId.value,
      callId: callId.value,
      sdp: offer,
    });
  } catch (e) {
    console.error("[Call] startCall error:", e);
    uni.showToast({ title: "麦克风权限未开启", icon: "none" });
    doHangup();
  }
}

async function createAndSendAnswer() {
  try {
    if (!peerConnection) {
      const hasPermission = await requestMicrophonePermission();
      if (!hasPermission) {
        uni.showToast({ title: "麦克风权限未开启", icon: "none" });
        doHangup();
        return;
      }

      createPeerConnection({
        onRemoteStream: (stream) => playRemoteStream(stream),
        onIceCandidate: (candidate) => {
          sendWSMessage({
            type: "ice",
            to: peerId.value,
            callId: callId.value,
            candidate: candidate.candidate,
            sdpMid: candidate.sdpMid,
            sdpMLineIndex: candidate.sdpMLineIndex,
          });
        },
        onConnectionState: (state) => {
          if (state === "failed" || state === "disconnected") {
            callState.value = "ended";
            stopDurationTimer();
          }
        },
      });
      try {
        await startLocalStream();
      } catch (micErr) {
        console.error("[Call] startLocalStream error:", micErr);
        uni.showToast({ title: micErr.message || "麦克风权限未开启", icon: "none" });
        doHangup();
        return;
      }
    }
    if (pendingRemoteSdp) {
      await setRemoteSdp(pendingRemoteSdp);
      pendingRemoteSdp = null;
      const answer = await createAnswer();
      if (answer) {
        sendWSMessage({
          type: "sdp",
          to: peerId.value,
          callId: callId.value,
          sdp: answer,
        });
      }
    }
  } catch (e) {
    console.error("[Call] createAnswer error:", e);
    doHangup();
  }
}

async function handleRemoteSdp(sdp) {
  try {
    if (!peerConnection) {
      pendingRemoteSdp = sdp;
      return;
    }
    await setRemoteSdp(sdp);
    if (sdp.type === "offer" && role.value === "callee") {
      const answer = await createAnswer();
      sendWSMessage({
        type: "sdp",
        to: peerId.value,
        callId: callId.value,
        sdp: answer,
      });
    }
  } catch (e) {
    console.error("[Call] handleRemoteSdp error:", e);
  }
}

async function handleRemoteIce(data) {
  try {
    await addIceCandidate({
      candidate: data.candidate,
      sdpMid: data.sdpMid,
      sdpMLineIndex: data.sdpMLineIndex,
    });
  } catch (e) {
    console.error("[Call] handleRemoteIce error:", e);
  }
}

async function doAccept() {
  callState.value = "ongoing";
  startDurationTimer();
  await createAndSendAnswer();
  sendWSMessage({
    type: "accept",
    to: peerId.value,
    callId: callId.value,
  });
}

function doReject() {
  sendWSMessage({
    type: "reject",
    to: peerId.value,
    callId: callId.value,
  });
  callState.value = "rejected";
  setTimeout(() => closePage(), 1000);
}

function doHangup() {
  sendWSMessage({
    type: "hangup",
    to: peerId.value,
    callId: callId.value,
  });
  stopDurationTimer();
  rtcHangup();
  callState.value = "ended";
  setTimeout(() => closePage(), 1500);
}

function toggleMute() {
  isMuted.value = !isMuted.value;
  rtcToggleMute(isMuted.value);
}

function toggleSpeaker() {
  isSpeakerOn.value = !isSpeakerOn.value;
  rtcToggleSpeaker(isSpeakerOn.value);
}

function startDurationTimer() {
  stopDurationTimer();
  callDuration.value = 0;
  durationTimer = setInterval(() => {
    callDuration.value++;
  }, 1000);
}

function stopDurationTimer() {
  if (durationTimer) {
    clearInterval(durationTimer);
    durationTimer = null;
  }
}

function formatDuration(seconds) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function closePage() {
  cleanup();
  uni.navigateBack({ delta: 1 });
}

function cleanup() {
  stopDurationTimer();
  rtcHangup();
  pendingRemoteSdp = null;
  if (removeWSHandler) {
    removeWSHandler();
    removeWSHandler = null;
  }
}
</script>

<style scoped>
.call-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  z-index: 999;
}

.call-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.call-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.peer-avatar {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 40rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 80rpx;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.peer-name {
  font-size: 40rpx;
  color: #fff;
  font-weight: 500;
  margin-bottom: 20rpx;
}

.call-status {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 20rpx;
}

.call-duration {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 300;
  font-variant-numeric: tabular-nums;
}

.call-actions {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 60rpx;
  padding-bottom: 120rpx;
  padding-top: 40rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.action-btn {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.2s;
}

.action-btn.active {
  background: rgba(255, 255, 255, 0.35);
}

.action-btn.accept-btn {
  background: #07c160;
}

.action-btn.reject-btn {
  background: #ff3b30;
}

.action-btn.hangup-btn {
  background: #ff3b30;
  width: 140rpx;
  height: 140rpx;
}

.action-btn.hangup-only {
  width: 140rpx;
  height: 140rpx;
}

.action-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}
</style>