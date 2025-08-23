
use axum::{Extension, Router, middleware};

use crate::{app:: error::HttpError, app::middleware::auth, app::response::R};


pub mod file;
pub fn create_router() -> Router {
    let api_router = Router::new()
        .nest(
            "/file",
            file::create_router()
        )
        .fallback(async || -> R<()> {
            Err(HttpError::BadRequest("Not found".to_string()))
        })
        .method_not_allowed_fallback(async || -> R<()> {
            Err(HttpError::BadRequest("Method not allowed".to_string()))
        })
        .route_layer(middleware::from_fn(auth))
       ;

    Router::new().nest("/api", api_router)
}
