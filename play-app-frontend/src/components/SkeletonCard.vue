<template>
  <view class="skeleton-card" :class="variant" :style="{ '--count': count }">
    <!-- 封面图骨架 -->
    <view class="sk-cover" v-if="variant === 'companion' || variant === 'gallery'">
      <view class="sk-shimmer"></view>
    </view>

    <!-- 内容区骨架 -->
    <view class="sk-body">
      <!-- 标题行 -->
      <view class="sk-line wide" v-if="variant !== 'compact'"></view>
      <!-- 副标题 -->
      <view class="sk-line" :class="variant === 'compact' ? 'medium' : 'medium'"></view>
      <!-- 底部行 -->
      <view class="sk-line short" v-if="variant !== 'compact'"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    /** 骨架变体: companion(陪玩卡片) | order(订单) | compact(紧凑列表) | gallery(相册) */
    variant?: 'companion' | 'order' | 'compact' | 'gallery'
    /** 同时渲染几个骨架（仅用于样式占位） */
    count?: number
  }>(),
  { variant: 'companion', count: 1 }
)
</script>

<style lang="scss" scoped>
.skeleton-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 24rpx;
  border: 1px solid var(--border-color);
}

/* ── 变体：陪玩卡片 ── */
.companion {
  .sk-cover {
    height: 320rpx;
    background: rgba(var(--color-primary-rgb), 0.04);
    position: relative;
    overflow: hidden;
  }
  .sk-body { padding: 16rpx; }
}

/* ── 变体：订单 ── */
.order {
  padding: 24rpx;
  .sk-line.wide { width: 45%; margin-bottom: 16rpx; }
  .sk-line.medium { width: 70%; margin-bottom: 16rpx; }
  .sk-line.short { width: 30%; }
}

/* ── 变体：紧凑列表 ── */
.compact {
  display: flex;
  align-items: center;
  padding: 20rpx;
  gap: 16rpx;

  &::before {
    content: '';
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: rgba(var(--color-primary-rgb), 0.04);
    flex-shrink: 0;
  }
  .sk-body { flex: 1; }
  .sk-line { height: 20rpx; }
}

/* ── 变体：相册 ── */
.gallery {
  .sk-cover { height: 400rpx; }
  .sk-body { padding: 16rpx; }
  .sk-line { height: 24rpx; }
}

/* ── 骨架线 ── */
.sk-line {
  height: 22rpx;
  background: linear-gradient(
    90deg,
    rgba(var(--color-primary-rgb), 0.05),
    rgba(var(--color-primary-rgb), 0.1),
    rgba(var(--color-primary-rgb), 0.05)
  );
  background-size: 200% 100%;
  border-radius: var(--radius-full);
  animation: shimmer 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  margin-bottom: 14rpx;

  &.wide { width: 75%; }
  &.medium { width: 55%; }
  &.short { width: 38%; }
  &:last-child { margin-bottom: 0; }
}

.sk-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(var(--color-primary-rgb), 0.04),
    transparent
  );
  background-size: 200% 100%;
  animation: shimmer 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 尊重 reduced-motion */
@media (prefers-reduced-motion: reduce) {
  .sk-line, .sk-shimmer {
    animation: none;
    background: rgba(var(--color-primary-rgb), 0.06);
  }
}
</style>
