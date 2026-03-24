
-- 1. 先更新 video_detail 表的 owner_id
-- 创建临时映射表，存储旧的owner.id和对应的mid
CREATE TEMPORARY TABLE temp_owner_mapping AS
SELECT id AS old_id, mid 
FROM "owner";

-- 创建索引提高更新速度
CREATE INDEX temp_idx_old_id ON temp_owner_mapping(old_id);
CREATE INDEX temp_idx_mid ON temp_owner_mapping(mid);

-- 更新 video_detail 的 owner_id 为对应的 mid 值
UPDATE video_detail 
SET owner_id = (
    SELECT mid 
    FROM temp_owner_mapping 
    WHERE temp_owner_mapping.old_id = video_detail.owner_id
)
WHERE owner_id IN (SELECT old_id FROM temp_owner_mapping);

-- 2. 创建新的 owner 表结构
-- 先重命名原表
ALTER TABLE "owner" RENAME TO "owner_old";

-- 创建新表，id 改为数字类型
CREATE TABLE "owner" (
    "id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR(100) NOT NULL,
    "face" VARCHAR(350)
);

-- 3. 迁移数据，使用 mid 作为新的 id
-- 注意：这里假设 mid 是数字类型，如果不是数字类型，需要先转换
INSERT INTO "owner" (id, name, face)
SELECT CAST(mid AS INTEGER), name, face
FROM owner_old;

-- 4. 删除旧表
DROP TABLE owner_old;

-- 5. 修改 video_detail 表的 owner_id 字段类型
-- SQLite 不支持直接修改列类型，需要重建表

-- 创建临时表，保存 video_detail 的所有数据
CREATE TABLE video_detail_temp (
    id INTEGER NOT NULL,
    tid INTEGER,
    tname VARCHAR(255),
    pic VARCHAR(350),
    title VARCHAR(255),
    pubdate INTEGER,
    "desc" VARCHAR(255),
    duration INTEGER,
    dynamic VARCHAR(255),
    bvid VARCHAR(255) NOT NULL,
    owner_id INTEGER,  -- 改为整数类型
    handle_time DATE,
    handle_type VARCHAR(50),
    handle_step INTEGER NOT NULL DEFAULT 0,
    handle_reason TEXT,
    tag TEXT,
    created_date DATE
);

-- 复制数据到临时表，将 owner_id 转换为整数
INSERT INTO video_detail_temp 
SELECT 
    id,
    tid,
    tname,
    pic,
    title,
    pubdate,
    "desc",
    duration,
    dynamic,
    bvid,
    CAST(owner_id AS INTEGER),  -- 转换为整数
    handle_time,
    handle_type,
    handle_step,
    handle_reason,
    tag,
    created_date
FROM video_detail;

-- 删除原表
DROP TABLE video_detail;

-- 重命名临时表
ALTER TABLE video_detail_temp RENAME TO video_detail;

-- 6. 重建约束和索引
-- 添加主键约束
CREATE UNIQUE INDEX "pk_video_detail_new" ON "video_detail"("id" ASC);

-- 添加唯一约束
CREATE UNIQUE INDEX "idx_video_detail_bvid" ON "video_detail"("bvid" ASC);

-- 重建其他索引
CREATE INDEX "idx_video_detail_new_handle_step" ON "video_detail"("handle_step" ASC);
CREATE INDEX "idx_video_detail_new_handle_type" ON "video_detail"("handle_type" ASC);

-- 7. 清理临时映射表
DROP TABLE temp_owner_mapping;

