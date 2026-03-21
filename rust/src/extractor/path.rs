

use axum::extract::FromRequestParts;

use crate::app::error::HttpError;
#[derive(Debug,Clone,Copy,Default,FromRequestParts)]
#[from_request(via(axum::extract::Path),rejection(HttpError))]
pub struct MyPath<T>(pub T);

