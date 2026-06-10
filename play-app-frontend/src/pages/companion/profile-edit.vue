<template>
  <view class="container" :class="appStore.themeClass">
    <view class="tabs">
      <view class="tab" :class="{ active: activeTab === 0 }" @click="activeTab = 0">相册</view>
      <view class="tab" :class="{ active: activeTab === 1 }" @click="activeTab = 1">技能</view>
      <view class="tab" :class="{ active: activeTab === 2 }" @click="activeTab = 2">标签</view>
    </view>

    <!-- 相册 Tab -->
    <view class="tab-content" v-if="activeTab === 0">
      <view class="album-grid">
        <view class="album-item" v-for="(photo, i) in albums" :key="photo.id">
          <image :src="photo.imageUrl" mode="aspectFill" @click="previewImage(photo.imageUrl)"></image>
          <view class="photo-actions">
            <text v-if="photo.isCover" class="cover-badge">封面</text>
            <text v-else class="set-cover" @click="setCover(photo.id)">设为封面</text>
            <text class="delete" @click="deletePhoto(photo.id)">删除</text>
          </view>
        </view>
        <view class="album-item add-btn" v-if="albums.length < 9" @click="addPhoto">
          <text class="plus">+</text>
          <text class="add-text">添加照片</text>
        </view>
      </view>
    </view>

    <!-- 技能 Tab -->
    <view class="tab-content" v-if="activeTab === 1">
      <view class="skill-card" v-for="skill in skills" :key="skill.id">
        <view class="skill-info">
          <text class="skill-name">{{ getCategoryName(skill.categoryId) }}</text>
          <text class="skill-price">¥{{ skill.pricePerHour }}/小时</text>
          <text class="skill-desc" v-if="skill.experienceDesc">{{ skill.experienceDesc }}</text>
        </view>
        <view class="skill-actions">
          <text class="edit" @click="editSkill(skill)">修改</text>
          <text class="delete" @click="deleteSkill(skill.id)">删除</text>
        </view>
      </view>
      <button class="add-skill-btn" @click="showAddSkill = true">添加技能</button>

      <view class="modal-mask" v-if="showAddSkill || editingSkill" @click="closeSkillModal">
        <view class="modal" @click.stop>
          <text class="modal-title">{{ editingSkill ? '修改技能' : '添加技能' }}</text>
          <view class="form-item">
            <text class="label">品类</text>
            <picker :range="categoryNames" @change="onCategoryChange" :value="skillForm.categoryIndex">
              <text class="value">{{ categoryNames[skillForm.categoryIndex] || '请选择' }}</text>
            </picker>
          </view>
          <view class="form-item">
            <text class="label">价格(元/小时)</text>
            <input v-model="skillForm.price" type="digit" placeholder="输入价格" />
          </view>
          <view class="form-item">
            <text class="label">经验描述</text>
            <input v-model="skillForm.desc" placeholder="如：王者百星、台球十年" />
          </view>
          <view class="modal-btns">
            <button class="btn cancel" @click="closeSkillModal">取消</button>
            <button class="btn confirm" @click="saveSkill">确定</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 标签 Tab -->
    <view class="tab-content" v-if="activeTab === 2">
      <view class="tag-grid">
        <view class="tag-item" v-for="tag in allTags" :key="tag.id"
              :class="{ selected: selectedTagIds.includes(tag.id) }"
              @click="toggleTag(tag.id)">
          {{ tag.name }}
        </view>
      </view>
      <button class="save-tags-btn" @click="saveTags">保存标签</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { request } from '../../utils/request';
import { useAppStore } from '../../store/app';

const appStore = useAppStore();
const activeTab = ref(0);
const albums = ref<any[]>([]);
const skills = ref<any[]>([]);
const allTags = ref<any[]>([]);
const selectedTagIds = ref<number[]>([]);
const showAddSkill = ref(false);
const editingSkill = ref<any>(null);

const categories = ref<any[]>([]);
const categoryNames = ref<string[]>([]);
const skillForm = ref({ categoryIndex: 0, categoryId: 0, price: '', desc: '' });

onMounted(async () => {
  await loadCategories();
  await Promise.all([fetchAlbums(), fetchSkills(), fetchTags(), fetchMyTags()]);
});

