<template>
  <view class="container">
    <!-- 顶部导航与筛选区 -->
    <view class="header-wrap">
      <!-- 搜索框 -->
      <view class="search-bar">
        <text class="iconfont icon-search"></text>
        <input type="text" placeholder="搜索搭子昵称或技能" />
      </view>

      <!-- 筛选标签 -->
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-tabs">
          <view class="tab" :class="{ active: currentSort === 'recommend' }" @click="setSort('recommend')">综合推荐</view>
          <view class="tab" :class="{ active: currentSort === 'distance' }" @click="setSort('distance')">距离优先</view>
          <view class="tab" :class="{ active: currentSort === 'new' }" @click="setSort('new')">最新入驻</view>
          <view class="tab" :class="{ active: currentSort === 'rating' }" @click="setSort('rating')">评分最高</view>
        </view>
      </scroll-view>
      
      <!-- 次级筛选 -->
      <view class="sub-filters">
        <view class="sub-tab" @click="showGenderPicker = true">
          性别 <text class="iconfont icon-arrow-down"></text>
        </view>
        <view class="sub-tab">
          只看在线
        </view>
        <view class="sub-tab">
          价格 <text class="iconfont icon-sort"></text>
        </view>
      </view>
    </view>

    <!-- 列表区 -->
    <scroll-view scroll-y class="list-content" @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh">
      <!-- 骨架加载状态 -->
      <view class="skeleton-grid" v-if="loading && companionList.length === 0">
        <SkeletonCard variant="companion" v-for="i in 4" :key="i" />
      </view>

      <view class="companion-list" v-else>
        <view class="companion-card" v-for="item in companionList" :key="item.userId" @click="goToDetail(item.userId)" hover-class="card-hover">
          <view class="card-cover-wrap">
            <image :src="item.coverUrl || fallbackCover" mode="aspectFill" class="card-cover" lazy-load></image>
            <view class="status-badge" :class="{ 'busy': item.workStatus === 2 }">
              <text class="dot"></text>
              <text>{{ item.workStatus === 1 ? '可接单' : (item.workStatus === 2 ? '忙碌中' : '休息中') }}</text>
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

      <!-- 加载状态 -->
      <view class="loading-state">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多数据了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import {onLoad} from '@dcloudio/uni-app';
import {ref} from 'vue';
import {request} from '../../utils/request';
import {useAppStore} from '../../store/app';
import SkeletonCard from '../../components/SkeletonCard.vue';

const appStore = useAppStore();
const companionList = ref<any[]>([]);
const current = ref(1);
const size = ref(10);
const loading = ref(false);
const noMore = ref(false);
const isRefreshing = ref(false);
const currentSort = ref('recommend');
const categoryId = ref<string | null>(null);
const showGenderPicker = ref(false);
const fallbackCover = 'https://picsum.photos/seed/companion-list-cover/600/800';

onLoad((options: any) => {
  categoryId.value = options?.categoryId || null;
  keyword.value = options?.keyword || '';
  fetchList(true);
});

const setSort = (sort: string) => {
  currentSort.value = sort;
  fetchList(true);
};

const keyword = ref('');
const selectedGender = ref(0);

const fetchList = async (reset = false) => {
  if (reset) {
    current.value = 1;
    noMore.value = false;
  }
  if (noMore.value || loading.value) return;

  loading.value = true;
  try {
    const params = new URLSearchParams();
    params.append('current', String(current.value));
    params.append('size', String(size.value));
    if (categoryId.value) params.append('categoryId', categoryId.value);
    if (keyword.value) params.append('keyword', keyword.value);
    if (selectedGender.value > 0) params.append('gender', String(selectedGender.value));
    if (currentSort.value !== 'recommend') params.append('sortBy', currentSort.value);

    const res = await request({
      url: `/companions?${params.toString()}`,
      method: 'GET'
    });
    
    if (res.code === 200) {
      if (reset) {
        companionList.value = res.data.records;
      } else {
        companionList.value = [...companionList.value, ...res.data.records];
      }
      
      if (res.data.records.length < size.value) {
        noMore.value = true;
      } else {
        current.value++;
      }
    }
  } finally {
    loading.value = false;
    isRefreshing.value = false;
  }
};

const onRefresh = () => {
  isRefreshing.value = true;
  fetchList(true);
};

const loadMore = () => {
  fetchList();
};

const goToDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/companion/detail?id=${id}`
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-main);
  color: var(--text-primary);
}

.header-wrap {
  background-color: var(--bg-card);
  padding: 20rpx 0 10rpx;
  z-index: $z-index-sticky;
  border-bottom: 1px solid var(--border-color);
}

.search-bar {
  margin: 0 30rpx 20rpx;
  background-color: var(--bg-main);
  height: 72rpx;
  border-radius: $border-radius-pill;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  
  .icon-search {
    color: $text-color-secondary;
    font-size: 32rpx;
    margin-right: 16rpx;
  }
  
  input {
    flex: 1;
    font-size: $font-size-sm;
  }
}

.filter-scroll {
  white-space: nowrap;
  padding: 0 10rpx;
}

.filter-tabs {
  display: inline-flex;
  padding: 0 20rpx;
  
  .tab {
    display: inline-block;
    padding: 10rpx 30rpx;
    font-size: $font-size-base;
    color: $text-color-regular;
    position: relative;
    transition: all 0.2s;
    
    &.active {
      color: $color-primary;
      font-weight: bold;
      font-size: 32rpx;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 32rpx;
        height: 6rpx;
        background: $gradient-primary;
        border-radius: 4rpx;
      }
    }
  }
}

.sub-filters {
  display: flex;
  padding: 20rpx 30rpx;
  
  .sub-tab {
    background-color: var(--bg-main);
    border: 1px solid var(--border-color);
    padding: 8rpx 24rpx;
    border-radius: $border-radius-pill;
    font-size: 24rpx;
    color: $text-color-regular;
    margin-right: 20rpx;
    display: flex;
    align-items: center;
    
    .iconfont {
      font-size: 20rpx;
      margin-left: 8rpx;
    }
  }
}

.list-content {
  flex: 1;
  overflow: hidden;
}

.companion-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
  padding: 20rpx;

  .skeleton-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24rpx;
    padding: 0 10rpx;
  }

  .card-hover { opacity: 0.85; transform: scale(0.98); }

  .companion-card {
    background-color: var(--bg-card);
    border-radius: $border-radius-lg;
    overflow: hidden;
    margin-bottom: 20rpx;
    box-shadow: $box-shadow-sm;
    
    .card-cover-wrap {
      position: relative;
      width: 100%;
      height: 400rpx;
      
      .card-cover {
        width: 100%;
        height: 100%;
      }
      
      .status-badge {
        position: absolute;
        top: 16rpx;
        left: 16rpx;
        background: rgba(26, 22, 19, 0.62);
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
        height: 36rpx;
        overflow: hidden;
        
        .tag {
          font-size: 20rpx;
          color: $color-primary;
          background-color: rgba(var(--color-primary-rgb), 0.12);
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
          font-size: 32rpx;
          
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

.loading-state {
  text-align: center;
  padding: 30rpx 0 50rpx;
  font-size: 24rpx;
  color: $text-color-secondary;
}
</style>
