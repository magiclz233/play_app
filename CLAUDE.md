# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

本项目是**同城伴玩（助教搭子）私域担保平台**——一个基于微信小程序的本地陪玩/技能助教预约平台，支持平台资金担保交易。

---

## 🏛️ 技术架构

### 后端
* **JDK 版本**：Java 25 (LTS)
* **框架**：Spring Boot 4.0.6
* **数据库**：PostgreSQL（支持 JSONB 字段）
* **缓存与令牌**：Redis（localhost:6379，无密码）
* **ORM**：MyBatis-Plus 3.5.6（使用 Spring Boot 3 适配版）
* **认证**：Spring Security（无状态 JWT + 过滤器）
* **微信 SDK**：WxJava（`wx-java-miniapp-spring-boot-starter` & `wx-java-pay-spring-boot-starter` 4.6.0）

### 前端（小程序）
* **框架**：uni-app（Vue 3 + Vite + TypeScript）
* **目标平台**：微信小程序（mp-weixin）
* **样式方案**：原生 CSS（模块化设计体系，配置于 `uni.scss`）

### 前端（PC Web 管理端）
* **框架**：Vue 3 + Vite + TypeScript
* **UI 库**：Element Plus
* **状态管理**：Pinia
* **路由**：Vue Router 4
* **构建产物**：SPA 静态文件（部署至 Nginx 或后端静态资源目录）

---

## 📂 核心包与目录结构

```text
play_app/
├── play-app-backend/
│   ├── src/main/java/com/playapp/
│   │   ├── common/                # BusinessException, Result<T>, ErrorCode
│   │   ├── config/                # SecurityConfig, MybatisPlusConfig, WebMvcConfig
│   │   ├── controller/            # 控制器层（Admin、Auth、Companion、User 等）
│   │   ├── dto/                   # 请求体（OrderCreateDTO、WithdrawDTO）
│   │   ├── entity/                # 数据库实体类
│   │   ├── mapper/                # MyBatis-Plus Mapper 接口（继承 BaseMapper<T>）
│   │   ├── service/               # Service 接口
│   │   │   └── impl/              # Service 实现（继承 ServiceImpl）
│   │   ├── utils/                 # JwtUtils
│   │   ├── vo/                    # 视图对象（LoginVO、CompanionVO）
│   │   └── PlayAppApplication.java # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml        # 主配置（spring.profiles.active=dev_1）
│   │   └── application-dev_1.yml  # 本地开发配置（数据库密码、Redis）
│   └── pom.xml                    # Maven 构建文件
│
├── play-app-admin/               # PC Web 管理端（Vue 3 + Element Plus）
└── play-app-frontend/             # uni-app 前端根目录
```

---

## 🔐 安全与认证架构

### JWT 认证流程（无状态）
1. `JwtAuthenticationFilter`（每次请求执行一次）从 `Authorization` 请求头中提取 Bearer 令牌。
2. 验证 JWT，然后检查 Redis 键 `token:<userId>`——若存储的令牌不匹配，请求被拒绝（以此实现单设备登录/强制下线）。
3. 验证通过后，将 userId 设为主体的 `UsernamePasswordAuthenticationToken` 及对应的 `SimpleGrantedAuthority` 角色写入 `SecurityContextHolder`。
4. 控制器通过 `@AuthenticationPrincipal Long userId` 获取当前用户 ID。

### 角色映射（roleId → Spring Security 角色）
| roleId | 角色名称 | Spring Authority |
|--------|---------|------------------|
| 1 | 客户 | `ROLE_CUSTOMER` |
| 2 | 陪玩 | `ROLE_COMPANION` |
| 3 | 管理员 | `ROLE_ADMIN` |

### API 路由权限规则（SecurityConfig）
| URL 前缀 | 是否需要认证 | 说明 |
|---------|------------|------|
| `/api/wx/*` | 否（permitAll） | 微信登录、支付回调 |
| `/api/public/**` | 否（permitAll） | 公开的主题配置等 |
| `/api/companions/**` | 否（permitAll） | 公开的助教列表和详情 |
| `/api/admin/**` | 是，需要 `ROLE_ADMIN` | 管理后台接口 |
| 其余所有路径 | 是（需认证） | 订单、用户信息、助教申请、文件上传 |

