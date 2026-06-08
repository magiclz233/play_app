<template>
  <view class="container" v-if="companion">
    <view class="companion-card">
      <image :src="coverUrl" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="name-box">
          <text class="name">{{ companion.nickname }}</text>
          <text class="tag">{{ companion.gender === 1 ? '男' : '女' }} {{ companion.age }}岁</text>
        </view>
        <text class="price">¥{{ companion.pricePerHour }}/小时</text>
      </view>
    </view>

    <view class="form-group">
      <view class="section-title">预约信息</view>
      <view class="form-item" @click="showTimePicker = true">
        <text class="label">服务时间</text>
        <view class="value" :class="{ placeholder: !selectedTime }">{{ formattedTime || '请选择时间段' }}</view>
        <text class="iconfont icon-arrow-right"></text>
      </view>

      <view class="form-item">
        <text class="label">服务时长</text>
        <view class="stepper">
          <view class="step-btn" :class="{ disabled: hours <= 1 }" @click="changeHours(-1)">-</view>
          <input type="number" v-model="hours" class="hour-input" @blur="checkHours" />
          <view class="step-btn" @click="changeHours(1)">+</view>
        </view>
        <text class="unit">小时</text>
      </view>
    </view>

    <view class="form-group">
      <view class="section-title">对接信息</view>
      <view class="form-item vertical">
        <text class="label">微信号/手机号</text>
        <input class="std-input" v-model="customerWechat" placeholder="用于客服拉群和线下对接" maxlength="50" />
      </view>
      <view class="form-item vertical">
        <text class="label">服务地址</text>
        <input class="std-input" v-model="address" placeholder="请选择或填写服务地址" maxlength="255" />
      </view>
      <view class="form-item vertical">
        <text class="label">详细地址</text>
        <input class="std-input" v-model="addressDetail" placeholder="门牌号、包间号等（选填）" maxlength="255" />
      </view>
      <view class="form-item vertical">
        <text class="label">订单备注</text>
        <textarea v-model="remark" placeholder="特殊要求（选填）" maxlength="100"></textarea>
      </view>
    </view>

    <view class="form-group">
      <view class="section-title">费用明细</view>
      <view class="cost-row">
        <text class="label">服务费 (¥{{ companion.pricePerHour }} x {{ hours }}时)</text>
        <text class="value">¥{{ totalServiceFee }}</text>
      </view>
      <view class="cost-row">
        <text class="label">平台服务费</text>
        <text class="value">¥0.00</text>
      </view>
      <view class="cost-row total">
        <text class="label">合计</text>
        <text class="value highlight">¥{{ totalServiceFee }}</text>
      </view>
    </view>

    <view class="safe-notice">
      <text class="iconfont icon-safe"></text>
      <text>支付资金由平台担保，服务完成并核销后再结算给助教。</text>
    </view>

    <view class="bottom-bar">
      <view class="total-box">
        <text class="label">合计：</text>
        <text class="price">¥{{ totalServiceFee }}</text>
      </view>
      <button class="pay-btn" @click="submitOrder" :loading="isSubmitting">立即支付</button>
    </view>

    <view class="time-mask" v-if="showTimePicker" @click="showTimePicker = false">
      <view class="time-panel" @click.stop>
        <view class="panel-title">选择服务时间</view>
        <scroll-view scroll-x class="date-tabs">
          <view class="date-tab"
                v-for="item in dateOptions"
                :key="item.value"
                :class="{ active: selectedDate === item.value }"
                @click="selectedDate = item.value">
            {{ item.label }}
          </view>
        </scroll-view>
        <view class="slot-grid">
          <view class="slot"
                v-for="slot in timeSlots"
                :key="slot"
                :class="{ active: selectedTime === slot, disabled: isSlotDisabled(slot) }"
                @click="chooseTime(slot)">
            <text>{{ slot }}</text>
            <text>{{ isSlotDisabled(slot) ? '不可预约' : '可预约' }}</text>
          </view>
        </view>
        <button class="panel-confirm" @click="confirmTime">确定</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';

const companion = ref<any>(null);
const hours = ref(3);
const remark = ref('');
const address = ref('');
const addressDetail = ref('');
const customerWechat = ref('');
const isSubmitting = ref(false);
const showTimePicker = ref(false);

