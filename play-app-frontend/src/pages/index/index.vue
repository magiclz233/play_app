<template>
  <view class="container">
    <!-- 顶部状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 顶部搜索与定位 -->
    <view class="header">
      <view class="location">
        <text class="iconfont icon-location"></text>
        <text class="city">杭州市</text>
        <text class="iconfont icon-arrow-down"></text>
      </view>
      <view class="search-box">
        <text class="iconfont icon-search"></text>
        <input type="text" placeholder="搜索心仪搭子或技能" placeholder-class="placeholder" />
      </view>
    </view>

    <scroll-view scroll-y class="main-content">
      <!-- 轮播图 Banner -->
      <swiper class="banner-swiper" circular autoplay interval="3000" indicator-dots indicator-active-color="#7C3AED">
        <swiper-item v-for="(item, index) in bannerList" :key="index" class="swiper-item">
          <image :src="item.image" mode="aspectFill" class="banner-img"></image>
        </swiper-item>
      </swiper>

      <!-- 我要接单 Banner -->
      <view class="companion-banner" @click="goToCompanion">
        <view class="banner-text">
          <text class="banner-title">🎯 我要接单</text>
          <text class="banner-sub">成为陪玩，展示技能，获得收益</text>
        </view>
        <text class="banner-arrow">›</text>
      </view>

      <!-- 服务分类 -->
      <view class="category-grid">
        <view class="category-item" v-for="(item, index) in categoryList" :key="index" @click="goToCategory(item.id)">
          <view class="icon-wrap">
            <image :src="item.icon" mode="aspectFit" class="category-icon"></image>
          </view>
          <text class="category-name">{{ item.name }}</text>
        </view>
      </view>

      <!-- 推荐列表头部 -->
      <view class="section-header">
        <text class="title">推荐搭子</text>
        <view class="filter-tabs">
          <text class="tab active">综合</text>
          <text class="tab">附近</text>
          <text class="tab">新人</text>
        </view>
      </view>

      <!-- 陪玩卡片列表 -->
      <view class="companion-list">
        <view class="companion-card" v-for="(item, index) in companionList" :key="index" @click="goToDetail(item.userId)">
          <view class="card-cover-wrap">
            <image :src="item.coverUrl" mode="aspectFill" class="card-cover"></image>
            <view class="status-badge" :class="{ 'busy': item.workStatus !== 1 }">
              <text class="dot"></text>
              <text>{{ item.workStatus === 1 ? '可接单' : '忙碌中' }}</text>
            </view>
          </view>

          <view class="card-info">
            <view class="info-top">
              <text class="name">{{ item.nickname }}</text>
              <view class="rating">
                <text class="iconfont icon-star"></text>
                <text class="score">{{ item.rating }}</text>
              </view>
            </view>
            <view class="tags">
              <text class="tag" v-for="(tag, tIdx) in item.tags" :key="tIdx">{{ tag }}</text>
            </view>
            <view class="info-bottom">
              <text class="price"><text class="currency">¥</text>{{ item.pricePerHour }}<text class="unit">/小时</text></text>
              <text class="distance">{{ item.distance }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="loading-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {request} from '../../utils/request';

const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight || 20);

const bannerList = ref([
  { image: 'https://images.unsplash.com/photo-1543269865-cbf427effbad?q=80&w=800&auto=format&fit=crop' },
  { image: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&auto=format&fit=crop' }
]);

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
}
const companionList = ref<CompanionItem[]>([]);

const loadCategories = async () => {
  try {
    const res = await request({ url: '/categories', method: 'GET' });
    if (res.code === 200) {
      categoryList.value = res.data.map((cat: any) => ({
        id: cat.id,
        name: cat.name,
        icon: cat.iconUrl || 'https://api.iconify.design/noto:plus.svg'
      }));
    }
  } catch (e) {
    console.error('加载分类失败', e);
  }
};

const loadCompanions = async () => {
  try {
    const res = await request({ url: '/companions/recommended', method: 'GET' });
    if (res.code === 200) {
      companionList.value = res.data;
    }
  } catch (e) {
    console.error('加载推荐失败', e);
  }
};

onMounted(() => {
  loadCategories();
  loadCompanions();
});

const goToCategory = (id: number) => {
  uni.navigateTo({ url: `/pages/companion/list?categoryId=${id}` });
};

const goToDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/companion/detail?id=${id}` });
};

const goToCompanion = () => {
  uni.switchTab({ url: '/pages/mine/index' });
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $bg-color-page;
}

.header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: $bg-color-white;
  
  .location {
    display: flex;
    align-items: center;
    margin-right: 30rpx;
    font-size: $font-size-base;
    font-weight: 500;
    
    .city {
      margin: 0 8rpx;
    }
  }
  
  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    background-color: #F3F4F6;
    border-radius: $border-radius-pill;
    padding: 12rpx 24rpx;
    
    .icon-search {
      color: $text-color-secondary;
      margin-right: 12rpx;
      font-size: 32rpx;
    }
    
    input {
      flex: 1;
      font-size: $font-size-sm;
    }
    
    .placeholder {
      color: $text-color-placeholder;
    }
  }
}

.main-content {
  flex: 1;
  overflow: hidden;
}

.companion-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 30rpx 20rpx;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, #FFF7ED, #FED7AA);
  border-radius: $border-radius-lg;
  .banner-title { font-size: 28rpx; font-weight: bold; color: #EA580C; display: block; }
  .banner-sub { font-size: 22rpx; color: #C2410C; margin-top: 6rpx; display: block; }
  .banner-arrow { font-size: 40rpx; color: #EA580C; font-weight: bold; }
}

.banner-swiper {
  height: 300rpx;
  padding: 20rpx 30rpx;
  
  .swiper-item {
    border-radius: $border-radius-lg;
    overflow: hidden;
    transform: translateY(0); // Fix border-radius issue in some swipers
  }
  
  .banner-img {
    width: 100%;
    height: 100%;
  }
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 10rpx 10rpx 30rpx;
  background-color: $bg-color-white;
  margin: 0 30rpx;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-sm;
  
  .category-item {
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 30rpx;
    
    .icon-wrap {
      width: 90rpx;
      height: 90rpx;
      background-color: #F8F5FF;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      
      .category-icon {
        width: 50rpx;
        height: 50rpx;
      }
    }
    
    .category-name {
      font-size: $font-size-sm;
      color: $text-color-regular;
    }
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  
  .title {
    font-size: $font-size-lg;
    font-weight: bold;
    color: $text-color-primary;
  }
  
  .filter-tabs {
    display: flex;
    
    .tab {
      font-size: $font-size-sm;
      color: $text-color-secondary;
      margin-left: 30rpx;
      
      &.active {
        color: $color-primary;
        font-weight: 500;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          bottom: -8rpx;
          left: 50%;
          transform: translateX(-50%);
          width: 24rpx;
          height: 4rpx;
          background-color: $color-primary;
          border-radius: 2rpx;
        }
      }
    }
  }
}

.companion-list {
  display: flex;
  flex-wrap: wrap;
  padding: 0 20rpx;
  justify-content: space-between;
  
  .companion-card {
    width: calc(50% - 10rpx);
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    overflow: hidden;
    margin-bottom: 20rpx;
    box-shadow: $box-shadow-sm;
    
    .card-cover-wrap {
      position: relative;
      width: 100%;
      height: 360rpx;
      
      .card-cover {
        width: 100%;
        height: 100%;
      }
      
      .status-badge {
        position: absolute;
        top: 16rpx;
        left: 16rpx;
        background: rgba(0, 0, 0, 0.5);
        backdrop-filter: blur(4px);
        border-radius: $border-radius-pill;
        padding: 4rpx 16rpx;
        display: flex;
        align-items: center;
        font-size: 20rpx;
        color: #fff;
        
        .dot {
          width: 8rpx;
          height: 8rpx;
          border-radius: 50%;
          background-color: $color-success;
          margin-right: 8rpx;
        }
        
        &.busy {
          .dot {
            background-color: $color-warning;
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
        margin-bottom: 12rpx;
        
        .name {
          font-size: $font-size-base;
          font-weight: bold;
          color: $text-color-primary;
        }
        
        .rating {
          display: flex;
          align-items: center;
          color: $color-warning;
          font-size: $font-size-sm;
          font-weight: bold;
          
          .icon-star {
            font-size: 20rpx;
            margin-right: 4rpx;
          }
        }
      }
      
      .tags {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 16rpx;
        height: 36rpx; // Fixed height to align
        overflow: hidden;
        
        .tag {
          font-size: 20rpx;
          color: $color-primary;
          background-color: rgba(124, 58, 237, 0.1);
          padding: 2rpx 12rpx;
          border-radius: $border-radius-sm;
          margin-right: 8rpx;
        }
      }
      
      .info-bottom {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        
        .price {
          color: $color-secondary;
          font-weight: bold;
          font-size: 36rpx;
          
          .currency {
            font-size: $font-size-xs;
            margin-right: 2rpx;
          }
          
          .unit {
            font-size: $font-size-xs;
            color: $text-color-secondary;
            font-weight: normal;
          }
        }
        
        .distance {
          font-size: 20rpx;
          color: $text-color-secondary;
        }
      }
    }
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx 0 50rpx;
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
