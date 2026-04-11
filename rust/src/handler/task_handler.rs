use crate::app::config::CC;
use crate::app::error::HttpError;
use crate::app::response::{FailRespExt, OkRespExt, RR};
use crate::domain::dtos::{PageDTO, SearchHandleVideoRequest, SecondHandleDto, VideoVo};
use crate::domain::task::Task;
use crate::service::{task_service, video_detail_service};
use axum::extract::Query;
use axum::routing::{get, post, put};
use axum::{Json, Router, debug_handler};
use rbs::value;

pub fn create_router() -> Router {
    Router::new()
        .route("/second-process", axum::routing::post(second_handler))
        .route("/batch-second-process", post(batch_second_handle))
        .route("/already-handle", get(search_handle_video))
        .route("/task-list", get(task_list))
        .route("/", put(update_task))
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
pub async fn task_list()->RR<Vec<Task>>{

    let list = Task::select_by_map(&CC.rb, value!{}).await?;

    RR::success(list)
}

/// 修改任务信息
#[debug_handler]
pub async fn update_task(Json(t):Json<Task>)->RR<()>{


    let update_by_id: rbatis::rbdc::db::ExecResult = Task::update_by_id(&CC.rb, &t).await?;


    if update_by_id.rows_affected>0{
        RR::success(())
    }else {
        
        RR::fail(HttpError::BadRequest("更新失败！".to_string()))
    }



}