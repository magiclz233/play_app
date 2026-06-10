# 同城伴玩平台 — 迭代开发计划

> **使用说明**：按编号顺序开发，完成任务改 `[x]`。搜索 `[ ]` 定位下一个待做任务。微信相关任务标记为 🔒 暂不可执行。

---

## 📊 进度总览

| 分类 | 任务数 | 完成 | 进度 |
|------|--------|------|------|
| 后端核心逻辑 | 7 | 7 | 100% ✅ |
| 后端业务补全 | 8 | 8 | 100% ✅ |
| 前端重构 | 3 | 3 | 100% ✅ |
| 体验优化 | 5 | 5 | 100% ✅ |
| 代码质量 | 3 | 3 | 100% ✅ |
| 🔒 微信对接（暂跳过） | 4 | 0 | 0% |
| **总计** | **30** | **26** | **87%** |

---

## 标记说明

| 标记 | 含义 |
|------|------|
| `[ ]` | 未开始 |
| `[~]` | 进行中 |
| `[x]` | 已完成 |
| `[-]` | 已取消 |
| 🔒 | 需微信资质，暂不可执行 |

---

# 一、后端核心逻辑（优先做）

---

## 1.1 订单状态机严格校验

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h
- **依赖**: 无

### 目标
防止订单状态非法跳跃，在所有状态变更方法中增加统一的前置校验，保证数据一致性。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/java/com/playapp/service/impl/OrderServiceImpl.java` | 修改 — 所有状态变更方法增加校验 |
| `play-app-backend/src/main/java/com/playapp/common/ErrorCode.java` | 修改 — 确认有对应错误码 |

### 实现要点

1. 在 `OrderServiceImpl` 中定义状态转移映射表：
```java
private static final Map<Integer, Set<Integer>> STATUS_TRANSITIONS = Map.of(
    10, Set.of(20, 100, 250),   // 待付款 → 已付款/取消/超时
    20, Set.of(30, 110),        // 已付款 → 客服拉群/退款处理中
    30, Set.of(40),             // 客服已拉群 → 双方确认
    40, Set.of(50),             // 双方确认 → 服务进行中
    50, Set.of(60, 100),        // 服务中 → 陪玩完工/申请取消
    60, Set.of(70),             // 陪玩完工 → 用户确认
    70, Set.of(80),             // 用户确认 → 平台放款
    100, Set.of(110),           // 申请取消 → 退款处理中
    110, Set.of(120, 130),      // 退款处理中 → 全额/部分退款
    // 纠纷状态可从多个状态进入
    10, Set.of(20, 100, 200, 250),
    20, Set.of(30, 110, 200),
    30, Set.of(40, 200),
    40, Set.of(50, 200),
    50, Set.of(60, 100, 200),
    60, Set.of(70, 200),
    200, Set.of(210)            // 纠纷中 → 纠纷处理完成
);
```

2. 添加校验方法：
```java
private void assertValidTransition(Integer fromStatus, Integer toStatus) {
    Set<Integer> allowed = STATUS_TRANSITIONS.get(fromStatus);
    if (allowed == null || !allowed.contains(toStatus)) {
        throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL,
            "不允许从状态 " + fromStatus + " 变更为 " + toStatus);
    }
}
```

3. 在以下方法开头调用校验：
   - `markPaid` → `assertValidTransition(10, 20)`
   - `acceptOrder` → `assertValidTransition(20, 30)`
   - `cancelOrder` → `assertValidTransition(current, 100)` 或 `assertValidTransition(10, 250)`
   - `applyRefund` → `assertValidTransition(current, 100)`
   - `adminMarkGroupCreated` → `assertValidTransition(20, 30)`
   - `adminStartService` → `assertValidTransition(40, 50)`
   - `companionRequestFinish` → `assertValidTransition(50, 60)`
   - `adminConfirmFinish` / `confirmService` → `assertValidTransition(60, 70)`
   - `adminSettleOrder` → `assertValidTransition(70, 80)`
   - `adminApproveRefund` → `assertValidTransition(110, 120)`
   - 纠纷创建 → `assertValidTransition(current, 200)`
   - 超时关闭 → `assertValidTransition(10, 250)`

### 验证方式
- 尝试非法跳转（如 10→80）应抛出 BusinessException
- 正常状态流转不报错

---

## 1.2 平台费用计算

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 2h
- **依赖**: 无

### 目标
下单时根据费率计算平台抽成，而非硬编码 0。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/resources/application.yml` | 修改 — 添加平台费率配置 |
| `play-app-backend/src/main/java/com/playapp/service/impl/OrderServiceImpl.java` | 修改 — `createOrder` 加入计算逻辑 |
| `play-app-frontend/src/pages/order/create.vue` | 修改 — 费用明细显示真实平台费 |

### 实现要点

1. 配置项（`application.yml`）：
```yaml
app:
  platform:
    fee-rate: 0.05        # 5%
    fee-min: 5.00         # 最低 5 元
    fee-max: 50.00        # 最高 50 元封顶
```

