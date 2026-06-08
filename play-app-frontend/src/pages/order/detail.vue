<template>
  <view class="container" v-if="order">
    <!-- 状态头 -->
    <view class="status-header">
      <view class="status-text">{{ getStatusText(order.status) }}</view>
      <view class="status-desc">{{ getStatusDesc(order.status) }}</view>
    </view>

    <!-- 助教信息 -->
    <view class="card companion-card" @click="goToCompanion(order.companionId)">
      <view class="info">
        <text class="title">同城伴玩服务</text>
        <text class="iconfont icon-arrow-right"></text>
      </view>
    </view>

    <!-- 服务详情 -->
    <view class="card details-card">
      <view class="section-title">服务详情</view>
      <view class="row">
        <text class="label">预约时间</text>
        <text class="value">{{ order.reserveDate }} {{ order.reserveTimeStart }}{{ order.reserveTimeEnd ? '-' + order.reserveTimeEnd : '' }}</text>
      </view>
      <view class="row">
        <text class="label">服务时长</text>
        <text class="value">{{ order.hours }} 小时</text>
      </view>
      <view class="row">
        <text class="label">服务地址</text>
        <text class="value">{{ order.address }}{{ order.addressDetail ? ' ' + order.addressDetail : '' }}</text>
      </view>
      <view class="row">
        <text class="label">联系方式</text>
        <text class="value">{{ order.customerWechat || '未填写' }}</text>
      </view>
      <view class="row">
        <text class="label">订单备注</text>
        <text class="value">{{ order.customerRemark || '无' }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="card info-card">
      <view class="section-title">订单信息</view>
      <view class="row">
        <text class="label">订单编号</text>
        <text class="value">{{ order.orderNo }}</text>
      </view>
      <view class="row">
        <text class="label">创建时间</text>
        <text class="value">{{ order.createTime }}</text>
      </view>
      <view class="row" v-if="order.payTime">
        <text class="label">支付时间</text>
        <text class="value">{{ order.payTime }}</text>
      </view>
    </view>

    <!-- 费用明细 -->
    <view class="card cost-card">
      <view class="section-title">费用明细</view>
      <view class="row">
        <text class="label">服务金额</text>
        <text class="value">¥{{ order.totalAmount }}</text>
      </view>
      <view class="row total">
        <text class="label">实付款</text>
        <text class="value highlight">¥{{ order.totalAmount }}</text>
      </view>
      <view class="row" v-if="order.refundAmount && Number(order.refundAmount) > 0">
        <text class="label">退款金额</text>
        <text class="value">¥{{ order.refundAmount }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="hasActions(order.status)">
      <button class="btn" v-if="order.status === 10" @click="goToPay">去支付</button>
      <button class="btn" v-if="canCancel(order.status)" @click="cancelOrder">取消订单</button>
      <button class="btn" v-if="canRefund(order.status)" @click="goToRefund">申请退款</button>
      <button class="btn primary" v-if="order.status === 60" @click="confirmOrder">确认完工</button>
      <button class="btn primary" v-if="order.status === 70 || order.status === 80" @click="goToReview">去评价</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {request} from '../../utils/request';

const order = ref<any>(null);
const orderNo = ref('');

onLoad((options: any) => {
  const no = options?.orderNo;
  if (no) {
    orderNo.value = no;
    fetchDetail();
  }
});

const fetchDetail = async () => {
  const res = await request({
    url: `/orders/${orderNo.value}`,
    method: 'GET'
  });
  if (res.code === 200) {
    order.value = res.data;
  }
};

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    10: '待付款', 20: '待拉群', 30: '已拉群', 40: '双方确认',
    50: '服务中', 60: '待确认', 70: '待评价/待结算', 80: '已完成',
    100: '退款申请中', 110: '退款处理中', 120: '已退款', 130: '部分退款', 250: '已关闭'
  };
  return map[status] || '未知';
};

