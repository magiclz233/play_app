# Page Override: 助教详情页 (companion/detail)

> Overrides MASTER.md for this page. See `play-app-frontend/src/pages/companion/detail.vue`

## Layout
- **Custom nav**: transparent + back button overlaid on album
- **Album swiper**: `45vh`, circular autoplay, page indicator
- **Bottom bar**: fixed `120rpx`, `backdrop-filter: blur(10px)`
- **Container**: `padding-bottom: 140rpx` (accounts for bottom bar)

## Components
- Album swiper with `lazy-load` images
- Voice card with `transform: scaleY()` wave animation
- 4-column attribute grid (height/weight/zodiac/area)
- Star rating via SVG `<StarRating>` component
- Review list with avatar + rating + date

## Colors
- Play button: `var(--color-primary)` + white inner triangle
- Gender male: `#6366F1` (dedicated gender identifier)
- Gender female: `var(--color-primary)`
- Disabled state: `var(--text-muted)`

## States
- Loading: full-page skeleton (album + info + reviews)
- Empty reviews: `<EmptyState text="暂无评价" />`
- Error: `<ErrorBlock message="加载失败" @retry="loadData" />`
- Busy: order button `[disabled]` with grey styling
