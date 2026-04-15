use axum::debug_handler;
use crate::app::response::{OkRespExt, RR};
use crate::domain::overview::OverviewVo;
use crate::service::overview_service::get_overview_info;
use axum::extract::Query;
use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct OverviewQuery {
    year: u32,
}

#[debug_handler]
pub async fn overview_info(Query(params): Query<OverviewQuery>) -> RR<OverviewVo> {
    let overview_vo = get_overview_info(params.year).await?;
    RR::success(overview_vo)
}

pub fn create_router() -> axum::Router {
    use axum::routing::get;

    axum::Router::new()
        .route("/", get(overview_info))
}
