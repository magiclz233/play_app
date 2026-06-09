<template>
  <view :class="['container', appStore.themeClass]" :style="appStore.themeStyle">
    <view class="header-bg"></view>

    <!-- ===== 用户信息卡片 ===== -->
    <view class="user-card" v-if="userInfo">
      <image :src="userInfo.avatarUrl || defaultAvatar" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="nickname">
          {{ userInfo.nickname || t('mine.wechatUser') }}
          <text class="mode-tag" v-if="isCompanion && mode === 'companion'">{{ t('mine.companionModeTag') }}</text>
        </view>
        <view class="phone">{{ userInfo.phone ? formatPhone(userInfo.phone) : t('mine.unboundPhone') }}</view>
      </view>
    </view>
    <view class="user-card not-login" v-else @click="login">
      <image :src="defaultAvatar" mode="aspectFill" class="avatar"></image>
      <view class="info">
        <view class="nickname">{{ t('mine.clickLogin') }}</view>
        <view class="desc">{{ t('mine.loginPrompt') }}</view>
      </view>
    </view>

    <!-- ===== 角色切换 ===== -->
    <view class="role-switch" v-if="isCompanion">
      <view class="switch-bar">
        <view class="switch-item" :class="{ active: mode === 'customer' }" @click="switchMode('customer')">
          <view class="mini-icon search"></view>
          <text>{{ t('mine.customerMode') }}</text>
        </view>
        <view class="switch-item" :class="{ active: mode === 'companion' }" @click="switchMode('companion')">
          <view class="mini-icon case"></view>
          <text>{{ t('mine.companionMode') }}</text>
        </view>
      </view>
    </view>

    <!-- ==================== 客户模式 ==================== -->
    <template v-if="mode === 'customer'">
      <!-- 订单入口 -->
      <view class="menu-card">
        <view class="card-title">
          <text>{{ t('order.list') }}</text>
          <view class="more" @click="goToOrderList('')">
            <text>{{ t('mine.allOrders') }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
        <view class="grid-menu">
          <view class="grid-item" @click="goToOrderList('10')">
            <view class="icon-wrap"><text class="text-icon">¥</text></view>
            <text class="label">{{ t('mine.waitingPay') }}</text>
          </view>
          <view class="grid-item" @click="goToOrderList('20')">
            <view class="icon-wrap"><view class="line-icon list"></view></view>
            <text class="label">{{ t('mine.waitingAccept') }}</text>
          </view>
          <view class="grid-item" @click="goToOrderList('30')">
            <view class="icon-wrap"><view class="line-icon progress"></view></view>
            <text class="label">{{ t('mine.inProgress') }}</text>
          </view>
          <view class="grid-item" @click="goToOrderList('70')">
            <view class="icon-wrap"><text class="text-icon">评</text></view>
            <text class="label">{{ t('mine.waitingReview') }}</text>
          </view>
        </view>
      </view>

      <!-- 快捷入口 -->
      <view class="menu-card list-menu">
        <view class="list-item companion-entry" @click="goToCompanion">
          <view class="left">
            <text class="entry-icon">接</text>
            <view>
              <text class="label">{{ isCompanion ? t('mine.companionPortal') : t('lobby.wantToJoin') }}</text>
              <text class="sub" v-if="!isCompanion">{{ t('mine.companionEntrySub') }}</text>
              <text class="sub" v-else>{{ t('mine.companionManageSub') }}</text>
            </view>
          </view>
          <view class="right">
            <text class="tip" v-if="!isCompanion">{{ t('lobby.postBtn') }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </template>

    <!-- ==================== 陪玩模式 ==================== -->
    <template v-if="mode === 'companion'">
      <!-- 陪玩数据概览 -->
      <view class="menu-card">
        <view class="card-title"><text>{{ t('mine.todayOverview') }}</text></view>
        <view class="stats-row">
          <view class="stat-item" @click="goToCompanionOrders">
            <text class="stat-val">{{ companionStats.totalOrderCount || 0 }}</text>
            <text class="stat-lbl">{{ t('mine.totalOrders') }}</text>
          </view>
          <view class="stat-item" @click="goToWallet">
            <text class="stat-val">¥{{ companionStats.balance || '0.00' }}</text>
            <text class="stat-lbl">{{ t('mine.balance') }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-val">¥{{ companionStats.totalIncome || '0.00' }}</text>
            <text class="stat-lbl">{{ t('mine.totalIncome') }}</text>
          </view>
        </view>
      </view>

      <!-- 陪玩功能入口 -->
      <view class="menu-card list-menu">
        <view class="list-item" @click="goToCompanionOrders">
          <view class="left">
            <text class="entry-icon">单</text>
            <text class="label">{{ t('mine.orderManage') }}</text>
          </view>
          <view class="right"><text class="arrow">›</text></view>
        </view>
        <view class="list-item" @click="goToWallet">
          <view class="left">
            <text class="entry-icon">¥</text>
            <text class="label">{{ t('mine.myWallet') }}</text>
          </view>
          <view class="right">
            <text class="tip">{{ t('mine.withdraw') }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
        <view class="list-item" @click="goToCompanionProfile">
          <view class="left">
            <text class="entry-icon">人</text>
            <text class="label">{{ t('mine.profile') }}</text>
          </view>
          <view class="right"><text class="arrow">›</text></view>
        </view>
      </view>
    </template>

    <!-- ===== 系统通用设置 ===== -->
    <view class="menu-card list-menu">
      <view class="card-title"><text>{{ t('mine.sysSettings') }}</text></view>

      <!-- 主题设置 -->
      <view class="list-item" @click="appStore.toggleTheme">
        <view class="left">
          <text class="entry-icon">色</text>
          <text class="label">{{ t('mine.theme') }}</text>
        </view>
        <view class="picker-val">
          <text>{{ currentThemeText }}</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 语言设置 -->
      <view class="list-item">
        <view class="left">
          <text class="entry-icon">文</text>
          <text class="label">{{ t('mine.language') }}</text>
        </view>
        <picker :value="localeIndex" :range="localeOptions" @change="onLocaleChange">
          <view class="picker-val">
            <text>{{ localeOptions[localeIndex] }}</text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 客服 -->
      <button class="list-item feedback-btn" open-type="contact">
        <view class="left">
          <text class="entry-icon">客</text>
          <text class="label">{{ t('common.contactCS') }}</text>
        </view>
        <text class="arrow">›</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useUserStore } from '../../store/user';
import { useAppStore } from '../../store/app';
import { t } from '../../utils/i18n';

const userStore = useUserStore();
const appStore = useAppStore();

const userInfo = ref<any>(null);
const isCompanion = ref(false);
const mode = ref<'customer' | 'companion'>('customer');
const companionStats = ref<any>({});
const defaultAvatar = 'https://picsum.photos/seed/profile-default/200/200';

const currentThemeText = computed(() => appStore.currentTheme === 'light' ? t('mine.light') : t('mine.dark'));

// 语言偏好设置
const localeOptions = computed(() => [
  t('mine.zhHans'),
  t('mine.zhHant'),
  t('mine.en')
]);
const locales = ['zh-Hans', 'zh-Hant', 'en'] as const;
const localeIndex = computed(() => locales.indexOf(appStore.locale));

const onLocaleChange = (e: any) => {
  const index = e.detail.value;
  appStore.setLocale(locales[index]);
};

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
  try {
    const res = await request({ url: '/user/profile', method: 'GET' });
    if (res.code === 200) {
      userInfo.value = res.data;
      userStore.setUserInfo(res.data);
    }
  } catch (e) {
    // 未注册/本地调试异常时，若已有 mock token 则伪造本地 profile
    if (userStore.token && userStore.token.includes('mock')) {
      userInfo.value = {
        nickname: '测试用户_13800000000',
        phone: '13800000000',
        avatarUrl: defaultAvatar
      };
    }
  }
};

const checkCompanionStatus = async () => {
  try {
    const res = await request({ url: '/companion/apply/status', method: 'GET' });
    if (res.code === 200 && res.data) {
      isCompanion.value = res.data.auditStatus === 1;
    }
  } catch {
    // 默认如果已有 mock 登录，可以直接解锁助教面板进行预览
    isCompanion.value = true;
  }
};

const fetchCompanionStats = async () => {
  try {
    const res = await request({ url: '/companion/dashboard', method: 'GET' });
    if (res.code === 200) companionStats.value = res.data;
  } catch {
    // Mock 助教统计数据
    companionStats.value = {
      totalOrderCount: 16,
      balance: '820.00',
      totalIncome: '1920.00'
    };
  }
};

const switchMode = (m: 'customer' | 'companion') => {
  mode.value = m;
  if (m === 'companion') fetchCompanionStats();
};

// 模拟快捷登录
const login = async () => {
  uni.showLoading({ title: t('mine.mockLogin') });
  try {
    const res = await request({ url: '/wx/mock-login', method: 'POST', data: { phone: '13800000000' } });
    if (res.code === 200) {
      userStore.login(res.data.token, res.data.user);
      uni.showToast({ title: t('mine.loginSuccess'), icon: 'success' });
      await fetchProfile();
      await checkCompanionStatus();
    }
  } catch (e) {
    // 本地网络未联通时，直接在本地假生成 mock token 保证开发预览顺利进行
    userStore.login('mock_token_key', { nickname: '测试用户_13800000000', phone: '13800000000' });
    uni.showToast({ title: t('mine.mockLoginSuccess'), icon: 'success' });
    await fetchProfile();
    await checkCompanionStatus();
  } finally {
    uni.hideLoading();
  }
};

const goToOrderList = (status: string) => {
  if (!userStore.isLoggedIn) return uni.showToast({ title: t('common.loginRequired'), icon: 'none' });
  uni.navigateTo({ url: `/pages/order/list?status=${status}` });
};

const goToCompanion = () => {
  if (!userStore.isLoggedIn) return uni.showToast({ title: t('common.loginRequired'), icon: 'none' });
  if (isCompanion.value) {
    switchMode('companion');
  } else {
    uni.navigateTo({ url: '/pages/companion/apply' });
  }
};

const goToCompanionOrders = () => uni.navigateTo({ url: '/pages/companion/dashboard' });
const goToWallet = () => uni.navigateTo({ url: '/pages/companion/wallet' });
const goToCompanionProfile = () => uni.navigateTo({ url: '/pages/companion/apply' });

const formatPhone = (phone: string) => phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-primary);
  position: relative;
  padding-bottom: 50rpx;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 280rpx;
  background: linear-gradient(135deg, var(--color-primary), var(--color-gradient-end));
  border-radius: 0 0 40rpx 40rpx;
  z-index: 0;
}