2. 在 `OrderServiceImpl.createOrder` 中：
```java
BigDecimal rawFee = totalAmount.multiply(feeRate);
BigDecimal platformFee = rawFee.max(feeMin).min(feeMax);
BigDecimal companionAmount = totalAmount.subtract(platformFee);
```

3. 前端 `create.vue` 的"费用明细"卡片改为从 API 返回的 `Order` 对象取 `platformFee`、`companionAmount`。

### 验证方式
- 下单 50 元 → 平台费 5 元、陪玩 45 元
- 下单 200 元 → 平台费 10 元、陪玩 190 元
- 下单 2000 元 → 平台费 50 元封顶

---

## 1.3 订单超时自动关闭

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h
- **依赖**: 1.1

### 目标
未支付订单超时后自动关闭（10→250），释放日期时段。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/java/com/playapp/PlayAppApplication.java` | 修改 — 加 `@EnableScheduling` |
| `play-app-backend/src/main/java/com/playapp/config/ScheduledTaskConfig.java` | **新建** |
| `play-app-backend/src/main/java/com/playapp/service/OrderService.java` | 修改 — 新增方法 |
| `play-app-backend/src/main/java/com/playapp/service/impl/OrderServiceImpl.java` | 修改 — 实现 |

### 实现要点

1. `@EnableScheduling` 加到启动类
2. 新建 `ScheduledTaskConfig`：
```java
@Configuration
@EnableScheduling
public class ScheduledTaskConfig {
    // 定时任务在此注册
}
```
3. `OrderService` 新增：`void autoCloseExpiredOrders();`
4. 实现：
```java
@Scheduled(fixedDelay = 60000)  // 每 60 秒
public void autoCloseExpiredOrders() {
    List<Order> expired = orderMapper.selectList(
        new LambdaQueryWrapper<Order>()
            .eq(Order::getStatus, 10)
            .lt(Order::getCreateTime, LocalDateTime.now().minusMinutes(15))
    );
    for (Order order : expired) {
        order.setStatus(250);
        orderMapper.updateById(order);
        addStatusLog(order.getOrderId(), 10, 250, 0L, 4, "超时未支付自动关闭");
    }
}
```
5. 超时时间配置化：`@Value("${app.order.pay-timeout-minutes:15}")`

### 验证方式
- 创建订单 → 不支付 → 15 分钟后刷新 → 状态变为"已关闭(250)"
- `order_status_logs` 有 operatorRole=4 的自动关闭记录

---

## 1.4 陪玩发起完工

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h
- **依赖**: 1.1

### 目标
陪玩端可主动发起完工申请（50→60），填写完工备注。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/java/com/playapp/dto/CompanionFinishDTO.java` | **新建** |
| `play-app-backend/src/main/java/com/playapp/service/OrderService.java` | 修改 — 新增方法签名 |
| `play-app-backend/src/main/java/com/playapp/service/impl/OrderServiceImpl.java` | 修改 — 实现 |
| `play-app-backend/src/main/java/com/playapp/controller/CompanionDashboardController.java` | 修改 — 新增接口 |
| `play-app-frontend/src/pages/companion/dashboard.vue` | 修改 — 增加按钮和弹窗 |

### 实现要点

1. `CompanionFinishDTO`：
```java
@Data
public class CompanionFinishDTO {
    @NotBlank @Size(max=500)
    private String finishRemark;
    private Integer finishType;    // 1-正常 2-提前结束 3-超时
    private Integer actualDuration; // 实际时长(分钟)
}
```

2. 接口：`PUT /api/companion/orders/{orderNo}/request-finish`

3. Service 逻辑：
```java
public void companionRequestFinish(Long companionId, String orderNo, CompanionFinishDTO dto) {
    Order order = getOrderByNo(orderNo);
    if (!order.getCompanionId().equals(companionId)) throw forbidden;
    assertValidTransition(order.getStatus(), 60);
    order.setStatus(60);
    orderMapper.updateById(order);
    // 更新或创建 ServiceRecord
    ensureServiceRecord(order).setFinishRemark(dto.getFinishRemark());
    // ...
    addStatusLog(orderId, 50, 60, companionId, 2, dto.getFinishRemark());
}
```

4. 前端 dashboard：status=50 的订单卡片上增加「发起完工」按钮，弹出填写备注和实际时长的模态框。

### 验证方式
- 订单 status=50 → 陪玩点击发起完工 → 订单变 status=60
- 状态日志正确记录

---

## 1.5 管理员订单操作补全

- **状态**: `[x]` 已完成 <!-- 2026-06-10（在1.1中完成）-->
- **预估工时**: 2h
- **依赖**: 1.1, 1.4

