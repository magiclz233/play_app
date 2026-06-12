-- V8__operation_logs.sql
-- 操作日志表，用于记录管理员敏感操作

CREATE TABLE IF NOT EXISTS operation_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    username    VARCHAR(64),
    module      VARCHAR(64)   NOT NULL,          -- 操作模块: ORDER, COMPANION, USER, WITHDRAWAL, RISK, ROLE, SYSTEM
    action      VARCHAR(64)   NOT NULL,          -- 操作动作: AUDIT, CREATE, UPDATE, DELETE, STATUS_CHANGE, SETTLE, REFUND
    target_type VARCHAR(64),                     -- 目标类型: Order, CompanionProfile, User, etc.
    target_id   VARCHAR(128),                    -- 目标ID
    detail      VARCHAR(512),                    -- 操作描述
    params      JSONB         DEFAULT '{}',      -- 请求参数 (JSON)
    result      VARCHAR(32)   DEFAULT 'SUCCESS', -- 操作结果: SUCCESS, FAIL
    error_msg   TEXT,                            -- 错误信息
    ip          VARCHAR(45),                     -- 操作IP
    create_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_op_logs_user_id    ON operation_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_op_logs_module     ON operation_logs(module);
CREATE INDEX IF NOT EXISTS idx_op_logs_action     ON operation_logs(action);
CREATE INDEX IF NOT EXISTS idx_op_logs_create_time ON operation_logs(create_time DESC);
CREATE INDEX IF NOT EXISTS idx_op_logs_target     ON operation_logs(target_type, target_id);

COMMENT ON TABLE operation_logs IS '管理员操作日志';
COMMENT ON COLUMN operation_logs.module      IS '操作模块';
COMMENT ON COLUMN operation_logs.action      IS '操作动作';
COMMENT ON COLUMN operation_logs.target_type IS '目标类型';
COMMENT ON COLUMN operation_logs.target_id   IS '目标ID';
COMMENT ON COLUMN operation_logs.detail      IS '操作描述';
COMMENT ON COLUMN operation_logs.params      IS '请求参数JSON';
COMMENT ON COLUMN operation_logs.result      IS '操作结果 SUCCESS/FAIL';
COMMENT ON COLUMN operation_logs.error_msg   IS '错误信息';
COMMENT ON COLUMN operation_logs.ip          IS '操作IP';
