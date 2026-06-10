# Design System Master File — 同城伴玩

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** 同城伴玩（助教搭子私域担保平台）
**Style:** 暖杏落日风 (Warm Oat & Sunset Coral)
**Platform:** 微信小程序 (uni-app Vue 3)
**Updated:** 2026-06-10

---

## Global Rules

### Color Palette

| Role | Light | Dark | CSS Variable |
|------|-------|------|--------------|
| 品牌主色 | `#FF3B5C` | `#FF5C73` | `--color-primary` |
| 渐变终点 | `#FF7B54` | `#FF8B62` | `--color-gradient-end` |
| 强调色 | `#FF9F1C` | `#FFB347` | `--color-accent` |
| 页面背景 | `#FAF5F5` | `#121216` | `--bg-main` |
| 卡片背景 | `#FFFFFF` | `#24262D` | `--bg-card` |
| 浮层背景 | `#FFFFFF` | `#3D3F47` | `--bg-elevated` |
| 微妙背景 | `#FFF0F2` | `rgba(255,59,92,0.06)` | `--bg-subtle` |
| 主要文字 | `#1A1A1F` | `#E2E4E9` | `--text-primary` |
| 次要文字 | `#5E6066` | `#A1A5B1` | `--text-secondary` |
| 占位文字 | `#A1A5B1` | `#737887` | `--text-muted` |
| 成功色 | `#10B981` | `#34D399` | `--color-success` |
| 警告色 | `#FF9F1C` | `#FFB347` | `--color-warning` |
| 错误色 | `#EF4444` | `#F87171` | `--color-error` |
| 信息色 | `#3B82F6` | `#60A5FA` | `--color-info` |

**Color Rules:**
- 亮色模式品牌色基准为 coral-500，暗色模式自动提亮到 coral-400
- 状态色在暗色模式均提亮 1 级
- 阴影在暗色模式加深背景阴影 + 保留品牌辉光
- 所有文字对比度 ≥ 4.5:1 (WCAG AA)

### Typography

- **系统字体栈:** `-apple-system, BlinkMacSystemFont, 'Helvetica Neue', 'PingFang SC', 'Microsoft Yahei', sans-serif`
- **数字/价格字体:** 系统等宽数字（bold + `font-variant-numeric: tabular-nums`）
- **字号梯度:** `--font-size-xs(20rpx)` `sm(24rpx)` `base(28rpx)` `lg(32rpx)` `xl(36rpx)` `xxl(48rpx)`
- **正文行高:** 1.5-1.6
- **标题行高:** 1.2-1.4

### Spacing

| Token | Value (rpx) | Usage |
|-------|-------------|-------|
| `--space-xs` | 8 | 图标与文字间隙 |
| `--space-sm` | 16 | 内边距 |
| `--space-md` | 24 | 标准间距 |
| `--space-lg` | 32 | 段落/区块间距 |
| `--space-xl` | 48 | 大区块间距 |

### Border Radius

| Token | Value (rpx) | Usage |
|-------|-------------|-------|
| `--radius-sm` | 8 | 标签、小徽章 |
| `--radius-md` | 16 | 按钮、输入框 |
| `--radius-lg` | 24 | 卡片、弹窗 |
| `--radius-xl` | 32 | 大卡片 |
| `--radius-full` | 999 | 胶囊按钮、药丸标签 |

### Shadow Depths

| Token | Value | Usage |
|-------|-------|-------|
| `--shadow-card` | `0 8px 30px rgba(0,0,0,0.04), 0 4px 16px rgba(255,59,92,0.04)` | 卡片悬浮 |
| `--shadow-floating` | `0 12px 40px rgba(255,59,92,0.12)` | FAB、弹窗 |
| `--shadow-glow` | `0 0 20px rgba(255,59,92,0.15)` | 品牌辉光 |

### z-index Scale

| SCSS Variable | Value | Usage |
|---------------|-------|-------|
| `$z-index-dropdown` | 10 | 下拉菜单 |
| `$z-index-sticky` | 100 | 悬浮按钮、固定底栏、吸顶导航 |
| `$z-index-fixed` | 200 | 固定定位元素 |
| `$z-index-modal-backdrop` | 500 | 弹窗遮罩 |
| `$z-index-modal` | 1000 | 弹窗/对话框 |
| `$z-index-toast` | 2000 | Toast 提示 |

