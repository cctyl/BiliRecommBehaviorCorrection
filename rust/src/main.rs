#![allow(non_camel_case_types)]
#![cfg_attr(
    debug_assertions,
    allow(dead_code, unused_imports, unused_variables, unused_mut)
)]

mod app;

mod api;
mod entity;
mod handler;
mod service;
mod single_test;
mod utils;
use crate::app::database::{self};
use crate::app::config::CC;
use crate::app::error::HttpError;
use crate::app::global::GLOBAL_STATE;
use crate::app::response::R;
use crate::entity::enumeration::{Classify, MediaType};
use crate::utils::migration::start_migration;

use app::config::ServerConfig;
use axum::{Extension, Router, extract::DefaultBodyLimit};
use log::{error, info};
use rbatis::rbdc::DateTime;
use rbatis::{RBatis, sql};

use rbatis::impled;
use std::{sync::Arc, time::Duration};
use tokio::{net::TcpListener, runtime::Runtime};
use tower_http::cors::{self, CorsLayer};

use crate::service::cookie_header_data_service::{self, get_map_by_classify_and_media_type, init_common_header_map};
use std::collections::HashMap;
use std::fs;
use std::path::Path;

/**
 * 初始化数据库和日志
 */
pub async fn init() -> u16 {
    //日志
    crate::utils::log::init_log();

    //全局变量的初始化
    CC.init().await 
        .expect("全局变量初始化失败!");
  

    //端口
    let port = CC.config.port;

    init_common_header_map().await.unwrap();
    port
}

#[tokio::main]
pub async fn main() {
    let port = init().await;
    start_migration().await.expect("数据库迁移失败");

    info!(
        "{}",
        format!("🚀 Server is running on http://localhost:{}", port)
    );

    let listener = tokio::net::TcpListener::bind(format!("0.0.0.0:{}", port))
        .await
        .unwrap();
    let app = build_router();
    axum::serve(listener, app).await.unwrap();
}

fn build_router() -> Router {
    let cors = CorsLayer::new()
        .allow_origin(cors::Any)
        .allow_methods(cors::Any)
        .allow_headers(cors::Any)
        .allow_credentials(false)
        .max_age(Duration::from_secs(3600 * 12));

    handler::create_router().layer(cors)
}
