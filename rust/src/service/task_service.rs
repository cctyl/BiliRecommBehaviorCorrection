use log::{error, info};
use rbatis::{
    PageRequest, executor::Executor, impled, py_sql, rbdc::{DateTime, db::ExecResult}, sql
};
use rbs::value;
use std::collections::HashSet;
use std::time::Instant;

use crate::{
    app::{
        config::CC,
        constans::{
            DO_DEFAULT_PROCESS_VIDEO, DO_HOME_RECOMMEND_TASK, DO_HOT_RANK_TASK, DO_THIRD_PROCESS,
        },
        error::HttpError,
        response::R,
        task_pool::TASK_POOL,
    },
    domain::{enumeration::TaskStatus, task::Task},
};

/// 检查并执行任务
pub async fn check_and_run_task(hour: u32) -> R<()> {
    let tasks = Task::select_by_map(
        &CC.rb,
        value! {
            "is_enabled":true,
            "scheduled_hour":hour,
            "current_run_status":TaskStatus::STOPPED
        },
    )
    .await?;

    for t in tasks {
        do_task_by_name(t.class_method_name.as_str()).await?;
    }

    R::Ok(())
}

/// 按照名字调用任务
pub async fn do_task_by_name(name: &str) -> R<()> {
    info!("执行：{} 任务", name);
    match name {
        // 关键词搜索任务
        DO_SEARCH_TASK => {
            search_keyword_task().await?;
        }
        // 热门排行榜任务
        DO_HOT_RANK_TASK => {
            hot_rank_video_task().await?;
        }
        // 首页推荐任务
        DO_HOME_RECOMMEND_TASK => {
            home_recommend_task().await?;
        }
        // 把未处理的视频，全部加入处理队列中，按照默认的状态去处理 ，相当于代替人工处理，执行后step是2
        DO_DEFAULT_PROCESS_VIDEO => {
            default_process().await?;
        }

        // 进行三次处理，把判断好的视频开始执行点赞点踩操作
        DO_THIRD_PROCESS => {
            third_process().await?;
        }
        // 点赞用户所有视频任务
        THUMB_UP_ALL_USER_VIDEO_TASK => {
            // 这个任务通过 handler 直接调用，带有参数，这里只是占位
            // 实际执行在 task_handler 中处理
            info!("点赞用户所有视频任务被调用");
        }
        e => {
            error!("class_name={} ,未知的任务，跳过", e);
        }
    }

    R::Ok(())
}

/// 进行三次处理，把判断好的视频开始执行点赞点踩操作
/// 每次只处理40条，防止触发风控
pub async fn third_process() -> R<()> {


    // 黑白名单各取20条
    let handle_type_arr = vec![AccessType::BLACK,AccessType::WHITE];

    for handle_type in handle_type_arr {

        let video_list = VideoDetail::select_page_by_condition(&CC.rb, &PageRequest::new(1, 20), value!{
            "handle_step":2,
            "handle_type":handle_type
        }).await?.records;


        for v in video_list {
            
            let r = {
                
                if handle_type==AccessType::BLACK {
                    //点踩
                    bili::dislike(v.id).await

                }else{
                    //点赞并播放
                    bili::play_and_thumb_up(&v).await
                }
            };


            match r {
                Ok(_) => {},
                Err(e) => {
                    error!("对视频aid={}处理时，出现错误:{:#?}",v.id,e);

                },
            }

            ThreadUtil::s10().await;

        }

    }


    R::Ok(())
}


/// 把未处理的视频，全部加入处理队列中，按照默认的状态去处理 ，相当于代替人工处理，执行后step是2
pub async fn default_process() -> R<()> {
    let video_detail_arr = VideoDetail::select_by_map(
        &CC.rb,
        value! {
            "handle_step":1
        },
    )
    .await?;

    let reason = Some("用户执行了默认状态处理".to_string());
    for mut ele in video_detail_arr {
        ele.handle_step = 2;

        let mut match_result = ele.handle_reason.unwrap_or(MatchResult::default());
        match_result.user_handle_reason = reason.clone();
        ele.handle_reason = Some(match_result);

        VideoDetail::update_by_id(&CC.rb, &ele).await?;
    }

    R::Ok(())
}

/// 不存在则新增该任务
pub async fn add_if_not_exist(name: &String) -> R<()> {
    let tasks = find_by_class_method_name(&name).await?;

    if tasks.is_none() {
        let mut t = Task::default();
        t.class_method_name = name.clone();
        t.is_enabled = true;
        t.current_run_status = TaskStatus::STOPPED;
        t.total_run_count = Some(0);
        t.scheduled_hour = 0;
        Task::insert(&CC.rb, &t).await?;
    }

    R::Ok(())
}

