import {computed, ref} from 'vue';
import {defineStore} from 'pinia';
import {request} from '../utils/request';

/**
 * 从 JWT Token 中解码 payload（不验证签名）
 */
function decodeJwtPayload(token: string): Record<string, any> | null {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return null;
        // JWT payload 是 Base64URL 编码的
        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
        const json = uni.base64ToArrayBuffer
            ? new TextDecoder().decode(uni.base64ToArrayBuffer(base64))
            : decodeURIComponent(escape(atob(base64))); // 兼容旧版本
        return JSON.parse(json);
    } catch {
        return null;
    }
}

export const useUserStore = defineStore('user', () => {
    const token = ref<string>(uni.getStorageSync('token') || '');
    const userInfo = ref<any>(uni.getStorageSync('userInfo') || null);

    const isLoggedIn = computed(() => !!token.value);

    /** 从 JWT 中解析角色列表 */
    const userRoles = computed<number[]>(() => {
        if (!token.value) return [];
        const payload = decodeJwtPayload(token.value);
        if (!payload) return [];
        // 优先读取 roles 数组（多角色），回退到单 role
        if (payload.roles && Array.isArray(payload.roles)) {
            return payload.roles.map(Number);
        }
        if (payload.role != null) {
            return [Number(payload.role)];
        }
        return [];
    });

    /** 是否为管理员（roleId=3 或拥有 ROLE_ADMIN） */
    const isAdmin = computed(() => userRoles.value.includes(3));

    /** 是否为陪玩 */
    const isCompanion = computed(() => userRoles.value.includes(2));

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
        userRoles,
        isAdmin,
        isCompanion,
        login,
        setToken,
        setUserInfo,
        logout,
        wxLogin
    };
});
