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
      <view class="tab" :class="{ active: currentTab === 3 }" @click="currentTab = 3">需求跟进</view>
      <view class="tab" :class="{ active: currentTab === 4 }" @click="currentTab = 4">风控举报</view>
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
        <view class="card-row" v-if="item.address"><text class="label">地址:</text><text>{{ item.address }}</text></view>
        <view class="card-row" v-if="item.customerWechat"><text class="label">联系:</text><text>{{ item.customerWechat }}</text></view>
        <view class="card-actions" v-if="getAdminOrderActions(item).length">
          <button class="btn"
                  v-for="action in getAdminOrderActions(item)"
                  :key="action.key"
                  :class="action.primary ? 'success' : ''"
                  @click="handleOrderAction(item, action.key)">
            {{ action.label }}
          </button>
        </view>
      </view>
      <view class="empty" v-if="orderList.length === 0">暂无订单</view>
    </scroll-view>

    <!-- 需求跟进列表 -->
    <scroll-view v-if="currentTab === 3" scroll-y class="content">
      <view class="card" v-for="item in requestList" :key="item.id">
        <view class="card-row"><text class="label">发布人:</text><text>{{ item.nickname || item.userId }}</text></view>
        <view class="card-row"><text class="label">需求:</text><text>{{ item.description }}</text></view>
        <view class="card-row"><text class="label">时间:</text><text>{{ item.reserveTime || '协商' }}</text></view>
        <view class="card-row"><text class="label">地点:</text><text>{{ item.address || '协商' }}</text></view>
        <view class="card-row"><text class="label">预算:</text><text class="price">¥{{ item.budget || 0 }}</text></view>
        <view class="card-row"><text class="label">状态:</text><text>{{ getRequestStatusText(item.status, item.contactStatus) }}</text></view>
        <view class="card-actions">
          <button class="btn success" @click="updateRequest(item.id, 1, 1, '客服已联系用户')">已联系</button>
          <button class="btn success" @click="updateRequest(item.id, 1, 2, '已建立沟通群')">已建群</button>
          <button class="btn" @click="updateRequest(item.id, 2, 2, '需求已转化成单')">已成单</button>
          <button class="btn danger" @click="updateRequest(item.id, 3, 3, '无效或已关闭')">关闭</button>
        </view>
      </view>
      <view class="empty" v-if="requestList.length === 0">暂无需求</view>
    </scroll-view>

    <!-- 风控举报列表 -->
    <scroll-view v-if="currentTab === 4" scroll-y class="content">
      <view class="card" v-for="item in reportList" :key="item.id">
        <view class="card-row"><text class="label">举报人:</text><text>{{ item.reporterId }}</text></view>
        <view class="card-row"><text class="label">目标:</text><text>{{ getReportTargetText(item.targetType) }} / {{ item.targetId || item.orderId || '-' }}</text></view>
        <view class="card-row"><text class="label">原因:</text><text>{{ item.reason }}</text></view>
        <view class="card-row"><text class="label">说明:</text><text>{{ item.description || '-' }}</text></view>
        <view class="card-row"><text class="label">状态:</text><text>{{ getReportStatusText(item.status) }}</text></view>
        <view class="card-actions">
          <button class="btn" @click="handleReport(item.id, 1, '进入处理中')">处理中</button>
          <button class="btn success" @click="handleReport(item.id, 2, '已核实并处理')">已处理</button>
          <button class="btn danger" @click="handleReport(item.id, 3, '证据不足，驳回')">驳回</button>
        </view>
      </view>
      <view class="empty" v-if="reportList.length === 0">暂无举报</view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { request } from '../../utils/request';
import {useAppStore} from '../../store/app';

const appStore = useAppStore();
const currentTab = ref(0);
const companionList = ref<any[]>([]);
const withdrawalList = ref<any[]>([]);
const orderList = ref<any[]>([]);
const requestList = ref<any[]>([]);
const reportList = ref<any[]>([]);

onMounted(() => {
  loadCompanions();
  loadWithdrawals();
  loadOrders();
  loadRequests();
  loadReports();
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

const loadRequests = async () => {
  try {
    const res = await request({ url: '/admin/requests?current=1&size=50', method: 'GET' });
    if (res.code === 200) requestList.value = res.data.records || [];
  } catch (e) { /* ignore */ }
};

const loadReports = async () => {
  try {
    const res = await request({ url: '/admin/risk/reports?current=1&size=50', method: 'GET' });
    if (res.code === 200) reportList.value = res.data.records || [];
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

const handleOrderAction = (item: any, action: string) => {
  const config: Record<string, { title: string; url: string; data?: any }> = {
    group: { title: '标记客服已拉群？', url: `/admin/orders/${item.orderNo}/group-created`, data: { remark: '客服已完成三方群对接' } },
    start: { title: '标记服务开始？', url: `/admin/orders/${item.orderNo}/start`, data: { actualAddress: item.address, remark: '服务已开始' } },
    finish: { title: '核销该订单完工？', url: `/admin/orders/${item.orderNo}/finish`, data: { finishRemark: '平台核销完工', finishType: 1 } },
    settle: { title: '确认结算并放款？', url: `/admin/orders/${item.orderNo}/settle`, data: { remark: '平台结算放款' } },
    refund: { title: '确认退款并关闭该订单？', url: `/admin/orders/${item.orderNo}/refund`, data: { remark: '平台确认退款' } }
  };
  const target = config[action];
  if (!target) return;

  uni.showModal({
    title: '订单操作',
    content: target.title,
    success: async (res: any) => {
      if (!res.confirm) return;
      try {
        await request({ url: target.url, method: 'PUT', data: target.data || {} });
        uni.showToast({ title: '操作成功', icon: 'success' });
        loadOrders();
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    }
  });
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

const getOrderStatusText = (status: number) => {
  const map: Record<number, string> = {
    10:'待付款', 20:'待拉群', 30:'已拉群', 40:'双方确认', 50:'服务中',
    60:'待确认', 70:'待结算/待评价', 80:'已完成', 100:'退款申请中',
    110:'退款处理中', 120:'已退款', 130:'部分退款', 250:'已关闭'
  };
  return map[status] || String(status);
};

const updateRequest = async (id: number, status: number, contactStatus: number, adminRemark: string) => {
  try {
    await request({ url: `/admin/requests/${id}`, method: 'PUT', data: { status, contactStatus, adminRemark } });
    uni.showToast({ title: '操作成功', icon: 'success' });
    loadRequests();
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const handleReport = async (id: number, status: number, handleResult: string) => {
  try {
    await request({ url: `/admin/risk/reports/${id}`, method: 'PUT', data: { status, handleResult } });
    uni.showToast({ title: '处理成功', icon: 'success' });
    loadReports();
  } catch {
    uni.showToast({ title: '处理失败', icon: 'none' });
  }
};

const getRequestStatusText = (status: number, contactStatus: number) => {
  const statusMap: Record<number, string> = { 0: '待响应', 1: '跟进中', 2: '已成单', 3: '已关闭' };
  const contactMap: Record<number, string> = { 0: '未跟进', 1: '已联系', 2: '已建群', 3: '无效' };
  return `${statusMap[status] || status} / ${contactMap[contactStatus] || contactStatus}`;
};

const getReportTargetText = (targetType: number) => {
  const map: Record<number, string> = { 1: '用户', 2: '助教', 3: '订单', 4: '内容', 5: '需求' };
  return map[targetType] || String(targetType);
};

const getReportStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已处理', 3: '已驳回' };
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
  background: var(--bg-card);
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
  background: var(--bg-card);
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