const loadCategories = async () => {
  try {
    const res = await request({ url: '/categories', method: 'GET' });
    if (res.code === 200) {
      categories.value = res.data;
      categoryNames.value = res.data.map((c: any) => c.name);
    }
  } catch (_) {}
};

const getCategoryName = (id: number) => {
  const cat = categories.value.find((c: any) => c.id === id);
  return cat ? cat.name : '品类' + id;
};

const fetchAlbums = async () => {
  try {
    const res = await request({ url: '/companion/albums', method: 'GET' });
    if (res.code === 200) albums.value = res.data;
  } catch (_) {}
};

const fetchSkills = async () => {
  try {
    const res = await request({ url: '/companion/skills', method: 'GET' });
    if (res.code === 200) skills.value = res.data;
  } catch (_) {}
};

const fetchTags = async () => {
  try {
    const res = await request({ url: '/companion/tags', method: 'GET' });
    if (res.code === 200) allTags.value = res.data;
  } catch (_) {}
};

const fetchMyTags = async () => {
  // My tags are included in companion detail; use companion info
  try {
    const res = await request({ url: '/companion/apply/status', method: 'GET' });
    if (res.code === 200 && res.data?.tags) {
      selectedTagIds.value = res.data.tags.map((t: any) => t.id);
    }
  } catch (_) {}
};

const addPhoto = () => {
  uni.chooseImage({
    count: 1,
    success: async (res: any) => {
      uni.showLoading({ title: '上传中...' });
      try {
        const uploadRes = await uni.uploadFile({
          url: 'http://127.0.0.1:8080/api/file/upload',
          filePath: res.tempFilePaths[0],
          name: 'file'
        });
        const data = JSON.parse(uploadRes.data);
        if (data.code === 200) {
          await request({ url: '/companion/albums', method: 'POST', data: { imageUrl: data.data } });
          fetchAlbums();
          uni.showToast({ title: '添加成功', icon: 'success' });
        }
      } catch (_) {
        uni.showToast({ title: '上传失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    }
  });
};

const setCover = async (id: number) => {
  await request({ url: `/companion/albums/${id}/cover`, method: 'PUT' });
  fetchAlbums();
  uni.showToast({ title: '已设封面', icon: 'success' });
};

const deletePhoto = (id: number) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这张照片吗？',
    success: async (r: any) => {
      if (r.confirm) {
        await request({ url: `/companion/albums/${id}`, method: 'DELETE' });
        fetchAlbums();
      }
    }
  });
};

const editSkill = (skill: any) => {
  const idx = categories.value.findIndex((c: any) => c.id === skill.categoryId);
  skillForm.value = { categoryIndex: idx >= 0 ? idx : 0, categoryId: skill.categoryId, price: String(skill.pricePerHour), desc: skill.experienceDesc || '' };
  editingSkill.value = skill;
};

const deleteSkill = (id: number) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个技能吗？',
    success: async (r: any) => {
      if (r.confirm) {
        await request({ url: `/companion/skills/${id}`, method: 'DELETE' });
        fetchSkills();
      }
    }
  });
};

const onCategoryChange = (e: any) => {
  const idx = e.detail.value;
  skillForm.value.categoryIndex = idx;
  skillForm.value.categoryId = categories.value[idx]?.id;
};

const closeSkillModal = () => {
  showAddSkill.value = false;
  editingSkill.value = null;
  skillForm.value = { categoryIndex: 0, categoryId: 0, price: '', desc: '' };
};

const saveSkill = async () => {
  if (!skillForm.value.price || Number(skillForm.value.price) <= 0) {
    uni.showToast({ title: '请输入有效价格', icon: 'none' }); return;
  }
  if (editingSkill.value) {
    await request({ url: `/companion/skills/${editingSkill.value.id}`, method: 'PUT', data: { pricePerHour: Number(skillForm.value.price), experienceDesc: skillForm.value.desc } });
  } else {
    const catId = categories.value[skillForm.value.categoryIndex]?.id;
    if (!catId) { uni.showToast({ title: '请选择品类', icon: 'none' }); return; }
    await request({ url: '/companion/skills', method: 'POST', data: { categoryId: catId, pricePerHour: Number(skillForm.value.price), experienceDesc: skillForm.value.desc } });
  }
  closeSkillModal();
  fetchSkills();
  uni.showToast({ title: '保存成功', icon: 'success' });
};

