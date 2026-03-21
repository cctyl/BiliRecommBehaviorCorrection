use axum::{Router, debug_handler, extract::Path};
use log::info;

use crate::{app::response::{OkRespExt, RR}, entity::enumeration::AccessType, extractor::path::MyPath};




pub fn create_router() -> Router {
    Router::new().route("/list/{access_type}/{page}/{limit}", axum::routing::get(list))
}

/// 查询规则列表
#[debug_handler]
async fn list(MyPath((access_type,page,limit)): MyPath<(AccessType,u64,u64)>)-> RR<String>{

    
    
    RR::success("ok".to_string())
}