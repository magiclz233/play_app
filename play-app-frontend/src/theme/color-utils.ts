/**
 * 颜色工具函数 (Color Utilities)
 *
 * 提供 HEX ↔ RGB/HSL 转换、透明度混合、明度调整、色阶生成等函数。
 * 所有函数均为纯函数，无副作用。
 */

// ============================================================
// HEX ↔ RGB 转换
// ============================================================

/** RGB 分量对象 */
export interface RgbColor {
  r: number;
  g: number;
  b: number;
}

/** HSL 分量对象 */
export interface HslColor {
  h: number; // 0-360
  s: number; // 0-100
  l: number; // 0-100
}

/**
 * HEX 字符串 → RGB 分量对象
 * @throws 无效 HEX 时返回默认值 {r:0, g:0, b:0}
 */
export function hexToRgb(hex: string): RgbColor {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  if (!result) {
    console.warn(`[color-utils] Invalid hex color: ${hex}, falling back to black`);
    return { r: 0, g: 0, b: 0 };
  }
  return {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16),
  };
}

/**
 * HEX → CSS rgb() 字符串参数（如 "255, 59, 92"）
 * 用于 rgba(var(--color-primary-rgb), 0.5) 语法
 */
export function hexToRgbString(hex: string): string {
  const { r, g, b } = hexToRgb(hex);
  return `${r}, ${g}, ${b}`;
}

/**
 * RGB 分量 → HEX 字符串
 */
export function rgbToHex(r: number, g: number, b: number): string {
  const clamp = (v: number) => Math.max(0, Math.min(255, Math.round(v)));
  return `#${[clamp(r), clamp(g), clamp(b)]
    .map((v) => v.toString(16).padStart(2, '0'))
    .join('')}`;
}

// ============================================================
// HEX ↔ HSL 转换
// ============================================================

/**
 * HEX → HSL 分量对象
 */
export function hexToHsl(hex: string): HslColor {
  let { r, g, b } = hexToRgb(hex);
  r /= 255;
  g /= 255;
  b /= 255;

  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  const l = (max + min) / 2;

  let h = 0;
  let s = 0;

  if (max !== min) {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

    switch (max) {
      case r:
        h = ((g - b) / d + (g < b ? 6 : 0)) / 6;
        break;
      case g:
        h = ((b - r) / d + 2) / 6;
        break;
      case b:
        h = ((r - g) / d + 4) / 6;
        break;
    }
  }

  return {
    h: Math.round(h * 360),
    s: Math.round(s * 100),
    l: Math.round(l * 100),
  };
}

/**
 * HSL 分量 → HEX 字符串
 */
export function hslToHex(h: number, s: number, l: number): string {
  h /= 360;
  s /= 100;
  l /= 100;

  const hue2rgb = (p: number, q: number, t: number): number => {
    if (t < 0) t += 1;
    if (t > 1) t -= 1;
    if (t < 1 / 6) return p + (q - p) * 6 * t;
    if (t < 1 / 2) return q;
    if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
    return p;
  };

  let r: number, g: number, b: number;
  if (s === 0) {
    r = g = b = l;
  } else {
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
    const p = 2 * l - q;
    r = hue2rgb(p, q, h + 1 / 3);
    g = hue2rgb(p, q, h);
    b = hue2rgb(p, q, h - 1 / 3);
  }

  return rgbToHex(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
}

// ============================================================
// 颜色操作
// ============================================================

/**
 * 明度调整（基于 RGB 通道统一偏移）
 * @param amount 正数变亮，负数变暗（范围 -255 ~ 255）
 */
export function adjustBrightness(hex: string, amount: number): string {
  const { r, g, b } = hexToRgb(hex);
  const clamp = (v: number) => Math.max(0, Math.min(255, v + amount));
  return rgbToHex(clamp(r), clamp(g), clamp(b));
}

/**
 * 明度调整（基于 HSL，保持色相和饱和度）
 * @param amount 正数变亮，负数变暗（范围 -100 ~ 100）
 */
export function adjustLightness(hex: string, amount: number): string {
  const { h, s, l } = hexToHsl(hex);
  return hslToHex(h, s, Math.max(0, Math.min(100, l + amount)));
}

/**
 * 饱和度调整
 * @param amount 正数增加饱和度，负数减少（范围 -100 ~ 100）
 */
export function adjustSaturation(hex: string, amount: number): string {
  const { h, s, l } = hexToHsl(hex);
  return hslToHex(h, Math.max(0, Math.min(100, s + amount)), l);
}

/**
 * 透明度混合：给定 HEX 颜色 + alpha 通道
 * @returns CSS rgba() 字符串
 */
export function withAlpha(hex: string, alpha: number): string {
  const { r, g, b } = hexToRgb(hex);
  const clamped = Math.max(0, Math.min(1, alpha));
  return `rgba(${r}, ${g}, ${b}, ${clamped})`;
}

/**
 * 两个颜色混合（线性插值）
 * @param ratio 混合比例，0 = 纯 color1，1 = 纯 color2
 */
export function blend(hex1: string, hex2: string, ratio: number): string {
  const c1 = hexToRgb(hex1);
  const c2 = hexToRgb(hex2);
  const r = 1 - ratio;
  return rgbToHex(
    c1.r * r + c2.r * ratio,
    c1.g * r + c2.g * ratio,
    c1.b * r + c2.b * ratio
  );
}

// ============================================================
// 色阶生成
// ============================================================

/**
 * 从基准色自动生成 10 级色阶（50 → 950）
 *
 * 策略：
 * - 50-400: 向白色混合，逐级增亮
 * - 500: 基准色
 * - 600-950: 向黑色混合，逐级变暗
 *
 * @param baseHex 基准色（对应 500 级别）
 * @returns 10 级色阶对象
 */
export function generateScale(baseHex: string): Record<number, string> {
  const { h, s, l } = hexToHsl(baseHex);

  // 亮端：从基准 L 值线性增加到 ~98%（50 级）
  // 暗端：从基准 L 值线性减少到 ~8%（950 级）
  const lightTarget = 98;
  const darkTarget = 8;

  const scale: Record<number, string> = {};
  const levels = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950];

  for (const level of levels) {
    if (level === 500) {
      scale[level] = baseHex;
    } else if (level < 500) {
      // 向白色混合：level 越小越亮
      const ratio = (500 - level) / 450; // 0 → 1
      const targetL = l + (lightTarget - l) * ratio;
      // 饱和度随亮度微降（避免过于鲜艳的浅色）
      const targetS = s * (1 - ratio * 0.6);
      scale[level] = hslToHex(h, targetS, targetL);
    } else {
      // 向黑色混合：level 越大越暗
      const ratio = (level - 500) / 450; // 0 → 1
      const targetL = l - (l - darkTarget) * ratio;
      // 饱和度在暗端微降
      const targetS = s * (1 - ratio * 0.5);
      scale[level] = hslToHex(h, targetS, Math.max(2, targetL));
    }
  }

  return scale;
}

