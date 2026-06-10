# Design System Master File — 同城伴玩

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** 同城伴玩（助教搭子私域担保平台）
**Style:** 暖杏落日风 (Warm Oat & Sunset Coral)
**Platform:** 微信小程序 (uni-app Vue 3)
**Theme Engine:** `play-app-frontend/src/theme/engine.ts`
**Updated:** 2026-06-10

---

## Quick Reference: Must-Read Files

- **CLAUDE.md** — AI 每次对话自动加载，含完整 CSS 变量速查表 + 组件模板
- **docs/THEME_OPTIMIZATION_PLAN.md** — 主题系统架构详细文档
- **design.md** — 原始视觉设计指南

## Page-Level Overrides

| Page | Override File | Key Deviations |
|------|--------------|----------------|
| 助教详情 | `pages/companion-detail.md` | 全屏相册、底部固定预约栏、语音波形 |
| 订单流程 | `pages/order-flow.md` | 状态驱动 UI、支付倒计时、退款表单 |
| 管理后台 | `pages/admin-dashboard.md` | 数据表格、审核操作、批量驳回 |
| 钱包/提现 | `pages/wallet.md` | 金额展示、交易列表、提现弹窗 |

## Anti-Patterns (DO NOT USE)

- ❌ 硬编码颜色: `color: #FF3B5C` → 必须用 `var(--color-primary)`
- ❌ Emoji 图标: ♂♀★☆ → 必须用 SVG (viewBox 0 0 24 24)
- ❌ 裸 z-index: `z-index: 99` → 必须用 SCSS 变量
- ❌ 动画 height: `@keyframes { height: 10rpx }` → 必须用 `transform: scaleY()`
- ❌ background: #fff → 必须用 `var(--bg-card)`

## Pre-Delivery Checklist

- [ ] 所有颜色通过 `var(--xxx)` 引用
- [ ] 所有 `<image>` 有 `lazy-load`（首屏除外）
- [ ] 所有可点击元素有 `hover-class` 或 `:active`
- [ ] 图标按钮有 `aria-label`
- [ ] 数字输入有 `inputmode`
- [ ] `line-height` 显式 1.5-1.6
- [ ] z-index 使用 SCSS 变量
- [ ] `prefers-reduced-motion` 已尊重