### 目标
修复现有 admin 订单操作中的问题，确保状态机校验和日志完整。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/java/com/playapp/service/impl/OrderServiceImpl.java` | 修改 — 补全各 admin 方法 |
| `play-app-frontend/src/pages/admin/index.vue` | 修改 — UI 联动修正 |

### 实现要点

逐一检查以下 admin 方法，确保：
- `adminMarkGroupCreated`：20→30，记录 ServiceRecord
- `adminStartService`：40→50，记录 actualAddress
- `adminConfirmFinish`：60→70，写入 ServiceRecord.finishRemark
- `adminSettleOrder`：70→80，触发 wallet.addBalance
- `adminApproveRefund`：110→120/130，计算退款金额

每个方法必须：校验状态转移 → 更新 order → 记 status log → 记 admin audit log

### 验证方式
- 逐个 admin 操作走通完整订单流程 10→20→30→40→50→60→70→80

---

## 1.6 退款金额计算

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 2h
- **依赖**: 1.1

### 目标
退款时根据服务进度计算退款金额，支持扣除违约金（部分退款 130）。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/java/com/playapp/service/impl/OrderServiceImpl.java` | 修改 — `adminApproveRefund` |

### 实现要点

退款规则：
- 未拉群（status=20）：全额退款 → 120
- 已拉群未服务（status=30/40）：扣 10% 违约金 → 130
- 服务已开始（status=50+）：扣 30% 违约金 → 130
- 纠纷裁决：按管理员指定金额退款

```java
public void adminApproveRefund(Long adminId, String orderNo, BigDecimal refundAmount, String remark) {
    Order order = getOrderByNo(orderNo);
    assertValidTransition(order.getStatus(), refundAmount.compareTo(order.getTotalAmount()) < 0 ? 130 : 120);
    order.setStatus(refundAmount.compareTo(order.getTotalAmount()) < 0 ? 130 : 120);
    order.setRefundAmount(refundAmount);
    orderMapper.updateById(order);
    // 记录退款流水...
}
```

### 验证方式
- 不同阶段取消 → 退款金额正确
- status 写对 120/130

---

## 1.7 陪玩评分实时更新

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 1h
- **依赖**: 无

### 目标
确认 `ReviewServiceImpl.submitReview` 中评分更新逻辑正确，修复可能存在的 Bug。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/java/com/playapp/service/impl/ReviewServiceImpl.java` | 检查修复 |

### 实现要点

已有逻辑需要确认：
```java
// 评价后更新陪玩评分
CompanionProfile profile = companionProfileMapper.selectById(review.getCompanionId());
profile.setTotalRatingCount(profile.getTotalRatingCount() + 1);
profile.setTotalRatingScore(profile.getTotalRatingScore() + review.getRating());
profile.setRating(new BigDecimal(profile.getTotalRatingScore())
    .divide(new BigDecimal(profile.getTotalRatingCount()), 2, RoundingMode.HALF_UP));
companionProfileMapper.updateById(profile);
```

检查项：
- 是否有并发问题（两个评价同时更新）→ 可以暂时接受，后续用乐观锁
- 小数精度是否正确
- 评价被隐藏（status=2）时是否需要扣回评分

### 验证方式
- 创建评价 → 陪玩详情页评分更新
- 多次评价 → 平均分计算正确

---

# 二、后端业务补全

---

## 2.1 陪玩资料管理 API

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 6h
- **依赖**: 无

### 目标
陪玩自主管理相册、技能价格、标签，无需管理员介入。

### 涉及文件（后端新建）

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/CompanionAlbumController.java` | **新建** |
| `play-app-backend/.../controller/CompanionSkillController.java` | **新建** |
| `play-app-backend/.../controller/CompanionTagController.java` | **新建** |
| `play-app-backend/.../service/CompanionAlbumService.java` + impl | **新建** |
| `play-app-backend/.../service/CompanionSkillService.java` + impl | **新建** |
| `play-app-backend/.../dto/AlbumAddDTO.java` | **新建** |
| `play-app-backend/.../dto/SkillSaveDTO.java` | **新建** |
| `play-app-backend/.../dto/TagUpdateDTO.java` | **新建** |

### 涉及文件（前端新建）

| 文件 | 操作 |
|------|------|
| `play-app-frontend/src/pages/companion/profile-edit.vue` | **新建** |
| `play-app-frontend/src/pages/companion/dashboard.vue` | 修改 — 加入口 |
| `play-app-frontend/src/pages.json` | 修改 — 注册新页面 |

### API 设计

**相册** (`/api/companion/albums`)：
- `GET` — 获取我的相册列表（含封面标记和排序）
- `POST` — 添加照片 `{imageUrl, thumbnailUrl?}`
- `PUT /{id}/cover` — 设为封面
- `DELETE /{id}` — 删除（限制最少保留 1 张）

**技能价格** (`/api/companion/skills`)：
- `GET` — 获取我的技能列表（含品类名）
- `POST` — 添加技能 `{categoryId, pricePerHour, experienceDesc?}`
- `PUT /{id}` — 修改价格/描述
- `DELETE /{id}` — 删除（限制最少保留 1 个）

