use std::collections::{HashMap, HashSet};
use std::hash::Hash;

use aho_corasick::{AhoCorasick, BuildError};
use log::{error, info};
use rbatis::table_field_vec;
use rbs::value;

use crate::app::config::CC;
use crate::domain::associate_rule::AssociateRule;
use crate::domain::dict::Dict;
use crate::domain::dtos::AssociateRuleAc;
use crate::domain::enumeration::{AccessType, DictStatus, DictType};
use crate::domain::video_detail::{AiMatch, MatchResult};
use crate::service::associate_rule_service;
use crate::utils::collection_tool::VecGroupByExt;
use crate::utils::glm_chat::{Message, MessageRole};
use crate::utils::segmenter_util;
use crate::{
    app::response::R,
    domain::{dtos::VideoDetailDTO, video_detail::VideoDetail},
    service::dict_service,
};

/// 根据视频列表训练黑名单
/// 应该改为通用的训练，如果后续还需要训练的话
pub(crate) async fn train_blacklist_by_video_list(video_detail_list: Vec<VideoDetailDTO>) -> R<()> {
    let mut title_process = vec![];
    let mut desc_processs = vec![];
    let mut tag_name_process = vec![];
    //生成需要被审核的关键词
    for v in video_detail_list.iter() {
        let detail = &v.video_detail;
        //1.标题处理
        if let Some(title) = &detail.title {
            title_process.extend(segmenter_util::process(title))
        }

        //2.描述处理
        if let Some(desc) = &detail.desc_field {
            desc_processs.extend(segmenter_util::process(desc));
            if let Some(desc_v2) = &v.desc_v2 {
                for vv in desc_v2 {
                    if let Some(rw) = &vv.raw_text {
                        desc_processs.extend(segmenter_util::process(rw));
                    }
                }
            }
        }

        //3.标签处理
        if let Some(tags) = &v.tags {
            for t in tags {
                tag_name_process.extend(segmenter_util::process(&t.tag_name));
            }
        }
    }

    let stop_word_list: Vec<String> = dict_service::get_stop_word_list().await?;

    let top_title_word =
        segmenter_util::get_top_frequent_word_from_list_auto_limit(&title_process, &stop_word_list);
    let top_desc_word =
        segmenter_util::get_top_frequent_word_from_list_auto_limit(&desc_processs, &stop_word_list);
    let top_tag_name_word = segmenter_util::get_top_frequent_word_from_list_auto_limit(
        &tag_name_process,
        &stop_word_list,
    );

    //过滤Bv的
    let mut top_title_word = filter_start_with_bv(top_title_word)?;
    let mut top_desc_word = filter_start_with_bv(top_desc_word)?;
    let mut top_tag_name_word = filter_start_with_bv(top_tag_name_word)?;

    info!(
        "本次训练结果: desc关键词: {:?},标签:{:?},标题关键词:{:?}",
        top_desc_word, top_tag_name_word, top_title_word
    );

    //获取需要忽略的黑名单关键词
    let ignore_keyword = dict_service::find_ignore_value_by_dict_type_and_access_type(
        DictType::KEYWORD,
        AccessType::BLACK,
    )
    .await?;
    let ignore_tag_keyword = dict_service::find_ignore_value_by_dict_type_and_access_type(
        DictType::TAG,
        AccessType::BLACK,
    )
    .await?;

    //已有的关键词
    let exist_keyword = dict_service::find_normal_value_by_dict_type_and_access_type(
        DictType::KEYWORD,
        AccessType::BLACK,
    )
    .await?;
    let exist_tag_keyword = dict_service::find_normal_value_by_dict_type_and_access_type(
        DictType::TAG,
        AccessType::BLACK,
    )
    .await?;

    //去重
    top_title_word.retain(|s| !(ignore_keyword.contains(s) || exist_keyword.contains(s)));

    if !top_title_word.is_empty() {
        dict_service::batch_add_dict_from_value(
            top_title_word,
            DictType::KEYWORD,
            AccessType::BLACK,
            DictStatus::CACHE,
        )
        .await?;
    }

    //去重
    top_desc_word.retain(|s| !(ignore_keyword.contains(s) || exist_keyword.contains(s)));

    if !top_desc_word.is_empty() {
        dict_service::batch_add_dict_from_value(
            top_desc_word,
            DictType::KEYWORD,
            AccessType::BLACK,
            DictStatus::CACHE,
        )
        .await?;
    }

    //去重
    top_tag_name_word
        .retain(|s| !(ignore_tag_keyword.contains(s) || exist_tag_keyword.contains(s)));

    if !top_tag_name_word.is_empty() {
        dict_service::batch_add_dict_from_value(
            top_tag_name_word,
            DictType::TAG,
            AccessType::BLACK,
            DictStatus::CACHE,
        )
        .await?;
    }

    R::Ok(())
}