const pad = (num: number) => String(num).padStart(2, '0');
const formatDateValue = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const dateOptions = Array.from({ length: 5 }).map((_, index) => {
  const date = new Date();
  date.setDate(date.getDate() + index);
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()];
  return {
    value: formatDateValue(date),
    label: `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${week}`
  };
});

const timeSlots = ['10:00', '10:30', '11:00', '11:30', '12:00', '12:30', '13:00', '13:30', '14:00', '14:30', '15:00', '15:30', '16:00', '16:30', '17:00', '17:30', '18:00', '18:30', '19:00', '19:30', '20:00', '20:30', '21:00', '21:30', '22:00', '22:30', '23:00'];
const selectedDate = ref(dateOptions[0].value);
const selectedTime = ref('');

const coverUrl = computed(() => companion.value?.photoUrls?.[0] || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600&auto=format&fit=crop');
const selectedDateTime = computed(() => selectedTime.value ? new Date(`${selectedDate.value}T${selectedTime.value}:00`) : null);
const formattedTime = computed(() => {
  if (!selectedDateTime.value) return '';
  const d = selectedDateTime.value;
  return `${pad(d.getMonth() + 1)}月${pad(d.getDate())}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`;
});
const totalServiceFee = computed(() => {
  if (!companion.value) return '0.00';
  return (Number(companion.value.pricePerHour || 0) * Number(hours.value || 1)).toFixed(2);
});

onLoad((options: any) => {
  const id = options?.companionId;
  initDefaultTime();
  if (id) fetchCompanion(id);
});

const initDefaultTime = () => {
  const firstAvailable = timeSlots.find((slot) => !isSlotDisabled(slot));
  selectedTime.value = firstAvailable || timeSlots[0];
};

const fetchCompanion = async (id: string) => {
  const res = await request({ url: `/companions/${id}`, method: 'GET' });
  if (res.code === 200) companion.value = res.data;
};

const isSlotDisabled = (slot: string) => {
  const candidate = new Date(`${selectedDate.value}T${slot}:00`);
  return candidate.getTime() <= Date.now() + 30 * 60 * 1000;
};

const chooseTime = (slot: string) => {
  if (isSlotDisabled(slot)) return;
  selectedTime.value = slot;
};

const confirmTime = () => {
  if (!selectedTime.value || isSlotDisabled(selectedTime.value)) {
    uni.showToast({ title: '请选择可预约时间', icon: 'none' });
    return;
  }
  showTimePicker.value = false;
};

const changeHours = (step: number) => {
  const next = Number(hours.value || 1) + step;
  if (next >= 1 && next <= 12) hours.value = next;
};

const checkHours = () => {
  const value = Number(hours.value);
  if (!value || value < 1) hours.value = 1;
  if (value > 12) hours.value = 12;
};

const toLocalIso = (date: Date) => new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 19);

