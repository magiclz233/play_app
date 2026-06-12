-- ================================================================
-- 手动执行：初始化管理员账号 + 权限
-- 用 psql 或其他客户端在 play_app 库执行即可
-- ================================================================

-- 1. 创建 admin 账号（密码: admin123）
--    如果 openid 已存在则刷新用户名和密码
INSERT INTO users (openid, phone, nickname, username, password_hash, status, create_time, update_time)
VALUES ('sys_admin_openid_001', '13800000000', '超级管理员', 'admin',
        '$2a$10$O5Ap1sUHNsBeYPROGvcfFuoLzTSfsiMJAI8rVNoWLYh0YpWShkSNK',
        1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (openid) DO UPDATE SET
    username = EXCLUDED.username,
    password_hash = EXCLUDED.password_hash,
    phone = EXCLUDED.phone,
    update_time = CURRENT_TIMESTAMP;

-- 2. admin 绑定 ROLE_ADMIN 角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 3. ROLE_ADMIN 获取全部权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