const toggleTag = (id: number) => {
  const idx = selectedTagIds.value.indexOf(id);
  if (idx >= 0) selectedTagIds.value.splice(idx, 1);
  else selectedTagIds.value.push(id);
};

const saveTags = async () => {
  await request({ url: '/companion/tags', method: 'PUT', data: { tagIds: selectedTagIds.value } });
  uni.showToast({ title: '标签已保存', icon: 'success' });
};

const previewImage = (url: string) => {
  uni.previewImage({ urls: albums.value.map((a: any) => a.imageUrl), current: url });
};
</script>

<style lang="scss" scoped>
.container { min-height: 100vh; background-color: $bg-color-page; }
.tabs { display: flex; background: $bg-color-white; border-bottom: 1px solid $border-color-light; }
.tab { flex: 1; text-align: center; padding: 28rpx 0; font-size: $font-size-base; color: $text-color-regular; }
.tab.active { color: $color-primary; font-weight: bold; border-bottom: 4rpx solid $color-primary; }

.tab-content { padding: 24rpx; }
.album-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; }
.album-item { aspect-ratio: 1; border-radius: $border-radius-md; overflow: hidden; position: relative; background: $bg-color-page; }
.album-item image { width: 100%; height: 100%; }
.photo-actions { position: absolute; bottom: 0; left: 0; right: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: space-around; padding: 8rpx 0; }
.cover-badge { color: $color-accent; font-size: 20rpx; }
.set-cover, .delete { color: #fff; font-size: 20rpx; }
.add-btn { display: flex; flex-direction: column; align-items: center; justify-content: center; border: 2rpx dashed $border-color-light; }
.plus { font-size: 60rpx; color: $text-color-secondary; }
.add-text { font-size: 22rpx; color: $text-color-secondary; margin-top: 8rpx; }

.skill-card { background: $bg-color-white; border-radius: $border-radius-md; padding: 24rpx; margin-bottom: 16rpx; display: flex; justify-content: space-between; align-items: center; }
.skill-name { font-weight: bold; color: $text-color-primary; }
.skill-price { color: $color-primary; font-weight: bold; font-family: 'Outfit', sans-serif; margin-left: 16rpx; }
.skill-desc { font-size: 22rpx; color: $text-color-secondary; display: block; margin-top: 8rpx; }
.skill-actions .edit { color: $color-primary; margin-right: 20rpx; }
.skill-actions .delete { color: #EF4444; }
.add-skill-btn { margin-top: 20rpx; background: $gradient-primary; color: #fff; border-radius: $border-radius-pill; border: none; }

.tag-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag-item { padding: 14rpx 28rpx; border-radius: $border-radius-pill; background: $bg-color-page; color: $text-color-regular; font-size: 26rpx; border: 1px solid $border-color-light; }
.tag-item.selected { background: rgba(255,59,92,0.1); color: $color-primary; border-color: $color-primary; }
.save-tags-btn { margin-top: 40rpx; background: $gradient-primary; color: #fff; border-radius: $border-radius-pill; border: none; }

.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal { width: 80%; background: $bg-color-white; border-radius: $border-radius-lg; padding: 40rpx; }
.modal-title { text-align: center; font-weight: bold; font-size: 32rpx; display: block; margin-bottom: 30rpx; }
.form-item { margin-bottom: 24rpx; }
.form-item .label { display: block; font-size: $font-size-sm; color: $text-color-regular; margin-bottom: 12rpx; }
.form-item input, .form-item .value { width: 100%; height: 80rpx; background: $bg-color-page; border-radius: $border-radius-md; padding: 0 20rpx; box-sizing: border-box; }
.modal-btns { display: flex; gap: 20rpx; margin-top: 30rpx; }
.modal-btns .btn { flex: 1; height: 76rpx; line-height: 76rpx; border-radius: $border-radius-pill; border: none; font-size: $font-size-base; }
.modal-btns .cancel { background: $bg-color-page; color: $text-color-regular; }
.modal-btns .confirm { background: $gradient-primary; color: #fff; }
</style>
