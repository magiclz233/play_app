<template>
  <view class="container" :class="appStore.themeClass">
    <view class="card rating-card">
      <view class="title">{{ appStore.locale === 'en' ? 'Rate This Service' : '为本次服务打分' }}</view>
      <view class="stars">
        <text 
          class="iconfont" 
          :class="i <= rating ? 'icon-star-fill active' : 'icon-star'" 
          v-for="i in 5" 
          :key="i"
          @click="rating = i"
        ></text>
      </view>
      <view class="rating-text">{{ ratingText }}</view>
    </view>

    <view class="card content-card">
      <textarea v-model="content" :placeholder="appStore.locale === 'en' ? 'How was the companion? Write down your feedback to help others booking...' : '搭子服务怎么样？满足你的期待吗？写下评价帮助更多人参考吧~'" placeholder-class="std-placeholder" maxlength="500"></textarea>
    </view>

    <view class="submit-wrap">
      <button class="submit-btn" @click="submitReview" :loading="isSubmitting" hover-class="button-hover">
        {{ appStore.locale === 'en' ? 'Submit Review' : '发布评价' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAppStore } from '../../store/app';
import { t } from '../../utils/i18n';

const appStore = useAppStore();
const orderNo = ref('');
const rating = ref(5);
const content = ref('');
const isSubmitting = ref(false);

const ratingText = computed(() => {
  const textMap: Record<string, Record<number, string>> = {
    'zh-Hans': { 1: '非常差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意' },
    'zh-Hant': { 1: '非常差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意' },
    'en': { 1: 'Very Bad', 2: 'Bad', 3: 'Average', 4: 'Good', 5: 'Excellent' }
  };
  return textMap[appStore.locale]?.[rating.value] || '';
});

onLoad((options: any) => {
  if (options?.orderNo) orderNo.value = options.orderNo;
});

const submitReview = async () => {
  if (!orderNo.value) return;
  if (rating.value < 1) {
    uni.showToast({ title: appStore.locale === 'en' ? 'Please select a rating' : '请先打分', icon: 'none' });
    return;
  }

  isSubmitting.value = true;
  try {
    const res = await request({
      url: `/orders/${orderNo.value}/review`,
      method: 'POST',
      data: {
        rating: rating.value,
        content: content.value
      }
    });

    if (res.code === 200) {
      uni.showToast({ title: t('common.success'), icon: 'success' });
      setTimeout(() => {
        uni.navigateBack({ delta: 1 });
      }, 1500);
    }
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
  padding: 24rpx;
  box-sizing: border-box;
}

.card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 48rpx 30rpx;
  margin-bottom: 24rpx;
  box-shadow: $box-shadow-sm;
  border: 1px solid $border-color-light;
}

.rating-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .title { font-size: $font-size-base; font-weight: bold; margin-bottom: 36rpx; color: $text-color-primary; }
  
  .stars {
    display: flex;
    justify-content: center;
    margin-bottom: 24rpx;
    
    .iconfont {
      font-size: 72rpx;
      color: rgba(0, 0, 0, 0.08);
      margin: 0 12rpx;
      transition: color 0.2s ease, transform 0.1s ease;
      
      &:active {
        transform: scale(0.9);
      }
      
      &.active { color: $color-warning; }
    }
  }
  
  .rating-text {
    font-size: $font-size-sm;
    color: $color-warning;
    font-weight: bold;
  }
}

.content-card {
  padding: 30rpx;
  
  textarea {
    width: 100%;
    height: 320rpx;
    font-size: $font-size-base;
    line-height: 1.6;
    color: $text-color-primary;
  }
}

.std-placeholder {
  color: $text-color-secondary;
}

.submit-wrap {
  margin-top: 60rpx;
  padding: 0 20rpx;
  
  .submit-btn {
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    height: 90rpx;
    line-height: 90rpx;
    transition: all 0.1s ease;
    
    &::after { display: none; }
    
    &.button-hover {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}

/* 深色模式特异微调 */
.theme-dark {
  .stars {
    .iconfont {
      color: rgba(255, 255, 255, 0.15);
    }
  }
  .submit-btn {
    box-shadow: 0 6px 20px rgba(255, 59, 92, 0.25);
  }
}
</style>
