# 同城伴玩平台 - 前端 UI/UX 视觉设计设计系统指南 (暖杏落日风)

本设计指南作为前端开发与样式重构的唯一依据。系统采用**“暖杏落日风 (Warm Oat & Sunset Coral)”**，兼顾社交娱乐的青春朝气与高奢设计的质感。系统全面支持**自适应系统明暗色模式切换**。

---

## 🎨 一、 设计系统基础令牌 (Design Tokens)

我们将样式基础属性抽象为 CSS 变量 (Custom Properties)，以支持明暗色无缝重绘。所有自定义组件与第三方 UI 库的主题色必须继承这些变量。

### 1. 颜色变量 (Color Custom Properties)

应用的核心 UI 色彩表现通过全局唯一的配置文件 `src/theme/tokens.ts` 进行管理。所有组件应直接使用 CSS 变量（如 `var(--bg-main)`）。以下是当前采用的**自适应明暗色主题变量**：

#### 浅色模式 - 晨露粉杏与流光珊瑚 (Light Theme)
*   **--bg-main**: `#FAF5F5` (柔和暖杏白：大背景底色)
*   **--bg-card**: `#FFFFFF` (纯净润白：卡片/弹窗背景)
*   **--border-color**: `rgba(255, 59, 92, 0.08)` (微红细边框)
*   **--text-primary**: `#1A1A1F` (深墨黑：主要文字)
*   **--text-secondary**: `#5E6066` (青灰：副标题)
*   **--text-muted**: `#999A9F` (浅灰：占位符)
*   **--shadow-floating**: `0 12px 40px rgba(255, 59, 92, 0.12)` (悬浮按钮柔和阴影)

#### 深色模式 - 深邃炭灰与霓虹珊瑚 (Dark Theme)
*   **--bg-main**: `#18181C` (深邃炭灰护眼色：大背景底色)
*   **--bg-card**: `#202127` (高级暗灰：卡片/弹窗背景)
*   **--border-color**: `rgba(255, 255, 255, 0.06)` (极细微光白边框)
*   **--text-primary**: `#E2E4E9` (柔和灰白：主要文字)
*   **--text-secondary**: `#A1A5B1` (中度灰：副标题)
*   **--text-muted**: `#737887` (深灰：占位符)
*   **--shadow-floating**: `0 12px 32px rgba(255, 59, 92, 0.12)` (微弱的霓虹珊瑚红发光阴影)

#### 全局共享色彩变量 (明暗通用)
*   **--color-primary**: `#FF3B5C` (品牌主色：流光珊瑚红)
*   **--color-primary-light**: 浅色下为 `#FFF0F2`，深色下为深克莱因红 `#31171E` (按钮、标签底色)
*   **--color-gradient-end**: `#FF7B54` (主渐变色终点：日落橘)
*   **--color-accent**: `#FF9F1C` (强调色：暖琥珀金，用于皇冠、高亮)
*   **--color-success**: `#10B981` (状态色：在线翡翠绿)

*请注意：主按钮 `button[type="primary"]` 已在 `App.vue` 中配置了全局渐变 `linear-gradient(135deg, var(--color-primary), var(--color-gradient-end))` 与发光阴影效果。*

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

### 0. 全局卡片华贵质感提升 (Premium Component Styling)
为了在不依赖大量 DOM 结构和繁重 CSS 的前提下提升整体华贵感，所有卡片级组件（所有带有 `card` 类名或 `u-card` 的 `view`）默认生效以下全局样式（配置于 `App.vue`）：
*   **表面材质微反光 (Surface Reflection)**：通过 `linear-gradient` 附加一层极弱（3%）的自左上而下的表面渐变光，打破纯色的沉闷感。
*   **玻璃切割边缘 (Beveled Edge Highlight)**：利用 `box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.08)` 在卡片内侧顶部打上一层白色高光，模拟玻璃或高级亚克力的边缘切割反光。
*   **色彩弥散阴影 (Tinted Drop Shadow)**：卡片的外阴影 `var(--shadow-card)` 中融合了微弱的主色调（流光珊瑚红）辉光，取代了传统的脏黑色阴影，带来极为高级的悬浮发光质感。

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

当前项目采用了更加现代和高效的 **Pinia 状态管理 + 原生 API 注入** 架构，不再需要在每个页面手动绑定 `class`。

### 1. 全局配置与状态流转 (`src/store/app.ts`)
我们通过 `appStore` 来管理当前主题，并在获取系统主题后，通过 `applyThemeToNativeUi` 自动将 `tokens.ts` 中的变量挂载到小程序的原生 UI (NavigationBar, TabBar) 和 全局 `<page>` 标签上。

### 2. App.vue 统一接管
在 `App.vue` 的 `onLaunch` 与 `onShow` 生命周期中调用 Store，完成无感知的自适应重绘：

```typescript
// App.vue
<script setup lang="ts">
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { useAppStore } from "./store/app";

onLaunch(() => {
  const appStore = useAppStore();
  appStore.initApp(); // 内部会监听 uni.onThemeChange 并触发更新
});

onShow(() => {
  const appStore = useAppStore();
  appStore.applyThemeToNativeUi();
});
</script>
```

### 3. 组件级色彩调用
由于 `applyThemeToNativeUi` 已经将 CSS 变量注入了原生层，所有 `.vue` 页面与组件的开发中，**无需手动判断环境**，直接使用变量即可。无论是暗色还是亮色，都会瞬间平滑过渡：

```css
.card {
  background-color: var(--bg-card); /* 自动响应明暗模式 */
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}
```
