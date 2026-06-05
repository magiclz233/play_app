export const BASE_URL = 'http://127.0.0.1:8080/api';
const TIMEOUT = 10000; // 10秒超时

export interface Result<T = any> {
    code: number;
    message: string;
    data: T;
}

let showingNetworkError = false;

export const request = <T = any>(options: UniApp.RequestOptions): Promise<Result<T>> => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token');

        const header: Record<string, string> = {
            'Content-Type': 'application/json',
            ...(options.header as Record<string, string> || {})
        };

        if (token) {
            header['Authorization'] = `Bearer ${token}`;
        }

        uni.request({
            url: options.url.startsWith('http') ? options.url : `${BASE_URL}${options.url}`,
            method: options.method || 'GET',
            data: options.data,
            header: header,
            timeout: TIMEOUT,
            success: (res) => {
                showingNetworkError = false;
                const data = res.data as Result<T>;
                if (data.code === 200) {
                    resolve(data);
                } else if (data.code === 401) {
                    uni.removeStorageSync('token');
                    uni.showToast({ title: '请先登录', icon: 'none' });
                    reject(data);
                } else {
                    uni.showToast({ title: data.message || '请求失败', icon: 'none', duration: 2000 });
                    reject(data);
                }
            },
            fail: (err) => {
                // 避免重复弹窗
                if (!showingNetworkError) {
                    showingNetworkError = true;
                    uni.showToast({ title: '无法连接服务器，请确认后端已启动', icon: 'none', duration: 3000 });
                    setTimeout(() => { showingNetworkError = false; }, 3000);
                }
                console.error('[Request Failed]', options.url, err);
                reject(err);
            }
        });
    });
};
