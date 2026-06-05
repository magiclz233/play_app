<template>
  <view class="container">
    <view class="header-bg"></view>

    <!-- ===== 用户信息卡片 ===== -->
    <view class="user-card" v-if="userInfo">
      <image :src="userInfo.avatarUrl || defaultAvatar" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="nickname">
          {{ userInfo.nickname || '微信用户' }}
          <text class="mode-tag" v-if="isCompanion && mode === 'companion'">陪玩模式</text>
        </view>
        <view class="phone">{{ userInfo.phone ? formatPhone(userInfo.phone) : '暂未绑定手机号' }}</view>
      </view>
    </view>
    <view class="user-card not-login" v-else @click="login">
      <image :src="defaultAvatar" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="nickname">点击登录/注册</view>
        <view class="desc">登录后体验更多功能</view>
      </view>
    </view>

    <!-- ===== 角色切换 ===== -->
    <view class="role-switch" v-if="isCompanion">
      <view class="switch-bar">
        <view class="switch-item" :class="{ active: mode === 'customer' }" @click="switchMode('customer')">
          <text class="icon">🔍</text>
          <text>找陪玩</text>
        </view>
        <view class="switch-item" :class="{ active: mode === 'companion' }" @click="switchMode('companion')">
          <text class="icon">💼</text>
          <text>去接单</text>
        </view>
      </view>
    </view>

    <!-- ==================== 客户模式 ==================== -->
    <template v-if="mode === 'customer'">
      <!-- 订单入口 -->
      <view class="menu-card">
        <view class="card-title">
          <text>我的订单</text>
          <view class="more" @click="goToOrderList('')">
            <text>全部</text>
            <text class="arrow">›</text>
          </view>
        </view>
        <view class="grid-menu">
          <view class="grid-item" @click="goToOrderList('10')">
            <view class="icon-wrap"><text class="icon">💰</text></view>
            <text class="label">待付款</text>
          </view>
          <view class="grid-item" @click="goToOrderList('20')">
            <view class="icon-wrap"><text class="icon">📋</text></view>
            <text class="label">待接单</text>
          </view>
          <view class="grid-item" @click="goToOrderList('30')">
            <view class="icon-wrap"><text class="icon">🔄</text></view>
            <text class="label">进行中</text>
          </view>
          <view class="grid-item" @click="goToOrderList('70')">
            <view class="icon-wrap"><text class="icon">⭐</text></view>
            <text class="label">待评价</text>
          </view>
        </view>
      </view>

      <!-- 我要接单入口 -->
      <view class="menu-card list-menu">
        <view class="list-item companion-entry" @click="goToCompanion">
          <view class="left">
            <text class="entry-icon">🎯</text>
            <view>
              <text class="label">{{ isCompanion ? '助教工作台' : '我要接单' }}</text>
              <text class="sub" v-if="!isCompanion">成为陪玩，开启赚钱之旅</text>
              <text class="sub" v-else>管理接单、查看收益</text>
            </view>
          </view>
          <view class="right">
            <text class="tip" v-if="!isCompanion">成为陪玩</text>
            <text class="arrow">›</text>
          </view>
        </view>

        <view class="list-item">
          <view class="left">
            <text class="entry-icon">🎧</text>
            <text class="label">联系客服</text>
          </view>
          <text class="arrow">›</text>
        </view>

        <view class="list-item">
          <view class="left">
            <text class="entry-icon">ℹ️</text>
            <text class="label">关于我们</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
    </template>

    <!-- ==================== 陪玩模式 ==================== -->
    <template v-if="mode === 'companion'">
      <!-- 陪玩数据概览 -->
      <view class="menu-card">
        <view class="card-title"><text>今日概览</text></view>
        <view class="stats-row">
          <view class="stat-item" @click="goToCompanionOrders">
            <text class="stat-val">{{ companionStats.totalOrderCount || 0 }}</text>
            <text class="stat-lbl">总订单</text>
          </view>
          <view class="stat-item" @click="goToWallet">
            <text class="stat-val">¥{{ companionStats.balance || '0.00' }}</text>
            <text class="stat-lbl">余额</text>
          </view>
          <view class="stat-item">
            <text class="stat-val">¥{{ companionStats.totalIncome || '0.00' }}</text>
            <text class="stat-lbl">累计收入</text>
          </view>
        </view>
      </view>

      <!-- 陪玩功能入口 -->
      <view class="menu-card list-menu">
        <view class="list-item" @click="goToCompanionOrders">
          <view class="left">
            <text class="entry-icon">📋</text>
            <text class="label">接单管理</text>
          </view>
          <view class="right"><text class="arrow">›</text></view>
        </view>
        <view class="list-item" @click="goToWallet">
          <view class="left">
            <text class="entry-icon">💳</text>
            <text class="label">我的钱包</text>
          </view>
          <view class="right">
            <text class="tip">提现</text>
            <text class="arrow">›</text>
          </view>
        </view>
        <view class="list-item" @click="goToCompanionProfile">
          <view class="left">
            <text class="entry-icon">👤</text>
            <text class="label">个人资料</text>
          </view>
          <view class="right"><text class="arrow">›</text></view>
        </view>
        <view class="list-item">
          <view class="left">
            <text class="entry-icon">🎧</text>
            <text class="label">联系客服</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const userInfo = ref<any>(null);
