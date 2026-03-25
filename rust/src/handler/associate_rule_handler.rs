use axum::{Json, Router, debug_handler, extract::Path, routing::{delete, post}};
use log::info;

use crate::{
    app::response::{OkRespExt, RR},
    domain::{
        dtos::{AssociateRuleAddDto, AssociateRuleListDto, AssociateRuleUpdateDto, PageDTO},
        enumeration::AccessType, associate_rule::AssociateRule,
    },
    extractor::path::MyPath,
    handler::associate_rule_handler,
    service::associate_rule_service,
};

pub fn create_router() -> Router {
    Router::new()
        .route(
            "/list/{access_type}/{page}/{limit}",
            axum::routing::get(list),
        )
        .route("/del/{id}", delete(delete_rule_by_id))
        .route("/", post(add_rule).put(update_rule))
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

/// 根据id删除一条复合规则
#[debug_handler]
async fn delete_rule_by_id(MyPath(id): MyPath<String>) -> RR<String> {
    RR::success(associate_rule_service::delete_rule_by_id(id).await?)
}

/// 添加一条复合规则
#[debug_handler]
async fn add_rule(Json(add):Json<AssociateRuleAddDto>) ->RR<AssociateRule>{

    RR::success(associate_rule_service::add_rule(add).await?)
}


/// 修改一条复合规则
#[debug_handler]
async fn update_rule(Json(update):Json<AssociateRuleUpdateDto>) ->RR<AssociateRule>{

    RR::success(associate_rule_service::update_rule(update).await?)
}