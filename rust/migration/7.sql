-- 1. 创建新表
CREATE TABLE "task_new"
(
    id                 char(30) not null
        constraint pk_task
            primary key,
    last_run_time      DATE,
    current_run_status TEXT    not null default 'unknown',
    total_run_count    int,
    last_run_duration  int,
    task_name          TEXT,
    scheduled_hour     int     not null default 0,
    is_enabled         int     not null default 1,
    class_method_name  text    not null default '',
    description        text,
    img                text
);

-- 2. 复制数据（处理所有NULL值）
INSERT INTO task_new (
    id, last_run_time, current_run_status, total_run_count, 
    last_run_duration, task_name, scheduled_hour, is_enabled, 
    class_method_name, description, img
)
SELECT 
    id, last_run_time, 
    COALESCE(current_run_status, 'unknown'),
    total_run_count, 
    last_run_duration, task_name, 
    COALESCE(scheduled_hour, 0),
    COALESCE(is_enabled, 1),
    COALESCE(class_method_name, ''),  -- 处理 class_method_name 的 NULL
    description, img
FROM task;

-- 3. 删除旧表并重命名
DROP TABLE task;
ALTER TABLE task_new RENAME TO task;