# Page Override: 钱包/提现 (companion/wallet)

> Overrides MASTER.md. See `play-app-frontend/src/pages/companion/wallet.vue`

## Layout
- Balance hero card: large number + "可提现余额" label
- Transaction list: ordered by time desc
- Withdraw modal: amount input + name + submit

## Components
- Balance display: large `48rpx` bold number
- Transaction item: type icon + amount + time
- Withdraw button: `var(--color-primary)` gradient
- Modal: `$z-index-modal` overlay + centered card

## Typography
- Balance number: `48rpx` bold (Outfit-style numeric)
- Transaction amount: `28rpx`
- Transaction time: `var(--font-size-xs)` muted

## States
- Empty transactions: `<EmptyState text="暂无资金明细" />`
- Minimum check: "最低提现10元" toast
- Submitting: `:loading` on submit button
- Success: toast + modal close + refresh
- Error: inline error in modal
