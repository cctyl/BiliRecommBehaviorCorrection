use axum::{Router, debug_handler};

use crate::{
    app::{
        database::CONTEXT,
        response::{OkRespExt, RR},
    },
    entity::models::Config,
};

pub fn create_router() -> Router {
    Router::new()
        .route("/tv-qr-code", axum::routing::get(get_tv_qr_code))
        .route(
            "/tv-scan-result",
            axum::routing::get(get_tv_qr_code_scan_result),
        )
        .route("/get_config_list", axum::routing::get(get_config_list))
}

#[debug_handler]
async fn get_tv_qr_code() -> RR<String> {
    let get_tv_login_qr_code = crate::api::bili::get_tv_login_qr_code().await?;
    RR::success(get_tv_login_qr_code)
}

#[debug_handler]
async fn get_config_list() -> RR<Vec<Config>> {
    let select_all: Vec<Config> = Config::select_all(&CONTEXT.rb).await?;

    RR::success(select_all)
}

#[debug_handler]
async fn get_tv_qr_code_scan_result() -> RR<serde_json::Value> {
    RR::success(crate::api::bili::get_tv_qr_code_scan_result().await?)
}
