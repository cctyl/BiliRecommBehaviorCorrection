use std::time::{SystemTime, UNIX_EPOCH};
use chrono::{TimeZone, Utc};
use crate::app::config::CC;
use crate::app::response::R;
use crate::domain::enumeration::TaskStatus;
use crate::domain::overview::{DateCountMap, OverviewVo, TaskInfo};
use crate::domain::{config::Config, task::Task};
use rbs::value;
use serde::{Deserialize, Serialize};
use sqlx::SqlitePool;
use tokio::join;

/// 获取总览信息
pub async fn get_overview_info(year: u32) -> R<OverviewVo> {
    let mut overview_vo = OverviewVo {
        year,
        running_task_count: 0,
        black_rule_count: 0,
        white_rule_count: 0,
        search_keyword_count: 0,
        black_cache_count: 0,
        run_days: 0,
        white_history: vec![],
        black_history: vec![],
        other_history: vec![],
        second_handle_count: 0,
        third_handle_count: 0,
        task_list: vec![],
        like_video_count: 0,
        hate_video_count: 0,
        memory_usage_mb: 0.0,
        uptime_secs: 0,
        cpu_usage_percent: 0.0,
    };
    // 同时执行所有异步函数
    let (task_result, dict_result, video_result, config_result) = join!(
        fill_task_info(),
        fill_dict_info(),
        fill_video_detail_info(year),
        fill_config_info()
    );

    // 检查结果
    let (running_task_count, task_list) = task_result?;
    overview_vo.running_task_count = task_list.len() as u32;
    overview_vo.task_list = task_list;

    let (black_count, white_count, search_count, black_cache_count) = dict_result?;
    overview_vo.black_rule_count = black_count;
    overview_vo.white_rule_count = white_count;
    overview_vo.search_keyword_count = search_count;
    overview_vo.black_cache_count = black_cache_count;

    let (
        second_handle_count,
        third_handle_count,
        like_video_count,
        hate_video_count,
        white_history,
        black_history,
        other_history,
    ) = video_result?;
    overview_vo.second_handle_count = second_handle_count;
    overview_vo.third_handle_count = third_handle_count;
    overview_vo.like_video_count = like_video_count;
    overview_vo.hate_video_count = hate_video_count;
    overview_vo.white_history = white_history;
    overview_vo.black_history = black_history;
    overview_vo.other_history = other_history;

    let run_days = config_result?;
    overview_vo.run_days = run_days;

    // 填充系统运行信息
    fill_system_info(&mut overview_vo).await;

    R::Ok(overview_vo)
}

/// 填充任务信息
async fn fill_task_info() -> R<(u32, Vec<TaskInfo>)> {
    // 查找正在运行的任务
    let running_tasks = Task::select_by_map(
        &CC.rb,
        value! {
            "current_run_status": [TaskStatus::RUNNING, TaskStatus::WAITING]
        },
    )
    .await?;

    // 转换为 TaskInfo
    let task_list: Vec<TaskInfo> = running_tasks
        .into_iter()
        .map(|t| TaskInfo {
            id: t.id,
            task_name: t.task_name,
            class_method_name: t.class_method_name.clone(),
            is_enabled: t.is_enabled,
            current_run_status: format!("{:?}", t.current_run_status),
            last_run_time: t.last_run_time,
            total_run_count: t.total_run_count,
            last_run_duration: t.last_run_duration,
            scheduled_hour: t.scheduled_hour,
        })
        .collect();

    // overview_vo.running_task_count = task_list.len() as u32;
    // overview_vo.task_list = task_list;

    R::Ok((task_list.len() as u32, task_list))
}

/// 填充字典信息（使用 COUNT(*) 替代 SELECT *，减少数据传输）
async fn fill_dict_info() -> R<(u64, u64, u64, u64)> {
    let pool = CC.sqlx.get().expect("数据库未初始化");

    // 统计黑名单数量
    let black_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM dict WHERE access_type = ? AND status = ?"
    )
    .bind("BLACK")
    .bind("NORMAL")
    .fetch_one(pool)
    .await?;

    // 统计白名单数量
    let white_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM dict WHERE access_type = ? AND status = ?"
    )
    .bind("WHITE")
    .bind("NORMAL")
    .fetch_one(pool)
    .await?;

    // 统计搜索关键词数
    let search_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM dict WHERE access_type = ? AND dict_type = ? AND status = ?"
    )
    .bind("OTHER")
    .bind("SEARCH_KEYWORD")
    .bind("NORMAL")
    .fetch_one(pool)
    .await?;

    // 统计黑名单缓存数
    let black_cache_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM dict WHERE access_type = ? AND status = ?"
    )
    .bind("BLACK")
    .bind("CACHE")
    .fetch_one(pool)
    .await?;

    R::Ok((
        black_count.0 as u64,
        white_count.0 as u64,
        search_count.0 as u64,
        black_cache_count.0 as u64,
    ))
}