---

## Component Specs

### Buttons

```css
/* 主按钮 — 珊瑚渐变 + 辉光阴影 */
button[type="primary"]:not([plain]) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-gradient-end));
  box-shadow: 0 6px 16px rgba(var(--color-primary-rgb), 0.3);
  border-radius: var(--radius-full);
  color: #fff;
}

/* 描边按钮 */
button[type="primary"][plain] {
  background: var(--color-primary-light);
  border: 1px solid var(--color-primary);
  color: var(--color-primary);
  border-radius: var(--radius-full);
}

/* 按钮交互 */
button:active { transform: scale(0.97); }
button[disabled] { opacity: 0.5; transform: none; }
```

### Cards

```css
/* 卡片 — 表面微反光 + 内顶部高光 + 品牌色弥散阴影 */
.card {
  background-color: var(--bg-card);
  background-image: linear-gradient(145deg, rgba(255,255,255,0.03), transparent);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: inset 0 1px 1px rgba(255,255,255,0.06), var(--shadow-card);
}

/* 卡片触摸反馈 */
.card-hover {
  opacity: 0.85;
  transform: scale(0.98);
  transition: opacity 0.15s ease, transform 0.15s ease;
}
```

### Inputs

```css
.input {
  background: var(--bg-main);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16rpx 24rpx;
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.input::placeholder { color: var(--text-muted); }
.input:focus { border-color: var(--border-strong); }
```

### Modals

```css
.modal-overlay {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: $z-index-modal-backdrop;
}

.modal {
  background: var(--bg-elevated);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-floating);
  z-index: $z-index-modal;
  width: 80%;
}
```

---

## Anti-Patterns (DO NOT USE)

### ❌ Forbidden

- ❌ **硬编码颜色:** `color: #FF3B5C` / `background: #fff` → 必须用 `var(--xxx)`
- ❌ **Emoji 图标:** ♂♀★☆🎨🚀 → 必须用内联 SVG (viewBox 0 0 24 24)
- ❌ **裸 z-index:** `z-index: 99` / `z-index: 999` → 必须用 SCSS 变量
- ❌ **动画 width/height:** `@keyframes { height: 10rpx }` → 必须用 `transform: scaleY()`
- ❌ **outline: none 无替代:** 移除聚焦环必须提供替代方案
- ❌ **透明度过低的玻璃卡片:** 亮色下 `bg-white/10` → 最低 `bg-white/80`
- ❌ **placeholder 作为唯一标签:** → 必须有关联的 `<label>` 或 `aria-label`

### ✅ Required

- ✅ 所有 `<image>` 添加 `lazy-load`（首屏除外）
- ✅ 所有可点击元素添加 `hover-class` 或 `:active`
- ✅ 图标按钮添加 `aria-label`
- ✅ 数字输入框添加 `inputmode="numeric"` / `"decimal"`
- ✅ `line-height` 显式设为 1.5-1.6
- ✅ 触摸目标最小 88rpx (44px)
- ✅ 所有颜色通过 CSS 变量引用
- ✅ `prefers-reduced-motion` 受尊重

---

## Pre-Delivery Checklist

Before delivering any UI code, verify:

- [ ] No emojis as icons (SVG instead)
- [ ] All colors via `var(--xxx)` CSS variables
- [ ] `hover-class` or `:active` on all clickable elements
- [ ] Hover/active transitions 150-300ms
- [ ] Light mode text contrast ≥ 4.5:1
- [ ] Focus states visible for keyboard nav
- [ ] `prefers-reduced-motion` respected
- [ ] z-index values use SCSS variables
- [ ] Images have `lazy-load` (below-fold)
- [ ] `aria-label` on icon-only buttons
- [ ] `inputmode` on numeric inputs
- [ ] `line-height` explicitly set (1.5-1.6)
- [ ] Touch targets ≥ 88rpx
- [ ] No `outline: none` without replacement
- [ ] No content hidden behind fixed bars
