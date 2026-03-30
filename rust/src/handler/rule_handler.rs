use axum::{Json, Router, debug_handler, response::IntoResponse};
use log::info;
use rbs::value;
use serde::Serialize;

use crate::{app::{config::CC, constans::DEFAULT_PROMPT, response::{OkRespExt, RR}}, domain::{dict::Dict, dtos::TestRuleDto, enumeration::{AccessType, DictStatus, DictType}, video_detail::{AiMatch, MatchResult, VideoDetail}}, extractor::path::MyPath, handler::ai, service::{rule_service, video_detail_service}, utils::{collection_tool::VecGroupByExt, data_util::bvid_to_aid}};

pub fn create_router() -> Router {
    Router::new().route(
        "/testRule",
        axum::routing::post(test_rule),
    )
}

/// 测试ai对该视频的判断
#[debug_handler]
pub async fn test_rule(Json(dto): Json<TestRuleDto>)->RR<MatchResult>
{
    
    let TestRuleDto{ bvid, ai_chat_enable, single_match_enable, complex_match_enable } = dto;
    let aid = bvid_to_aid(&bvid);
    let find_or_save_video = video_detail_service::find_or_save_video(aid).await?;
    let (_,m) = rule_service::total_rule_match(&find_or_save_video, ai_chat_enable, single_match_enable, complex_match_enable).await?;
    RR::success(m)
}


