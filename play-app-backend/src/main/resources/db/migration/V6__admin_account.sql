-- ================================================================
-- V6: PC 管理端初始化（admin 账号 + 扩展权限 + 角色分配）
-- 一次性合并脚本，替代之前的 V6/V7/V8
-- ================================================================

-- 1. users 表增加登录列
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50) DEFAULT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) DEFAULT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username) WHERE username IS NOT NULL;

COMMENT ON COLUMN users.username IS '登录用户名（PC Web 管理端使用）';
COMMENT ON COLUMN users.password_hash IS 'BCrypt 密码哈希';

-- 2. 创建 admin 账号（密码: admin123）
--    ON CONFLICT 用 username 做冲突检测，已存在则刷新密码
INSERT INTO users (openid, phone, nickname, username, password_hash, status, create_time, update_time)
VALUES ('sys_admin_openid_001', '13800000000', '超级管理员', 'admin',
        '$2a$10$O5Ap1sUHNsBeYPROGvcfFuoLzTSfsiMJAI8rVNoWLYh0YpWShkSNK',
        1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (openid) DO UPDATE SET
    username = EXCLUDED.username,
    password_hash = EXCLUDED.password_hash,
    phone = EXCLUDED.phone,
    update_time = CURRENT_TIMESTAMP;

-- 3. 扩展权限种子（PC 管理端各模块精细权限）
INSERT INTO permissions (name, permission_code, parent_id, sort_order) VALUES
('用户列表', 'admin:user:list', 0, 10),
('用户详情', 'admin:user:detail', 0, 20),
('用户状态管理', 'admin:user:status', 0, 30),
('用户角色分配', 'admin:user:role', 0, 40),
('角色管理', 'admin:role:manage', 0, 50),
('权限管理', 'admin:permission:manage', 0, 60),
('助教列表', 'admin:companion:list', 0, 70),
('入驻审核', 'admin:companion:audit', 0, 80),
('助教推荐', 'admin:companion:recommend', 0, 90),
('订单列表', 'admin:order:list', 0, 100),
('订单状态操作', 'admin:order:status', 0, 110),
('完工核销与放款', 'admin:order:settle', 0, 120),
('退款仲裁', 'admin:order:refund', 0, 130),
('纠纷处理', 'admin:dispute:resolve', 0, 140),
('钱包列表', 'admin:wallet:list', 0, 150),
('提现审核', 'admin:withdraw:audit', 0, 160),
('流水明细', 'admin:wallet:transactions', 0, 170),
('分类管理', 'admin:category:manage', 0, 180),
('标签管理', 'admin:tag:manage', 0, 190),
('评价管理', 'admin:review:manage', 0, 200),
('需求管理', 'admin:request:manage', 0, 210),
('风控举报', 'admin:risk:manage', 0, 220),
('系统配置', 'admin:system:config', 0, 230),
('品牌主题', 'admin:theme:config', 0, 240),
('操作日志', 'admin:audit:log', 0, 250),
('数据仪表盘', 'admin:dashboard', 0, 260)
ON CONFLICT (permission_code) DO NOTHING;

-- 4. admin 绑定 ROLE_ADMIN（数据库 role id=1）
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 5. ROLE_ADMIN 获取全部权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
