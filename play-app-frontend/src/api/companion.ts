import { request } from '../utils/request'
import type { PageResult } from '../types/api'
import type {
  CompanionVO, CompanionDetailVO, CompanionAlbum, CompanionSkill,
  CompanionWallet, WalletTransaction, PlayRequest, ReviewVO
} from '../types/companion'

export const companionApi = {
  /** 陪玩大厅列表 */
  getList(params: { current?: number; size?: number; categoryId?: number; keyword?: string; gender?: number; sortBy?: string }) {
    return request<PageResult<CompanionVO>>({ url: '/companions', method: 'GET', data: params })
  },
  /** 推荐陪玩 */
  getRecommended() {
    return request<CompanionVO[]>({ url: '/companions/recommended', method: 'GET' })
  },
  /** 陪玩详情 */
  getDetail(id: number) {
    return request<CompanionDetailVO>({ url: `/companions/${id}`, method: 'GET' })
  },
  /** 陪玩评价列表 */
  getReviews(companionId: number, current = 1, size = 20) {
    return request<PageResult<ReviewVO>>({ url: `/companions/${companionId}/reviews`, method: 'GET', data: { current, size } })
  },
  /** 入驻申请 */
  apply(data: any) {
    return request<void>({ url: '/companion/apply', method: 'POST', data })
  },
  /** 申请状态 */
  getApplyStatus() {
    return request<any>({ url: '/companion/apply/status', method: 'GET' })
  },
  /** 工作台数据 */
  getDashboard() {
    return request<any>({ url: '/companion/dashboard', method: 'GET' })
  },
  /** 陪玩订单列表 */
  getOrders(params: { current?: number; size?: number; status?: number }) {
    return request<PageResult<any>>({ url: '/companion/orders', method: 'GET', data: params })
  },
  /** 接单 */
  acceptOrder(orderNo: string) {
    return request<void>({ url: `/companion/orders/${orderNo}/accept`, method: 'PUT' })
  },
  /** 拒单 */
  rejectOrder(orderNo: string, reason: string) {
    return request<void>({ url: `/companion/orders/${orderNo}/reject`, method: 'PUT', data: { reason } })
  },
  /** 发起完工 */
  requestFinish(orderNo: string, data: { finishRemark: string; finishType?: number; actualDuration?: number }) {
    return request<void>({ url: `/companion/orders/${orderNo}/request-finish`, method: 'PUT', data })
  },
  /** 钱包 */
  getWallet() { return request<CompanionWallet>({ url: '/companion/wallet', method: 'GET' }) },
  /** 交易记录 */
  getTransactions() { return request<WalletTransaction[]>({ url: '/companion/wallet/transactions', method: 'GET' }) },
  /** 申请提现 */
  withdraw(data: { amount: number; realName: string }) {
    return request<void>({ url: '/companion/withdraw', method: 'POST', data })
  },
  /** 相册 */
  getAlbums() { return request<CompanionAlbum[]>({ url: '/companion/albums', method: 'GET' }) },
  addPhoto(data: { imageUrl: string; thumbnailUrl?: string }) {
    return request<CompanionAlbum>({ url: '/companion/albums', method: 'POST', data })
  },
  setCover(id: number) { return request<void>({ url: `/companion/albums/${id}/cover`, method: 'PUT' }) },
  deletePhoto(id: number) { return request<void>({ url: `/companion/albums/${id}`, method: 'DELETE' }) },
  /** 技能 */
  getSkills() { return request<CompanionSkill[]>({ url: '/companion/skills', method: 'GET' }) },
  addSkill(data: { categoryId: number; pricePerHour: number; experienceDesc?: string }) {
    return request<CompanionSkill>({ url: '/companion/skills', method: 'POST', data })
  },
  updateSkill(id: number, data: { pricePerHour?: number; experienceDesc?: string }) {
    return request<void>({ url: `/companion/skills/${id}`, method: 'PUT', data })
  },
  deleteSkill(id: number) { return request<void>({ url: `/companion/skills/${id}`, method: 'DELETE' }) },
  /** 标签 */
  getAllTags() { return request<any[]>({ url: '/companion/tags', method: 'GET' }) },
  updateTags(tagIds: number[]) { return request<void>({ url: '/companion/tags', method: 'PUT', data: { tagIds } }) },
  /** 回复评价 */
  replyReview(reviewId: number, content: string) {
    return request<void>({ url: `/reviews/${reviewId}/reply`, method: 'POST', data: { content } })
  },
  /** 收款流水 */
  getPayments(current = 1, size = 20) {
    return request<PageResult<any>>({ url: '/companion/payments', method: 'GET', data: { current, size } })
  }
}
