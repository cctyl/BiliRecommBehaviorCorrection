use crate::web::{index_handler, static_assets_handler};
use crate::{app::error::HttpError, app::middleware::auth, app::response::R};
use axum::{Extension, Router, middleware, routing};
use axum::routing::any;

pub mod ai;
pub mod associate_rule_handler;
pub mod bili_api_handler;
pub mod black_rule_handler;
pub mod config_handler;
pub mod cookie_header_data_handler;
pub mod dict_handler;
pub mod file;
pub mod region_handler;
pub mod rule_handler;
pub mod task_handler;

pub fn create_router() -> Router {
    let api_router = Router::new()
        .nest("/config", config_handler::create_router())
        .nest(
            "/cookie-header-data",
            cookie_header_data_handler::create_router(),
        )
        .nest("/dict", dict_handler::create_router())
        .nest("/black-rule", black_rule_handler::create_router())
        .nest("/ai", ai::create_router())
        .nest("/associate_rule", associate_rule_handler::create_router())
        .nest("/region", region_handler::create_router())
        .nest("/bili", bili_api_handler::create_router())
        .nest("/rule", rule_handler::create_router())
        .nest("/task", task_handler::create_router())
        // /api/xxx 匹配不到的路径都会到这里，防止被后续的 static_assets_handler 匹配
        .route("/{*path}", any(async || -> R<()> { Err(HttpError::BadRequest("Not found".to_string())) }))
        .method_not_allowed_fallback(async || -> R<()> {
            Err(HttpError::BadRequest("Method not allowed".to_string()))
        })
        .route_layer(middleware::from_fn(auth));

    Router::new()
        .nest("/api", api_router)
        //这个fallback可以用于前面没匹配到的，也就是给静态资源和index.html的请求
        .merge(
            Router::new()
                .route("/{*file}", routing::get(static_assets_handler))
                // .route_layer(CompressionLayer::new())
                .fallback(index_handler)

        )


}
