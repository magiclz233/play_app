-- 需求大厅：把首页模拟数据落为真实业务数据
CREATE TABLE IF NOT EXISTS play_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    nickname VARCHAR(64),
    avatar_url VARCHAR(512),
    description VARCHAR(500) NOT NULL,
    reserve_time VARCHAR(100),
    address VARCHAR(200),
    budget NUMERIC(10,2) DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 0,
    contact_status INTEGER NOT NULL DEFAULT 0,
    assigned_admin_id BIGINT,
    admin_remark VARCHAR(500),
    extra JSONB DEFAULT '{}',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_play_requests_user ON play_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_play_requests_status ON play_requests(status);
CREATE INDEX IF NOT EXISTS idx_play_requests_create_time ON play_requests(create_time);

COMMENT ON TABLE play_requests IS '陪伴需求大厅';
COMMENT ON COLUMN play_requests.status IS '状态: 0-待响应, 1-客服跟进中, 2-已成单, 3-已关闭';
COMMENT ON COLUMN play_requests.contact_status IS '客服跟进状态: 0-未跟进, 1-已联系, 2-已建群, 3-无效需求';

-- 风控举报：用户、助教、订单、内容均可进入一个基础队列
CREATE TABLE IF NOT EXISTS risk_reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    target_type INTEGER NOT NULL,
    target_id BIGINT,
    order_id BIGINT REFERENCES orders(order_id),
    reason VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    evidence_urls JSONB DEFAULT '[]',
    status INTEGER NOT NULL DEFAULT 0,
    handler_id BIGINT,
    handle_result VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_risk_reports_reporter ON risk_reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_risk_reports_status ON risk_reports(status);
CREATE INDEX IF NOT EXISTS idx_risk_reports_target ON risk_reports(target_type, target_id);

COMMENT ON TABLE risk_reports IS '风控举报与申诉线索';
COMMENT ON COLUMN risk_reports.target_type IS '目标类型: 1-用户, 2-助教, 3-订单, 4-评价/内容, 5-需求';
COMMENT ON COLUMN risk_reports.status IS '状态: 0-待处理, 1-处理中, 2-已处理, 3-驳回';

-- 后台审计：管理员关键动作统一留痕
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(80),
    remark VARCHAR(1000),
    before_data JSONB DEFAULT '{}',
    after_data JSONB DEFAULT '{}',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_admin ON admin_audit_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_target ON admin_audit_logs(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_create_time ON admin_audit_logs(create_time);

COMMENT ON TABLE admin_audit_logs IS '后台关键操作审计日志';

-- 支付/钱包幂等保护
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_records_payment_no ON payment_records(payment_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_records_wx_transaction_id
    ON payment_records(wx_transaction_id)
    WHERE wx_transaction_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_wallet_order_income
    ON wallet_transactions(companion_id, source_type, source_id, transaction_type)
    WHERE source_type = 1 AND transaction_type = 1;
