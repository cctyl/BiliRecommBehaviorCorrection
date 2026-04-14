use crate::app::config::CC;
use crate::app::constans::THUMB_UP_ALL_USER_VIDEO_TASK;
use crate::app::error::HttpError;
use crate::app::response::{FailRespExt, OkRespExt, RR};
use crate::domain::dtos::{CommonTriggerTaskRequest, PageDTO, SearchHandleVideoRequest, SecondHandleDto, VideoVo, ThumbUpUserAllVideoRequest};
use crate::domain::task::Task;
use crate::service::{task_service, video_detail_service};
use axum::extract::{Path, Query};
use axum::routing::{get, post, put};
use axum::{Json, Router, debug_handler};
use log::info;
use rbs::value;
use serde_json::Value;

pub fn create_router() -> Router {
    Router::new()
        .route("/second-process", axum::routing::post(second_handler))
        .route("/batch-second-process", post(batch_second_handle))
        .route("/already-handle", get(search_handle_video))
        .route("/task-list", get(task_list))
        .route("/", put(update_task))
        .route("/common-trigger-task", get(common_trigger_task))
        .route("/thumb-up-all", post(thumb_up_user_all_video))
}

/// 二次处理视频
#[debug_handler]
pub async fn second_handler(Json(dto): Json<SecondHandleDto>) -> RR<String> {
    let string = task_service::second_process(dto).await?;
    RR::success(string)
}

/// 批量二次处理
#[debug_handler]
pub async fn batch_second_handle(Json(arr): Json<Vec<SecondHandleDto>>) -> RR<String> {
    let string = task_service::batch_second_handle(arr).await?;
    RR::success(string)
}

/// 查询处理中/待处理数据
#[debug_handler]
pub async fn search_handle_video(
    Query(r): Query<SearchHandleVideoRequest>,
) -> RR<PageDTO<VideoVo>> {
    let get_handle_video = video_detail_service::get_handle_video(
        r.page,
        r.limit,
        r.search,
        r.handle_step,
        r.handle_type,
    )
    .await?;

    RR::success(get_handle_video)
}

/// 任务列表
#[debug_handler]
pub async fn task_list() -> RR<Vec<Task>> {
    let list = Task::select_by_map(&CC.rb, value! {}).await?;

    RR::success(list)
}

/// 修改任务信息
#[debug_handler]
pub async fn update_task(Json(t): Json<Task>) -> RR<()> {
    let update_by_id: rbatis::rbdc::db::ExecResult = Task::update_by_id(&CC.rb, &t).await?;

    if update_by_id.rows_affected > 0 {
        RR::success(())
    } else {
        RR::fail(HttpError::BadRequest("更新失败！".to_string()))
    }
}

/// 触发任务
#[debug_handler]
pub async fn common_trigger_task(Query(CommonTriggerTaskRequest{name}): Query<CommonTriggerTaskRequest>) -> RR<()> {


    task_service::do_task_by_name(&name).await?;
    RR::success(())


}

/// 点赞用户所有视频
#[debug_handler]
pub async fn thumb_up_user_all_video(
    Query(request): Query<ThumbUpUserAllVideoRequest>,
) -> RR<String> {
    info!("收到点赞用户所有视频请求: mid={}, page={}, keyword={}", request.mid, request.page, request.keyword);

    // 构建任务名
    let task_name = format!("{}:{}", THUMB_UP_ALL_USER_VIDEO_TASK, request.mid);

    // 提交任务
    let result = task_service::do_task(task_name.clone(), async move || {
        task_service::thumb_up_user_all_video(request.mid, request.page, &request.keyword).await
    })
    .await?;

    if result {
        RR::success("点赞任务已开始".to_string())
    } else {
        RR::fail(HttpError::BadRequest("该任务正在被运行中，请等待上一个任务结束".to_string()))
    }
}
