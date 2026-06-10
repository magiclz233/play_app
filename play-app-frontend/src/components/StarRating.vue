<template>
  <view class="stars" :style="{ '--star-size': size + 'rpx' }">
    <view
      v-for="i in 5"
      :key="i"
      class="star"
      :class="{
        filled: i <= value,
        half: half && i === Math.ceil(value) && value % 1 !== 0
      }"
      @click="readonly ? null : $emit('change', i)"
    >
      <!-- SVG 五角星图标替代 emoji ★/☆ -->
      <svg viewBox="0 0 24 24" class="star-svg" :class="{ filled: i <= value }">
        <path
          d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
          :fill="i <= value ? 'var(--color-accent, #FF9F1C)' : 'none'"
          :stroke="i <= value ? 'var(--color-accent, #FF9F1C)' : '#D1D5DB'"
          stroke-width="1.5"
        />
      </svg>
    </view>
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
.star {
  width: var(--star-size);
  height: var(--star-size);
  margin-right: 4rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.star-svg {
  width: 100%;
  height: 100%;
  transition: transform 0.15s ease;
}
.star:active .star-svg {
  transform: scale(1.2);
}
.text { margin-left: 12rpx; font-size: 24rpx; color: var(--text-secondary); }
</style>
