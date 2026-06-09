<template>
  <view :class="['container', appStore.themeClass]" :style="appStore.themeStyle">
    <!-- 顶部状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 顶部搜索与定位 -->
    <view class="header">
      <view class="location">
        <view class="loc-icon"></view>
        <text class="city">{{ currentCity }}</text>
      </view>
      <view class="search-box">
        <view class="search-icon"></view>
        <input type="text" :placeholder="t('lobby.searchPlaceholder')" placeholder-class="placeholder" />
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <!-- 轮播图 Banner -->
      <swiper class="banner-swiper" circular autoplay interval="3000" indicator-dots indicator-active-color="var(--color-primary)">
        <swiper-item v-for="(item, index) in bannerList" :key="index" class="swiper-item">
          <image :src="item.image" mode="aspectFill" class="banner-img"></image>
        </swiper-item>
      </swiper>

      <!-- 快速入驻 Banner -->
      <view class="companion-banner" @click="goToCompanion">
        <view class="banner-text">
          <text class="banner-title">{{ t('lobby.goToCompanion') }}</text>
          <text class="banner-sub">{{ t('lobby.goToCompanionSub') }}</text>
        </view>
        <text class="banner-arrow">›</text>
      </view>

      <!-- 服务分类 -->
      <view class="category-grid" v-if="loadingCategories">
        <view class="category-item skeleton-category" v-for="i in 8" :key="i">
          <view class="icon-wrap skeleton-block"></view>
          <view class="category-name skeleton-line"></view>
        </view>
      </view>
      <view class="category-grid" v-else-if="categoryList.length > 0">
        <view class="category-item" v-for="(item, index) in categoryList" :key="index" @click="goToCategory(item.id)">
          <view class="icon-wrap">
            <image v-if="item.icon" :src="item.icon" mode="aspectFit" class="category-icon"></image>
            <text v-else class="category-fallback">{{ item.name.slice(0, 1) }}</text>
          </view>
          <text class="category-name">{{ item.name }}</text>
        </view>
      </view>

      <!-- 魅力排行榜 (Crown Rankings) -->
      <view class="ranking-section" v-if="rankList.length > 0">
        <view class="section-title">
          <text class="title-text">{{ t('lobby.popularRank') }}</text>
        </view>
        <view class="ranking-cards">
          <view v-for="(item, index) in rankList.slice(0, 3)" :key="index" :class="['rank-card', 'rank-' + (index + 1)]" @click="goToDetail(item.userId)">
            <view class="rank-crown">
              <text class="crown-mark" v-if="index === 0">TOP</text>
              <text class="crown-badge" v-else>{{ index + 1 }}</text>
            </view>
            <image :src="item.coverUrl" mode="aspectFill" class="rank-avatar"></image>
            <view class="rank-info">
              <text class="rank-name">{{ item.nickname }}</text>
              <text class="rank-tag" v-if="index === 0">{{ t('lobby.rank1') }}</text>
              <text class="rank-price">{{ t('lobby.priceUnit', { price: item.pricePerHour }) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 标签页切换 -->
      <view class="tabs-header">
        <view class="tab-item" :class="{ active: activeTab === 'recommend' }" @click="activeTab = 'recommend'">
          <text>{{ t('lobby.recommend') }}</text>
        </view>
        <view class="tab-item" :class="{ active: activeTab === 'requests' }" @click="activeTab = 'requests'">
          <text>{{ t('lobby.requestHub') }}</text>
        </view>
      </view>

      <!-- 陪玩卡片列表 -->
      <view class="companion-list" v-if="activeTab === 'recommend' && loadingCompanions">
        <view class="companion-card skeleton-card" v-for="i in 4" :key="i">
          <view class="card-cover-wrap skeleton-block"></view>
          <view class="card-info">
            <view class="skeleton-line wide"></view>
            <view class="skeleton-line short"></view>
            <view class="skeleton-line"></view>
          </view>
        </view>
      </view>
      <view class="companion-list" v-else-if="activeTab === 'recommend' && companionList.length > 0">
        <view class="companion-card" v-for="(item, index) in companionList" :key="index" @click="goToDetail(item.userId)">
          <view class="card-cover-wrap">
            <image :src="item.coverUrl" mode="aspectFill" class="card-cover"></image>
            <view class="status-badge" :class="{ 'busy': item.workStatus !== 1 }">
              <text class="dot"></text>
              <text>{{ item.workStatus === 1 ? t('lobby.available') : t('lobby.busy') }}</text>
            </view>
          </view>

          <view class="card-info">
            <view class="info-top">
              <text class="name">{{ item.nickname }}</text>
              <view class="rating">
                <text class="star">★</text>
                <text class="score">{{ item.rating }}</text>
              </view>
            </view>

            <!-- 语音微型播放气泡 -->
            <view class="voice-capsule" v-if="item.voiceUrl" @click.stop="togglePlayVoice(item)">
              <view class="voice-icon" :class="{ pause: playingVoiceUrl === item.voiceUrl }"></view>
              <text class="voice-dur">{{ item.voiceDuration || 10 }}s</text>
            </view>

            <view class="tags">
              <text class="tag" v-for="(tag, tIdx) in item.tags" :key="tIdx">{{ tag }}</text>
            </view>
            <view class="info-bottom">
              <text class="price">{{ t('lobby.priceUnit', { price: item.pricePerHour }) }}</text>
              <text class="distance">{{ item.distance || '1.0km' }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else-if="activeTab === 'recommend'">
        <text>{{ t('lobby.emptyRecommend') }}</text>
      </view>

      <!-- 需求大厅列表 -->
      <view class="request-list" v-if="activeTab === 'requests' && loadingRequests">
        <view class="request-card skeleton-request" v-for="i in 3" :key="i">
          <view class="skeleton-line wide"></view>
          <view class="skeleton-line"></view>
          <view class="skeleton-line short"></view>
        </view>
      </view>
      <view class="request-list" v-else-if="activeTab === 'requests'">
        <view class="request-card" v-for="(req, index) in requestList" :key="index">
          <view class="req-header">
            <view class="req-user">
              <image :src="req.avatarUrl" mode="aspectFill" class="req-avatar"></image>
              <text class="req-name">{{ req.nickname }}</text>
            </view>
            <text class="req-time">{{ req.timeAgo }}</text>
          </view>
          <view class="req-body">
            <text class="req-desc">{{ req.description }}</text>
            <view class="req-details">
              <text class="req-detail-item"><text class="detail-label">{{ t('common.time') }}</text>{{ req.reserveTime }}</text>
              <text class="req-detail-item"><text class="detail-label">{{ t('common.place') }}</text>{{ req.address }}</text>
            </view>
          </view>
          <view class="req-footer">
            <text class="req-price">{{ t('lobby.priceUnit', { price: req.price }) }}</text>
            <button class="req-btn" @click="applyRequest(req)">{{ t('common.contactCS') }}</button>
          </view>
        </view>
      </view>
      <view class="empty-state" v-if="activeTab === 'requests' && !loadingRequests && requestList.length === 0">
        <text>{{ t('lobby.emptyRequests') }}</text>
      </view>

      <view class="loading-more" v-if="!loadingCompanions">
        <text>{{ t('common.noMore') }}</text>
      </view>
    </scroll-view>

    <!-- 发布需求悬浮按钮 -->
    <view class="fab-btn" v-if="activeTab === 'requests'" @click="showPostModal = true">
      <view class="fab-icon"></view>
      <text class="fab-text">{{ t('lobby.postBtn') }}</text>
    </view>

    <!-- 发布需求弹窗 -->
    <view class="post-modal" v-if="showPostModal" @click.self="showPostModal = false">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ t('lobby.postRequest') }}</text>
          <text class="modal-close" @click="showPostModal = false">×</text>
        </view>
        <view class="modal-body">
          <textarea class="modal-input" v-model="newRequestDesc" :placeholder="t('lobby.searchPlaceholder')" />
          <input class="modal-field" type="text" v-model="newRequestTime" :placeholder="t('lobby.expectedTime')" />
          <input class="modal-field" type="text" v-model="newRequestAddress" :placeholder="t('lobby.expectedAddress')" />
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="showPostModal = false">{{ t('common.cancel') }}</button>
          <button class="modal-btn submit" @click="submitRequest">{{ t('common.confirm') }}</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted, computed} from 'vue';
