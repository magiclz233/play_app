/**
 * 主题引擎 (ThemeEngine)
 *
 * 核心职责：
 * 1. 合并三层设计令牌 → 计算最终 CSS 变量集
 * 2. 注入 CSS 变量到页面根元素（支持 uni-app 多端）
 * 3. 同步原生 UI 组件（NavigationBar、TabBar）
 * 4. 响应品牌配置变更（本地/远程）
 * 5. 响应系统明暗模式切换
 *
 * 使用方式：
 *   import { themeEngine } from '@/theme/engine';
 *   themeEngine.init();           // 初始化（App.vue onLaunch）
 *   themeEngine.setTheme('dark'); // 切换到暗色
 *   themeEngine.setConfig({ brand: { primaryColor: '#FF6B81' } }); // 更新品牌色
 */

import { lightSemantic, darkSemantic, mergeBrandConfig, type SemanticTokens } from './semantic';
import { flattenComponentTokens, componentTokens } from './components';
import {
  defaultThemeConfig,
  mergeThemeConfig,
  RADIUS_BASE,
  RADIUS_MULTIPLIERS,
  SPACING_BASE,
  SPACING_MULTIPLIERS,
  type ThemeConfig,
  type ThemeName,
  type ThemeMode,
} from './theme.config';
import { hexToRgbString } from './color-utils';

// ============================================================
// 类型
// ============================================================

/** CSS 变量映射表 */
type CssVarMap = Record<string, string>;

/** 原生导航栏配置 */
interface NavBarConfig {
  frontColor: '#000000' | '#ffffff';
  backgroundColor: string;
}

/** 原生 TabBar 配置 */
interface TabBarConfig {
  color: string;
  selectedColor: string;
  backgroundColor: string;
  borderStyle: 'black' | 'white';
}

// ============================================================
// ThemeEngine 类
// ============================================================

class ThemeEngine {
  private config: ThemeConfig = defaultThemeConfig;
  private currentMode: ThemeName = 'light';
  private themeMode: ThemeMode = 'light';
  private initialized = false;
  private themeChangeListeners: Array<(mode: ThemeName) => void> = [];

  // ============================================================
  // 初始化
  // ============================================================

  /**
   * 初始化主题引擎
   * 应在 App.vue onLaunch 中调用一次
   */
  init(savedThemeMode?: ThemeMode): void {
    if (this.initialized) return;

    // 1. 恢复保存的主题模式
    this.themeMode = savedThemeMode || 'light';

    // 2. 解析当前实际主题
    this.currentMode = this.resolveTheme();

    // 3. 应用主题
    this.apply();

    // 4. 监听系统主题变化
    this.watchSystemTheme();

    this.initialized = true;
  }

  // ============================================================
  // 主题切换
  // ============================================================

  /** 设置主题模式 */
  setThemeMode(mode: ThemeMode): void {
    this.themeMode = mode;
    const newTheme = this.resolveTheme();
    if (newTheme !== this.currentMode) {
      this.currentMode = newTheme;
      this.apply();
    }
  }

  /** 直接设置明暗主题 */
  setTheme(theme: ThemeName): void {
    if (this.currentMode !== theme) {
      this.currentMode = theme;
      this.apply();
    }
  }

  /** 切换明暗 */
  toggleTheme(): void {
    this.setTheme(this.currentMode === 'light' ? 'dark' : 'light');
  }

  /** 获取当前主题名称 */
  getCurrentTheme(): ThemeName {
    return this.currentMode;
  }

  /** 是否为暗色模式 */
  isDark(): boolean {
    return this.currentMode === 'dark';
  }

  // ============================================================
  // 配置更新
  // ============================================================

  /** 设置品牌配置（支持部分更新） */
  setConfig(partial: Partial<ThemeConfig>): void {
    this.config = mergeThemeConfig(this.config, partial);
    this.apply();
  }

  /** 获取当前配置 */
  getConfig(): ThemeConfig {
    return { ...this.config };
  }

  // ============================================================
  // 核心：计算 CSS 变量
  // ============================================================

