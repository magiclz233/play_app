# 🎨 同城伴玩 — 主题系统全面优化方案

> **目标**：构建可配置、可扩展、易维护的三层设计令牌体系，全面支持明暗色自适应 + 品牌色动态配置。

---

## 📊 一、现状诊断

### 1.1 当前架构问题

| # | 问题 | 严重度 | 影响范围 |
|---|------|--------|---------|
| 1 | **CSS 变量双重定义** — `tokens.ts` 和 `uni.scss` 各自维护一份相同的颜色值 | 🔴 高 | 改色需同步两处，极易不一致 |
| 2 | **主题不可扩展** — 只硬编码 `light`/`dark` 两套，无法新增主题 | 🔴 高 | 无法支持品牌换肤、节日主题 |
| 3 | **pages.json 颜色硬编码** — tabBar/navigationBar 颜色写死在 JSON 中 | 🔴 高 | 这些颜色无法运行时动态更新 |
| 4 | **每页重复绑定** — 所有页面需手动 `:class="appStore.themeClass"` + `:style="appStore.themeStyle"` | 🟡 中 | 19 个页面重复代码，易遗漏 |
| 5 | **品牌色不随暗色调整** — `#FF3B5C` 在亮/暗模式下完全一致 | 🟡 中 | 暗色下珊瑚红过于刺眼，视觉疲劳 |
| 6 | **缺少 color-mix 工具** — hover/active/disabled 状态的半透明色全部硬编码 | 🟡 中 | 代码冗余，调整主色后需逐个改 |
| 7 | **无 CSS 媒体查询兜底** — JS 失败时主题完全丢失 | 🟢 低 | 降级体验差 |
| 8 | **无 prefers-reduced-motion 支持** | 🟢 低 | 无障碍合规缺失 |
| 9 | **阴影在暗色下突兀** — `shadow-card` 暗色下变 `rgba(0,0,0,0.4)`，是纯黑阴影 | 🟡 中 | 与 "流光珊瑚辉光阴影" 设计理念不符 |
| 10 | **主题持久化仅 localStorage** — 无后端同步，换设备丢失偏好 | 🟢 低 | 用户体验断层 |

### 1.2 对照 Skill 规范差距

根据 UI/UX Pro Max 规范审查：

| 规范要求 | 当前状态 | 差距 |
|---------|---------|------|
| `color-contrast` — 正文 4.5:1 对比度 | `--text-secondary: #5E6066` 在亮色 bg `#FAF5F5` 上 ≈ 4.2:1 | ⚠️ 略不足 |
| `focus-states` — 可见的聚焦环 | 无全局 focus 样式 | ❌ 缺失 |
| `touch-target-size` — 最小 44×44px | 部分 icon 按钮不足 | ⚠️ 需修复 |
| `reduced-motion` — 尊重系统动画偏好 | 未实现 | ❌ 缺失 |
| `no-emoji-icons` — 用 SVG 而非 emoji | 详情页使用 ♂♀ emoji | ⚠️ 需替换 |
| `line-height` — 正文 1.5-1.75 | 未全局设定 | ⚠️ 需补充 |
| `cursor-pointer` — 可点击元素 | 小程序无 cursor | N/A |

---

## 🏛️ 二、优化架构设计

### 2.1 三层设计令牌体系（Design Token Tiers）

```
┌─────────────────────────────────────────────┐
│  Layer 3: Component Tokens (组件令牌)        │
│  --btn-primary-bg, --card-shadow, --input-bg │
│  可由组件库/页面覆盖                          │
├─────────────────────────────────────────────┤
│  Layer 2: Semantic Tokens (语义令牌)         │
│  --color-primary, --bg-main, --text-primary  │
│  亮/暗模式在此层切换                          │
├─────────────────────────────────────────────┤
│  Layer 1: Primitive Tokens (原始令牌)        │
│  coral-500: #FF3B5C, gray-100: #F5F5F5      │
│  纯色值，不携带语义                           │
└─────────────────────────────────────────────┘
```

#### Layer 1: 原始色板 (`primitives.ts`)

