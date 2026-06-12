<template>
  <view class="container">
    <!-- Dashboard 概览 -->
    <view class="dashboard" v-if="currentTab === 0">
      <view class="stats-grid">
        <view class="stat-card" v-for="stat in dashboardStats" :key="stat.key">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>
      <view class="quick-actions">
        <text class="section-title">快捷操作</text>
        <view class="action-grid">
          <view class="action-item" v-for="action in quickActions" :key="action.tab" @click="currentTab = action.tab">
            <text class="action-icon">{{ action.icon }}</text>
            <text class="action-label">{{ action.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Tab 切换 -->
    <view class="tabs">
      <view class="tab-scroll">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          class="tab"
          :class="{ active: currentTab === index }"
          @click="currentTab = index"
          aria-label="切换到{{ tab }}选项卡"
        >{{ tab }}</view>
      </view>
    </view>

    <!-- 助教审核 -->
    <scroll-view v-if="currentTab === 1" scroll-y class="content" @scrolltolower="loadMoreCompanions">
      <SkeletonCard variant="compact" :count="3" v-if="companionLoading" />
      <view class="card" v-for="item in companionList" :key="item.userId" hover-class="card-hover">
        <view class="card-row">
          <text class="label">ID</text><text>{{ item.userId }}</text>
        </view>
        <view class="card-row">
          <text class="label">姓名</text><text>{{ item.realName }}</text>
        </view>
        <view class="card-row">
          <text class="label">状态</text>
          <StatusBadge type="audit" :status="item.auditStatus" />
        </view>
        <view class="card-row" v-if="item.summary">
          <text class="label">简介</text><text class="text-ellipsis">{{ item.summary }}</text>
        </view>
        <view class="card-actions">
          <button class="btn success" hover-class="btn-hover" @click="auditCompanion(item.userId, true)">通过</button>
          <button class="btn danger" hover-class="btn-hover" @click="showReject(item.userId)">驳回</button>
        </view>
      </view>
      <EmptyState text="暂无待审核助教" hint="新的入驻申请将出现在这里" v-if="!companionLoading && companionList.length === 0" />
    </scroll-view>

    <!-- 提现审核 -->
    <scroll-view v-if="currentTab === 1" scroll-y class="content" @scrolltolower="loadMoreWithdrawals">
      <SkeletonCard variant="compact" :count="3" v-if="withdrawalLoading" />
      <view class="card" v-for="item in withdrawalList" :key="item.id" hover-class="card-hover">
        <view class="card-row">
          <text class="label">申请人</text><text>{{ item.companionId }}</text>
        </view>
        <view class="card-row">
          <text class="label">金额</text><text class="highlight">{{ formatPrice(item.amount) }}</text>
        </view>
        <view class="card-row">
          <text class="label">申请时间</text><text>{{ formatTime(item.createTime) }}</text>
        </view>
        <view class="card-actions">
          <button class="btn success" hover-class="btn-hover" @click="auditWithdrawal(item.id, 1)">通过</button>
          <button class="btn danger" hover-class="btn-hover" @click="auditWithdrawal(item.id, 2)">驳回</button>
        </view>
      </view>
      <EmptyState text="暂无待审核提现" hint="陪玩发起提现后将显示在这里" v-if="!withdrawalLoading && withdrawalList.length === 0" />
    </scroll-view>

    <!-- 订单管理 -->
    <scroll-view v-if="currentTab === 2" scroll-y class="content" @scrolltolower="loadMoreOrders">
      <SkeletonCard variant="order" :count="3" v-if="orderLoading" />
      <view class="card" v-for="item in orderList" :key="item.orderNo" hover-class="card-hover">
        <view class="card-row">
          <text class="label">订单号</text><text class="text-ellipsis">{{ item.orderNo }}</text>
        </view>
        <view class="card-row">
          <text class="label">客户</text><text>{{ item.userId }}</text>
        </view>
        <view class="card-row">
          <text class="label">助教</text><text>{{ item.companionId }}</text>
        </view>
        <view class="card-row">
          <text class="label">金额</text><text class="highlight">{{ formatPrice(item.totalAmount) }}</text>
        </view>
        <view class="card-row">
          <text class="label">状态</text>
          <StatusBadge type="order" :status="item.status" />
        </view>
        <view class="card-actions" v-if="getAdminOrderActions(item).length">
          <button
            class="btn"
            v-for="action in getAdminOrderActions(item)"
            :key="action.key"
            :class="action.primary ? 'success' : ''"
            hover-class="btn-hover"
            @click="handleOrderAction(item, action.key)"
          >{{ action.label }}</button>
        </view>
      </view>
      <EmptyState text="暂无订单" hint="新的订单将显示在这里" v-if="!orderLoading && orderList.length === 0" />
    </scroll-view>

    <!-- 需求跟进 -->
    <scroll-view v-if="currentTab === 3" scroll-y class="content" @scrolltolower="loadMoreRequests">
      <SkeletonCard variant="compact" :count="3" v-if="requestLoading" />
      <view class="card" v-for="item in requestList" :key="item.id" hover-class="card-hover">
        <view class="card-row">
          <text class="label">发布人</text><text>{{ item.nickname || item.userId }}</text>
        </view>
        <view class="card-row">
          <text class="label">需求</text><text class="text-ellipsis">{{ item.description }}</text>
        </view>
        <view class="card-row">
          <text class="label">预算</text><text class="highlight">{{ formatPrice(item.budget || 0) }}</text>
        </view>
        <view class="card-row">
          <text class="label">状态</text>
          <StatusBadge type="audit" :status="item.status" />
        </view>
        <view class="card-actions">
          <button class="btn success" hover-class="btn-hover" @click="updateRequest(item.id, 1, 1, '客服已联系用户')">已联系</button>
          <button class="btn success" hover-class="btn-hover" @click="updateRequest(item.id, 1, 2, '已建立沟通群')">已建群</button>
          <button class="btn" hover-class="btn-hover" @click="updateRequest(item.id, 2, 2, '需求已转化成单')">已成单</button>
          <button class="btn danger" hover-class="btn-hover" @click="updateRequest(item.id, 3, 3, '无效或已关闭')">关闭</button>
        </view>
      </view>
      <EmptyState text="暂无需求" hint="用户发布的需求将显示在这里" v-if="!requestLoading && requestList.length === 0" />
    </scroll-view>

    <!-- 风控举报 -->
    <scroll-view v-if="currentTab === 4" scroll-y class="content" @scrolltolower="loadMoreReports">
      <SkeletonCard variant="compact" :count="3" v-if="reportLoading" />
      <view class="card" v-for="item in reportList" :key="item.id" hover-class="card-hover">
        <view class="card-row">
          <text class="label">举报人</text><text>{{ item.reporterId }}</text>
        </view>
        <view class="card-row">
          <text class="label">目标</text><text>{{ getReportTargetText(item.targetType) }} / ID:{{ item.targetId || item.orderId || '-' }}</text>
        </view>
        <view class="card-row">
          <text class="label">原因</text><text class="text-ellipsis">{{ item.reason }}</text>
        </view>
        <view class="card-row">
          <text class="label">状态</text>
          <StatusBadge type="audit" :status="item.status" />
        </view>
        <view class="card-actions">
          <button class="btn" hover-class="btn-hover" @click="handleReport(item.id, 1, '进入处理中')">处理中</button>
          <button class="btn success" hover-class="btn-hover" @click="handleReport(item.id, 2, '已核实并处理')">已处理</button>
          <button class="btn danger" hover-class="btn-hover" @click="handleReport(item.id, 3, '证据不足，驳回')">驳回</button>
        </view>
      </view>
      <EmptyState text="暂无举报" hint="用户提交的举报将显示在这里" v-if="!reportLoading && reportList.length === 0" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { adminApi } from '../../api/admin';
import EmptyState from '../../components/EmptyState.vue';
import StatusBadge from '../../components/StatusBadge.vue';
import SkeletonCard from '../../components/SkeletonCard.vue';

// ==================== Tab 定义 ====================
const tabs = ['概览', '助教审核', '提现审核', '订单管理', '需求跟进', '风控举报'];
const currentTab = ref(0);

// ==================== Dashboard ====================
const dashboardStats = ref([
  { key: 'todayOrders', label: '今日订单', value: 0 },
  { key: 'todayRevenue', label: '今日收入', value: '¥0' },
  { key: 'pendingAudit', label: '待审核入驻', value: 0 },
  { key: 'pendingWithdraw', label: '待处理提现', value: 0 },
  { key: 'activeCompanions', label: '活跃助教', value: 0 },
  { key: 'totalUsers', label: '注册用户', value: 0 },
]);

const quickActions = ref([
  { tab: 1, icon: '审', label: '助教审核' },
  { tab: 3, icon: '单', label: '订单管理' },
  { tab: 2, icon: '提', label: '提现审核' },
  { tab: 5, icon: '控', label: '风控举报' },
  { tab: 4, icon: '需', label: '需求跟进' },
]);

const loadDashboard = async () => {
  try {
    const res = await adminApi.getDashboard();
    if (res.code === 200 && res.data) {
      const d = res.data;
      dashboardStats.value = [
        { key: 'todayOrders', label: '今日订单', value: d.todayOrderCount || 0 },
        { key: 'todayRevenue', label: '今日收入', value: `¥${d.todayRevenue || 0}` },
        { key: 'pendingAudit', label: '待审核入驻', value: d.pendingAuditCount || 0 },
        { key: 'pendingWithdraw', label: '待处理提现', value: d.pendingWithdrawCount || 0 },
        { key: 'activeCompanions', label: '活跃助教', value: d.activeCompanionCount || 0 },
        { key: 'totalUsers', label: '注册用户', value: d.totalUserCount || 0 },
      ];
    }
  } catch (e) { /* ignore */ }
};

// ==================== 助教审核 ====================
const companionList = ref<any[]>([]);
const companionLoading = ref(false);
const loadCompanions = async () => {
  companionLoading.value = true;
  try {
    const res = await adminApi.getPendingCompanions();
    if (res.code === 200) companionList.value = res.data?.records || [];
  } catch (e) { /* ignore */ }
  finally { companionLoading.value = false; }
};
const loadMoreCompanions = () => { /* 分页预留 */ };

const auditCompanion = async (companionId: number, isPass: boolean) => {
  try {
    await adminApi.auditCompanion(companionId, { approved: isPass, rejectReason: '' });
    uni.showToast({ title: isPass ? '已通过' : '已驳回', icon: 'success' });
    loadCompanions();
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }); }
};

