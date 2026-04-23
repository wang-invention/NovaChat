<script setup>
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";

onLaunch(() => {
  console.log("[ChatNova] App Launch");
});

onShow(() => {
  console.log("[ChatNova] App Show");
});

onHide(() => {
  console.log("[ChatNova] App Hide");
});
</script>

<style lang="scss">
/* ===== ChatNova 全局样式 ===== */
page {
  background-color: #f7f8fa;
  color: #111111;
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  font-size: 28rpx;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -webkit-text-size-adjust: 100%;
}

view,
text,
input,
textarea,
button,
scroll-view {
  box-sizing: border-box;
}
</style>
