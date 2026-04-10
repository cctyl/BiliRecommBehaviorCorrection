use axum::{debug_handler, Json, Router};
use crate::app::response::{OkRespExt, RR};
use crate::domain::dtos::SecondHandleDto;
use crate::handler::rule_handler::test_rule;
use crate::service::task_service;

pub fn create_router() -> Router {
    Router::new().route(
        "/testRule",
        axum::routing::post(test_rule),
    )
}

/// 二次处理视频
#[debug_handler]
pub async fn second_handler(Json(dto):  Json<SecondHandleDto>)->RR<String>{
    let string = task_service::second_process(dto).await?;
    RR::success(string)
}