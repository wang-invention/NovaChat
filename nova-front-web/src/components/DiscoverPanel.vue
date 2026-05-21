<template>
  <div>
    <div class="discover-item" @click="$emit('open-moments')">
      <div class="discover-icon-wrap" style="background:linear-gradient(135deg,#FF6B6B,#FFD93D)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
      </div>
      <span class="discover-name">朋友圈</span>
      <span class="discover-arrow"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg></span>
    </div>
    <div class="discover-item">
      <div class="discover-icon-wrap" style="background:linear-gradient(135deg,#10AEFF,#6C5CE7)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
      </div>
      <span class="discover-name">视频号</span>
      <span class="discover-arrow"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg></span>
    </div>
    <div class="discover-item">
      <div class="discover-icon-wrap" style="background:linear-gradient(135deg,#00B894,#07C160)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
      </div>
      <span class="discover-name">看一看</span>
      <span class="discover-arrow"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg></span>
    </div>
    <div class="discover-item">
      <div class="discover-icon-wrap" style="background:linear-gradient(135deg,#6C5CE7,#A29BFE)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="5"/><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/></svg>
      </div>
      <span class="discover-name">小程序</span>
      <span class="discover-arrow"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg></span>
    </div>
  </div>
</template>

<script setup>
defineEmits(['open-moments'])
</script>