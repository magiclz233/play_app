# 同城伴玩平台 - 前端 UI/UX 视觉设计设计系统指南 (暖杏落日风)

本设计指南作为前端开发与样式重构的唯一依据。系统采用**“暖杏落日风 (Warm Oat & Sunset Coral)”**，兼顾社交娱乐的青春朝气与高奢设计的质感。系统全面支持**自适应系统明暗色模式切换**。

---

## 🎨 一、 设计系统基础令牌 (Design Tokens)

我们将样式基础属性抽象为 CSS 变量 (Custom Properties)，以支持明暗色无缝重绘。所有自定义组件与第三方 UI 库的主题色必须继承这些变量。

### 1. 颜色变量 (Color Custom Properties)

在全局 `uni.scss` 或根组件样式中注册以下变量：

```css
/* ==========================================================================
   自适应明暗色主题变量定义
   ========================================================================== */

/* 1. 浅色模式 - 暖杏燕麦色系 (Light Theme) */
.theme-light {
  --bg-main: #FAF6F0;           /* 柔和暖杏燕麦白：大背景底色 */
  --bg-card: #FFFDFB;           /* 润白乳脂色：卡片/弹窗背景 */
  --border-color: rgba(26, 22, 19, 0.05); /* 卡片与输入框细边框 */
  
  --text-primary: #1A1613;      /* 深咖啡黑：标题与正文主要文字 */
  --text-secondary: #8E8279;    /* 暖褐灰：副标题与辅助说明文字 */
  --text-muted: #BDB2AA;        /* 麦芽浅灰：禁用状态与占位文字 */
  
  --shadow-card: 0 8px 30px rgba(26, 22, 19, 0.03); /* 温和软阴影 */
  --shadow-floating: 0 12px 40px rgba(255, 91, 113, 0.08); /* 悬浮按钮阴影 */
}

/* 2. 深色模式 - 意式特浓黑系 (Dark Theme - 默认) */
.theme-dark {
  --bg-main: #1A1613;           /* 温暖意式浓缩黑：大背景底色 */
  --bg-card: #221E1A;           /* 暗香槟金黑：卡片/弹窗背景 */
  --border-color: rgba(255, 255, 255, 0.04); /* 极细微光边框 */
  
  --text-primary: #F5EFEB;      /* 象牙暖白：标题与正文主要文字 */
  --text-secondary: #A19791;    /* 麦芽沙灰：副标题与辅助说明文字 */
  --text-muted: #625852;        /* 烟熏深褐灰：禁用状态与占位文字 */
  
  --shadow-card: 0 8px 32px rgba(0, 0, 0, 0.4); /* 深色三维阴影 */
  --shadow-floating: 0 12px 40px rgba(255, 91, 113, 0.15); /* 深色悬浮按钮阴影 */
}

/* 3. 全局共享色彩变量 (明暗通用) */
:root {
  --color-primary: #FF5B71;     /* 品牌主色：落日珊瑚红 */
  --color-primary-rgb: 255, 91, 113;
  --color-primary-hover: #E03E54;
  --color-primary-light: #FFECEF; /* 浅珊瑚红背景（标签用） */
  
  --color-accent: #F59E0B;      /* 强调色：暖琥珀金 (用于排行榜第一、皇冠、高亮) */
  --color-success: #10B981;     /* 成功/在线状态：翡翠绿 */
  --color-error: #EF4444;       /* 异常/驳回状态：警示红 */
  
  /* 圆角与边距规范 */
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-full: 9999px;
  
  --space-xs: 8rpx;
  --space-sm: 16rpx;
  --space-md: 24rpx;
  --space-lg: 32rpx;
  --space-xl: 48rpx;
}
```

### 2. 字体与字号规范 (Typography)

* **英文字体与数字**：`Outfit`, `Inter`, `-apple-system`, sans-serif。在价格、倒计时与评分上使用 `Outfit` 加粗，提升现代潮流感。
* **中文字体**：等线、Microsoft YaHei、系统默认无衬线字体。
* **字号梯度**：
  * 大标题（页面首标题 / 助教姓名）：`36rpx` (Bold)
  * 二级标题（模块名称 / 卡片标题）：`30rpx` (Medium)
  * 正文一（常规说明 / 标签文字）：`26rpx` (Regular)
  * 正文二（小字辅助 / 时间 / 评价）：`22rpx` (Light)

---

## 📱 二、 核心页面视觉与布局规范 (Page Specs)

### 1. 首页 / 助教大厅 (pages/index/index.vue)
* **大背景**：应用全局背景色 `var(--bg-main)`。
* **分类导航栏**：
  * 平铺或横向滑动列表，背景透明。
  * 选中的分类：文字颜色为 `var(--color-primary)`，字重加粗，底端展示一条宽 `32rpx`、高 `6rpx`、圆角 `full` 的 `var(--color-primary)` 装饰线。
  * 未选中分类：文字颜色为 `var(--text-secondary)`。
* **人气排行榜 (魅力 Top 3)**：
  * 横向卡片容器，使用 `var(--bg-card)`，圆角 `var(--radius-lg)`。
  * 冠亚季军特殊装饰：
    * 第一名：头像框为 `var(--color-accent)` 渐变，顶部加浮空小金冠，大字标明“人气王”。
    * 第二、三名：分别用银色、铜色细环勾勒头像框。
  * 卡片内部包含：高清头像、昵称、每小时价格（价格字体使用 `Outfit`）、特长标签（如：技术流、声音甜）。
