<template>
  <view class="container" v-if="companion">
    <!-- 顶部返回键 -->
    <view class="back-btn" :style="{ top: (statusBarHeight + 10) + 'px' }" @click="goBack" aria-label="返回">
      <text class="back-icon">‹</text>
    </view>

    <!-- 顶部相册画廊 (45vh 占位) -->
    <view class="album-wrap">
      <swiper class="album-swiper" circular autoplay interval="4000" @change="onSwiperChange">
        <swiper-item v-for="(img, index) in companion.photoUrls" :key="index">
          <image :src="img" mode="aspectFill" class="album-img" lazy-load @click="previewImage(index)"></image>
        </swiper-item>
      </swiper>
      <view class="indicator">
        {{ currentImageIndex + 1 }} / {{ companion.photoUrls?.length || 1 }}
      </view>
    </view>

    <!-- 核心信息区 -->
    <view class="info-card">
      <view class="header-row">
        <view class="name-box">
          <text class="name">{{ companion.nickname }}</text>
          <view class="status-badge" :class="{ 'busy': companion.workStatus !== 1 }">
            <text class="dot"></text>
            <text>{{ companion.workStatus === 1 ? t('lobby.available') : t('lobby.busy') }}</text>
          </view>
        </view>
        <view class="price-box">
          <text class="price">{{ t('lobby.priceUnit', { price: companion.pricePerHour }) }}</text>
        </view>
      </view>

      <view class="basic-tags">
        <view class="tag gender" :class="companion.gender === 1 ? 'male' : 'female'">
          <!-- SVG 性别图标替代 emoji ♂/♀ -->
          <svg v-if="companion.gender === 1" viewBox="0 0 24 24" class="gender-svg">
            <circle cx="10" cy="8" r="5" fill="none" stroke="currentColor" stroke-width="2"/>
            <line x1="10" y1="13" x2="10" y2="22" stroke="currentColor" stroke-width="2"/>
            <line x1="7" y1="17" x2="13" y2="17" stroke="currentColor" stroke-width="2"/>
            <line x1="14" y1="2" x2="22" y2="10" stroke="currentColor" stroke-width="2"/>
            <polyline points="18,0 22,0 22,4" fill="none" stroke="currentColor" stroke-width="2"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" class="gender-svg">
            <circle cx="12" cy="8" r="5" fill="none" stroke="currentColor" stroke-width="2"/>
            <line x1="12" y1="13" x2="12" y2="18" stroke="currentColor" stroke-width="2"/>
            <line x1="8" y1="22" x2="16" y2="22" stroke="currentColor" stroke-width="2"/>
            <line x1="8" y1="15" x2="16" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
          <text>{{ t('detail.ageUnit', { age: companion.age }) }}</text>
        </view>
        <view class="tag height" v-if="companion.height">{{ companion.height }}cm</view>
      </view>

      <view class="data-row">
        <view class="data-item">
          <text class="value">{{ companion.rating }}</text>
          <text class="label">{{ t('detail.ratingLabel') }}</text>
        </view>
        <view class="data-item">
          <text class="value">{{ companion.orderCount }}</text>
          <text class="label">{{ t('detail.orderLabel') }}</text>
        </view>
        <view class="data-item">
          <text class="value">{{ companion.goodRate }}</text>
          <text class="label">{{ t('detail.goodRateLabel') }}</text>
        </view>
      </view>
    </view>

    <!-- 语音展示区 -->
    <view class="section" v-if="companion.voiceUrl">
      <view class="section-title">{{ t('detail.voiceIntro') }}</view>
      <view class="voice-card" @click="playVoice" :aria-label="isPlaying ? '暂停播放' : '播放语音介绍'">
        <view class="avatar-wrap">
          <image :src="companion.photoUrls?.[0]" mode="aspectFill" class="avatar"></image>
          <view class="play-btn" :class="{ 'playing': isPlaying }">
            <view class="play-symbol" :class="{ pause: isPlaying }"></view>
          </view>
        </view>
        <view class="voice-info">
          <view class="waveform">
            <view class="wave" :class="{ 'active': isPlaying }" v-for="i in 16" :key="i"></view>
          </view>
          <text class="duration">{{ companion.voiceDuration || 10 }}s</text>
        </view>
      </view>
    </view>

    <!-- 个人简介 -->
    <view class="section">
      <view class="section-title">{{ t('detail.bio') }}</view>
      <view class="summary-content">
        {{ companion.summary || t('detail.emptySummary') }}
      </view>
      <view class="skill-tags">
        <text class="skill-tag" v-for="(tag, idx) in companion.tags" :key="idx">{{ tag }}</text>
      </view>
    </view>

    <!-- 真实评价 -->
    <view class="section">
      <view class="section-title">{{ t('detail.reviews', { count: companion.totalRatingCount || 0 }) }}</view>
      <view class="reviews-list" v-if="reviewList.length > 0">
        <view class="review-item" v-for="(review, index) in reviewList" :key="index">
          <view class="rev-user">
            <image :src="review.avatarUrl" mode="aspectFill" class="rev-avatar" lazy-load></image>
            <view class="rev-user-info">
              <text class="rev-name">{{ review.nickname }}</text>
              <view class="rev-rating">
                <svg viewBox="0 0 24 24" class="rev-star-svg">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" fill="var(--color-accent)" stroke="var(--color-accent)" stroke-width="1"/>
                </svg>
                {{ review.rating }}
              </view>
            </view>
            <text class="rev-date">{{ review.date }}</text>
          </view>
          <text class="rev-content">{{ review.content }}</text>
        </view>
      </view>
      <view v-else class="empty-reviews">
        <text>{{ t('detail.noReviews') }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <!-- 官方客服一键跳转 -->
      <button class="cs-btn" open-type="contact">
        <view class="cs-icon"></view>
        <text class="cs-text">{{ t('common.contactCS') }}</text>
      </button>
      <button class="order-btn" @click="goToOrder" :disabled="companion.workStatus !== 1">
        {{ companion.workStatus === 1 ? t('detail.bookNow') : t('lobby.busy') }}
      </button>
    </view>
  </view>
  <view v-else class="loading-full">
    <view class="detail-skeleton">
      <view class="skeleton-hero"></view>
      <view class="skeleton-panel">
        <view class="skeleton-line wide"></view>
        <view class="skeleton-line short"></view>
        <view class="skeleton-grid">
          <view class="skeleton-line" v-for="i in 3" :key="i"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {request} from '../../utils/request';
import {useAppStore} from '../../store/app';
import {t} from '../../utils/i18n';

const appStore = useAppStore();
const companion = ref<any>(null);
const currentImageIndex = ref(0);
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight || 20);
const fallbackPhoto = 'https://picsum.photos/seed/play-detail-cover/800/980';

