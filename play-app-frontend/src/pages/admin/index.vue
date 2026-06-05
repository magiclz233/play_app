<template>
  <view class="container">
    <view class="header">
      <text class="title">管理后台</text>
    </view>

    <!-- Tab 切换 -->
    <view class="tabs">
      <view class="tab" :class="{ active: currentTab === 0 }" @click="currentTab = 0">助教审核</view>
      <view class="tab" :class="{ active: currentTab === 1 }" @click="currentTab = 1">提现审核</view>
      <view class="tab" :class="{ active: currentTab === 2 }" @click="currentTab = 2">订单管理</view>
    </view>

    <!-- 助教审核列表 -->
    <scroll-view v-if="currentTab === 0" scroll-y class="content">
      <view class="card" v-for="item in companionList" :key="item.userId">
        <view class="card-row"><text class="label">ID:</text><text>{{ item.userId }}</text></view>
        <view class="card-row"><text class="label">姓名:</text><text>{{ item.realName }}</text></view>
        <view class="card-row"><text class="label">性别:</text><text>{{ item.gender === 1 ? '男' : '女' }}</text></view>
        <view class="card-row"><text class="label">简介:</text><text>{{ item.summary }}</text></view>
        <view class="card-row"><text class="label">状态:</text><text class="pending">待审核</text></view>
        <view class="card-actions">
          <button class="btn success" @click="auditCompanion(item.userId, true)">通过</button>
          <button class="btn danger" @click="showReject(item.userId)">驳回</button>
        </view>
      </view>
      <view class="empty" v-if="companionList.length === 0">暂无待审核申请</view>
    </scroll-view>

    <!-- 提现审核列表 -->
    <scroll-view v-if="currentTab === 1" scroll-y class="content">
      <view class="card" v-for="item in withdrawalList" :key="item.id">
        <view class="card-row"><text class="label">申请人ID:</text><text>{{ item.companionId }}</text></view>
        <view class="card-row"><text class="label">金额:</text><text class="price">¥{{ item.amount }}</text></view>
        <view class="card-row"><text class="label">申请时间:</text><text>{{ item.createTime }}</text></view>
        <view class="card-actions">
          <button class="btn success" @click="auditWithdrawal(item.id, 1)">通过</button>
          <button class="btn danger" @click="auditWithdrawal(item.id, 2)">驳回</button>
        </view>
      </view>
      <view class="empty" v-if="withdrawalList.length === 0">暂无待审核提现</view>
    </scroll-view>

    <!-- 订单管理列表 -->
    <scroll-view v-if="currentTab === 2" scroll-y class="content">
      <view class="card" v-for="item in orderList" :key="item.orderNo">
        <view class="card-row"><text class="label">订单号:</text><text>{{ item.orderNo }}</text></view>
        <view class="card-row"><text class="label">客户ID:</text><text>{{ item.userId }}</text></view>
        <view class="card-row"><text class="label">助教ID:</text><text>{{ item.companionId }}</text></view>
        <view class="card-row"><text class="label">金额:</text><text class="price">¥{{ item.totalAmount }}</text></view>
        <view class="card-row"><text class="label">状态:</text><text>{{ getOrderStatusText(item.status) }}</text></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { request } from '../../utils/request';

const currentTab = ref(0);
const companionList = ref<any[]>([]);
const withdrawalList = ref<any[]>([]);
const orderList = ref<any[]>([]);

onMounted(() => {
  loadCompanions();
  loadWithdrawals();
  loadOrders();
});

const loadCompanions = async () => {
  try {
    const res = await request({ url: '/admin/companions/pending?current=1&size=50', method: 'GET' });
    if (res.code === 200) companionList.value = res.data.records || [];
  } catch (e) { /* ignore */ }
};

const loadWithdrawals = async () => {
  try {
    const res = await request({ url: '/admin/withdrawals?status=0&current=1&size=50', method: 'GET' });
    if (res.code === 200) withdrawalList.value = res.data.records || [];
  } catch (e) { /* ignore */ }
};

const loadOrders = async () => {
  try {
    const res = await request({ url: '/admin/orders?current=1&size=50', method: 'GET' });
    if (res.code === 200) orderList.value = res.data.records || [];
  } catch (e) { /* ignore */ }
};

const auditCompanion = async (companionId: number, isPass: boolean) => {
  try {
    await request({ url: `/admin/companions/${companionId}/audit`, method: 'PUT', data: { isPass, rejectReason: '' } });
    uni.showToast({ title: isPass ? '已通过' : '已驳回', icon: 'success' });
    loadCompanions();
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const showReject = (companionId: number) => {
  uni.showModal({
    title: '驳回原因',
    editable: true,
    placeholderText: '请输入驳回原因',
    success: async (res: any) => {
      if (res.confirm) {
        try {
          await request({ url: `/admin/companions/${companionId}/audit`, method: 'PUT', data: { isPass: false, rejectReason: res.content || '' } });
          uni.showToast({ title: '已驳回', icon: 'success' });
          loadCompanions();
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' });
        }
      }
    }
  });
};

const auditWithdrawal = async (id: number, status: number) => {
  try {
    await request({ url: `/admin/withdrawals/${id}/audit`, method: 'PUT', data: { status, remark: status === 2 ? '审核驳回' : '' } });
    uni.showToast({ title: status === 1 ? '已通过' : '已驳回', icon: 'success' });
    loadWithdrawals();
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const getOrderStatusText = (status: number) => {
  const map: Record<number, string> = { 10:'待付款', 20:'已付款', 30:'已接单', 70:'已确认', 80:'已完成', 100:'取消申请', 120:'已退款', 250:'已关闭' };
  return map[status] || String(status);
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
}
.header {
  padding: 30rpx;
  background: $gradient-primary;
  .title { font-size: 36rpx; font-weight: bold; color: #fff; }
}
.tabs {
  display: flex;
  background: #fff;
  padding: 0 30rpx;
  border-bottom: 1rpx solid $border-color-light;
  .tab {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 28rpx;
    color: $text-color-regular;
    &.active { color: $color-primary; font-weight: bold; border-bottom: 4rpx solid $color-primary; }
  }
}
.content {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
}
.card {
  background: #fff;
  border-radius: $border-radius-md;
  padding: 24rpx;
  margin-bottom: 20rpx;
  .card-row {
    display: flex;
    margin-bottom: 12rpx;
    font-size: 26rpx;
    .label { width: 140rpx; color: $text-color-secondary; flex-shrink: 0; }
    .pending { color: $color-warning; font-weight: bold; }
    .price { color: $color-secondary; font-weight: bold; }
  }
  .card-actions {
    display: flex;
    justify-content: flex-end;
    gap: 20rpx;
    margin-top: 20rpx;
    .btn {
      margin: 0;
      padding: 0 32rpx;
      height: 56rpx;
      line-height: 56rpx;
      font-size: 24rpx;
      border-radius: $border-radius-pill;
      &.success { background: $color-success; color: #fff; }
      &.danger { background: $color-error; color: #fff; }
    }
  }
}
.empty { text-align: center; padding: 100rpx 0; color: $text-color-placeholder; }
</style>
