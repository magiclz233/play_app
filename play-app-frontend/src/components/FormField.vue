<template>
  <view class="form-field" :class="{ 'has-error': !!error }">
    <text class="field-label" v-if="label">{{ label }}</text>
    <slot />
    <view class="field-error" v-if="error" role="alert">
      <svg viewBox="0 0 16 16" class="error-icon-svg">
        <circle cx="8" cy="8" r="7" fill="none" stroke="var(--color-error)" stroke-width="1.5"/>
        <line x1="8" y1="4.5" x2="8" y2="9" stroke="var(--color-error)" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="8" cy="11.5" r="0.8" fill="var(--color-error)"/>
      </svg>
      <text class="error-msg">{{ error }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  label?: string
  error?: string
}>()
</script>

<style lang="scss" scoped>
.form-field {
  margin-bottom: 24rpx;
}

.field-label {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: 10rpx;
  line-height: 1.5;
}

.has-error :deep(input),
.has-error :deep(textarea) {
  border-color: var(--color-error) !important;
  background-color: rgba(239, 68, 68, 0.04) !important;
}

.field-error {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
  padding: 8rpx 0;
}

.error-icon-svg {
  width: 24rpx;
  height: 24rpx;
  margin-right: 8rpx;
  flex-shrink: 0;
}

.error-msg {
  font-size: var(--font-size-xs);
  color: var(--color-error);
  line-height: 1.5;
}
</style>
