use axum::{Json, Router, debug_handler, response::IntoResponse};
use log::info;
use rbs::value;
use serde::Serialize;

use crate::{app::{config::CC, constans::DEFAULT_PROMPT, response::{OkRespExt, RR}}, domain::{dict::Dict, dtos::TestRuleDto, enumeration::{AccessType, DictStatus, DictType}, video_detail::{AiMatch, MatchResult, VideoDetail}}, extractor::path::MyPath, handler::ai, service::{rule_service, video_detail_service}, utils::{collection_tool::VecGroupByExt, data_util::bvid_to_aid}};
use crate::domain::dtos::{AssociateRuleAc, SingleMatchRuleAc};
use crate::service::rule_service::{build_complex_rule_list, build_single_match_rule_ac, get_match_need_config};

pub fn create_router() -> Router {
    Router::new().route(
        "/testRule",
        axum::routing::post(test_rule),
    )
}

/// 测试ai对该视频的判断
#[debug_handler]
pub async fn test_rule(Json(dto): Json<TestRuleDto>) -> RR<MatchResult>
{
    // 0.1 单一规则
    let black_single_match: SingleMatchRuleAc =
        build_single_match_rule_ac(AccessType::BLACK).await?;
    let white_single_match: SingleMatchRuleAc =
        build_single_match_rule_ac(AccessType::WHITE).await?;

    // 0.2 复合规则
    let black_complex_rule: Vec<AssociateRuleAc> = build_complex_rule_list(AccessType::BLACK).await?;
    let white_complex_rule: Vec<AssociateRuleAc> = build_complex_rule_list(AccessType::WHITE).await?;

    // 0.3 ai提示词
    let mut prompt_map = Dict::select_by_map(
        &CC.rb,
        value! {
            "dict_type":DictType::AI_JUDGMENT_PROMPT,
            "status":DictStatus::NORMAL
        },
    )
        .await?
        .group_by_full(|f| f.access_type);

    let black_prompt: String = prompt_map
        .remove(&AccessType::BLACK)
        .map_or(String::from(""), |mut f| {
            if f.is_empty() {
                String::from("")
            } else {
                f.remove(0).value
            }
        });
    let white_prompt: String = prompt_map
        .remove(&AccessType::WHITE)
        .map_or(String::from(""), |mut f| {
            if f.is_empty() {
                String::from("")
            } else {
                f.remove(0).value
            }
        });

    let (.., prompt) = get_match_need_config().await?;
    let TestRuleDto { bvid, ai_chat_enable, single_match_enable, complex_match_enable } = dto;
    let aid = bvid_to_aid(&bvid);
    let find_or_save_video = video_detail_service::find_or_save_video(aid).await?;
    let (_, m) = rule_service::total_rule_match(&find_or_save_video, ai_chat_enable, single_match_enable, complex_match_enable, &prompt,
                                                &black_single_match, &white_single_match,
                                                &black_complex_rule,
                                                &white_complex_rule,
                                                &black_prompt,
                                                &white_prompt,
    ).await?;
    RR::success(m)
}