### 控制器层管理员双重保护
Admin 控制器在类级别额外添加 `@PreAuthorize("hasRole('ADMIN')")` 注解，与 URL 守卫形成双重校验。

### RBAC 角色与权限体系（v2 升级）
* **数据表**：`roles`、`permissions`、`user_roles`、`role_permissions` 四张关联表
* **角色分配优先级**：
  1. 首选从 `user_roles` 表查询（DB 驱动）
  2. 若 `user_roles` 无记录，回退到业务规则自动分配（默认客户=1，认证陪玩=2，管理员手机号=3）
  3. 首次回退分配时自动同步写入 `user_roles` 表（渐进式升级）
* **JWT 多角色支持**：Token 中 `roles` 字段存储角色 ID 列表，`JwtAuthenticationFilter` 将其转换为多个 `SimpleGrantedAuthority`
* **管理 API 路径**：
  * `GET/POST/PUT/DELETE /api/admin/roles` — 角色 CRUD
  * `PUT /api/admin/roles/{id}/permissions` — 分配权限
  * `GET /api/admin/permissions` — 权限树
  * `GET/PUT /api/admin/users/{id}/roles` — 用户角色管理
* **PC Web 认证**：
  * `POST /api/auth/login` — 账号密码登录（permitAll）
  * `GET /api/auth/me` — 当前用户信息（含角色+权限列表）

---

## 🔑 Mock 登录与本地测试（无需微信 AppID）

由于本地开发没有微信 `appid`，系统支持 **mock 登录** 流程，绕过微信原生静默登录（`jscode2session`）。

### 1. 后端 Mock 接口
* **位置**：`com.playapp.controller.AuthController`
* **路由**：`POST /api/wx/mock-login`
* **请求体**：`{"phone": "13800000000"}`
* **行为**：
  * 模拟微信 openid：`mock_openid_13800000000`。
  * 用户不存在时自动创建（默认角色：`1 - 客户`）。
  * 生成合法 JWT 令牌并缓存至 Redis（`token:<userId>`）。
  * **管理员旁路**：若 `phone` 为 `"admin"` 或 `"13800000000"`，令牌将被赋予管理员角色（`0`）。

### 2. Service 实现
参见 `UserServiceImpl.java`：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public LoginVO mockLogin(String phone) {
    String mockOpenid = "mock_openid_" + phone;
    User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, mockOpenid));
    if (user == null) {
        user = new User();
        user.setOpenid(mockOpenid);
        user.setPhone(phone);
        user.setNickname("测试用户_" + phone);
        user.setStatus(1);
        user.setLastLoginTime(LocalDateTime.now());
        this.save(user);
    }
    // ... 角色分配、令牌生成 ...
}
```

---

## 🗄️ 数据库规范

系统使用 **PostgreSQL**。
* 数据库名：`play_app`
* 建表脚本：`schema.sql`（位于仓库根目录）
* 主键使用 `BIGSERIAL` / `SERIAL` 自增。
* 索引规范：普通搜索列使用 B-tree 索引（`idx_users_phone`、`idx_orders_status`）；JSONB 字段使用 GIN 索引（`idx_users_extra`）。
* 列注释直接通过 `COMMENT ON COLUMN` 在数据库中注册。

### 核心表
1. `users`：用户账号（openid、昵称、角色、状态）。
2. `companion_profiles`：助教认证与展示信息（相册画廊、语音链接、价格、评分、状态）。
3. `orders`：订单表，共 15 种状态。
4. `companion_wallets`：陪玩财务跟踪（余额、冻结金额、累计收支）。
5. `withdrawal_records`：陪玩提现记录（待审核、已通过、已打款、失败、已拒绝）。

---

## 🔄 订单状态机（15 种状态）

```
10-待付款 → 20-已付款/待拉群 → 30-客服已拉群 → 40-双方确认 → 50-服务进行中
                                                                     ↓