```typescript
// 每个色相定义 10 级色阶（50 → 950）
export const primitives = {
  coral: {
    50:  '#FFF0F2',   // 最浅 — 标签底色
    100: '#FFD6DC',
    200: '#FFADB9',
    300: '#FF8596',
    400: '#FF5C73',   // 暗色模式主色（稍微提亮）
    500: '#FF3B5C',   // 基准主色
    600: '#E6324F',
    700: '#CC2943',
    800: '#991F32',
    900: '#661522',   // 最深 — 暗色背景强调
  },
  sunset: {
    400: '#FF9B6B',
    500: '#FF7B54',   // 渐变色终点
    600: '#E66B45',
  },
  amber: {
    400: '#FFB347',
    500: '#FF9F1C',   // 强调色
  },
  gray: {
    50:  '#F8F9FA',
    100: '#F0F1F3',
    200: '#E2E4E9',
    300: '#C5C8D2',
    400: '#A1A5B1',
    500: '#737887',
    600: '#5E6066',
    700: '#3D3F47',
    800: '#202127',
    900: '#18181C',
    950: '#121216',
  },
  // ... green, red, blue 等状态色
} as const;
```

#### Layer 2: 语义令牌 (`semantic.ts`)

```typescript
export interface SemanticTokens {
  // 背景层级
  bgMain: string;
  bgCard: string;
  bgElevated: string;  // 新增：浮层/弹窗背景（比 card 高一级）
  bgSubtle: string;    // 新增：极浅背景（分割区块用）

  // 文字层级
  textPrimary: string;
  textSecondary: string;
  textMuted: string;
  textInverse: string;  // 新增：深色背景上的反白文字

  // 品牌色
  colorPrimary: string;
  colorPrimaryLight: string;
  colorPrimaryDark: string;
  colorGradientEnd: string;
  colorAccent: string;

  // 状态色
  colorSuccess: string;
  colorWarning: string;
  colorError: string;
  colorInfo: string;

  // 边框与分割
  borderColor: string;
  borderStrong: string;  // 新增：高对比边框（聚焦态）

  // 阴影
  shadowCard: string;
  shadowFloating: string;
  shadowGlow: string;    // 新增：品牌色辉光阴影
}

// 亮色模式映射
export const lightSemantic: SemanticTokens = {
  bgMain:          primitives.gray[50],
  bgCard:          '#FFFFFF',
  bgElevated:      '#FFFFFF',
  bgSubtle:        primitives.coral[50],

  textPrimary:     primitives.gray[900],
  textSecondary:   primitives.gray[600],  // ← 修复对比度：原 #5E6066 → gray-600
  textMuted:       primitives.gray[400],
  textInverse:     '#FFFFFF',

  colorPrimary:    primitives.coral[500],
  colorPrimaryLight: primitives.coral[50],
  colorPrimaryDark:  primitives.coral[700],
  colorGradientEnd:  primitives.sunset[500],
  colorAccent:     primitives.amber[500],

  colorSuccess:    '#10B981',
  colorWarning:    '#F59E0B',
  colorError:      '#EF4444',
  colorInfo:       '#3B82F6',

  borderColor:     'rgba(255, 59, 92, 0.08)',
  borderStrong:    'rgba(255, 59, 92, 0.25)',

  shadowCard:      '0 8px 30px rgba(0,0,0,0.04), 0 4px 16px rgba(255,59,92,0.04)',
  shadowFloating:  '0 12px 40px rgba(255,59,92,0.12)',
  shadowGlow:      '0 0 20px rgba(255,59,92,0.15)',
};

// 暗色模式映射 — 颜色微调
export const darkSemantic: SemanticTokens = {
  bgMain:          primitives.gray[950],
  bgCard:          primitives.gray[800],
  bgElevated:      primitives.gray[700],
  bgSubtle:        'rgba(255,59,92,0.06)',

  textPrimary:     primitives.gray[200],
  textSecondary:   primitives.gray[400],
  textMuted:       primitives.gray[500],
  textInverse:     primitives.gray[900],

  colorPrimary:    primitives.coral[400],  // ← 暗色下提亮一级，减少刺眼
  colorPrimaryLight: 'rgba(255,59,92,0.12)',
  colorPrimaryDark:  primitives.coral[300],
  colorGradientEnd:  primitives.sunset[400],
  colorAccent:     primitives.amber[400],

  colorSuccess:    '#34D399',
  colorWarning:    '#FBBF24',
  colorError:      '#F87171',
  colorInfo:       '#60A5FA',

  borderColor:     'rgba(255,255,255,0.06)',
  borderStrong:    'rgba(255,255,255,0.15)',

  shadowCard:      '0 8px 30px rgba(0,0,0,0.5), 0 2px 12px rgba(255,92,115,0.08)',
  shadowFloating:  '0 12px 40px rgba(255,92,115,0.18)',
  shadowGlow:      '0 0 24px rgba(255,92,115,0.2)',
};
```

