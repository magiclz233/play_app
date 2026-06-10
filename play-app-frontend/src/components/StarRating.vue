<template>
  <view class="stars" :style="{ '--star-size': size + 'rpx' }">
    <text v-for="i in 5" :key="i" class="star" :class="{ filled: i <= value, half: half && i === Math.ceil(value) && value % 1 !== 0 }" @click="readonly ? null : $emit('change', i)">
      {{ i <= value ? '★' : '☆' }}
    </text>
    <text v-if="showText" class="text">{{ valueText }}</text>
  </view>
</template>
<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  value: number; size?: number; readonly?: boolean; showText?: boolean; half?: boolean
}>(), { size: 28, readonly: true, showText: false, half: false })

defineEmits(['change'])

const ratingTexts = ['非常差','较差','一般','满意','非常满意']
const valueText = computed(() => ratingTexts[Math.round(props.value) - 1] || '')
</script>
<style scoped>
.stars { display: inline-flex; align-items: center; }
.star { font-size: var(--star-size); color: #D1D5DB; margin-right: 4rpx; }
.star.filled { color: #FF9F1C; }
.text { margin-left: 12rpx; font-size: 24rpx; color: var(--text-secondary); }
</style>
