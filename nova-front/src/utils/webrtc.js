/**
 * WebRTC 语音通话工具
 * 负责：麦克风采集、P2P连接、音频播放
 * 修复：uni-app Android/iOS 兼容，避免 mediaDevices 未定义报错
 */

const STUN_SERVERS = {
  iceServers: [
    { urls: "stun:stun.l.google.com:19302" },
    { urls: "stun:stun1.l.google.com:19302" },
  ],
};

export async function requestMicrophonePermission() {
  // 正确判断 uni-app 平台
  let platform = 'h5';
  let isApp = false;

  try {
    if (typeof uni !== 'undefined' && uni.getSystemInfoSync) {
      const sys = uni.getSystemInfoSync();
      platform = sys.platform || 'h5';
      // 安卓 / iOS 都判定为 App 环境
      isApp = platform === 'android' || platform === 'ios';
    }
  } catch (e) { }

  console.warn('[WebRTC] platform:', platform, 'uni exists:', typeof uni !== 'undefined');
  const isH5 = !isApp;
  console.warn('[WebRTC] isH5:', isH5);

  // ================== App 端逻辑（安卓/iOS）==================
  if (isApp) {
    try {
      const settingInfo = await new Promise((resolve) => {
        uni.getSetting({
          success: resolve,
          fail: () => resolve({ authSetting: {} }),
        });
      });
      const micAuth = settingInfo.authSetting?.['scope.record'];

      if (micAuth === false) {
        const confirm = await new Promise((resolve) => {
          uni.showModal({
            title: '麦克风权限',
            content: '语音通话需要麦克风权限，请在设置中开启',
            confirmText: '去设置',
            success: resolve,
          });
        });
        if (confirm.confirm) {
          await new Promise(r => uni.openSetting({ complete: r }));
        }
        return false;
      }

      if (micAuth === undefined && uni.authorize) {
        try {
          await new Promise((resolve, reject) => {
            uni.authorize({
              scope: 'scope.record',
              success: resolve,
              fail: reject,
            });
          });
          return true;
        } catch (e) {
          return false;
        }
      }
      return micAuth === true;
    } catch (e) {
      console.warn('[WebRTC] App 权限异常:', e);
      return true;
    }
  }

  // ================== H5 浏览器逻辑 ==================
  if (isH5) {
    try {
      if (!navigator || !navigator.mediaDevices) {
        console.warn('[WebRTC] H5 不支持 mediaDevices');
        return false;
      }
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
      stream.getTracks().forEach(t => t.stop());
      return true;
    } catch (e) {
      console.warn('[WebRTC] H5 麦克风错误:', e);
      return false;
    }
  }

  return true;
}

let peerConnection = null;
let localStream = null;
let remoteAudio = null;
let onRemoteStreamCallback = null;
let onIceCandidateCallback = null;
let onConnectionStateCallback = null;

export function createPeerConnection({ onRemoteStream, onIceCandidate, onConnectionState }) {
  onRemoteStreamCallback = onRemoteStream;
  onIceCandidateCallback = onIceCandidate;
  onConnectionStateCallback = onConnectionState;

  peerConnection = new RTCPeerConnection(STUN_SERVERS);

  peerConnection.onicecandidate = (event) => {
    if (event.candidate && onIceCandidateCallback) onIceCandidateCallback(event.candidate);
  };

  peerConnection.ontrack = (event) => {
    if (event.streams && event.streams[0] && onRemoteStreamCallback) {
      onRemoteStreamCallback(event.streams[0]);
    }
  };

  peerConnection.onconnectionstatechange = () => {
    if (onConnectionStateCallback) onConnectionStateCallback(peerConnection.connectionState);
  };

  return peerConnection;
}

// ================== 【修复核心】startLocalStream 安全判断 ==================
export async function startLocalStream() {
  try {
    // 安全判断：App 端直接抛出友好提示，不执行 H5 API
    const isApp = typeof uni !== 'undefined' && (uni.getSystemInfoSync().platform === 'android' || uni.getSystemInfoSync().platform === 'ios');
    if (isApp) {
      console.warn("[WebRTC] App 端不支持 H5 WebRTC，已跳过媒体流采集");
      throw new Error("App 端请使用原生语音通话插件");
    }

    // H5 才执行
    if (!navigator || !navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error("当前环境不支持麦克风访问");
    }

    localStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true,
      },
      video: false,
    });

    if (peerConnection) {
      localStream.getTracks().forEach(track => {
        peerConnection.addTrack(track, localStream);
      });
    }

    return localStream;
  } catch (err) {
    console.error("[WebRTC] 麦克风错误:", err);
    throw err;
  }
}

export async function createOffer() {
  if (!peerConnection) return null;
  const offer = await peerConnection.createOffer();
  await peerConnection.setLocalDescription(offer);
  return offer;
}

export async function createAnswer() {
  if (!peerConnection) return null;
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);
  return answer;
}

export async function setRemoteSdp(sdp) {
  if (!peerConnection) return;
  await peerConnection.setRemoteDescription(new RTCSessionDescription(sdp));
}

export async function addIceCandidate(candidate) {
  if (!peerConnection) return;
  try {
    await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
  } catch (e) { }
}

export function playRemoteStream(stream) {
  if (!remoteAudio) {
    remoteAudio = new Audio();
    remoteAudio.autoplay = true;
    document.body.appendChild(remoteAudio);
  }
  remoteAudio.srcObject = stream;
}

export function stopRemoteStream() {
  if (remoteAudio) {
    remoteAudio.srcObject = null;
    remoteAudio.remove();
    remoteAudio = null;
  }
}

export function toggleMute(muted) {
  if (localStream) {
    localStream.getAudioTracks().forEach(track => {
      track.enabled = !muted;
    });
  }
}

export function toggleSpeaker(enabled) {
  if (remoteAudio) remoteAudio.muted = !enabled;
}

export function hangup() {
  if (localStream) {
    localStream.getTracks().forEach(track => track.stop());
    localStream = null;
  }
  stopRemoteStream();
  if (peerConnection) {
    peerConnection.close();
    peerConnection = null;
  }
}

export function getConnectionState() {
  return peerConnection ? peerConnection.connectionState : "closed";
}