**标签** (`/api/companion/tags`)：
- `GET /api/tags` — 获取系统所有标签（公开接口，可复用现有逻辑）
- `PUT /api/companion/tags` — 更新自己的标签 `{tagIds: [1,3,5]}`

### 验证方式
- 陪玩编辑相册/价格/标签 → 保存 → 大厅展示更新

---

## 2.2 评价系统完善

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 4h
- **依赖**: 1.7

### 目标
评价支持图片上传和匿名，陪玩可回复评价，陪玩详情页展示真实评价列表。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/ReviewController.java` | **新建** |
| `play-app-backend/.../service/ReviewService.java` | 修改 — 新增方法 |
| `play-app-backend/.../service/impl/ReviewServiceImpl.java` | 修改 — 实现 |
| `play-app-backend/.../dto/ReviewReplyDTO.java` | **新建** |
| `play-app-frontend/src/pages/order/review.vue` | 修改 — 加图片上传和匿名开关 |
| `play-app-frontend/src/pages/companion/detail.vue` | 修改 — 用真实 API 替换 mock |

### API 设计

- `GET /api/companions/{id}/reviews?current=1&size=20` — 陪玩的评价列表（公开）
- `POST /api/reviews/{reviewId}/reply` — 陪玩回复评价 `{content}`
- `POST /api/orders/{orderNo}/review` — 已有，本次增加 `images` 数组和 `isAnonymous`

### 验证方式
- 确认完工 → 评价（带图+匿名）→ 详情页可见评价
- 陪玩回复评价 → 详情页可见回复

---

## 2.3 纠纷处理流程

- **状态**: `[x]` 已完成 <!-- 2026-06-10（简化版）-->
- **预估工时**: 5h
- **依赖**: 1.1

### 目标
实现订单纠纷申诉和处理完整流程（200/210）。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/DisputeController.java` | **新建** |
| `play-app-backend/.../service/impl/OrderServiceImpl.java` | 修改 — 新增 createDispute, resolveDispute |
| `play-app-backend/.../dto/DisputeCreateDTO.java` | **新建** |
| `play-app-backend/.../dto/DisputeResolveDTO.java` | **新建** |
| `play-app-frontend/src/pages/order/dispute.vue` | **新建** |
| `play-app-frontend/src/pages/order/detail.vue` | 修改 — 加申诉入口 |
| `play-app-frontend/src/pages/admin/index.vue` | 修改 — 加纠纷处理 tab |
| `play-app-frontend/src/pages.json` | 修改 — 注册新页面 |

### 实现要点

1. 纠纷数据存 `orders.extra` JSONB：
```json
{
  "dispute": {
    "initiator": "customer",
    "reasonType": "service_quality",
    "description": "服务态度差",
    "evidenceUrls": ["/uploads/xxx.jpg"],
    "createTime": "2026-06-10T12:00:00",
    "resolution": null
  }
}
```

2. `POST /api/orders/{orderNo}/dispute` — 创建纠纷，订单状态 → 200
3. `PUT /api/admin/orders/{orderNo}/dispute` — 管理员处理纠纷：
   - 驳回纠纷 → 恢复原状态 → 210
   - 同意退款 → 触发退款流程 → 210
4. 前端 `dispute.vue`：原因选择器 + 描述框 + 图片上传

### 验证方式
- 订单详情 → 申诉 → 提交 → 状态变 200
- 管理后台 → 处理纠纷 → 状态变 210

---

## 2.4 服务过程时间线

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 6h
- **依赖**: 1.4

### 目标
实现服务过程中的关键节点记录（拍照打卡、凭证上传）。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/ServiceTimelineController.java` | **新建** |
| `play-app-backend/.../service/ServiceTimelineService.java` + impl | **新建** |
| `play-app-backend/.../dto/TimelineEventDTO.java` | **新建** |
| `play-app-frontend/src/pages/order/timeline.vue` | **新建** |
| `play-app-frontend/src/pages/order/detail.vue` | 修改 — 加入口 |
| `play-app-frontend/src/pages.json` | 修改 — 注册新页面 |

### API 设计

- `GET /api/orders/{orderNo}/timeline` — 时间线列表（按时间正序）
- `POST /api/orders/{orderNo}/timeline` — 创建事件 `{eventType, eventDesc, fileUrls?}`
- `POST /api/orders/{orderNo}/evidence` — 上传凭证 `{fileUrl, description?}`

事件类型枚举：
```
1-陪玩出发前往   2-陪玩到达现场   3-用户确认见面
4-服务正式开始   5-中途拍照打卡   6-陪玩申请完工
7-用户确认完工   8-客服介入备注   9-用户发起投诉   10-异常上报
```

### 前端页面
纵向时间线 UI，左侧时间轴竖线 + 圆点，右侧事件卡片。事件类型不同颜色。图片可点击预览。

### 验证方式
- 服务中 → 拍照打卡 → 时间线有记录
- 完工后可查看完整时间线

---

## 2.5 支付流水查询

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 2h
- **依赖**: 无

### 目标
用户和陪玩可查询自己的支付/收款流水。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/PaymentController.java` | **新建** |
| `play-app-backend/.../service/PaymentRecordService.java` + impl | **新建** |
| `play-app-frontend/src/pages/companion/wallet.vue` | 修改 — 加流水入口 |