import {request} from '../../utils/request';
import {useAppStore} from '../../store/app';
import {t} from '../../utils/i18n';

const appStore = useAppStore();
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight || 20);
const currentCity = ref('杭州市');
const activeTab = ref<'recommend' | 'requests'>('recommend');
const loadingCategories = ref(false);
const loadingCompanions = ref(false);
const loadingRequests = ref(false);
const fallbackCover = 'https://picsum.photos/seed/play-companion-cover/600/800';

// 轮播图列表
const bannerList = ref([
  { image: 'https://picsum.photos/seed/play-lobby-a/900/420' },
  { image: 'https://picsum.photos/seed/play-lobby-b/900/420' }
]);

// 分类与推荐陪玩
interface CategoryItem {
  id: number;
  name: string;
  icon: string;
}
const categoryList = ref<CategoryItem[]>([]);

interface CompanionItem {
  userId: number;
  nickname: string;
  coverUrl: string;
  rating: string;
  tags: string[];
  pricePerHour: number;
  distance: string;
  workStatus: number;
  voiceUrl?: string;
  voiceDuration?: number;
}
const companionList = ref<CompanionItem[]>([]);

// 魅力排行榜 (由推荐列表中前3名计算，若不够则使用默认数据)
const rankList = computed(() => {
  if (companionList.value.length > 0) {
    return companionList.value.slice(0, 3);
  }
  return [];
});

