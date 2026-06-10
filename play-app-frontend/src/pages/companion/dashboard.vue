<template>
  <view class="container">
    <view class="header-bg"></view>
    
    <!-- 顶部数据概览 -->
    <view class="stats-card">
      <view class="stats-row">
        <view class="stats-item" @click="goToWallet">
          <text class="label">账户余额 (元)</text>
          <text class="value">{{ stats.balance || '0.00' }}</text>
        </view>
        <view class="divider"></view>
        <view class="stats-item">
          <text class="label">累计收入 (元)</text>
          <text class="value">{{ stats.totalIncome || '0.00' }}</text>
        </view>
        <view class="divider"></view>
        <view class="stats-item">
          <text class="label">总接单数</text>
          <text class="value">{{ stats.totalOrderCount || 0 }}</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-actions">
      <view class="action-item" @click="goToProfileEdit">
        <text class="action-icon">✏️</text>
        <text class="action-text">编辑资料</text>
      </view>
      <view class="action-item" @click="goToWallet">
        <text class="action-icon">💰</text>
        <text class="action-text">我的钱包</text>
      </view>
    </view>

    <!-- 订单管理 -->
    <view class="section">
      <view class="section-header">
        <text class="title">接单管理</text>
        <view class="tabs">
          <text class="tab" :class="{ active: currentTab === '' }" @click="switchTab('')">全部</text>
          <text class="tab" :class="{ active: currentTab === '20' }" @click="switchTab('20')">新订单</text>
          <text class="tab" :class="{ active: currentTab === '30' }" @click="switchTab('30')">服务中</text>
        </view>
      </view>

      <scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
        <view class="order-card" v-for="item in orderList" :key="item.orderNo">
          <view class="card-top">
            <text class="time">预约日期: {{ item.reserveDate }} {{ item.reserveTimeStart }}</text>
            <text class="status" :class="'status-' + item.status">{{ getStatusText(item.status) }}</text>
          </view>

          <view class="card-mid">
            <view class="detail">
              <text class="label">服务时长:</text>
              <text class="val">{{ item.hours }} 小时</text>
            </view>
            <view class="detail">
              <text class="label">订单收益:</text>
              <text class="val price">¥{{ item.totalAmount }}</text>
            </view>
            <view class="detail" v-if="item.customerRemark">
              <text class="label">客户备注:</text>
              <text class="val">{{ item.customerRemark }}</text>
            </view>
          </view>

          <view class="card-bottom" v-if="item.status === 20">
            <button class="btn primary" @click="takeOrder(item)">接单</button>
            <button class="btn danger" @click="rejectOrder(item)">拒单</button>
          </view>
          <view class="card-bottom" v-if="item.status === 50">
            <button class="btn primary" @click="requestFinish(item)">发起完工</button>
          </view>
        </view>
        
        <EmptyState v-if="orderList.length === 0 && !loading" text="暂无订单" hint="完成服务后订单将在此展示" />
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {request} from '../../utils/request';
import {useAppStore} from '../../store/app';
import EmptyState from '../../components/EmptyState.vue';

const appStore = useAppStore();
const stats = ref<any>({});
const orderList = ref<any[]>([]);
const currentTab = ref('');
const current = ref(1);
const size = ref(10);
const loading = ref(false);
const noMore = ref(false);

onMounted(() => {
  fetchStats();
  fetchOrders(true);
});

const fetchStats = async () => {
  const res = await request({ url: '/companion/dashboard', method: 'GET' });
  if (res.code === 200) {
    stats.value = res.data;
  }
};

const switchTab = (tab: string) => {
  currentTab.value = tab;
  fetchOrders(true);
};

const fetchOrders = async (reset = false) => {
  if (reset) { current.value = 1; noMore.value = false; }
  if (noMore.value || loading.value) return;

  loading.value = true;
  try {
    const res = await request({
      url: `/companion/orders?current=${current.value}&size=${size.value}${currentTab.value ? '&status=' + currentTab.value : ''}`,
      method: 'GET'
    });

    if (res.code === 200) {
      if (reset) orderList.value = res.data.records;
      else orderList.value = [...orderList.value, ...res.data.records];

      if (res.data.records.length < size.value) noMore.value = true;
      else current.value++;
    }
  } finally {
    loading.value = false;
  }
};

const loadMore = () => fetchOrders();

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    10: '待付款', 20: '待接单', 30: '已接单/待服务', 40: '已确认', 50: '服务中',
    60: '已完工/待确认', 70: '用户已确认', 80: '已完成', 100: '申请取消', 120: '已退款', 250: '已关闭'
  };
  return map[status] || '未知';
};

const formatTime = (timeStr: string) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : '';

const goToWallet = () => {
  uni.navigateTo({ url: '/pages/companion/wallet' });
};

const goToProfileEdit = () => {
  uni.navigateTo({ url: '/pages/companion/profile-edit' });
};

