use rbatis::rbdc::DateTime;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct OverviewVo {
    pub year: u32,

    // 当前运行任务数
    pub running_task_count: u32,

    // 黑名单规则数
    pub black_rule_count: u64,

    // 白名单规则数
    pub white_rule_count: u64,

    // 搜索关键词数
    pub search_keyword_count: u64,

    // 待筛选的黑名单关键词
    pub black_cache_count: u64,

    // 系统运行天数
    pub run_days: u64,

    // 历史处理过的白名单数据 天-数量
    pub white_history: Vec<DateCountMap>,

    // 历史处理过的黑名单数据 天-数量
    pub black_history: Vec<DateCountMap>,

    // 历史处理过的其他数据 天-数量
    pub other_history: Vec<DateCountMap>,

    // 待二次处理的数据量，逻辑为 VideoDetail 表，handle_step = 1 的数据
    pub second_handle_count: u64,

    // 待三次处理的数据量，逻辑为 VideoDetail 表，handle_step = 2 的数据
    pub third_handle_count: u64,

    // 正在运行的任务列表
    pub task_list: Vec<TaskInfo>,

    // 历史点赞的视频数
    pub like_video_count: u64,

    // 历史点踩的视频数
    pub hate_video_count: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize,sqlx::FromRow)]
pub struct DateCountMap {
    pub date: String,
    pub count: i64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TaskInfo {
    pub id: String,
    pub task_name: Option<String>,
    pub class_method_name: String,
    pub is_enabled: bool,
    pub current_run_status: String,
    pub last_run_time: Option<DateTime>,
    pub total_run_count: Option<u32>,
    pub last_run_duration: Option<u32>,
    pub scheduled_hour: i32,
}
