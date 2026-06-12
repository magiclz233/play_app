# 管理端双端方案设计

## Context

当前平台只有一个小程序端，管理功能集中在一个简陋的 `pages/admin/index.vue` 页面中（5个Tab：助教审核、提现审核、订单管理、需求跟进、风控举报）。存在以下问题：

- **没有PC Web管理端**，大量管理操作（用户管理、角色权限配置、数据分析等）在小程序上体验极差
- **小程序管理端没有权限管控**，管理后台入口对所有登录用户可见（标注为"测试入口"）
- **RBAC数据库表已建但未接入代码**（roles/permissions/user_roles/role_permissions 表存在但不参与运行时鉴权）
- **角色分配硬编码**在 `UserServiceImpl` 中（通过手机号判断是否为admin），`user_roles` 表从未被查询
- **mockLogin 有bug**：admin 手机号角色设为0，映射为 ROLE_USER 而非 ROLE_ADMIN
- **前端没有角色感知**，userStore 中没有 role/isAdmin 字段

### 目标架构

```
┌─────────────────────────────────────────────────────────┐
│                    同城伴玩平台                            │
├─────────────────────┬───────────────────────────────────┤
│   小程序端 (现有)      │   PC Web 管理端 (新建)               │
│   uni-app (Vue3)     │   Vue 3 + Vite + Element Plus     │
├─────────────────────┤───────────────────────────────────┤
│ 用户端  │ 小程序管理端  │   全面管理后台                        │
│ 大厅/订单/我的        │   Dashboard / 用户管理 / 角色权限       │
│ 助教浏览/下单/评价     │   助教审核 / 订单管理 / 财务结算          │
│ 助教入驻/工作台/钱包   │   需求管理 / 风控中心 / 系统配置          │
│                     │   数据统计 / 操作日志 / 标签管理           │
├─────────────────────┴───────────────────────────────────┤
│              后端 API (Spring Boot 4.0.6)                │
│         统一认证 (JWT) + RBAC (角色+权限)                    │
│         共用现有 Service 层，扩展 Admin API                │
└─────────────────────────────────────────────────────────┘
```

---

## 实施计划（按阶段）

### 阶段一：后端 RBAC 体系升级 🔐

> **目标**：将现有的"硬编码角色判断"升级为真正的数据库驱动的 RBAC 体系，为双端管理提供统一的权限基础。

#### 1.1 修复 mockLogin Bug
- 文件：`play-app-backend/src/main/java/com/playapp/service/impl/UserServiceImpl.java`
- 将 `mockLogin()` 中 admin 的 role 从 `0` 改为 `3`（与 `wxLogin()` 保持一致）
- JwtAuthenticationFilter 的 `getRoleName()` 已经支持 roleId=3 → ROLE_ADMIN

#### 1.2 接入 user_roles 表
- 修改 `UserServiceImpl.wxLogin()` 和 `mockLogin()`：
  - 登录时从 `user_roles` 表查询用户角色（而非硬编码判断）
  - 如果 `user_roles` 无记录，则按现有逻辑回退（companion=2, 否则=1）
  - 将查到的角色写入 JWT claims
- 修改 `JwtUtils.generateToken()`：token 中存储 roleId 列表（支持多角色）

#### 1.3 新增 RBAC 管理 API
新建文件：
- `play-app-backend/src/main/java/com/playapp/controller/AdminRoleController.java`
  - `GET /api/admin/roles` — 角色列表
  - `POST /api/admin/roles` — 创建角色
  - `PUT /api/admin/roles/{id}` — 更新角色
  - `DELETE /api/admin/roles/{id}` — 删除角色
  - `PUT /api/admin/roles/{id}/permissions` — 分配权限
- `play-app-backend/src/main/java/com/playapp/controller/AdminPermissionController.java`
  - `GET /api/admin/permissions` — 权限树（按 parentId 组装）
  - `POST /api/admin/permissions` — 创建权限
  - `PUT /api/admin/permissions/{id}` — 更新权限
  - `DELETE /api/admin/permissions/{id}` — 删除权限
- 用户-角色关联管理（扩展 `AdminUserController`）：
  - `GET /api/admin/users/{id}/roles` — 查看用户角色
  - `PUT /api/admin/users/{id}/roles` — 分配用户角色
- 对应的 Service + Mapper（使用现有 `RoleMapper`、`PermissionMapper`、`UserRoleMapper`、`RolePermissionMapper`）

#### 1.4 CORS 确认
现有 CORS 配置已在 `SecurityConfig` 中允许所有来源（`setAllowedOriginPatterns(List.of("*"))`），PC Web 端可直接跨域调用。

---

### 阶段二：小程序管理端优化 📱

> **目标**：将现有的粗糙管理页面改造为正经的"移动管理工具"，增加权限管控和交互优化。

