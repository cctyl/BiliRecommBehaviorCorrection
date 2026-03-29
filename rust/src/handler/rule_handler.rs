use axum::{Router, debug_handler};
use rbs::value;

use crate::{app::{config::CC, response::{OkRespExt, RR}}, domain::{dict::Dict, enumeration::{AccessType, DictStatus, DictType}, video_detail::{AiMatch, VideoDetail}}, extractor::path::MyPath, handler::ai, service::{rule_service, video_detail_service}, utils::{collection_tool::VecGroupByExt, data_util::bvid_to_aid}};

pub fn create_router() -> Router {
    Router::new().route(
        "/testAiRule/{bvid}/{access_type}",
        axum::routing::post(test_ai_rule),
    )
}


/// 测试ai对该视频的判断
#[debug_handler]
pub async fn test_ai_rule(MyPath((bvid,access_type)): MyPath<(String, AccessType)>)->RR<AiMatch>{

    let lock = &CC.config_map.read().await;
    let default_prompt = r#"**角色定义**  
你是一个严格的视频内容审核助手。你的任务是根据用户提供的视频信息（如标题、描述等）以及预设的审核规则（黑名单、白名单），
结合你自身的知识库，判断该视频属于“黑名单”“白名单”还是“其他”。

**审核规则**  
用户将提供以下两类规则：  
1. **黑名单规则**：包含禁止出现的内容、关键词、主题、敏感领域等。一旦视频涉及其中任何一项，应判定为“黑名单”。  
2. **白名单规则**：包含允许或优先通过的内容、关键词、主题等。仅当视频符合白名单规则且未触发任何黑名单规则时，判定为“白名单”。

**判断原则**  
- 严格遵循用户提供的规则，不得自行放宽或添加条件。  
- 若用户规则与你的知识库存在冲突，以用户规则为最优先。  
- 若视频信息不足以明确判定，或同时触发黑白名单规则（需按黑名单优先原则），应判定为“待定/其他”，并简要说明原因。  
- 结合你的知识库对视频内容进行理解，尤其当标题、描述存在隐喻、隐晦表达或行业术语时，需识别其真实含义以判断是否命中规则。"#.to_string();

    let prefix_prompt = lock.get("ai_system_prompt").unwrap_or(&default_prompt);

    let aid = bvid_to_aid(&bvid);
    let find_or_save_video = video_detail_service::find_or_save_video(aid).await?;

    let mut prompt_map = Dict::select_by_map(&CC.rb, value! {
        "dict_type":DictType::AI_JUDGMENT_PROMPT,
        "status":DictStatus::NORMAL
    }).await?
    .group_by_full(|f|f.access_type);

    let black = prompt_map.remove(&AccessType::BLACK).map_or(String::from(""), |mut f|{

        if f.is_empty(){
            String::from("")
        }else {
            f.remove(0).value
        }

    });
    let white = prompt_map.remove(&AccessType::WHITE).map_or(String::from(""), |mut f|{

        if f.is_empty(){
            String::from("")
        }else {
            f.remove(0).value
        }

    });


    let ai_match = rule_service::ai_match(&find_or_save_video, &white, &black, &prefix_prompt).await?;

    RR::success(ai_match)


}