const getStatusDesc = (status: number) => {
  const map: Record<number, string> = {
    10: '请尽快完成支付',
    20: '支付成功，等待客服拉三方群',
    30: '客服已拉群，请按约定时间履约',
    50: '服务进行中，如需退款请先与客服沟通',
    60: '助教已发起完工，请确认服务结果',
    70: '服务已核销，等待平台结算，可先评价',
    80: '订单已完成，感谢您的使用',
    100: '退款申请已提交，等待平台处理'
  };
  return map[status] || '';
};

const goToCompanion = (id: number) => {
  uni.navigateTo({ url: `/pages/companion/detail?id=${id}` });
};

const goToPay = () => {
  uni.navigateTo({ url: `/pages/order/pay?orderNo=${orderNo.value}&amount=${order.value.totalAmount}` });
};

const confirmOrder = () => {
  uni.showModal({
    title: '确认完工',
    content: '确认服务已完成？确认后平台将进入结算流程。',
    success: async (res: any) => {
      if (res.confirm) {
        const confirmRes = await request({
          url: `/orders/${orderNo.value}/confirm`, method: 'PUT'
        });
        if (confirmRes.code === 200) {
          uni.showToast({ title: '确认成功' });
          fetchDetail();
        }
      }
    }
  });
};

const cancelOrder = () => {
  uni.showModal({
    title: '取消订单',
    content: '确定取消此订单？',
    success: async (res: any) => {
      if (res.confirm) {
        const apiRes = await request({
          url: `/orders/${orderNo.value}/cancel`, method: 'PUT',
          data: { reason: '用户取消' }
        });
        if (apiRes.code === 200) {
          uni.showToast({ title: '已取消' });
          fetchDetail();
        }
      }
    }
  });
};

const goToReview = () => {
  uni.navigateTo({ url: `/pages/order/review?orderNo=${orderNo.value}` });
};

const goToRefund = () => {
  uni.navigateTo({ url: `/pages/order/refund?orderNo=${orderNo.value}` });
};

const canCancel = (status: number) => status === 10;
const canRefund = (status: number) => [20, 30, 40, 50, 60].includes(status);
const hasActions = (status: number) => canCancel(status) || canRefund(status) || status === 60 || status === 70 || status === 80;
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
  padding-bottom: 140rpx;
}

.status-header {
  background: $gradient-primary;
  padding: 60rpx 40rpx 80rpx;
  color: #fff;
  
  .status-text { font-size: 48rpx; font-weight: bold; margin-bottom: 10rpx; }
  .status-desc { font-size: $font-size-sm; opacity: 0.8; }
}

.card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  margin: 0 20rpx 20rpx;
  padding: 30rpx;
  
  &.companion-card {
    margin-top: -40rpx;
    position: relative;
    z-index: 10;
    
    .info {
      display: flex; justify-content: space-between; align-items: center;
      .title { font-size: $font-size-lg; font-weight: bold; color: $text-color-primary; }
      .icon-arrow-right { color: $text-color-secondary; }
    }
  }
}

.section-title {
  font-size: $font-size-base;
  font-weight: bold;
  color: $text-color-primary;
  margin-bottom: 20rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  
  .label { color: $text-color-regular; font-size: $font-size-sm; }
  .value { color: $text-color-primary; font-size: $font-size-sm; text-align: right; flex: 1; margin-left: 40rpx; }
  
  &.total {
    border-top: 1rpx solid $border-color-light;
    margin-top: 10rpx;
    padding-top: 26rpx;
    
    .label { color: $text-color-primary; font-weight: bold; font-size: $font-size-base; }
    .value.highlight { color: $color-secondary; font-weight: bold; font-size: 36rpx; }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background-color: $bg-color-white;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  
  .btn {
    margin: 0 0 0 20rpx;
    padding: 0 40rpx;
    height: 72rpx;
    line-height: 72rpx;
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
</style>
