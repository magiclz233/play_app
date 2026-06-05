<template>
  <view class="container" v-if="companion">
    <!-- 顶部透明导航，随滚动渐变 (可通过样式实现或原生配置) -->
    
    <!-- 顶部相册画廊 -->
    <view class="album-wrap">
      <swiper class="album-swiper" circular autoplay interval="4000" @change="onSwiperChange">
        <swiper-item v-for="(img, index) in companion.photoUrls" :key="index">
          <image :src="img" mode="aspectFill" class="album-img" @click="previewImage(index)"></image>
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
          <view class="status-badge" :class="{ 'busy': companion.workStatus === 2 }">
            <text class="dot"></text>
            <text>{{ companion.workStatus === 1 ? '可接单' : (companion.workStatus === 2 ? '忙碌中' : '休息中') }}</text>
          </view>
        </view>
        <view class="price-box">
          <text class="currency">¥</text>
          <text class="price">{{ companion.pricePerHour }}</text>
          <text class="unit">/时</text>
        </view>
      </view>

      <view class="basic-tags">
        <view class="tag gender" :class="companion.gender === 1 ? 'male' : 'female'">
          <text class="iconfont" :class="companion.gender === 1 ? 'icon-man' : 'icon-woman'"></text>
          <text>{{ companion.age }}岁</text>
        </view>
        <view class="tag height" v-if="companion.height">{{ companion.height }}cm</view>
      </view>

      <view class="data-row">
        <view class="data-item">
          <text class="value">{{ companion.rating }}</text>
          <text class="label">综合评分</text>
        </view>
        <view class="data-item">
          <text class="value">{{ companion.orderCount }}</text>
          <text class="label">接单次数</text>
        </view>
        <view class="data-item">
          <text class="value">98%</text>
          <text class="label">好评率</text>
        </view>
      </view>
    </view>

    <!-- 语音展示区 -->
    <view class="section" v-if="companion.voiceUrl">
      <view class="section-title">真人语音</view>
      <view class="voice-card" @click="playVoice">
        <view class="avatar-wrap">
          <image :src="companion.photoUrls?.[0]" mode="aspectFill" class="avatar"></image>
          <view class="play-btn" :class="{ 'playing': isPlaying }">
            <text class="iconfont" :class="isPlaying ? 'icon-pause' : 'icon-play'"></text>
          </view>
        </view>
        <view class="voice-info">
          <view class="waveform">
            <view class="wave" :class="{ 'active': isPlaying }" v-for="i in 8" :key="i"></view>
          </view>
          <text class="duration">{{ companion.voiceDuration || 0 }}s</text>
        </view>
      </view>
    </view>

    <!-- 个人简介 -->
    <view class="section">
      <view class="section-title">关于我</view>
      <view class="summary-content">
        {{ companion.summary || '这个人很懒，什么都没写~' }}
      </view>
      <view class="skill-tags">
        <text class="skill-tag" v-for="(tag, idx) in companion.tags" :key="idx">{{ tag }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-btn icon-wrap">
        <text class="iconfont icon-share"></text>
        <text>分享</text>
      </view>
      <view class="action-btn icon-wrap">
        <text class="iconfont icon-heart"></text>
        <text>关注</text>
      </view>
      <button class="order-btn" @click="goToOrder" :disabled="companion.workStatus !== 1">
        {{ companion.workStatus === 1 ? '立即预约' : '暂时无法接单' }}
      </button>
    </view>
  </view>
  <view v-else class="loading-full">
    <text>加载中...</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onUnmounted } from '@dcloudio/uni-app';
import {request} from '../../utils/request';

const companion = ref<any>(null);
const currentImageIndex = ref(0);

// Audio
const innerAudioContext = uni.createInnerAudioContext();
const isPlaying = ref(false);

onLoad((options: any) => {
  const id = options?.id;
  if (id) fetchDetail(id);

  innerAudioContext.onPlay(() => { isPlaying.value = true; });
  innerAudioContext.onEnded(() => {
    isPlaying.value = false;
  });
});

onUnmounted(() => {
  innerAudioContext.destroy();
});

const fetchDetail = async (id: string) => {
  const res = await request({
    url: `/companions/${id}`,
    method: 'GET'
  });
  if (res.code === 200) {
    companion.value = res.data;
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
    isPlaying.value = false;
  } else {
    innerAudioContext.play();
  }
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
  background-color: $bg-color-page;
  padding-bottom: 120rpx; // 留出底部栏空间
}

.album-wrap {
  position: relative;
  width: 100%;
  height: 750rpx; // 正方形
  
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
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    padding: 6rpx 20rpx;
    border-radius: $border-radius-pill;
    font-size: 24rpx;
    backdrop-filter: blur(4px);
  }
}

