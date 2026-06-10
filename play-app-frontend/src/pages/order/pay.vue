<template>
  <view class="container">
    <view class="pay-header">
      <text class="time-limit">{{ appStore.locale === 'en' ? 'Time Remaining ' : '支付剩余时间 ' }}{{ countdownText }}</text>
      <view class="amount-box">
        <text class="currency">¥</text>
        <text class="amount">{{ amount }}</text>
      </view>
      <text class="order-no">No. {{ orderNo }}</text>
    </view>

    <view class="pay-methods">
      <view class="method-item">
        <view class="method-info">
          <text class="iconfont icon-wechat-pay"></text>
          <text class="name">{{ appStore.locale === 'en' ? 'WeChat Pay' : '微信支付' }}</text>
        </view>
        <radio checked :color="String(radioColor)"></radio>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="pay-btn" @click="doPay" :loading="isPaying" hover-class="button-hover">
        {{ appStore.locale === 'en' ? 'Confirm Payment ¥' : '确认支付 ¥' }}{{ amount }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAppStore } from '../../store/app';
import { t } from '../../utils/i18n';

const appStore = useAppStore();
const orderNo = ref('');
const amount = ref('0.00');
const isPaying = ref(false);
const radioColor = '#10B981';
const remainingSeconds = ref(900); // 15分钟
const countdownText = ref('15:00');
let timer: any = null;

onLoad((options: any) => {
  orderNo.value = options?.orderNo || '';
  amount.value = options?.amount || '0.00';
  startCountdown();
});

onUnmounted(() => { if (timer) clearInterval(timer); });

const startCountdown = () => {
  timer = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      clearInterval(timer);
      uni.showToast({ title: '订单已超时', icon: 'none' });
      setTimeout(() => uni.reLaunch({ url: '/pages/order/list' }), 1500);
      return;
    }
    remainingSeconds.value--;
    const m = Math.floor(remainingSeconds.value / 60);
    const s = remainingSeconds.value % 60;
    countdownText.value = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }, 1000);
};

const doPay = async () => {
  if (!orderNo.value) return;
  
  isPaying.value = true;
  try {
    const res = await request({
      url: `/orders/${orderNo.value}/prepay`,
      method: 'POST'
    });

    if (res.code === 200) {
      const payInfo = res.data;
      
      if (payInfo.mock) {
        uni.showLoading({ title: appStore.locale === 'en' ? 'Processing Pay...' : '模拟支付中...' });
        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: t('common.success'), icon: 'success' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/order/list' });
          }, 1500);
        }, 1500);
        return;
      }

      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: payInfo.timeStamp,
        nonceStr: payInfo.nonceStr,
        package: payInfo.package,
        signType: payInfo.signType,
        paySign: payInfo.paySign,
        success: () => {
          uni.showToast({ title: t('common.success'), icon: 'success' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/order/list' });
          }, 1500);
        },
        fail: (err) => {
          console.error('支付失败', err);
          uni.showToast({ title: appStore.locale === 'en' ? 'Payment Cancelled' : '支付取消或失败', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/order/list' });
          }, 1500);
        }
      });
    }
  } finally {
    isPaying.value = false;
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
  box-sizing: border-box;
}

.pay-header {
  background-color: $bg-color-white;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
  margin-bottom: 24rpx;
  box-shadow: $box-shadow-sm;
  border-bottom: 1px solid $border-color-light;
  
  .time-limit {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-bottom: 24rpx;
  }
  
  .amount-box {
    color: $text-color-primary;
    margin-bottom: 24rpx;
    display: flex;
    align-items: baseline;
    justify-content: center;
    
    .currency { font-size: 44rpx; font-weight: 800; }
    .amount { 
      font-size: 88rpx; 
      font-weight: 800; 
      margin-left: 8rpx; 
      font-family: 'Outfit', sans-serif;
    }
  }
  
  .order-no {
    font-size: 22rpx;
    color: $text-color-secondary;
    font-family: monospace;
  }
}

.pay-methods {
  background-color: $bg-color-white;
  padding: 0 30rpx;
  box-shadow: $box-shadow-sm;
  border-top: 1px solid $border-color-light;
  border-bottom: 1px solid $border-color-light;
  
  .method-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 36rpx 0;
    
    .method-info {
      display: flex;
      align-items: center;
      
      .icon-wechat-pay {
        color: var(--color-success);
        font-size: 48rpx;
        margin-right: 20rpx;
      }
      
      .name {
        font-size: $font-size-base;
        color: $text-color-primary;
        font-weight: bold;
      }
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
  background-color: $bg-color-white;
  border-top: 1px solid $border-color-light;
  
  .pay-btn {
    width: 100%;
    height: 96rpx;
    background-color: var(--color-success);
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 32rpx;
    font-weight: bold;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
    transition: all 0.1s ease;
    
    &::after { display: none; }
    
    &.button-hover {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}

/* 暗色模式特殊发光处理 */
.theme-dark {
  .pay-btn {
    box-shadow: 0 8px 24px rgba(16, 185, 129, 0.25);
  }
}
</style>