  /**
   * 计算完整的 CSS 变量映射表
   * Layer 1 (Primitives) → Layer 2 (Semantic) → Layer 3 (Components) → Layout Tokens
   */
  computeAllCssVars(): CssVarMap {
    // ── 1. 获取对应主题的语义令牌 ──
    let semantic: SemanticTokens =
      this.currentMode === 'light' ? { ...lightSemantic } : { ...darkSemantic };

    // ── 2. 应用品牌配置覆盖 ──
    semantic = mergeBrandConfig(semantic, this.config.brand);

    // ── 3. 展平语义令牌为 CSS 变量 ──
    const vars: CssVarMap = {
      // 背景层级
      '--bg-main': semantic.bgMain,
      '--bg-card': semantic.bgCard,
      '--bg-elevated': semantic.bgElevated,
      '--bg-subtle': semantic.bgSubtle,

      // 文字层级
      '--text-primary': semantic.textPrimary,
      '--text-secondary': semantic.textSecondary,
      '--text-muted': semantic.textMuted,
      '--text-inverse': semantic.textInverse,

      // 品牌色
      '--color-primary': semantic.colorPrimary,
      '--color-primary-rgb': semantic.colorPrimaryRgb,
      '--color-primary-light': semantic.colorPrimaryLight,
      '--color-primary-dark': semantic.colorPrimaryDark,
      '--color-gradient-end': semantic.colorGradientEnd,
      '--color-accent': semantic.colorAccent,
      '--color-accent-rgb': semantic.colorAccentRgb,

      // 状态色
      '--color-success': semantic.colorSuccess,
      '--color-warning': semantic.colorWarning,
      '--color-error': semantic.colorError,
      '--color-info': semantic.colorInfo,

      // 边框与分割
      '--border-color': semantic.borderColor,
      '--border-strong': semantic.borderStrong,

      // 阴影
      '--shadow-card': semantic.shadowCard,
      '--shadow-floating': semantic.shadowFloating,
      '--shadow-glow': semantic.shadowGlow,

      // 特殊效果
      '--color-glow': semantic.colorGlow,
      '--color-glow-secondary': semantic.colorGlowSecondary,
    };

    // ── 4. 布局令牌（圆角、字号、间距） ──
    const radiusBase = RADIUS_BASE[this.config.radius.scale];
    Object.assign(vars, {
      '--radius-sm': `${Math.round(radiusBase * RADIUS_MULTIPLIERS.sm)}rpx`,
      '--radius-md': `${Math.round(radiusBase * RADIUS_MULTIPLIERS.md)}rpx`,
      '--radius-lg': `${Math.round(radiusBase * RADIUS_MULTIPLIERS.lg)}rpx`,
      '--radius-xl': `${Math.round(radiusBase * RADIUS_MULTIPLIERS.xl)}rpx`,
      '--radius-full': `${RADIUS_MULTIPLIERS.full}rpx`,
    });

    const fontScale = this.config.typography.scale;
    Object.assign(vars, {
      '--font-size-xs': `${Math.round(20 * fontScale)}rpx`,
      '--font-size-sm': `${Math.round(24 * fontScale)}rpx`,
      '--font-size-base': `${Math.round(28 * fontScale)}rpx`,
      '--font-size-lg': `${Math.round(32 * fontScale)}rpx`,
      '--font-size-xl': `${Math.round(36 * fontScale)}rpx`,
      '--font-size-xxl': `${Math.round(48 * fontScale)}rpx`,
    });

    const spacingBase = SPACING_BASE[this.config.spacing.scale];
    Object.assign(vars, {
      '--space-xs': `${spacingBase * SPACING_MULTIPLIERS.xs}rpx`,
      '--space-sm': `${spacingBase * SPACING_MULTIPLIERS.sm}rpx`,
      '--space-md': `${spacingBase * SPACING_MULTIPLIERS.md}rpx`,
      '--space-lg': `${spacingBase * SPACING_MULTIPLIERS.lg}rpx`,
      '--space-xl': `${spacingBase * SPACING_MULTIPLIERS.xl}rpx`,
    });

    // ── 5. 组件令牌 ──
    Object.assign(vars, flattenComponentTokens(componentTokens));

    return vars;
  }

  // ============================================================
  // CSS 变量注入
  // ============================================================

  /**
   * 完整应用主题：注入 CSS 变量 + 更新原生 UI
   */
  apply(): void {
    const vars = this.computeAllCssVars();
    this.injectCssVars(vars);
    this.applyNativeUi(vars);
    this.notifyListeners();
  }

