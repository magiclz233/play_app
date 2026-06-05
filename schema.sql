-- ================================================================
-- 同城伴玩私域担保平台 - 全链路数据库设计 (PostgreSQL)
-- ================================================================
-- 业务链路: 用户浏览 → 下单付款 → 客服拉群 → 线下履约 → 
--          过程记录 → 完工确认 → 用户评价 → 平台结算 → 陪玩提现
-- ================================================================

-- ================================================================
-- 一、用户与权限模块 (RBAC)
-- ================================================================

-- 1. 用户基本信息表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    openid VARCHAR(64) NOT NULL UNIQUE,
    unionid VARCHAR(64) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    gender SMALLINT DEFAULT 0,
    city VARCHAR(50) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    last_login_time TIMESTAMP DEFAULT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_extra ON users USING gin(extra);

COMMENT ON TABLE users IS '用户基本信息表';
COMMENT ON COLUMN users.openid IS '微信小程序openid';
COMMENT ON COLUMN users.unionid IS '微信unionid (多端打通用)';
COMMENT ON COLUMN users.gender IS '性别: 0-未知, 1-男, 2-女';
COMMENT ON COLUMN users.city IS '所在城市';
COMMENT ON COLUMN users.status IS '状态: 1-正常, 2-禁用, 3-注销';
COMMENT ON COLUMN users.last_login_time IS '最近登录时间';
COMMENT ON COLUMN users.extra IS '扩展字段 (JSONB)';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    remark VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE roles IS '系统角色表';
COMMENT ON COLUMN roles.code IS '角色代码 (如 ROLE_ADMIN, ROLE_COMPANION, ROLE_CUSTOMER)';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    parent_id INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE permissions IS '系统权限表';
COMMENT ON COLUMN permissions.permission_code IS '权限标识符 (如 admin:order:settle)';

-- 4. 用户-角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 5. 角色-权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);


-- ================================================================
-- 二、服务品类与标签模块
-- ================================================================

-- 6. 服务分类表 (台球、桌游、电竞、同城向导等)
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon_url VARCHAR(255) DEFAULT NULL,
    cover_url VARCHAR(255) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE categories IS '服务分类表';
COMMENT ON COLUMN categories.cover_url IS '分类封面图';
COMMENT ON COLUMN categories.description IS '分类说明';

-- 7. 系统标签库表
CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    tag_type SMALLINT NOT NULL DEFAULT 1,
    color VARCHAR(20) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tags IS '系统标签库表';
COMMENT ON COLUMN tags.tag_type IS '类型: 1-个性标签(温柔/幽默), 2-技能标签(王者百星), 3-服务标签(准时/高颜值)';
COMMENT ON COLUMN tags.color IS '标签颜色值 (前端展示用)';


-- ================================================================
-- 三、陪玩信息模块
-- ================================================================

-- 8. 陪玩详细资料表
CREATE TABLE IF NOT EXISTS companion_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    real_name VARCHAR(50) NOT NULL,
    id_card_no VARCHAR(18) DEFAULT NULL,
    wechat_code VARCHAR(50) NOT NULL,
    voice_url VARCHAR(255) DEFAULT NULL,
    voice_duration INT DEFAULT 0,
    gender SMALLINT NOT NULL DEFAULT 2,
    age SMALLINT DEFAULT NULL,
    height SMALLINT DEFAULT NULL,
    summary TEXT DEFAULT NULL,
    service_area VARCHAR(100) DEFAULT NULL,
    work_status SMALLINT NOT NULL DEFAULT 1,
    rating DECIMAL(3,2) NOT NULL DEFAULT 5.00,
    total_rating_count INT NOT NULL DEFAULT 0,
    total_rating_score INT NOT NULL DEFAULT 0,
    order_count INT NOT NULL DEFAULT 0,
    audit_status SMALLINT NOT NULL DEFAULT 0,
    reject_reason VARCHAR(255) DEFAULT NULL,
    is_recommend BOOLEAN NOT NULL DEFAULT FALSE,
    sort_weight INT NOT NULL DEFAULT 0,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_companion_audit ON companion_profiles(audit_status);
CREATE INDEX idx_companion_work ON companion_profiles(work_status);
CREATE INDEX idx_companion_extra ON companion_profiles USING gin(extra);

