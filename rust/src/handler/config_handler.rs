use std::{collections::HashMap, hash::Hash};

use axum::{debug_handler, extract::Query, Json, Router};
use fast_log::config;
use serde::Deserialize;

use crate::{
    api::bili, app::{
        config::CC,
        response::{OkRespExt, RR, *},
    }, entity::{dtos::ConfigAddUpdateDTO, models::Config}, service::{config_service, cookie_header_data_service}
};



pub fn create_router() -> Router {
    Router::new()
        .route("/tv-qr-code", axum::routing::get(get_tv_qr_code))
        .route(
            "/tv-scan-result",
            axum::routing::get(get_tv_qr_code_scan_result),
        )
        .route("/get_config_list", axum::routing::get(get_standard_config_info))
        .route("/check-accesskey", axum::routing::get(check_accesskey))
        .route("/getPic", axum::routing::get(stream_image_from_url))
        .route("/check-cookie", axum::routing::get(check_accesskey))
        .route("/standard", axum::routing::get(get_standard_config_info).post(update_standard_config_info))
        .route("/refresh-cookie", axum::routing::get(get_refresh_cookie).put(update_refresh_cookie) )
}

use axum::{http::header, response::IntoResponse, http::StatusCode, body::Body};  
use reqwest::{self};  
use tokio_stream::StreamExt;  
  

#[derive(Debug, Deserialize)]

struct UpdateRefreshCookieParams {

    cookie_str: String
}
/**
 * 更新 及时更新的cookie
 */
#[debug_handler]
async fn update_refresh_cookie(Query(UpdateRefreshCookieParams{ cookie_str}): Query<UpdateRefreshCookieParams>) -> RR<HashMap<String, String>>{  

    if cookie_str.is_empty() {
        return RR::fail("cookie_str 不能为空");
    }

    let get_refresh_cookie = cookie_header_data_service::update_refresh_cookie(cookie_str).await?;
    RR::success(get_refresh_cookie)
}

/**
 * 获取 及时更新的cookie
 */
#[debug_handler]
async fn get_refresh_cookie() -> RR<HashMap<String, String>>{  
    let get_refresh_cookie = cookie_header_data_service::get_refresh_cookie().await?;
    RR::success(get_refresh_cookie)
}
/**
 * 查询基本配置信息
 */
#[debug_handler]
async fn update_standard_config_info(Json(payload): Json<Vec<ConfigAddUpdateDTO>>)->RR<()>{
    config_service::update_config_list(payload).await?;

    RR::success(())
}



/**
 * 
 * 检查cookie
 */
#[debug_handler]
async fn check_cookie() -> RR<serde_json::Value> {  

    let get_history = bili::get_history().await?;
    RR::success(get_history)
}


#[derive(Debug, Deserialize)]
struct UrlQueryParams {
    url: String
}
/**
 * 
 * 获取图片
 */
#[debug_handler]
async fn stream_image_from_url( Query(UrlQueryParams{url}): Query<UrlQueryParams>) -> Result<impl IntoResponse, (StatusCode, String)> {  
    let response = reqwest::get(url)  
        .await  
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, format!("下载失败: {e}")))?;  
      
    if !response.status().is_success() {  
        return Err((StatusCode::NOT_FOUND, "图片不存在".to_string()));  
    }  
      
    let content_type = response  
        .headers()  
        .get("content-type")  
        .and_then(|ct| ct.to_str().ok().map(|s| s.to_string()))  
        .unwrap_or(String::from("application/octet-stream"));  
      
    // 将reqwest的字节流转换为axum的Body  
    let stream = response.bytes_stream();  
    let body = Body::from_stream(stream);  
      
    Ok((  
        [(header::CONTENT_TYPE, content_type)],  
        body  
    ))  
}


/**
 * 获取二维码

 */
#[debug_handler]
async fn get_tv_qr_code() -> RR<String> {
    let get_tv_login_qr_code = bili::get_tv_login_qr_code().await?;
    RR::success(get_tv_login_qr_code)
}
/**
 * 查询配置列表
 */
#[debug_handler]
async fn get_standard_config_info() -> RR<Vec<Config>> {
    let select_all: Vec<Config> = Config::select_all(&CC.rb).await?;

    RR::success(select_all)
}


/**
 * 查询二维码扫码结果
 */
#[debug_handler]
async fn get_tv_qr_code_scan_result() -> RR<serde_json::Value> {
    RR::success(bili::get_tv_qr_code_scan_result().await?)
}

/**
 * 检查access_key
 */
#[debug_handler]
async fn check_accesskey() -> RR<serde_json::Value> {
    match bili::get_user_info().await {
        Ok(info) => RR::success(info),
        Err(e) => {
            use crate::app::response::FailRespExt;
            RR::fail(e)
        }
    }
}