// Audio
const innerAudioContext = uni.createInnerAudioContext();
const isPlaying = ref(false);

// 评价列表
interface ReviewItem {
  avatarUrl: string;
  nickname: string;
  rating: number;
  content: string;
  date: string;
}
const reviewList = ref<ReviewItem[]>([]);

const fetchReviews = async (companionId: number) => {
  try {
    const res = await request({ url: `/companions/${companionId}/reviews?current=1&size=20`, method: 'GET' });
    if (res.code === 200 && res.data?.records) {
      reviewList.value = res.data.records.map((r: any) => ({
        avatarUrl: r.isAnonymous ? '' : (`https://picsum.photos/seed/user${r.userId}/120/120`),
        nickname: r.isAnonymous ? '匿名用户' : `用户${r.userId}`,
        rating: r.rating,
        content: r.content || '',
        date: (r.createTime || '').substring(0, 10)
      }));
    }
  } catch (_) {}
};

const normalizeDetail = (data: any) => ({
  ...data,
  userId: data.userId,
  nickname: data.nickname || `用户${data.userId}`,
  pricePerHour: Number(data.pricePerHour || 0),
  rating: data.rating || '5.0',
  orderCount: data.orderCount || 0,
  totalRatingCount: data.totalRatingCount || 0,
  goodRate: `${Math.min(97, Math.max(82, Math.round(Number(data.rating || 5) * 19.2)))}%`,
  workStatus: data.workStatus || 2,
  tags: Array.isArray(data.tags) ? data.tags : [],
  photoUrls: Array.isArray(data.photoUrls) && data.photoUrls.length > 0 ? data.photoUrls : [fallbackPhoto],
  voiceUrl: data.voiceUrl || '',
  voiceDuration: data.voiceDuration || 0
});

