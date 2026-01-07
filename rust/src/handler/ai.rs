use axum::{Json, Router, debug_handler};
use serde::Deserialize;

use crate::{app::{config::CC, response::{OkRespExt, RR}}, utils::glm_chat::Message};

pub fn create_router() -> Router {
    Router::new().route("/test-chat", axum::routing::post(test_chat))
}





#[derive(Debug, Deserialize)]

struct ChatParam {

    messages:Vec<Message>
}

/// 测试聊天
#[debug_handler]
 async fn test_chat( Json(body) : Json<ChatParam> )->RR<String>{

    let chat = &CC.chat;
    let read = chat.read().await;

    let chat_request = read.chat_request(body.messages).await?;

    RR::success(chat_request)
}