/// 更新任务状态
#[sql("UPDATE task SET current_run_status = ? WHERE class_method_name = ?")]
async fn update_task_status(
    rb: &dyn Executor,
    new_status: TaskStatus,
    class_method_name: &str,
) -> Result<ExecResult, rbatis::Error> {
}

/// 更新任务最后运行时间 updateLastRunTime

#[sql("UPDATE task SET last_run_time = ? WHERE class_method_name = ?")]
async fn update_last_run_time(
    rb: &dyn Executor,
    last_run_time: DateTime,
    class_method_name: &str,
) -> Result<ExecResult, rbatis::Error> {
}

/// 通过任务名称查找任务
pub async fn find_by_class_method_name(name: &str) -> R<Option<Task>> {
    let mut tasks = select_one_by_condition(&CC.rb, value! {"class_method_name":name}).await?;

    R::Ok(tasks)
}

use crate::api::bili;
use crate::app::constans::{DISLIKE_BY_USER_ID_TASK, DO_SEARCH_TASK, THUMB_UP_ALL_USER_VIDEO_TASK};
use crate::domain::dict::Dict;
use crate::domain::dtos::{
    AssociateRuleAc, SearchKeywordDto, SecondHandleDto, SingleMatchRuleAc, TestRuleDto,
};
use crate::domain::enumeration::{AccessType, DictStatus, DictType};
use crate::domain::video_detail::{MatchResult, VideoDetail};
use crate::service::rule_service::{
    build_complex_rule_list, build_single_match_rule_ac, get_match_need_config,
};
use crate::service::{bili_service, dict_service, rule_service, video_detail_service};
use crate::utils::collection_tool::VecGroupByExt;
use crate::utils::data_util::{bvid_to_aid, get_random_set, get_random};
use crate::utils::thread_util::ThreadUtil;
use rbatis::crud_traits::ValueOperatorSql;

#[py_sql(
    "`select * from task`  
     trim end=' where ':  
       ` where `  
       trim ' and ': for key,item in condition:  
                    if item == null:  
                       continue:  
                    if !item.is_array():  
                      ` and ${key.operator_sql()}#{item}`  
                    if item.is_array():  
                      ` and ${key} in (`  
                         trim ',': for _,item_array in item:  
                              #{item_array},  
                      `)`  
     ` limit 1`"
)]
async fn select_one_by_condition(
    rb: &dyn Executor,
    condition: rbs::Value,
) -> Result<Option<Task>, rbatis::Error> {
    impled!()
}

/// 执行任务并记录任务信息
pub async fn do_task<F, Fut>(method_name: String, task: F) -> R<bool>
where
    F: FnOnce() -> Fut + Send + 'static,
    Fut: Future<Output = R<()>> + Send + 'static,
{
    // 需要记录任务的执行情况
    add_if_not_exist(&method_name).await?;
    update_task_status(&CC.rb, TaskStatus::WAITING, &method_name).await?;
    update_last_run_time(&CC.rb, DateTime::now(), &method_name).await?;

    let method_name_move = method_name.clone();
    R::Ok(
        TASK_POOL
            .put_if_absent(method_name_move, async move || {
                let start = Instant::now();
                update_task_status(&CC.rb, TaskStatus::RUNNING, &method_name).await?;
                task().await?;
                let end = Instant::now();
                let millis = end.duration_since(start).as_millis();

                let mut t = find_by_class_method_name(&method_name).await?;
                if let Some(mut t) = t {
                    t.last_run_time = Some(DateTime::now());
                    t.total_run_count = Some(t.total_run_count.unwrap_or(0) + 1);
                    t.last_run_duration = Some(millis as u32);
                    t.current_run_status = TaskStatus::STOPPED;
                    Task::update_by_id(&CC.rb, &t).await?;
                }

                R::Ok(())
            })
            .await,
    )
}

