<template>
  <view class="container">
    <view class="tabs">
      <view class="tab" :class="{ active: currentTab === '' }" @click="switchTab('')" hover-class="tab-hover">
        {{ t('mine.allOrders') }}
      </view>
      <view class="tab" :class="{ active: currentTab === '10' }" @click="switchTab('10')" hover-class="tab-hover">
        {{ t('mine.waitingPay') }}
      </view>
      <view class="tab" :class="{ active: currentTab === '20' }" @click="switchTab('20')" hover-class="tab-hover">
        {{ t('mine.waitingAccept') }}
      </view>
      <view class="tab" :class="{ active: currentTab === '50' }" @click="switchTab('50')" hover-class="tab-hover">
        {{ t('mine.inProgress') }}
      </view>
      <view class="tab" :class="{ active: currentTab === '80' }" @click="switchTab('80')" hover-class="tab-hover">
        {{ t('order.status.210') }}
      </view>
    </view>

    <scroll-view scroll-y class="list-content" @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh">
      <view class="order-list" v-if="orderList.length > 0">
        <view class="order-card" v-for="item in orderList" :key="item.orderNo" @click="goToDetail(item.orderNo)" hover-class="card-hover">
          <view class="card-header">
            <text class="order-no">No. {{ item.orderNo }}</text>
            <text class="status" :class="'status-' + item.status">{{ getStatusText(item.status) }}</text>
          </view>

          <view class="card-body">
            <view class="service-info">
              <view class="title">{{ t('lobby.title') }}{{ t('detail.orderLabel') }}</view>
              <view class="time">{{ t('order.time') }}: {{ item.reserveDate }} {{ item.reserveTimeStart }}{{ item.reserveTimeEnd ? '-' + item.reserveTimeEnd : '' }}</view>
              <view class="duration">
                {{ t('order.duration') }}: {{ item.hours }} {{ appStore.locale === 'en' ? 'Hrs' : '小时' }}
              </view>
              <view class="duration" v-if="item.address">{{ t('order.reserveAddress') }}: {{ item.address }}</view>
            </view>
            <view class="price-info">
              <text class="label">{{ t('order.amount') }}</text>
              <text class="price">¥{{ item.totalAmount }}</text>
            </view>
          </view>

          <view class="card-footer" v-if="hasActions(item.status)">
            <button class="action-btn" v-if="item.status === 10" @click.stop="goToPay(item)" hover-class="button-hover">{{ t('mine.waitingPay') }}</button>
            <button class="action-btn" v-if="canRefund(item.status)" @click.stop="goToRefund(item.orderNo)" hover-class="button-hover">{{ t('order.actionRefund') }}</button>
            <button class="action-btn primary" v-if="item.status === 60" @click.stop="confirmOrder(item.orderNo)" hover-class="button-hover">{{ t('order.actionConfirm') }}</button>
            <button class="action-btn primary" v-if="item.status === 70 || item.status === 80" @click.stop="goToReview(item.orderNo)" hover-class="button-hover">{{ t('order.actionReview') }}</button>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else-if="!loading">
        <text>{{ t('lobby.emptyRequests') }}</text>
      </view>

      <view class="loading-state" v-if="loading && orderList.length > 0">
        <text>{{ t('common.loading') }}</text>
      </view>
      <view class="loading-state" v-if="noMore && orderList.length > 0">
        <text>{{ t('common.noMore') }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAppStore } from '../../store/app';
import { t } from '../../utils/i18n';

const appStore = useAppStore();
const orderList = ref<any[]>([]);
const currentTab = ref('');
const current = ref(1);
const size = ref(10);
const loading = ref(false);
const noMore = ref(false);
const isRefreshing = ref(false);

onLoad((options: any) => {
  if (options?.status) currentTab.value = options.status;
});

onShow(() => {
  fetchList(true);
});

const switchTab = (tab: string) => {
  currentTab.value = tab;
  fetchList(true);
};

const fetchList = async (reset = false) => {
  if (reset) {
    current.value = 1;
    noMore.value = false;
  }
  if (noMore.value || loading.value) return;

  loading.value = true;
  try {
    const res = await request({
      url: `/orders?current=${current.value}&size=${size.value}${currentTab.value ? '&status=' + currentTab.value : ''}`,
      method: 'GET'
    });
    
    if (res.code === 200) {
      if (reset) {
        orderList.value = res.data.records;
      } else {
        orderList.value = [...orderList.value, ...res.data.records];
      }
      
      if (res.data.records.length < size.value) {
        noMore.value = true;
      } else {
        current.value++;
      }
    }
  } finally {
    loading.value = false;
    isRefreshing.value = false;
  }
};

const onRefresh = () => {
  isRefreshing.value = true;
  fetchList(true);
};

const loadMore = () => {
  fetchList();
};