100-申请取消 → 110-退款处理中 → 120-全额退款 / 130-部分退款         60-陪玩发起完工
                                                                     ↓
200-纠纷申诉中 → 210-纠纷处理完成                                  70-用户确认完工/待结算
                                                                     ↓
250-超时自动关闭                                                    80-平台放款/订单完成
```

所有状态码：`10`、`20`、`30`、`40`、`50`、`60`、`70`、`80`、`100`、`110`、`120`、`130`、`200`、`210`、`250`。

状态变更记录至 `order_status_logs` 表，包含操作人追踪（客户/陪玩/管理员/系统自动）。

---

## 📜 后端开发规范

修改或扩展后端代码时，请遵循以下原则：

### 1. 统一返回体（`Result<T>`）
每个 REST 控制器方法必须返回 `Result<T>` 或 `Result<Void>` 对象：
* 成功：`Result.success(data)` 或 `Result.success(message, data)`。
* 失败：`Result.error(errorCode, message)`。

### 2. 业务异常（`BusinessException`）
* 不要在控制器中通过 try-catch 手动包装错误信息。
* 在 Service 中抛出携带 `ErrorCode` 的 `BusinessException`。
* `GlobalExceptionHandler` 会自动捕获并返回标准 HTTP JSON 响应。

### 3. ORM 配置（MyBatis-Plus）
* 实体类字段应与数据库列一一对应。
* 数据库下划线命名与 Java 驼峰命名的差异可通过 `@TableField` 处理（MyBatis-Plus 默认自动映射标准 snake/camel 转换）。
* Mapper 必须继承 `BaseMapper<T>`，Service 必须继承 `ServiceImpl<M, T>`。

### 4. 依赖注入
* 使用 Lombok `@RequiredArgsConstructor` 配合 `private final` 字段，而非 Spring 的 `@Autowired` 字段注入。

### 5. 控制器模式
* 使用 `@AuthenticationPrincipal Long userId` 获取当前认证用户 ID（由 `JwtAuthenticationFilter` 设置）。
* Admin 控制器：在类上添加 `@PreAuthorize("hasRole('ADMIN')")` 注解，作为 SecurityConfig URL 守卫的补充保护。
* 使用 `@Valid` 注解请求体以触发 Jakarta Bean Validation。

### 6. 微信支付证书
将微信支付 V3 API 客户端证书（`.pem` 文件）放置在 `src/main/resources/cert/` 目录下。配置中通过 `classpath:cert/apiclient_key.pem` 引用。

### 7. 暂无测试
项目目前没有测试类（`pom.xml` 中已引入 `spring-boot-starter-test` 但尚未编写测试）。后端验证通过 mock 登录接口进行人工 API 测试。

---

## 📱 前端架构与模式

### 登录流程
`useUserStore.wxLogin()` → `uni.login({provider:'weixin'})` 获取 code → `POST /api/wx/login` 将 code 发送至后端 → 获取 JWT 令牌及用户信息 → 通过 Pinia store 存入 `uni.storage`。

### HTTP 客户端（`src/utils/request.ts`）
所有 API 调用统一使用 `request<T>` 封装。基础地址为 `http://127.0.0.1:8080/api`。封装自动附加 `Authorization: Bearer <token>` 请求头，解析 `Result<T>` 响应，并处理 401（清除令牌）和错误提示。

### 页面导航
- TabBar：大厅（index）、订单（order/list）、我的（mine/index）
- 二级页面：助教详情、订单创建/支付/详情/评价、助教入驻/工作台/钱包

---

## 📁 文件上传

文件存储在本地 `D:/code/project/play_app/uploads/`（可通过 `file.upload.path` 配置）。通过 `POST /api/file/upload`（multipart）上传，经 `WebMvcConfig` 资源映射提供 `http://127.0.0.1:8080/uploads/<文件名>` 的访问地址。

---

## 🎨 前端 UI/UX 设计系统（强制遵守）

