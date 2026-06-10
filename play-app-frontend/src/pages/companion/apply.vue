<template>
  <view class="container">
    <view class="status-box" v-if="applyStatus !== -1">
      <view class="status-card" :class="statusClass">
        <text class="iconfont" :class="statusIcon"></text>
        <view class="status-info">
          <text class="title">{{ statusTitle }}</text>
          <text class="desc">{{ statusDesc }}</text>
        </view>
      </view>
    </view>

    <view class="form-wrapper" v-if="applyStatus === -1 || applyStatus === 2">
      <view class="section-title">基本信息</view>
      <view class="form-group">
        <view class="form-item">
          <text class="label">真实姓名</text>
          <input type="text" v-model="formData.realName" placeholder="身份证姓名 (不对外展示)" />
        </view>
        <view class="form-item">
          <text class="label">对外昵称</text>
          <input type="text" v-model="formData.nickname" placeholder="客户看到的名称" />
        </view>
        <view class="form-item" v-if="applyStatus !== -1 && existingCode">
          <text class="label">你的助教号</text>
          <view class="code-display">{{ existingCode }}</view>
          <text class="code-tip">客户可通过此号码搜索到你</text>
        </view>
        <view class="form-item">
          <text class="label">微信号</text>
          <input type="text" v-model="formData.wechatCode" placeholder="请输入用于接单的微信号" />
        </view>
        <view class="form-item">
          <text class="label">性别</text>
          <picker @change="onGenderChange" :range="genderOptions" range-key="name">
            <view class="picker-value" :class="{ 'placeholder': !formData.gender }">
              {{ genderText }}
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">年龄</text>
          <input type="number" v-model="formData.age" placeholder="请输入年龄" inputmode="numeric" />
        </view>
        <view class="form-item">
          <text class="label">身高 (cm)</text>
          <input type="number" v-model="formData.height" placeholder="请输入身高" inputmode="numeric" />
        </view>
      </view>

      <view class="section-title">服务信息</view>
      <view class="form-group">
        <view class="form-item">
          <text class="label">收费标准</text>
          <view class="price-input">
            <input type="digit" v-model="formData.pricePerHour" placeholder="0" inputmode="decimal" />
            <text class="unit">元/小时</text>
          </view>
        </view>
        <view class="form-item vertical">
          <text class="label">个人简介</text>
          <textarea v-model="formData.summary" placeholder="请简单介绍一下自己，吸引更多用户~ (最多200字)" maxlength="200"></textarea>
        </view>
      </view>

      <view class="section-title">个人展示 (审核关键)</view>
      <view class="form-group transparent">
        <view class="upload-item">
          <text class="label-title">照片墙 <text class="sub">(1~6张，第一张为封面)</text></text>
          <view class="image-grid">
            <view class="img-box" v-for="(img, index) in formData.photoUrls" :key="index">
              <image :src="img" mode="aspectFill"></image>
              <view class="del-btn" @click="removePhoto(index)">
                <text class="iconfont icon-close"></text>
              </view>
            </view>
            <view class="upload-btn" @click="chooseImage" v-if="formData.photoUrls.length < 6">
              <text class="iconfont icon-camera"></text>
              <text>上传照片</text>
            </view>
          </view>
        </view>

        <view class="upload-item">
          <text class="label-title">语音介绍 <text class="sub">(录制一段 5~15s 的真人介绍)</text></text>
          <view class="voice-box">
            <view class="record-btn" @longpress="startRecord" @touchend="stopRecord" :class="{ 'recording': isRecording }">
              <text class="iconfont icon-mic"></text>
              <text>{{ isRecording ? '松开 结束' : '长按 录音' }}</text>
            </view>
            <view class="voice-preview" v-if="formData.voiceUrl" @click="playVoice">
              <view class="play-icon">
                <text class="iconfont" :class="isPlaying ? 'icon-pause' : 'icon-play'"></text>
              </view>
              <view class="waveform">
                <view class="wave" :class="{ 'active': isPlaying }" v-for="i in 5" :key="i"></view>
              </view>
              <text class="duration">{{ formData.voiceDuration }}s</text>
              <view class="del-voice" @click.stop="removeVoice">
                <text class="iconfont icon-delete"></text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="submit-btn-wrap">
        <button class="submit-btn" @click="submitApply" :loading="isSubmitting">提交申请</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {request} from '../../utils/request';
import {useAppStore} from '../../store/app';
import {haptic} from '../../utils/haptic';

// API Configuration
const appStore = useAppStore();
const UPLOAD_URL = 'http://127.0.0.1:8080/api/file/upload';

const applyStatus = ref(-1); // -1: 未申请, 0: 待审核, 1: 通过, 2: 驳回
const rejectReason = ref('');
const existingCode = ref('');

