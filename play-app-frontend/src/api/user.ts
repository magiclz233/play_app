import { request } from '../utils/request'

export const userApi = {
  /** 获取个人信息 */
  getProfile() {
    return request<any>({ url: '/user/profile', method: 'GET' })
  },
  /** 更新个人信息 */
  updateProfile(data: Record<string, any>) {
    return request<void>({ url: '/user/profile', method: 'PUT', data })
  },
  /** 微信登录 */
  wxLogin(code: string) {
    return request<{ token: string; user: any }>({ url: '/wx/login', method: 'POST', data: { code } })
  },
  /** Mock登录（开发用） */
  mockLogin(phone: string) {
    return request<{ token: string; user: any }>({ url: '/wx/mock-login', method: 'POST', data: { phone } })
  },
  /** 支付流水 */
  getPayments(current = 1, size = 20) {
    return request<any>({ url: '/payments', method: 'GET', data: { current, size } })
  },
  /** 上传文件 */
  uploadFile(filePath: string) {
    return new Promise<string>((resolve, reject) => {
      uni.uploadFile({
        url: 'http://127.0.0.1:8080/api/file/upload',
        filePath,
        name: 'file',
        success: (res: any) => {
          const data = JSON.parse(res.data)
          if (data.code === 200) resolve(data.data)
          else reject(new Error(data.message))
        },
        fail: reject
      })
    })
  }
}