const getStatusText = (status: number) => {
  const text = t(`order.status.${status}`);
  return text.includes('order.status.') ? t('common.status') : text;
};

const goToDetail = (orderNo: string) => {
  uni.navigateTo({ url: `/pages/order/detail?orderNo=${orderNo}` });
};

const goToPay = (item: any) => {
  uni.navigateTo({ url: `/pages/order/pay?orderNo=${item.orderNo}&amount=${item.totalAmount}` });
};

const confirmOrder = (orderNo: string) => {
  uni.showModal({
    title: t('order.actionConfirm'),
    content: appStore.locale === 'en' ? 'Are you sure this service is completed?' : '确认服务已经完成了吗？确认后平台将进入结算流程。',
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    success: async (res) => {
      if (res.confirm) {
        const confirmRes = await request({
          url: `/orders/${orderNo}/confirm`,
          method: 'PUT'
        });
        if (confirmRes.code === 200) {
          uni.showToast({ title: t('common.success') });
          fetchList(true);
        }
      }
    }
  });
};

const goToReview = (orderNo: string) => {
  uni.navigateTo({ url: `/pages/order/review?orderNo=${orderNo}` });
};

const goToRefund = (orderNo: string) => {
  uni.navigateTo({ url: `/pages/order/refund?orderNo=${orderNo}` });
};

const canRefund = (status: number) => [20, 30, 40, 50, 60].includes(status);
const hasActions = (status: number) => status === 10 || status === 60 || status === 70 || status === 80 || canRefund(status);
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-color-page;
}

.tabs {
  display: flex;
  background-color: $bg-color-white;
  padding: 0 10rpx;
  z-index: $z-index-sticky;
  border-bottom: 1px solid $border-color-light;
  
  .tab {
    flex: 1;
    text-align: center;
    padding: 28rpx 0;
    font-size: $font-size-sm;
    color: $text-color-regular;
    position: relative;
    transition: all 0.2s ease;
    
    &.active {
      color: $color-primary;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 36rpx;
        height: 6rpx;
        background: $gradient-primary;
        border-radius: 3rpx;
      }
    }
  }

  .tab-hover {
    opacity: 0.7;
  }
}

.list-content {
  flex: 1;
  overflow: hidden;
}

.order-list {
  padding: 24rpx;
}

.order-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: $box-shadow-sm;
  border: 1px solid $border-color-light;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  
  &.card-hover {
    transform: scale(0.995);
    box-shadow: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid $border-color-light;
    padding-bottom: 20rpx;
    margin-bottom: 20rpx;
    
    .order-no { 
      font-size: $font-size-sm; 
      color: $text-color-secondary;
      font-family: monospace; 
    }
    
    .status {
      font-size: $font-size-sm;
      font-weight: bold;
      
      &.status-10 { color: $color-error; }
      &.status-20 { color: $color-warning; }
      &.status-30 { color: $color-primary; }
      &.status-40, &.status-50 { color: $color-success; }
      &.status-80, &.status-210 { color: $text-color-secondary; }
    }
  }
  
  .card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .service-info {
      .title { font-size: $font-size-base; font-weight: bold; color: $text-color-primary; margin-bottom: 12rpx; }
      .time, .duration { font-size: $font-size-sm; color: $text-color-regular; margin-bottom: 8rpx; }
    }
    
    .price-info {
      text-align: right;
      
      .label { font-size: $font-size-xs; color: $text-color-secondary; display: block; margin-bottom: 6rpx; }
      .price { 
        font-size: 34rpx; 
        font-weight: bold; 
        color: $text-color-primary;
        font-family: 'Outfit', -apple-system, sans-serif;
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 30rpx;
    
    .action-btn {
      margin: 0;
      margin-left: 20rpx;
      padding: 0 32rpx;
      height: 64rpx;
      line-height: 62rpx;
      border-radius: $border-radius-pill;
      font-size: $font-size-sm;
      background-color: rgba(255, 255, 255, 0.05);
      color: $text-color-primary;
      border: 1px solid $border-color;
      box-sizing: border-box;
      transition: all 0.1s ease;
      
      &::after { display: none; }
      
      &.button-hover {
        transform: scale(0.97);
        opacity: 0.9;
      }
      
      &.primary {
        background: $gradient-primary;
        color: #fff;
        border: none;
        line-height: 64rpx;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 240rpx;
  color: $text-color-secondary;
  font-size: $font-size-base;
}

.loading-state {
  text-align: center;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: $text-color-secondary;
}

/* 深色模式按钮及背景专项微调 */
.theme-dark {
  .action-btn {
    background-color: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
    
    &.primary {
      background: $gradient-primary;
      border: none;
      box-shadow: 0 6px 20px rgba(255, 59, 92, 0.25);
    }
  }
}
</style>
