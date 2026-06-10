/**
 * 主题系统统一导出入口
 *
 * 使用方式：
 *   import { themeEngine, primitives, colorUtils } from '@/theme';
 *
 * 三层令牌：
 *   1. primitives.ts — 原始色板（不要直接用于组件）
 *   2. semantic.ts   — 语义令牌（亮/暗映射）
 *   3. components.ts — 组件令牌（按钮、卡片、输入框等）
 *
 * 核心引擎：
 *   engine.ts — 计算 + 合并 + 注入 CSS 变量
 *
 * 配置：
 *   theme.config.ts — 品牌色、圆角、间距、字体缩放
 */

// ── 原始色板 ──
export {
  primitives,
  getPrimitive,
  type PrimitiveScale,
  type HueName,
  type ScaleLevel,
} from './primitives';

// ── 语义令牌 ──
export {
  lightSemantic,
  darkSemantic,
  mergeBrandConfig,
  type SemanticTokens,
} from './semantic';

// ── 组件令牌 ──
export {
  componentTokens,
  flattenComponentTokens,
  type ComponentTokenSet,
} from './components';

// ── 主题引擎 ──
export {
  themeEngine,
  applyTheme,
  updateBrandConfig,
} from './engine';

// ── 品牌配置 ──
export {
  defaultThemeConfig,
  defaultBrandConfig,
  mergeThemeConfig,
  mapRemoteConfig,
  RADIUS_BASE,
  SPACING_BASE,
  type ThemeConfig,
  type BrandConfig,
  type ThemeName,
  type ThemeMode,
  type RadiusScale,
  type SpacingScale,
  type FontScaleLevel,
} from './theme.config';

// ── 颜色工具（作为命名空间导出，避免污染全局） ──
export * as colorUtils from './color-utils';
