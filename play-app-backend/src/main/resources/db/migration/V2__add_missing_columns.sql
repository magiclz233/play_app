-- =============================================
-- V2: 补充代码层新增的数据库字段
-- =============================================

-- orders: 支付追踪 & 完工时间
ALTER TABLE orders ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS pay_time TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS finish_time TIMESTAMP;

-- wallet_transactions: 多态来源关联
ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS source_type INTEGER;
ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS source_id BIGINT;

-- reviews: 评价图片
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS images TEXT;

-- companion_wallets: 乐观锁版本号
ALTER TABLE companion_wallets ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