const formData = ref({
  realName: '',
  nickname: '',
  companionCode: '',
  wechatCode: '',
  gender: null as number | null,
  age: '',
  height: '',
  summary: '',
  tagIds: [1],
  pricePerHour: '',
  photoUrls: [] as string[],
  voiceUrl: '',
  voiceDuration: 0
});

const genderOptions = [
  { id: 1, name: '男' },
  { id: 2, name: '女' }
];

const genderText = computed(() => {
  if (!formData.value.gender) return '请选择性别';
  return genderOptions.find(item => item.id === formData.value.gender)?.name;
});

const onGenderChange = (e: any) => {
  const index = e.detail.value;
  formData.value.gender = genderOptions[index].id;
};

// 状态卡片计算属性
const statusClass = computed(() => {
  if (applyStatus.value === 0) return 'status-pending';
  if (applyStatus.value === 1) return 'status-success';
  if (applyStatus.value === 2) return 'status-reject';
  return '';
});

const statusIcon = computed(() => {
  if (applyStatus.value === 0) return 'icon-time';
  if (applyStatus.value === 1) return 'icon-check-circle';
  if (applyStatus.value === 2) return 'icon-close-circle';
  return '';
});

const statusTitle = computed(() => {
  if (applyStatus.value === 0) return '申请审核中';
  if (applyStatus.value === 1) return '审核已通过';
  if (applyStatus.value === 2) return '审核被驳回';
  return '';
});

const statusDesc = computed(() => {
  if (applyStatus.value === 0) return '您的资料已提交，工作人员将在1-2个工作日内完成审核，请耐心等待。';
  if (applyStatus.value === 1) return '恭喜您成为平台认证助教，快去接单吧！';
  if (applyStatus.value === 2) return `驳回原因：${rejectReason.value}。请修改下方资料后重新提交。`;
  return '';
});

