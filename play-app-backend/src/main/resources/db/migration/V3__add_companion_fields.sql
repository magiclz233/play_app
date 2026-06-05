-- V3: 助教昵称、助教号码
ALTER TABLE companion_profiles ADD COLUMN IF NOT EXISTS nickname VARCHAR(50);
ALTER TABLE companion_profiles ADD COLUMN IF NOT EXISTS companion_code VARCHAR(20);

-- 唯一索引：助教号码不可重复
CREATE UNIQUE INDEX IF NOT EXISTS idx_companion_code ON companion_profiles(companion_code);
