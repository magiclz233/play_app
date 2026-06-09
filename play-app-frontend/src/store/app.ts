import {ref, computed} from 'vue';
import {defineStore} from 'pinia';
import {themeTokens, type ThemeMode, type ThemeName} from '../theme/tokens';

export const useAppStore = defineStore('app', () => {
    // 主题设置模式: 'auto' (跟随系统) | 'light' (锁定浅色) | 'dark' (锁定深色)
    const themeMode = ref<ThemeMode>(uni.getStorageSync('themeMode') || 'light');
    // 当前正在生效的实际明暗色: 'light' | 'dark'
    const currentTheme = ref<ThemeName>('light');

    // 语言偏好: 'zh-Hans' (简中) | 'zh-Hant' (繁中) | 'en' (英文)
    const locale = ref<'zh-Hans' | 'zh-Hant' | 'en'>(uni.getStorageSync('locale') || 'zh-Hans');

    // 格式化后的类名，方便页面直接绑定: 'theme-light' | 'theme-dark'
    const themeClass = computed(() => `theme-${currentTheme.value}`);
    const themeStyle = computed(() => themeTokens[currentTheme.value].cssVars);

    const applyThemeToNativeUi = () => {
        const tokens = themeTokens[currentTheme.value];
        uni.setNavigationBarColor({
            ...tokens.navigationBar,
            animation: { duration: 200, timingFunc: 'easeIn' }
        });
        uni.setTabBarStyle(tokens.tabBar);
    };

    // 更新实际明暗色主题
    const updateActualTheme = (systemTheme?: ThemeName) => {
        if (themeMode.value === 'auto') {
            // 跟随系统
            const sysTheme = systemTheme || uni.getSystemInfoSync().theme || 'dark';
            currentTheme.value = sysTheme === 'light' ? 'light' : 'dark';
        } else {
            // 手动锁定
            currentTheme.value = themeMode.value;
        }
        applyThemeToNativeUi();
    };

    // 切换主题模式
    const setThemeMode = (mode: ThemeMode) => {
        themeMode.value = mode;
        uni.setStorageSync('themeMode', mode);
        updateActualTheme();
    };

    const toggleTheme = () => {
        setThemeMode(currentTheme.value === 'light' ? 'dark' : 'light');
    };

    // 切换多语言
    const setLocale = (lang: 'zh-Hans' | 'zh-Hant' | 'en') => {
        locale.value = lang;
        uni.setStorageSync('locale', lang);
        // 通知多语言框架更新翻译字典
        uni.$emit('locale-changed', lang);
    };

    // 初始化应用配置
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

        // 2. 初始化明暗主题
        updateActualTheme();

        // 3. 监听系统主题变化
        uni.onThemeChange((res) => {
            if (themeMode.value === 'auto') {
                updateActualTheme(res.theme as 'light' | 'dark');
            }
        });
    };

    return {
        themeMode,
        currentTheme,
        themeClass,
        themeStyle,
        locale,
        setThemeMode,
        toggleTheme,
        setLocale,
        updateActualTheme,
        applyThemeToNativeUi,
        initApp
    };
});
