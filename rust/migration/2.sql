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