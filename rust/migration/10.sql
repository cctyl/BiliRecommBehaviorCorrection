-- 复合索引优化 overview 接口性能

-- video_detail: 针对 COUNT 和 GROUP BY 查询的复合索引
-- 查询模式: WHERE handle_type = ? AND handle_step = ? AND handle_time >= ? AND handle_time <= ?
CREATE INDEX IF NOT EXISTS idx_vd_type_step_time 
    ON video_detail(handle_type, handle_step, handle_time);

-- dict: 针对 access_type + status 的复合索引
-- 查询模式: WHERE access_type = ? AND status = ?
CREATE INDEX IF NOT EXISTS idx_dict_access_type_status 
    ON dict(access_type, status);

-- dict: 针对 access_type + dict_type + status 的复合索引（搜索关键词查询）
-- 查询模式: WHERE access_type = ? AND dict_type = ? AND status = ?
CREATE INDEX IF NOT EXISTS idx_dict_type_access_status 
    ON dict(dict_type, access_type, status);