/// 关键词搜索任务
pub async fn search_keyword_task() -> R<()> {
    let flag = do_task(DO_SEARCH_TASK.to_string(), async move || {
        let keyword_set = dict_service::get_search_keyword_set().await?;
        // todo 记得删除
        // let mut keyword_set:HashSet<String> =HashSet::new();
        // keyword_set.insert("红色沙漠".to_string());

        // 0.1 单一规则
        let (
            black_single_match,
            white_single_match,
            black_complex_rule,
            white_complex_rule,
            black_prompt,
            white_prompt,
            ai_chat_enable,
            single_match_enable,
            complex_match_enable,
            prompt,
        ) = rule_service::build_match_config().await?;
        for keyword in keyword_set {
            for page in 0..2 {
                info!("搜索关键词={}", keyword);
                let set: HashSet<SearchKeywordDto> = bili::search_keyword(&keyword, page)
                    .await?
                    .into_iter()
                    .collect();
                ThreadUtil::sleep(3).await;

                for item in set {
                    let aid = item.aid;
                    let video_detail_dto = bili::get_video_detail(aid).await?;
                    ThreadUtil::s1().await;
                    match first_process(
                        video_detail_dto.video_detail.into(),
                        ai_chat_enable,
                        single_match_enable,
                        complex_match_enable,
                        &prompt,
                        &black_single_match,
                        &white_single_match,
                        &black_complex_rule,
                        &white_complex_rule,
                        &black_prompt,
                        &white_prompt,
                    )
                    .await
                    {
                        Ok(_) => {}
                        Err(e) => {
                            error!("处理aid={aid} 时出错！ 错误：{:?}", e);
                        }
                    };
                    ThreadUtil::s5().await;
                }
            }
            ThreadUtil::sleep(3).await;
        }
        R::Ok(())
    })
    .await?;
    info!("关键词搜索任务 启动，提交结果：{flag}");
    R::Ok(())
}

/// 热门排行榜任务
pub async fn hot_rank_video_task() -> R<()> {
    let flag = do_task(DO_HOT_RANK_TASK.to_string(), async move || {
        // 0.1 单一规则
        let (
            black_single_match,
            white_single_match,
            black_complex_rule,
            white_complex_rule,
            black_prompt,
            white_prompt,
            ai_chat_enable,
            single_match_enable,
            complex_match_enable,
            prompt,
        ) = rule_service::build_match_config().await?;
        for page in 1..=10 {
            // todo 记得删
            // for page in 1..=1 {

            let set = bili::hot_rank_video(page, 10).await?;
            ThreadUtil::sleep(3).await;

            for item in set {
                let aid = item.id;
                match first_process(
                    item.into(),
                    ai_chat_enable,
                    single_match_enable,
                    complex_match_enable,
                    &prompt,
                    &black_single_match,
                    &white_single_match,
                    &black_complex_rule,
                    &white_complex_rule,
                    &black_prompt,
                    &white_prompt,
                )
                .await
                {
                    Ok(_) => {}
                    Err(e) => {
                        error!("处理aid={aid} 时出错！ 错误：{:?}", e);
                    }
                };
                ThreadUtil::s5().await;
            }
        }
        ThreadUtil::sleep(3).await;
        R::Ok(())
    })
    .await?;
    info!("热门排行榜任务 启动，提交结果：{flag}");
    R::Ok(())
}

/// 首页推荐任务
pub async fn home_recommend_task() -> R<()> {
    let flag = do_task(DO_HOME_RECOMMEND_TASK.to_string(), async move || {
        // 0.1 单一规则
        let (
            black_single_match,
            white_single_match,
            black_complex_rule,
            white_complex_rule,
            black_prompt,
            white_prompt,
            ai_chat_enable,
            single_match_enable,
            complex_match_enable,
            prompt,
        ) = rule_service::build_match_config().await?;
        // for page in 1..=10 {
        // todo 记得删
        for page in 1..=1 {
            let aid_set = bili::get_home_recommend_video().await?;

            for aid in aid_set {
                let item = video_detail_service::find_or_save_video(aid).await?;
                match first_process(
                    item,
                    ai_chat_enable,
                    single_match_enable,
                    complex_match_enable,
                    &prompt,
                    &black_single_match,
                    &white_single_match,
                    &black_complex_rule,
                    &white_complex_rule,
                    &black_prompt,
                    &white_prompt,
                )
                .await
                {
                    Ok(_) => {}
                    Err(e) => {
                        error!("处理aid={aid} 时出错！ 错误：{:?}", e);
                    }
                };
                ThreadUtil::s5().await;
            }

            ThreadUtil::sleep(3).await;
        }
        ThreadUtil::sleep(3).await;
        R::Ok(())
    })
    .await?;
    info!("首页推荐任务 启动，提交结果：{flag}");
    R::Ok(())
}

