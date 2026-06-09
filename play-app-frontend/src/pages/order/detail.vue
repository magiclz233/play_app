<template>
  <view class="container" :class="appStore.themeClass" v-if="order">
    <!-- 状态头 -->
    <view class="status-header">
      <view class="status-text">{{ getStatusText(order.status) }}</view>
      <view class="status-desc">{{ getStatusDesc(order.status) }}</view>
    </view>

    <!-- 助教信息 -->
    <view class="card companion-card" @click="goToCompanion(order.companionId)" hover-class="card-hover">
      <view class="info">
        <text class="title">{{ t('lobby.title') }}{{ t('detail.orderLabel') }}</text>
        <text class="iconfont icon-arrow-right"></text>
      </view>
    </view>

    <!-- 服务详情 -->
    <view class="card details-card">
      <view class="section-title">{{ appStore.locale === 'en' ? 'Service Details' : '服务详情' }}</view>
      <view class="row">
        <text class="label">{{ t('order.time') }}</text>
        <text class="value">{{ order.reserveDate }} {{ order.reserveTimeStart }}{{ order.reserveTimeEnd ? '-' + order.reserveTimeEnd : '' }}</text>
      </view>
      <view class="row">
        <text class="label">{{ t('order.duration') }}</text>
        <text class="value">{{ order.hours }} {{ appStore.locale === 'en' ? 'Hrs' : '小时' }}</text>
      </view>
      <view class="row">
        <text class="label">{{ t('order.reserveAddress') }}</text>
        <text class="value">{{ order.address }}{{ order.addressDetail ? ' ' + order.addressDetail : '' }}</text>
      </view>
      <view class="row">
        <text class="label">{{ appStore.locale === 'en' ? 'Contact Info' : '联系方式' }}</text>
        <text class="value">{{ order.customerWechat || (appStore.locale === 'en' ? 'Not Provided' : '未填写') }}</text>
      </view>
      <view class="row">
        <text class="label">{{ t('detail.remark') }}</text>
        <text class="value">{{ order.customerRemark || (appStore.locale === 'en' ? 'None' : '无') }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="card info-card">
      <view class="section-title">{{ t('order.detail') }}</view>
      <view class="row">
        <text class="label">{{ appStore.locale === 'en' ? 'Order ID' : '订单编号' }}</text>
        <text class="value code">{{ order.orderNo }}</text>
      </view>
      <view class="row">
        <text class="label">{{ appStore.locale === 'en' ? 'Created Time' : '创建时间' }}</text>
        <text class="value">{{ order.createTime }}</text>
      </view>
      <view class="row" v-if="order.payTime">
        <text class="label">{{ appStore.locale === 'en' ? 'Payment Time' : '支付时间' }}</text>
        <text class="value">{{ order.payTime }}</text>
      </view>
    </view>

    <!-- 费用明细 -->
    <view class="card cost-card">
      <view class="section-title">{{ appStore.locale === 'en' ? 'Billing Summary' : '费用明细' }}</view>
      <view class="row">
        <text class="label">{{ appStore.locale === 'en' ? 'Service Amount' : '服务金额' }}</text>
        <text class="value">¥{{ order.totalAmount }}</text>
      </view>
      <view class="row total">
        <text class="label">{{ t('order.amount') }}</text>
        <text class="value highlight">¥{{ order.totalAmount }}</text>
      </view>
      <view class="row" v-if="order.refundAmount && Number(order.refundAmount) > 0">
        <text class="label">{{ appStore.locale === 'en' ? 'Refunded Amount' : '退款金额' }}</text>
        <text class="value text-danger">¥{{ order.refundAmount }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="hasActions(order.status)">
      <button class="btn" v-if="order.status === 10" @click="goToPay" hover-class="button-hover">{{ t('mine.waitingPay') }}</button>
      <button class="btn" v-if="canCancel(order.status)" @click="cancelOrder" hover-class="button-hover">{{ t('order.actionCancel') }}</button>
      <button class="btn" v-if="canRefund(order.status)" @click="goToRefund" hover-class="button-hover">{{ t('order.actionRefund') }}</button>
      <button class="btn primary" v-if="order.status === 60" @click="confirmOrder" hover-class="button-hover">{{ t('order.actionConfirm') }}</button>
      <button class="btn primary" v-if="order.status === 70 || order.status === 80" @click="goToReview" hover-class="button-hover">{{ t('order.actionReview') }}</button>
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
  const text = t(`order.status.${status}`);
  return text.includes('order.status.') ? t('common.status') : text;
};

const getStatusDesc = (status: number) => {
  const descMap: Record<string, Record<number, string>> = {
    'zh-Hans': {
      10: '请尽快完成支付',
      20: '支付成功，等待客服拉三方群',
      30: '客服已拉群，请按约定时间履约',
      40: '双方已确认，正为您安排服务',
      50: '服务进行中，如需退款请先与客服沟通',
      60: '助教已发起完工，请确认服务结果',
      70: '服务已核销，等待平台结算，可先评价',
      80: '订单已完成，感谢您的使用',
      100: '订单已取消',
      110: '退款申请已提交，等待平台处理',
      120: '已退款成功',
      130: '已部分退款成功',
      250: '订单已关闭'
    },
    'zh-Hant': {
      10: '請儘快完成支付',
      20: '支付成功，等待客服拉三方群',
      30: '客服已拉群，請按約定時間履約',
      40: '雙方已確認，正為您安排服務',
      50: '服務進行中，如需退款請先與客服溝通',
      60: '助教已發起完工，請確認服務結果',
      70: '服務已核銷，等待平台結算，可先評價',
      80: '訂單已完成，感謝您的使用',
      100: '訂單已取消',
      110: '退款申請已提交，等待平台處理',
      120: '已退款成功',
      130: '已部分退款成功',
      250: '訂單已關閉'
    },
    'en': {
      10: 'Please finish payment as soon as possible',
      20: 'Payment successful, waiting to create group chat',
      30: 'Chat group created, please serve on schedule',
      40: 'Both parties confirmed, planning coordinates',
      50: 'Service is active, contact support if refund needed',
      60: 'Companion marked finished, please confirm results',
      70: 'Service verified, waiting for settlement',
      80: 'Order completed, thank you for using',
      100: 'Order cancelled',
      110: 'Refund requested, waiting for review',
      120: 'Refund completed',
      130: 'Partially refunded',
      250: 'Order closed'
    }
  };
  const currentLocale = appStore.locale;
  const map = descMap[currentLocale] || descMap['zh-Hans'];
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
    title: t('order.actionConfirm'),
    content: appStore.locale === 'en' ? 'Confirm service is completed? This enters settlement process.' : '确认服务已完成？确认后平台将进入结算流程。',
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    success: async (res: any) => {
      if (res.confirm) {
        const confirmRes = await request({
          url: `/orders/${orderNo.value}/confirm`, method: 'PUT'
        });
        if (confirmRes.code === 200) {
          uni.showToast({ title: t('common.success') });
          fetchDetail();
        }
      }
    }
  });
};

