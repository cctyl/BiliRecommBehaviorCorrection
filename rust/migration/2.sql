-- 删除每个 name 中，不是最新 created_date 的记录
DELETE FROM config
WHERE rowid NOT IN (
    SELECT min_rowid
    FROM (
        SELECT 
            FIRST_VALUE(rowid) OVER (PARTITION BY name ORDER BY created_date DESC, rowid DESC) AS min_rowid,
            rowid
        FROM config
    )
);

CREATE UNIQUE INDEX "uni_name"
ON "config" (
  "name"
);


ALTER TABLE "dict" RENAME TO "_dict_old_20250922";

CREATE TABLE "dict" (
  "id" CHAR(30) NOT NULL,
  "value" VARCHAR(255) NOT NULL,
  "access_type" VARCHAR(50) NOT NULL,
  "dict_type" VARCHAR(50) NOT NULL,
  "outer_id" VARCHAR(60),
  "created_date" DATE,
  "last_modified_date" DATE,
  "desc" VARCHAR(50),
  CONSTRAINT "pk_dict" PRIMARY KEY ("id")
);

INSERT INTO "dict" ("id", "value", "access_type", "dict_type", "outer_id", "created_date", "last_modified_date", "desc") SELECT "id", "value", "access_type", "dict_type", "outer_id", "created_date", "last_modified_date", "desc" FROM "_dict_old_20250922";



DROP INDEX "idx_aid";

DROP INDEX "idx_video_detail_handle_type";

ALTER TABLE "video_detail" RENAME TO "_video_detail_old_20251029";

CREATE TABLE "video_detail" (
                                "id" CHAR(30) NOT NULL,
                                "aid" INT NOT NULL,
                                "tid" INT,
                                "tname" VARCHAR(255),
                                "pic" VARCHAR(350),
                                "title" VARCHAR(255),
                                "pubdate" INT,
                                "ctime" INT,
                                "desc" VARCHAR(255),
                                "duration" INT,
                                "dynamic" VARCHAR(255),
                                "first_frame" VARCHAR(255),
                                "pub_location" VARCHAR(255),
                                "bvid" VARCHAR(255) NOT NULL,
                                "owner_id" CHAR(30),
                                "handle" TINYINT(1) DEFAULT 0,
                                "rcmd_reason" VARCHAR(255),
                                "handle_type" VARCHAR(50),
                                "created_date" DATE,
                                "last_modified_date" DATE,
                                "handle_reason" TEXT,
                                CONSTRAINT "pk_video_detail" PRIMARY KEY ("id"),
                                UNIQUE ("aid" ASC),
                                UNIQUE ("bvid" ASC)
);

INSERT INTO "video_detail" (
    "id", "aid", "tid", "tname", "pic", "title", "pubdate", "ctime", "desc", "duration",
    "dynamic", "first_frame", "pub_location", "bvid", "owner_id", "handle", "rcmd_reason",
    "handle_type", "created_date", "last_modified_date", "handle_reason"
)
SELECT
    "id", "aid", "tid", "tname", "pic", "title", "pubdate", "ctime", "desc", "duration",
    "dynamic", "first_frame", "pub_location", "bvid", "owner_id", "handle", "rcmd_reason",
    "handle_type", "created_date", "last_modified_date",
    CASE
        WHEN "handle_type" = 'THUMB_UP' THEN "thumb_up_reason"
        WHEN "handle_type" = 'DISLIKE' THEN "black_reason"
        ELSE NULL
        END AS "handle_reason"
FROM "_video_detail_old_20251029";

CREATE INDEX "idx_aid"
    ON "video_detail" (
                       "aid" ASC
        );

CREATE INDEX "idx_video_detail_handle_type"
    ON "video_detail" (
                       "handle_type" ASC
        );


drop table _video_detail_old_20251029;



DROP INDEX "main"."idx_aid";

DROP INDEX "main"."idx_video_detail_handle_type";

ALTER TABLE "main"."video_detail" RENAME TO "_video_detail_old_20251029";

