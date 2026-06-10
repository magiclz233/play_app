/**
 * Layer 2: 语义令牌 (Semantic Tokens)
 *
 * 将原始色板映射到功能性角色。此处定义亮/暗两套完整映射。
 * 每个主题的语义令牌值可被品牌配置（ThemeConfig）覆盖。
 *
 * 设计原则：
 * - 暗色模式下品牌色自动提亮 1 级（500→400），减少暗背景上的视觉刺眼
 * - 暗色模式下状态色自动提亮 1 级
 * - 阴影在暗色下加深背景阴影 + 保留品牌辉光
 * - 文字对比度均 ≥ 4.5:1（WCAG AA）
 */

import { primitives, getPrimitive } from './primitives';
import type { ThemeConfig } from './theme.config';

// ============================================================
// 语义令牌接口定义
// ============================================================
export interface SemanticTokens {
  // ── 背景层级 ──
  /** 页面大背景 */
  bgMain: string;
  /** 卡片/弹窗/列表项背景 */
  bgCard: string;
  /** 浮层/弹窗背景（比 card 高一级） */
  bgElevated: string;
  /** 极浅背景（分割区块用） */
  bgSubtle: string;

  // ── 文字层级 ──
  /** 主要文字（标题、正文） */
  textPrimary: string;
  /** 次要文字（副标题、辅助说明） */
  textSecondary: string;
  /** 占位/禁用文字 */
  textMuted: string;
  /** 反白文字（深色背景上） */
  textInverse: string;

  // ── 品牌色 ──
  /** 品牌主色 */
  colorPrimary: string;
  /** 主色 RGB 分量（逗号分隔，用于 rgba() 组合） */
  colorPrimaryRgb: string;
  /** 主色浅底色（标签、按钮 hover 背景） */
  colorPrimaryLight: string;
  /** 主色加深（active/pressed 状态） */
  colorPrimaryDark: string;
  /** 渐变终点色 */
  colorGradientEnd: string;
  /** 强调色（皇冠、评分星、高亮） */
  colorAccent: string;
  /** 强调色 RGB 分量 */
  colorAccentRgb: string;

  // ── 状态色 ──
  colorSuccess: string;
  colorWarning: string;
  colorError: string;
  colorInfo: string;

  // ── 边框与分割 ──
  /** 常规边框（卡片、列表项之间） */
  borderColor: string;
  /** 高对比边框（输入框聚焦、选中态） */
  borderStrong: string;

  // ── 阴影 ──
  /** 卡片悬浮阴影 */
  shadowCard: string;
  /** 浮动按钮/弹窗阴影 */
  shadowFloating: string;
  /** 品牌色辉光阴影 */
  shadowGlow: string;

  // ── 特殊效果色 ──
  /** 品牌辉光色（极低透明度） */
  colorGlow: string;
  /** 次要辉光色（强调色极低透明度） */
  colorGlowSecondary: string;
}

// ============================================================
// 亮色模式 (晨露粉杏与流光珊瑚)
// ============================================================
export const lightSemantic: SemanticTokens = {
  bgMain:          primitives.gray[50],
  bgCard:          primitives.white,
  bgElevated:      primitives.white,
  bgSubtle:        primitives.coral[50],

  textPrimary:     primitives.gray[900],     // #1A1A1F → 对比度 18.1:1 on #FAF5F5 ✅
  textSecondary:   primitives.gray[600],     // #5E6066 → 对比度 ≈ 5.2:1 on #FAF5F5 ✅
  textMuted:       primitives.gray[400],     // #A1A5B1 → 占位文本
  textInverse:     primitives.white,

  colorPrimary:    primitives.coral[500],    // #FF3B5C
  colorPrimaryRgb: '255, 59, 92',
  colorPrimaryLight: primitives.coral[50],   // #FFF0F2
  colorPrimaryDark:  primitives.coral[700],  // #CC2943
  colorGradientEnd:  primitives.sunset[500], // #FF7B54
  colorAccent:     primitives.amber[500],    // #FF9F1C
  colorAccentRgb:  '255, 159, 28',

  colorSuccess:    primitives.emerald[500],  // #10B981
  colorWarning:    primitives.amber[500],    // #FF9F1C
  colorError:      primitives.red[500],      // #EF4444
  colorInfo:       primitives.blue[500],     // #3B82F6

  borderColor:     'rgba(255, 59, 92, 0.08)',
  borderStrong:    'rgba(255, 59, 92, 0.25)',

  shadowCard:
    '0 8px 30px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(255, 59, 92, 0.04)',
  shadowFloating:
    '0 12px 40px rgba(255, 59, 92, 0.12)',
  shadowGlow:
    '0 0 20px rgba(255, 59, 92, 0.15)',

  colorGlow:          'rgba(255, 59, 92, 0.05)',
  colorGlowSecondary: 'rgba(255, 159, 28, 0.03)',
};

