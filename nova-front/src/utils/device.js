/**
 * 设备信息工具函数
 * 用于多端登录管理
 */

const DEVICE_ID_KEY = 'device_id';
const DEVICE_TYPE_KEY = 'device_type';

/**
 * 生成唯一设备ID
 * 格式：时间戳-随机数
 */
function generateDeviceId() {
  const timestamp = Date.now().toString(36);
  const random = Math.random().toString(36).substring(2, 8);
  return `${timestamp}-${random}`;
}

/**
 * 获取或创建设备ID
 * 优先从本地存储获取，不存在则生成并保存
 */
export function getDeviceId() {
  try {
    let deviceId = uni.getStorageSync(DEVICE_ID_KEY);
    if (!deviceId) {
      deviceId = generateDeviceId();
      uni.setStorageSync(DEVICE_ID_KEY, deviceId);
    }
    return deviceId;
  } catch (e) {
    // 存储失败时返回临时ID
    return generateDeviceId();
  }
}

/**
 * 获取设备类型
 * 根据运行平台返回设备类型标识
 */
export function getDeviceType() {
  try {
    // 检查是否已有存储的设备类型
    let deviceType = uni.getStorageSync(DEVICE_TYPE_KEY);
    if (deviceType) {
      return deviceType;
    }

    // 根据平台判断设备类型
    // #ifdef H5
    deviceType = 'Web';
    // #endif

    // #ifdef MP-WEIXIN
    deviceType = 'WeChat';
    // #endif

    // #ifdef APP-PLUS
    const systemInfo = uni.getSystemInfoSync();
    const platform = systemInfo.platform;
    if (platform === 'ios') {
      deviceType = 'iOS';
    } else if (platform === 'android') {
      deviceType = 'Android';
    } else {
      deviceType = 'App';
    }
    // #endif

    // 默认类型
    if (!deviceType) {
      deviceType = 'Unknown';
    }

    uni.setStorageSync(DEVICE_TYPE_KEY, deviceType);
    return deviceType;
  } catch (e) {
    return 'Unknown';
  }
}

/**
 * 获取设备详细信息
 * 包含设备型号、系统版本等
 */
export function getDeviceInfo() {
  try {
    const systemInfo = uni.getSystemInfoSync();
    return {
      deviceType: getDeviceType(),
      deviceId: getDeviceId(),
      brand: systemInfo.brand || '',
      model: systemInfo.model || '',
      system: systemInfo.system || '',
      platform: systemInfo.platform || '',
      version: systemInfo.version || '',
      screenWidth: systemInfo.screenWidth || 0,
      screenHeight: systemInfo.screenHeight || 0,
    };
  } catch (e) {
    return {
      deviceType: getDeviceType(),
      deviceId: getDeviceId(),
      brand: '',
      model: '',
      system: '',
      platform: '',
      version: '',
      screenWidth: 0,
      screenHeight: 0,
    };
  }
}

/**
 * 清除设备信息（登出时调用）
 */
export function clearDeviceInfo() {
  try {
    uni.removeStorageSync(DEVICE_TYPE_KEY);
    // 注意：deviceId 通常保留，用于下次登录识别同一设备
  } catch (e) {
    console.error('清除设备信息失败', e);
  }
}