const showReject = (companionId: number) => {
  uni.showModal({
    title: '驳回原因',
    editable: true,
    placeholderText: '请输入驳回原因',
    success: async (res: any) => {
      if (res.confirm) {
        try {
          await adminApi.auditCompanion(companionId, { approved: false, rejectReason: res.content || '' });
          uni.showToast({ title: '已驳回', icon: 'success' });
          loadCompanions();
        } catch { uni.showToast({ title: '操作失败', icon: 'none' }); }
      }
    }
  });
};

// ==================== 提现审核 ====================
const withdrawalList = ref<any[]>([]);
const withdrawalLoading = ref(false);
const loadWithdrawals = async () => {
  withdrawalLoading.value = true;
  try {
    const res = await adminApi.getWithdrawals(0);
    if (res.code === 200) withdrawalList.value = res.data?.records || [];
  } catch (e) { /* ignore */ }
  finally { withdrawalLoading.value = false; }
};
const loadMoreWithdrawals = () => { /* 分页预留 */ };

const auditWithdrawal = async (id: number, status: number) => {
  try {
    await adminApi.auditWithdrawal(id, { status });
    uni.showToast({ title: status === 1 ? '已通过' : '已驳回', icon: 'success' });
    loadWithdrawals();
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }); }
};

// ==================== 订单管理 ====================
const orderList = ref<any[]>([]);
const orderLoading = ref(false);
const loadOrders = async () => {
  orderLoading.value = true;
  try {
    const res = await adminApi.getOrders({ current: 1, size: 50 });
    if (res.code === 200) orderList.value = res.data?.records || [];
  } catch (e) { /* ignore */ }
  finally { orderLoading.value = false; }
};
const loadMoreOrders = () => { /* 分页预留 */ };

