<template>
  <view class="container">
    <view class="tabs">
      <view class="tab" :class="{ active: currentTab === '' }" @click="switchTab('')">全部</view>
      <view class="tab" :class="{ active: currentTab === '10' }" @click="switchTab('10')">待支付</view>
      <view class="tab" :class="{ active: currentTab === '20' }" @click="switchTab('20')">待接单</view>
      <view class="tab" :class="{ active: currentTab === '30' }" @click="switchTab('30')">进行中</view>
      <view class="tab" :class="{ active: currentTab === '70' }" @click="switchTab('70')">待评价</view>
    </view>

    <scroll-view scroll-y class="list-content" @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh">
      <view class="order-list" v-if="orderList.length > 0">
        <view class="order-card" v-for="item in orderList" :key="item.orderNo" @click="goToDetail(item.orderNo)">
          <view class="card-header">
            <text class="order-no">订单号: {{ item.orderNo }}</text>
            <text class="status" :class="'status-' + item.status">{{ getStatusText(item.status) }}</text>
          </view>

          <view class="card-body">
            <view class="service-info">
              <view class="title">同城伴玩服务</view>
              <view class="time">预约: {{ item.reserveDate }} {{ item.reserveTimeStart }}</view>
              <view class="duration">时长: {{ item.hours }} 小时</view>
            </view>
            <view class="price-info">
              <text class="label">实付款</text>
              <text class="price">¥{{ item.totalAmount }}</text>
            </view>
          </view>

          <view class="card-footer" v-if="item.status === 10 || item.status === 30 || item.status === 70">
            <button class="action-btn" v-if="item.status === 10" @click.stop="goToPay(item)">去支付</button>
            <button class="action-btn primary" v-if="item.status === 30" @click.stop="confirmOrder(item.orderNo)">确认完工</button>
            <button class="action-btn" v-if="item.status === 70" @click.stop="goToReview(item.orderNo)">去评价</button>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else-if="!loading">
        <text>暂无订单记录</text>
      </view>

      <view class="loading-state" v-if="loading && orderList.length > 0">
        <text>加载中...</text>
      </view>
      <view class="loading-state" v-if="noMore && orderList.length > 0">
        <text>没有更多数据了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {request} from '../../utils/request';

const orderList = ref<any[]>([]);
const currentTab = ref('');
const current = ref(1);
const size = ref(10);
const loading = ref(false);
const noMore = ref(false);
const isRefreshing = ref(false);

onLoad((options: any) => {
  if (options?.status) currentTab.value = options.status;
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
  const map: Record<number, string> = {
    10: '待付款', 20: '待接单', 30: '已接单', 40: '已确认',
    50: '服务中', 60: '待确认', 70: '待评价', 80: '已完成',
    100: '取消中', 120: '已退款', 250: '已关闭'
  };
  return map[status] || '未知';
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 16);
};

const goToDetail = (orderNo: string) => {
  uni.navigateTo({ url: `/pages/order/detail?orderNo=${orderNo}` });
};

const goToPay = (item: any) => {
  uni.navigateTo({ url: `/pages/order/pay?orderNo=${item.orderNo}&amount=${item.totalAmount}` });
};

const confirmOrder = (orderNo: string) => {
  uni.showModal({
    title: '确认完工',
    content: '确认服务已经完成了吗？确认后款项将打给搭子。',
    success: async (res) => {
      if (res.confirm) {
        const confirmRes = await request({
          url: `/orders/${orderNo}/confirm`,
          method: 'PUT'
        });
        if (confirmRes.code === 200) {
          uni.showToast({ title: '确认成功' });
          fetchList(true);
        }
      }
    }
  });
};

const goToReview = (orderNo: string) => {
  uni.navigateTo({ url: `/pages/order/review?orderNo=${orderNo}` });
};
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-color-page;
}

.tabs {
  display: flex;
  background-color: $bg-color-white;
  padding: 0 20rpx;
  z-index: $z-index-sticky;
  
  .tab {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: $font-size-base;
    color: $text-color-regular;
    position: relative;
    
    &.active {
      color: $color-primary;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 6rpx;
        background: $gradient-primary;
        border-radius: 4rpx;
      }
    }
  }
}

.list-content {
  flex: 1;
  overflow: hidden;
}

.order-list {
  padding: 20rpx;
}

.order-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1rpx solid $border-color-light;
    padding-bottom: 20rpx;
    margin-bottom: 20rpx;
    
    .order-no { font-size: $font-size-sm; color: $text-color-secondary; }
    
    .status {
      font-size: $font-size-sm;
      font-weight: bold;
      
      &.status-10 { color: $color-error; }
      &.status-20 { color: $color-warning; }
      &.status-30 { color: $color-primary; }
      &.status-40, &.status-50 { color: $color-success; }
      &.status-90 { color: $text-color-secondary; }
    }
  }
  
  .card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .service-info {
      .title { font-size: $font-size-base; font-weight: bold; color: $text-color-primary; margin-bottom: 10rpx; }
      .time, .duration { font-size: $font-size-sm; color: $text-color-regular; margin-bottom: 6rpx; }
    }
    
    .price-info {
      text-align: right;
      
      .label { font-size: $font-size-sm; color: $text-color-secondary; display: block; margin-bottom: 6rpx; }
      .price { font-size: 36rpx; font-weight: bold; color: $text-color-primary; }
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
      height: 60rpx;
      line-height: 60rpx;
      border-radius: $border-radius-pill;
      font-size: $font-size-sm;
      background-color: $bg-color-white;
      color: $text-color-regular;
      border: 1rpx solid $border-color;
      
      &::after { display: none; }
      
      &.primary {
        background: $gradient-primary;
        color: #fff;
        border: none;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
  color: $text-color-secondary;
  
  .icon-empty { font-size: 120rpx; margin-bottom: 20rpx; color: $border-color; }
}

.loading-state {
  text-align: center;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: $text-color-secondary;
}
</style>
