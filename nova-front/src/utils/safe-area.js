/**
 * 安全区域工具 - 获取状态栏、导航栏、安全区域高度
 */

let statusBarHeight = 0;
let navBarContentHeight = 44; // 导航栏内容标准高度
let safeAreaBottom = 0;

export function initSafeArea() {
  try {
    const systemInfo = uni.getSystemInfoSync();
    statusBarHeight = systemInfo.statusBarHeight || 0;
    safeAreaBottom = systemInfo.safeAreaInsets?.bottom || 0;

    // #ifdef APP-PLUS
    // APP 平台尝试获取更准确的导航栏高度
    if (systemInfo.platform === 'android') {
      // Android 可以通过状态栏高度 + 导航栏内容高度计算
      navBarContentHeight = 44;
    } else if (systemInfo.platform === 'ios') {
      // iOS 根据机型判断
      const model = systemInfo.model || '';
      if (model.includes('iPhone X') || model.includes('iPhone 1') || model.includes('iPhone 2')) {
        navBarContentHeight = 44; // 刘海屏
      } else {
        navBarContentHeight = 44;
      }
    }
    // #endif
  } catch (e) {
    console.error('[SafeArea] init failed:', e);
    statusBarHeight = 20;
    navBarContentHeight = 44;
    safeAreaBottom = 0;
  }
}

export function getStatusBarHeight() {
  if (!statusBarHeight) {
    initSafeArea();
  }
  return statusBarHeight;
}

export function getNavBarContentHeight() {
  if (!statusBarHeight) {
    initSafeArea();
  }
  return navBarContentHeight;
}

export function getTotalNavBarHeight() {
  if (!statusBarHeight) {
    initSafeArea();
  }
  return statusBarHeight + navBarContentHeight;
}

export function getSafeAreaBottom() {
  if (!safeAreaBottom) {
    initSafeArea();
  }
  return safeAreaBottom;
}

export function getNavBarHeight() {
  return getTotalNavBarHeight();
}

// 导出 CSS 变量格式，用于模板直接使用
export function getNavBarStyle() {
  return `margin-top: ${getTotalNavBarHeight()}px;`;
}