// ===== 用户卡片 =====
.user-card {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 60rpx 40rpx 30rpx;

  .avatar {
    width: 100rpx; height: 100rpx;
    border-radius: 50%;
    border: 4rpx solid var(--bg-card);
    margin-right: 24rpx;
    flex-shrink: 0;
  }

  .info {
    flex: 1; color: #fff;
    .nickname {
      font-size: var(--font-size-lg); font-weight: bold; margin-bottom: 6rpx;
      display: flex; align-items: center; gap: 12rpx;
    }
    .mode-tag {
      font-size: 18rpx; font-weight: normal;
      background: rgba(255,255,255,0.25); padding: 4rpx 14rpx; border-radius: 20rpx;
    }
    .phone { font-size: 24rpx; opacity: 0.9; }
    .desc { font-size: 22rpx; opacity: 0.8; }
  }
}

// ===== 角色切换 =====
.role-switch {
  position: relative; z-index: 1;
  margin: 0 30rpx 24rpx;
}
.switch-bar {
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 8rpx;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
}
.switch-item {
  flex: 1; text-align: center; padding: 18rpx 0;
  font-size: var(--font-size-sm); color: var(--text-secondary);
  border-radius: var(--radius-md);
  transition: all 0.2s;
  display: flex; align-items: center; justify-content: center; gap: 8rpx;

  &.active {
    background: var(--color-primary);
    color: #fff;
    font-weight: bold;
  }
}