/// 初次处理视频
pub async fn first_process(
    mut v: VideoDetail,
    ai_chat_enable: bool,
    single_match_enable: bool,
    complex_match_enable: bool,
    prefix_prompt: &String,
    //单一规则
    black_single_match: &SingleMatchRuleAc,
    white_single_match: &SingleMatchRuleAc,

    //复合规则
    black_complex_rule: &Vec<AssociateRuleAc>,
    white_complex_rule: &Vec<AssociateRuleAc>,

    //ai 规则
    black_prompt: &String,
    white_prompt: &String,
) -> R<()> {
    info!("first_process 处理视频:{},{:?}", v.id, v.title);

    let option = VideoDetail::select_by_id(&CC.rb, v.id).await?;
    if let Some(db) = option {
        if db.handle_step != 0 {
            // 已处理过，跳过
            info!(
                "first_process :{},{:?}  已处理过，跳过该视频",
                v.id, v.title
            );
            return R::Ok(());
        }
    } else {
        VideoDetail::insert(&CC.rb, &v).await?;
    }

    match rule_service::total_rule_match(
        &v,
        ai_chat_enable,
        single_match_enable,
        complex_match_enable,
        prefix_prompt,
        //单一规则
        black_single_match,
        white_single_match,
        //复合规则
        black_complex_rule,
        white_complex_rule,
        //ai 规则
        black_prompt,
        white_prompt,
    )
    .await
    {
        Ok((access_type, match_result)) => {
            info!(
                "first_process :{},{:?}  匹配结果为：{:?}",
                v.id, v.title, access_type
            );

            video_detail_service::update_handle_data(
                &mut v,
                1,
                Some(match_result),
                None,
                Some(access_type),
            )
            .await?;
        }
        Err(e) => {
            info!("first_process :{},{:?}  匹配错误！：{:?}", v.id, v.title, e);
        }
    };

    R::Ok(())
}

/// 二次处理视频
pub(crate) async fn second_process(dto: SecondHandleDto) -> R<String> {
    let v = VideoDetail::select_by_id(&CC.rb, dto.id).await?;
    if let Some(mut v) = v {
        let mut r = v.handle_reason.clone().unwrap_or(MatchResult::default());
        if let Some(reason) = dto.user_handle_reason {
            r.user_handle_reason = Some(reason);
        }

        //如果是被纠正的数据，那么执行步骤变成2
        let handle_step =   if dto.re_handle{

            2

        }else {
            if dto.handle_type == AccessType::OTHER{
                100
            }else { 2 }

        };

        video_detail_service::update_handle_data(
            &mut v,
            handle_step,
            Some(r),
            Some(DateTime::now()),
            Some(dto.handle_type),
        )
        .await?;
        R::Ok("成功".to_string())
    } else {
        R::Err(HttpError::BadRequest("id 对应的视频不存在".to_string()))
    }
}

/// 批量进行二次处理
pub(crate) async fn batch_second_handle(arr: Vec<SecondHandleDto>) -> R<String> {
    // 批量处理都是直接按照一次匹配的数据进行处理，所以只需要修改handle_step, handle_time 即可
    // 创建包含新值的 VideoDetail 实例
    // let update_data = VideoDetail {
    //     id: 0, // 这个字段会被跳过，因为它是主键
    //     handle_step: 2u64,
    //     handle_time: Some(DateTime::now()),
    //     // 其他字段保持 None，这样不会被更新
    //     tid: None,
    //     tname: None,
    //     pic: None,
    //     title: None,
    //     pubdate: None,
    //     desc_field: None,
    //     duration: None,
    //     dynamic: None,
    //     bvid: String::new(),
    //     owner_id: None,
    //     handle_reason: None,
    //     handle_type: None,
    //     created_date: None,
    //     tag: None,
    // };

    let len = arr.len();
    let id_arr: Vec<u64> = arr.into_iter().map(|f| f.id).collect();

    // 使用 update_by_map 批量更新

    let exec_result = VideoDetail::update_handle_step_by_ids(
        &CC.rb,
        &id_arr,
         2u64,
         DateTime::now()
    ).await?;
    // let exec_result = VideoDetail::update_by_map(
    //     &CC.rb,
    //     &update_data,
    //     value! {
    //         "id": id_arr,  // 使用数组实现 IN 查询
    //         "column": ["handle_step", "handle_time"]  // 只更新这两个字段
    //     },
    // )
    // .await?;

    R::Ok(format!("成功修改{}条数据", len))
}