// 上传图片
const chooseImage = () => {
  const count = 6 - formData.value.photoUrls.length;
  uni.chooseImage({
    count,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths as string[];
      // 循环上传
      uni.showLoading({ title: '上传中' });
      let uploadCount = 0;
      for (const tempPath of tempFilePaths) {
        uni.uploadFile({
          url: UPLOAD_URL,
          filePath: tempPath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${uni.getStorageSync('token')}`
          },
          success: (uploadRes) => {
            const data = JSON.parse(uploadRes.data);
            if (data.code === 200) {
              formData.value.photoUrls.push(data.data);
            }
          },
          complete: () => {
            uploadCount++;
            if (uploadCount === tempFilePaths.length) {
              uni.hideLoading();
            }
          }
        });
      }
    }
  });
};

const removePhoto = (index: number) => {
  formData.value.photoUrls.splice(index, 1);
};

// 录音相关
const recorderManager = uni.getRecorderManager ? uni.getRecorderManager() : null;
const innerAudioContext = uni.createInnerAudioContext();
const isRecording = ref(false);
const isPlaying = ref(false);
let recordStartTime = 0;

if (recorderManager) {
  recorderManager.onStart(() => {
    isRecording.value = true;
    recordStartTime = Date.now();
  });
}

if (recorderManager) {
  recorderManager.onStop((res) => {
  isRecording.value = false;
  const duration = Math.round((Date.now() - recordStartTime) / 1000);
  
  if (duration < 3) {
    uni.showToast({ title: '录音太短啦', icon: 'none' });
    return;
  }
  
  formData.value.voiceDuration = duration;
  
  // 上传录音
  uni.showLoading({ title: '上传中' });
  uni.uploadFile({
    url: UPLOAD_URL,
    filePath: res.tempFilePath,
    name: 'file',
    header: {
      'Authorization': `Bearer ${uni.getStorageSync('token')}`
    },
    success: (uploadRes) => {
      const data = JSON.parse(uploadRes.data);
      if (data.code === 200) {
        formData.value.voiceUrl = data.data;
      }
    },
    complete: () => {
      uni.hideLoading();
    }
  });
});
} // end if (recorderManager)

innerAudioContext.onPlay(() => {
  isPlaying.value = true;
});

innerAudioContext.onEnded(() => {
  isPlaying.value = false;
});

const startRecord = () => {
  if (!recorderManager) return uni.showToast({ title: 'H5不支持录音', icon: 'none' });
  recorderManager.start({
    duration: 15000,
    format: 'mp3'
  });
};

const stopRecord = () => {
  if (!recorderManager) return;
  recorderManager.stop();
};

const playVoice = () => {
  if (isPlaying.value) {
    innerAudioContext.stop();
    isPlaying.value = false;
  } else {
    innerAudioContext.src = formData.value.voiceUrl;
    innerAudioContext.play();
  }
};

const removeVoice = () => {
  formData.value.voiceUrl = '';
  formData.value.voiceDuration = 0;
  if (isPlaying.value) {
    innerAudioContext.stop();
    isPlaying.value = false;
  }
};

// 提交与查询逻辑
const isSubmitting = ref(false);

const fetchApplyStatus = async () => {
  try {
    const res = await request({ url: '/companion/apply/status', method: 'GET' });
    if (res.code === 200 && res.data) {
      applyStatus.value = res.data.auditStatus;
      rejectReason.value = res.data.rejectReason || '';
      existingCode.value = res.data.companionCode || '';
      
      // 回显数据 (驳回时方便修改)
      if (applyStatus.value === 2) {
        Object.assign(formData.value, res.data);
      }
    }
  } catch (e) {
    // 未申请
  }
};

const submitApply = async () => {
  if (!formData.value.realName) return uni.showToast({ title: '请输入真实姓名', icon: 'none' });
  if (!formData.value.nickname) return uni.showToast({ title: '请输入对外昵称', icon: 'none' });
  if (!formData.value.wechatCode) return uni.showToast({ title: '请输入微信号', icon: 'none' });
  if (!formData.value.gender) return uni.showToast({ title: '请选择性别', icon: 'none' });
  if (!formData.value.pricePerHour) return uni.showToast({ title: '请输入价格', icon: 'none' });
  if (formData.value.photoUrls.length === 0) return uni.showToast({ title: '请至少上传一张照片', icon: 'none' });

  isSubmitting.value = true;
  try {
    const res = await request({
      url: '/companion/apply',
      method: 'POST',
      data: formData.value
    });
    if (res.code === 200) {
      uni.showToast({ title: '提交成功', icon: 'success' });
      haptic.heavy();
      applyStatus.value = 0; // 变为审核中
    }
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  fetchApplyStatus();
});
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $bg-color-page;
  padding: 20rpx;
}

.status-box {
  margin-bottom: 30rpx;
  
  .status-card {
    display: flex;
    padding: 30rpx;
    border-radius: $border-radius-lg;
    background-color: $bg-color-white;
    
    .iconfont {
      font-size: 80rpx;
      margin-right: 20rpx;
    }
    
    .status-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .title {
        font-size: $font-size-lg;
        font-weight: bold;
        margin-bottom: 10rpx;
      }
      
      .desc {
        font-size: $font-size-sm;
        line-height: 1.5;
      }
    }
    
    &.status-pending {
      background-color: rgba(245, 158, 11, 0.1);
      .iconfont, .title { color: $color-warning; }
      .desc { color: var(--color-warning); }
    }
    
    &.status-success {
      background-color: rgba(16, 185, 129, 0.1);
      .iconfont, .title { color: $color-success; }
      .desc { color: var(--color-success); }
    }
    
    &.status-reject {
      background-color: rgba(239, 68, 68, 0.1);
      .iconfont, .title { color: $color-error; }
      .desc { color: var(--color-error); }
    }
  }
}

.form-wrapper {
  padding-bottom: 100rpx;
}

.section-title {
  font-size: $font-size-base;
  font-weight: bold;
  color: $text-color-secondary;
  margin: 30rpx 0 16rpx 10rpx;
}

.form-group {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 0 30rpx;
  margin-bottom: 30rpx;
  
  &.transparent {
    background-color: transparent;
    padding: 0;
  }
  
  .form-item {
    display: flex;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid $border-color-light;
    
    &:last-child {
      border-bottom: none;
    }
    
    &.vertical {
      flex-direction: column;
      align-items: flex-start;
      
      .label { margin-bottom: 20rpx; width: 100%; }
      textarea {
        width: 100%;
        height: 160rpx;
        background-color: $bg-color-page;
        border-radius: $border-radius-md;
        padding: 20rpx;
        font-size: $font-size-sm;
        box-sizing: border-box;
      }
    }
    
    .label {
      width: 180rpx;
      font-size: $font-size-base;
      color: $text-color-primary;
    }
    
    input, picker {
      flex: 1;
      font-size: $font-size-base;
    }
    
    .picker-value {
      &.placeholder { color: $text-color-placeholder; }
    }
    
    .price-input {
      flex: 1;
      display: flex;
      align-items: center;
      
      input { flex: 1; color: $color-secondary; font-weight: bold; }
      .unit { font-size: $font-size-sm; color: $text-color-secondary; margin-left: 10rpx; }
    }
  }
}

.upload-item {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .label-title {
    display: block;
    font-size: $font-size-base;
    font-weight: bold;
    color: $text-color-primary;
    margin-bottom: 24rpx;
    
    .sub {
      font-size: $font-size-xs;
      color: $text-color-secondary;
      font-weight: normal;
    }
  }
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -10rpx;
  
  .img-box, .upload-btn {
    width: calc(33.33% - 20rpx);
    height: 200rpx;
    margin: 10rpx;
    border-radius: $border-radius-md;
    overflow: hidden;
    position: relative;
  }
  
  .img-box {
    image { width: 100%; height: 100%; }
    .del-btn {
      position: absolute;
      top: 0; right: 0;
      background: rgba(0,0,0,0.5);
      width: 40rpx; height: 40rpx;
      display: flex; justify-content: center; align-items: center;
      border-bottom-left-radius: $border-radius-md;
      .icon-close { color: #fff; font-size: 20rpx; }
    }
  }
  
  .upload-btn {
    background-color: $bg-color-page;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: $text-color-secondary;
    border: 2rpx dashed $border-color;
    
    .icon-camera { font-size: 48rpx; margin-bottom: 10rpx; }
    text { font-size: $font-size-xs; }
  }
}

.voice-box {
  display: flex;
  align-items: center;
  
  .record-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: $gradient-primary;
    color: white;
    box-shadow: $box-shadow-md;
    transition: all 0.3s;
    
    &.recording {
      transform: scale(1.1);
      box-shadow: 0 0 20rpx rgba(236, 72, 153, 0.5);
      animation: pulse 1s infinite;
    }
    
    .icon-mic { font-size: 48rpx; margin-bottom: 8rpx; }
    text { font-size: 20rpx; }
  }
  
  .voice-preview {
    flex: 1;
    margin-left: 30rpx;
    background-color: var(--bg-subtle);
    border-radius: $border-radius-pill;
    height: 80rpx;
    display: flex;
    align-items: center;
    padding: 0 30rpx 0 10rpx;
    position: relative;
    border: 1rpx solid rgba(var(--color-primary-rgb), 0.2);
    
    .play-icon {
      width: 60rpx; height: 60rpx;
      background-color: $color-primary;
      border-radius: 50%;
      display: flex; justify-content: center; align-items: center;
      color: white;
      margin-right: 20rpx;
    }
    
    .waveform {
      flex: 1;
      display: flex;
      align-items: center;
      height: 40rpx;
      
      .wave {
        width: 4rpx; height: 30rpx;
        background-color: $color-primary;
        margin-right: 8rpx;
        transform-origin: bottom;
        border-radius: 2rpx;
        border-radius: 2rpx;
        transition: height 0.2s;
        
        &.active {
          &:nth-child(1) { animation: wave 1s infinite 0.1s; }
          &:nth-child(2) { animation: wave 1s infinite 0.3s; }
          &:nth-child(3) { animation: wave 1s infinite 0.5s; }
          &:nth-child(4) { animation: wave 1s infinite 0.2s; }
          &:nth-child(5) { animation: wave 1s infinite 0.4s; }
        }
      }
    }
    
    .duration {
      font-size: $font-size-sm;
      color: $color-primary;
      font-weight: bold;
    }
    
    .del-voice {
      position: absolute;
      right: -20rpx;
      top: -20rpx;
      width: 40rpx; height: 40rpx;
      background: $bg-color-white;
      border-radius: 50%;
      box-shadow: $box-shadow-sm;
      display: flex; justify-content: center; align-items: center;
      color: $text-color-secondary;
    }
  }
}

.submit-btn-wrap {
  margin-top: 60rpx;
  
  .submit-btn {
    background: $gradient-primary;
    color: white;
    border-radius: $border-radius-pill;
    font-size: $font-size-lg;
    font-weight: bold;
    border: none;
    
    &::after { display: none; }
    &:active { opacity: 0.9; }
  }
}

@keyframes pulse {
  0% { transform: scale(1); box-shadow: 0 0 0 0 rgba(236, 72, 153, 0.7); }
  70% { transform: scale(1.1); box-shadow: 0 0 0 20rpx rgba(236, 72, 153, 0); }
  100% { transform: scale(1); box-shadow: 0 0 0 0 rgba(236, 72, 153, 0); }
}

@keyframes wave {
  0% { transform: scaleY(0.35); }
  50% { transform: scaleY(1); }
  100% { transform: scaleY(0.35); }
}
.code-display {
  font-size: 48rpx;
  font-weight: bold;
  color: $color-primary;
  letter-spacing: 8rpx;
  padding: 12rpx 0;
}
.code-tip {
  font-size: 22rpx;
  color: $text-color-secondary;
  display: block;
  margin-top: 6rpx;
}
</style>