### API

- `GET /api/payments?current=1&size=20&type=&startDate=&endDate=` — 当前用户关联的流水
- `GET /api/companion/payments?current=1&size=20` — 陪玩收款流水（含订单收入/提现）

### 验证方式
- 有订单记录后 → 支付流水页可见

---

## 2.6 管理员用户管理 API

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h
- **依赖**: 无

### 目标
管理员可查看用户列表、搜索、启停用。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/AdminUserController.java` | **新建** |
| `play-app-backend/.../service/UserService.java` | 修改 — 新增 admin 方法 |
| `play-app-backend/.../service/impl/UserServiceImpl.java` | 修改 — 实现 |
| `play-app-frontend/src/pages/admin/index.vue` | 修改 — 加用户管理 tab |

### API

- `GET /api/admin/users?current=1&size=20&keyword=&status=` — 用户列表
- `GET /api/admin/users/{id}` — 用户详情
- `PUT /api/admin/users/{id}/status` — 启用/禁用 `{status: 1|2}`

### 验证方式
- 管理后台 → 用户列表 → 搜索/禁用/启用

---

## 2.7 管理员统计大盘 API

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 2h
- **依赖**: 1.1~1.6

### 目标
管理后台首页展示平台运营数据概览。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/AdminDashboardController.java` | **新建** |
| `play-app-backend/.../service/AdminDashboardService.java` + impl | **新建** |
| `play-app-frontend/src/pages/admin/index.vue` | 修改 — 首页改为统计面板 |

### API

- `GET /api/admin/dashboard` — 返回：
```json
{
  "todayOrderCount": 12,
  "todayRevenue": 350.00,
  "pendingAuditCount": 3,
  "pendingWithdrawCount": 2,
  "activeCompanionCount": 45,
  "totalUserCount": 1280,
  "disputeCount": 1
}
```

### 验证方式
- 管理后台首页显示统计卡片

---

## 2.8 陪玩标签管理 API（系统级）

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 1h
- **依赖**: 无

### 目标
管理员可管理系统的标签库。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../controller/AdminTagController.java` | **新建** |

### API

- `GET /api/admin/tags` — 标签列表
- `POST /api/admin/tags` — 创建标签 `{name, tagType, color?}`
- `PUT /api/admin/tags/{id}` — 修改
- `DELETE /api/admin/tags/{id}` — 删除

---

# 三、前端重构

---

## 3.1 API 层 + TypeScript 类型

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 5h
- **依赖**: 无（可独立进行）

### 目标
创建 `src/api/` 和 `src/types/`，封装所有后端 API 调用，消除 `any` 类型。

### 新建文件

```
play-app-frontend/src/
├── types/
│   ├── api.ts          # PageResult<T>, Result<T>
│   ├── order.ts        # OrderVO, OrderStatus...
│   ├── companion.ts    # CompanionVO, CompanionDetailVO...
│   └── user.ts         # UserVO, LoginVO...
└── api/
    ├── index.ts        # 导出所有 API 模块
    ├── order.ts        # orderApi
    ├── companion.ts    # companionApi
    ├── user.ts         # userApi
    ├── admin.ts        # adminApi
    ├── payment.ts      # paymentApi
    └── request.ts      # playRequestApi
```

### 示例代码

```typescript
// types/order.ts
export type OrderStatus = 10|20|30|40|50|60|70|80|100|110|120|130|200|210|250

export interface OrderVO {
  orderId: number; orderNo: string
  userId: number; companionId: number; status: OrderStatus
  hours: number; pricePerHour: number; totalAmount: number
  companionAmount: number; platformFee: number
  reserveDate: string; reserveTimeStart: string; address: string
  addressDetail?: string; customerWechat?: string; customerRemark?: string
  cancelReason?: string; cancelType?: number; refundAmount?: number
  wechatGroupStatus: number
  createTime: string; updateTime?: string
}

// api/order.ts
import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type { OrderVO, OrderStatus } from '@/types/order'