const cancelOrder = () => {
  uni.showModal({
    title: t('order.actionCancel'),
    content: appStore.locale === 'en' ? 'Are you sure you want to cancel this order?' : '确定取消此订单？',
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    success: async (res: any) => {
      if (res.confirm) {
        const apiRes = await request({
          url: `/orders/${orderNo.value}/cancel`, method: 'PUT',
          data: { reason: 'User cancelled' }
        });
        if (apiRes.code === 200) {
          uni.showToast({ title: t('common.success') });
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
  padding-bottom: 160rpx;
  box-sizing: border-box;
}

.status-header {
  background: $gradient-primary;
  padding: 80rpx 40rpx 100rpx;
  color: #fff;
  
  .status-text { font-size: 48rpx; font-weight: 800; margin-bottom: 12rpx; }
  .status-desc { font-size: $font-size-sm; opacity: 0.9; line-height: 1.4; }
}

.card {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  margin: 0 24rpx 24rpx;
  padding: 30rpx;
  box-shadow: $box-shadow-sm;
  border: 1px solid $border-color-light;
  
  &.companion-card {
    margin-top: -50rpx;
    position: relative;
    z-index: 10;
    
    .info {
      display: flex; justify-content: space-between; align-items: center;
      .title { font-size: $font-size-base; font-weight: bold; color: $text-color-primary; }
      .icon-arrow-right { color: $text-color-secondary; font-size: 24rpx; }
    }
  }
}

.card-hover {
  opacity: 0.95;
  transform: scale(0.995);
}

.section-title {
  font-size: $font-size-base;
  font-weight: bold;
  color: $text-color-primary;
  margin-bottom: 24rpx;
  border-left: 6rpx solid $color-primary;
  padding-left: 16rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  font-size: $font-size-sm;
  border-bottom: 1px solid rgba(0, 0, 0, 0.01);
  
  .label { color: $text-color-regular; }
  .value { 
    color: $text-color-primary; 
    text-align: right; 
    flex: 1; 
    margin-left: 40rpx;
    font-weight: 500;
    
    &.code {
      font-family: monospace;
      color: $text-color-secondary;
    }
  }

  &:last-child {
    border-bottom: none;
  }
  
  &.total {
    border-top: 1px solid $border-color-light;
    margin-top: 10rpx;
    padding-top: 28rpx;
    border-bottom: none;
    
    .label { color: $text-color-primary; font-weight: bold; font-size: $font-size-base; }
    .value.highlight { 
      color: $color-primary; 
      font-weight: 800; 
      font-size: 38rpx;
      font-family: 'Outfit', sans-serif;
    }
  }

  .text-danger {
    color: $color-error;
    font-weight: bold;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background-color: $bg-color-white;
  padding: 24rpx 30rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.04);
  border-top: 1px solid $border-color-light;
  z-index: $z-index-sticky;
  
  .btn {
    margin: 0 0 0 20rpx;
    padding: 0 40rpx;
    height: 72rpx;
    line-height: 70rpx;
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
      line-height: 72rpx;
    }
  }
}

/* 暗色模式特殊微调 */
.theme-dark {
  .status-header {
    background: linear-gradient(135deg, $color-primary-dark, #2E1015);
  }
  .btn {
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