/// 过滤掉以bv开头的关键词
pub fn filter_start_with_bv(list: Vec<String>) -> R<Vec<String>> {
    R::Ok(
        list.into_iter()
            .filter(|x| !(x.starts_with("bv") || x.starts_with("BV") || x.starts_with("Bv")))
            .collect::<Vec<String>>(),
    )
}

/// 根据access_type获得复杂规则列表
pub async fn get_associate_rule_list(access_type: AccessType) -> R<Vec<AssociateRuleAc>> {
    //规则不可能超过999条
    let associate_rules = associate_rule_service::get_associate_rule_list(access_type, 1, 999)
        .await?
        .records;

    let mut ac_associate_rule: Vec<AssociateRuleAc> = vec![];
    //每一条规则对应多个ac
    for ele in associate_rules {
        let title_arr: Vec<String> = ele.title.into_iter().map(|f| f.value).collect();
        let desc_arr: Vec<String> = ele.desc.into_iter().map(|f| f.value).collect();
        let tag_arr: Vec<String> = ele.tag.into_iter().map(|f| f.value).collect();
        let cover_arr: Vec<String> = ele.cover.into_iter().map(|f| f.value).collect();
        let tid_arr: HashSet<u64> = ele
            .tid
            .into_iter()
            .map(|f| match f.value.parse::<u64>() {
                Ok(a) => a,
                Err(e) => {
                    error!("tid 转换数字失败:{:#?}", e);
                    0
                }
            })
            .collect();
        let mid_arr: HashSet<u64> = ele
            .mid
            .into_iter()
            .map(|f| match f.value.parse::<u64>() {
                Ok(a) => a,
                Err(e) => {
                    error!("mid 转换数字失败:{:#?}", e);
                    0
                }
            })
            .collect();

        let associate_rule_ac = AssociateRuleAc {
            id: ele.id,
            title: build_ac(title_arr),
            desc: build_ac(desc_arr),
            tag: build_ac(tag_arr),
            cover: build_ac(cover_arr),
            tid: tid_arr,
            mid: mid_arr,
        };

        ac_associate_rule.push(associate_rule_ac);
    }

    R::Ok(ac_associate_rule)
}

/// 根据黑白类型，构建acmap ，数据直接在函数内部查询得到
pub async fn build_single_matchac_map(
    access_type: AccessType,
) -> R<HashMap<DictType, AhoCorasick>> {
    let dicts = Dict::select_by_map(
        &CC.rb,
        value! {
            "access_type": access_type,
            "status": DictStatus::NORMAL
        },
    )
    .await?;

    let mut group_by_full = dicts.group_by_full(|f| f.dict_type);
    let mut ac_map: HashMap<DictType, AhoCorasick> = HashMap::new();
    let dict_enum = vec![
        DictType::TAG,
        DictType::DESC,
        DictType::TITLE,
        DictType::COVER,
        DictType::MID,
        DictType::TID,
    ];

    for dict_type in dict_enum {
        if let Some(dict_arr) = group_by_full.remove(&dict_type) {
            let patterns: Vec<String> = dict_arr.into_iter().map(|f| f.value).collect();
            match AhoCorasick::builder()
                .ascii_case_insensitive(true)
                .build(patterns)
            {
                Ok(ac) => {
                    ac_map.insert(dict_type, ac);
                }
                Err(err) => {
                    error!(
                        "access_type={:?},dict_type={:?}ac构建失败！{:#?}",
                        access_type, dict_type, err
                    );
                }
            }
        }
    }

    R::Ok(ac_map)
}

/// 构建ac
pub fn build_ac(patterns: Vec<String>) -> AhoCorasick {
    match AhoCorasick::builder()
        .ascii_case_insensitive(true)
        .build(patterns)
    {
        Ok(ac) => ac,
        Err(err) => {
            error!("ac构建失败！ {:#?}", err);
            AhoCorasick::new(vec![""]).unwrap()
        }
    }
}

