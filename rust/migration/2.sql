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