/// 把所有任务的状态都改为停止
pub(crate) async fn update_stop_status() -> R<ExecResult> {
    let exec_result: ExecResult = Task::update_task_state(&CC.rb, TaskStatus::STOPPED).await?;

    R::Ok(exec_result)
}

/// 修改定时任务状态
pub(crate) async fn set_cron(is_enable: bool)->R<()> {

    Task::update_task_is_enabled(&CC.rb, is_enable).await?;
    R::Ok(())
}

/// 点赞用户所有视频
///
/// # 参数
/// * `mid` - 用户id
/// * `page` - 起始页码
/// * `keyword` - 搜索关键词
///
/// # 功能
/// 1. 获取用户所有投稿视频
/// 2. 随机顺序点赞所有视频
/// 3. 每次点赞之间随机休眠 10-23 秒
pub(crate) async fn thumb_up_user_all_video(mid: u64, page: i64, keyword: &str) -> R<()> {
    info!("开始点赞用户 {} 的所有视频，起始页码: {}, 关键词: {}", mid, page, keyword);

    // 获取用户所有投稿视频
    let user_submission_videos = bili::search_user_all_submission_video(mid, page, keyword).await?;

    if user_submission_videos.is_empty() {
        info!("用户 {} 投稿视频为空", mid);
        return R::Ok(());
    }

    info!("用户 {} 共有 {} 条投稿视频，开始点赞", mid, user_submission_videos.len());

    // 生成随机索引集合
    // 注意：当视频数量为1时，end值需要特殊处理，避免 panic
    let end_index = if user_submission_videos.len() > 1 {
        (user_submission_videos.len() - 1) as i32
    } else {
        1 as i32
    };
    let indices = get_random_set(user_submission_videos.len(), 0, end_index);

    // 按随机索引顺序遍历视频
    for index in indices {
        let video = &user_submission_videos[index as usize];
        let title = video.title.clone().unwrap_or_else(|| "未知标题".to_string());

        match bili::thumb_up(video.aid).await {
            Ok(_) => {
                info!("点赞成功: {}", title);
            }
            Err(e) => {
                error!("点赞失败: {}, 错误: {:?}", title, e);
            }
        }

        // 随机休眠 10-23 秒
        let sleep_seconds = get_random(10, 23);
        ThreadUtil::sleep(sleep_seconds as u64).await;
    }

    info!("共点赞用户 {} 的 {} 条视频", mid, user_submission_videos.len());
    R::Ok(())
}

#[cfg(test)]
mod tests {
    use crate::app::task_pool::TASK_POOL;
    use crate::domain::dtos::SecondHandleDto;
    use crate::domain::enumeration::AccessType;
    use crate::domain::video_detail::VideoDetail;
    use crate::service::task_service::{batch_second_handle, default_process, home_recommend_task, hot_rank_video_task, search_keyword_task, second_process, set_cron, update_stop_status};
    use crate::{
        app::{config::CC, response::R},
        domain::{enumeration::TaskStatus, task::Task},
        impl_select_by_id,
        service::task_service::update_task_status,
    };
    use log::{error, info};
    use rbatis::dark_std::sync::vec;
    use rbatis::rbdc::DateTime;
    use rbs::value;
    use tokio::runtime::Runtime;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    //set_cron
    #[tokio::test]
    async fn test_set_cron() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        set_cron(false).await.unwrap();


