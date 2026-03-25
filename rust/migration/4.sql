-- 步骤1: 创建新表（不包含tag字段，先不填充tag）
CREATE TABLE "video_detail_new" (
    "id" INT NOT NULL,  -- 原aid字段，作为新id
    "tid" INT,
    "tname" VARCHAR(255),
    "pic" VARCHAR(350),
    "title" VARCHAR(255),
    "pubdate" INT,
    "desc" VARCHAR(255),
    "duration" INT,
    "dynamic" VARCHAR(255),
    "bvid" VARCHAR(255) NOT NULL,
    "owner_id" CHAR(30),
    "handle_time" DATE,
    "handle_type" VARCHAR(50),
    "handle_step" INT NOT NULL DEFAULT 0,
    "handle_reason" TEXT,
    "tag" TEXT,  -- 先保留字段，稍后填充
    "created_date" DATE,
    CONSTRAINT "pk_video_detail_new" PRIMARY KEY ("id"),
    UNIQUE ("bvid" ASC)
);

-- 步骤2: 创建索引
CREATE INDEX "idx_video_detail_new_handle_type" ON "video_detail_new"("handle_type" ASC);
CREATE INDEX "idx_video_detail_new_handle_step" ON "video_detail_new"("handle_step" ASC);

-- 步骤3: 迁移数据（使用转义处理特殊字符）
INSERT INTO "video_detail_new" (
    "id",
    "tid",
    "tname",
    "pic",
    "title",
    "pubdate",
    "desc",
    "duration",
    "dynamic",
    "bvid",
    "owner_id",
    "handle_time",
    "handle_type",
    "handle_step",
    "handle_reason",
    "tag",
    "created_date"
)
SELECT 
    vd.aid,
    vd.tid,
    vd.tname,
    vd.pic,
    vd.title,
    vd.pubdate,
    vd.desc,
    vd.duration,
    vd.dynamic,
    vd.bvid,
    vd.owner_id,
    vd.last_modified_date,
    vd.handle_type,
    CASE 
        WHEN vd.handle_type IS NOT NULL AND vd.handle_type != '' AND vd.handle = 0 THEN 1
        WHEN vd.handle_type IS NOT NULL AND vd.handle_type != '' AND vd.handle = 1 THEN 100
        ELSE 0
    END AS handle_step,
    CASE 
        WHEN vd.handle_reason IS NOT NULL AND vd.handle_reason != '' 
        THEN '{"user_handle_reason":"' || 
            replace(
                replace(
                    replace(
                        replace(
                            replace(
                                vd.handle_reason,
                                '\', '\\'    -- 转义反斜杠
                            ),
                            '"', '\\"'      -- 转义双引号
                        ),
                        CHAR(10), '\\n'     -- 转义换行符
                    ),
                    CHAR(13), '\\r'         -- 转义回车符
                ),
                CHAR(9), '\\t'              -- 转义制表符
            ) || '"}'
        ELSE NULL
    END AS handle_reason,
    NULL,  -- tag字段先留空
    vd.created_date
FROM video_detail vd;

-- 步骤4: 创建临时表存储video_id到tag_name的映射（按原video_detail的id分组）
CREATE TEMP TABLE temp_video_tags AS
SELECT 
    vt.video_id,
    GROUP_CONCAT(t.tag_name, ',') AS tag_names
FROM video_tag vt
INNER JOIN tag t ON vt.tag_id = t.id
GROUP BY vt.video_id;

-- 步骤5: 为临时表创建索引以加速更新
CREATE INDEX idx_temp_video_tags_video_id ON temp_video_tags(video_id);

-- 步骤6: 更新新表的tag字段（通过原video_detail的id关联）
UPDATE video_detail_new 
SET tag = (
    SELECT tvt.tag_names 
    FROM temp_video_tags tvt 
    WHERE tvt.video_id = (
        SELECT vd.id 
        FROM video_detail vd 
        WHERE vd.aid = video_detail_new.id
    )
)
WHERE EXISTS (
    SELECT 1 
    FROM temp_video_tags tvt 
    WHERE tvt.video_id = (
        SELECT vd.id 
        FROM video_detail vd 
        WHERE vd.aid = video_detail_new.id
    )
);

-- 步骤7: 删除临时表
DROP TABLE temp_video_tags;

-- 步骤8: 删除原表
DROP TABLE video_detail;

-- 步骤9: 重命名新表
ALTER TABLE video_detail_new RENAME TO video_detail;

-- 步骤10: 清理其他表
DROP TABLE IF EXISTS stat;
DROP TABLE IF EXISTS white_list_rule;
DROP TABLE IF EXISTS prepare_video;
DROP TABLE IF EXISTS video_reply;
DROP TABLE IF EXISTS video_relate;
DROP TABLE IF EXISTS video_tag;
DROP TABLE IF EXISTS watched_video;