use std::sync::Arc;

use axum::{Extension, extract::Request, http::header, middleware::Next, response::IntoResponse};

use log::info;
use serde::{Deserialize, Serialize};

use crate::app::{
    database::CONTEXT,
    error::{ErrorMessage, HttpError},
    response::R,
};

pub async fn auth(mut req: Request, next: Next) -> R<impl IntoResponse> {
    //从cookie获取token，拿不到就从请求头中获取
    let token = req
        .headers()
        .get(header::AUTHORIZATION)
        .and_then(|auth_header| auth_header.to_str().ok())
        .and_then(|auth_value| {
            if auth_value.starts_with("Bearer ") {
                Some(auth_value.trim_start_matches("Bearer ").to_string())
            } else {
                None
            }
        })
        // .ok_or_else(|| HttpError::Unauthorized(ErrorMessage::TokenNotProvided.to_string()))?
        ;

    //TODO 暂时关闭
    // if token != CONTEXT.config.secret {
    //     info!("访问失败：token={}", token);
    //     return Err(HttpError::Unauthorized(
    //         ErrorMessage::InvalidToken.to_string(),
    //     ));
    // }

    Ok(next.run(req).await)
}
