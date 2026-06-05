# 同城伴玩（助教搭子）私域担保平台

本项目定位为**同城线下伴玩与技能助教私域撮合担保平台**。针对早期阶段运营，采用“轻系统、重私域、重服务”的 MVP 运营理念：通过小程序大厅进行助教的形象展示（相册、真人语音条），用户在线支付 100% 全款托管于平台；付款后一键唤起企业微信客服进行拉群、线下对接、履约，最终由管理员在后台核销完工并手动结算。

---

## 🚀 核心业务闭环

```
[用户浏览小程序大厅] ──> [选中助教，在线支付全款托管] ──> [一键跳转企业微信客服名片]
                                                                  │
[平台手动核销，向助教分账] <── [群内确认完工] <── [线下见面履约] <── [客服微信拉建三方群对接]
```

1. **付款金额模式**：在线付全款，资金全额托管在平台商户号中，保障交易安全。
2. **沟通渠道**：付款成功后，一键唤起微信官方“企业微信客服”。
3. **助教端**：助教自助提交照片、录音进行入驻审核，审核通过后自动上架展示。

---

## 🛠️ 技术选型

### 后端 (Spring Boot + JDK 25)
* **核心框架**：Spring Boot 4.0.6 (兼容 JDK 25 LTS 特性)
* **持久层**：MyBatis-Plus 3.5.6 (定制版，支持 Spring Boot 3/4)
* **安全/鉴权**：Spring Security + JWT 缓存（无状态令牌）
* **微信生态**：`WxJava` (WeChat Mini Program + WeChat Pay SDK)
* **数据存储**：
  * **PostgreSQL**：主关系型数据库（包含 JSONB 字段存储多媒体画廊等扩展信息）
  * **Redis**：缓存微信 AccessToken、登录 Token 校验、并发控制

### 前端 (uni-app + Vue 3 + TS)
* **核心框架**：uni-app (Vue 3 + Vite + TypeScript)
* **样式控制**：原生 CSS 变量 + 响应式布局 + 高质感毛玻璃与过渡微动画设计
* **多端编译**：主要编译目标为微信小程序

---

## 📂 项目目录结构

```text
play_app/
├── play-app-backend/              # 后端 Spring Boot 工程
│   ├── src/main/java/com/playapp/
│   │   ├── common/                # 全局异常、统一返回体、错误码
│   │   ├── config/                # Security、MyBatis-Plus、WebMvc 配置
│   │   ├── controller/            # 控制器层 (用户端、助教端、管理后台)
│   │   ├── dto/                   # 数据传输对象 (DTO)
│   │   ├── entity/                # 数据库实体类 (Entity)
│   │   ├── mapper/                # 数据访问层 (Mapper)
│   │   ├── service/               # 业务逻辑接口层 (Service)
│   │   └── PlayAppApplication.java# 启动类
│   ├── src/main/resources/
│   │   ├── application.yml        # 全局配置入口
│   │   └── application-dev_1.yml  # 本地开发/测试配置文件 (dev_1 专属配置)
│   └── pom.xml                    # Maven 依赖管理
│
├── play-app-frontend/             # 前端 uni-app 工程
│   ├── src/
│   │   ├── api/                   # API 请求封装与各模块接口
│   │   ├── components/            # 公共组件 (陪玩卡片、状态标签等)
│   │   ├── pages/                 # 前端页面目录 (用户端、陪玩工作台)
│   │   ├── static/                # 静态资源 (图片、图标)
│   │   ├── store/                 # Pinia 状态管理 (用户登录态)
│   │   ├── App.vue                # 应用生命周期入口
│   │   ├── main.ts                # 前端入口文件
│   │   ├── pages.json             # 路由与TabBar配置
│   │   └── uni.scss               # 全局样式主题变量
│   ├── package.json               # Node 依赖管理
│   └── vite.config.ts             # Vite 配置
│
└── schema.sql                     # PostgreSQL 数据库建表与初始化 SQL 脚本
```

---

## 📦 本地开发快速启动

### 1. 数据库准备 (PostgreSQL)
1. 连接本地或开发环境 PostgreSQL 数据库，创建数据库名 `play_app`。
2. 运行根目录下的 `schema.sql` 脚本，导入 21 张表结构以及基础角色、权限、服务品类与个性化标签的初始化数据。

### 2. Redis 准备
* 启动本地 Redis 服务，默认端口为 `6379`，无需密码（或在 `application-dev_1.yml` 中配置对应密码）。

### 3. 后端启动 (VS Code / IDEA)
1. 配置 JDK 25 作为编译和运行环境。
2. 在 `play-app-backend/src/main/resources/application.yml` 中确保激活的配置文件为 `dev_1`：
   ```yaml
   spring:
     profiles:
       active: dev_1
   ```
3. 在 `application-dev_1.yml` 中修改本地 PostgreSQL 密码（默认为 `qwerwasd*963.`）：
   ```yaml
   spring:
     datasource:
       username: postgres
       password: your_postgresql_password
       url: jdbc:postgresql://localhost:5432/play_app
   ```
4. 运行 `PlayAppApplication.java` 启动后端服务。

> **💡 免 AppID 本地测试**
>
> 平台提供了本地 Mock 微信登录接口：`POST /api/wx/mock-login`。
> 前端开启 mock 配置后，无需配置合法的微信 `appid` 即可通过模拟数据完成登录与业务测试。

### 4. 前端启动 (uni-app)
1. 进入 `play-app-frontend` 目录，执行依赖安装：
   ```bash
   npm install
   ```
2. 启动前端 Vite 开发服务器：
   ```bash
   npm run dev:mp-weixin
   ```
3. 打开**微信开发者工具**，导入编译生成的 `dist/dev/mp-weixin` 文件夹，即可在微信模拟器中进行开发与调试。

---

## 🗄️ 核心数据表对照

| 模块 | 表名 | 用途说明 |
| :--- | :--- | :--- |
| **用户/RBAC** | `users` / `roles` / `permissions` | 核心用户账号、RBAC权限控制 |
| **助教信息** | `companion_profiles` | 助教基本信息、语音链接、排序权重、审核状态 |
| | `companion_albums` | 助教照片展示画廊（支持多图上传与审核） |
| | `companion_skills` | 助教对于每个具体品类（如台球搭子）设定的单价 |
| **订单服务** | `orders` / `order_status_logs` | 核心交易订单、状态变迁日志（订单状态共 15 种） |
| | `service_records` / `service_timeline_events` | 线下履约过程、状态关键节点时间轴记录 |
| | `service_evidence` | 服务过程中的打卡照片、合照或投诉凭证 |
| **评价系统** | `reviews` | 完工后用户对助教的星级与文字评价 |
| **财务钱包** | `payment_records` | 微信支付/退款/放款流水对账记录 |
| | `companion_wallets` / `wallet_transactions` | 助教个人钱包、可提现余额与变动流水 |
| | `withdrawal_records` | 助教发起的钱包余额提现记录及状态 |