  /**
   * 注入 CSS 变量到页面根元素
   * 跨端兼容：H5 用 document.documentElement，小程序用 uni 页面样式
   */
  private injectCssVars(vars: CssVarMap): void {
    const styleString = Object.entries(vars)
      .map(([k, v]) => `${k}:${v}`)
      .join(';');

    // #ifdef H5
    if (typeof document !== 'undefined' && document.documentElement) {
      document.documentElement.style.cssText += styleString;
    }
    // #endif

    // #ifdef MP-WEIXIN
    // 微信小程序：通过 wxss 编译时变量 + 页面根节点 class 实现
    // uni.scss 中定义了 CSS 变量兜底值，此处仅存储以供页面内使用
    // #endif

    // 存储到全局变量，供页面内 JS 读取
    if (typeof globalThis !== 'undefined') {
      (globalThis as any).__themeVars = vars;
    }
  }

  /**
   * 获取 CSS 变量映射表（供页面 :style 绑定使用）
   * 保留此方法以兼容仍在使用 :style="themeStyle" 的页面
   */
  getCssVars(): CssVarMap {
    return this.computeAllCssVars();
  }

  /**
   * @deprecated 请使用 getCssVars() 代替
   */
  get themeStyle(): CssVarMap {
    return this.getCssVars();
  }

  /**
   * 获取当前主题 class 名
   * @deprecated 请直接使用 :class 绑定 isDark 计算属性
   */
  get themeClass(): string {
    return `theme-${this.currentMode}`;
  }

  // ============================================================
  // 原生 UI 同步
  // ============================================================

  /**
   * 更新微信小程序原生组件颜色
   */
  private applyNativeUi(vars: CssVarMap): void {
    const isDark = this.currentMode === 'dark';

    // NavigationBar
    const navConfig: NavBarConfig = {
      frontColor: isDark ? '#ffffff' : '#000000',
      backgroundColor: vars['--bg-main'] || (isDark ? '#121216' : '#FAF5F5'),
    };

    // TabBar
    const tabConfig: TabBarConfig = {
      color: vars['--text-muted'] || (isDark ? '#737887' : '#A1A5B1'),
      selectedColor: vars['--color-primary'] || '#FF3B5C',
      backgroundColor: vars['--bg-card'] || (isDark ? '#24262D' : '#FFFFFF'),
      borderStyle: isDark ? 'white' : 'black',
    };

    try {
      uni.setNavigationBarColor({
        ...navConfig,
        animation: { duration: 200, timingFunc: 'easeIn' },
        fail: () => {}
      });
      uni.setTabBarStyle({
        ...tabConfig,
        fail: () => {}
      });
    } catch (e) {
      // 某些页面可能没有 TabBar，忽略错误
      console.debug('[ThemeEngine] applyNativeUi error (may be expected):', e);
    }
  }

  // ============================================================
  // 系统主题监听
  // ============================================================

  private watchSystemTheme(): void {
    try {
      uni.onThemeChange((res: { theme: 'light' | 'dark' }) => {
        if (this.themeMode === 'auto') {
          const newTheme = res.theme as ThemeName;
          if (this.currentMode !== newTheme) {
            this.currentMode = newTheme;
            this.apply();
          }
        }
      });
    } catch (e) {
      console.debug('[ThemeEngine] onThemeChange not supported on this platform');
    }
  }

  // ============================================================
  // 监听器
  // ============================================================

  /** 注册主题变更回调 */
  onChange(callback: (mode: ThemeName) => void): () => void {
    this.themeChangeListeners.push(callback);
    return () => {
      this.themeChangeListeners = this.themeChangeListeners.filter((cb) => cb !== callback);
    };
  }

  private notifyListeners(): void {
    for (const cb of this.themeChangeListeners) {
      try {
        cb(this.currentMode);
      } catch (e) {
        console.error('[ThemeEngine] listener error:', e);
      }
    }
  }

  // ============================================================
  // 内部工具
  // ============================================================

  /** 根据 themeMode 解析当前实际应使用的明暗主题 */
  private resolveTheme(): ThemeName {
    if (this.themeMode === 'auto') {
      try {
        const sysInfo = uni.getSystemInfoSync();
        return sysInfo.theme === 'dark' ? 'dark' : 'light';
      } catch {
        return 'light';
      }
    }
    return this.themeMode;
  }
}

// ============================================================
// 单例导出
// ============================================================

export const themeEngine = new ThemeEngine();

// ============================================================
// 便捷函数（兼容旧的 tokens.ts 导出风格）
// ============================================================

/** @deprecated 请使用 themeEngine.setTheme() */
export function applyTheme(mode: ThemeName): void {
  themeEngine.setTheme(mode);
}

/** @deprecated 请使用 themeEngine.setConfig() */
export function updateBrandConfig(config: Partial<ThemeConfig>): void {
  themeEngine.setConfig(config);
}