const isCompanion = ref(false);
const mode = ref<'customer' | 'companion'>('customer');
const companionStats = ref<any>({});
const defaultAvatar = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop';

onShow(async () => {
  if (userStore.isLoggedIn) {
    await fetchProfile();
    await checkCompanionStatus();
    if (isCompanion.value) {
      await fetchCompanionStats();
    }
  }
});

const fetchProfile = async () => {
  const res = await request({ url: '/user/profile', method: 'GET' });
  if (res.code === 200) {
    userInfo.value = res.data;
    userStore.setUserInfo(res.data);
  }
};

const checkCompanionStatus = async () => {
  try {
    const res = await request({ url: '/companion/apply/status', method: 'GET' });
    if (res.code === 200 && res.data) {
      isCompanion.value = res.data.auditStatus === 1;
    }
  } catch {
    isCompanion.value = false;
  }
};

const fetchCompanionStats = async () => {
  try {
    const res = await request({ url: '/companion/dashboard', method: 'GET' });
    if (res.code === 200) companionStats.value = res.data;
  } catch { /* ignore */ }
};

const switchMode = (m: 'customer' | 'companion') => {
  mode.value = m;
  if (m === 'companion') fetchCompanionStats();
};

// ===== 客户功能 =====
const login = async () => {
  uni.showLoading({ title: '模拟登录中...' });
  try {
    const res = await request({ url: '/wx/mock-login', method: 'POST', data: { phone: '13800000000' } });
    if (res.code === 200) {
      userStore.login(res.data.token, res.data.user);
      uni.showToast({ title: '登录成功', icon: 'success' });
      await fetchProfile();
      await checkCompanionStatus();
    }
  } finally {
    uni.hideLoading();
  }
};

const goToOrderList = (status: string) => {
  if (!userStore.isLoggedIn) return uni.showToast({ title: '请先登录', icon: 'none' });
  uni.navigateTo({ url: `/pages/order/list?status=${status}` });
};

const goToCompanion = () => {
  if (!userStore.isLoggedIn) return uni.showToast({ title: '请先登录', icon: 'none' });
  if (isCompanion.value) {
    switchMode('companion');
  } else {
    uni.navigateTo({ url: '/pages/companion/apply' });
  }
};

// ===== 陪玩功能 =====
const goToCompanionOrders = () => uni.navigateTo({ url: '/pages/companion/dashboard' });
const goToWallet = () => uni.navigateTo({ url: '/pages/companion/wallet' });
const goToCompanionProfile = () => uni.navigateTo({ url: '/pages/companion/apply' });

