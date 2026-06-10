<template>
  <view class="empty">
    <text class="icon">{{ icon }}</text>
    <text class="text">{{ text }}</text>
    <button v-if="actionText" class="action" @click="$emit('action')">{{ actionText }}</button>
  </view>
</template>
<script setup lang="ts">
withDefaults(defineProps<{ icon?: string; text?: string; actionText?: string }>(), {
  icon: '📭', text: '暂无数据', actionText: ''
})
defineEmits(['action'])
</script>
<style scoped>
.empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80rpx 0; }
.icon { font-size: 80rpx; margin-bottom: 20rpx; }
.text { font-size: 28rpx; color: var(--text-secondary); }
.action { margin-top: 30rpx; padding: 16rpx 40rpx; background: var(--color-primary); color: #fff; border-radius: 40rpx; border: none; font-size: 26rpx; }
</style>
