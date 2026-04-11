use crate::app::response::{OkRespExt, RR};
use crate::domain::dtos::{PageDTO, SearchHandleVideoRequest, SecondHandleDto, VideoVo};
use crate::service::{task_service, video_detail_service};
use axum::extract::Query;
use axum::routing::{get, post};
use axum::{Json, Router, debug_handler};

pub fn create_router() -> Router {
    Router::new()
        .route("/second-process", axum::routing::post(second_handler))
        .route("/batch-second-process", post(batch_second_handle))
        .route("/already-handle", get(search_handle_video))
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
