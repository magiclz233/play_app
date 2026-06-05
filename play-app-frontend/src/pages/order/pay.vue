<template>
  <view class="container">
    <view class="pay-header">
      <text class="time-limit">支付剩余时间 14:59</text>
      <view class="amount-box">
        <text class="currency">¥</text>
        <text class="amount">{{ amount }}</text>
      </view>
      <text class="order-no">订单号：{{ orderNo }}</text>
    </view>

    <view class="pay-methods">
      <view class="method-item">
        <view class="method-info">
          <text class="iconfont icon-wechat-pay"></text>
          <text class="name">微信支付</text>
        </view>
        <radio checked color="#10B981"></radio>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="pay-btn" @click="doPay" :loading="isPaying">确认支付 ¥{{ amount }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {request} from '../../utils/request';

const orderNo = ref('');
const amount = ref('0.00');
const isPaying = ref(false);

onLoad((options: any) => {
  orderNo.value = options?.orderNo || '';
  amount.value = options?.amount || '0.00';
});

const doPay = async () => {
  if (!orderNo.value) return;
  
  isPaying.value = true;
  try {
    // 1. 获取后端 prepay 参数
    const res = await request({
      url: `/orders/${orderNo.value}/prepay`,
      method: 'POST'
    });

    if (res.code === 200) {
      const payInfo = res.data;
      
      // MVP阶段防拦截，如果后端返回 mock=true，则直接跳转成功
      if (payInfo.mock) {
        uni.showLoading({ title: '模拟支付中...' });
        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: '支付成功', icon: 'success' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/order/list' });
          }, 1500);
        }, 1500);
        return;
      }

      // 2. 真实环境调用微信原生支付组件
      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: payInfo.timeStamp,
        nonceStr: payInfo.nonceStr,
        package: payInfo.package,
        signType: payInfo.signType,
        paySign: payInfo.paySign,
        success: () => {
          uni.showToast({ title: '支付成功', icon: 'success' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/order/list' });
          }, 1500);
        },
        fail: (err) => {
          console.error('支付失败', err);
          uni.showToast({ title: '支付取消或失败', icon: 'none' });
          // 可跳转到订单列表页去继续支付
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
}

.pay-header {
  background-color: $bg-color-white;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  margin-bottom: 20rpx;
  
  .time-limit {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-bottom: 20rpx;
  }
  
  .amount-box {
    color: $text-color-primary;
    margin-bottom: 20rpx;
    
    .currency { font-size: 40rpx; font-weight: bold; }
    .amount { font-size: 80rpx; font-weight: bold; margin-left: 8rpx; }
  }
  
  .order-no {
    font-size: 20rpx;
    color: $text-color-placeholder;
  }
}

.pay-methods {
  background-color: $bg-color-white;
  padding: 0 30rpx;
  
  .method-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 0;
    
    .method-info {
      display: flex;
      align-items: center;
      
      .icon-wechat-pay {
        color: #10B981;
        font-size: 48rpx;
        margin-right: 20rpx;
      }
      
      .name {
        font-size: $font-size-base;
        color: $text-color-primary;
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
  
  .pay-btn {
    width: 100%;
    height: 96rpx;
    background-color: #10B981; // 微信绿
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 32rpx;
    font-weight: bold;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
    
    &::after { display: none; }
    &:active { opacity: 0.9; }
  }
}
</style>
