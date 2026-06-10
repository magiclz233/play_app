<template>
  <text class="badge" :class="statusClass">{{ text }}</text>
</template>
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: number; type: 'order' | 'audit' | 'withdraw' }>()

/** 状态 → [显示文本, CSS 类名] */
const orderMap: Record<number, [string, string]> = {
  10: ['待付款', 'badge-warning'],
  20: ['待接单', 'badge-info'],
  30: ['已接单', 'badge-info'],
  40: ['已确认', 'badge-success'],
  50: ['服务中', 'badge-error'],
  60: ['待确认', 'badge-warning'],
  70: ['已完工', 'badge-success'],
  80: ['已完成', 'badge-info'],
  100: ['取消中', 'badge-error'],
  120: ['已退款', 'badge-muted'],
  130: ['部分退款', 'badge-muted'],
  200: ['纠纷中', 'badge-error'],
  210: ['纠纷完成', 'badge-muted'],
  250: ['已关闭', 'badge-muted']
}

const auditMap: Record<number, [string, string]> = {
  0: ['待审核', 'badge-warning'],
  1: ['已通过', 'badge-success'],
  2: ['已驳回', 'badge-error'],
  3: ['已冻结', 'badge-muted']
}

const withdrawMap: Record<number, [string, string]> = {
  0: ['待审核', 'badge-warning'],
  1: ['打款中', 'badge-info'],
  2: ['已打款', 'badge-success'],
  3: ['打款失败', 'badge-error'],
  4: ['已驳回', 'badge-muted']
}

const map = props.type === 'order' ? orderMap : props.type === 'audit' ? auditMap : withdrawMap
const entry = map[props.status] || ['未知', 'badge-muted']

const text = entry[0]
const statusClass = computed(() => entry[1])
</script>
<style lang="scss" scoped>
.badge {
  padding: 4rpx 14rpx;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  line-height: 1.6;
}

/* 使用 CSS 变量实现状态色，自动适配明暗模式 */
.badge-warning {
  background-color: rgba(var(--color-accent-rgb), 0.12);
  color: var(--color-accent);
}
.badge-success {
  background-color: rgba(16, 185, 129, 0.1);
  color: var(--color-success);
}
.badge-error {
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--color-error);
}
.badge-info {
  background-color: rgba(59, 130, 246, 0.1);
  color: var(--color-info);
}
.badge-muted {
  background-color: var(--bg-subtle);
  color: var(--text-muted);
}
</style>