/// ac匹配，获得匹配的关键词
pub fn ac_match(ac: AhoCorasick, haystack: &str) -> R<Vec<String>> {
    // 获取所有匹配的关键词
    let matched_keywords: Vec<String> = ac
        .find_iter(haystack)
        .map(|mat| &haystack[mat.start()..mat.end()])
        .map(|f| f.to_string())
        .collect();

    R::Ok(matched_keywords)
}

/// 将视频信息，以及黑白名单规则发送给ai，然后让ai判断这属于什么类型的视频，并给出简短的解释
pub async fn ai_match(
    video: &VideoDetail,
    white_prompt: &str,
    black_prompt: &str,
    prefix_prompt: &str,
) -> R<AiMatch> {
    let mut output_prompt = r#"**输出格式**  
        将内容输出为一个json对象，包含两个字段，
        match_type字段: 匹配结果，黑名单为BLACK，白名单为：WHITE，其他为：OTHER。
        reason字段：20个字，简要描述一下得出这个结果的原因"#;

    let system_prompt = format!("{}{}", prefix_prompt, output_prompt);
    let video_info = format!(
        r#"
        视频信息如下：
        标题：{};
        描述: {};
        封面文字:{};
        标签：{};
        分区:{}
    "#,
        video.title.clone().unwrap_or(String::from("")),
        video.desc_field.clone().unwrap_or(String::from("")),
        "", // 暂时没有完成封面ocr或者摘要模型的配置，封面文字留空
        video.tag.clone().unwrap_or(String::from("")),
        video.tname.clone().unwrap_or(String::from(""))
    );

    let glm = &CC.chat.read().await;

    let message = vec![
        Message {
            role: MessageRole::System,
            content: system_prompt,
        },
        Message {
            role: MessageRole::User,
            content: video_info,
        },
    ];
    let result = match glm.chat_request(message).await {
        Ok(json) => match serde_json::from_str(&json) {
            Ok(r) => r,
            Err(e) => {
                error!("ai回答解析失败！原因：{:#?}, {}", e, json);
                AiMatch {
                    match_type: AccessType::OTHER,
                    reason: format!("ai回答解析失败！ {}", json),
                }
            }
        },
        Err(e) => {
            error!("ai判断调用失败！原因：{:#?}", e);
            AiMatch {
                match_type: AccessType::OTHER,
                reason: String::from("ai调用失败"),
            }
        }
    };

    R::Ok(result)
}

/// 规则匹配, 返回这是黑名单还是白名单，还是其他，然后把匹配结果也返回
/// 传什么东西过来？规则需要什么，就传什么
/// 按照配置来决定要判断什么
pub async fn rule_match(v: &VideoDetail) -> R<(AccessType, MatchResult)> {
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

    //0. 读取规则配置，看看现在开启了什么规则
    let ai_chat_enable = lock.get("ai_chat_enable").map_or(false, |f| f == "true");
    let single_match = lock.get("single_match").map_or(false, |f| f == "true");
    let complex_match = lock.get("complex_match").map_or(false, |f| f == "true");

    //1. 单一包含匹配
    if single_match {
        // if let Some(title) = v.title {}
    }

    //2. 复杂规则匹配
    if complex_match {}

    //3. ai调用匹配
    if ai_chat_enable {

        // let chat_glm = &CC.chat.read().await;
        // chat_glm.chat(question)
    }

    todo!();
}

#[cfg(test)]
mod tests {
    use jieba_rs::Jieba;

    use crate::{
        domain::enumeration::AccessType,
        service::rule_service::{self, build_single_matchac_map},
    };

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_build_ac_map() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let build_ac_map = build_single_matchac_map(AccessType::BLACK).await.unwrap();

        println!("{:#?}", build_ac_map.keys());

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_jieba() {
        //第一句必须是这个
        crate::init().await;

        let jieba = Jieba::new();
        let words = jieba.cut("我们中出了一个叛徒", false);

        println!("{:?}", words);

        //最后一句必须是这个
        log::logger().flush();
    }

    //测试filter_start_with_bv
    #[tokio::test]
    async fn test_filter_start_with_bv() {
        //第一句必须是这个
        crate::init().await;

        let list = vec![
            "bv123".to_string(),
            "BV123".to_string(),
            "Bv123".to_string(),
            "123".to_string(),
            "wqe744".to_string(),
        ];

        let result = rule_service::filter_start_with_bv(list);

        println!("{:?}", result);
        //最后一句必须是这个
        log::logger().flush();
    }
}
