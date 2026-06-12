<template>
  <view class="empty-state" :class="{ 'has-action': !!actionText }">
    <!-- SVG 空态插图图标替代 emoji -->
    <svg viewBox="0 0 120 120" class="empty-illustration">
      <!-- 盒子图标 -->
      <rect x="20" y="45" width="80" height="55" rx="8" fill="none" stroke="var(--text-muted)" stroke-width="2.5" opacity="0.5"/>
      <line x1="20" y1="45" x2="60" y2="25" stroke="var(--text-muted)" stroke-width="2.5" opacity="0.5"/>
      <line x1="100" y1="45" x2="60" y2="25" stroke="var(--text-muted)" stroke-width="2.5" opacity="0.5"/>
      <line x1="20" y1="70" x2="100" y2="70" stroke="var(--text-muted)" stroke-width="2" opacity="0.3"/>
      <!-- 星星装饰 -->
      <path d="M60 15l2 6 6 1-4.5 4 1.5 6-5-3-5 3 1.5-6L52 22l6-1z" fill="var(--color-primary-light)" opacity="0.6"/>
    </svg>

    <text class="empty-text">{{ text }}</text>
    <text class="empty-hint" v-if="hint">{{ hint }}</text>

    <button
      v-if="actionText"
      class="empty-action"
      hover-class="button-hover"
      @click="$emit('action')"
    >
      {{ actionText }}
    </button>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    text?: string
    hint?: string
    actionText?: string
  }>(),
  {
    text: '暂无数据',
    hint: '',
    actionText: '',
  }
)

defineEmits<{
  action: []
}>()
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 48rpx;
  min-height: 360rpx;
}

.empty-illustration {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 32rpx;
  opacity: 0.85;
}

.empty-text {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  font-weight: 500;
  text-align: center;
  line-height: 1.6;
}

.empty-hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: 12rpx;
  text-align: center;
  line-height: 1.5;
}

.empty-action {
  margin-top: 36rpx;
  padding: 20rpx 64rpx;
  background: rgba(var(--color-primary-rgb), 0.08);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  border: 1px solid rgba(var(--color-primary-rgb), 0.2);
  font-size: var(--font-size-sm);
  font-weight: 600;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.1);
  line-height: 1.5;
  transition: all 0.2s ease;
}

.button-hover {
  opacity: 0.85;
  transform: scale(0.97);
  background: rgba(var(--color-primary-rgb), 0.12);
}
</style>