const orderStatusTextMap: Record<number, string> = {
  10:'待付款', 20:'待拉群', 30:'已拉群', 40:'双方确认', 50:'服务中',
  60:'待确认', 70:'待结算', 80:'已完成', 100:'退款申请中',
  110:'退款处理中', 120:'已退款', 130:'部分退款', 250:'已关闭'
};

const getAdminOrderActions = (item: any) => {
  const status = item.status;
  if (status === 20) return [{ key: 'group', label: '标记拉群', primary: true }];
  if (status === 30 || status === 40) return [{ key: 'start', label: '服务开始', primary: true }, { key: 'finish', label: '直接核销' }];
  if (status === 50 || status === 60) return [{ key: 'finish', label: '核销完工', primary: true }];
  if (status === 70) return [{ key: 'settle', label: '结算放款', primary: true }];
  if (status === 100 || status === 110) return [{ key: 'refund', label: '确认退款', primary: true }];
  return [];
};

const handleOrderAction = (item: any, action: string) => {
  const config: Record<string, { title: string; fn: () => Promise<any> }> = {
    group: {
      title: '标记客服已拉群？',
      fn: () => adminApi.markGroupCreated(item.orderNo, '客服已完成三方群对接')
    },
    start: {
      title: '标记服务开始？',
      fn: () => adminApi.startService(item.orderNo, { actualAddress: item.address, remark: '服务已开始' })
    },
    finish: {
      title: '核销该订单完工？',
      fn: () => adminApi.confirmFinish(item.orderNo, { finishRemark: '平台核销完工', finishType: 1 })
    },
    settle: {
      title: '确认结算并放款？',
      fn: () => adminApi.settle(item.orderNo, '平台结算放款')
    },
    refund: {
      title: '确认退款并关闭该订单？',
      fn: () => adminApi.approveRefund(item.orderNo, '平台确认退款')
    }
  };
  const target = config[action];
  if (!target) return;

  uni.showModal({
    title: '订单操作',
    content: target.title,
    success: async (res: any) => {
      if (!res.confirm) return;
      try {
        await target.fn();
        uni.showToast({ title: '操作成功', icon: 'success' });
        loadOrders();
      } catch { uni.showToast({ title: '操作失败', icon: 'none' }); }
    }
  });
};