* **推荐助教列表**：
  * 瀑布流（两列）布局。卡片采用 `var(--bg-card)`，四边加 1px 细线边框 `var(--border-color)`，无毛糙阴影。
  * 包含**微型语音气泡**：一个高为 `48rpx` 的胶囊状语音播放条，底色为 `var(--color-primary-light)`，带有红色小喇叭和时长（如 `12"`），点击可直接试听。

### 2. 助教详情页 (pages/companion/detail.vue)
* **顶部风采相册**：
  * 轮播图 Swiper 高度占比 `45vh`，底部使用弧形切角或半透明高斯模糊面板显示页码。
* **语音自我介绍卡片**：
  * 页面中段的大卡片，底色为 `var(--bg-card)`，中间是渐变的声波跳动波形图（暗色模式下为紫粉渐变，亮色模式下为纯雅致靛蓝）。
  * 放置一个精美的圆形播放/暂停控制按钮（背景为 `var(--color-primary)`，白色播放图标）。
* **详细属性格栅**：
  * 使用 4 列网格展示：身高（如 `168cm`）、体重（如 `48kg`）、星座、所在区域（如 `朝阳区`），统一使用 `var(--text-secondary)`。
* **底部悬停预约栏**：
  * 常驻底部（`position: fixed; bottom: 0;`），背景采用高斯模糊磨砂玻璃效果（`backdrop-filter: blur(10px)`）。
  * 左侧放置“客服”快捷按键（`open-type="contact"`，唤起企业微信客服）。
  * 右侧为宽度 `70%` 的“立即预约下单”按钮，背景色 `var(--color-primary)`，白色文字，圆角 `var(--radius-full)`，带微弱发光阴影。

### 3. 需求大厅 (pages/request/list.vue)
* **需求快讯卡片**：
  * 背景 `var(--bg-card)`，上沿显示发布人昵称（隐藏中间字符如 `张*三`）与发布时间。
  * 主体文字以大字号显示需求详情（例如：“台球打子，今晚8点，需要一位技术好的小姐姐拉群沟通时间”）。
  * 右下角配备“我要应约”按钮，点击后拉起企业微信客服并携带该需求卡片。

---

## 🛠️ 三、 主流 UI 框架集成与主题定制

为了免去手写大量 CSS，本设计系统可以完美映射到 uni-app 的第三方组件库。

### 1. 映射 uview-plus / uView
在 `uni.scss` 引入 uview-plus 样式之前，重定义以下全局变量：
```scss
$u-primary: #FF5B71;          /* 替换为落日珊瑚红 */
$u-primary-dark: #E03E54;
$u-primary-light: #FFECEF;
$u-bg-color: var(--bg-main);   /* 替换为自适应背景色 */
$u-card-bg-color: var(--bg-card);
```

在使用 `u-button`、`u-card`、`u-tag` 时，直接指定类型为 `primary`，圆角设置为 `16`：
```html
<u-card :border="false" :style="{ backgroundColor: 'var(--bg-card)', borderRadius: 'var(--radius-lg)' }">
  <!-- 内部组件自动继承 CSS 变量 -->
</u-card>
```

### 2. 映射 uni-ui (官方默认组件库)
如果采用 `uni-ui`，在 `uni.scss` 中覆盖全局预定义变量：
```scss
$uni-color-primary: #FF5B71;
$uni-bg-color-grey: var(--bg-main);
$uni-border-radius-lg: var(--radius-lg);
```

---

## ⚙️ 四、 微信小程序明暗模式自适应开发实现

前端需要自动监听系统的明暗主题设置，并在用户界面中瞬时重绘。

### 1. App.vue 主题配置监听
在 `App.vue` 的 `onLaunch` 与 `onShow` 中获取微信客户端的主题配置：

```typescript
// App.vue
export default {
  onLaunch: function() {
    this.updateTheme();
    // 监听系统主题变化
    uni.onThemeChange((res) => {
      this.setThemeClass(res.theme);
    });
  },
  methods: {
    updateTheme() {
      // 获取当前系统明暗主题
      const systemInfo = uni.getSystemInfoSync();
      const theme = systemInfo.theme || 'dark'; // 默认采用深色模式
      this.setThemeClass(theme);
    },
    setThemeClass(theme: 'light' | 'dark') {
      // 将主题类名存入全局状态或直接挂载至页面容器上
      uni.$emit('theme-change', theme);
      // 可通过 Pinia 存入 store 状态
    }
  }
}
```

### 2. 页面根节点变量绑定
在每个 Vue 页面文件的最外层容器中绑定主题 Class：

```html
<!-- pages/index/index.vue -->
<template>
  <view :class="['page-container', themeClass]">
    <view class="lobby-card">
      <text class="title">推荐助教</text>
    </view>
  </view>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';

const themeClass = ref('theme-dark'); // 默认深色系

const handleThemeChange = (theme: 'light' | 'dark') => {
  themeClass.value = `theme-${theme}`;
};

onMounted(() => {
  // 初始化获取当前主题
  const info = uni.getSystemInfoSync();
  themeClass.value = `theme-${info.theme || 'dark'}`;
  uni.$on('theme-change', handleThemeChange);
});

onUnmounted(() => {
  uni.$off('theme-change', handleThemeChange);
});
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-primary);
  padding: var(--space-md);
  transition: background-color 0.3s ease, color 0.3s ease;
}

.lobby-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-md);
}
</style>
```
