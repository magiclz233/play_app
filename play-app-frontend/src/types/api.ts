/** 统一响应体 */
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
