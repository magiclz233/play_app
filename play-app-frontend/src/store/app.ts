import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { themeEngine } from '../theme/engine';
import type { ThemeMode, ThemeName, ThemeConfig } from '../theme/theme.config';

/**
 * 应用全局状态管理
 *
 * 职责：
 * - 主题模式（auto/light/dark）状态管理
 * - 多语言偏好（zh-Hans/zh-Hant/en）
 * - 初始化时委托 themeEngine 注入 CSS 变量 + 监听系统主题变化
 *
 * 相比旧版的改进：
 * - 不再手动维护 CSS 变量表（由 themeEngine 统一计算）
 * - 不再需要页面绑定 :style="appStore.themeStyle"（由引擎自动注入）
 * - 品牌色可从远程 API 加载并合并
 */

export const useAppStore = defineStore('app', () => {
    // ============================================================
    // 主题状态
    // ============================================================

    /** 主题设置模式: 'auto' (跟随系统) | 'light' | 'dark' */
    const themeMode = ref<ThemeMode>(
        (uni.getStorageSync('themeMode') as ThemeMode) || 'light'
    );

    /** 当前正在生效的实际明暗色 */
    const currentTheme = ref<ThemeName>(themeEngine.getCurrentTheme());

    /** 是否为暗色模式（便捷计算属性） */
    const isDark = computed(() => currentTheme.value === 'dark');

    // ============================================================
    // 语言状态
    // ============================================================

    const locale = ref<'zh-Hans' | 'zh-Hant' | 'en'>(
        uni.getStorageSync('locale') || 'zh-Hans'
    );

    // ============================================================
    // 主题操作
    // ============================================================

    /** 更新原生 UI 组件颜色（NavigationBar、TabBar） */
    const applyThemeToNativeUi = () => {
        // themeEngine.apply() 内部已包含 applyNativeUi 调用
        // 此方法保留用于 onShow 时的刷新
        const vars = themeEngine.getCssVars();
        const isDarkMode = themeEngine.isDark();

        try {
            uni.setNavigationBarColor({
                frontColor: isDarkMode ? '#ffffff' : '#000000',
                backgroundColor: vars['--bg-main'] || (isDarkMode ? '#121216' : '#FAF5F5'),
                animation: { duration: 200, timingFunc: 'easeIn' },
            });
            uni.setTabBarStyle({
                color: vars['--text-muted'] || (isDarkMode ? '#737887' : '#A1A5B1'),
                selectedColor: vars['--color-primary'] || '#FF3B5C',
                backgroundColor: vars['--bg-card'] || (isDarkMode ? '#24262D' : '#FFFFFF'),
                borderStyle: isDarkMode ? 'white' : 'black',
            });
        } catch (e) {
            // 某些页面可能没有 TabBar，忽略错误
        }
    };

    /** 切换主题模式 */
    const setThemeMode = (mode: ThemeMode) => {
        themeMode.value = mode;
        uni.setStorageSync('themeMode', mode);

        if (mode === 'auto') {
            // 跟随系统
            try {
                const sysInfo = uni.getSystemInfoSync();
                const sysTheme: ThemeName = sysInfo.theme === 'dark' ? 'dark' : 'light';
                themeEngine.setTheme(sysTheme);
                currentTheme.value = sysTheme;
            } catch {
                themeEngine.setTheme('light');
                currentTheme.value = 'light';
            }
        } else {
            themeEngine.setTheme(mode);
            currentTheme.value = mode;
        }
    };

    /** 切换明暗主题 */
    const toggleTheme = () => {
        const next = currentTheme.value === 'light' ? 'dark' : 'light';
        setThemeMode(next);
    };

    /** 更新品牌配置（从远程 API 加载后调用） */
    const updateBrandConfig = (config: Partial<ThemeConfig>) => {
        themeEngine.setConfig(config);
    };

    // ============================================================
    // 语言操作
    // ============================================================

    const setLocale = (lang: 'zh-Hans' | 'zh-Hant' | 'en') => {
        locale.value = lang;
        uni.setStorageSync('locale', lang);
        uni.$emit('locale-changed', lang);
    };

    // ============================================================
    // 初始化
    // ============================================================

    const initApp = () => {
        // 1. 初始化多语言
        let savedLocale = uni.getStorageSync('locale');
        if (!savedLocale) {
            const sysLang = uni.getSystemInfoSync().language || '';
            if (sysLang.toLowerCase().includes('hant') || sysLang.includes('hk') || sysLang.includes('tw')) {
                locale.value = 'zh-Hant';
            } else if (sysLang.toLowerCase().includes('en')) {
                locale.value = 'en';
            } else {
                locale.value = 'zh-Hans';
            }
            uni.setStorageSync('locale', locale.value);
        }

        // 2. 初始化主题引擎
        const savedMode = uni.getStorageSync('themeMode') as ThemeMode | '';
        themeMode.value = savedMode || 'light';

        // 初始化引擎（如果尚未初始化）
        themeEngine.init(themeMode.value);

        // 同步当前主题状态
        currentTheme.value = themeEngine.getCurrentTheme();

        // 3. 监听引擎主题变更
        themeEngine.onChange((newTheme) => {
            currentTheme.value = newTheme;
        });

        // 4. 尝试加载远程品牌配置
        loadRemoteThemeConfig();
    };

    /**
     * 从后端加载品牌主题配置
     */
    const loadRemoteThemeConfig = async () => {
        try {
            // 动态导入 request 避免循环依赖
            const { request } = await import('../utils/request');
            const res = await request({
                url: '/public/theme-config',
                method: 'GET',
            });
            if (res.code === 200 && res.data) {
                const { mapRemoteConfig } = await import('../theme/theme.config');
                const partialConfig = mapRemoteConfig(res.data);
                if (Object.keys(partialConfig).length > 0) {
                    themeEngine.setConfig(partialConfig);
                }
            }
        } catch {
            // 网络失败或接口不存在时使用默认配置，静默处理
            console.debug('[AppStore] Failed to load remote theme config, using defaults');
        }
    };

    return {
        // 状态
        themeMode,
        currentTheme,
        isDark,
        locale,

        // 主题操作
        setThemeMode,
        toggleTheme,
        updateBrandConfig,
        applyThemeToNativeUi,

        // 语言
        setLocale,

        // 生命周期
        initApp,
    };
});
