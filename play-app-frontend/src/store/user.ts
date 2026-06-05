import {computed, ref} from 'vue';
import {defineStore} from 'pinia';
import {request} from '../utils/request';

export const useUserStore = defineStore('user', () => {
    const token = ref<string>(uni.getStorageSync('token') || '');
    const userInfo = ref<any>(uni.getStorageSync('userInfo') || null);

    const isLoggedIn = computed(() => !!token.value);

    // 登录成功后设置状态
    const login = (newToken: string, user: any) => {
        token.value = newToken;
        userInfo.value = user;
        uni.setStorageSync('token', newToken);
        uni.setStorageSync('userInfo', user);
    };

    // 仅设置 token
    const setToken = (newToken: string) => {
        token.value = newToken;
        uni.setStorageSync('token', newToken);
    };

    // 仅设置用户信息
    const setUserInfo = (user: any) => {
        userInfo.value = user;
        uni.setStorageSync('userInfo', user);
    };

    // 退出登录
    const logout = () => {
        token.value = '';
        userInfo.value = null;
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
    };

    // 微信小程序静默登录
    const wxLogin = async () => {
        try {
            const [loginRes] = await uni.login({ provider: 'weixin' });
            if (loginRes.code) {
                const res = await request({
                    url: '/wx/login',
                    method: 'POST',
                    data: { code: loginRes.code }
                });
                if (res.code === 200) {
                    login(res.data.token, res.data.user);
                    return true;
                }
            }
            return false;
        } catch (error) {
            console.error('Login failed', error);
            return false;
        }
    };

    return {
        token,
        userInfo,
        isLoggedIn,
        login,
        setToken,
        setUserInfo,
        logout,
        wxLogin
    };
});
