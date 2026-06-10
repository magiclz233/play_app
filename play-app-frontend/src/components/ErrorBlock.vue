<template>
  <view class="error-block" role="alert">
    <!-- SVG 错误图标 -->
    <svg viewBox="0 0 24 24" class="error-icon">
      <circle cx="12" cy="12" r="10" fill="none" stroke="var(--color-error)" stroke-width="2" opacity="0.8"/>
      <line x1="12" y1="7" x2="12" y2="13" stroke="var(--color-error)" stroke-width="2" stroke-linecap="round"/>
      <circle cx="12" cy="16.5" r="1.2" fill="var(--color-error)"/>
    </svg>

    <text class="error-text">{{ message }}</text>

    <view class="error-actions" v-if="retryText || helpText">
      <button
        v-if="retryText"
        class="retry-btn"
        hover-class="button-hover"
        @click="$emit('retry')"
      >
        {{ retryText }}
      </button>
      <text
        v-if="helpText"
        class="help-link"
        @click="$emit('help')"
      >
        {{ helpText }}
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    message?: string
    retryText?: string
    helpText?: string
  }>(),
  {
    message: '加载失败，请稍后重试',
    retryText: '重试',
    helpText: '',
  }
)

defineEmits<{
  retry: []
  help: []
}>()
</script>

<style lang="scss" scoped>
.error-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 48rpx;
  min-height: 300rpx;
}

.error-icon {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 24rpx;
}

.error-text {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.6;
  max-width: 500rpx;
}

.error-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 32rpx;
  gap: 20rpx;
}

.retry-btn {
  padding: 16rpx 44rpx;
  background: linear-gradient(135deg, var(--color-primary), var(--color-gradient-end));
  color: #fff;
  border-radius: var(--radius-full);
  border: none;
  font-size: var(--font-size-sm);
  font-weight: 600;
  box-shadow: var(--shadow-floating);
  line-height: 1.5;
}

.help-link {
  font-size: var(--font-size-sm);
  color: var(--color-info);
  text-decoration: underline;
  padding: 8rpx 16rpx;
}

.button-hover {
  opacity: 0.85;
  transform: scale(0.97);
}
</style>
