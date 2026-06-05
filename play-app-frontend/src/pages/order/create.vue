<template>
  <view class="container" v-if="companion">
    <!-- 助教简短信息 -->
    <view class="companion-card">
      <image :src="companion.photoUrls?.[0] || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600&auto=format&fit=crop'" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="name-box">
          <text class="name">{{ companion.nickname }}</text>
          <text class="tag">{{ companion.gender === 1 ? '男' : '女' }} {{ companion.age }}岁</text>
        </view>
        <view class="price-box">
          <text class="price">¥{{ companion.pricePerHour }}/小时</text>
        </view>
      </view>
    </view>

    <!-- 预约设置 -->
    <view class="form-group">
      <view class="section-title">预约设置</view>
      
      <view class="form-item" @click="showTimePicker = true">
        <text class="label">预约时间</text>
        <view class="value" :class="{ 'placeholder': !appointmentTime }">
          {{ formattedTime || '请选择开始时间' }}
        </view>
        <text class="iconfont icon-arrow-right"></text>
      </view>

      <view class="form-item">
        <text class="label">服务时长</text>
        <view class="stepper">
          <view class="btn" @click="changeHours(-1)" :class="{ 'disabled': hours <= 1 }">-</view>
          <input type="number" v-model="hours" class="input" @blur="checkHours" />
          <view class="btn" @click="changeHours(1)">+</view>
        </view>
        <text class="unit">小时</text>
      </view>
      
      <view class="form-item vertical">
        <text class="label">订单备注</text>
        <textarea v-model="remark" placeholder="想对搭子说些什么？或者写下您的特殊要求（选填）" maxlength="100"></textarea>
      </view>
    </view>

    <!-- 费用明细 -->
    <view class="form-group">
      <view class="section-title">费用明细</view>
      <view class="cost-row">
        <text class="label">服务费 (¥{{ companion.pricePerHour }} x {{ hours }}时)</text>
        <text class="value">¥{{ totalServiceFee }}</text>
      </view>
      <view class="cost-row">
        <text class="label">平台服务费 (免首单)</text>
        <text class="value highlight">-¥0.00</text>
      </view>
    </view>

    <view class="safe-notice">
      <text class="iconfont icon-safe"></text>
      <text>支付资金由平台担保，服务确认完成后才会打款给搭子。</text>
    </view>

    <!-- 底部支付栏 -->
    <view class="bottom-bar">
      <view class="total-box">
        <text class="label">合计：</text>
        <text class="currency">¥</text>
        <text class="price">{{ totalServiceFee }}</text>
      </view>
      <button class="pay-btn" @click="submitOrder" :loading="isSubmitting">立即支付</button>
    </view>

    <!-- 简单的原生日历/时间选择器替代品 -->
    <!-- 由于uni-app自带的时间选择器可能不能精准选择未来具体小时，这里用原生的picker模拟 -->
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';

const companion = ref<any>(null);
const hours = ref(1);
const remark = ref('');
const isSubmitting = ref(false);

// 为了MVP简化，我们直接默认预约时间为1小时后
const appointmentTime = ref<Date>(new Date(Date.now() + 3600000));