export const orderApi = {
  getList(params: { current: number; size: number; status?: OrderStatus }) {
    return request<PageResult<OrderVO>>({ url: '/orders', method: 'GET', data: params })
  },
  getDetail(orderNo: string) {
    return request<OrderVO>({ url: `/orders/${orderNo}`, method: 'GET' })
  },
  create(data: OrderCreateDTO) {
    return request<string>({ url: '/orders', method: 'POST', data })
  },
  prepay(orderNo: string) {
    return request<PrepayResult>({ url: `/orders/${orderNo}/prepay`, method: 'POST' })
  },
  confirm(orderNo: string) {
    return request<void>({ url: `/orders/${orderNo}/confirm`, method: 'PUT' })
  },
  cancel(orderNo: string) {
    return request<void>({ url: `/orders/${orderNo}/cancel`, method: 'PUT' })
  },
  refund(orderNo: string, reason: string) {
    return request<void>({ url: `/orders/${orderNo}/refund`, method: 'POST', data: { reason } })
  },
  review(orderNo: string, data: ReviewDTO) {
    return request<void>({ url: `/orders/${orderNo}/review`, method: 'POST', data })
  },
}
```

### 修改范围
所有页面的内联 `request()` 调用改为 `xxxApi.yyy()` 调用。大约涉及 14 个页面文件。

### 验证方式
- `npm run dev:mp-weixin` 编译无 TS 错误
- 每个页面功能正常

---

## 3.2 公共组件抽取

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 5h
- **依赖**: 3.1

### 目标
抽取重复 UI → 公共组件，减少代码重复，统一视觉。

### 新建组件

```text
src/components/
├── CompanionCard.vue     # 陪玩卡片，props: {companion, showVoice?}
├── OrderCard.vue         # 订单卡片，props: {order, role}, emits: [pay, cancel, confirm...]
├── StarRating.vue        # 星级评分，props: {value, size?, readonly?}, emits: [change]
├── StatusBadge.vue       # 状态标签，props: {status, type}
├── SkeletonCard.vue      # 骨架屏，props: {count?}
├── EmptyState.vue        # 空状态，props: {icon?, text}
├── ImageUploader.vue     # 图片上传网格，props: {maxCount?, value}, emits: [change]
└── VoicePlayer.vue       # 语音播放条，props: {url, duration?}
```

### 涉及修改的页面

| 页面 | 替换为组件 |
|------|----------|
| index/index.vue | CompanionCard, VoicePlayer, EmptyState |
| companion/list.vue | CompanionCard, SkeletonCard |
| companion/detail.vue | StarRating, VoicePlayer |
| companion/apply.vue | ImageUploader |
| order/list.vue | OrderCard, StatusBadge, EmptyState |
| order/detail.vue | StatusBadge |
| order/review.vue | StarRating, ImageUploader |
| companion/dashboard.vue | OrderCard, StatusBadge |
| mine/index.vue | SkeletonCard |

### 验证方式
- 替换后视觉效果和交互与原来一致

---

## 3.3 搜索筛选排序功能实现

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h
- **依赖**: 3.1

### 目标
首页搜索框、陪玩列表筛选/排序真正生效。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../service/impl/CompanionProfileServiceImpl.java` | 修改 — 支持 keyword/gender/sortBy |
| `play-app-backend/.../controller/PublicCompanionController.java` | 修改 — 增加 query 参数 |
| `play-app-frontend/src/pages/index/index.vue` | 修改 — 搜索框绑定 |
| `play-app-frontend/src/pages/companion/list.vue` | 修改 — 筛选排序传参 |

### 实现要点

1. API `GET /api/companions` 增加参数：
   - `keyword` — 搜索昵称、简介
   - `gender` — 性别筛选
   - `sortBy` — recommend / rating / price / newest

2. 后端 `LambdaQueryWrapper` 动态构建：
```java
if (StringUtils.hasText(keyword)) {
    wrapper.and(w -> w.like(CompanionProfile::getNickname, keyword)
        .or().like(CompanionProfile::getSummary, keyword));
}
```

3. 前端：index 页搜索框 `@confirm` 跳转到 list 页并传参；list 页筛选栏点击后重新请求。

### 验证方式
- 搜索关键词 → 结果过滤
- 切换排序 → 列表重排
- 性别筛选 → 结果过滤

---

# 四、体验优化

---

## 4.1 管理后台增强

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 4h
- **依赖**: 2.7, 3.1

### 目标
管理后台增加搜索筛选分页、统计面板、用户管理 Tab。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-frontend/src/pages/admin/index.vue` | 修改 — 大幅重构 |

### 重构内容

1. Tab 结构调整：统计概览 / 入驻审核 / 订单管理 / 提现审核 / 需求跟进 / 风控举报 / 纠纷处理(如果做了2.3) / 用户管理
2. 统计数据在上方卡片展示
3. 各列表增加：顶部搜索框、下拉刷新、触底加载更多
4. 状态筛选改为 radio 或下拉，传参给 API

### 验证方式
- 管理后台搜索/翻页/刷新正常
- 统计数字正确

---

## 4.2 陪玩详情页完善

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h
- **依赖**: 2.2, 3.1

### 目标
详情页接入真实数据（评价、相册），增加分享和举报功能。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-frontend/src/pages/companion/detail.vue` | 修改 — 真实评价 API + 功能补全 |
| `play-app-backend/.../controller/PublicCompanionController.java` | 修改 — detail 接口返回更多数据 |

### 修改内容