> **规则优先级**：本章规则为强制性约束。任何前端代码变更必须通过以下检查清单。
> 完整设计规范文档：`docs/THEME_OPTIMIZATION_PLAN.md` | `design.md`

### 主题架构（三层设计令牌）

```
play-app-frontend/src/theme/
├── index.ts              # 统一导出入口
├── primitives.ts         # Layer 1: 原始色板（10级色阶 × 8个色相）
├── semantic.ts           # Layer 2: 语义令牌（亮/暗两套完整映射）
├── components.ts         # Layer 3: 组件令牌（按钮/卡片/输入框等）
├── theme.config.ts       # 品牌配置（可被后端 API 覆盖）
├── engine.ts             # 主题引擎（计算 + 合并 + 注入 CSS 变量）
├── color-utils.ts        # 颜色工具（hexToRgb, blend, contrastRatio 等）
└── tokens.ts             # [废弃] 向后兼容层
```

### 核心原则

1. **所有颜色必须通过 CSS 变量引用**，严禁硬编码 hex 值。
2. **亮/暗模式自动适配**——ThemeEngine 在 `App.vue onLaunch` 中注入 CSS 变量。
3. **页面无需手动绑定主题**——`<view class="container">` 即可，CSS 变量自动继承。
4. **品牌色可通过后端 API 实时更新**，无需重新编译。

### 强制检查清单（每次前端变更前必查）

#### 🚫 禁止项（违反必须拒绝）
- ❌ **硬编码颜色**：`color: #FF3B5C` / `background: #fff` / `border-color: #eee` → 必须用 `var(--xxx)`
- ❌ **emoji 作为图标**：♂♀★☆🎨🚀⚙️ → 必须用内联 SVG（viewBox 0 0 24 24）
- ❌ **裸 z-index 值**：`z-index: 99` / `z-index: 999` → 必须用 SCSS 变量（`$z-index-sticky`, `$z-index-modal` 等）
- ❌ **动画 width/height**：`@keyframes { height: 10rpx }` → 必须用 `transform: scaleY()`
- ❌ **outline: none 无替代**：移除聚焦环必须提供替代方案

#### ✅ 必须项（缺少必须补全）
- ✅ 所有 `<image>` 添加 `lazy-load`（首屏 banner 除外）
- ✅ 所有可点击元素添加 `hover-class` 或 `:active` 反馈
- ✅ 图标按钮添加 `aria-label` 属性
- ✅ 数字输入框添加 `inputmode="numeric"` / `"decimal"`
- ✅ `line-height` 显式设为 1.5-1.6（正文）
- ✅ 触摸目标最小 88rpx（44px）

### CSS 变量速查表

| 用途 | 变量 | 说明 |
|------|------|------|
| 页面背景 | `var(--bg-main)` | 浅色: #FAF5F5, 暗色: #121216 |
| 卡片背景 | `var(--bg-card)` | 浅色: #FFFFFF, 暗色: #24262D |
| 浮层背景 | `var(--bg-elevated)` | 弹窗/下拉菜单 |
| 微妙背景 | `var(--bg-subtle)` | 标签底色、区分区块 |
| 主要文字 | `var(--text-primary)` | 浅色: #1A1A1F, 暗色: #E2E4E9 |
| 次要文字 | `var(--text-secondary)` | 浅色: #5E6066, 暗色: #A1A5B1 |
| 占位文字 | `var(--text-muted)` | 浅色: #A1A5B1, 暗色: #737887 |
| 反白文字 | `var(--text-inverse)` | 深色背景上的白色文字 |
| 品牌主色 | `var(--color-primary)` | 浅色: #FF3B5C, 暗色: #FF5C73 |
| 主色RGB | `var(--color-primary-rgb)` | 用于 rgba() 组合：`rgba(var(--color-primary-rgb), 0.1)` |
| 主色浅底 | `var(--color-primary-light)` | 标签/按钮背景 |
| 主色深 | `var(--color-primary-dark)` | active/pressed 状态 |
| 渐变终点 | `var(--color-gradient-end)` | 渐变按钮右侧色 |
| 强调色 | `var(--color-accent)` | 皇冠、评分星、高亮 |
| 成功色 | `var(--color-success)` | 在线状态、支付按钮、已通过 |
| 警告色 | `var(--color-warning)` | 待审核、提醒 |
| 错误色 | `var(--color-error)` | 驳回、删除、退款 |
| 信息色 | `var(--color-info)` | 链接、提示 |
| 常规边框 | `var(--border-color)` | 卡片/列表项边框 |
| 强边框 | `var(--border-strong)` | 输入框聚焦 |
| 卡片阴影 | `var(--shadow-card)` | 卡片悬浮 |
| 浮动阴影 | `var(--shadow-floating)` | FAB、弹窗 |
| 辉光阴影 | `var(--shadow-glow)` | 品牌辉光 |
| 小/中/大圆角 | `var(--radius-sm)` / `md` / `lg` / `xl` | 8/16/24/32 rpx |
| 全圆角 | `var(--radius-full)` | 999rpx |
| 字号梯度 | `var(--font-size-xs)` ~ `xxl` | 20/24/28/32/36/48 rpx |
| 间距梯度 | `var(--space-xs)` ~ `xl` | 8/16/24/32/48 rpx |

