import { request } from '../utils/request'
import type { PlayRequest } from '../types/companion'
import type { PageResult } from '../types/api'

export const publicApi = {
  /** 分类列表 */
  getCategories() {
    return request<any[]>({ url: '/categories', method: 'GET' })
  },
  /** 需求列表 */
  getRequests(current = 1, size = 20) {
    return request<PageResult<PlayRequest>>({ url: '/requests', method: 'GET', data: { current, size } })
  },
  /** 我的需求 */
  getMyRequests(current = 1, size = 20) {
    return request<PageResult<PlayRequest>>({ url: '/requests/mine', method: 'GET', data: { current, size } })
  },
  /** 发布需求 */
  publishRequest(data: { description: string; reserveTime?: string; address?: string; budget?: number }) {
    return request<PlayRequest>({ url: '/requests', method: 'POST', data })
  },
  /** 提交风控举报 */
  submitReport(data: { targetType: string; targetId: number; orderId?: number; reason: string; description: string; evidenceUrls?: string[] }) {
    return request<void>({ url: '/risk/reports', method: 'POST', data })
  }
}