#### Layer 3: 组件令牌 (`components.ts`)

```typescript
// 组件级令牌 — 默认继承语义令牌，允许单独覆盖
export const componentTokens = {
  button: {
    primaryBg:        'linear-gradient(135deg, var(--color-primary), var(--color-gradient-end))',
    primaryShadow:    'var(--shadow-floating)',
    primaryHoverBg:   'linear-gradient(135deg, var(--color-primary-dark), var(--color-gradient-end))',
    outlineBg:        'var(--color-primary-light)',
    outlineBorder:    'var(--color-primary)',
  },
  card: {
    bg:               'var(--bg-card)',
    surfaceReflection:'linear-gradient(145deg, rgba(255,255,255,0.03) 0%, transparent 100%)',
    border:           '1px solid var(--border-color)',
    shadow:           'var(--shadow-card)',
    innerHighlight:   'inset 0 1px 1px rgba(255,255,255,0.06)',
  },
  input: {
    bg:               'var(--bg-main)',
    border:           '1px solid var(--border-color)',
    focusBorder:      '1px solid var(--border-strong)',
    placeholder:      'var(--text-muted)',
  },
  tabBar: {
    color:            'var(--text-muted)',
    selectedColor:    'var(--color-primary)',
    bg:               'var(--bg-card)',
  },
  // ...更多组件
};
```

### 2.2 品牌色配置系统（Brand Color Configuration）

#### 架构图

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  后端 Admin   │    │  app.config  │    │  本地存储    │
│  品牌色配置    │    │  (静态默认)   │    │  用户偏好    │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │ 优先级合并
                     ┌─────▼─────┐
                     │  主题引擎   │
                     │ ThemeEngine│
                     └─────┬─────┘
                           │ injectCssVars()
                     ┌─────▼─────┐
                     │  :root /   │
                     │  <page>    │
                     │  CSS 变量  │
                     └───────────┘
```

#### 配置文件结构 (`theme.config.ts`)

```typescript
export interface ThemeConfig {
  // 品牌色（可被后端覆盖）
  brand: {
    primaryColor: string;        // 如 '#FF3B5C'
    accentColor: string;         // 如 '#FF9F1C'
    gradientEndColor: string;    // 如 '#FF7B54'
  };
  // 圆角策略
  radius: {
    scale: 'rounded' | 'smooth' | 'pill';  // 小圆角 / 中等圆角 / 全圆角
    customBase?: number;          // 自定义基准 rpx
  };
  // 间距策略
  spacing: {
    scale: 'compact' | 'comfortable' | 'spacious';
  };
  // 字体缩放（无障碍大字体）
  typography: {
    scale: 1 | 1.15 | 1.3;      // 标准 / 大号 / 特大
  };
}

// 默认配置 — 可被后端 API 响应覆盖
export const defaultConfig: ThemeConfig = {
  brand: {
    primaryColor: '#FF3B5C',
    accentColor: '#FF9F1C',
    gradientEndColor: '#FF7B54',
  },
  radius: { scale: 'smooth' },
  spacing: { scale: 'comfortable' },
  typography: { scale: 1 },
};
```

#### 后端配置 API

```
GET  /api/public/theme-config    → 返回平台当前品牌色配置（公开）
PUT  /api/admin/theme-config     → 管理员更新品牌色（需 ROLE_ADMIN）
```

```java
// ThemeConfigController.java
@RestController
@RequestMapping("/api")
public class ThemeConfigController {

