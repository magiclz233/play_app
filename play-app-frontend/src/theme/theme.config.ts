/**
 * 品牌主题配置 (Theme Configuration)
 *
 * 定义可通过后端 API 或本地存储覆盖的品牌视觉配置。
 * 配置驱动主题引擎生成最终的 CSS 变量集。
 *
 * 优先级：后端 API 配置 > 本地存储覆盖 > 代码默认值
 */

import { primitives } from './primitives';

// ============================================================
// 类型定义
// ============================================================

/** 主题模式 */
export type ThemeName = 'light' | 'dark';
export type ThemeMode = 'auto' | ThemeName;

/** 圆角策略 */
export type RadiusScale = 'rounded' | 'smooth' | 'pill';

/** 间距策略 */
export type SpacingScale = 'compact' | 'comfortable' | 'spacious';

/** 字体缩放级别 */
export type FontScaleLevel = 1 | 1.15 | 1.3;

/** 品牌色配置（可被后端覆盖） */
export interface BrandConfig {
  /** 品牌主色 HEX */
  primaryColor: string;
  /** 强调色 HEX */
  accentColor: string;
  /** 渐变终点色 HEX */
  gradientEndColor: string;
}

/** 完整主题配置 */
export interface ThemeConfig {
  /** 品牌色 */
  brand: BrandConfig;
  /** 圆角策略 */
  radius: {
    scale: RadiusScale;
  };
  /** 间距策略 */
  spacing: {
    scale: SpacingScale;
  };
  /** 字体缩放（无障碍大字体） */
  typography: {
    scale: FontScaleLevel;
  };
}

// ============================================================
// 默认配置
// ============================================================

export const defaultBrandConfig: BrandConfig = {
  primaryColor: primitives.coral[500],     // '#FF3B5C'
  accentColor: primitives.amber[500],      // '#FF9F1C'
  gradientEndColor: primitives.sunset[500], // '#FF7B54'
};

export const defaultThemeConfig: ThemeConfig = {
  brand: { ...defaultBrandConfig },
  radius: { scale: 'smooth' },
  spacing: { scale: 'comfortable' },
  typography: { scale: 1 },
};

// ============================================================
// 圆角/间距/字号的物理值映射
// ============================================================

/** 圆角基准值映射（rpx） */
export const RADIUS_BASE: Record<RadiusScale, number> = {
  rounded: 8,   // 小圆角
  smooth: 16,   // 中等圆角（默认）
  pill: 24,     // 大圆角
};

/** 圆角倍率映射 */
export const RADIUS_MULTIPLIERS = {
  sm: 0.5,
  md: 1.0,
  lg: 1.5,
  xl: 2.0,
  full: 999,    // 全圆角
} as const;

/** 间距基准值映射（rpx） */
export const SPACING_BASE: Record<SpacingScale, number> = {
  compact: 6,
  comfortable: 8,
  spacious: 12,
};

/** 间距倍率映射 */
export const SPACING_MULTIPLIERS = {
  xs: 1,
  sm: 2,
  md: 3,
  lg: 4,
  xl: 6,
} as const;

// ============================================================
// 配置合并工具
// ============================================================

/**
 * 深度合并两个 ThemeConfig，override 中的值会覆盖 base
 */
export function mergeThemeConfig(
  base: ThemeConfig,
  override: Partial<ThemeConfig>
): ThemeConfig {
  return {
    brand: { ...base.brand, ...override.brand },
    radius: { ...base.radius, ...override.radius },
    spacing: { ...base.spacing, ...override.spacing },
    typography: { ...base.typography, ...override.typography },
  };
}

/**
 * 从后端 API 响应映射到 ThemeConfig
 * 后端返回的字段可能不完整，需要与默认值合并
 */
export function mapRemoteConfig(raw: Record<string, any>): Partial<ThemeConfig> {
  const partial: Partial<ThemeConfig> = {};

  if (raw.brand) {
    partial.brand = {};
    if (raw.brand.primaryColor) partial.brand.primaryColor = raw.brand.primaryColor;
    if (raw.brand.accentColor) partial.brand.accentColor = raw.brand.accentColor;
    if (raw.brand.gradientEndColor) partial.brand.gradientEndColor = raw.brand.gradientEndColor;
  }

  if (raw.radius?.scale) {
    partial.radius = { scale: raw.radius.scale };
  }

  if (raw.spacing?.scale) {
    partial.spacing = { scale: raw.spacing.scale };
  }

  if (raw.typography?.scale) {
    partial.typography = { scale: raw.typography.scale };
  }

  return partial;
}
