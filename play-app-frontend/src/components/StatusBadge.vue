<template>
  <text class="badge" :style="{ background: bg, color: fg }">{{ text }}</text>
</template>
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: number; type: 'order' | 'audit' | 'withdraw' }>()

const orderMap: Record<number, [string, string, string]> = {
  10: ['待付款', '#FEF3C7', '#92400E'],
  20: ['待接单', '#DBEAFE', '#1E40AF'],
  30: ['已接单', '#E0E7FF', '#3730A3'],
  40: ['已确认', '#D1FAE5', '#065F46'],
  50: ['服务中', '#FEE2E2', '#991B1B'],
  60: ['待确认', '#FED7AA', '#9A3412'],
  70: ['已完工', '#D1FAE5', '#065F46'],
  80: ['已完成', '#E0E7FF', '#3730A3'],
  100: ['取消中', '#FEE2E2', '#991B1B'],
  120: ['已退款', '#F3F4F6', '#374151'],
  130: ['部分退款', '#F3F4F6', '#374151'],
  200: ['纠纷中', '#FEE2E2', '#991B1B'],
  210: ['纠纷完成', '#F3F4F6', '#374151'],
  250: ['已关闭', '#F3F4F6', '#6B7280']
}

const auditMap: Record<number, [string, string, string]> = {
  0: ['待审核', '#FEF3C7', '#92400E'],
  1: ['已通过', '#D1FAE5', '#065F46'],
  2: ['已驳回', '#FEE2E2', '#991B1B'],
  3: ['已冻结', '#F3F4F6', '#6B7280']
}

const withdrawMap: Record<number, [string, string, string]> = {
  0: ['待审核', '#FEF3C7', '#92400E'],
  1: ['打款中', '#DBEAFE', '#1E40AF'],
  2: ['已打款', '#D1FAE5', '#065F46'],
  3: ['打款失败', '#FEE2E2', '#991B1B'],
  4: ['已驳回', '#F3F4F6', '#6B7280']
}

const map = props.type === 'order' ? orderMap : props.type === 'audit' ? auditMap : withdrawMap
const [text, bg, fg] = map[props.status] || ['未知', '#F3F4F6', '#6B7280']
</script>
<style scoped>
.badge { padding: 4rpx 14rpx; border-radius: 8rpx; font-size: 20rpx; font-weight: 600; }
</style>