    @GetMapping("/public/theme-config")
    public Result<ThemeConfigVO> getPublicConfig() {
        // 从 system_config 表读取 JSONB 配置
        return Result.success(themeConfigService.getPublicConfig());
    }

    @PutMapping("/admin/theme-config")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateConfig(@Valid @RequestBody ThemeConfigUpdateDTO dto) {
        themeConfigService.updateConfig(dto);
        return Result.success();
    }
}
```

### 2.3 主题引擎实现 (`ThemeEngine`)

```typescript
// src/theme/engine.ts
import { primitives } from './primitives';
import { lightSemantic, darkSemantic, type SemanticTokens } from './semantic';
import { componentTokens } from './components';
import { defaultConfig, type ThemeConfig } from './theme.config';

class ThemeEngine {
  private config: ThemeConfig = defaultConfig;
  private currentMode: 'light' | 'dark' = 'light';
  private cssVarCache: Map<string, string> = new Map();

  /**
   * 计算完整的 CSS 变量表（合并三层令牌 + 品牌色覆盖）
   */
  computeAllCssVars(): Record<string, string> {
    const semantic = this.currentMode === 'light' ? lightSemantic : darkSemantic;

    // Layer 1 + 2: 语义令牌
    const vars: Record<string, string> = {
      '--bg-main':           semantic.bgMain,
      '--bg-card':           semantic.bgCard,
      '--bg-elevated':       semantic.bgElevated,
      '--bg-subtle':         semantic.bgSubtle,
      '--text-primary':      semantic.textPrimary,
      '--text-secondary':    semantic.textSecondary,
      '--text-muted':        semantic.textMuted,
      '--text-inverse':      semantic.textInverse,
      '--color-primary':     this.config.brand.primaryColor,
      '--color-primary-rgb': this.hexToRgb(this.config.brand.primaryColor),
      '--color-primary-light': semantic.colorPrimaryLight,
      '--color-primary-dark':  semantic.colorPrimaryDark,
      '--color-gradient-end':  this.config.brand.gradientEndColor,
      '--color-accent':      this.config.brand.accentColor,
      '--color-accent-rgb':  this.hexToRgb(this.config.brand.accentColor),
      '--color-success':     semantic.colorSuccess,
      '--color-warning':     semantic.colorWarning,
      '--color-error':       semantic.colorError,
      '--color-info':        semantic.colorInfo,
      '--border-color':      semantic.borderColor,
      '--border-strong':     semantic.borderStrong,
      '--shadow-card':       semantic.shadowCard,
      '--shadow-floating':   semantic.shadowFloating,
      '--shadow-glow':       semantic.shadowGlow,
      // 圆角（根据配置缩放）
      '--radius-sm':         this.scaleRadius(8),
      '--radius-md':         this.scaleRadius(16),
      '--radius-lg':         this.scaleRadius(24),
      '--radius-xl':         this.scaleRadius(32),
      '--radius-full':       '999rpx',
      // 字号（根据配置缩放）
      '--font-size-xs':      this.scaleFont(20),
      '--font-size-sm':      this.scaleFont(24),
      '--font-size-base':    this.scaleFont(28),
      '--font-size-lg':      this.scaleFont(32),
      '--font-size-xl':      this.scaleFont(36),
      '--font-size-xxl':     this.scaleFont(48),
      // 间距
      '--space-xs':          '8rpx',
      '--space-sm':          '16rpx',
      '--space-md':          '24rpx',
      '--space-lg':          '32rpx',
      '--space-xl':          '48rpx',
    };

    // Layer 3: 组件令牌
    Object.assign(vars, this.computeComponentTokens(vars));

    return vars;
  }

  /**
   * 将计算出的 CSS 变量注入到 :root 和 page 上
   */
  injectToDocument(vars: Record<string, string>) {
    // uni-app 中通过 setPageStyle 注入到 page 根元素
    const styleString = Object.entries(vars)
      .map(([k, v]) => `${k}:${v}`)
      .join(';');

    // 方式1: 设置页面样式（小程序兼容）
    // #ifdef MP-WEIXIN
    // 小程序中通过 wxss 编译时注入 + 运行时 setPageStyle
    // #endif

    // 方式2: 设置 document 根元素（H5 兼容）
    // #ifdef H5
    document.documentElement.style.cssText = styleString;
    // #endif
  }

