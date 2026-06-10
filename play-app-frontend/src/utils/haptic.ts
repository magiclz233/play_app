/**
 * 触觉反馈工具
 *
 * 用法：
 *   haptic.light()   — 轻反馈（按钮点击、tab 切换）
 *   haptic.medium()  — 中等反馈（表单提交、确认操作）
 *   haptic.heavy()   — 重反馈（支付、提现等关键操作）
 *   haptic.success() — 成功反馈模式
 */

type HapticType = 'light' | 'medium' | 'heavy'

function vibrate(type: HapticType = 'light'): void {
  try {
    // #ifdef MP-WEIXIN
    uni.vibrateShort({ type } as any)
    // #endif
    // #ifdef H5
    // H5 环境无振动 API，静默降级
    // #endif
  } catch {
    // 降级：设备不支持振动时静默忽略
  }
}

export const haptic = {
  /** 轻反馈 — 按钮点击、tab 切换、卡片选中 */
  light: () => vibrate('light'),

  /** 中等反馈 — 表单提交、确认操作 */
  medium: () => vibrate('medium'),

  /** 重反馈 — 支付成功、提现申请、审核通过等关键操作 */
  heavy: () => vibrate('heavy'),

  /** 成功操作模式：两次轻振 */
  success: () => {
    vibrate('light')
    setTimeout(() => vibrate('light'), 100)
  },

  /** 错误操作模式：一次重振 */
  error: () => vibrate('heavy'),
}