onLoad((options: any) => {
  const id = options?.id;
  if (id) fetchDetail(id);

  innerAudioContext.onPlay(() => { isPlaying.value = true; });
  innerAudioContext.onPause(() => { isPlaying.value = false; });
  innerAudioContext.onEnded(() => { isPlaying.value = false; });
  innerAudioContext.onError((e) => {
    console.error('音频播放错误', e);
    isPlaying.value = false;
  });
});

onUnmounted(() => {
  innerAudioContext.destroy();
});

const fetchDetail = async (id: string) => {
  try {
    const res = await request({
      url: `/companions/${id}`,
      method: 'GET'
    });
    if (res.code === 200) {
      companion.value = normalizeDetail(res.data);
      fetchReviews(Number(id));
      if (companion.value.voiceUrl) {
        innerAudioContext.src = companion.value.voiceUrl;
      }
    }
  } catch (e) {
    console.error('获取陪玩详情失败', e);
    // Mock 本地假数据以便演示
    companion.value = normalizeDetail({
      userId: Number(id),
      nickname: '蜜桃小酥',
      pricePerHour: 100,
      gender: 2,
      age: 22,
      height: 165,
      rating: '5.0',
      orderCount: 128,
      voiceUrl: '',
      voiceDuration: 0,
      summary: '哈喽！我是蜜桃小酥。平时特别喜欢打台球、狼人杀和桌游。性格幽默好相处，能够快速带你融入轻松开怀的娱乐氛围哦！期待和你一起组搭子～',
      tags: ['台球高手', '声音甜', '狼人杀高玩', '准时到达'],
      photoUrls: [
        'https://picsum.photos/seed/detail-a/800/980',
        'https://picsum.photos/seed/detail-b/800/980'
      ]
    });
    if (companion.value.voiceUrl) {
      innerAudioContext.src = companion.value.voiceUrl;
    }
  }
};

const onSwiperChange = (e: any) => {
  currentImageIndex.value = e.detail.current;
};

const previewImage = (current: number) => {
  uni.previewImage({
    current,
    urls: companion.value.photoUrls
  });
};

const playVoice = () => {
  if (isPlaying.value) {
    innerAudioContext.pause();
  } else {
    innerAudioContext.play();
  }
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const goToOrder = () => {
  uni.navigateTo({
    url: `/pages/order/create?companionId=${companion.value.userId}`
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-primary);
  padding-bottom: 140rpx;
  position: relative;
  transition: background-color 0.2s ease, color 0.2s ease;
}

/* 浮空返回键 */
.back-btn {
  position: fixed;
  left: 30rpx;
  width: 70rpx;
  height: 70rpx;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;

  .back-icon {
    color: #fff;
    font-size: 48rpx;
    font-weight: bold;
    transform: translateY(-2rpx);
  }
}

.album-wrap {
  position: relative;
  width: 100%;
  height: 45vh;
  
  .album-swiper {
    width: 100%;
    height: 100%;
  }
  
  .album-img {
    width: 100%;
    height: 100%;
  }
  
  .indicator {
    position: absolute;
    right: 30rpx;
    bottom: 30rpx;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    padding: 6rpx 20rpx;
    border-radius: var(--radius-full);
    font-size: 24rpx;
    backdrop-filter: blur(4px);
  }
}

.info-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  margin-top: -30rpx;
  position: relative;
  z-index: 10;
  padding: 40rpx 30rpx;
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  
  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .name-box {
      display: flex;
      align-items: center;
      
      .name {
        font-size: 36rpx;
        font-weight: bold;
        color: var(--text-primary);
        margin-right: 20rpx;
      }
      
      .status-badge {
        display: flex;
        align-items: center;
        background: rgba(16, 185, 129, 0.1);
        color: var(--color-success);
        padding: 4rpx 16rpx;
        border-radius: var(--radius-full);
        font-size: 20rpx;
        
        .dot {
          width: 8rpx; height: 8rpx;
          border-radius: 50%;
          background-color: var(--color-success);
          margin-right: 8rpx;
        }
        
        &.busy {
          background: rgba(var(--color-primary-rgb), 0.1);
          color: var(--color-primary);
          .dot { background-color: var(--color-primary); }
        }
      }
    }

    .price-box {
      color: var(--color-primary);
      font-weight: bold;
      font-size: 36rpx;
    }
  }
  
  .basic-tags {
    display: flex;
    margin-bottom: 40rpx;
    
    .tag {
      padding: 6rpx 16rpx;
      border-radius: var(--radius-sm);
      font-size: 20rpx;
      margin-right: 16rpx;
      display: flex;
      align-items: center;
      
      &.male { background-color: rgba(99, 102, 241, 0.1); color: #6366F1; }
      &.female { background-color: var(--color-primary-light); color: var(--color-primary); }
      &.height { background-color: var(--bg-main); color: var(--text-primary); border: 1px solid var(--border-color); }

      .gender-svg {
        width: 22rpx;
        height: 22rpx;
        margin-right: 6rpx;
      }
    }
  }
  
  .data-row {
    display: flex;
    justify-content: space-around;
    padding-top: 30rpx;
    border-top: 1rpx solid var(--border-color);
    
    .data-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .value { font-size: 36rpx; font-weight: bold; color: var(--text-primary); margin-bottom: 8rpx; }
      .label { font-size: 24rpx; color: var(--text-secondary); }
    }
  }
}