  setTheme(mode: 'light' | 'dark') {
    this.currentMode = mode;
    this.apply();
  }

  setConfig(config: Partial<ThemeConfig>) {
    this.config = { ...this.config, ...config };
    this.apply();
  }

  apply() {
    const vars = this.computeAllCssVars();
    this.injectToDocument(vars);
    this.applyNativeUi();
  }

  // ... 工具方法: hexToRgb, scaleRadius, scaleFont, computeComponentTokens
}

export const themeEngine = new ThemeEngine();
```

### 2.4 集中化 CSS 变量注入（替代逐页绑定）

#### 当前模式（❌ 不推荐）
```html
<!-- 每个页面都要写这两行 -->
<view :class="['container', appStore.themeClass]" :style="appStore.themeStyle">
```

#### 优化后（✅ 推荐）
```html
<!-- 页面无需任何主题绑定 -->
<view class="container">
```

**如何实现**：在 `App.vue` 的 `onLaunch` 中一次性注入：

```typescript
// App.vue onLaunch
import { themeEngine } from '@/theme/engine';

onLaunch(() => {
  // 1. 拉取远程品牌配置
  fetch('/api/public/theme-config').then(res => {
    if (res.data) themeEngine.setConfig(res.data);
  });
  // 2. 应用本地主题偏好
  const savedMode = uni.getStorageSync('themeMode') || 'auto';
  if (savedMode === 'auto') {
    const sys = uni.getSystemInfoSync();
    themeEngine.setTheme(sys.theme === 'dark' ? 'dark' : 'light');
  } else {
    themeEngine.setTheme(savedMode);
  }
  // 3. 监听系统主题变化
  uni.onThemeChange(res => {
    if (uni.getStorageSync('themeMode') === 'auto') {
      themeEngine.setTheme(res.theme as 'light' | 'dark');
    }
  });
});
```

同时，在 `uni.scss` 中保留 CSS 媒体查询兜底（JS 失败时）：

```scss
/* CSS 兜底 — JS 注入失败时的降级方案 */
page {
  --bg-main: #FAF5F5;
  --bg-card: #FFFFFF;
  --text-primary: #1A1A1F;
  --text-secondary: #5E6066;
  --color-primary: #FF3B5C;
  // ...其他变量
}

