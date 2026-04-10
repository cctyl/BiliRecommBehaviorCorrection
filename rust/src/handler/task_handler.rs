use crate::app::response::{OkRespExt, RR};
use crate::domain::dtos::SecondHandleDto;
use crate::service::task_service;
use axum::routing::post;
use axum::{Json, Router, debug_handler};

pub fn create_router() -> Router {
    Router::new()
        .route("/second-process", axum::routing::post(second_handler))
        .route("/batch-second-process", post(batch_second_handle))
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
