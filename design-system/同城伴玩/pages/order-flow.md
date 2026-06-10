# Page Override: 订单流程 (order/create, pay, detail, refund, review, timeline)

> Overrides MASTER.md. See `play-app-frontend/src/pages/order/`

## Flow
create → pay (countdown 15min) → detail (status-driven) → timeline → review
detail → refund (status 100-130)

## Key Patterns
- **Status-driven UI**: order status determines visible actions
- **Payment countdown**: `15:00` format, red when < 5min
- **Refund form**: reason textarea + submit with `:loading`
- **Review**: star rating (interactive) + content textarea
- **Timeline**: vertical line + colored dots per event type

## Color Rules
- Payment button: `var(--color-success)` (green for WeChat Pay)
- Refund status: `var(--color-error)` (red)
- Timeline dots: mapped to semantic colors (`--color-info/success/warning/error`)
- Countdown urgent: `var(--color-error)` when < 300s

## States
- Order list empty: `<EmptyState text="暂无订单" actionText="去大厅逛逛" />`
- Loading: `<SkeletonCard variant="order" />`
- Payment timeout: auto-close + toast
- Form validation: inline error via `<FormField error="..." />`
