# Page Override: 管理后台 (admin/index)

> Overrides MASTER.md. See `play-app-frontend/src/pages/admin/index.vue`

## Sections
1. Companion applications (approve/reject)
2. Withdrawal reviews (approve/reject)
3. Order management
4. Play requests
5. Risk reports

## Patterns
- **Tab navigation**: horizontal tabs with $z-index-sticky
- **Data cards**: `var(--bg-card)` + border
- **Audit actions**: approve (green) / reject (red) buttons
- **Batch operations**: select + bulk action
- **Status badges**: `<StatusBadge :status="..." type="audit" />`

## Colors
- Approve button: `var(--color-success)`
- Reject button: `var(--color-error)`
- Pending badge: `var(--color-warning)`
- Tabs bg: `var(--bg-card)`
- Card bg: `var(--bg-card)`

## States
- Empty: `<EmptyState text="暂无待审核申请" />`
- Loading: per-section spinner
- Error: `<ErrorBlock @retry="loadData" />`
