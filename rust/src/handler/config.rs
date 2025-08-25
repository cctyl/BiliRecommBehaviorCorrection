use axum::{Router, debug_handler};

use crate::app::response::{OkRespExt, RR};

pub fn create_router() -> Router {
    Router::new()
    .route("/tv-qr-code", axum::routing::get(get_tv_qr_code))
    .route("/tv-scan-result", axum::routing::get(get_tv_qr_code_scan_result))
}

#[debug_handler]
async fn get_tv_qr_code() -> RR<String> {
    let get_tv_login_qr_code = crate::api::bili::get_tv_login_qr_code().await?;
    RR::success(get_tv_login_qr_code)
}



#[debug_handler]
async fn get_tv_qr_code_scan_result() -> RR<serde_json::Value> {

    RR::success(crate::api::bili::get_tv_qr_code_scan_result().await?)
}