const formatPhone = (phone: string) => phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
  position: relative;
  padding-bottom: 40rpx;
}

.header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 320rpx;
  background: $gradient-primary;
  border-radius: 0 0 60rpx 60rpx;
  z-index: 0;
}

// ===== 用户卡片 =====
.user-card {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 50rpx 40rpx 30rpx;

  .avatar {
    width: 110rpx; height: 110rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255,255,255,0.5);
    margin-right: 24rpx;
    flex-shrink: 0;
  }

  .info {
    flex: 1; color: #fff;
    .nickname {
      font-size: 36rpx; font-weight: bold; margin-bottom: 8rpx;
      display: flex; align-items: center; gap: 12rpx;
    }
    .mode-tag {
      font-size: 20rpx; font-weight: normal;
      background: rgba(255,255,255,0.25); padding: 4rpx 14rpx; border-radius: 20rpx;
    }
    .phone { font-size: 24rpx; opacity: 0.8; }
  }
}

// ===== 角色切换 =====
.role-switch {
  position: relative; z-index: 1;
  margin: 0 30rpx 24rpx;
}
.switch-bar {
  display: flex;
  background: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 8rpx;
  box-shadow: $box-shadow-sm;
}
.switch-item {
  flex: 1; text-align: center; padding: 18rpx 0;
  font-size: 28rpx; color: $text-color-regular;
  border-radius: $border-radius-md;
  transition: all 0.2s;
  display: flex; align-items: center; justify-content: center; gap: 8rpx;

  &.active {
    background: $gradient-primary;
    color: #fff;
    font-weight: bold;
  }
}

// ===== 通用卡片 =====
.menu-card {
  position: relative; z-index: 1;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  margin: 0 30rpx 24rpx;
  padding: 28rpx;
  box-shadow: $box-shadow-sm;

  .card-title {
    display: flex; justify-content: space-between; align-items: center;
    margin-bottom: 24rpx;
    font-size: 30rpx; font-weight: bold; color: $text-color-primary;

    .more {
      display: flex; align-items: center; font-size: 24rpx; color: $color-primary; font-weight: normal;
      .arrow { font-size: 32rpx; margin-left: 4rpx; }
    }
  }
}

// ===== 订单网格 =====
.grid-menu {
  display: flex; justify-content: space-between;
  .grid-item {
    display: flex; flex-direction: column; align-items: center; gap: 12rpx;
    .icon-wrap { width: 88rpx; height: 88rpx; background: #F5F3FF; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; }
    .icon { font-size: 40rpx; }
    .label { font-size: 22rpx; color: $text-color-regular; }
  }
}

// ===== 统计行 =====
.stats-row {
  display: flex;
  .stat-item {
    flex: 1; text-align: center;
    .stat-val { font-size: 40rpx; font-weight: bold; color: $text-color-primary; display: block; }
    .stat-lbl { font-size: 22rpx; color: $text-color-secondary; margin-top: 6rpx; }
  }
}

// ===== 列表菜单 =====
.list-menu { padding: 4rpx 28rpx; }
.list-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 26rpx 0;
  border-bottom: 1rpx solid $border-color-light;
  &:last-child { border-bottom: none; }

  .left {
    display: flex; align-items: center; gap: 20rpx;
    .entry-icon { font-size: 36rpx; }
    .label { font-size: 28rpx; color: $text-color-primary; }
    .sub { display: block; font-size: 22rpx; color: $text-color-secondary; margin-top: 4rpx; }
  }

  .right { display: flex; align-items: center;
    .tip { font-size: 22rpx; color: $color-primary; margin-right: 8rpx; }
    .arrow { font-size: 32rpx; color: $text-color-secondary; }
  }
}
.companion-entry {
  background: linear-gradient(135deg, #F5F3FF, #EDE9FE);
  border-radius: $border-radius-md;
  padding: 26rpx 20rpx;
  margin-bottom: 4rpx;
  border-bottom: none;
}
</style>