CREATE TABLE "main"."video_detail" (
                                       "id" CHAR(30) NOT NULL,
                                       "aid" INT NOT NULL,
                                       "tid" INT,
                                       "tname" VARCHAR(255),
                                       "pic" VARCHAR(350),
                                       "title" VARCHAR(255),
                                       "pubdate" INT,
                                       "ctime" INT,
                                       "desc" VARCHAR(255),
                                       "duration" INT,
                                       "dynamic" VARCHAR(255),
                                       "first_frame" VARCHAR(255),
                                       "pub_location" VARCHAR(255),
                                       "bvid" VARCHAR(255) NOT NULL,
                                       "owner_id" CHAR(30),
                                       "handle" TINYINT(1) DEFAULT 0,
                                       "rcmd_reason" VARCHAR(255),
                                       "handle_type" VARCHAR(50),
                                       "created_date" DATE,
                                       "last_modified_date" DATE,
                                       "handle_reason" TEXT,
                                       "tag_ids" TEXT,
                                       CONSTRAINT "pk_video_detail" PRIMARY KEY ("id"),
                                       UNIQUE ("aid" ASC),
                                       UNIQUE ("bvid" ASC)
);

INSERT INTO "main"."video_detail" ("id", "aid", "tid", "tname", "pic", "title", "pubdate", "ctime", "desc", "duration", "dynamic", "first_frame", "pub_location", "bvid", "owner_id", "handle", "rcmd_reason", "handle_type", "created_date", "last_modified_date", "handle_reason") SELECT "id", "aid", "tid", "tname", "pic", "title", "pubdate", "ctime", "desc", "duration", "dynamic", "first_frame", "pub_location", "bvid", "owner_id", "handle", "rcmd_reason", "handle_type", "created_date", "last_modified_date", "handle_reason" FROM "main"."_video_detail_old_20251029";

CREATE INDEX "main"."idx_aid"
    ON "video_detail" (
                       "aid" ASC
        );

CREATE INDEX "main"."idx_video_detail_handle_type"
    ON "video_detail" (
                       "handle_type" ASC
        );


drop table _video_detail_old_20251029;








-- 重命名旧表
ALTER TABLE "dict" RENAME TO "_dict_old_20251030";

-- 创建新表，status 字段设为 NOT NULL
CREATE TABLE "dict" (
                        "id" CHAR(30) NOT NULL,
                        "value" VARCHAR(255) NOT NULL,
                        "access_type" VARCHAR(50) NOT NULL,
                        "dict_type" VARCHAR(50) NOT NULL,
                        "outer_id" VARCHAR(60),
                        "created_date" DATE,
                        "last_modified_date" DATE,
                        "desc" VARCHAR(50),
                        "status" TEXT NOT NULL,  -- 明确设置为非空
                        CONSTRAINT "pk_dict" PRIMARY KEY ("id")
);

-- 插入数据，并按规则转换字段
INSERT INTO "dict" (
    "id", "value", "access_type", "dict_type", "outer_id",
    "created_date", "last_modified_date", "desc", "status"
)
SELECT
    "id",
    "value",
    CASE
        WHEN "access_type" = 'BLACK_CACHE' THEN 'BLACK'
        WHEN "access_type" = 'WHITE_CACHE' THEN 'WHITE'
        ELSE "access_type"
        END AS "access_type",
    CASE
        WHEN "dict_type" = 'IGNORE_KEYWORD' THEN 'KEYWORD'
        WHEN "dict_type" = 'IGNORE_TAG' THEN 'TAG'
        ELSE "dict_type"
        END AS "dict_type",
    "outer_id",
    "created_date",
    "last_modified_date",
    "desc",
    CASE
        WHEN "access_type" IN ('BLACK_CACHE', 'WHITE_CACHE') THEN 'CACHE'
        WHEN "dict_type" IN ('IGNORE_KEYWORD', 'IGNORE_TAG') THEN 'IGNORE'
        ELSE 'NORMAL'
        END AS "status"
FROM "_dict_old_20251030";

drop table "_dict_old_20251030";




CREATE TABLE "associate_rule" (
                                  "id" char(30) NOT NULL,
                                  "info" varchar(100) NOT NULL,
                                  "created_date" DATE,
                                  "last_modified_date" DATE,
                                  "access_type" TEXT NOT NULL,
                                  CONSTRAINT "pk_relate_rule" PRIMARY KEY ("id")
);


INSERT INTO "associate_rule" ("id", "info", "created_date", "last_modified_date", "access_type")
SELECT "id", "info", "created_date", "last_modified_date", 'WHITE'
FROM "white_list_rule"
WHERE "is_deleted" = 0;