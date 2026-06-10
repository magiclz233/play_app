<template>
  <view class="container" v-if="order">
    <view class="order-card">
      <view class="header-row">
        <text class="order-no">No. {{ order.orderNo }}</text>
        <text class="status">{{ getStatusText(order.status) }}</text>
      </view>
      <view class="service-row">
        <view class="cover">
          <text>{{ t('lobby.title') }}</text>
        </view>
        <view class="service-info">
          <text class="title">{{ appStore.locale === 'en' ? 'Reserve Service' : '预约服务' }}（{{ order.hours }}{{ appStore.locale === 'en' ? ' Hrs' : '小时' }}）</text>
          <text class="desc">{{ order.reserveDate }} {{ order.reserveTimeStart }}-{{ order.reserveTimeEnd || '' }}</text>
          <text class="price">¥{{ order.totalAmount }}</text>
        </view>
      </view>
      <view class="amount-row">
        <text>{{ appStore.locale === 'en' ? 'Refund Amount' : '退款金额' }}</text>
        <text class="amount">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <view class="reason-card">
      <view class="section-title">{{ appStore.locale === 'en' ? 'Refund Reason' : '退款原因' }}</view>
      <textarea v-model="reason" maxlength="200" :placeholder="appStore.locale === 'en' ? 'Please explain why, funds will return to original payment source.' : '请输入退款原因，平台将按原支付渠道处理退款'" placeholder-class="std-placeholder"></textarea>
    </view>

    <view class="bottom-bar">
      <view class="summary">{{ appStore.locale === 'en' ? '1 Item, Refund ' : '共1件 退款金额 ' }}<text>¥{{ order.totalAmount }}</text></view>
      <button class="submit-btn" :loading="submitting" @click="submitRefund" hover-class="button-hover">{{ t('common.submit') }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAppStore } from '../../store/app';
import { t } from '../../utils/i18n';

const appStore = useAppStore();
const order = ref<any>(null);
const orderNo = ref('');
const reason = ref('');
const submitting = ref(false);

onLoad((options: any) => {
  if (options?.orderNo) {
    orderNo.value = options.orderNo;
    fetchDetail();
  }
});

const fetchDetail = async () => {
  const res = await request({ url: `/orders/${orderNo.value}`, method: 'GET' });
  if (res.code === 200) order.value = res.data;
};

const getStatusText = (status: number) => {
  const text = t(`order.status.${status}`);
  return text.includes('order.status.') ? t('common.status') : text;
};

const submitRefund = async () => {
  if (!reason.value.trim()) {
    uni.showToast({ title: appStore.locale === 'en' ? 'Please enter refund reason' : '请输入退款原因', icon: 'none' });
    return;
  }

  submitting.value = true;
  try {
    const res = await request({
      url: `/orders/${orderNo.value}/refund`,
      method: 'POST',
      data: { reason: reason.value.trim() }
    });
    if (res.code === 200) {
      uni.showToast({ title: t('common.success'), icon: 'success' });
      setTimeout(() => {
        uni.redirectTo({ url: `/pages/order/detail?orderNo=${orderNo.value}` });
      }, 500);
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: $bg-color-page;
  padding: 24rpx;
  padding-bottom: 150rpx;
  box-sizing: border-box;
}

.order-card, .reason-card {
  background: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: $box-shadow-sm;
  border: 1px solid $border-color-light;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 22rpx;
  border-bottom: 1px solid $border-color-light;

  .order-no { font-size: $font-size-base; color: $text-color-primary; font-family: monospace; }
  .status { color: $color-success; font-size: 24rpx; font-weight: bold; }
}

.service-row {
  display: flex;
  padding: 30rpx 0;
  border-bottom: 1px solid $border-color-light;

  .cover {
    width: 140rpx;
    height: 140rpx;
    border-radius: $border-radius-md;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;

    text { color: #fff; font-size: 24rpx; font-weight: bold; }
  }

  .service-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .title { font-size: 28rpx; font-weight: bold; color: $text-color-primary; margin-bottom: 12rpx; }
    .desc { font-size: 24rpx; color: $text-color-secondary; margin-bottom: 12rpx; }
    .price { 
      color: $color-primary; 
      font-size: 34rpx; 
      font-weight: 800; 
      font-family: 'Outfit', sans-serif;
    }
  }
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding-top: 24rpx;
  font-size: $font-size-base;
  color: $text-color-primary;

  .amount { 
    color: $color-primary; 
    font-weight: 800; 
    font-family: 'Outfit', sans-serif;
  }
}

.section-title {
  font-size: $font-size-base;
  font-weight: bold;
  color: $text-color-primary;
  margin-bottom: 20rpx;
}

textarea {
  width: 100%;
  height: 180rpx;
  background: $bg-color-page;
  border-radius: $border-radius-md;
  padding: 20rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: $text-color-primary;
  border: 1px solid $border-color-light;
}

.std-placeholder {
  color: $text-color-secondary;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: $bg-color-white;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.04);
  border-top: 1px solid $border-color-light;
  z-index: $z-index-sticky;

  .summary {
    font-size: $font-size-sm;
    color: $text-color-primary;

    text { 
      color: $color-primary; 
      font-weight: 800; 
      font-size: 34rpx;
      font-family: 'Outfit', sans-serif;
    }
  }

  .submit-btn {
    margin: 0;
    width: 240rpx;
    height: 80rpx;
    line-height: 80rpx;
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 28rpx;
    font-weight: bold;
    border: none;
    transition: all 0.1s ease;

    &::after { display: none; }
    
    &.button-hover {
      transform: scale(0.97);
      opacity: 0.9;
    }
  }
}

/* 深色模式按钮特异发光 */
.theme-dark {
  .submit-btn {
    box-shadow: 0 6px 20px rgba(255, 59, 92, 0.25);
  }
}
</style>