.section {
  background-color: var(--bg-card);
  margin-top: 20rpx;
  padding: 30rpx;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: var(--text-primary);
    margin-bottom: 24rpx;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      width: 8rpx;
      height: 28rpx;
      background-color: var(--color-primary);
      border-radius: var(--radius-full);
      margin-right: 16rpx;
    }
  }
}

.voice-card {
  background-color: var(--color-primary-light);
  border-radius: var(--radius-lg);
  padding: 24rpx;
  display: flex;
  align-items: center;
  border: 1rpx solid rgba(var(--color-primary-rgb), 0.1);

  .theme-dark & {
    background-color: rgba(var(--color-primary-rgb), 0.08);
  }
  
  .avatar-wrap {
    position: relative;
    width: 90rpx;
    height: 90rpx;
    
    .avatar {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }
    
    .play-btn {
      position: absolute;
      right: -6rpx;
      bottom: -6rpx;
      width: 40rpx;
      height: 40rpx;
      background-color: var(--color-primary);
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 4rpx solid var(--bg-card);
      
      .play-symbol {
        width: 0;
        height: 0;
        border-top: 9rpx solid transparent;
        border-bottom: 9rpx solid transparent;
        border-left: 14rpx solid var(--bg-card);
        transform: translateX(2rpx);
        transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);

        &.pause {
          width: 16rpx;
          height: 18rpx;
          border: none;
          transform: translateX(0);
          position: relative;

          &::before,
          &::after {
            content: '';
            position: absolute;
            top: 0;
            width: 5rpx;
            height: 18rpx;
            background: #fff;
            border-radius: 2rpx;
          }

          &::before { left: 1rpx; }
          &::after { right: 1rpx; }
        }
      }
      
      &.playing {
        .play-symbol {
          transform: translateX(0);
        }
      }
    }
  }
  
  .voice-info {
    flex: 1;
    margin-left: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .waveform {
      display: flex;
      align-items: center;
      height: 40rpx;
      gap: 6rpx;
      
      .wave {
        width: 6rpx;
        height: 28rpx;
        background-color: var(--color-primary);
        border-radius: 3rpx;
        opacity: 0.5;
        transform: scaleY(0.35);
        transform-origin: center;
        will-change: transform, opacity;
        
        &.active {
          opacity: 1;
          &:nth-child(1) { animation: wave 1s infinite 0.1s; }
          &:nth-child(2) { animation: wave 1s infinite 0.3s; }
          &:nth-child(3) { animation: wave 1s infinite 0.5s; }
          &:nth-child(4) { animation: wave 1s infinite 0.2s; }
          &:nth-child(5) { animation: wave 1s infinite 0.4s; }
          &:nth-child(6) { animation: wave 1s infinite 0.6s; }
          &:nth-child(7) { animation: wave 1s infinite 0.3s; }
          &:nth-child(8) { animation: wave 1s infinite 0.1s; }
          &:nth-child(9) { animation: wave 1s infinite 0.2s; }
          &:nth-child(10) { animation: wave 1s infinite 0.4s; }
          &:nth-child(11) { animation: wave 1s infinite 0.5s; }
          &:nth-child(12) { animation: wave 1s infinite 0.1s; }
          &:nth-child(13) { animation: wave 1s infinite 0.3s; }
          &:nth-child(14) { animation: wave 1s infinite 0.6s; }
          &:nth-child(15) { animation: wave 1s infinite 0.2s; }
          &:nth-child(16) { animation: wave 1s infinite 0.4s; }
        }
      }
    }
    
    .duration {
      color: var(--color-primary);
      font-weight: bold;
      font-size: 26rpx;
    }
  }
}

