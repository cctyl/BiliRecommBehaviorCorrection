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


ALTER TABLE "main"."dict" RENAME TO "_dict_old_20250922";

CREATE TABLE "main"."dict" (
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

INSERT INTO "main"."dict" ("id", "value", "access_type", "dict_type", "outer_id", "created_date", "last_modified_date", "desc") SELECT "id", "value", "access_type", "dict_type", "outer_id", "created_date", "last_modified_date", "desc" FROM "main"."_dict_old_20250922";