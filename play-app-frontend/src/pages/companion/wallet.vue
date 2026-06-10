<template>
  <view class="container">
    <view class="wallet-card">
      <view class="label">可提现余额 (元)</view>
      <view class="balance">{{ wallet.balance || '0.00' }}</view>
      <view class="frozen">冻结中: ¥{{ wallet.frozenAmount || '0.00' }}</view>
      
      <button class="withdraw-btn" @click="showWithdraw = true">申请提现</button>
    </view>

    <view class="transaction-list">
      <view class="title">资金流水</view>
      <view class="list">
        <view class="tx-item" v-for="item in transactions" :key="item.id">
          <view class="left">
            <view class="tx-type">{{ item.transType === 1 ? '订单收益' : '申请提现' }}</view>
            <view class="tx-time">{{ formatTime(item.createTime) }}</view>
          </view>
          <view class="right">
            <text class="amount" :class="item.transType === 1 ? 'income' : 'expense'">
              {{ item.transType === 1 ? '+' : '' }}{{ item.amount }}
            </text>
            <text class="remark">{{ item.remark }}</text>
          </view>
        </view>
        <view class="empty" v-if="transactions.length === 0">暂无资金明细</view>
      </view>
    </view>

    <!-- 提现弹窗 -->
    <view class="modal-mask" v-if="showWithdraw" @click="showWithdraw = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">发起提现</view>
        <view class="modal-body">
          <view class="form-item">
            <text class="label">提现金额</text>
            <view class="input-wrap">
              <text>¥</text>
              <input type="digit" v-model="withdrawAmount" placeholder="单次最低10元" inputmode="decimal" />
            </view>
          </view>
          <view class="form-item">
            <text class="label">真实姓名</text>
            <input type="text" v-model="realName" placeholder="微信实名校验" class="std-input"/>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn cancel" @click="showWithdraw = false">取消</button>
          <button class="btn confirm" @click="submitWithdraw">确认提现</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {request} from '../../utils/request';
import {useAppStore} from '../../store/app';
import {haptic} from '../../utils/haptic';

const appStore = useAppStore();
const wallet = ref<any>({});
const transactions = ref<any[]>([]);

const showWithdraw = ref(false);
const withdrawAmount = ref('');
const realName = ref('');

onMounted(() => {
  fetchWallet();
  fetchTransactions();
});

const fetchWallet = async () => {
  const res = await request({ url: '/companion/wallet', method: 'GET' });
  if (res.code === 200) wallet.value = res.data;
};

const fetchTransactions = async () => {
  const res = await request({ url: '/companion/wallet/transactions', method: 'GET' });
  if (res.code === 200) transactions.value = res.data;
};

const submitWithdraw = async () => {
  if (!withdrawAmount.value || parseFloat(withdrawAmount.value) < 10) {
    return uni.showToast({ title: '最低提现10元', icon: 'none' });
  }
  if (!realName.value) {
    return uni.showToast({ title: '请输入真实姓名', icon: 'none' });
  }

  const res = await request({
    url: '/companion/withdraw',
    method: 'POST',
    data: {
      amount: parseFloat(withdrawAmount.value),
      realName: realName.value
    }
  });

  if (res.code === 200) {
    uni.showToast({ title: '申请成功', icon: 'success' });
    haptic.heavy();
    showWithdraw.value = false;
    withdrawAmount.value = '';
    fetchWallet();
    fetchTransactions();
  }
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 16) : '';
</script>

<style lang="scss" scoped>
.container { min-height: 100vh; background-color: $bg-color-page; padding: 20rpx; }

.wallet-card {
  background: $gradient-primary;
  border-radius: $border-radius-lg;
  padding: 50rpx 40rpx;
  color: #fff;
  position: relative;
  box-shadow: 0 10rpx 30rpx rgba(var(--color-primary-rgb), 0.18);
  margin-bottom: 30rpx;
  
  .label { font-size: 28rpx; opacity: 0.9; margin-bottom: 10rpx; }
  .balance { font-size: 80rpx; font-weight: bold; margin-bottom: 20rpx; }
  .frozen { font-size: 24rpx; opacity: 0.8; }
  
  .withdraw-btn {
    position: absolute; right: 40rpx; top: 50rpx;
    background-color: var(--bg-card); color: $color-primary;
    font-size: 28rpx; padding: 0 30rpx; height: 60rpx; line-height: 60rpx;
    border-radius: $border-radius-pill; font-weight: bold;
    &::after { display: none; }
  }
}

.transaction-list {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 30rpx;
  
  .title { font-size: $font-size-lg; font-weight: bold; margin-bottom: 30rpx; color: $text-color-primary; }
  
  .tx-item {
    display: flex; justify-content: space-between; align-items: center;
    padding: 24rpx 0; border-bottom: 1rpx solid $border-color-light;
    
    .left {
      .tx-type { font-size: $font-size-base; font-weight: bold; color: $text-color-primary; margin-bottom: 8rpx; }
      .tx-time { font-size: 24rpx; color: $text-color-secondary; }
    }
    
    .right {
      text-align: right;
      .amount { font-size: 32rpx; font-weight: bold; display: block; margin-bottom: 8rpx; }
      .amount.income { color: $color-success; }
      .amount.expense { color: $text-color-primary; }
      .remark { font-size: 20rpx; color: $text-color-secondary; }
    }
  }
  
  .empty { text-align: center; padding: 60rpx 0; color: $text-color-placeholder; font-size: 28rpx; }
}

/* Modal */
.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 100;
  
  .modal-content {
    width: 600rpx; background-color: var(--bg-card); color: var(--text-primary); border-radius: $border-radius-lg; overflow: hidden;
    
    .modal-header { text-align: center; padding: 30rpx; font-size: 32rpx; font-weight: bold; border-bottom: 1rpx solid $border-color-light; }
    
    .modal-body {
      padding: 40rpx;
      
      .form-item {
        margin-bottom: 30rpx;
        .label { font-size: 28rpx; color: var(--text-secondary); display: block; margin-bottom: 16rpx; }
        .input-wrap {
          display: flex; align-items: center; border-bottom: 1rpx solid $color-primary; padding-bottom: 10rpx;
          text { font-size: 48rpx; font-weight: bold; margin-right: 16rpx; }
          input { flex: 1; font-size: 48rpx; font-weight: bold; height: 60rpx; color: var(--text-primary); }
        }
        .std-input { width: 100%; height: 80rpx; background-color: var(--bg-main); color: var(--text-primary); border-radius: $border-radius-sm; padding: 0 20rpx; box-sizing: border-box; border: 1px solid var(--border-color); }
      }
    }
    
    .modal-footer {
      display: flex; border-top: 1rpx solid $border-color-light;
      
      .btn {
        flex: 1; height: 100rpx; line-height: 100rpx; text-align: center; font-size: 32rpx; background-color: var(--bg-card); color: var(--text-primary); border-radius: 0; margin: 0;
        &::after { display: none; }
        &.cancel { color: var(--text-secondary); border-right: 1rpx solid $border-color-light; }
        &.confirm { color: $color-primary; font-weight: bold; }
      }
    }
  }
}
</style>