// 模拟需求大厅数据
interface RequestItem {
  id?: number;
  avatarUrl: string;
  nickname: string;
  timeAgo: string;
  description: string;
  reserveTime: string;
  address: string;
  price: number;
}
const requestList = ref<RequestItem[]>([
  {
    avatarUrl: 'https://picsum.photos/seed/request-wang/120/120',
    nickname: '王*亮',
    timeAgo: '5分钟前',
    description: '今晚需要一位台球搭子，最好会打美式台球，主要为了切磋技术，包间已开。',
    reserveTime: '今天 20:00 - 22:00',
    address: '下城区世纪台球汇',
    price: 120
  },
  {
    avatarUrl: 'https://picsum.photos/seed/request-chen/120/120',
    nickname: '陈*东',
    timeAgo: '15分钟前',
    description: '需要两位剧本杀（阿瓦隆）搭子，差人上车，都是熟人，氛围活跃不沉闷。',
    reserveTime: '明天 14:00 - 18:00',
    address: '拱墅区探案谋杀剧本杀馆',
    price: 90
  }
]);

// 需求发布弹窗
const showPostModal = ref(false);
const newRequestDesc = ref('');
const newRequestTime = ref('');
const newRequestAddress = ref('');

// 语音播放控制
const audioContext = ref<any>(null);
const playingVoiceUrl = ref<string>('');

const togglePlayVoice = (item: CompanionItem) => {
  if (!item.voiceUrl) return;

  if (playingVoiceUrl.value === item.voiceUrl) {
    audioContext.value?.stop();
    playingVoiceUrl.value = '';
    return;
  }

  if (audioContext.value) {
    audioContext.value.stop();
  } else {
    audioContext.value = uni.createInnerAudioContext();
  }

  playingVoiceUrl.value = item.voiceUrl;
  audioContext.value.src = item.voiceUrl;
  audioContext.value.play();

  audioContext.value.onEnded(() => {
    playingVoiceUrl.value = '';
  });
  audioContext.value.onError((res: any) => {
    console.error('音频播放错误', res);
    playingVoiceUrl.value = '';
  });
};

