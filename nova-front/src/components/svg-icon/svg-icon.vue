<template>
  <view class="svg-icon" :style="iconStyle">
    <image class="svg-icon__img" :src="svgSrc" mode="aspectFit" />
  </view>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  icon: { type: String, default: "" },
  size: { type: [Number, String], default: 40 },
  width: { type: [Number, String], default: "" },
  height: { type: [Number, String], default: "" },
  color: { type: String, default: "" },
});

const sizeRpx = (val) => {
  if (!val) return "";
  const num = parseFloat(val);
  return isNaN(num) ? val : `${num}rpx`;
};

const iconStyle = computed(() => {
  const w = props.width ? sizeRpx(props.width) : sizeRpx(props.size);
  const h = props.height ? sizeRpx(props.height) : sizeRpx(props.size);
  return { width: w, height: h };
});

const svgSrc = computed(() => {
  if (!props.icon) return "";
  let svgContent = props.icon;
  // 如果传入的不是完整 svg 标签，包装一下
  if (!svgContent.trim().startsWith("<svg")) {
    const colorAttr = props.color ? ` stroke="${props.color}"` : "";
    svgContent = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"${colorAttr} stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${svgContent}</svg>`;
  } else {
    // 替换颜色
    if (props.color) {
      svgContent = svgContent.replace(/stroke="currentColor"/g, `stroke="${props.color}"`);
      svgContent = svgContent.replace(/fill="currentColor"/g, `fill="${props.color}"`);
    }
    // 确保有 xmlns
    if (!svgContent.includes("xmlns=")) {
      svgContent = svgContent.replace("<svg", '<svg xmlns="http://www.w3.org/2000/svg"');
    }
  }
  return "data:image/svg+xml;utf8," + encodeURIComponent(svgContent);
});
</script>

<style scoped>
.svg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.svg-icon__img {
  width: 100%;
  height: 100%;
}
</style>
