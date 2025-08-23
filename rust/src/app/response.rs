use axum::{http::StatusCode, response::IntoResponse};
use serde::{Deserialize, Serialize};

use crate::app::error::{ErrorMessage, HttpError};
pub type R<T> = Result<T, HttpError>;
pub type RR<T> = R<Resp<T>>;

#[derive(Debug, Serialize, Deserialize)]
pub struct Resp<T> 
{
    pub status: u16,
    pub message: String,
    pub data: Option<T>,
}

impl<T:Serialize> IntoResponse for Resp<T> {
    fn into_response(self) -> axum::response::Response {
        axum::Json(self).into_response()
    }
}




pub trait OkRespExt<T> {
    fn success(data: T) -> RR<T>;

}

pub trait OkMsgExt{
    fn msg(msg:&str)->RR<()>;
}


impl OkMsgExt for RR<()> {
    
    fn msg(msg:&str)->RR<()>{

        Ok(Resp {
            status: 200,
            message:msg.to_string(),
            data:None,
        })

    }
}

pub trait FailRespExt<T, D> {
    fn fail(e: D) -> RR<T>;
}

pub trait BizRespExt<T> {
    fn biz(msg: &str) -> RR<T>;
}
impl<T> OkRespExt<T> for RR<T> {
    fn success(data: T) -> RR<T> {
        Ok(Resp {
            status: 200,
            message: "success".to_string(),
            data: Some(data),
        })
    }
}



impl<T> FailRespExt<T, HttpError> for RR<T> {
    fn fail(e: HttpError) -> RR<T> {
        Err(e)
    }
}

impl<T> FailRespExt<T, &str> for RR<T> {
    fn fail(e: &str) -> RR<T> {
        Err(HttpError::ServerError(e.to_string()))
    }
}

impl<T> FailRespExt<T, ErrorMessage> for RR<T> {
    fn fail(e: ErrorMessage) -> RR<T> {
        Err(HttpError::ServerError(e.to_string()))
    }
}

impl<T> BizRespExt<T> for RR<T> {
    fn biz(msg: &str) -> RR<T> {
        Err(HttpError::Biz(msg.to_string()))
    }
}