const loadCategories = async () => {
  loadingCategories.value = true;
  try {
    const res = await request({ url: '/categories', method: 'GET' });
    if (res.code === 200) {
      categoryList.value = res.data.map((cat: any) => ({
        id: cat.id,
        name: cat.name,
        icon: cat.iconUrl || ''
      }));
    }
  } catch (e) {
    console.error('加载分类失败', e);
  } finally {
    loadingCategories.value = false;
  }
};

const normalizeCompanion = (item: any): CompanionItem => ({
  userId: item.userId,
  nickname: item.nickname || `用户${item.userId}`,
  coverUrl: item.coverUrl || fallbackCover,
  rating: String(item.rating || '5.0'),
  tags: Array.isArray(item.tags) ? item.tags : [],
  pricePerHour: Number(item.pricePerHour || 0),
  distance: item.distance || '同城',
  workStatus: item.workStatus || 2,
  voiceUrl: item.voiceUrl || '',
  voiceDuration: item.voiceDuration || 0
});

const loadCompanions = async () => {
  loadingCompanions.value = true;
  try {
    const res = await request({ url: '/companions/recommended', method: 'GET' });
    if (res.code === 200) {
      companionList.value = res.data.map(normalizeCompanion);
    }
  } catch (e) {
    console.error('加载推荐失败', e);
  } finally {
    loadingCompanions.value = false;
  }
};

const normalizePlayRequest = (item: any): RequestItem => ({
  id: item.id,
  avatarUrl: item.avatarUrl || `https://picsum.photos/seed/request-${item.id || 'default'}/120/120`,
  nickname: item.nickname || '同城用户',
  timeAgo: item.createTime ? '刚刚' : '刚刚',
  description: item.description,
  reserveTime: item.reserveTime || '协商安排',
  address: item.address || '线上/协商地址',
  price: Number(item.budget || 0)
});

const loadRequests = async () => {
  loadingRequests.value = true;
  try {
    const res = await request({ url: '/requests?current=1&size=20', method: 'GET' });
    if (res.code === 200 && res.data?.records) {
      requestList.value = res.data.records.map(normalizePlayRequest);
    }
  } catch (e) {
    console.error('加载需求大厅失败', e);
  } finally {
    loadingRequests.value = false;
  }
};

// 提交发布需求
const submitRequest = async () => {
  if (!newRequestDesc.value.trim()) {
    uni.showToast({ title: t('lobby.requestRequired'), icon: 'none' });
    return;
  }

  const fallbackReq: RequestItem = {
    avatarUrl: 'https://picsum.photos/seed/request-me/120/120',
    nickname: '我',
    timeAgo: '刚刚',
    description: newRequestDesc.value,
    reserveTime: newRequestTime.value || '协商安排',
    address: newRequestAddress.value || '线上/协商地址',
    price: 100
  };

  try {
    const res = await request({
      url: '/requests',
      method: 'POST',
      data: {
        description: newRequestDesc.value,
        reserveTime: newRequestTime.value,
        address: newRequestAddress.value,
        budget: 100
      }
    });
    requestList.value.unshift(normalizePlayRequest(res.data));
  } catch {
    requestList.value.unshift(fallbackReq);
  } finally {
    showPostModal.value = false;
    newRequestDesc.value = '';
    newRequestTime.value = '';
    newRequestAddress.value = '';
  }

  uni.showModal({
    title: t('common.success'),
    content: t('lobby.requestSuccess'),
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    success: (res) => {
      if (res.confirm) {
        // 客服对接
        uni.showToast({ title: t('lobby.contactJump'), icon: 'none' });
      }
    }
  });
};

