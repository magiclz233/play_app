/**
 * 数字滚动动画 (Count-Up)
 *
 * 兼容微信小程序（使用 setTimeout 替代 requestAnimationFrame）
 *
 * 用法：
 *   const counter = countUp(0, 1234.56, (val) => { display.value = val })
 *   counter.start()
 */

export function countUp(
  from: number,
  to: number,
  onTick: (display: string) => void,
  duration = 800
) {
  const startTime = Date.now()
  const diff = to - from
  const decimals = (String(to).split('.')[1] || '').length
  let timerId = 0
  let stopped = false

  const easeOut = (t: number) => 1 - Math.pow(1 - t, 3)

  function tick() {
    if (stopped) return

    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = easeOut(progress)
    const current = from + diff * eased

    const display = decimals > 0
      ? current.toFixed(decimals)
      : String(Math.round(current))

    onTick(display)

    if (progress < 1) {
      timerId = setTimeout(tick, 16) as unknown as number
    }
  }

  return {
    start() { timerId = setTimeout(tick, 16) as unknown as number },
    stop() { stopped = true; if (timerId) clearTimeout(timerId) },
  }
}