.summary-content {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 24rpx;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .skill-tag {
    padding: 10rpx 24rpx;
    background-color: var(--color-primary-light);
    color: var(--color-primary);
    border-radius: var(--radius-full);
    font-size: 24rpx;

    .theme-dark & {
      background-color: rgba(var(--color-primary-rgb), 0.15);
    }
  }
}

/* 评价列表 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .review-item {
    padding-bottom: 20rpx;
    border-bottom: 1px solid var(--border-color);

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    .rev-user {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;

      .rev-avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        margin-right: 12rpx;
      }

      .rev-user-info {
        flex: 1;

        .rev-name {
          font-size: var(--font-size-sm);
          font-weight: bold;
          color: var(--text-primary);
          display: block;
        }

        .rev-rating {
          font-size: 18rpx;
          color: var(--color-accent);
          font-weight: bold;
          display: flex;
          align-items: center;

          .rev-star-svg {
            width: 20rpx;
            height: 20rpx;
            margin-right: 4rpx;
          }
        }
      }

      .rev-date {
        font-size: 20rpx;
        color: var(--text-secondary);
      }
    }

    .rev-content {
      font-size: var(--font-size-sm);
      color: var(--text-primary);
      line-height: 1.5;
      display: block;
    }
  }
}

.empty-reviews {
  text-align: center;
  padding: 40rpx 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: var(--bg-card);
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: var(--shadow-card);
  z-index: $z-index-sticky;

  .cs-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    line-height: 1.2;
    padding: 0;
    margin: 0 40rpx 0 0;
    color: var(--text-primary);

    &::after { border: none; }

    .cs-icon {
      width: 36rpx;
      height: 30rpx;
      margin-bottom: 4rpx;
      border: 3rpx solid var(--text-primary);
      border-radius: 14rpx;
      position: relative;
      box-sizing: border-box;

      &::after {
        content: '';
        position: absolute;
        left: 7rpx;
        bottom: -8rpx;
        width: 10rpx;
        height: 10rpx;
        border-left: 3rpx solid var(--text-primary);
        border-bottom: 3rpx solid var(--text-primary);
        transform: rotate(-20deg);
      }
    }
    .cs-text {
      font-size: 20rpx;
      font-weight: 500;
    }
  }
  
  .order-btn {
    flex: 1;
    height: 84rpx;
    background-color: var(--color-primary);
    color: #fff;
    border-radius: var(--radius-full);
    font-size: var(--font-size-base);
    font-weight: bold;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
    box-shadow: var(--shadow-floating);
    
    &::after { display: none; }
    
    &[disabled] {
      background: var(--text-muted);
      color: var(--text-muted);
      box-shadow: none;
    }
  }
}

.loading-full {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-secondary);
}

.detail-skeleton {
  min-height: 100vh;
}

.skeleton-hero {
  height: 45vh;
  background: linear-gradient(90deg, rgba(var(--color-primary-rgb), 0.05), rgba(255, 255, 255, 0.08), rgba(var(--color-primary-rgb), 0.05));
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}

.skeleton-panel {
  margin: -30rpx 30rpx 0;
  padding: 40rpx 30rpx;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  position: relative;
}

.skeleton-line {
  height: 24rpx;
  border-radius: var(--radius-full);
  background: linear-gradient(90deg, rgba(var(--color-primary-rgb), 0.05), rgba(255, 255, 255, 0.08), rgba(var(--color-primary-rgb), 0.05));
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  margin-bottom: 18rpx;

  &.wide { width: 72%; }
  &.short { width: 42%; }
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
  margin-top: 24rpx;
}

@keyframes wave {
  0% { transform: scaleY(0.35); }
  50% { transform: scaleY(1); }
  100% { transform: scaleY(0.35); }
}

@keyframes skeleton-shimmer {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