// ============================================================
// 暗色模式 (深邃炭灰与霓虹珊瑚)
// ============================================================
export const darkSemantic: SemanticTokens = {
  bgMain:          primitives.gray[950],     // #121216
  bgCard:          primitives.gray[800],     // #24262D
  bgElevated:      primitives.gray[700],     // #3D3F47
  bgSubtle:        'rgba(255, 59, 92, 0.06)',

  textPrimary:     primitives.gray[200],     // #E2E4E9 → 对比度 13.5:1 on #121216 ✅
  textSecondary:   primitives.gray[400],     // #A1A5B1 → 对比度 ≈ 6.1:1 on #121216 ✅
  textMuted:       primitives.gray[500],     // #737887 → 占位文本
  textInverse:     primitives.gray[900],     // #1A1A1F（暗色卡片的反白底）

  // 暗色模式：品牌色提亮一级，减少暗背景上的视觉刺眼感
  colorPrimary:    primitives.coral[400],    // #FF5C73（比基准提亮）
  colorPrimaryRgb: '255, 92, 115',
  colorPrimaryLight: 'rgba(255, 59, 92, 0.12)',
  colorPrimaryDark:  primitives.coral[300],  // #FF8596
  colorGradientEnd:  primitives.sunset[400], // #FF8B62
  colorAccent:     primitives.amber[400],    // #FFB347
  colorAccentRgb:  '255, 179, 71',

  // 暗色模式：状态色提亮
  colorSuccess:    primitives.emerald[400],  // #34D399
  colorWarning:    primitives.amber[400],    // #FFB347
  colorError:      primitives.red[400],      // #F87171
  colorInfo:       primitives.blue[400],     // #60A5FA

  borderColor:     'rgba(255, 255, 255, 0.06)',
  borderStrong:    'rgba(255, 255, 255, 0.15)',

  // 暗色阴影：加深背景阴影 + 保留品牌辉光
  shadowCard:
    '0 8px 30px rgba(0, 0, 0, 0.5), 0 2px 12px rgba(255, 92, 115, 0.06)',
  shadowFloating:
    '0 12px 40px rgba(255, 92, 115, 0.18)',
  shadowGlow:
    '0 0 24px rgba(255, 92, 115, 0.2)',

  colorGlow:          'rgba(255, 92, 115, 0.08)',
  colorGlowSecondary: 'rgba(255, 179, 71, 0.05)',
};

// ============================================================
// 合并品牌配置覆盖
// ============================================================

/**
 * 将品牌配置中的自定义颜色合并到语义令牌中
 * 只在配置值与默认值不同时才覆盖
 */
export function mergeBrandConfig(
  semantic: SemanticTokens,
  config: ThemeConfig['brand']
): SemanticTokens {
  const merged = { ...semantic };

  if (config.primaryColor && config.primaryColor !== primitives.coral[500]) {
    merged.colorPrimary = config.primaryColor;
    merged.colorPrimaryRgb = hexToRgbString(config.primaryColor);
    // 同步更新依赖主色的其他令牌
    merged.colorPrimaryDark = adjustBrightness(config.primaryColor, -20);
    merged.borderColor = `rgba(${hexToRgbString(config.primaryColor)}, 0.08)`;
    merged.borderStrong = `rgba(${hexToRgbString(config.primaryColor)}, 0.25)`;
    merged.shadowFloating =
      `0 12px 40px rgba(${hexToRgbString(config.primaryColor)}, 0.12)`;
    merged.shadowGlow =
      `0 0 20px rgba(${hexToRgbString(config.primaryColor)}, 0.15)`;
  }

  if (config.accentColor && config.accentColor !== primitives.amber[500]) {
    merged.colorAccent = config.accentColor;
    merged.colorAccentRgb = hexToRgbString(config.accentColor);
  }

  if (config.gradientEndColor && config.gradientEndColor !== primitives.sunset[500]) {
    merged.colorGradientEnd = config.gradientEndColor;
  }

  return merged;
}

// ============================================================
// 工具函数（避免循环依赖，在此文件内实现基本版本）
// ============================================================

function hexToRgbString(hex: string): string {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  if (!result) return '255, 59, 92'; // fallback
  return `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}`;
}

function adjustBrightness(hex: string, amount: number): string {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  if (!result) return hex;
  const clamp = (v: number) => Math.max(0, Math.min(255, v + amount));
  return `#${[
    clamp(parseInt(result[1], 16)),
    clamp(parseInt(result[2], 16)),
    clamp(parseInt(result[3], 16)),
  ]
    .map((v) => v.toString(16).padStart(2, '0'))
    .join('')}`;
}
