use axum::{Router, debug_handler, extract::Path};
use log::info;

use crate::{
    app::response::{OkRespExt, RR},
    entity::{dtos::{AssociateRuleListDto, PageDTO}, enumeration::AccessType},
    extractor::path::MyPath,
    handler::associate_rule,
    service::associate_rule_service,
};

pub fn create_router() -> Router {
    Router::new().route(
        "/list/{access_type}/{page}/{limit}",
        axum::routing::get(list),
    )
}

/// 查询规则列表
#[debug_handler]
async fn list(
    MyPath((access_type, page, limit)): MyPath<(AccessType, u64, u64)>,
) -> RR<PageDTO<AssociateRuleListDto>> {
    let page_dto =
        associate_rule_service::get_associate_tule_list(access_type, page, limit).await?;

    RR::success(page_dto)
}