#[derive(Debug, Clone, Serialize, Deserialize, sqlx::FromRow)]
struct SqlxVideoDetail {
    id: i64,
    tid: Option<i64>,
    tname: Option<String>,
    pic: Option<String>,
    title: Option<String>,
    cid: Option<i64>,
    pubdate: Option<i64>,
    desc: Option<String>,
    duration: Option<i64>,
    dynamic: Option<String>,
    bvid: String,
    owner_id: Option<i64>,
    handle_time: Option<String>,
    handle_type: Option<String>,
    handle_step: i64,
    handle_reason: Option<String>,
    tag: Option<String>,
    created_date: Option<String>,
}

/// 合并后的 video_detail 计数查询结果
#[derive(Debug, Clone, sqlx::FromRow)]
struct VideoDetailCounts {
    #[sqlx(rename = "second_handle_count")]
    second_handle_count: i64,
    #[sqlx(rename = "third_handle_count")]
    third_handle_count: i64,
    #[sqlx(rename = "like_video_count")]
    like_video_count: i64,
    #[sqlx(rename = "hate_video_count")]
    hate_video_count: i64,
}

/// 合并后的按日期分组查询结果
#[derive(Debug, Clone, sqlx::FromRow)]
struct DateCountByType {
    handle_type: String,
    date: String,
    count: i64,
}




/// 合并的 COUNT 查询：一次查询获取所有 video_detail 统计数
async fn count_video_detail_all(pool: &SqlitePool) -> Result<VideoDetailCounts, sqlx::Error> {
    sqlx::query_as::<_, VideoDetailCounts>(
        "SELECT 
            COUNT(CASE WHEN handle_step = 1 THEN 1 END) AS second_handle_count,
            COUNT(CASE WHEN handle_step = 2 THEN 1 END) AS third_handle_count,
            COUNT(CASE WHEN handle_step = 100 AND handle_type = 'WHITE' THEN 1 END) AS like_video_count,
            COUNT(CASE WHEN handle_step = 100 AND handle_type = 'BLACK' THEN 1 END) AS hate_video_count
        FROM video_detail"
    )
    .fetch_one(pool)
    .await
}

/// 合并的按日期分组查询：一次查询获取 WHITE/BLACK/OTHER 三种类型的历史数据
async fn select_video_count_by_date_all(
    pool: &SqlitePool,
    start_time: &chrono::DateTime<Utc>,
    end_time: &chrono::DateTime<Utc>,
) -> Result<Vec<DateCountByType>, sqlx::Error> {
    sqlx::query_as::<_, DateCountByType>(
        r#"
        SELECT 
            handle_type,
            strftime('%Y-%m-%d', handle_time) as date,
            COUNT(*) as count
        FROM video_detail
        WHERE handle_step = 100
            AND handle_time >= $1
            AND handle_time <= $2
        GROUP BY handle_type, strftime('%Y-%m-%d', handle_time)
        ORDER BY date
        "#
    )
    .bind(start_time)
    .bind(end_time)
    .fetch_all(pool)
    .await
}

///   填充视频详情信息（合并查询优化版：7次查询 → 2次查询）
async fn fill_video_detail_info(
    year: u32,
) -> R<
        (
            u64,
            u64,
            u64,
            u64,
            Vec<DateCountMap>,
            Vec<DateCountMap>,
            Vec<DateCountMap>,

    ),
