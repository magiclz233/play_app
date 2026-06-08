<template>
  <view class="container" v-if="order">
    <view class="order-card">
      <view class="header-row">
        <text class="order-no">订单号：{{ order.orderNo }}</text>
        <text class="status">{{ getStatusText(order.status) }}</text>
      </view>
      <view class="service-row">
        <view class="cover">
          <text>同城伴玩</text>
        </view>
        <view class="service-info">
          <text class="title">预约服务（{{ order.hours }}小时）</text>
          <text class="desc">{{ order.reserveDate }} {{ order.reserveTimeStart }}-{{ order.reserveTimeEnd || '' }}</text>
          <text class="price">¥{{ order.totalAmount }}</text>
        </view>
      </view>
      <view class="amount-row">
        <text>退款金额</text>
        <text class="amount">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <view class="reason-card">
      <view class="section-title">退款原因</view>
      <textarea v-model="reason" maxlength="200" placeholder="请输入退款原因，平台将按原支付渠道处理退款"></textarea>
    </view>

    <view class="bottom-bar">
      <view class="summary">共1件 退款金额 <text>¥{{ order.totalAmount }}</text></view>
      <button class="submit-btn" :loading="submitting" @click="submitRefund">提交申请</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';

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
  const map: Record<number, string> = {
    20: '待服务', 30: '已拉群', 40: '待服务', 50: '服务中', 60: '待确认', 70: '待评价', 80: '已完成', 100: '退款申请中'
  };
  return map[status] || String(status);
};

const submitRefund = async () => {
  if (!reason.value.trim()) {
    uni.showToast({ title: '请输入退款原因', icon: 'none' });
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
      uni.showToast({ title: '已提交', icon: 'success' });
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
  padding: 20rpx;
  padding-bottom: 140rpx;
}

.order-card, .reason-card {
  background: #fff;
  border-radius: $border-radius-lg;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 22rpx;
  border-bottom: 1rpx solid $border-color-light;

  .order-no { font-size: 28rpx; color: $text-color-primary; }
  .status { color: $color-success; font-size: 24rpx; font-weight: bold; }
}

.service-row {
  display: flex;
  padding: 26rpx 0;
  border-bottom: 1rpx solid $border-color-light;

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

    .title { font-size: 30rpx; font-weight: bold; color: $text-color-primary; margin-bottom: 12rpx; }
    .desc { font-size: 24rpx; color: $text-color-secondary; margin-bottom: 12rpx; }
    .price { color: $color-secondary; font-size: 34rpx; font-weight: bold; }
  }
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding-top: 24rpx;
  font-size: 28rpx;

  .amount { color: $color-secondary; font-weight: bold; }
}

.section-title {
  font-size: 30rpx;
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
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);

  .summary {
    font-size: 24rpx;
    color: $text-color-regular;

    text { color: $color-secondary; font-weight: bold; }
  }

  .submit-btn {
    margin: 0;
    width: 220rpx;
    height: 76rpx;
    line-height: 76rpx;
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 28rpx;
    font-weight: bold;

    &::after { display: none; }
  }
}
</style>