COMMENT ON TABLE companion_profiles IS '陪玩详细资料表';
COMMENT ON COLUMN companion_profiles.id_card_no IS '身份证号 (加密存储，后期实名认证用)';
COMMENT ON COLUMN companion_profiles.age IS '年龄';
COMMENT ON COLUMN companion_profiles.height IS '身高(cm)';
COMMENT ON COLUMN companion_profiles.service_area IS '服务区域 (如: 朝阳区/海淀区)';
COMMENT ON COLUMN companion_profiles.work_status IS '接单状态: 1-接单中, 2-忙碌中, 3-休息中';
COMMENT ON COLUMN companion_profiles.total_rating_count IS '累计评价人数 (用于计算平均分)';
COMMENT ON COLUMN companion_profiles.total_rating_score IS '累计评价总分 (rating = total_score / total_count)';
COMMENT ON COLUMN companion_profiles.is_recommend IS '是否首页推荐';
COMMENT ON COLUMN companion_profiles.sort_weight IS '排序权重 (越大越靠前，管理员可调)';
COMMENT ON COLUMN companion_profiles.audit_status IS '审核状态: 0-待审核, 1-审核通过, 2-已驳回, 3-已冻结';

-- 9. 陪玩相册表 (独立出来，支持排序和审核)
CREATE TABLE IF NOT EXISTS companion_albums (
    id BIGSERIAL PRIMARY KEY,
    companion_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_cover BOOLEAN NOT NULL DEFAULT FALSE,
    audit_status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_album_companion ON companion_albums(companion_id);

COMMENT ON TABLE companion_albums IS '陪玩相册表';
COMMENT ON COLUMN companion_albums.thumbnail_url IS '缩略图链接';
COMMENT ON COLUMN companion_albums.is_cover IS '是否封面主图';
COMMENT ON COLUMN companion_albums.audit_status IS '审核状态: 0-待审核, 1-正常, 2-违规已屏蔽';

-- 10. 陪玩技能单价表
CREATE TABLE IF NOT EXISTS companion_skills (
    id BIGSERIAL PRIMARY KEY,
    companion_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    price_per_hour DECIMAL(10,2) NOT NULL,
    experience_desc VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(companion_id, category_id)
);

COMMENT ON TABLE companion_skills IS '陪玩技能单价关联表';
COMMENT ON COLUMN companion_skills.experience_desc IS '技能段位/经验描述';

-- 11. 陪玩-标签关联表
CREATE TABLE IF NOT EXISTS companion_tag_relations (
    companion_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_id INT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (companion_id, tag_id)
);


-- ================================================================
-- 四、订单与服务过程模块
-- ================================================================

-- 12. 交易订单表 (核心主表)
CREATE TABLE IF NOT EXISTS orders (
    order_id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    companion_id BIGINT NOT NULL REFERENCES users(id),
    category_id INT NOT NULL REFERENCES categories(id),
    
    -- 服务与定价
    hours DECIMAL(4,1) NOT NULL,
    price_per_hour DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    companion_amount DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL,
    
    -- 预约信息
    reserve_date DATE NOT NULL,
    reserve_time_start TIME NOT NULL,
    reserve_time_end TIME DEFAULT NULL,
    address VARCHAR(255) NOT NULL,
    address_detail VARCHAR(255) DEFAULT NULL,
    
    -- 客户沟通信息
    customer_wechat VARCHAR(50) DEFAULT NULL,
    customer_remark TEXT DEFAULT NULL,
    
    -- 订单状态
    status SMALLINT NOT NULL DEFAULT 10,
    wechat_group_status SMALLINT NOT NULL DEFAULT 0,
    
    -- 取消与退款
    cancel_reason VARCHAR(255) DEFAULT NULL,
    cancel_type SMALLINT DEFAULT NULL,
    refund_amount DECIMAL(10,2) DEFAULT NULL,
    
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_companion ON orders(companion_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_reserve_date ON orders(reserve_date);
CREATE INDEX idx_orders_extra ON orders USING gin(extra);

COMMENT ON TABLE orders IS '交易订单表';
COMMENT ON COLUMN orders.reserve_date IS '预约服务日期';
COMMENT ON COLUMN orders.reserve_time_start IS '预约开始时间';
COMMENT ON COLUMN orders.reserve_time_end IS '预约结束时间';
COMMENT ON COLUMN orders.address_detail IS '详细地址/门牌/包间号';
COMMENT ON COLUMN orders.customer_remark IS '客户特殊备注';
COMMENT ON COLUMN orders.status IS '订单状态:
  10-待付款,
  20-已付款/待客服拉群,
  30-客服已拉群/待沟通,
  40-双方已确认/待服务,
  50-服务进行中,
  60-陪玩发起完工申请,
  70-用户已确认完工/待结算,
  80-平台已放款/订单完成,
  100-用户申请取消,
  110-退款处理中,
  120-全额退款完成,
  130-部分退款完成(扣违约金),
  200-纠纷申诉中,
  210-纠纷处理完成,
  250-超时未支付/自动关闭';
COMMENT ON COLUMN orders.wechat_group_status IS '三方群状态: 0-未拉群, 1-已拉群, 2-已解散';
COMMENT ON COLUMN orders.cancel_reason IS '取消原因描述';
COMMENT ON COLUMN orders.cancel_type IS '取消方: 1-用户主动取消, 2-陪玩取消, 3-平台介入取消';
COMMENT ON COLUMN orders.refund_amount IS '实际退款金额';

-- 13. 订单状态变更日志表 (审计追踪)
CREATE TABLE IF NOT EXISTS order_status_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    from_status SMALLINT DEFAULT NULL,
    to_status SMALLINT NOT NULL,
    operator_id BIGINT NOT NULL DEFAULT 0,
    operator_role SMALLINT NOT NULL,
    change_reason VARCHAR(255) DEFAULT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status_logs_order ON order_status_logs(order_id);

COMMENT ON TABLE order_status_logs IS '订单状态变更日志表';
COMMENT ON COLUMN order_status_logs.operator_id IS '操作人ID (0=系统自动)';
COMMENT ON COLUMN order_status_logs.operator_role IS '操作角色: 1-客户, 2-陪玩, 3-客服管理员, 4-系统自动';

-- 14. 服务过程记录表 (每笔订单的线下服务实际执行记录)
CREATE TABLE IF NOT EXISTS service_records (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(order_id) ON DELETE CASCADE,
    companion_id BIGINT NOT NULL REFERENCES users(id),
    
    -- 实际服务时间
    actual_start_time TIMESTAMP DEFAULT NULL,
    actual_end_time TIMESTAMP DEFAULT NULL,
    actual_duration INT DEFAULT NULL,
    actual_address VARCHAR(255) DEFAULT NULL,
    
    -- 完工信息
    finish_remark VARCHAR(500) DEFAULT NULL,
    finish_type SMALLINT DEFAULT NULL,
    
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_companion ON service_records(companion_id);

COMMENT ON TABLE service_records IS '服务过程记录表';
COMMENT ON COLUMN service_records.actual_start_time IS '实际服务开始时间';
COMMENT ON COLUMN service_records.actual_end_time IS '实际服务结束时间';
COMMENT ON COLUMN service_records.actual_duration IS '实际服务时长(分钟)';
COMMENT ON COLUMN service_records.actual_address IS '实际服务地点';
COMMENT ON COLUMN service_records.finish_remark IS '完工备注 (陪玩填写)';
COMMENT ON COLUMN service_records.finish_type IS '完工类型: 1-正常完工, 2-提前结束(双方同意), 3-超时结束';

-- 15. 服务过程时间线事件表 (记录服务期间的关键节点)
CREATE TABLE IF NOT EXISTS service_timeline_events (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    event_type SMALLINT NOT NULL,
    event_desc VARCHAR(500) DEFAULT NULL,
    operator_id BIGINT NOT NULL DEFAULT 0,
    operator_role SMALLINT NOT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_timeline_order ON service_timeline_events(order_id);

COMMENT ON TABLE service_timeline_events IS '服务过程时间线事件表';
COMMENT ON COLUMN service_timeline_events.event_type IS '事件类型:
  1-陪玩出发前往,
  2-陪玩到达现场,
  3-用户确认见面,
  4-服务正式开始,
  5-中途拍照打卡(凭证),
  6-陪玩申请完工,
  7-用户确认完工,
  8-客服介入备注,
  9-用户发起投诉,
  10-陪玩发起异常上报';
COMMENT ON COLUMN service_timeline_events.operator_role IS '操作角色: 1-客户, 2-陪玩, 3-客服, 4-系统';

-- 16. 服务凭证/证据表 (拍照打卡、合照凭证、投诉截图等)
CREATE TABLE IF NOT EXISTS service_evidence (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    timeline_event_id BIGINT DEFAULT NULL REFERENCES service_timeline_events(id),
    file_type SMALLINT NOT NULL DEFAULT 1,
    file_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) DEFAULT NULL,
    uploader_id BIGINT NOT NULL REFERENCES users(id),
    uploader_role SMALLINT NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evidence_order ON service_evidence(order_id);

COMMENT ON TABLE service_evidence IS '服务凭证/证据表';
COMMENT ON COLUMN service_evidence.timeline_event_id IS '关联时间线事件ID (可选)';
COMMENT ON COLUMN service_evidence.file_type IS '文件类型: 1-图片, 2-视频, 3-语音';
COMMENT ON COLUMN service_evidence.uploader_role IS '上传角色: 1-客户, 2-陪玩, 3-客服';
COMMENT ON COLUMN service_evidence.description IS '凭证说明';


-- ================================================================
-- 五、评价模块
-- ================================================================

-- 17. 用户评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(order_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    companion_id BIGINT NOT NULL REFERENCES users(id),
    rating SMALLINT NOT NULL,
    content TEXT DEFAULT NULL,
    tag_ids JSONB DEFAULT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    reply_content TEXT DEFAULT NULL,
    reply_time TIMESTAMP DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_companion ON reviews(companion_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);

COMMENT ON TABLE reviews IS '用户评价表';
COMMENT ON COLUMN reviews.rating IS '评分: 1-5星';
COMMENT ON COLUMN reviews.content IS '评价文字内容';
COMMENT ON COLUMN reviews.tag_ids IS '评价标签ID列表 (JSONB数组，如[1,3,5]，关联tags表)';
COMMENT ON COLUMN reviews.is_anonymous IS '是否匿名评价';
COMMENT ON COLUMN reviews.reply_content IS '陪玩回复评价内容';
COMMENT ON COLUMN reviews.reply_time IS '回复时间';
COMMENT ON COLUMN reviews.status IS '状态: 1-正常, 2-隐藏, 3-违规屏蔽';


-- ================================================================
-- 六、财务与结算模块
-- ================================================================

-- 18. 支付流水表 (记录微信支付的每一笔支付/退款事务)
CREATE TABLE IF NOT EXISTS payment_records (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(order_id),
    payment_no VARCHAR(64) NOT NULL UNIQUE,
    wx_transaction_id VARCHAR(64) DEFAULT NULL,
    payment_type SMALLINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    paid_time TIMESTAMP DEFAULT NULL,
    notify_data TEXT DEFAULT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_order ON payment_records(order_id);

COMMENT ON TABLE payment_records IS '支付流水表';
COMMENT ON COLUMN payment_records.payment_no IS '平台内部支付流水号';
COMMENT ON COLUMN payment_records.wx_transaction_id IS '微信支付交易号';
COMMENT ON COLUMN payment_records.payment_type IS '支付类型: 1-用户付款, 2-平台退款(全额), 3-平台退款(部分), 4-平台放款给陪玩';
COMMENT ON COLUMN payment_records.status IS '状态: 0-待处理, 1-成功, 2-失败, 3-已关闭';
COMMENT ON COLUMN payment_records.notify_data IS '微信支付回调原始数据 (用于对账)';

-- 19. 陪玩钱包表 (每个陪玩一个虚拟钱包)
CREATE TABLE IF NOT EXISTS companion_wallets (
    companion_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    frozen_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_income DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_withdrawn DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE companion_wallets IS '陪玩钱包表';
COMMENT ON COLUMN companion_wallets.balance IS '可提现余额';
COMMENT ON COLUMN companion_wallets.frozen_amount IS '冻结金额 (待结算的订单收入)';
COMMENT ON COLUMN companion_wallets.total_income IS '累计总收入';
COMMENT ON COLUMN companion_wallets.total_withdrawn IS '累计已提现金额';

-- 20. 钱包流水明细表 (记录钱包余额的每一次变动)
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    companion_id BIGINT NOT NULL REFERENCES users(id),
    order_id BIGINT DEFAULT NULL REFERENCES orders(order_id),
    transaction_type SMALLINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    balance_before DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallet_tx_companion ON wallet_transactions(companion_id);
CREATE INDEX idx_wallet_tx_order ON wallet_transactions(order_id);

COMMENT ON TABLE wallet_transactions IS '钱包流水明细表';
COMMENT ON COLUMN wallet_transactions.transaction_type IS '流水类型:
  1-订单收入(从冻结转入可提现),
  2-提现扣款,
  3-平台扣罚(违约金),
  4-平台奖励/补贴,
  5-退款回滚(订单退款导致收入冲销)';
COMMENT ON COLUMN wallet_transactions.balance_before IS '变动前余额';
COMMENT ON COLUMN wallet_transactions.balance_after IS '变动后余额';

-- 21. 提现申请表
CREATE TABLE IF NOT EXISTS withdrawal_records (
    id BIGSERIAL PRIMARY KEY,
    companion_id BIGINT NOT NULL REFERENCES users(id),
    amount DECIMAL(10,2) NOT NULL,
    fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    actual_amount DECIMAL(10,2) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    wx_payment_no VARCHAR(64) DEFAULT NULL,
    reject_reason VARCHAR(255) DEFAULT NULL,
    auditor_id BIGINT DEFAULT NULL,
    audit_time TIMESTAMP DEFAULT NULL,
    paid_time TIMESTAMP DEFAULT NULL,
    extra JSONB DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_withdraw_companion ON withdrawal_records(companion_id);
CREATE INDEX idx_withdraw_status ON withdrawal_records(status);

COMMENT ON TABLE withdrawal_records IS '提现申请表';
COMMENT ON COLUMN withdrawal_records.amount IS '申请提现金额';
COMMENT ON COLUMN withdrawal_records.fee IS '手续费';
COMMENT ON COLUMN withdrawal_records.actual_amount IS '实际到账金额';
COMMENT ON COLUMN withdrawal_records.status IS '状态: 0-待审核, 1-审核通过/打款中, 2-打款成功, 3-打款失败, 4-审核驳回';
COMMENT ON COLUMN withdrawal_records.wx_payment_no IS '微信商家转账流水号';
COMMENT ON COLUMN withdrawal_records.auditor_id IS '审核人(管理员ID)';


-- ================================================================
-- 七、基础数据初始化
-- ================================================================

-- 角色初始化
INSERT INTO roles (name, code, remark) VALUES 
('系统管理员', 'ROLE_ADMIN', '平台运营管理人员，拥有全部权限'),
('认证陪玩', 'ROLE_COMPANION', '通过入驻审核的陪玩人员'),
('普通客户', 'ROLE_CUSTOMER', '注册的普通消费用户')
ON CONFLICT (code) DO NOTHING;

-- 权限初始化
INSERT INTO permissions (name, permission_code, parent_id) VALUES
-- 管理员权限
('管理中心', 'admin:dashboard', 0),
('入驻审核', 'admin:companion:audit', 0),
('相册审核', 'admin:album:audit', 0),
('完工核销与放款', 'admin:order:settle', 0),
('退款仲裁', 'admin:order:refund', 0),
('提现审核', 'admin:withdraw:audit', 0),
('系统配置', 'admin:system:config', 0),
-- 陪玩权限
('修改个人资料', 'companion:profile:update', 0),
('自助入驻申请', 'companion:apply:submit', 0),
('发起完工', 'companion:order:finish', 0),
('申请提现', 'companion:wallet:withdraw', 0),
-- 客户权限
('下单预约', 'customer:order:create', 0),
('取消预约', 'customer:order:cancel', 0),
('确认完工', 'customer:order:confirm', 0),
('发表评价', 'customer:review:create', 0)
ON CONFLICT (permission_code) DO NOTHING;

-- 品类初始化
INSERT INTO categories (name, sort_order) VALUES
('台球搭子', 10),
('桌游搭子', 20),
('电竞搭子', 30),
('运动健身', 40),
('同城向导', 50),
('剧本杀搭子', 60),
('逛街购物', 70),
('KTV唱歌', 80)
ON CONFLICT (name) DO NOTHING;

-- 标签初始化
INSERT INTO tags (name, tag_type) VALUES
-- 个性标签
('温柔体贴', 1),
('活泼开朗', 1),
('幽默搞笑', 1),
('话多健谈', 1),
('安静内敛', 1),
('颜值高', 1),
-- 技能标签
('台球高手', 2),
('王者百星', 2),
('剧本杀老手', 2),
('英语流利', 2),
-- 服务标签
('准时到达', 3),
('好评如潮', 3),
('回头客多', 3)
ON CONFLICT (name) DO NOTHING;