.mini-icon {
  width: 28rpx;
  height: 28rpx;
  position: relative;
  box-sizing: border-box;

  &.search {
    border: 3rpx solid currentColor;
    border-radius: 50%;

    &::after {
      content: '';
      position: absolute;
      width: 10rpx;
      height: 3rpx;
      right: -8rpx;
      bottom: -4rpx;
      background: currentColor;
      border-radius: var(--radius-full);
      transform: rotate(45deg);
    }
  }

  &.case {
    border: 3rpx solid currentColor;
    border-radius: 6rpx;

    &::before {
      content: '';
      position: absolute;
      left: 7rpx;
      top: -8rpx;
      width: 10rpx;
      height: 8rpx;
      border: 3rpx solid currentColor;
      border-bottom: 0;
      border-radius: 6rpx 6rpx 0 0;
      box-sizing: border-box;
    }
  }
}

// ===== 通用卡片 =====
.menu-card {
  position: relative; z-index: 1;
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  margin: 0 30rpx 24rpx;
  padding: 28rpx;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);

  .card-title {
    display: flex; justify-content: space-between; align-items: center;
    margin-bottom: 24rpx;
    font-size: 28rpx; font-weight: bold; color: var(--text-primary);

    .more {
      display: flex; align-items: center; font-size: 24rpx; color: var(--color-primary); font-weight: normal;
      .arrow { font-size: 32rpx; margin-left: 4rpx; }
    }
  }
}