// ============================================================
// WCAG 对比度计算
// ============================================================

/**
 * 计算相对亮度（WCAG 2.1 公式）
 */
function relativeLuminance({ r, g, b }: RgbColor): number {
  const toLinear = (c: number): number => {
    const sRGB = c / 255;
    return sRGB <= 0.03928 ? sRGB / 12.92 : Math.pow((sRGB + 0.055) / 1.055, 2.4);
  };
  return 0.2126 * toLinear(r) + 0.7152 * toLinear(g) + 0.0722 * toLinear(b);
}

/**
 * 计算两个 HEX 颜色之间的 WCAG 对比度
 * @returns 对比度比值（1:1 → 21:1）
 */
export function contrastRatio(hex1: string, hex2: string): number {
  const l1 = relativeLuminance(hexToRgb(hex1));
  const l2 = relativeLuminance(hexToRgb(hex2));
  const lighter = Math.max(l1, l2);
  const darker = Math.min(l1, l2);
  return (lighter + 0.05) / (darker + 0.05);
}

/**
 * 检查对比度是否满足 WCAG AA 标准（≥ 4.5:1）
 */
export function meetsAA(hex1: string, hex2: string): boolean {
  return contrastRatio(hex1, hex2) >= 4.5;
}

/**
 * 检查对比度是否满足 WCAG AAA 标准（≥ 7:1）
 */
export function meetsAAA(hex1: string, hex2: string): boolean {
  return contrastRatio(hex1, hex2) >= 7;
}

/**
 * 在给定背景色上找到满足对比度要求的文字颜色
 * 逐步调整文字色明度直到满足目标对比度
 * @param bgHex 背景色
 * @param textHex 期望文字色（会在此色附近调整）
 * @param targetRatio 目标对比度，默认 4.5（AA）
 */
export function ensureContrast(
  bgHex: string,
  textHex: string,
  targetRatio: number = 4.5
): string {
  let current = textHex;
  let attempts = 0;
  const maxAttempts = 50;

  // 如果当前已满足，直接返回
  if (contrastRatio(bgHex, current) >= targetRatio) return current;

  const bgLuminance = relativeLuminance(hexToRgb(bgHex));
  const textLuminance = relativeLuminance(hexToRgb(current));

  // 如果文字比背景亮但对比度不足，则提亮文字；反之变暗
  const direction = textLuminance > bgLuminance ? 1 : -1;
  const step = 2;

  while (attempts < maxAttempts) {
    current = adjustLightness(current, direction * step);
    if (contrastRatio(bgHex, current) >= targetRatio) return current;
    attempts++;
  }

  // 兜底：返回纯黑或纯白
  return bgLuminance > 0.5 ? '#000000' : '#FFFFFF';
}
