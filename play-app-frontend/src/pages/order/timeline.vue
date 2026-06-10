<template>
  <view class="container" :class="appStore.themeClass">
    <view class="timeline">
      <view class="event" v-for="item in events" :key="item.id">
        <view class="dot" :style="{ background: dotColor(item.eventType) }"></view>
        <view class="line" v-if="item !== events[events.length-1]"></view>
        <view class="card">
          <view class="card-header">
            <text class="type">{{ eventLabel(item.eventType) }}</text>
            <text class="time">{{ formatTime(item.createTime) }}</text>
          </view>
          <text class="desc" v-if="item.eventDesc">{{ item.eventDesc }}</text>
        </view>
      </view>
      <view class="empty" v-if="events.length === 0 && !loading">
        <text>暂无服务记录</text>
      </view>
    </view>

    <!-- 添加快捷按钮 -->
    <view class="fab" @click="showPanel = !showPanel">
      <text class="fab-icon">{{ showPanel ? '×' : '+' }}</text>
    </view>
    <view class="action-panel" v-if="showPanel">
      <view class="action" v-for="act in quickActions" :key="act.type" @click="addEvent(act.type, act.label)">
        <text>{{ act.emoji }}</text>
        <text>{{ act.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAppStore } from '../../store/app';

const appStore = useAppStore();
const orderNo = ref('');
const events = ref<any[]>([]);
const loading = ref(false);
const showPanel = ref(false);

const quickActions = [
  { type: 5, label: '拍照打卡', emoji: '📸' },
  { type: 1, label: '出发前往', emoji: '🚗' },
  { type: 2, label: '到达现场', emoji: '📍' },
  { type: 4, label: '开始服务', emoji: '▶️' },
];

const eventLabels: Record<number, string> = {
  1: '出发前往', 2: '到达现场', 3: '确认见面', 4: '服务开始',
  5: '拍照打卡', 6: '申请完工', 7: '确认完工', 8: '客服备注',
  9: '发起投诉', 10: '异常上报'
};

const dotColors = ['', '#3B82F6','#10B981','#F59E0B','#8B5CF6','#EC4899','#EF4444','#10B981','#6B7280','#EF4444','#F97316'];

const dotColor = (type: number) => dotColors[type] || '#6B7280';
const eventLabel = (type: number) => eventLabels[type] || `事件${type}`;
const formatTime = (t: string) => t ? t.replace('T',' ').substring(0,16) : '';

onLoad((opts: any) => { orderNo.value = opts.orderNo || ''; fetchTimeline(); });

const fetchTimeline = async () => {
  loading.value = true;
  try {
    const res = await request({ url: `/orders/${orderNo.value}/timeline`, method: 'GET' });
    if (res.code === 200) events.value = res.data;
  } catch (_) {} finally { loading.value = false; }
};

const addEvent = async (eventType: number, label: string) => {
  try {
    const res = await request({ url: `/orders/${orderNo.value}/timeline`, method: 'POST', data: { eventType, eventDesc: label, operatorRole: 1 } });
    if (res.code === 200) { fetchTimeline(); uni.showToast({ title: '已记录', icon: 'success' }); showPanel.value = false; }
  } catch (_) { uni.showToast({ title: '操作失败', icon: 'none' }); }
};
</script>

<style lang="scss" scoped>
.container { min-height: 100vh; background: $bg-color-page; padding: 40rpx; box-sizing: border-box; }
.timeline { position: relative; padding-left: 60rpx; }
.event { position: relative; margin-bottom: 30rpx; }
.dot { position: absolute; left: -52rpx; top: 16rpx; width: 24rpx; height: 24rpx; border-radius: 50%; z-index: 1; }
.line { position: absolute; left: -41rpx; top: 40rpx; width: 2rpx; height: calc(100% + 30rpx); background: $border-color-light; }
.card { background: $bg-color-white; border-radius: $border-radius-md; padding: 24rpx; box-shadow: $box-shadow-sm; }
.card-header { display: flex; justify-content: space-between; margin-bottom: 12rpx; }
.type { font-weight: bold; font-size: 28rpx; color: $text-color-primary; }
.time { font-size: 22rpx; color: $text-color-secondary; }
.desc { font-size: 26rpx; color: $text-color-regular; line-height: 1.5; }
.empty { text-align: center; padding: 100rpx 0; color: $text-color-secondary; }

.fab { position: fixed; bottom: 120rpx; right: 40rpx; width: 96rpx; height: 96rpx; background: $gradient-primary; border-radius: 50%; display: flex; align-items: center; justify-content: center; z-index: 99; box-shadow: 0 8px 24px rgba(255,59,92,0.3); }
.fab-icon { color: #fff; font-size: 48rpx; font-weight: bold; }
.action-panel { position: fixed; bottom: 240rpx; right: 20rpx; background: $bg-color-white; border-radius: $border-radius-lg; padding: 20rpx; z-index: 99; box-shadow: $box-shadow-sm; display: flex; flex-wrap: wrap; width: 300rpx; }
.action { width: 50%; text-align: center; padding: 20rpx 0; font-size: 24rpx; color: $text-color-primary; }
</style>