### z-index 层级体系

| SCSS 变量 | 值 | 用途 |
|-----------|------|------|
| `$z-index-dropdown` | 10 | 下拉菜单 |
| `$z-index-sticky` | 100 | 悬浮按钮、固定底栏、吸顶导航 |
| `$z-index-fixed` | 200 | 固定定位元素 |
| `$z-index-modal-backdrop` | 500 | 弹窗遮罩 |
| `$z-index-modal` | 1000 | 弹窗/对话框 |
| `$z-index-toast` | 2000 | Toast 提示 |

### 新页面/组件开发模板

```vue
<template>
  <view class="container">
    <!-- ✅ 正确：class="container" 即可，无需手动绑定主题 -->
    <view class="my-card" hover-class="card-hover" @click="handleClick">
      <image :src="url" mode="aspectFill" lazy-load />
      <text class="title">标题</text>
    </view>
    <button :loading="submitting" hover-class="button-hover">提交</button>
  </view>
</template>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);     /* ✅ CSS 变量 */
  padding: var(--space-md);
}
.my-card {
  background-color: var(--bg-card);      /* ✅ CSS 变量 */
  border: 1px solid var(--border-color); /* ✅ CSS 变量 */
  border-radius: var(--radius-lg);       /* ✅ 设计令牌 */
  box-shadow: var(--shadow-card);        /* ✅ 设计令牌 */
}
.title {
  font-size: var(--font-size-base);      /* ✅ 字号令牌 */
  color: var(--text-primary);            /* ✅ 文字令牌 */
  line-height: 1.6;                      /* ✅ 显式行高 */
}
.card-hover { opacity: 0.85; transform: scale(0.98); }  /* ✅ 触摸反馈 */
</style>
```

### 品牌色动态配置

- **前端拉取**：`GET /api/public/theme-config`（公开接口，无需认证）
- **管理员更新**：`PUT /api/admin/theme-config`（需 `ROLE_ADMIN`）
- **数据库存储**：`system_configs` 表，`config_key='theme_brand'`，JSONB 格式
- **生效机制**：ThemeEngine 合并远程配置 → 重新计算 CSS 变量 → 注入页面根元素

---

## ⚙️ 编译与运行命令

### 后端命令（在 `play-app-backend` 目录下执行）
* 编译：`mvn clean compile`
* 打包：`mvn clean package`
* 运行：`mvn spring-boot:run -Dspring-boot.run.profiles=dev_1`

### 前端命令（在 `play-app-frontend` 目录下执行）
* 安装依赖：`npm install`
* 开发构建：`npm run dev:mp-weixin`
* 生产构建：`npm run build:mp-weixin`

### PC Web 管理端命令（在 `play-app-admin` 目录下执行）
* 安装依赖：`npm install`
* 开发运行：`npm run dev`（端口 5173，代理 /api → localhost:8080）
* 生产构建：`npm run build`（产出至 dist/ 目录）