1. 评价列表从 mock 改为 `GET /api/companions/{id}/reviews`
2. 相册使用 `companion_albums` 表数据（目前可能内嵌在 profile 的 photoUrls）
3. 增加「分享」按钮（`uni.share` 或 `<button open-type="share">`）
4. 增加「举报」按钮（调用 `POST /api/risk/reports`）

### 验证方式
- 详情页评价列表展示真实数据
- 分享/举报功能可用

---

## 4.3 下单流程优化

- **状态**: `[ ]` 未开始
- **预估工时**: 3h
- **依赖**: 1.2, 3.1

### 目标
下单页完善时间选择、费用明细展示，支付页修复倒计时。

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 3h

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-frontend/src/pages/order/create.vue` | 修改 |
| `play-app-frontend/src/pages/order/pay.vue` | 修改 |

### 修改内容

**create.vue**：
- 费用明细从 API 返回数据取真实值（platformFee, companionAmount）
- 时间槽保留本地生成方式，但标注"具体时间以客服确认为准"
- 品类选择改为从 `GET /api/categories` 获取并展示

**pay.vue**：
- 倒计时改为真实 15 分钟倒计时（`setInterval`）
- 倒计时归零后自动跳回订单列表
- 保留 mock 支付模式（等微信对接后切换）

### 验证方式
- 下单 → 费用明细正确 → 支付页倒计时走秒 → 归零后跳转

---

## 4.4 需求大厅功能完善

- **状态**: `[ ]` 未开始
- **预估工时**: 2h
- **依赖**: 3.1

### 目标
首页需求列表去掉 mock 数据，完善发布和应约功能。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-frontend/src/pages/index/index.vue` | 修改 — 去 mock + 加功能 |
| `play-app-backend/.../controller/PlayRequestController.java` | 检查 API 是否完整 |

### 修改内容

1. 需求列表去掉 mock fallback，只展示 API 数据
2. 增加下拉刷新和加载更多
3. 「我要应约」按钮目前只弹 toast，后续可关联客服消息或跳转下单
4. 发布需求弹窗完善：预算输入、时间选择

### 验证方式
- 需求列表从 API 加载 → 下拉刷新 → 加载更多

---

## 4.5 个人中心功能完善

- **状态**: `[ ]` 未开始
- **预估工时**: 2h
- **依赖**: 3.1

### 目标
个人中心增加头像上传、退出登录、版本信息。

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-frontend/src/pages/mine/index.vue` | 修改 |

### 修改内容

1. 头像上传：点击头像 → `uni.chooseImage` → `uni.uploadFile` → `PUT /user/profile`
2. 退出登录：清除 token 和 userInfo，回到首页
3. 底部显示版本号（从 `manifest.json` 读取）
4. mock 登录保留但加 dev 标识

### 验证方式
- 上传头像 → 刷新显示新头像
- 退出登录 → 回到未登录状态

---

# 五、代码质量

---

## 5.1 Swagger API 文档

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 1h
- **依赖**: 无

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/pom.xml` | 修改 — 加依赖 |
| `play-app-backend/src/main/resources/application.yml` | 修改 — 加配置 |

### 步骤

1. 添加依赖：
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

2. 配置：
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

3. 启动后访问 `http://localhost:8080/swagger-ui.html`

### 验证方式
- 浏览器打开 Swagger UI → 所有 API 列出 → 可在线测试

---

## 5.2 单元测试

- **状态**: `[x]` 已完成 <!-- 2026-06-10 -->
- **预估工时**: 6h
- **依赖**: 1.1, 1.2

### 目标
为核心 Service 编写单元测试，覆盖关键业务逻辑。

### 新建文件

| 文件 |
|------|
| `play-app-backend/src/test/java/com/playapp/service/OrderServiceImplTest.java` |
| `play-app-backend/src/test/java/com/playapp/service/CompanionWalletServiceImplTest.java` |
| `play-app-backend/src/test/java/com/playapp/service/ReviewServiceImplTest.java` |

### 测试重点

- **OrderServiceImpl**：状态转移合法性、平台费计算、退款金额计算
- **CompanionWalletServiceImpl**：余额变动、冻结/解冻、提现流程
- **ReviewServiceImpl**：评价创建、评分更新、回复评价

使用 JUnit 5 + Mockito + H2 内存数据库。

### 验证方式
- `mvn test` 全部通过

---

## 5.3 代码清理与 review 修复

- **状态**: `[x]` 已完成 <!-- 2026-06-10（编译通过无错误，仅Lombok警告）-->
- **预估工时**: 3h
- **依赖**: 以上各任务

### 内容

1. 清理未使用的 import
2. 统一异常处理（所有 Service 抛 BusinessException 而非 RuntimeException）
3. 统一日志格式（关键操作 info、异常 error）
4. 修复硬编码 URL（如入驻页的上传地址）
5. 检查 SQL 注入风险（MyBatis-Plus 动态查询）
6. 统一命名规范（controller 路径、方法名）

---