#### 2.1 前端增加角色感知
- 文件：`play-app-frontend/src/store/user.ts`
  - `userInfo` 增加类型定义（`UserInfo` 接口，含 `roleId`/`roles`/`permissions` 字段）
  - 新增 computed：`isAdmin`、`userRoles`
- 文件：`play-app-frontend/src/pages/mine/index.vue`
  - 管理后台入口增加 `v-if="userStore.isAdmin"` 权限管控
  - 移除"测试入口"标注

#### 2.2 重构管理页面
- 文件：`play-app-frontend/src/pages/admin/index.vue`
  - 使用已有的 `adminApi` 封装（`src/api/admin.ts`）替代原始 `request()` 调用
  - 使用已有的 `EmptyState`、`StatusBadge`、`SkeletonCard` 组件
  - 增加 Dashboard 概览卡片（今日订单、待审核数、今日收入等），调用 `GET /api/admin/dashboard`
  - 增加加载态和错误态处理
  - 使用 i18n 替代硬编码中文
  - 新增子页面导航结构（取代单页5个Tab的拥挤布局）

#### 2.3 小程序管理端功能范围
管理端作为一个分组入口，包含以下子页面：

| 页面 | 路由 | 功能 |
|------|------|------|
| 管理首页 | `pages/admin/index` | Dashboard概览 + 快捷入口 |
| 助教审核 | `pages/admin/companion-audit` | 列表 + 通过/驳回 |
| 提现审核 | `pages/admin/withdrawal-audit` | 列表 + 通过/驳回 |
| 订单管理 | `pages/admin/orders` | 列表 + 筛选 + 状态操作 |
| 需求跟进 | `pages/admin/requests` | 列表 + 状态更新 |
| 风控中心 | `pages/admin/risk` | 举报列表 + 处理 |

> 设计原则：小程序端只保留"移动端高频操作"，不做大而全的数据管理。

---

### 阶段三：PC Web 管理端新建 🖥️

> **目标**：新建独立的 Vue 3 + Element Plus 管理后台 SPA 项目，提供全面的平台管理能力。

#### 3.1 项目初始化

在仓库根目录新建 `play-app-admin/` 项目：

```
play-app-admin/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── src/
│   ├── main.ts                 # 入口
│   ├── App.vue                 # 根组件
│   ├── router/                 # 路由
│   │   └── index.ts            # 路由配置（含权限守卫）
│   ├── stores/                 # Pinia stores
│   │   ├── auth.ts             # 认证（登录/登出/token管理）
│   │   └── app.ts              # 全局状态（侧边栏折叠等）
│   ├── api/                    # API 封装
│   │   ├── request.ts          # Axios 实例（拦截器+token）
│   │   ├── auth.ts             # 登录API
│   │   ├── dashboard.ts        # Dashboard API
│   │   ├── user.ts             # 用户管理API
│   │   ├── role.ts             # 角色管理API
│   │   ├── permission.ts       # 权限管理API
│   │   ├── companion.ts        # 助教管理API
│   │   ├── order.ts            # 订单管理API
│   │   ├── wallet.ts           # 财务/提现API
│   │   ├── risk.ts             # 风控API
│   │   └── system.ts           # 系统配置API
│   ├── views/                  # 页面
│   │   ├── login/              # 登录页
│   │   ├── dashboard/          # 数据仪表盘
│   │   ├── user/               # 用户管理
│   │   ├── role/               # 角色管理
│   │   ├── companion/          # 助教管理（审核+列表）
│   │   ├── order/              # 订单管理
│   │   ├── finance/            # 财务管理（钱包+提现+流水）
│   │   ├── risk/               # 风控中心
│   │   ├── request/            # 需求管理
│   │   ├── content/            # 内容管理（分类+标签+评价）
│   │   ├── system/             # 系统配置（主题+参数）
│   │   └── log/                # 操作日志
│   ├── components/             # 共享组件
│   ├── utils/                  # 工具函数
│   └── types/                  # TypeScript 类型
```

#### 3.2 技术选型

| 类别 | 选择 | 理由 |
|------|------|------|
| 框架 | Vue 3 + Composition API | 与小程序端一致，降低认知负担 |
| 构建 | Vite 5 | 与小程序端一致 |
| UI库 | Element Plus | Vue 3 生态最成熟的管理后台组件库 |
| 状态管理 | Pinia | 与小程序端一致 |
| HTTP | Axios | PC端标准选择 |
| 路由 | Vue Router 4 | SPA 必备 |
| 图表 | ECharts 5 | Dashboard 数据可视化 |
| 图标 | Element Plus Icons | 与UI库配套 |
| 表格 | vxe-table (可选) | 复杂表格（订单列表等）如需可后期引入 |

