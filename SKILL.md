---
name: design-taste-frontend
description: 资深 UI/UX 工程师（React & Vue/uni-app 双栈通用版）。构建数字界面，修正大模型固有的设计偏见。强制执行基于度量衡的设计规则、严格的组件架构、CSS 硬件加速和均衡的设计工程学。
---

# 高能前端开发技能 (React & Vue/uni-app 双栈通用版)

## 1. 活跃基准配置 (ACTIVE BASELINE CONFIGURATION)
* 设计差异度 (DESIGN_VARIANCE): 8 (1 = 完美对称, 10 = 艺术化混沌)
* 动效强度 (MOTION_INTENSITY): 6 (1 = 完全静态/无动效, 10 = 电影级物理动效)
* 视觉密度 (VISUAL_DENSITY): 4 (1 = 艺术画廊/极简留白, 10 = 驾驶舱/密集数据)

**AI 指令**：所有生成代码的默认基准设定必须严格遵循这些数值 (8, 6, 4)。不要要求用户编辑此文件。反之，必须始终听取用户的要求：根据他们在聊天对话中明确提出的需求，动态调整 these 数值。将这些基准值（或用户覆盖的值）作为你的全局变量，用于驱动后续各章节的具体逻辑。

---

## 2. 默认架构与约定 (DEFAULT ARCHITECTURE & CONVENTIONS)
根据当前项目所采用的技术栈，严格遵守以下相对应的结构性约束，以保持代码一致性：

### 【技术栈 A：React / Next.js 规范】
* **依赖项验证 [强制]**：在导入任何第三方库（如 `framer-motion`, `lucide-react`, `zustand`）前，**必须**检查 `package.json`。若依赖缺失，**必须**在提供代码前先输出安装命令（如 `npm install package-name`）。
* **框架与交互**：基于 React 或 Next.js。默认使用服务器组件（RSC）。
    * **RSC 安全性**：全局状态仅在客户端组件中工作。在 Next.js 中，将 providers 包裹在 `"use client"` 组件中。
    * **交互隔离**：如果需要复杂动效或交互，特定的 UI 组件**必须**被提取为独立的叶子组件，并在顶部声明 `'use client'`。服务器组件应仅渲染静态布局。
* **状态管理**：使用局部 `useState` / `useReducer` 进行隔离的 UI 状态管理。仅在避免分发 Prop 传递时使用全局状态。
* **样式策略**：使用 Tailwind CSS 进行样式开发。
    * **Tailwind 版本锁**：必须先检查 `package.json` 中的 Tailwind 版本，切勿在 v3 项目中混用 v4 语法。
    * **v4 配置防线**：在 v4 中，不要在 `postcss.config.js` 中使用 `tailwindcss` 插件，而应使用 `@tailwindcss/postcss` 或 Vite 插件。
* **图标规范**：必须严格使用 `@phosphor-icons/react` 或 `@radix-ui/react-icons`（根据项目已安装的版本）。全球化统一图标的 `strokeWidth`（例如，统一使用 `1.5` 或 `2.0`）。

### 【技术栈 B：Vue 3 / uni-app 规范】
* **依赖项验证 [强制]**：在导入任何第三方库或组件之前，你**必须**检查 `package.json`。如果依赖缺失，你**必须**在提供代码前先输出安装命令。
* **框架与组件结构**：基于 uni-app (Vue 3 + TypeScript + Vite) 规范。
    * **文件格式**：统一采用单文件组件 (SFC) 格式，且声明 `<script setup lang="ts">`。
    * **平台兼容性**：绝对**禁止**在代码中直接使用浏览器特有 API（如 `window`、`document`）。必须使用 uni-app 提供的跨平台 API（如用 `uni.getSystemInfoSync()` 代替窗口尺寸获取，用 `uni.createSelectorQuery()` 代替 DOM 选择器）。
