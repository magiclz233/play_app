<template>
  <view class="container">
    <view class="card rating-card">
      <view class="title">为本次服务打分</view>
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
      <textarea v-model="content" placeholder="搭子服务怎么样？满足你的期待吗？写下评价帮助更多人参考吧~" maxlength="500"></textarea>
    </view>

    <view class="submit-wrap">
      <button class="submit-btn" @click="submitReview" :loading="isSubmitting">发布评价</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';

const orderNo = ref('');
const rating = ref(5);
const content = ref('');
const isSubmitting = ref(false);

const ratingText = computed(() => {
  const map: Record<number, string> = {
    1: '非常差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意'
  };
  return map[rating.value] || '';
});

onLoad((options: any) => {
  if (options?.orderNo) orderNo.value = options.orderNo;
});

const submitReview = async () => {
  if (!orderNo.value) return;
  if (rating.value < 1) return uni.showToast({ title: '请先打分', icon: 'none' });

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
      uni.showToast({ title: '评价成功', icon: 'success' });
      setTimeout(() => {
        // 返回订单列表
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
  padding: 20rpx;
}

.card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
}

.rating-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .title { font-size: $font-size-base; font-weight: bold; margin-bottom: 30rpx; color: $text-color-primary; }
  
  .stars {
    display: flex;
    justify-content: center;
    margin-bottom: 20rpx;
    
    .iconfont {
      font-size: 64rpx;
      color: #E5E7EB;
      margin: 0 10rpx;
      transition: color 0.3s;
      
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
  textarea {
    width: 100%;
    height: 300rpx;
    font-size: $font-size-base;
    line-height: 1.5;
  }
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
    
    &::after { display: none; }
  }
}
</style>
