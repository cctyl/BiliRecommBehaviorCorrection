use axum::{Router, debug_handler};

use crate::{
    app::response::{OkRespExt, RR}, entity::models::Region, service::region_service,
};

pub fn create_router() -> Router {
    Router::new()
        .route(
            "/list",
            axum::routing::get(list),
        )
    
}


/// 查询分区列表
#[debug_handler]
async fn list() ->RR<Vec<Region>>{
    let list =  region_service::list().await?;
    RR::success(list)
}