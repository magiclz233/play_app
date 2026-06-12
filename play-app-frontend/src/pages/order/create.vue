<template>
  <view class="container" v-if="companion">
    <view class="companion-card">
      <image :src="coverUrl" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="name-box">
          <text class="name">{{ companion.nickname }}</text>
          <text class="tag">{{ getGenderText(companion.gender) }} {{ t('detail.ageUnit', { age: companion.age }) }}</text>
        </view>
        <text class="price">¥{{ companion.pricePerHour }}/{{ appStore.locale === 'en' ? 'hr' : '小时' }}</text>
      </view>
    </view>

    <view class="form-group">
      <view class="section-title">{{ t('order.bookingInfo') }}</view>
      <view class="form-item" @click="showTimePicker = true" hover-class="item-hover">
        <text class="label">{{ t('order.time') }}</text>
        <view class="value" :class="{ placeholder: !selectedTime }">{{ formattedTime || t('order.selectTimePlaceholder') }}</view>
        <text class="iconfont icon-arrow-right"></text>
      </view>

      <view class="form-item">
        <text class="label">{{ t('order.duration') }}</text>
        <view class="stepper">
          <view class="step-btn" :class="{ disabled: hours <= 1 }" @click="changeHours(-1)" hover-class="button-hover">-</view>
          <input type="number" v-model="hours" class="hour-input" @blur="checkHours" />
          <view class="step-btn" @click="changeHours(1)" hover-class="button-hover">+</view>
        </view>
        <text class="unit">{{ appStore.locale === 'en' ? 'Hrs' : '小时' }}</text>
      </view>
    </view>

    <view class="form-group">
      <view class="section-title">{{ t('order.contactInfo') }}</view>
      <view class="form-item vertical">
        <text class="label">{{ t('order.contactId') }}</text>
        <input class="std-input" v-model="customerWechat" :placeholder="t('order.contactPlaceholder')" maxlength="50" placeholder-class="std-placeholder" />
      </view>
      <view class="form-item vertical">
        <text class="label">{{ t('order.reserveAddress') }}</text>
        <input class="std-input" v-model="address" :placeholder="t('order.addressPlaceholder')" maxlength="255" placeholder-class="std-placeholder" />
      </view>
      <view class="form-item vertical">
        <text class="label">{{ t('order.addressDetail') }}</text>
        <input class="std-input" v-model="addressDetail" :placeholder="t('order.addressDetailPlaceholder')" maxlength="255" placeholder-class="std-placeholder" />
      </view>
      <view class="form-item vertical">
        <text class="label">{{ t('detail.remark') }}</text>
        <textarea v-model="remark" :placeholder="t('order.remarkPlaceholder')" maxlength="100" placeholder-class="std-placeholder"></textarea>
      </view>
    </view>

    <view class="form-group">
      <view class="section-title">{{ t('order.billingSummary') }}</view>
      <view class="cost-row">
        <text class="label">{{ serviceFeeLabel }} (¥{{ companion.pricePerHour }} x {{ hours }}{{ serviceHourUnit }})</text>
        <text class="value">¥{{ totalServiceFee }}</text>
      </view>
      <view class="cost-row">
        <text class="label">{{ t('order.platformFee') }}</text>
        <text class="value">¥{{ orderResult ? orderResult.platformFee.toFixed(2) : (totalServiceFee * 0.05).toFixed(2) }}</text>
      </view>
      <view class="cost-row">
        <text class="label">{{ t('order.companionIncome') }}</text>
        <text class="value" style="color: var(--color-success)">¥{{ orderResult ? orderResult.companionAmount.toFixed(2) : (totalServiceFee * 0.95).toFixed(2) }}</text>
      </view>
      <view class="cost-row total">
        <text class="label">{{ t('order.total') }}</text>
        <text class="value highlight">¥{{ orderResult ? orderResult.totalAmount.toFixed(2) : totalServiceFee }}</text>
      </view>
    </view>

    <view class="safe-notice">
      <text class="iconfont icon-safe"></text>
      <text>{{ t('order.safeNotice') }}</text>
    </view>

    <view class="bottom-bar">
      <view class="total-box">
        <text class="label">{{ t('order.totalLabel') }}</text>
        <text class="price">¥{{ orderResult ? orderResult.totalAmount.toFixed(2) : totalServiceFee }}</text>
      </view>
      <button class="pay-btn" @click="submitOrder" :loading="isSubmitting" hover-class="button-hover">
        {{ t('order.payNow') }}
      </button>
    </view>

    <view class="time-mask" v-if="showTimePicker" @click="showTimePicker = false">
      <view class="time-panel" @click.stop>
        <view class="panel-title">{{ t('order.selectServiceTime') }}</view>
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
            <text class="slot-time">{{ slot }}</text>
            <text class="slot-status">{{ isSlotDisabled(slot) ? t('order.slotFull') : t('order.slotOpen') }}</text>
          </view>
        </view>
        <button class="panel-confirm" @click="confirmTime" hover-class="button-hover">{{ t('common.confirm') }}</button>
      </view>
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
const companion = ref<any>(null);
const hours = ref(3);
const serviceFeeLabel = computed(() => {
  return appStore.locale === 'en' ? 'Service Fee' : '服务费';
});
const serviceHourUnit = computed(() => {
  return appStore.locale === 'en' ? 'hrs' : '时';
});
const remark = ref('');
const address = ref('');
const addressDetail = ref('');
const customerWechat = ref('');
const isSubmitting = ref(false);
const showTimePicker = ref(false);
const orderResult = ref<{ orderNo: string; totalAmount: number; platformFee: number; companionAmount: number } | null>(null);