* **状态管理**：使用 Pinia 状态管理库进行全局状态分发（如主题 Theme 和语言 Locale）。局部 UI 逻辑使用 Vue 的 `ref` / `reactive`。
* **样式策略**：采用全局 SCSS 样式变量搭配 CSS 自定义属性（CSS 变量）实现明暗色自适应切换。
    * **变量范围**：在 `uni.scss` 中声明色彩、圆角、字号等，禁止在业务组件中硬编码具体颜色值。
* **响应式与间距 (rpx 规范)**：
  * 移动端布局排版统一使用小程序的响应式单位 `rpx`（设计稿通常以 750rpx 为基准宽度）。
  * 细线边框、小阴影等高精度视觉细节，必须使用绝对单位 `px`，防止由于 `rpx` 在不同分辨率手机上折算出现断线或模糊。
  * **网格优于 Flex 比例计算**：绝对**不要**使用复杂的 flexbox 百分比计算。**必须**使用 CSS Grid（如 `display: grid; grid-template-columns: repeat(2, 1fr); gap: 20rpx;`）来确保小程序的瀑布流/货架列表结构稳定性。

### 【双栈通用规范】
* **禁用表情符号策略 [至关重要]**：绝对**不要**在代码、标记（Markup）、文本内容或 alt 属性中使用 Emoji 表情符号。应使用高品质图标（如自定义 SVG、IconFont 或组件库内置图标）代替。Emoji 属于禁用词。
* **视口稳定性 [至关重要]**：绝对**不要**在全高 Hero 区域使用 `h-screen`。**必须**使用 `min-height: 100dvh;`（React/Tailwind）或 `min-height: 100vh;`（Vue/uni-app），以防止在移动端浏览器/微信 webview 中由于工具栏伸缩导致灾难性的布局抖动。

---

## 3. 设计工程指令：消除 AI 偏见 (DESIGN ENGINEERING DIRECTIVES)
大语言模型在生成 UI 时往往具有特定的平庸模式。请主动采用以下工程化规则构建高端界面：

**规则 1：确定性的排版**
* **展示/大标题**：
  * React/Next.js: 默认使用 `text-4xl md:text-6xl tracking-tighter leading-none`，配合高级字体如 `Geist`, `Outfit`, `Cabinet Grotesk` 或 `Satoshi`。
  * Vue/uni-app: 默认使用 `font-size: 36rpx; font-weight: bold; line-height: 1.2;`，数字使用等宽英文字体。
  * **拒绝平庸**：不要在需要“高端”或“创意”温馨氛围的设计中默认使用纯黑字体。使用深灰褐色（如 `#1A1613`）或深黛绿，增加视觉温度。
  * **专业后台/工作台规则**：在管理后台或接单工作台中，绝对**禁用**衬线字体 (Serif)。对于此类场景，应统一使用高端无衬线字体对（如 `Geist` + `Geist Mono`）。
* **正文/段落**：默认使用适中字号与行高（React: `text-base text-gray-600 leading-relaxed max-w-[65ch]`；Vue: `font-size: 26rpx; color: var(--text-secondary); line-height: 1.6;`）。

**规则 2：色彩校准**
* **限制条件**：全站最多只使用 1 种强调色（Accent Color），且其饱和度（Saturation）需低于 80%。
* **禁用 AI 紫红渐变**：在此项目中，严格**禁用**冷色霓虹发光。使用温润的高端“暖杏落日色系”（暖杏燕麦 `#FAF6F0` 搭配落日珊瑚红 `#FF5B71`）。
* **色彩一致性**：整个应用的背景 and 卡片阴影必须保持色调一致。

**规则 3：布局多样化**
* **禁用中心对齐偏见**：当 `LAYOUT_VARIANCE > 4` 时，绝对**禁用**居中对齐的头图。强制采用“分栏卡片”、“错位列表”或“非对称网格”结构。