> {
    let pool = CC.sqlx.get().expect("数据库未初始化");

    // 一次查询获取4个 COUNT（替代原来4次查询）
    let counts = count_video_detail_all(&pool).await?;

    let second_handle_count = counts.second_handle_count as u64;
    let third_handle_count = counts.third_handle_count as u64;
    let like_video_count = counts.like_video_count as u64;
    let hate_video_count = counts.hate_video_count as u64;

    // 构造日期范围
    let start_date = Utc.with_ymd_and_hms(year as i32, 1, 1, 0, 0, 0)
        .unwrap();
    let end_date = Utc.with_ymd_and_hms(year as i32, 12, 31, 23, 59, 59)
        .unwrap();

    // 一次查询获取三种类型的历史数据（替代原来3次查询）
    let all_history = select_video_count_by_date_all(&pool, &start_date, &end_date)
        .await?;

    // 按 handle_type 拆分结果
    let mut white_history = Vec::new();
    let mut black_history = Vec::new();
    let mut other_history = Vec::new();

    for item in all_history {
        let date_count = DateCountMap {
            date: item.date,
            count: item.count,
        };
        match item.handle_type.as_str() {
            "WHITE" => white_history.push(date_count),
            "BLACK" => black_history.push(date_count),
            _ => other_history.push(date_count),
        }
    }

    R::Ok((
        second_handle_count,
        third_handle_count,
        like_video_count,
        hate_video_count,
        white_history,
        black_history,
        other_history,
    ))
}

// ===== 以下函数保留用于测试兼容，生产代码已不再使用 =====

#[allow(dead_code)]
async fn select_handle_step_1(pool: &SqlitePool) -> Result<Vec<SqlxVideoDetail>, sqlx::Error> {
    sqlx::query_as::<_, SqlxVideoDetail>("select * from video_detail where handle_step = ?")
        .bind(1_i64)
        .fetch_all(pool)
        .await
}

#[allow(dead_code)]
async fn select_handle_step_2(pool: &SqlitePool) -> Result<Vec<SqlxVideoDetail>, sqlx::Error> {
    sqlx::query_as::<_, SqlxVideoDetail>("select * from video_detail where handle_step = ?")
        .bind(2_i64)
        .fetch_all(pool)
        .await
}

#[allow(dead_code)]
async fn select_handle_step_100_white(
    pool: &SqlitePool,
) -> Result<Vec<SqlxVideoDetail>, sqlx::Error> {
    sqlx::query_as::<_, SqlxVideoDetail>(
        "select * from video_detail where handle_step = ? and handle_type = ?",
    )
    .bind(100_i64)
    .bind("WHITE")
    .fetch_all(pool)
    .await
}

#[allow(dead_code)]
async fn select_handle_step_100_black(
    pool: &SqlitePool,
) -> Result<Vec<SqlxVideoDetail>, sqlx::Error> {
    sqlx::query_as::<_, SqlxVideoDetail>(
        "select * from video_detail where handle_step = ? and handle_type = ?",
    )
    .bind(100_i64)
    .bind("BLACK")
    .fetch_all(pool)
    .await
}

/// 填充配置信息（运行天数）
async fn fill_config_info() -> R<u64> {
    let first_start_time_config = Config::select_one_by_condition(
        &CC.rb,
        value! {
            "name": crate::app::constans::FIRST_START_TIME
        },
    )
    .await?;

    let mut run_days = 0;
    if let Some(config) = first_start_time_config {
        if let Some(value) = config.value {
            if let Ok(millis) = value.parse::<u128>() {
                let now = SystemTime::now()
                    .duration_since(UNIX_EPOCH)
                    .unwrap()
                    .as_millis();

                let start_millis = millis;
                let days = (now - start_millis) / (1000 * 60 * 60 * 24);

                run_days = days as u64;
            }
        }
    }

    R::Ok(run_days)
}

/// 填充系统运行信息（内存、启动时长、CPU）
async fn fill_system_info(overview_vo: &mut OverviewVo) {
    use crate::app::global::{APP_START_INSTANT, GLOBAL_SYSTEM};
    use sysinfo::ProcessesToUpdate;

    // 计算启动时长
    if let Some(start) = APP_START_INSTANT.get() {
        overview_vo.uptime_secs = start.elapsed().as_secs();
    }

    // 复用全局 System 实例，只刷新进程信息（比 new System 快得多）
    let mut sys = GLOBAL_SYSTEM.write().await;
    sys.refresh_processes(
        ProcessesToUpdate::All,
        true,
    );

    let pid = sysinfo::Pid::from_u32(std::process::id());
    if let Some(process) = sys.process(pid) {
        overview_vo.memory_usage_mb =
            ((process.memory() as f64) / (1024.0 * 1024.0) * 100.0).round() / 100.0;
        overview_vo.cpu_usage_percent =
            (process.cpu_usage() as f64 * 100.0).round() / 100.0;
    }
}