// 应约联系客服
const applyRequest = (req: RequestItem) => {
  uni.showModal({
    title: t('lobby.contactForRequest'),
    content: `${req.nickname}：${t('lobby.contactForRequestContent')}`,
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: t('lobby.contactJump'), icon: 'none' });
      }
    }
  });
};

onMounted(() => {
  loadCategories();
  loadCompanions();
  loadRequests();
});

onUnmounted(() => {
  if (audioContext.value) {
    audioContext.value.destroy();
  }
});

const goToCategory = (id: number) => {
  uni.navigateTo({ url: `/pages/companion/list?categoryId=${id}` });
};

const goToDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/companion/detail?id=${id}` });
};

const goToCompanion = () => {
  uni.navigateTo({ url: '/pages/companion/apply' });
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-primary);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.status-bar {
  width: 100%;
}

.header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);

  .location {
    display: flex;
    align-items: center;
    margin-right: 24rpx;
    font-size: var(--font-size-base);
    font-weight: bold;

    .loc-icon {
      width: 18rpx;
      height: 18rpx;
      border: 4rpx solid var(--color-primary);
      border-radius: 50% 50% 50% 0;
      transform: rotate(-45deg);
      margin-right: 10rpx;
      box-sizing: border-box;
    }
    
    .city {
      margin-right: 6rpx;
    }
  }

  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    background-color: var(--bg-main);
    border-radius: var(--radius-full);
    padding: 12rpx 24rpx;
    border: 1px solid var(--border-color);
    
    .search-icon {
      width: 24rpx;
      height: 24rpx;
      border: 3rpx solid var(--text-secondary);
      border-radius: 50%;
      margin-right: 14rpx;
      position: relative;
      box-sizing: border-box;

      &::after {
        content: '';
        position: absolute;
        width: 10rpx;
        height: 3rpx;
        right: -8rpx;
        bottom: -4rpx;
        background: var(--text-secondary);
        border-radius: var(--radius-full);
        transform: rotate(45deg);
      }
    }
    
    input {
      flex: 1;
      font-size: var(--font-size-sm);
      color: var(--text-primary);
    }
    
    .placeholder {
      color: var(--text-muted);
    }
  }
}

.main-content {
  flex: 1;
  overflow: hidden;
}

.banner-swiper {
  height: 280rpx;
  padding: 20rpx 30rpx;

  .swiper-item {
    border-radius: var(--radius-lg);
    overflow: hidden;
  }

  .banner-img {
    width: 100%;
    height: 100%;
  }
}

.companion-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 30rpx 20rpx;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, #FFF8F0, #FFEAE0);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(var(--color-primary-rgb), 0.1);
  box-shadow: var(--shadow-card);

  /* 暗色模式特殊微调 */
  .theme-dark & {
    background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.15), rgba(var(--color-primary-rgb), 0.06));
    border: 1px solid rgba(var(--color-primary-rgb), 0.12);
  }

  .banner-title {
    font-size: var(--font-size-base);
    font-weight: bold;
    color: var(--color-primary);
    display: block;
  }

  .banner-sub {
    font-size: var(--font-size-xs);
    color: var(--text-secondary);
    margin-top: 6rpx;
    display: block;
  }
  
  .banner-arrow {
    font-size: 40rpx;
    color: var(--color-primary);
    font-weight: bold;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx 10rpx;
  padding: 10rpx 10rpx 30rpx;
  background-color: var(--bg-card);
  margin: 0 30rpx 30rpx;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 24rpx;
    
    .icon-wrap {
      width: 90rpx;
      height: 90rpx;
      background-color: var(--bg-main);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      border: 1px solid var(--border-color);
      
      .category-icon {
        width: 50rpx;
        height: 50rpx;
      }

      .category-fallback {
        color: var(--color-primary);
        font-size: 30rpx;
        font-weight: 700;
      }
    }
    
    .category-name {
      font-size: var(--font-size-sm);
      color: var(--text-primary);
    }
  }
}

/* 魅力排行榜 (Crown Rankings) */
.ranking-section {
  margin: 0 30rpx 30rpx;
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24rpx 20rpx;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);

  .section-title {
    margin-bottom: 20rpx;
    .title-text {
      font-size: var(--font-size-base);
      font-weight: bold;
      color: var(--text-primary);
    }
  }

  .ranking-cards {
    display: flex;
    justify-content: space-between;
    gap: 16rpx;
  }

  .rank-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: var(--bg-main);
    border-radius: var(--radius-md);
    padding: 16rpx 10rpx;
    position: relative;
    border: 1px solid var(--border-color);

    .rank-crown {
      position: absolute;
      top: -12rpx;
      z-index: 10;
      
      .crown-mark {
        display: block;
        padding: 2rpx 10rpx;
        border-radius: var(--radius-full);
        background: var(--color-accent);
        color: #fff;
        font-size: 16rpx;
        font-weight: 700;
      }
      .crown-badge {
        width: 32rpx;
        height: 32rpx;
        border-radius: 50%;
        background-color: var(--text-secondary);
        color: #fff;
        font-size: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    /* 皇冠金高亮 */
    &.rank-1 {
      border: 1px solid var(--color-accent);
      background-color: rgba(245, 158, 11, 0.04);
      .rank-avatar {
        border: 4rpx solid var(--color-accent);
      }
    }

    .rank-avatar {
      width: 90rpx;
      height: 90rpx;
      border-radius: 50%;
      margin-bottom: 12rpx;
      border: 2rpx solid var(--border-color);
    }

    .rank-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 100%;

      .rank-name {
        font-size: 22rpx;
        font-weight: bold;
        color: var(--text-primary);
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        width: 100%;
      }

      .rank-tag {
        font-size: 16rpx;
        color: var(--color-accent);
        background-color: rgba(245, 158, 11, 0.1);
        padding: 2rpx 8rpx;
        border-radius: 4rpx;
        margin: 6rpx 0;
      }

      .rank-price {
        font-size: 18rpx;
        color: var(--color-primary);
        font-weight: bold;
      }
    }
  }
}

/* 标签切换 */
.tabs-header {
  display: flex;
  padding: 10rpx 30rpx;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20rpx;

  .tab-item {
    font-size: var(--font-size-base);
    color: var(--text-secondary);
    margin-right: 48rpx;
    padding-bottom: 16rpx;
    position: relative;

    &.active {
      color: var(--color-primary);
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 32rpx;
        height: 6rpx;
        background-color: var(--color-primary);
        border-radius: var(--radius-full);
      }
    }
  }
}

/* 陪玩卡片列表 */
.companion-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24rpx;
  padding: 0 30rpx;
  
  .companion-card {
    background-color: var(--bg-card);
    border-radius: var(--radius-lg);
    overflow: hidden;
    margin-bottom: 24rpx;
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-card);
    
    .card-cover-wrap {
      position: relative;
      width: 100%;
      height: 320rpx;
      
      .card-cover {
        width: 100%;
        height: 100%;
      }
      
      .status-badge {
        position: absolute;
        top: 16rpx;
        left: 16rpx;
        background: rgba(26, 22, 19, 0.62);
        border-radius: var(--radius-full);
        padding: 4rpx 16rpx;
        display: flex;
        align-items: center;
        font-size: 18rpx;
        color: #fff;
        
        .dot {
          width: 8rpx;
          height: 8rpx;
          border-radius: 50%;
          background-color: var(--color-success);
          margin-right: 8rpx;
        }
        
        &.busy {
          .dot {
            background-color: var(--color-accent);
          }
        }
      }
    }
    
    .card-info {
      padding: 16rpx;
      
      .info-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10rpx;
        
        .name {
          font-size: var(--font-size-sm);
          font-weight: bold;
          color: var(--text-primary);
        }
        
        .rating {
          display: flex;
          align-items: center;
          color: var(--color-accent);
          font-size: 22rpx;
          font-weight: bold;
          
          .star {
            margin-right: 4rpx;
          }
        }
      }

      /* 微型语音气泡 */
      .voice-capsule {
        display: inline-flex;
        align-items: center;
        background-color: var(--color-primary-light);
        border-radius: var(--radius-full);
        padding: 4rpx 12rpx;
        margin-bottom: 12rpx;
        border: 1px solid rgba(var(--color-primary-rgb), 0.1);

        .theme-dark & {
          background-color: rgba(var(--color-primary-rgb), 0.15);
        }

        .voice-icon {
          width: 18rpx;
          height: 22rpx;
          margin-right: 4rpx;
          position: relative;

          &::before {
            content: '';
            position: absolute;
            left: 4rpx;
            top: 2rpx;
            width: 10rpx;
            height: 14rpx;
            border: 3rpx solid var(--color-primary);
            border-radius: 8rpx;
            box-sizing: border-box;
          }

          &::after {
            content: '';
            position: absolute;
            left: 8rpx;
            bottom: 0;
            width: 3rpx;
            height: 8rpx;
            background: var(--color-primary);
            border-radius: var(--radius-full);
          }

          &.pause::before,
          &.pause::after {
            top: 2rpx;
            bottom: auto;
            width: 5rpx;
            height: 18rpx;
            border: none;
            background: var(--color-primary);
            border-radius: 2rpx;
          }

          &.pause::before { left: 3rpx; }
          &.pause::after { left: 11rpx; }
        }

        .voice-dur {
          font-size: 18rpx;
          color: var(--color-primary);
          font-weight: bold;
        }
      }
      
      .tags {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 12rpx;
        gap: 8rpx;
        height: 36rpx;
        overflow: hidden;
        
        .tag {
          font-size: 18rpx;
          color: var(--color-primary);
          background-color: var(--color-primary-light);
          padding: 2rpx 10rpx;
          border-radius: var(--radius-sm);

          .theme-dark & {
            background-color: rgba(var(--color-primary-rgb), 0.15);
          }
        }
      }
      
      .info-bottom {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        
        .price {
          color: var(--color-primary);
          font-weight: bold;
          font-size: 28rpx;
        }
        
        .distance {
          font-size: 18rpx;
          color: var(--text-secondary);
        }
      }
    }
  }
}

/* 需求大厅列表 */
.request-list {
  padding: 0 30rpx;

  .request-card {
    background-color: var(--bg-card);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-card);
    padding: 24rpx;
    margin-bottom: 24rpx;

    .req-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .req-user {
        display: flex;
        align-items: center;

        .req-avatar {
          width: 60rpx;
          height: 60rpx;
          border-radius: 50%;
          margin-right: 12rpx;
        }

        .req-name {
          font-size: var(--font-size-sm);
          font-weight: bold;
          color: var(--text-primary);
        }
      }

      .req-time {
        font-size: 20rpx;
        color: var(--text-secondary);
      }
    }

    .req-body {
      margin-bottom: 20rpx;

      .req-desc {
        font-size: var(--font-size-sm);
        color: var(--text-primary);
        line-height: 1.5;
        display: block;
        margin-bottom: 12rpx;
      }

      .req-details {
        display: flex;
        flex-direction: column;
        gap: 6rpx;

        .req-detail-item {
          font-size: 22rpx;
          color: var(--text-secondary);

          .detail-label {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 56rpx;
            margin-right: 10rpx;
            padding: 2rpx 8rpx;
            color: var(--color-primary);
            background: rgba(var(--color-primary-rgb), 0.08);
            border-radius: var(--radius-sm);
          }
        }
      }
    }

    .req-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid var(--border-color);
      padding-top: 16rpx;

      .req-price {
        font-size: var(--font-size-base);
        color: var(--color-primary);
        font-weight: bold;
      }

      .req-btn {
        margin: 0;
        font-size: 22rpx;
        background-color: var(--color-primary);
        color: #fff;
        border-radius: var(--radius-full);
        padding: 10rpx 32rpx;
        line-height: 1.5;
        border: none;
        box-shadow: var(--shadow-floating);
      }
    }
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx 0 50rpx;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.empty-state {
  margin: 20rpx 30rpx 40rpx;
  padding: 54rpx 24rpx;
  text-align: center;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-sm);
}

.skeleton-block,
.skeleton-line {
  background: linear-gradient(90deg, rgba(var(--color-primary-rgb), 0.05), rgba(255, 255, 255, 0.08), rgba(var(--color-primary-rgb), 0.05));
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}

.skeleton-line {
  width: 100%;
  height: 22rpx;
  border-radius: var(--radius-full);

  &.wide { width: 80%; margin-bottom: 14rpx; }
  &.short { width: 48%; margin-bottom: 14rpx; }
}

.skeleton-category {
  pointer-events: none;

  .skeleton-line {
    width: 64rpx;
  }
}

.skeleton-card {
  pointer-events: none;
}

/* 需求发布悬浮按钮 */
.fab-btn {
  position: fixed;
  bottom: 120rpx;
  right: 30rpx;
  background-color: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  padding: 16rpx 28rpx;
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-floating);
  z-index: 99;

  .fab-icon {
    width: 26rpx;
    height: 26rpx;
    margin-right: 10rpx;
    position: relative;

    &::before,
    &::after {
      content: '';
      position: absolute;
      left: 50%;
      top: 50%;
      width: 24rpx;
      height: 4rpx;
      background: #fff;
      border-radius: var(--radius-full);
      transform: translate(-50%, -50%);
    }

    &::after {
      transform: translate(-50%, -50%) rotate(90deg);
    }
  }

  .fab-text {
    font-size: 24rpx;
    font-weight: bold;
  }
}

/* 发布需求弹窗 */
.post-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);

  .modal-content {
    width: 80%;
    background-color: var(--bg-card);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
    padding: 30rpx;
    box-shadow: var(--shadow-card);

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;

      .modal-title {
        font-size: var(--font-size-lg);
        font-weight: bold;
        color: var(--text-primary);
      }

      .modal-close {
        font-size: 48rpx;
        color: var(--text-secondary);
        padding: 0 10rpx;
      }
    }

    .modal-body {
      display: flex;
      flex-direction: column;
      gap: 16rpx;
      margin-bottom: 30rpx;

      .modal-input {
        width: 100%;
        height: 150rpx;
        background-color: var(--bg-main);
        border-radius: var(--radius-md);
        border: 1px solid var(--border-color);
        padding: 16rpx;
        font-size: var(--font-size-sm);
        color: var(--text-primary);
        box-sizing: border-box;
      }

      .modal-field {
        width: 100%;
        background-color: var(--bg-main);
        border-radius: var(--radius-md);
        border: 1px solid var(--border-color);
        padding: 16rpx;
        font-size: var(--font-size-sm);
        color: var(--text-primary);
        box-sizing: border-box;
      }
    }

    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 16rpx;

      .modal-btn {
        margin: 0;
        font-size: var(--font-size-sm);
        border-radius: var(--radius-full);
        padding: 12rpx 36rpx;
        line-height: 1.5;
        border: none;

        &.cancel {
          background-color: var(--bg-main);
          color: var(--text-secondary);
          border: 1px solid var(--border-color);
        }

        &.submit {
          background-color: var(--color-primary);
          color: #fff;
          box-shadow: var(--shadow-floating);
        }
      }
    }
  }
}

@keyframes skeleton-shimmer {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
