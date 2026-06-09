<script setup lang="ts">
import {onHide, onLaunch, onShow} from "@dcloudio/uni-app";
import {useUserStore} from "./store/user";
import {useAppStore} from "./store/app";
import {applyLocaleToNativeUi} from "./utils/i18n";

onLaunch(async () => {
  console.log("App Launch");
  const appStore = useAppStore();
  appStore.initApp();
  applyLocaleToNativeUi();
  uni.$on('locale-changed', applyLocaleToNativeUi);

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
  font-size: 28rpx;
  box-sizing: border-box;
  transition: background-color 0.2s ease, color 0.2s ease;
  min-height: 100vh;
}

/* 全局炫酷按钮样式优化 */
button[type="primary"]:not([plain]) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-gradient-end)) !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: 0 6px 16px rgba(var(--color-primary-rgb), 0.3) !important;
  border-radius: var(--radius-full, 999rpx) !important;
}

button[type="primary"][plain] {
  background-color: var(--color-primary-light) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
  box-shadow: 0 2px 8px rgba(var(--color-primary-rgb), 0.1) !important;
  border-radius: var(--radius-full, 999rpx) !important;
}

/* 全局卡片华贵质感提升 (Premium Component Styling) */
view[class*="card"], .u-card {
  background-color: var(--bg-card);
  /* 增加微妙的表面材质反光 (Subtle surface reflection) */
  background-image: linear-gradient(145deg, rgba(255, 255, 255, 0.03) 0%, transparent 100%);
  /* 内部顶部高光与发光阴影结合 (Inner highlight + tinted drop shadow) */
  box-shadow: 
    inset 0 1px 1px rgba(255, 255, 255, 0.08), 
    inset 0 0 0 1px var(--border-color),
    var(--shadow-card) !important;
  border: none !important; /* 移除生硬的原生边框，交由 box-shadow 处理 */
  position: relative;
  border-radius: var(--radius-lg, 16rpx);
}
</style>
