<!--
  CnIcon - ChatNova 统一 SVG 图标组件
  - 跨端（H5 / 微信小程序 / App）：通过 data:image/svg+xml 传给 <image>
  - 支持色值继承（color 属性）
  - 图标 stroke: 1.75，圆角线端（与微信/iOS HIG 风格一致）

  用法：
    <cn-icon name="phone" :size="44" color="#111" />
-->
<template>
  <view
    class="cn-icon"
    :style="{ width: sizeRpx, height: sizeRpx }"
    aria-hidden="true"
  >
    <image class="cn-icon__img" :src="src" mode="aspectFit" />
  </view>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 40 }, // rpx
  color: { type: String, default: "#111111" },
  strokeWidth: { type: [Number, String], default: 1.75 },
});

const sizeRpx = computed(() =>
  typeof props.size === "number" ? `${props.size}rpx` : props.size
);

/**
 * 图标 path 字典（24x24 viewport，stroke 模式）
 * 新增图标：在此处追加即可。
 */
const PATHS = {
  phone:
    '<path d="M6 4h5l2 5-3 2a12 12 0 006 6l2-3 5 2v5a2 2 0 01-2 2A17 17 0 014 6a2 2 0 012-2z"/>',
  lock:
    '<rect x="4" y="10" width="16" height="11" rx="2.5"/><path d="M8 10V7a4 4 0 018 0v3"/>',
  eye:
    '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3.2"/>',
  "eye-off":
    '<path d="M3 3l18 18"/><path d="M10.6 6.2A10.6 10.6 0 0112 6c6.5 0 10 7 10 7a17.4 17.4 0 01-3.6 4.3"/><path d="M6.2 6.6A17.6 17.6 0 002 13s3.5 7 10 7a10.7 10.7 0 005.4-1.5"/><path d="M9.9 9.9a3 3 0 104.2 4.2"/>',
  message:
    '<path d="M4 5h16a1 1 0 011 1v11a1 1 0 01-1 1H9l-5 4V6a1 1 0 011-1z"/>',
  user:
    '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0116 0"/>',
  check:
    '<path d="M5 12l4 4 10-10"/>',
  close:
    '<path d="M6 6l12 12M6 18L18 6"/>',
  back:
    '<path d="M15 6l-6 6 6 6"/>',
  "arrow-right":
    '<path d="M9 6l6 6-6 6"/>',
  shield:
    '<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6l8-3z"/>',
  key:
    '<circle cx="8" cy="15" r="3.5"/><path d="M11 14l10-10m-3 3l3 3m-7 1l3 3"/>',
};

const toDataUri = (inner, color, strokeWidth) => {
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round">${inner}</svg>`;
  return "data:image/svg+xml;utf8," + encodeURIComponent(svg);
};

const src = computed(() => {
  const inner = PATHS[props.name] || "";
  return toDataUri(inner, props.color, props.strokeWidth);
});
</script>

<style scoped lang="scss">
.cn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &__img {
    width: 100%;
    height: 100%;
  }
}
</style>