const pad = (num: number) => String(num).padStart(2, '0');
const formatDateValue = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const getGenderText = (gender: number) => {
  if (gender === 1) return appStore.locale === 'en' ? 'Male' : '男';
  return appStore.locale === 'en' ? 'Female' : '女';
};

// 动态计算未来5天日期，包含中英文星期名
const dateOptions = computed(() => {
  const weekMap: Record<string, Record<number, string>> = {
    'zh-Hans': { 0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六' },
    'zh-Hant': { 0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六' },
    'en': { 0: 'Sun', 1: 'Mon', 2: 'Tue', 3: 'Wed', 4: 'Thu', 5: 'Fri', 6: 'Sat' }
  };
  const currentLocale = appStore.locale;
  const weeks = weekMap[currentLocale] || weekMap['zh-Hans'];

  return Array.from({ length: 5 }).map((_, index) => {
    const date = new Date();
    date.setDate(date.getDate() + index);
    const weekLabel = weeks[date.getDay()];
    return {
      value: formatDateValue(date),
      label: `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${weekLabel}`
    };
  });
});

const timeSlots = ['10:00', '10:30', '11:00', '11:30', '12:00', '12:30', '13:00', '13:30', '14:00', '14:30', '15:00', '15:30', '16:00', '16:30', '17:00', '17:30', '18:00', '18:30', '19:00', '19:30', '20:00', '20:30', '21:00', '21:30', '22:00', '22:30', '23:00'];
const selectedDate = ref(formatDateValue(new Date()));
const selectedTime = ref('');

const coverUrl = computed(() => companion.value?.photoUrls?.[0] || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600&auto=format&fit=crop');
const selectedDateTime = computed(() => selectedTime.value ? new Date(`${selectedDate.value}T${selectedTime.value}:00`) : null);
const formattedTime = computed(() => {
  if (!selectedDateTime.value) return '';
  const d = selectedDateTime.value;
  if (appStore.locale === 'en') {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${months[d.getMonth()]} ${pad(d.getDate())}, ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
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
    uni.showToast({ title: t('order.selectAvailableTime'), icon: 'none' });
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
    uni.showToast({ title: t('order.selectAvailableTime'), icon: 'none' });
    return;
  }
  if (!customerWechat.value.trim()) {
    uni.showToast({ title: t('order.enterContact'), icon: 'none' });
    return;
  }
  if (!address.value.trim()) {
    uni.showToast({ title: t('order.enterAddress'), icon: 'none' });
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
      const result = createRes.data as { orderNo: string; totalAmount: number; platformFee: number; companionAmount: number };
      orderResult.value = result;
      uni.navigateTo({ url: `/pages/order/pay?orderNo=${result.orderNo}&amount=${result.totalAmount}` });
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
  padding-bottom: 160rpx;
  box-sizing: border-box;
}

.companion-card, .form-group {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  margin-bottom: 24rpx;
  box-shadow: $box-shadow-sm;
  border: 1px solid $border-color-light;
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
    .name-box { display: flex; align-items: center; margin-bottom: 12rpx; }
    .name { font-size: $font-size-lg; font-weight: bold; margin-right: 16rpx; color: $text-color-primary; }
    .tag { 
      font-size: 20rpx; 
      color: $color-primary; 
      background: rgba(255, 59, 92, 0.08); 
      padding: 4rpx 14rpx; 
      border-radius: $border-radius-pill;
      font-weight: 500;
    }
    .price { 
      color: $color-primary; 
      font-weight: bold; 
      font-size: $font-size-base;
      font-family: 'Outfit', sans-serif;
    }
  }
}

.form-group {
  padding: 0 30rpx 30rpx;

  .section-title {
    padding: 30rpx 0;
    border-bottom: 1px solid $border-color-light;
    font-weight: bold;
    color: $text-color-primary;
    font-size: $font-size-base;
  }
}

.form-item {
  display: flex;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.02);

  &:last-child {
    border-bottom: none;
  }

  &.vertical {
    display: block;
    border-bottom: none;
    .label { display: block; width: auto; margin-bottom: 16rpx; font-weight: bold; }
  }

  .label { width: 180rpx; font-size: $font-size-base; color: $text-color-primary; }
  .value { 
    flex: 1; 
    text-align: right; 
    margin-right: 10rpx; 
    font-size: $font-size-base;
    color: $text-color-regular;
  }
  .placeholder { color: $text-color-secondary; }
  .icon-arrow-right { font-size: 24rpx; color: $text-color-secondary; }
}

.std-input, textarea {
  width: 100%;
  background-color: $bg-color-page;
  border-radius: $border-radius-md;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: $font-size-sm;
  color: $text-color-primary;
  border: 1px solid $border-color-light;
}

.std-placeholder {
  color: $text-color-secondary;
}

.std-input { height: 84rpx; }
textarea { height: 140rpx; padding-top: 20rpx; }

.stepper {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;

  .step-btn {
    width: 64rpx;
    height: 64rpx;
    background-color: $bg-color-page;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: $border-radius-sm;
    font-size: 32rpx;
    font-weight: bold;
    color: $text-color-primary;
    border: 1px solid $border-color-light;

    &.disabled { color: $text-color-secondary; opacity: 0.6; }
  }

  .hour-input {
    width: 90rpx;
    text-align: center;
    font-weight: bold;
    color: $text-color-primary;
    font-family: 'Outfit', sans-serif;
  }
}

.unit { margin-left: 20rpx; color: $text-color-regular; }

.cost-row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  font-size: $font-size-sm;

  .label { color: $text-color-regular; }
  .value { 
    font-weight: bold; 
    color: $text-color-primary;
    font-family: 'Outfit', sans-serif;
  }

  &.total {
    border-top: 1px solid $border-color-light;
    margin-top: 10rpx;
    padding-top: 28rpx;

    .highlight { 
      color: $color-primary; 
      font-size: 38rpx; 
      font-weight: 800;
    }
  }
}

.safe-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx 20rpx;

  .icon-safe { color: $color-success; margin-right: 12rpx; font-size: 28rpx; }
  text { font-size: 22rpx; color: $text-color-secondary; line-height: 1.4; }
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
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.04);
  z-index: $z-index-sticky;
  border-top: 1px solid $border-color-light;

  .total-box {
    display: flex;
    align-items: baseline;
    .label { color: $text-color-primary; font-size: $font-size-sm; }
    .price { 
      color: $color-primary; 
      font-size: 46rpx; 
      font-weight: 800;
      font-family: 'Outfit', sans-serif;
    }
  }

  .pay-btn {
    width: 280rpx;
    height: 84rpx;
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 30rpx;
    font-weight: bold;
    border: none;
    margin: 0;
    line-height: 84rpx;

    &::after { display: none; }
  }
}

