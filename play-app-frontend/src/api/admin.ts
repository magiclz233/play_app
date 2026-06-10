import { request } from '../utils/request'
import type { PageResult } from '../types/api'

export const adminApi = {
  /** 统计大盘 */
  getDashboard() {
    return request<any>({ url: '/admin/dashboard', method: 'GET' })
  },
  /** 待审核陪玩列表 */
  getPendingCompanions() {
    return request<any[]>({ url: '/admin/companions/pending', method: 'GET' })
  },
  /** 审核陪玩 */
  auditCompanion(id: number, data: { approved: boolean; rejectReason?: string }) {
    return request<void>({ url: `/admin/companions/${id}/audit`, method: 'PUT', data })
  },
  /** 订单列表 */
  getOrders(params: { current?: number; size?: number; status?: number }) {
    return request<PageResult<any>>({ url: '/admin/orders', method: 'GET', data: params })
  },
  /** 标记拉群 */
  markGroupCreated(orderNo: string, remark?: string) {
    return request<void>({ url: `/admin/orders/${orderNo}/group-created`, method: 'PUT', data: { remark } })
  },
  /** 标记服务开始 */
  startService(orderNo: string, data: { actualAddress?: string; remark?: string }) {
    return request<void>({ url: `/admin/orders/${orderNo}/start`, method: 'PUT', data })
  },
  /** 核销完工 */
  confirmFinish(orderNo: string, data: { finishRemark?: string; finishType?: number }) {
    return request<void>({ url: `/admin/orders/${orderNo}/finish`, method: 'PUT', data })
  },
  /** 结算放款 */
  settle(orderNo: string, remark?: string) {
    return request<void>({ url: `/admin/orders/${orderNo}/settle`, method: 'PUT', data: { remark } })
  },
  /** 退款审批 */
  approveRefund(orderNo: string, remark?: string) {
    return request<void>({ url: `/admin/orders/${orderNo}/refund`, method: 'PUT', data: { remark } })
  },
  /** 处理纠纷 */
  resolveDispute(data: { orderNo: string; resolution: string; refundAmount?: number; remark?: string }) {
    return request<void>({ url: '/admin/orders/dispute', method: 'PUT', data })
  },
  /** 提现列表 */
  getWithdrawals(status?: number) {
    return request<any[]>({ url: '/admin/withdrawals', method: 'GET', data: { status } })
  },
  /** 审核提现 */
  auditWithdrawal(id: number, data: { status: number; remark?: string }) {
    return request<void>({ url: `/admin/withdrawals/${id}/audit`, method: 'PUT', data })
  },
  /** 用户列表 */
  getUsers(params: { current?: number; size?: number; keyword?: string; status?: number }) {
    return request<PageResult<any>>({ url: '/admin/users', method: 'GET', data: params })
  },
  /** 禁用/启用用户 */
  updateUserStatus(id: number, status: number) {
    return request<void>({ url: `/admin/users/${id}/status`, method: 'PUT', data: { status } })
  },
  /** 需求列表 */
  getRequests() {
    return request<any[]>({ url: '/admin/requests', method: 'GET' })
  },
  /** 更新需求 */
  updateRequest(id: number, data: any) {
    return request<void>({ url: `/admin/requests/${id}`, method: 'PUT', data })
  },
  /** 标签管理 */
  getTags() { return request<any[]>({ url: '/admin/tags', method: 'GET' }) },
  createTag(data: any) { return request<any>({ url: '/admin/tags', method: 'POST', data }) },
  updateTag(id: number, data: any) { return request<void>({ url: `/admin/tags/${id}`, method: 'PUT', data }) },
  deleteTag(id: number) { return request<void>({ url: `/admin/tags/${id}`, method: 'DELETE' }) },
  /** 风控举报 */
  getRiskReports(status?: number) {
    return request<any[]>({ url: '/admin/risk/reports', method: 'GET', data: { status } })
  },
  handleRiskReport(id: number, data: { status: number; handleResult?: string }) {
    return request<void>({ url: `/admin/risk/reports/${id}`, method: 'PUT', data })
  }
}