#### 3.3 PC Web 端完整功能矩阵

```
┌─────────────────────────────────────────────────┐
│                   PC Web 管理端                    │
├──────────┬──────────┬──────────┬────────────────┤
│ Dashboard│ 用户管理  │ 助教管理  │  订单管理       │
├──────────┼──────────┼──────────┼────────────────┤
│• 核心指标 │• 用户列表  │• 助教列表  │• 订单列表(高级筛选)│
│• 趋势图表 │• 用户详情  │• 入驻审核  │• 订单详情/时间线  │
│• 待办事项 │• 启用/禁用 │• 相册审核  │• 状态流转操作    │
│• 快捷入口 │• 角色分配  │• 推荐管理  │• 退款处理       │
│          │          │• 技能管理  │• 纠纷仲裁       │
├──────────┼──────────┼──────────┼────────────────┤
│ 财务管理  │ 内容管理  │ 风控中心  │  系统管理       │
├──────────┼──────────┼──────────┼────────────────┤
│• 钱包列表  │• 分类管理  │• 举报列表  │• 角色管理       │
│• 提现审核  │• 标签管理  │• 举报处理  │• 权限管理       │
│• 流水明细  │• 评价管理  │• 用户封禁  │• 品牌主题配置    │
│• 支付记录  │• 需求管理  │• 操作日志  │• 系统参数       │
│• 收入统计  │• 首页配置  │          │• 管理员账号管理  │
└──────────┴──────────┴──────────┴────────────────┘
```

#### 3.4 登录方案

由于 PC Web 端无法调用微信登录（`uni.login` 只能在微信环境使用），提供两种登录方式：

1. **账号密码登录**（推荐主方案）
   - 扩展 `users` 表增加 `username` + `password_hash` 字段（仅管理员使用）
   - 新增 `POST /api/auth/login` 接口（账号密码 → JWT）
   - 新增 Migration `V6__admin_account.sql`

2. **扫码登录**（可选后期方案）
   - 小程序端生成扫码 Token → PC 端轮询 → 小程序端确认

#### 3.5 后端新增 API

需要为 PC 端补充以下 API：

| Controller | 新增端点 | 说明 |
|-----------|---------|------|
| `AuthController` | `POST /api/auth/login` | 账号密码登录 |
| `AuthController` | `GET /api/auth/me` | 获取当前用户信息（含角色+权限列表） |
| `AdminRoleController` | CRUD | 角色管理 |
| `AdminPermissionController` | CRUD | 权限管理 |
| `AdminUserController` | `GET /{id}/roles`, `PUT /{id}/roles` | 用户-角色关联 |
| `AdminOrderController` | `GET /statistics` | 订单统计（按状态/日期聚合） |
| `AdminDashboardController` | 扩展 | 增加趋势数据、图表数据 |
| `AdminCompanionController` | `GET /` | 全量助教列表（含筛选） |
| `AdminWalletController` | `GET /wallets` | 钱包列表 |
| `AdminWalletController` | `GET /wallets/{id}/transactions` | 钱包流水 |
| `AdminCategoryController` | CRUD | 分类管理 |
| `AdminReviewController` | `GET /` | 评价列表+审核 |
| `AdminLogController` | `GET /` | 操作日志查询 |
| `AdminConfigController` | `GET /`, `PUT /` | 系统配置管理（扩展） |

---

### 阶段四：数据与迁移 💾

新建 Flyway 迁移文件：
- `V6__admin_account.sql` — users 表增加 `username`、`password_hash` 列，为已存在的 admin 用户（phone=13800000000）设置默认密码
- `V7__seed_admin_permissions.sql` — 完善权限种子数据（当前只有15条基础权限，需要扩展为完整的权限树），为 ROLE_ADMIN 角色分配所有权限

---

## 实施顺序

| 顺序 | 阶段 | 预估工作量 | 依赖 |
|------|------|-----------|------|
| 1 | 阶段一：后端 RBAC 升级 | 2-3天 | 无 |
| 2 | 阶段二：小程序管理端优化 | 1-2天 | 阶段一 |
| 3 | 阶段三：PC Web 管理端 | 5-7天 | 阶段一 |
| 4 | 阶段四：数据迁移与完善 | 1天 | 阶段一 |

> 阶段二和阶段三可以并行开发（共享同一套后端 API）。

---

## 关键设计决策

1. **为什么不把 PC Web 端做成 uni-app H5？**
   uni-app H5 模式的布局和交互是为移动端设计的，无法提供专业管理后台所需的侧边栏导航、复杂表格、图表仪表盘等桌面端体验。独立 SPA 项目是最佳选择。