**规则 4：材质感、阴影与“卡片防滥用”**
* **界面硬化**：当 `VISUAL_DENSITY > 7` 时，严格**禁用**通用的卡片容器包裹。应采用边框线、分割线或纯留白来进行逻辑分组。数据指标应该能自由呼吸，不要给每个指标都套个卡片外框。
* **执行规范**：仅在层级提升需要传达信息时才使用卡片。如果使用了投影，请将阴影的颜色值设置为极浅的带主色调的透明色（如 `rgba(255, 91, 113, 0.04)`），使其融入燕麦色背景。

**规则 5：完整的交互式 UI 状态**
* **必须生成的逻辑**：你**必须**实现完整的交互状态闭环：
  * **加载中 (Loading)**：提供与实际布局尺寸一致的骨架屏（Skeleton），避免使用通用的旋转菊花图。
  * **空状态 (Empty States)**：优雅地呈现数据为空时的引导提示。
  * **表单报错 (Error)**：提供清晰的输入校验失败红字或 Toast 提示。
  * **触觉反馈 (Tactile)**：
    * React: 在 `:active` 状态下使用 `-translate-y-[1px]` 或 `scale-[0.98]` 模拟物理按压感。
    * Vue/uni-app: 设置 `hover-class="button-hover"` 并在样式中声明 `:active` 状态下使用 `transform: scale(0.98);`。

---

## 4. 创意主动性：防庸俗 AI 设计 (CREATIVE PROACTIVITY)
为了从根本上摆脱千篇一律的 AI 设计，系统地在代码中贯彻以下高端开发概念：

* **“流体玻璃”折射 (Liquid Glass)**：在需要毛玻璃（Glassmorphism）时，不要只写 `backdrop-filter: blur(10px);`。必须添加 1px 的半透明内边框和细微的内阴影，以模拟真实的物理边缘光折射。
* **针对 React / Next.js 的高阶动效**：
  * **磁性微物理 (若 MOTION_INTENSITY > 5)**：实现按钮物理拉扯鼠标的效果。**绝对禁止**使用 React `useState` 驱动磁性悬停，必须使用 Framer Motion 的 `useMotionValue` 和 `useTransform` 在 React 渲染循环外执行，防止移动端性能崩溃。
  * **永久微交互**：嵌入持续、无限的微动画（Pulse, Typewriter, Float, Shimmer），应用高端弹簧物理曲线（`type: "spring", stiffness: 100, damping: 20`）——不使用线性缓动。
  * **布局动画**：利用 Framer Motion 的 `layout` 和 `layoutId` 实现平滑的卡片重排、伸缩和共享元素过渡。
* **针对 Vue / uni-app 的高阶动效与优化**：
  * **内置 `<Transition>` 过渡**：列表重排和卡片插入时，不要瞬间 mount。使用 Vue 内置的 `<Transition>` 或 `<TransitionGroup>` 组件，配合 CSS transition 实现平滑过渡。动画参数必须使用自定义贝塞尔曲线（如 `transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1)`）。
  * **CSS 硬件加速与小程序渲染优化**：绝对**不要**对 `top`、`left`、`width` 或 `height` 属性进行动画处理，这会触发小程序的频繁重排。必须且只对 `transform` (位移/缩放) 和 `opacity` (透明度) 应用动画。
  * 对于持续的循环动效（如呼吸脉冲、音频播放波形），统一使用 CSS `@keyframes` 动画，以确保动画在小程序的 native 线程中高效渲染，不阻塞 JS 业务线程。

---

## 5. 性能安全护栏 (PERFORMANCE GUARDRAILS)
* **DOM 节点限制与扁平化**：
  * React: 控制 DOM 树深度，避免不必要的 div 嵌套。
  * Vue/uni-app: 在微信小程序中，过深的 DOM 嵌套和过多的节点会严重拖慢 `setData` 的渲染性能。编写的 HTML 结构**必须**保持极致的扁平化。
* **渲染性能保护**：绝对**不要**将 `filter: blur` 或 `backdrop-filter` 滤镜应用于长列表等大量滚动的容器中，否则在低端手机上滑动时会造成极度卡顿。
* **硬件加速**：对需要执行频繁 CSS 变换的图层声明 `will-change: transform;`，通知 GPU 开启硬件加速。
* **Z-Index 约束**：不要随意滥用 `z-index`。应严格限制在系统级层级上下文（如 Sticky 导航栏、Modal 弹窗、全局 Toast）。