.info-card {
  background-color: $bg-color-white;
  border-radius: 40rpx 40rpx 0 0;
  margin-top: -40rpx;
  position: relative;
  z-index: 10;
  padding: 40rpx 30rpx;
  
  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .name-box {
      display: flex;
      align-items: center;
      
      .name {
        font-size: 40rpx;
        font-weight: bold;
        color: $text-color-primary;
        margin-right: 20rpx;
      }
      
      .status-badge {
        display: flex;
        align-items: center;
        background: rgba(16, 185, 129, 0.1);
        color: $color-success;
        padding: 4rpx 16rpx;
        border-radius: $border-radius-pill;
        font-size: 20rpx;
        
        .dot {
          width: 8rpx; height: 8rpx;
          border-radius: 50%;
          background-color: $color-success;
          margin-right: 8rpx;
        }
        
        &.busy {
          background: rgba(245, 158, 11, 0.1);
          color: $color-warning;
          .dot { background-color: $color-warning; }
        }
      }
    }
    
    .price-box {
      color: $color-secondary;
      
      .currency { font-size: 24rpx; font-weight: bold; }
      .price { font-size: 48rpx; font-weight: bold; margin: 0 4rpx; }
      .unit { font-size: 24rpx; color: $text-color-secondary; }
    }
  }
  
  .basic-tags {
    display: flex;
    margin-bottom: 40rpx;
    
    .tag {
      padding: 6rpx 16rpx;
      border-radius: $border-radius-sm;
      font-size: 20rpx;
      margin-right: 16rpx;
      display: flex;
      align-items: center;
      
      .iconfont { font-size: 20rpx; margin-right: 4rpx; }
      
      &.male { background-color: rgba(59, 130, 246, 0.1); color: #3B82F6; }
      &.female { background-color: rgba(236, 72, 153, 0.1); color: #EC4899; }
      &.height { background-color: #F3F4F6; color: $text-color-regular; }
    }
  }
  
  .data-row {
    display: flex;
    justify-content: space-around;
    padding-top: 30rpx;
    border-top: 1rpx solid $border-color-light;
    
    .data-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .value { font-size: 36rpx; font-weight: bold; color: $text-color-primary; margin-bottom: 8rpx; }
      .label { font-size: 24rpx; color: $text-color-secondary; }
    }
  }
}

.section {
  background-color: $bg-color-white;
  margin-top: 20rpx;
  padding: 30rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: $text-color-primary;
    margin-bottom: 30rpx;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      width: 8rpx;
      height: 32rpx;
      background-color: $color-primary;
      border-radius: 4rpx;
      margin-right: 16rpx;
    }
  }
}

.voice-card {
  background-color: #F8F5FF;
  border-radius: $border-radius-lg;
  padding: 20rpx;
  display: flex;
  align-items: center;
  border: 1rpx solid rgba(124, 58, 237, 0.2);
  
  .avatar-wrap {
    position: relative;
    width: 100rpx;
    height: 100rpx;
    
    .avatar {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }
    
    .play-btn {
      position: absolute;
      right: -10rpx;
      bottom: -10rpx;
      width: 44rpx;
      height: 44rpx;
      background-color: $color-primary;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 4rpx solid #fff;
      
      .iconfont { color: #fff; font-size: 20rpx; }
      
      &.playing {
        background-color: $color-secondary;
      }
    }
  }
  
  .voice-info {
    flex: 1;
    margin-left: 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .waveform {
      display: flex;
      align-items: center;
      height: 40rpx;
      
      .wave {
        width: 6rpx; height: 10rpx;
        background-color: $color-primary;
        margin-right: 10rpx;
        border-radius: 3rpx;
        transition: height 0.2s;
        
        &.active {
          &:nth-child(1) { animation: wave 1s infinite 0.1s; }
          &:nth-child(2) { animation: wave 1s infinite 0.3s; }
          &:nth-child(3) { animation: wave 1s infinite 0.5s; }
          &:nth-child(4) { animation: wave 1s infinite 0.2s; }
          &:nth-child(5) { animation: wave 1s infinite 0.4s; }
          &:nth-child(6) { animation: wave 1s infinite 0.6s; }
          &:nth-child(7) { animation: wave 1s infinite 0.3s; }
          &:nth-child(8) { animation: wave 1s infinite 0.1s; }
        }
      }
    }
    
    .duration {
      color: $color-primary;
      font-weight: bold;
      font-size: 28rpx;
    }
  }
}

.summary-content {
  font-size: 28rpx;
  color: $text-color-regular;
  line-height: 1.6;
  margin-bottom: 30rpx;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  
  .skill-tag {
    padding: 10rpx 24rpx;
    background-color: rgba(124, 58, 237, 0.05);
    color: $color-primary;
    border-radius: $border-radius-pill;
    font-size: 24rpx;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: $z-index-sticky;
  
  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: $text-color-regular;
    margin-right: 40rpx;
    
    .iconfont { font-size: 40rpx; margin-bottom: 4rpx; }
    text { font-size: 20rpx; }
  }
  
  .order-btn {
    flex: 1;
    height: 88rpx;
    background: $gradient-primary;
    color: #fff;
    border-radius: $border-radius-pill;
    font-size: 32rpx;
    font-weight: bold;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
    
    &::after { display: none; }
    
    &[disabled] {
      background: #D1D5DB;
      color: #9CA3AF;
    }
  }
}

.loading-full {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  color: $text-color-secondary;
}

@keyframes wave {
  0% { height: 10rpx; }
  50% { height: 40rpx; }
  100% { height: 10rpx; }
}
</style>
