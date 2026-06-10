/** 订单状态 */
export type OrderStatus = 10 | 20 | 30 | 40 | 50 | 60 | 70 | 80 | 100 | 110 | 120 | 130 | 200 | 210 | 250

/** 订单对象 */
export interface OrderVO {
  orderId: number
  orderNo: string
  userId: number
  companionId: number
  categoryId: number
  status: OrderStatus
  hours: number
  pricePerHour: number
  totalAmount: number
  companionAmount: number
  platformFee: number
  reserveDate: string
  reserveTimeStart: string
  reserveTimeEnd?: string
  address: string
  addressDetail?: string
  customerWechat?: string
  customerRemark?: string
  wechatGroupStatus: number
  transactionId?: string
  payTime?: string
  cancelReason?: string
  cancelType?: number
  refundAmount?: number
  finishTime?: string
  extra?: Record<string, any>
  createTime: string
  updateTime?: string
}

/** 创建订单请求 */
export interface OrderCreateDTO {
  companionId: number
  serviceId: number
  hours: number
  appointmentTime: string
  address: string
  addressDetail?: string
  customerWechat?: string
  remark?: string
}

/** 创建订单响应 */
export interface OrderCreateVO {
  orderNo: string
  totalAmount: number
  platformFee: number
  companionAmount: number
}

/** 服务时间线事件 */
export interface TimelineEvent {
  id: number
  orderId: number
  eventType: number
  eventDesc?: string
  operatorId: number
  operatorRole: number
  createTime: string
}

/** 评价对象 */
export interface ReviewVO {
  id: number
  orderId: number
  userId: number
  companionId: number
  rating: number
  content?: string
  images?: string
  tagIds?: number[]
  isAnonymous: boolean
  replyContent?: string
  replyTime?: string
  status: number
  createTime: string
}
