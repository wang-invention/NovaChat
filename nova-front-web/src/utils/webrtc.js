let pc = null
let localStream = null
let remoteStream = null

export function createPeerConnection() {
  pc = new RTCPeerConnection({
    iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
  })
  return pc
}

export async function getLocalStream() {
  localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false })
  return localStream
}

export function addLocalTracks() {
  if (localStream && pc) {
    localStream.getTracks().forEach(t => pc.addTrack(t, localStream))
  }
}

export function getPeerConnection() { return pc }
export function getLocalStreamRef() { return localStream }
export function getRemoteStream() { return remoteStream }

export function setRemoteStream(stream) { remoteStream = stream }

export function closeCall() {
  if (localStream) {
    localStream.getTracks().forEach(t => t.stop())
    localStream = null
  }
  if (pc) {
    pc.close()
    pc = null
  }
  remoteStream = null
}

export function toggleMute() {
  if (localStream) {
    const enabled = !localStream.getAudioTracks()[0]?.enabled
    localStream.getAudioTracks().forEach(t => { t.enabled = enabled })
    return !enabled
  }
  return false
}