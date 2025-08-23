#![cfg_attr(
    debug_assertions,
    allow(dead_code, unused_imports, unused_variables, unused_mut)
)]

mod app;
mod dao;

mod entity;
mod handler;
mod utils;

use crate::app::database::{self, CONTEXT};
use app::config::Config;
use axum::{Extension, Router, extract::DefaultBodyLimit};
use bytesize::ByteSize;
use std::{sync::Arc, time::Duration};
use tokio::{net::TcpListener, runtime::Runtime};
use tower_http::{
    cors::{self, CorsLayer},
};



#[tokio::main]
pub async fn main() {
  


    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CONTEXT.init().await;
    let port = CONTEXT.config.port;
    println!(
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
    let body_limit = DefaultBodyLimit::max(ByteSize::mib(2048).as_u64() as usize);



    handler::create_router()
        .layer(cors)
        .layer(body_limit)


}