const takeOrder = async (item: any) => {
  try {
    const res = await request({ url: `/companion/orders/${item.orderNo}/accept`, method: 'PUT' });
    if (res.code === 200) {
      uni.showToast({ title: '已接单', icon: 'success' });
      fetchOrders(true);
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const rejectOrder = async (item: any) => {
  uni.showModal({
    title: '确认拒单',
    content: '确定要拒绝此订单吗？',
    success: async (modalRes: any) => {
      if (modalRes.confirm) {
        try {
          const res = await request({ url: `/companion/orders/${item.orderNo}/reject`, method: 'PUT', data: { reason: '助教拒单' } });
          if (res.code === 200) {
            uni.showToast({ title: '已拒单', icon: 'success' });
            fetchOrders(true);
          }
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' });
        }
      }
    }
  });
};

const requestFinish = (item: any) => {
  uni.showModal({
    title: '发起完工',
    content: '确认服务已完成，发起完工申请？',
    success: async (modalRes: any) => {
      if (modalRes.confirm) {
        try {
          const res = await request({
            url: `/companion/orders/${item.orderNo}/request-finish`,
            method: 'PUT',
            data: { finishRemark: '陪玩申请完工', finishType: 1 }
          });
          if (res.code === 200) {
            uni.showToast({ title: '已发起完工', icon: 'success' });
            fetchOrders(true);
          }
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' });
        }
      }
    }
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 300rpx;
  background: $gradient-primary;
  z-index: 0;
  border-radius: 0 0 40rpx 40rpx;
}

.stats-card {
  position: relative;
  z-index: 1;
  background-color: $bg-color-white;
  margin: 40rpx 30rpx;
  border-radius: $border-radius-lg;
  padding: 40rpx 20rpx;
  box-shadow: $box-shadow-sm;
  
  .stats-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .stats-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .label { font-size: 24rpx; color: $text-color-secondary; margin-bottom: 12rpx; }
      .value { font-size: 40rpx; font-weight: bold; color: $text-color-primary; }
    }
    
    .divider {
      width: 2rpx;
      height: 60rpx;
      background-color: $border-color-light;
    }
  }
}

.quick-actions {
  position: relative;
  z-index: 1;
  display: flex;
  margin: 0 30rpx 20rpx;
  gap: 20rpx;

  .action-item {
    flex: 1;
    background-color: $bg-color-white;
    border-radius: $border-radius-md;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: $box-shadow-sm;
  }
  .action-icon { font-size: 36rpx; margin-bottom: 8rpx; }
  .action-text { font-size: 24rpx; color: $text-color-regular; }
}

.section {
  position: relative;
  z-index: 1;
  background-color: $bg-color-white;
  margin: 0 30rpx;
  border-radius: $border-radius-lg;
  height: calc(100vh - 300rpx);
  display: flex;
  flex-direction: column;
  
  .section-header {
    padding: 30rpx;
    border-bottom: 1rpx solid $border-color-light;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title { font-size: $font-size-lg; font-weight: bold; color: $text-color-primary; }
    
    .tabs {
      display: flex;
      
      .tab {
        font-size: $font-size-sm;
        color: $text-color-regular;
        margin-left: 30rpx;
        position: relative;
        
        &.active {
          color: $color-primary;
          font-weight: bold;
          &::after {
            content: ''; position: absolute; bottom: -10rpx; left: 50%; transform: translateX(-50%);
            width: 20rpx; height: 4rpx; background-color: $color-primary; border-radius: 2rpx;
          }
        }
      }
    }
  }
  
  .order-list {
    flex: 1;
    padding: 20rpx;
    box-sizing: border-box;
    
    .order-card {
      background-color: var(--bg-main);
      border-radius: $border-radius-md;
      padding: 24rpx;
      margin-bottom: 20rpx;
      
      .card-top {
        display: flex; justify-content: space-between; align-items: center;
        margin-bottom: 16rpx;
        
        .time { font-size: 24rpx; color: $text-color-regular; }
        .status { font-size: 24rpx; font-weight: bold; color: $color-primary; }
      }
      
      .card-mid {
        .detail {
          display: flex; margin-bottom: 8rpx;
          .label { width: 120rpx; font-size: 24rpx; color: $text-color-secondary; }
          .val { flex: 1; font-size: 24rpx; color: $text-color-primary; }
          .val.price { font-weight: bold; color: $color-secondary; }
        }
      }
      
      .card-bottom {
        display: flex; justify-content: flex-end; margin-top: 20rpx;
        
        .btn {
          margin: 0; padding: 0 32rpx; height: 56rpx; line-height: 56rpx;
          font-size: 24rpx; border-radius: $border-radius-pill;
          
          &.primary { background: $gradient-primary; color: #fff; }
        }
      }
    }
  }
}

.empty-state { text-align: center; padding: 100rpx 0; color: $text-color-placeholder; font-size: 28rpx; }
</style>