2. **为什么选择 Element Plus 而不是 Ant Design Vue？**
   Element Plus 在 Vue 3 生态中社区最活跃，中文文档完善，与项目现有 Vue 3 技术栈匹配度高。

3. **RBAC 粒度的取舍**
   - 后端：使用 Spring Security 的 `hasRole()` + 自定义 `@PreAuthorize("hasPermission('xxx')")` 注解（可选后续细化）
   - 前端：从 `GET /api/auth/me` 获取当前用户权限列表，路由守卫 + 按钮级 `v-if` 控制
   - 当前阶段保持基于角色的粗粒度控制（ROLE_ADMIN 拥有全部权限），权限表为后期细粒度控制预留

4. **PC Web 端部署**
   - 开发阶段：`npm run dev` 启动 Vite dev server（独立端口如 5173）
   - 生产阶段：`npm run build` 产出静态文件，可部署到 Nginx 或与 Spring Boot 静态资源目录集成

---

## 验证方案

1. **后端 RBAC 验证**
   - 使用 mock-login 以 admin 手机号登录 → 确认返回 token 中 role 正确（3=ADMIN）
   - 调用 `/api/admin/dashboard` → 应返回 200
   - 使用普通用户 token 调用 `/api/admin/dashboard` → 应返回 403
   - 测试角色 CRUD API → 创建/编辑/删除角色，分配权限

2. **小程序管理端验证**
   - 普通用户登录 → "我的"页面不应显示管理后台入口
   - Admin 用户登录 → "我的"页面显示管理后台入口
   - 进入管理后台 → 各Tab功能正常（审核、订单操作等）

3. **PC Web 端验证**
   - 浏览器访问 `http://localhost:5173` → 跳转登录页
   - 使用 admin 账号密码登录 → 进入 Dashboard
   - 测试各模块：用户列表、角色分配、订单筛选、提现审核等
   - 验证 CORS 跨域正常

---

## 🎨 附录：Web PC 管理端 UI/UX 优化建议 (基于 ui-ux-pro-max)

为了确保后台管理系统（Play App Admin）不仅功能强大，而且在视觉与交互上符合现代 SaaS 平台的高奢专业感，特补充以下 UI/UX 设计优化建议：

### 1. 核心布局与视觉语言
- **设计模式 (Pattern)**：**Data-Dense Dashboard**。尽量利用屏幕空间，减少不必要的卡片内边距（padding），在同屏内展示更多 KPI 指标与表格数据。
- **色彩策略**：继承小程序的“暖杏落日风”，但为了适应长时间的数据处理，**降低大面积背景的饱和度**。
  - 主背景色：纯净的 `#FDFDFD` 或极淡的 `#FAF5F5`。
  - 主题色/按钮色：延续品牌色流光珊瑚红 `var(--color-primary)`。
  - 数据文字色：使用冷色调的炭灰或深墨黑（如 `#1E293B`），提升数据对比度和易读性。

### 2. 排版与字体 (Typography)
- **字体组合推荐**：
  - **数字/表格数据/金额**：强烈建议使用等宽字体（如 **Fira Code** 或 **Inter** 的 tabular-nums 特性），确保财务数据上下对齐。
  - **正文与界面 UI**：使用清晰的无衬线字体（如 **Fira Sans** 或系统默认字体）。

### 3. 数据表格交互增强 (Table Interactions)
- **悬停高亮 (Row Highlighting)**：当鼠标悬停在表格某一行时，背景轻微变色（如 `rgba(255, 59, 92, 0.05)`），提供精确的光标反馈。
- **快捷操作 (Inline Actions)**：将高频操作（如“审核通过”、“驳回”）放在表格右侧常驻，或在悬停时浮现，避免用户必须点进详情页。
- **过滤器平滑展开 (Smooth Filters)**：复杂的订单筛选区域（日期、状态、处理人）使用平滑折叠动画，不要瞬间闪现。

### 4. 专业感“反面模式” (Anti-patterns to Avoid)
- **避免过度装饰 (No Ornate Design)**：后台系统切忌过度使用渐变色大背景、粗重的阴影和花哨的图标。阴影应该只用于悬浮层（如 Dropdown、Modal）。
- **不要使用 Emoji**：所有菜单和操作图标必须使用正规的 SVG 图标库（如 Element Plus Icons 或 Lucide），严禁使用 Emoji。

### 5. 交付质量清单 (Pre-Delivery Checklist)
- [ ] 所有可点击元素（表格行、按钮、筛选项）必须具备 `cursor-pointer` 和明显的 hover 状态反馈（150-300ms过渡）。
- [ ] Element Plus 的默认亮色主题需要复查对比度，确保浅色文字不低于 4.5:1 的 WCAG 标准。
- [ ] 确保表格在窗口缩小至 1024px 时不会直接崩坏，启用原生横向滚动。