#[cfg(test)]
mod tests {
    use crate::app::config::CC;
    use crate::domain::enumeration::AccessType;
    use crate::domain::video_detail::VideoDetail;
    use crate::service::overview_service::{
         get_overview_info, select_handle_step_1, select_handle_step_2,
        select_handle_step_100_black, select_handle_step_100_white,
    };
    use crate::utils::thread_util::ThreadUtil;
    use rbatis::RBatis;
    use rbdc_sqlite::SqliteDriver;
    use rbs::value;
    use serde::{Deserialize, Serialize};
    use sqlx::sqlite::{SqlitePool, SqlitePoolOptions};

    async fn create_sqlx_pool() -> Result<SqlitePool, sqlx::Error> {
        SqlitePoolOptions::new()
            .max_connections(1)
            .connect("sqlite://./bili-recomm-test.db")
            .await
    }
    #[tokio::test]
    async fn test_get_overview_info() {
        crate::init().await;

        let overview_vo = get_overview_info(2026).await.unwrap();

        println!("{:#?}", overview_vo);

        log::logger().flush();
    }

    #[tokio::test]
    async fn test_video_select_by_map() {
        // fast_log::init(fast_log::Config::new().console()).expect("rbatis init fail");
        /// initialize rbatis. also you can call rb.clone(). this is  an Arc point
        let rb = RBatis::new();
        rb.link(SqliteDriver {}, "sqlite://./bili-recomm-test.db")
            .await
            .unwrap();

        loop {
            let second_handle = VideoDetail::select_by_map(
                &rb,
                value! {
                    "handle_step": 1u64
                },
            )
            .await
            .unwrap()
            .len() as u64;

            let third_handle = VideoDetail::select_by_map(
                &rb,
                value! {
                    "handle_step": 2u64
                },
            )
            .await
            .unwrap();

            let like_video = VideoDetail::select_by_map(
                &rb,
                value! {
                    "handle_type": AccessType::WHITE,
                    "handle_step": 100u64
                },
            )
            .await
            .unwrap();

            let hate_video = VideoDetail::select_by_map(
                &rb,
                value! {
                    "handle_type": AccessType::BLACK,
                    "handle_step": 100u64
                },
            )
            .await
            .unwrap();

            println!("执行一次");
            ThreadUtil::s10().await;
        }

        log::logger().flush();
    }

    #[tokio::test]
    async fn test_video_select_by_sqlx_once() {
        let pool = create_sqlx_pool().await.unwrap();

        let handle_step_1_videos = select_handle_step_1(&pool).await.unwrap();
        let handle_step_2_videos = select_handle_step_2(&pool).await.unwrap();
        let white_videos = select_handle_step_100_white(&pool).await.unwrap();
        let black_videos = select_handle_step_100_black(&pool).await.unwrap();

        println!(
            "handle_step=1: {}, handle_step=2: {}, step100 WHITE: {}, step100 BLACK: {}",
            handle_step_1_videos.len(),
            handle_step_2_videos.len(),
            white_videos.len(),
            black_videos.len()
        );
    }

    #[tokio::test]
    // #[ignore = "manual polling test that loops forever"]
    async fn test_video_select_by_sqlx_loop() {
        let pool = create_sqlx_pool().await.unwrap();

        loop {
            let handle_step_1_videos = select_handle_step_1(&pool).await.unwrap();
            let handle_step_2_videos = select_handle_step_2(&pool).await.unwrap();
            let white_videos = select_handle_step_100_white(&pool).await.unwrap();
            let black_videos = select_handle_step_100_black(&pool).await.unwrap();

            println!(
                "handle_step=1: {}, handle_step=2: {}, step100 WHITE: {}, step100 BLACK: {}",
                handle_step_1_videos.len(),
                handle_step_2_videos.len(),
                white_videos.len(),
                black_videos.len()
            );

            ThreadUtil::s10().await;
        }
    }
}
