/**
 * 安全区域工具 - 处理状态栏和底部安全区域
 */

let statusBarHeight = 0;
let safeAreaBottom = 0;

export function initSafeArea() {
  try {
    const systemInfo = uni.getSystemInfoSync();
    statusBarHeight = systemInfo.statusBarHeight || 0;
    
    // 计算底部安全区域
    if (systemInfo.safeAreaInsets) {
      safeAreaBottom = systemInfo.safeAreaInsets.bottom || 0;
    } else if (systemInfo.safeArea) {
      safeAreaBottom = systemInfo.screenHeight - systemInfo.safeArea.bottom;
    }
  } catch (e) {
    console.error("[SafeArea] init failed:", e);
  }
}

export function getStatusBarHeight() {
  if (!statusBarHeight) {
    initSafeArea();
  }
  return statusBarHeight;
}

export function getSafeAreaBottom() {
  if (!safeAreaBottom) {
    initSafeArea();
  }
  return safeAreaBottom;
}

export function getNavBarHeight() {
  // 导航栏高度 = 状态栏高度 + 44px（标准导航栏高度）
  return getStatusBarHeight() + 44;
}