# 🔒 六、微信对接（最高优先级，待资质就绪后立即执行）

> 以下任务需要：小程序 AppID + AppSecret + 商户号 + APIv3 密钥。Mock 模式下的功能已全部可用，微信对接是上线前的最后关口。

---

## 🔒 6.1 微信登录真实对接

- **状态**: `[ ]` 🔒 待微信资质
- **预估工时**: 4h
- **依赖**: 需小程序 AppID + AppSecret

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/src/main/resources/application.yml` | 修改 — 真实 appid/secret |
| `play-app-backend/.../service/impl/UserServiceImpl.java` | 修改 — wxLogin 用真实 jscode2session |
| `play-app-frontend/src/store/user.ts` | 修改 — H5 外走真实微信登录 |
| `play-app-frontend/src/pages/mine/index.vue` | 修改 — 去掉 mock 降级逻辑 |

### 要点
- `WxMaService.getUserService().getSessionInfo(code)` 已在代码中
- `POST /api/wx/mock-login` 保留用于开发环境
- 前端 `uni.login({ provider: 'weixin' })` → code → `/api/wx/login`

---

## 🔒 6.2 微信支付真实对接

- **状态**: `[ ]` 🔒 待微信商户号
- **预估工时**: 8h
- **依赖**: 需商户号 + APIv3 密钥 + 证书

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../service/impl/OrderServiceImpl.java` | 修改 — getWxPrepayInfo 真实下单 |
| `play-app-backend/.../controller/WxPayController.java` | 修改 — notify 验签 |
| `play-app-backend/src/main/resources/application.yml` | 修改 — 商户配置 |
| `play-app-frontend/src/pages/order/pay.vue` | 修改 — 去 mock，真实 requestPayment |

### 要点
- 使用 `wx-java-pay-spring-boot-starter`（已引入）
- JSAPI 下单 → prepay_id → 前端签名 → `uni.requestPayment`
- 回调验签 → 更新 order status 10→20 → 记 PaymentRecord

---

## 🔒 6.3 提现真实打款

- **状态**: `[ ]` 🔒 待微信商户号
- **预估工时**: 4h
- **依赖**: 6.2

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../service/impl/CompanionWalletServiceImpl.java` | 修改 — auditWithdrawal 调微信转账 |

### 要点
- 审核通过后调微信商家转账到零钱 API
- 成功 → 扣 frozenAmount、更新 status=2
- 失败 → 退回余额、status=3

---

## 🔒 6.4 微信订阅消息通知

- **状态**: `[ ]` 🔒 待小程序 AppID
- **预估工时**: 5h
- **依赖**: 6.1

### 涉及文件

| 文件 | 操作 |
|------|------|
| `play-app-backend/.../service/NotificationService.java` + impl | **新建** |
| `play-app-backend/.../service/impl/OrderServiceImpl.java` | 修改 — 状态变更后发通知 |
| `play-app-frontend/src/pages/mine/index.vue` | 修改 — 订阅授权入口 |

### 要点
- 申请订阅消息模板（订单状态通知、退款通知、完工提醒）
- 前端 `uni.requestSubscribeMessage` 获取授权
- 后端 `@Async` 异步发送

---

# 📋 附录

## A. 建议开发顺序

```
1.1 状态机校验 ──→ 1.2 平台费计算 ──→ 1.3 超时关闭
    │                    │
    ▼                    ▼
1.4 陪玩完工 ←── 1.5 Admin操作补全 ←── 1.6 退款计算 ←── 1.7 评分确认
    │
    ▼
2.1 资料管理API ──→ 2.2 评价完善 ──→ 2.3 纠纷处理
    │
    ▼
2.4 时间线 ──→ 2.5 支付流水 ──→ 2.6 用户管理 ──→ 2.7 统计大盘 ──→ 2.8 标签管理
    │
    ▼
3.1 API层+TS ──→ 3.2 公共组件 ──→ 3.3 搜索筛选
    │
    ▼
4.1 管理后台增强 ──→ 4.2 详情页完善 ──→ 4.3 下单优化 ──→ 4.4 需求大厅 ──→ 4.5 个人中心
    │
    ▼
5.1 Swagger ──→ 5.2 单元测试 ──→ 5.3 代码清理
    │
    ▼
🔒 6.1~6.4 微信对接（资质就绪后立即执行）
```

## B. Git 提交规范

```bash
feat: 新增xxx功能        # 新功能
fix: 修复xxx问题         # Bug 修复
refactor: 重构xxx        # 代码重构
chore: 更新依赖/配置      # 杂项
docs: 更新文档           # 文档
test: 添加测试           # 测试
```

## C. 数据库迁移

新增表/改表结构 → `play-app-backend/src/main/resources/db/migration/V{序号}__{描述}.sql`（当前 V5 起）

---

> **最后更新**: 2026-06-10
> **搜索 `[ ]` 定位下一个待做任务**
> **搜索 `🔒` 查看微信对接任务**