const formattedTime = computed(() => {
  if (!appointmentTime.value) return '';
  const d = appointmentTime.value;
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours() < 10 ? '0'+d.getHours() : d.getHours()}:${d.getMinutes() < 10 ? '0'+d.getMinutes() : d.getMinutes()}`;
});

onLoad((options: any) => {
  const id = options?.companionId;
  if (id) fetchCompanion(id);
});

const fetchCompanion = async (id: string) => {
  const res = await request({
    url: `/companions/${id}`,
    method: 'GET'
  });
  if (res.code === 200) {
    companion.value = res.data;
  }
};

const changeHours = (step: number) => {
  if (hours.value + step >= 1) {
    hours.value += step;
  }
};

const checkHours = () => {
  if (hours.value < 1 || isNaN(hours.value)) {
    hours.value = 1;
  }
};

const totalServiceFee = computed(() => {
  if (!companion.value) return '0.00';
  return (parseFloat(companion.value.pricePerHour) * hours.value).toFixed(2);
});

const submitOrder = async () => {
  if (!companion.value) return;
  
  isSubmitting.value = true;
  try {
    // 1. 创建订单
    // 构造ISO时间格式
    const t = appointmentTime.value;
    const isoTime = new Date(t.getTime() - (t.getTimezoneOffset() * 60000)).toISOString().slice(0, 19);
    
    const createRes = await request({
      url: '/orders',
      method: 'POST',
      data: {
        companionId: companion.value.userId,
        serviceId: 1, // mock
        hours: hours.value,
        appointmentTime: isoTime,
        remark: remark.value
      }
    });

    if (createRes.code === 200) {
      const orderNo = createRes.data;
      
      // 2. 跳转到支付中转页 (或者直接在这里获取prepay参数唤起支付)
      uni.navigateTo({
        url: `/pages/order/pay?orderNo=${orderNo}&amount=${totalServiceFee.value}`
      });
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
  padding-bottom: 140rpx;
}

.companion-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: $border-radius-md;
    margin-right: 30rpx;
  }
  
  .info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100rpx;
    
    .name-box {
      display: flex;
      align-items: center;
      
      .name { font-size: $font-size-lg; font-weight: bold; margin-right: 16rpx; }
      .tag {
        font-size: 20rpx;
        background-color: rgba(124, 58, 237, 0.1);
        color: $color-primary;
        padding: 4rpx 12rpx;
        border-radius: $border-radius-sm;
      }
    }
    
    .price-box {
      .price { color: $color-secondary; font-weight: bold; font-size: $font-size-base; }
    }
  }
}

.form-group {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 0 30rpx 30rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: $font-size-base;
    font-weight: bold;
    color: $text-color-primary;
    padding: 30rpx 0;
    border-bottom: 1rpx solid $border-color-light;
    margin-bottom: 10rpx;
  }
  
  .form-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    
    &.vertical {
      flex-direction: column;
      align-items: flex-start;
      
      .label { margin-bottom: 20rpx; }
      textarea {
        width: 100%;
        height: 120rpx;
        background-color: $bg-color-page;
        border-radius: $border-radius-md;
        padding: 20rpx;
        font-size: $font-size-sm;
        box-sizing: border-box;
      }
    }
    
    .label { width: 160rpx; font-size: $font-size-base; color: $text-color-regular; }
    
    .value {
      flex: 1;
      font-size: $font-size-base;
      text-align: right;
      margin-right: 10rpx;
      
      &.placeholder { color: $text-color-placeholder; }
    }
    
    .icon-arrow-right { font-size: 24rpx; color: $text-color-secondary; }
    
    .stepper {
      flex: 1;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      
      .btn {
        width: 60rpx; height: 60rpx;
        background-color: $bg-color-page;
        display: flex; justify-content: center; align-items: center;
        border-radius: $border-radius-sm;
        font-size: 32rpx; font-weight: bold; color: $text-color-primary;
        
        &.disabled { color: $text-color-placeholder; }
      }
      
      .input {
        width: 80rpx;
        text-align: center;
        font-size: $font-size-base;
        font-weight: bold;
      }
    }
    
    .unit { font-size: $font-size-base; color: $text-color-regular; margin-left: 20rpx; }
  }
  
  .cost-row {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    
    .label { font-size: $font-size-sm; color: $text-color-regular; }
    .value {
      font-size: $font-size-base; font-weight: bold;
      &.highlight { color: $color-success; }
    }
  }
}

.safe-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  
  .icon-safe { color: $color-success; font-size: 28rpx; margin-right: 8rpx; }
  text { font-size: 20rpx; color: $text-color-secondary; }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: $bg-color-white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: $z-index-sticky;
  
  .total-box {
    display: flex;
    align-items: flex-end;
    
    .label { font-size: 28rpx; color: $text-color-primary; margin-bottom: 6rpx; }
    .currency { font-size: 32rpx; color: $color-secondary; font-weight: bold; margin-bottom: 4rpx; }
    .price { font-size: 56rpx; color: $color-secondary; font-weight: bold; line-height: 1; }
  }
  
  .pay-btn {
    width: 280rpx;
    height: 88rpx;
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 32rpx;
    font-weight: bold;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
    margin: 0;
    
    &::after { display: none; }
  }
}
</style>
