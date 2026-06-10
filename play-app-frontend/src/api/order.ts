import { request } from '../utils/request'
import type { PageResult } from '../types/api'
import type { OrderVO, OrderStatus, OrderCreateDTO, OrderCreateVO, ReviewVO, TimelineEvent } from '../types/order'

export const orderApi = {
  /** 创建订单 */
  create(data: OrderCreateDTO) {
    return request<OrderCreateVO>({ url: '/orders', method: 'POST', data })
  },
  /** 获取支付参数 */
  prepay(orderNo: string) {
    return request<any>({ url: `/orders/${orderNo}/prepay`, method: 'POST' })
  },
  /** 订单列表 */
  getList(params: { current?: number; size?: number; status?: OrderStatus }) {
    return request<PageResult<OrderVO>>({ url: '/orders', method: 'GET', data: params })
  },
  /** 订单详情 */
  getDetail(orderNo: string) {
    return request<OrderVO>({ url: `/orders/${orderNo}`, method: 'GET' })
  },
  /** 确认完工 */
  confirm(orderNo: string) {
    return request<void>({ url: `/orders/${orderNo}/confirm`, method: 'PUT' })
  },
  /** 提交评价 */
  review(orderNo: string, data: { rating: number; content?: string; images?: string; isAnonymous?: boolean }) {
    return request<void>({ url: `/orders/${orderNo}/review`, method: 'POST', data })
  },
  /** 取消订单 */
  cancel(orderNo: string, reason: string) {
    return request<void>({ url: `/orders/${orderNo}/cancel`, method: 'PUT', data: { reason } })
  },
  /** 申请退款 */
  refund(orderNo: string, reason: string) {
    return request<void>({ url: `/orders/${orderNo}/refund`, method: 'POST', data: { reason } })
  },
  /** 获取时间线 */
  getTimeline(orderNo: string) {
    return request<TimelineEvent[]>({ url: `/orders/${orderNo}/timeline`, method: 'GET' })
  },
  /** 添加时间线事件 */
  addTimelineEvent(orderNo: string, data: { eventType: number; eventDesc: string; fileUrls?: string[]; operatorRole?: number }) {
    return request<TimelineEvent>({ url: `/orders/${orderNo}/timeline`, method: 'POST', data })
  },
  /** 发起纠纷 */
  dispute(orderNo: string, data: { orderNo: string; reasonType: string; description: string; evidenceUrls?: string[] }) {
    return request<void>({ url: `/orders/${orderNo}/dispute`, method: 'POST', data })
  }
}
