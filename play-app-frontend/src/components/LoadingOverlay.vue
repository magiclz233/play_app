<template>
  <view class="loading-overlay" :class="{ 'is-fullscreen': fullscreen }" v-if="visible">
    <view class="loading-spinner">
      <!-- 品牌色旋转指示器 SVG -->
      <svg viewBox="0 0 40 40" class="spinner-svg">
        <circle cx="20" cy="20" r="16" fill="none" stroke="var(--border-color)" stroke-width="3"/>
        <circle
          cx="20" cy="20" r="16" fill="none"
          stroke="url(#brandGradient)" stroke-width="3"
          stroke-dasharray="75" stroke-dashoffset="60"
          stroke-linecap="round"
        />
        <defs>
          <linearGradient id="brandGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="var(--color-primary)"/>
            <stop offset="100%" stop-color="var(--color-gradient-end)"/>
          </linearGradient>
        </defs>
      </svg>
    </view>
    <text class="loading-text" v-if="text">{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    visible?: boolean
    text?: string
    fullscreen?: boolean
  }>(),
  { visible: true, text: '加载中...', fullscreen: false }
)
</script>

<style lang="scss" scoped>
.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;

  &.is-fullscreen {
    position: fixed;
    inset: 0;
    background: var(--bg-main);
    z-index: $z-index-modal-backdrop;
  }
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
}

.spinner-svg {
  width: 100%;
  height: 100%;
  animation: spin 0.9s linear infinite;
}

.loading-text {
  margin-top: 20rpx;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  line-height: 1.5;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (prefers-reduced-motion: reduce) {
  .spinner-svg {
    animation: none;
    opacity: 0.6;
  }
}
</style>