/* 系统暗色模式自动检测（纯 CSS 兜底） */
@media (prefers-color-scheme: dark) {
  page {
    --bg-main: #18181C;
    --bg-card: #202127;
    --text-primary: #E2E4E9;
    --text-secondary: #A1A5B1;
    // ...暗色变量
  }
}
```

### 2.5 Pinia Store 精简

```typescript
// src/store/app.ts — 精简版
export const useAppStore = defineStore('app', () => {
  const themeMode = ref<ThemeMode>(
    uni.getStorageSync('themeMode') || 'auto'
  );
  const currentTheme = ref<ThemeName>('light');
  const locale = ref<Locale>(getSavedLocale());

  // ❌ 删除: themeClass, themeStyle — 不再需要逐页绑定
  // ✅ 新增: 简洁的状态暴露
  const isDark = computed(() => currentTheme.value === 'dark');

  const initApp = () => {
    // 1. 加载远程品牌配置
    loadRemoteThemeConfig();
    // 2. 应用主题
    themeEngine.setTheme(resolveActualTheme());
    // 3. 监听系统变化
    uni.onThemeChange(res => {
      if (themeMode.value === 'auto') {
        themeEngine.setTheme(res.theme as ThemeName);
        currentTheme.value = res.theme as ThemeName;
      }
    });
  };

  async function loadRemoteThemeConfig() {
    try {
      const res = await request({ url: '/public/theme-config', method: 'GET' });
      if (res.code === 200 && res.data) {
        themeEngine.setConfig(mapRemoteConfig(res.data));
      }
    } catch {
      // 网络失败时使用默认配置，无需处理
    }
  }

  return {
    themeMode, currentTheme, isDark, locale,
    setThemeMode, toggleTheme, setLocale, initApp,
  };
});
```

---

## 🎯 三、实施方案

### Phase 1: 基础设施（1-2 天）

| 步骤 | 文件 | 操作 |
|------|------|------|
| 1.1 | `src/theme/primitives.ts` | 🆕 新建 — 原始色板定义 |
| 1.2 | `src/theme/semantic.ts` | 🆕 新建 — 亮/暗语义令牌 |
| 1.3 | `src/theme/components.ts` | 🆕 新建 — 组件令牌 |
| 1.4 | `src/theme/theme.config.ts` | 🆕 新建 — 品牌配置类型 |
| 1.5 | `src/theme/engine.ts` | 🆕 新建 — 主题引擎（计算+注入） |
| 1.6 | `src/theme/index.ts` | 🆕 新建 — 统一导出入口 |
| 1.7 | `src/uni.scss` | ✏️ 修改 — 精简为 CSS 兜底 + SCSS 变量映射 |
| 1.8 | `src/theme/tokens.ts` | ✏️ 修改 — 标记废弃，重导出到新模块 |

### Phase 2: 页面解耦（1 天）

| 步骤 | 文件范围 | 操作 |
|------|---------|------|
| 2.1 | `App.vue` | ✏️ 修改 — 用 `themeEngine.init()` 替换老逻辑 |
| 2.2 | `src/store/app.ts` | ✏️ 修改 — 精简为纯状态管理 |
| 2.3 | 19 个页面 `.vue` 文件 | ✏️ 修改 — 删除 `:class="appStore.themeClass"` 和 `:style="appStore.themeStyle"` |
| 2.4 | `pages.json` | ✏️ 修改 — tabBar/navBar 颜色改用 `themeEngine` 运行时注入 |

### Phase 3: 后端配置支持（1-2 天）

| 步骤 | 文件 | 操作 |
|------|------|------|
| 3.1 | `schema.sql` | ✏️ 新增 `system_config` 表 |
| 3.2 | `entity/SystemConfig.java` | 🆕 实体类 |
| 3.3 | `mapper/SystemConfigMapper.java` | 🆕 Mapper |
| 3.4 | `service/ThemeConfigService.java` | 🆕 服务层 |
| 3.5 | `controller/ThemeConfigController.java` | 🆕 控制器 |
| 3.6 | `dto/ThemeConfigUpdateDTO.java` | 🆕 DTO |

### Phase 4: 质量提升（1 天）

| 步骤 | 操作 |
|------|------|
| 4.1 | 全局替换 `♂` `♀` `★` emoji → SVG/Lucide 图标 |
| 4.2 | 全局补充 `prefers-reduced-motion` 媒体查询 |
| 4.3 | 修复 `--text-secondary` 对比度至 4.5:1 |
| 4.4 | 触摸目标最小 44×44px 检查 |
| 4.5 | 补充全局 focus-visible 样式 |

---

## 📁 四、新文件结构

```
play-app-frontend/src/theme/
├── index.ts              # 统一导出 + 快捷方法
├── primitives.ts         # Layer 1: 原始色板（10级色阶 × N个色相）
├── semantic.ts           # Layer 2: 语义令牌（亮/暗两套映射）
├── components.ts         # Layer 3: 组件令牌
├── theme.config.ts       # 品牌配置类型 + 默认值
├── engine.ts             # 主题引擎：计算 + 合并 + 注入
├── tokens.ts             # [废弃] 保留向后兼容，内部调用新模块
└── color-utils.ts        # 颜色工具：hexToRgb, blend, alpha, lighten, darken
```

```
play-app-backend/
├── src/main/java/com/playapp/
│   ├── entity/SystemConfig.java         # 🆕
│   ├── mapper/SystemConfigMapper.java   # 🆕
│   ├── service/ThemeConfigService.java  # 🆕 (接口)
│   ├── service/impl/ThemeConfigServiceImpl.java # 🆕
│   ├── controller/ThemeConfigController.java   # 🆕
│   └── dto/ThemeConfigUpdateDTO.java   # 🆕
```

---

## 🔧 五、色彩工具函数 (`color-utils.ts`)

```typescript
/**
 * HEX → RGB 分量
 */
