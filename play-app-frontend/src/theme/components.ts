/**
 * Layer 3: 组件令牌 (Component Tokens)
 *
 * 组件级 CSS 变量 — 默认继承语义令牌，允许组件库/页面单独覆盖。
 * 所有值均为 CSS 表达式，引用 `var(--xxx)` 变量。
 *
 * 用途：
 * - 在 engine.ts 中注入为 CSS 变量
 * - 组件可直接使用 `var(--btn-primary-bg)` 而非手写渐变公式
 */

export interface ComponentTokenSet {
  [component: string]: Record<string, string>;
}

/**
 * 全局组件令牌定义
 */
export const componentTokens: ComponentTokenSet = {
  // ── 按钮 (Button) ──
  button: {
    '--btn-primary-bg':
      'linear-gradient(135deg, var(--color-primary), var(--color-gradient-end))',
    '--btn-primary-shadow':
      '0 6px 16px rgba(var(--color-primary-rgb), 0.3)',
    '--btn-primary-hover-bg':
      'linear-gradient(135deg, var(--color-primary-dark), var(--color-gradient-end))',
    '--btn-primary-active-bg':
      'linear-gradient(135deg, var(--color-primary-dark), var(--color-primary-dark))',
    '--btn-outline-bg':
      'var(--color-primary-light)',
    '--btn-outline-border':
      'var(--color-primary)',
    '--btn-outline-color':
      'var(--color-primary)',
    '--btn-outline-shadow':
      '0 2px 8px rgba(var(--color-primary-rgb), 0.1)',
    '--btn-height':
      '80rpx',
    '--btn-radius':
      'var(--radius-full)',
    '--btn-font-size':
      'var(--font-size-base)',
  },

  // ── 卡片 (Card) ──
  card: {
    '--card-bg':
      'var(--bg-card)',
    '--card-surface-reflection':
      'linear-gradient(145deg, rgba(255, 255, 255, 0.03) 0%, transparent 100%)',
    '--card-border':
      '1px solid var(--border-color)',
    '--card-shadow':
      'var(--shadow-card)',
    '--card-inner-highlight':
      'inset 0 1px 1px rgba(255, 255, 255, 0.06)',
    '--card-radius':
      'var(--radius-lg)',
    '--card-padding':
      '24rpx',
  },

  // ── 输入框 (Input) ──
  input: {
    '--input-bg':
      'var(--bg-main)',
    '--input-border':
      '1px solid var(--border-color)',
    '--input-focus-border':
      '1px solid var(--border-strong)',
    '--input-placeholder':
      'var(--text-muted)',
    '--input-radius':
      'var(--radius-md)',
    '--input-padding':
      '16rpx 24rpx',
    '--input-font-size':
      'var(--font-size-sm)',
  },

  // ── 标签 (Tag/Badge) ──
  tag: {
    '--tag-bg':
      'var(--color-primary-light)',
    '--tag-color':
      'var(--color-primary)',
    '--tag-radius':
      'var(--radius-sm)',
    '--tag-padding':
      '2rpx 10rpx',
    '--tag-font-size':
      'var(--font-size-xs)',
  },

  // ── 底部导航栏 (TabBar) ──
  tabBar: {
    '--tabbar-color':
      'var(--text-muted)',
    '--tabbar-selected-color':
      'var(--color-primary)',
    '--tabbar-bg':
      'var(--bg-card)',
    '--tabbar-border':
      'var(--border-color)',
  },

  // ── 导航栏 (NavigationBar) ──
  navigationBar: {
    '--navbar-bg':
      'var(--bg-card)',
    '--navbar-text':
      'var(--text-primary)',
    '--navbar-border':
      'var(--border-color)',
  },

  // ── 骨架屏 (Skeleton) ──
  skeleton: {
    '--skeleton-bg':
      'linear-gradient(90deg, rgba(var(--color-primary-rgb), 0.05), rgba(255, 255, 255, 0.08), rgba(var(--color-primary-rgb), 0.05))',
    '--skeleton-animation':
      'skeleton-shimmer 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite',
  },

  // ── 弹窗/浮层 (Modal/Overlay) ──
  modal: {
    '--modal-overlay':
      'rgba(0, 0, 0, 0.6)',
    '--modal-bg':
      'var(--bg-elevated)',
    '--modal-shadow':
      'var(--shadow-floating)',
    '--modal-radius':
      'var(--radius-lg)',
  },

  // ── 分隔线 (Divider) ──
  divider: {
    '--divider-color':
      'var(--border-color)',
    '--divider-width':
      '1px',
  },

  // ── 浮动操作按钮 (FAB) ──
  fab: {
    '--fab-bg':
      'var(--color-primary)',
    '--fab-shadow':
      'var(--shadow-floating)',
    '--fab-radius':
      'var(--radius-full)',
  },
};

/**
 * 将所有组件令牌展平为 CSS 变量映射表
 * @returns Record<"--btn-primary-bg", "linear-gradient(...)">
 */
export function flattenComponentTokens(tokens: ComponentTokenSet): Record<string, string> {
  const flat: Record<string, string> = {};
  for (const group of Object.values(tokens)) {
    Object.assign(flat, group);
  }
  return flat;
}