        //最后一句必须是这个
        log::logger().flush();
    }


    //update_handle_step_by_ids
    #[tokio::test]
    async fn test_update_handle_step_by_ids() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let update_handle_step_by_ids = VideoDetail::update_handle_step_by_ids(
            &CC.rb,
            &vec![305988942u64],
            1,
            DateTime::now()


        ).await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_do_default_process_video() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        default_process().await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }

    //update_stop_status
    #[tokio::test]
    async fn test_update_stop_status() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        let update_stop_status = update_stop_status().await.unwrap();
        println!("{:#?}", update_stop_status);

        //最后一句必须是这个
        log::logger().flush();
    }

    //batch_second_handle

    #[tokio::test]
    async fn test_batch_second_handle() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        batch_second_handle(vec![
            SecondHandleDto {
                id: 116259932933316u64,
                handle_type: AccessType::BLACK,
                user_handle_reason: None,
                re_handle:false
            },
            SecondHandleDto {
                id: 116301943021405u64,
                handle_type: AccessType::BLACK,
                user_handle_reason: None,
                re_handle:false
            },
        ])
        .await
        .unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }
    //116286373759185
    #[tokio::test]
    async fn test_second_process() {
        //第一句必须是这个
        crate::init().await;



        second_process(SecondHandleDto {
            id: 114206670064346,
            handle_type: AccessType::OTHER,
            user_handle_reason: None,
            re_handle:false
        })
        .await
        .unwrap();
        //最后一句必须是这个
        log::logger().flush();
    }

    // home_recommend_task
    #[tokio::test]
    async fn test_home_recommend_task() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        home_recommend_task().await.unwrap();
        TASK_POOL.shutdown().await;
        info!("任务结束！");
        //最后一句必须是这个
        log::logger().flush();
    }
    #[tokio::test]
    async fn test_hot_rank_video_task() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        hot_rank_video_task().await.unwrap();
        TASK_POOL.shutdown().await;
        info!("任务结束！");
        //最后一句必须是这个
        log::logger().flush();
    }
    #[tokio::test]
    async fn test_search_keyword_task() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        search_keyword_task().await.unwrap();
        TASK_POOL.shutdown().await;
        info!("任务结束！");
        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_impl_delete_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        Task::delete_by_id(&CC.rb, "11679171886120965")
            .await
            .unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_impl_update_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let id = "11679233969684485";
        let mut t: Task = Task::select_by_id(&CC.rb, id).await.unwrap().unwrap();

        t.last_run_time = Some(DateTime::now());
        t.total_run_count = Some(2001);
        t.class_method_name = String::from("111222测试的类方法名啊");
        t.task_name = Some(String::from("111222测试的名称啊"));

        Task::update_by_id(&CC.rb, &t).await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_impl_select_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let task = Task::select_by_id(&CC.rb, "5").await.unwrap();

        println!("task: {:?}", task);

        //最后一句必须是这个
        log::logger().flush();
    }

    //测试 impl_select_one_by_condition
    #[tokio::test]
    async fn test_impl_select_one_by_condition() {
        //第一句必须是这个
        crate::init().await;

        let select_one_by_condition =
            Task::select_one_by_condition(&CC.rb, value! {"id":"1883877101111631873"})
                .await
                .unwrap();

        println!("select_one_by_condition: {:?}", select_one_by_condition);

        //最后一句必须是这个
        log::logger().flush();
    }

    // 测试 do_task
    #[tokio::test]
    async fn test_do_task() {
        crate::init().await;

        let name = "test_do_task".to_string();

        // 定义一个简单的测试任务
        let task_fn = || async {
            // 模拟一些工作
            tokio::time::sleep(tokio::time::Duration::from_millis(10)).await;

            println!("任务执行完毕");
            R::Ok(())
        };

        // 执行任务
        let result = super::do_task(name.clone(), task_fn).await;

        // 验证任务被成功提交
        assert!(result.is_ok());
        assert_eq!(result.unwrap(), true); // TASK_POOL.put_if_absent 应该返回true表示任务被添加

        // 等待一段时间确保任务执行完成
        tokio::time::sleep(tokio::time::Duration::from_millis(50)).await;

        // 验证任务状态已被更新
        let task_result = super::find_by_class_method_name(&name).await;
        assert!(task_result.is_ok());
        let task = task_result.unwrap().unwrap();

        // 验证任务状态是 STOPPED (因为任务已经执行完毕)
        println!("任务状态: {:#?}", task);
        assert_eq!(task.current_run_status, TaskStatus::STOPPED);

        // 验证总运行次数增加
        assert_eq!(task.total_run_count.unwrap(), 1);

        // 验证运行时间已被记录
        assert!(task.last_run_duration.unwrap() > 0);

        log::logger().flush();
    }

    // 测试 select_one_by_condition
    #[tokio::test]
    async fn test_select_one_by_condition() {
        crate::init().await;

        let name = "test_select_one_by_condition".to_string();

        // 先确保任务存在
        super::add_if_not_exist(&name).await.unwrap();

        // 测试通过条件查找任务 - 存在的任务
        let condition = value! {"class_method_name": &name};
        let result = super::select_one_by_condition(&CC.rb, condition).await;
        assert!(result.is_ok());
        let task = result.unwrap();
        assert!(task.is_some());
        println!("任务: {:#?}", task);
        assert_eq!(task.unwrap().class_method_name, name);

        // 测试通过条件查找任务 - 不存在的任务
        let not_exist_name = "not_exist_task_name";
        let condition = value! {"class_method_name": not_exist_name};
        let result = super::select_one_by_condition(&CC.rb, condition).await;
        assert!(result.is_ok());
        assert!(result.unwrap().is_none());

        log::logger().flush();
    }


}