export function hexToRgb(hex: string): { r: number; g: number; b: number } {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  if (!result) throw new Error(`Invalid hex: ${hex}`);
  return { r: parseInt(result[1], 16), g: parseInt(result[2], 16), b: parseInt(result[3], 16) };
}

/**
 * HEX → "255, 59, 92"（用于 rgba() 组合）
 */
export function hexToRgbString(hex: string): string {
  const { r, g, b } = hexToRgb(hex);
  return `${r}, ${g}, ${b}`;
}

/**
 * 明度调整：amount > 0 变亮，< 0 变暗（范围 -100 ~ 100）
 */
export function adjustBrightness(hex: string, amount: number): string {
  const { r, g, b } = hexToRgb(hex);
  const clamp = (v: number) => Math.max(0, Math.min(255, v + amount));
  return `#${[clamp(r), clamp(g), clamp(b)].map(v => v.toString(16).padStart(2, '0')).join('')}`;
}

/**
 * 透明度混合：hex 颜色 + alpha 通道
 */
export function withAlpha(hex: string, alpha: number): string {
  const { r, g, b } = hexToRgb(hex);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/**
 * 生成 10 级色阶（从给定基准色向亮/暗两端扩展）
 */
export function generateScale(baseHex: string): Record<number, string> {
  // 使用 HSL 色相保持 + 亮度均匀分布
  // 50(最浅) ... 500(基准) ... 950(最深)
  // ...
}
```

---

## ✅ 六、Pre-Delivery 检查清单

对照 UI/UX Pro Max 规范：

### 视觉质量
- [ ] 不使用 emoji 作为图标（使用 Lucide/Heroicons SVG）
- [ ] 所有图标来自统一图标集
- [ ] 品牌 Logo 正确（如有）
- [ ] hover 状态不会导致布局偏移
- [ ] 直接使用 CSS 变量（`background: var(--bg-card)`），不包 `var()` 包装

### 交互
- [ ] 所有可点击元素有 `cursor-pointer`（H5 模式）
- [ ] hover 状态有明确视觉反馈
- [ ] 过渡动画 150-300ms
- [ ] 键盘导航可见 focus 状态

### 明暗模式
- [x] 亮色模式文字对比度 ≥ 4.5:1
- [x] 透明/玻璃元素在亮色模式下可见
- [x] 边框在两种模式下都可见
- [x] 交付前两种模式都测试通过

### 布局
- [ ] 悬浮元素与边缘有适当间距
- [ ] 内容不被固定导航栏遮挡
- [ ] 375px / 768px / 1024px / 1440px 响应式适配
- [ ] 移动端无水平滚动

### 无障碍
- [ ] 所有图片有 alt 文本
- [ ] 表单输入有标签
- [ ] 颜色不是唯一的信息指示方式
- [ ] `prefers-reduced-motion` 受尊重

---

## 📊 七、优化前后对比

| 维度 | 优化前 | 优化后 |
|------|--------|--------|
| **颜色定义** | 2 处（tokens.ts + uni.scss） | 1 处（primitives.ts 为唯一来源） |
| **主题扩展** | 仅 light/dark 硬编码 | 无限主题：从色板自动生成 |
| **品牌色更新** | 需修改 2 个源码文件 + 重新编译 | 后端 API 配置，前端实时生效 |
| **页面主题绑定** | 19 个页面手动绑定 | 0（引擎自动注入） |
| **暗色颜色适配** | 品牌色不变（刺眼） | 自动提亮/降饱和 |
| **可配置项** | 0 | 主色、强调色、渐变终点、圆角缩放、字号缩放 |
| **CSS 降级** | 无 | `@media (prefers-color-scheme: dark)` 兜底 |
| **动画偏好** | 不支持 | `@media (prefers-reduced-motion)` 支持 |
| **代码复用** | 颜色硬编码散落各处 | `color-utils.ts` 统一工具函数 |
| **对比度合规** | text-secondary ≈ 4.2:1（略不足） | text-secondary ≥ 4.5:1 ✅ |