// ===== 订单网格 =====
.grid-menu {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;

  .grid-item {
    display: flex; flex-direction: column; align-items: center; gap: 12rpx;
    .icon-wrap {
      width: 88rpx; height: 88rpx;
      background: var(--bg-main);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      display: flex; align-items: center; justify-content: center;
    }
    .text-icon {
      font-size: 34rpx;
      color: var(--color-primary);
      font-weight: 700;
      font-family: Outfit, Inter, -apple-system, BlinkMacSystemFont, sans-serif;
    }
    .line-icon {
      width: 34rpx;
      height: 34rpx;
      position: relative;

      &.list::before,
      &.list::after,
      &.progress::before,
      &.progress::after {
        content: '';
        position: absolute;
        background: var(--color-primary);
      }

      &.list::before {
        left: 4rpx;
        top: 7rpx;
        width: 26rpx;
        height: 4rpx;
        border-radius: var(--radius-full);
        box-shadow: 0 9rpx 0 var(--color-primary), 0 18rpx 0 var(--color-primary);
      }

      &.progress {
        border: 4rpx solid var(--color-primary);
        border-left-color: rgba(var(--color-primary-rgb), 0.18);
        border-radius: 50%;
        box-sizing: border-box;
      }
    }
    .label { font-size: 22rpx; color: var(--text-primary); }
  }
}

// ===== 统计行 =====
.stats-row {
  display: flex;
  .stat-item {
    flex: 1; text-align: center;
    .stat-val { font-size: 36rpx; font-weight: bold; color: var(--text-primary); display: block; }
    .stat-lbl { font-size: 22rpx; color: var(--text-secondary); margin-top: 6rpx; }
  }
}

// ===== 列表菜单 =====
.list-menu { padding: 4rpx 28rpx; }
.list-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 26rpx 0;
  border-bottom: 1rpx solid var(--border-color);
  background: transparent;
  width: 100%;
  border-radius: 0;
  margin: 0;
  line-height: normal;
  text-align: left;

  &::after { border: none; }

  .left {
    display: flex; align-items: center; gap: 20rpx;
    .entry-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 48rpx;
      height: 48rpx;
      color: var(--color-primary);
      background: rgba(var(--color-primary-rgb), 0.08);
      border: 1px solid rgba(var(--color-primary-rgb), 0.12);
      border-radius: var(--radius-md);
      font-size: 24rpx;
      font-weight: 700;
      box-sizing: border-box;
    }
    .label { font-size: 26rpx; color: var(--text-primary); }
    .sub { display: block; font-size: 20rpx; color: var(--text-secondary); margin-top: 4rpx; }
  }

  .right {
    display: flex; align-items: center;
    .tip { font-size: 22rpx; color: var(--color-primary); margin-right: 8rpx; }
  }

  .arrow { font-size: 32rpx; color: var(--text-muted); }

  .picker-val {
    display: flex;
    align-items: center;
    color: var(--text-secondary);
    font-size: 24rpx;
  }
}

.feedback-btn {
  padding: 26rpx 0;
  background: transparent;
  border: none;
  color: var(--text-primary);
  border-bottom: none;
}

.companion-entry {
  background: linear-gradient(135deg, var(--color-primary-light), rgba(var(--color-primary-rgb), 0.05));
  border-radius: var(--radius-md);
  padding: 26rpx 20rpx;
  margin-bottom: 4rpx;
  border-bottom: none;
  border: 1px solid rgba(var(--color-primary-rgb), 0.15);

  .theme-dark & {
    background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.15), rgba(var(--color-primary-rgb), 0.06));
  }
}
</style>