.time-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: $z-index-modal;
  display: flex;
  align-items: flex-end;
}

.time-panel {
  width: 100%;
  max-height: 80vh;
  background: var(--bg-card);
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx 30rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.panel-title {
  text-align: center;
  font-weight: bold;
  font-size: 32rpx;
  margin-bottom: 30rpx;
  color: $text-color-primary;
}

.date-tabs {
  white-space: nowrap;
  margin-bottom: 30rpx;

  .date-tab {
    display: inline-block;
    padding: 16rpx 28rpx;
    margin-right: 16rpx;
    font-size: 26rpx;
    color: $text-color-regular;
    border-bottom: 6rpx solid transparent;
    transition: all 0.2s ease;

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
  gap: 16rpx;
  max-height: 480rpx;
  overflow: auto;
  padding-bottom: 20rpx;
}

.slot {
  height: 96rpx;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $text-color-primary;
  background-color: $bg-color-page;
  transition: all 0.1s ease;

  .slot-time { font-size: 28rpx; margin-bottom: 4rpx; font-weight: bold; font-family: 'Outfit', sans-serif; }
  .slot-status { font-size: 20rpx; opacity: 0.8; }

  &.active {
    background: $gradient-primary;
    color: #fff;
    border-color: transparent;
  }

  &.disabled {
    background: rgba(0, 0, 0, 0.04);
    color: $text-color-secondary;
    opacity: 0.5;
    border-color: transparent;
  }
}

.panel-confirm {
  margin-top: 30rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: $gradient-primary;
  color: #fff;
  border-radius: $border-radius-pill;
  font-weight: bold;
  font-size: 30rpx;
}

/* 深色模式特异优化 */
.theme-dark {
  .time-panel {
    background-color: var(--bg-card);
  }
  .slot {
    border-color: rgba(255, 255, 255, 0.05);
    background-color: rgba(255, 255, 255, 0.02);
    &.disabled {
      background: rgba(255, 255, 255, 0.03);
      color: var(--text-muted);
    }
    &.active {
      background: $gradient-primary;
      color: #fff;
    }
  }
  .pay-btn, .panel-confirm {
    box-shadow: 0 6px 20px rgba(255, 59, 92, 0.25);
  }
}
</style>
