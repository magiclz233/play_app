<script setup lang="ts">
import { onHide, onLaunch, onShow } from "@dcloudio/uni-app";
import { useUserStore } from "./store/user";
import { useAppStore } from "./store/app";
import { applyLocaleToNativeUi } from "./utils/i18n";

onLaunch(async () => {
  console.log("App Launch");

  // 1. 初始化主题引擎（处理明暗模式 + 品牌色配置）
  const appStore = useAppStore();
  appStore.initApp();

  // 2. 初始化多语言
  applyLocaleToNativeUi();
  uni.$on('locale-changed', applyLocaleToNativeUi);

  // 3. 自动登录
  const userStore = useUserStore();
  if (!userStore.token) {
    // #ifdef H5
    console.log("H5 mode: skip auto wxLogin, use mock login on mine page");
    // #endif
    // #ifndef H5
    console.log("No token, attempting auto wxLogin...");
    await userStore.wxLogin();
    // #endif
  }
});

onShow(() => {
  console.log("App Show");
  // 每次 App 回到前台时，重新应用原生 UI 主题（以防被系统覆盖）
  const appStore = useAppStore();
  appStore.applyThemeToNativeUi();
  applyLocaleToNativeUi();
});

onHide(() => {
  console.log("App Hide");
});
</script>

<style>
/* 每个页面公共css */
page {
  background-color: var(--bg-main);
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica,
    Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei',
    sans-serif;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  line-height: 1.6;
  box-sizing: border-box;
  transition: background-color 0.2s ease, color 0.2s ease;
  min-height: 100vh;
  max-width: 100vw;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
}

/* ==========================================================================
   触摸目标最小尺寸保障（WCAG 2.5.5: 最小 44×44px ≈ 88rpx）
   ========================================================================== */
button,
[role="button"],
.clickable,
.icon-btn,
.tab-item,
.grid-item,
.category-item,
.switch-item,
.nav-item {
  min-width: 88rpx;
  min-height: 88rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 确保可点击元素有视觉反馈 */
.clickable:active,
.icon-btn:active,
.tab-item:active,
.grid-item:active,
.category-item:active,
.switch-item:active {
  opacity: 0.7;
}

/* ==========================================================================
   全局按钮样式（基于组件令牌）
   ========================================================================== */
button[type="primary"]:not([plain]) {
  background: var(--btn-primary-bg) !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: var(--btn-primary-shadow) !important;
  border-radius: var(--btn-radius, var(--radius-full)) !important;
}

button[type="primary"][plain] {
  background-color: var(--btn-outline-bg) !important;
  border-color: var(--btn-outline-border) !important;
  color: var(--btn-outline-color) !important;
  box-shadow: var(--btn-outline-shadow, 0 2px 8px rgba(var(--color-primary-rgb), 0.1)) !important;
  border-radius: var(--btn-radius, var(--radius-full)) !important;
}

/* 按钮通用交互增强 */
button {
  transition: opacity 0.2s ease, transform 0.15s ease;
}

button:active {
  transform: scale(0.97);
}

/* 禁用状态 */
button[disabled] {
  opacity: 0.5;
  transform: none;
}

/* ==========================================================================
   全局卡片华贵质感提升 (Premium Component Styling)
   ========================================================================== */
view[class*="card"], .u-card {
  background-color: var(--card-bg, var(--bg-card));
  background-image: linear-gradient(145deg, rgba(255, 255, 255, 0.03) 0%, transparent 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.06),
    inset 0 0 0 1px var(--border-color),
    var(--card-shadow, var(--shadow-card)) !important;
  border: none !important;
  position: relative;
  border-radius: var(--card-radius, var(--radius-lg));
}

/* ==========================================================================
   全局 focus-visible 样式（键盘导航可见聚焦指示）
   ========================================================================== */
button:focus-visible,
input:focus-visible,
textarea:focus-visible,
[tabindex]:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* ==========================================================================
   全局过渡 — 仅过渡颜色相关属性（遵循 "No Transition All" 规范）
   ========================================================================== */
page, view, text, button, input, textarea {
  transition: background-color 0.2s ease,
              color 0.2s ease,
              border-color 0.2s ease,
              box-shadow 0.2s ease;
}

/* ==========================================================================
   尊重系统动画偏好
   ========================================================================== */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }

  .skeleton-block,
  .skeleton-line {
    animation: none !important;
    background: rgba(var(--color-primary-rgb), 0.06) !important;
  }
}
</style>
