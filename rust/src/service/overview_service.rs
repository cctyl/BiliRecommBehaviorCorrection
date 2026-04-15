use std::str::FromStr;
use std::time::{SystemTime, UNIX_EPOCH};
use rbatis::{executor::Executor, py_sql, rbdc::DateTime};

use crate::app::config::CC;
use crate::app::response::R;
use crate::domain::enumeration::{AccessType, DictStatus, TaskStatus};
use crate::domain::overview::{DateCountMap, OverviewVo, TaskInfo};
use crate::domain::{config::Config, dict::Dict, task::Task, video_detail::VideoDetail};
use rbs::value;
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
    };
    // 同时执行所有异步函数
    let (task_result, dict_result, video_result, config_result) = join!(
        fill_task_info(),
        fill_dict_info(),
        fill_video_detail_info(year),
        fill_config_info()
    );

    // 检查结果
    let (running_task_count,task_list) = task_result?;
    overview_vo.running_task_count = task_list.len() as u32;
    overview_vo.task_list = task_list;


    let (black_count,white_count,search_count,black_cache_count) = dict_result?;
    overview_vo.black_rule_count = black_count;
    overview_vo.white_rule_count = white_count;
    overview_vo.search_keyword_count = search_count;
    overview_vo.black_cache_count = black_cache_count;

    let (second_handle_count, third_handle_count,like_video_count,hate_video_count,white_history,black_history,other_history) = video_result?;
    overview_vo.second_handle_count = second_handle_count;
    overview_vo.third_handle_count = third_handle_count;
    overview_vo.like_video_count = like_video_count;
    overview_vo.hate_video_count = hate_video_count;
    overview_vo.white_history = white_history;
    overview_vo.black_history = black_history;
    overview_vo.other_history = other_history;


    let run_days = config_result?;
    overview_vo.run_days = run_days;

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

    
    R::Ok((task_list.len() as u32,task_list))
}

/// 填充字典信息
async fn fill_dict_info() -> R<((u64,u64,u64,u64))> {
    // 统计黑名单数量
    let black_count = Dict::select_by_map(
        &CC.rb,
        value! {
            "access_type": AccessType::BLACK,
            "status": DictStatus::NORMAL
        },
    )
    .await?
    .len() as u64;

    // 统计白名单数量
    let white_count = Dict::select_by_map(
        &CC.rb,
        value! {
            "access_type": AccessType::WHITE,
            "status": DictStatus::NORMAL
        },
    )
    .await?
    .len() as u64;

    // 统计搜索关键词数
    let search_count = Dict::select_by_map(
        &CC.rb,
        value! {
            "access_type": AccessType::OTHER,
            "dict_type": crate::domain::enumeration::DictType::SEARCH_KEYWORD,
            "status": DictStatus::NORMAL
        },
    )
    .await?
    .len() as u64;

    // 统计黑名单缓存数
    let black_cache_count = Dict::select_by_map(
        &CC.rb,
        value! {
            "access_type": AccessType::BLACK,
            "status": DictStatus::CACHE
        },
    )
    .await?
    .len() as u64;

    // overview_vo.black_rule_count = black_count;
    // overview_vo.white_rule_count = white_count;
    // overview_vo.search_keyword_count = search_count;
    // overview_vo.black_cache_count = black_cache_count;

    R::Ok((black_count,white_count,search_count,black_cache_count))
}

///   填充视频详情信息
async fn fill_video_detail_info(year:u32) -> R<((
    u64,
    u64,
    u64,
    u64,
    Vec<DateCountMap>,
    Vec<DateCountMap>,
    Vec<DateCountMap>,
))> {
    // 统计待二次处理的数据量 (handle_step = 1)
    let second_handle_count = VideoDetail::select_by_map(
        &CC.rb,
        value! {
            "handle_step": 1u64
        },
    )
    .await?
    .len() as u64;

    // 统计待三次处理的数据量 (handle_step = 2)
    let third_handle_count = VideoDetail::select_by_map(
        &CC.rb,
        value! {
            "handle_step": 2u64
        },
    )
    .await?
    .len() as u64;

    // 统计历史点赞的视频数
    let like_video_count = VideoDetail::select_by_map(
        &CC.rb,
        value! {
            "handle_type": AccessType::WHITE,
            "handle_step": 100u64
        },
    )
    .await?
    .len() as u64;

    // 统计历史点踩的视频数
    let hate_video_count = VideoDetail::select_by_map(
        &CC.rb,
        value! {
            "handle_type": AccessType::BLACK,
            "handle_step": 100u64
        },
    )
    .await?
    .len() as u64;

    // 构造日期范围
    let start_date = DateTime::from_str( &format!("{}-01-01 00:00:00", year)).unwrap();
    let end_date = DateTime::from_str( &format!("{}-12-31 23:59:59", year)).unwrap();

    // 统计白名单历史数据
    // 由于SQL中的DATE函数在SQLite中可能不兼容，使用自定义查询
    let white_history = count_by_handle_type_and_date_range_sql(
        &CC.rb,
        AccessType::WHITE,
        &start_date,
        &end_date,
    )
    .await?;

    // 统计黑名单历史数据
    let black_history = count_by_handle_type_and_date_range_sql(
        &CC.rb,
        AccessType::BLACK,
        &start_date,
        &end_date,
    )
    .await?;

    // 统计其他历史数据
    let other_history = count_by_handle_type_and_date_range_sql(
        &CC.rb,
        AccessType::OTHER,
        &start_date,
        &end_date,
    )
    .await?;

    // overview_vo.second_handle_count = second_handle_count;
    // overview_vo.third_handle_count = third_handle_count;
    // overview_vo.like_video_count = like_video_count;
    // overview_vo.hate_video_count = hate_video_count;
    // overview_vo.white_history = white_history;
    // overview_vo.black_history = black_history;
    // overview_vo.other_history = other_history;

    R::Ok((second_handle_count, third_handle_count,like_video_count,hate_video_count,white_history,black_history,other_history))
}

/// 自定义SQL查询，用于统计按日期分组的处理数据
#[py_sql(
    "SELECT
        strftime('%Y-%m-%d', handle_time) as date,
        COUNT(*) as count
    FROM video_detail
    WHERE handle_type = #{handle_type}
        AND handle_step = 100
        AND handle_time >= #{start_time}
        AND handle_time <= #{end_time}
    GROUP BY strftime('%Y-%m-%d', handle_time)
    ORDER BY date"
)]
async fn count_by_handle_type_and_date_range_sql(
    rb: &dyn Executor,
    handle_type: AccessType,
    start_time: &DateTime,
    end_time: &DateTime,
) -> Result<Vec<DateCountMap>, rbatis::Error> {
    impled!()
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

#[cfg(test)]
mod tests {
    use crate::service::overview_service::get_overview_info;

    #[tokio::test]
    async fn test_get_overview_info() {
        crate::init().await;

        let overview_vo = get_overview_info(2026).await.unwrap();

        println!("{:#?}", overview_vo);

        log::logger().flush();
    }
}