// ==================== 需求跟进 ====================
const requestList = ref<any[]>([]);
const requestLoading = ref(false);
const loadRequests = async () => {
  requestLoading.value = true;
  try {
    const res = await adminApi.getRequests();
    if (res.code === 200) requestList.value = res.data?.records || [];
  } catch (e) { /* ignore */ }
  finally { requestLoading.value = false; }
};
const loadMoreRequests = () => { /* 分页预留 */ };

const updateRequest = async (id: number, status: number, contactStatus: number, adminRemark: string) => {
  try {
    await adminApi.updateRequest(id, { status, contactStatus, adminRemark });
    uni.showToast({ title: '操作成功', icon: 'success' });
    loadRequests();
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }); }
};

// ==================== 风控举报 ====================
const reportList = ref<any[]>([]);
const reportLoading = ref(false);
const loadReports = async () => {
  reportLoading.value = true;
  try {
    const res = await adminApi.getRiskReports();
    if (res.code === 200) reportList.value = res.data?.records || [];
  } catch (e) { /* ignore */ }
  finally { reportLoading.value = false; }
};
const loadMoreReports = () => { /* 分页预留 */ };

const handleReport = async (id: number, status: number, handleResult: string) => {
  try {
    await adminApi.handleRiskReport(id, { status, handleResult });
    uni.showToast({ title: '处理成功', icon: 'success' });
    loadReports();
  } catch { uni.showToast({ title: '处理失败', icon: 'none' }); }
};

const getReportTargetText = (targetType: number) => {
  const map: Record<number, string> = { 1: '用户', 2: '助教', 3: '订单', 4: '内容', 5: '需求' };
  return map[targetType] || String(targetType);
};

// ==================== 工具函数 ====================
const formatPrice = (amount: number) => `¥${(amount || 0).toFixed(2)}`;
const formatTime = (time: string) => time ? time.replace('T', ' ').substring(0, 19) : '-';

// ==================== 初始化 ====================
onMounted(() => {
  loadDashboard();
  loadCompanions();
  loadWithdrawals();
  loadOrders();
  loadRequests();
  loadReports();
});
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);
}

// ===== Dashboard =====
.dashboard {
  padding: 24rpx;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}
.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24rpx;
  text-align: center;
  .stat-value {
    display: block;
    font-size: var(--font-size-xl);
    font-weight: bold;
    color: var(--color-primary);
  }
  .stat-label {
    display: block;
    font-size: var(--font-size-xs);
    color: var(--text-secondary);
    margin-top: 8rpx;
  }
}
.section-title {
  font-size: var(--font-size-md);
  font-weight: bold;
  color: var(--text-primary);
  display: block;
  margin: 24rpx 0 16rpx;
}
.quick-actions {
  margin-top: 24rpx;
}
.action-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16rpx;
}
.action-item {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24rpx 16rpx;
  text-align: center;
  .action-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64rpx;
    height: 64rpx;
    border-radius: var(--radius-full);
    background: var(--color-primary-light);
    color: var(--color-primary);
    font-size: var(--font-size-md);
    font-weight: bold;
    margin: 0 auto 12rpx;
  }
  .action-label {
    font-size: var(--font-size-xs);
    color: var(--text-secondary);
  }
}

// ===== Tabs =====
.tabs {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  overflow: hidden;
}
.tab-scroll {
  display: flex;
  white-space: nowrap;
  padding: 0 12rpx;
}
.tab {
  flex-shrink: 0;
  padding: 24rpx 20rpx;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  border-bottom: 4rpx solid transparent;
  transition: all 0.2s ease;
  &.active {
    color: var(--color-primary);
    font-weight: bold;
    border-bottom-color: var(--color-primary);
  }
}

// ===== Content =====
.content {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
}

// ===== Cards =====
.card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.card-hover {
  opacity: 0.85;
  transform: scale(0.98);
}
.card-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  font-size: var(--font-size-sm);
  line-height: 1.5;
  .label {
    width: 140rpx;
    color: var(--text-secondary);
    flex-shrink: 0;
  }
  .highlight {
    color: var(--color-primary);
    font-weight: bold;
  }
  .text-ellipsis {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
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
    font-size: var(--font-size-xs);
    border-radius: var(--radius-full);
    background: var(--bg-subtle);
    color: var(--text-primary);
    &.success {
      background: var(--color-success);
      color: #fff;
    }
    &.danger {
      background: var(--color-error);
      color: #fff;
    }
  }
  .btn-hover {
    opacity: 0.8;
  }
}
</style>