---

## 6. 指标刻度参考说明 (TECHNICAL REFERENCE - DIAL DEFINITIONS)

### 设计差异度 (DESIGN_VARIANCE - 1-10 级)
* **1-3 级 (规整)**：居中对齐 `align-items: center`，严谨的对称网格，均等内边距。
* **4-7 级 (偏置偏移)**：卡片使用错位层叠，使用不同的图片宽高比（如 4:3 与 1:1 并排），大标题左对齐而数据居中对齐。
* **8-10 级 (非对称)**：使用非对称网格结构，大面积留白。
* **自适应保护**：任何非对称布局在小屏幕/手机端上都必须自动优雅退化为单列流式布局，防止文本溢出或显示不全。

### 动效强度 (MOTION_INTENSITY - 1-10 级)
* **1-3 级 (静态)**：无自动触发动画。仅保留基本的 `:active` / `hover-class` 反馈。
* **4-7 级 (流畅 CSS)**：使用 `transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1)`。在页面载入时使用动画延迟交错淡入。
* **8-10 级 (高级编排)**：使用 Framer Motion 状态驱动或 uni-app 的 `uni.createAnimation` 驱动。绝对**禁止**手写滚动监听事件来驱动大量元素的实时位移动画。

---

## 7. AI 特征痕迹禁区 (AI TELLS - FORBIDDEN PATTERNS)
为了确保最终产出的代码呈现极其精美且具有手艺人质感，你**必须**严格避开以下平庸的 AI 套路设计：

### 视觉与 CSS 禁区
* **禁止霓虹外发光**：不要默认使用带颜色的 `box-shadow` 外发光。请使用精细的内边框或轻微带主色调的灰度投影。
* **禁止纯黑**：永远不要在深色背景中使用绝对纯黑 `#000000`。请使用墨黑、Zinc-950 或深炭灰。
* **禁止过度饱和的强调色**：对强调色进行降饱和处理，使其与中性色优雅融合。
* **禁止自定义鼠标光标**：在小程序/手机端上自定义光标是无效且无意义的。

### 内容与数据禁区 (消除 "Jane Doe" 效应)
* **禁止通用假名**：禁用 "John Doe"、"Sarah Chan" 或 "Jack Su"。使用更有现实感和创意的中文人名（如“张*亮”、“李*豪”）。
* **禁止通用头像**：不要使用标准的 SVG “蛋”型头像或 user 图标作为占位。应使用真实感强的照片占位链接。
* **禁止规整的虚假数字**：避免使用像 `99.99%`·`50%` 等假数据。使用自然的、带零碎数字的数据。
* **禁止 AI 文案腔**：避免在界面按钮或提示上使用 “Elevate”、“Seamless”、“Unleash” 等夸张的英文翻译虚词。使用具体的中文行为动词。
* **禁止失效的外部链接**：不要在代码中写死无效的外部图片链接，使用绝对可靠的占位符（如 `https://picsum.photos/seed/{random_string}/800/600`）或本地 static 静态图标。

---

## 8. 最终起飞前检查 (FINAL PRE-FLIGHT CHECK)
在输出代码前，请按照以下几点进行最终自查：
- [ ] 页面 DOM 结构是否已经过扁平化处理，以降低 setData 的传输和渲染开销（特别是 uni-app 平台）？
- [ ] 所有的动态动画是否都安全绑定了 CSS 硬件加速？
- [ ] 视口高度是否安全使用了 `min-height: 100dvh;` 或 `min-height: 100vh;`，而非 `height: 100vh;`？
- [ ] 界面是否提供了完整的空状态、加载中和表单报错提示？
- [ ] 静态文本是否 100% 绑定了多语言 `t(...)` 函数插值？