const submitOrder = async () => {
  if (!companion.value) return;
  if (!selectedDateTime.value || isSlotDisabled(selectedTime.value)) {
    uni.showToast({ title: '请选择可预约时间', icon: 'none' });
    return;
  }
  if (!customerWechat.value.trim()) {
    uni.showToast({ title: '请填写微信号或手机号', icon: 'none' });
    return;
  }
  if (!address.value.trim()) {
    uni.showToast({ title: '请填写服务地址', icon: 'none' });
    return;
  }

  isSubmitting.value = true;
  try {
    const createRes = await request({
      url: '/orders',
      method: 'POST',
      data: {
        companionId: companion.value.userId,
        serviceId: 1,
        hours: Number(hours.value),
        appointmentTime: toLocalIso(selectedDateTime.value),
        address: address.value.trim(),
        addressDetail: addressDetail.value.trim(),
        customerWechat: customerWechat.value.trim(),
        remark: remark.value.trim()
      }
    });

    if (createRes.code === 200) {
      uni.navigateTo({ url: `/pages/order/pay?orderNo=${createRes.data}&amount=${totalServiceFee.value}` });
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
  padding-bottom: 150rpx;
}

.companion-card, .form-group {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
}

.companion-card {
  padding: 30rpx;
  display: flex;
  align-items: center;

  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: $border-radius-md;
    margin-right: 30rpx;
  }

  .info {
    flex: 1;
    .name-box { display: flex; align-items: center; margin-bottom: 18rpx; }
    .name { font-size: $font-size-lg; font-weight: bold; margin-right: 16rpx; }
    .tag { font-size: 20rpx; color: $color-primary; background: rgba(124, 58, 237, 0.1); padding: 4rpx 12rpx; border-radius: $border-radius-sm; }
    .price { color: $color-secondary; font-weight: bold; }
  }
}

.form-group {
  padding: 0 30rpx 30rpx;

  .section-title {
    padding: 30rpx 0;
    border-bottom: 1rpx solid $border-color-light;
    font-weight: bold;
    color: $text-color-primary;
  }
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;

  &.vertical {
    display: block;
    .label { display: block; width: auto; margin-bottom: 16rpx; }
  }

  .label { width: 180rpx; font-size: $font-size-base; color: $text-color-regular; }
  .value { flex: 1; text-align: right; margin-right: 10rpx; font-size: $font-size-base; }
  .placeholder { color: $text-color-placeholder; }
  .icon-arrow-right { font-size: 24rpx; color: $text-color-secondary; }
}

.std-input, textarea {
  width: 100%;
  background-color: $bg-color-page;
  border-radius: $border-radius-md;
  padding: 0 20rpx;
  box-sizing: border-box;
  font-size: $font-size-sm;
}

.std-input { height: 76rpx; }
textarea { height: 120rpx; padding-top: 18rpx; }

.stepper {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;

  .step-btn {
    width: 60rpx;
    height: 60rpx;
    background-color: $bg-color-page;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: $border-radius-sm;
    font-size: 32rpx;
    font-weight: bold;

    &.disabled { color: $text-color-placeholder; }
  }

  .hour-input {
    width: 90rpx;
    text-align: center;
    font-weight: bold;
  }
}

.unit { margin-left: 20rpx; color: $text-color-regular; }

.cost-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;

  .label { color: $text-color-regular; font-size: $font-size-sm; }
  .value { font-weight: bold; }

  &.total {
    border-top: 1rpx solid $border-color-light;
    margin-top: 10rpx;
    padding-top: 24rpx;

    .highlight { color: $color-secondary; font-size: 36rpx; }
  }
}

.safe-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;

  .icon-safe { color: $color-success; margin-right: 8rpx; }
  text { font-size: 22rpx; color: $text-color-secondary; }
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
    align-items: baseline;
    .label { color: $text-color-primary; }
    .price { color: $color-secondary; font-size: 44rpx; font-weight: bold; }
  }

  .pay-btn {
    width: 280rpx;
    height: 88rpx;
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    margin: 0;

    &::after { display: none; }
  }
}

.time-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.time-panel {
  width: 100%;
  max-height: 78vh;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 28rpx;
  padding-bottom: calc(28rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.panel-title {
  text-align: center;
  font-weight: bold;
  font-size: 32rpx;
  margin-bottom: 22rpx;
}

.date-tabs {
  white-space: nowrap;
  margin-bottom: 24rpx;

  .date-tab {
    display: inline-block;
    padding: 16rpx 24rpx;
    margin-right: 12rpx;
    font-size: 28rpx;
    color: $text-color-regular;
    border-bottom: 4rpx solid transparent;

    &.active {
      color: $color-primary;
      font-weight: bold;
      border-bottom-color: $color-primary;
    }
  }
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14rpx;
  max-height: 560rpx;
  overflow: auto;
}

.slot {
  height: 92rpx;
  border: 1rpx solid $border-color;
  border-radius: $border-radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $text-color-regular;

  text:first-child { font-size: 30rpx; margin-bottom: 4rpx; }
  text:last-child { font-size: 22rpx; }

  &.active {
    background: $gradient-primary;
    color: #fff;
    border-color: transparent;
  }

  &.disabled {
    background: #F3F4F6;
    color: $text-color-placeholder;
  }
}

.panel-confirm {
  margin-top: 24rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: $gradient-primary;
  color: #fff;
  border-radius: $border-radius-pill;
  font-weight: bold;
}
</style>
