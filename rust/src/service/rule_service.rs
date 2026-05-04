use std::cmp::max;
use std::collections::{HashMap, HashSet};
use std::hash::Hash;
use std::result;

use aho_corasick::{AhoCorasick, BuildError};
use log::{error, info};
use rbatis::table_field_vec;
use rbs::value;

use crate::app::config::CC;
use crate::app::constans::DEFAULT_PROMPT;
use crate::domain::associate_rule::AssociateRule;
use crate::domain::dict::Dict;
use crate::domain::dtos::{AssociateRuleAc, SingleMatchRuleAc};
use crate::domain::enumeration::{AccessType, DictStatus, DictType};
use crate::domain::tag;
use crate::domain::video_detail::{AiMatch, BatchAiMatchItem, BatchAiMatchResponse, ComplexMatch, MatchResult, SingleMatch};
use crate::service::{associate_rule_service, video_detail_service};
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
pub async fn build_complex_rule_list(access_type: AccessType) -> R<Vec<AssociateRuleAc>> {
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
            name: ele.info,
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
pub async fn build_single_match_rule_ac(access_type: AccessType) -> R<SingleMatchRuleAc> {
    info!("build_single_match_rule_ac , {:?}", access_type);
    let dict_enum = vec![
        DictType::TAG,
        DictType::DESC,
        DictType::TITLE,
        DictType::COVER,
        DictType::MID,
        DictType::TID,
    ];
    let dicts = Dict::select_by_map(
        &CC.rb,
        value! {
            "access_type": access_type,
            "status": DictStatus::NORMAL,
            "dict_type":&dict_enum
        },
    )
    .await?;

    let mut group_by_full = dicts.group_by_full(|f| f.dict_type);

    let mut title_arr: AhoCorasick = build_ac(vec![]);
    let mut desc_arr: AhoCorasick = build_ac(vec![]);
    let mut tag_arr: AhoCorasick = build_ac(vec![]);
    let mut cover_arr: AhoCorasick = build_ac(vec![]);
    let mut tid_arr: HashSet<u64> = HashSet::new();
    let mut mid_arr: HashSet<u64> = HashSet::new();

    for dict_type in dict_enum {
        if let Some(dict_arr) = group_by_full.remove(&dict_type) {
            let patterns: Vec<String> = dict_arr.into_iter().map(|f| f.value).collect();
            match dict_type {
                DictType::TAG => tag_arr = build_ac(patterns),
                DictType::DESC => desc_arr = build_ac(patterns),
                DictType::TITLE => {
                    info!(
                        "accesstype = {:?} ,title build ac  arr = {:?}",
                        access_type, patterns
                    );
                    title_arr = build_ac(patterns)
                }
                DictType::COVER => cover_arr = build_ac(patterns),
                DictType::MID => {
                    mid_arr = patterns
                        .into_iter()
                        .filter_map(|f| match f.parse::<u64>() {
                            Ok(a) => Some(a),
                            Err(e) => {
                                error!("tid 转换数字失败:{:#?}", e);
                                None
                            }
                        })
                        .collect();
                }
                DictType::TID => {
                    tid_arr = patterns
                        .into_iter()
                        .filter_map(|f| match f.parse::<u64>() {
                            Ok(a) => Some(a),
                            Err(e) => {
                                error!("mid 转换数字失败:{:#?}", e);
                                None // 使用 None 过滤掉错误的值
                            }
                        })
                        .collect();
                }
                _ => {}
            }
        }
    }

    R::Ok(SingleMatchRuleAc {
        title: title_arr,
        desc: desc_arr,
        tag: tag_arr,
        cover: cover_arr,
        tid: tid_arr,
        mid: mid_arr,
    })
}

/// 构建ac
pub fn build_ac(patterns: Vec<String>) -> AhoCorasick {
    if patterns.is_empty() {
        return AhoCorasick::new([] as [&str; 0]).unwrap();
    }
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
pub fn get_ac_match_result(ac: &AhoCorasick, haystack: &str) -> Vec<String> {
    // 获取所有匹配的关键词
    let matched_keywords: Vec<String> = ac
        .find_iter(haystack)
        .map(|mat| &haystack[mat.start()..mat.end()])
        .map(|f| f.to_string())
        .collect::<HashSet<_>>()
        .into_iter()
        .collect();

    matched_keywords
}

/// 将视频信息，以及黑白名单规则发送给ai，然后让ai判断这属于什么类型的视频，并给出简短的解释
pub async fn get_ai_match_result(
    video: &VideoDetail,
    white_prompt: &str,
    black_prompt: &str,
    prefix_prompt: &str,
) -> R<AiMatch> {
    info!("开始进行ai匹配 ：{:?}", video.title);
    let mut output_prompt = r#"**输出格式**  
        将内容输出为一个json对象，直接输出完整纯json字符串，不要有markdown相关内容，不要有```json等格式。
        包含两个字段，
        match_type字段: 匹配结果，黑名单为BLACK，白名单为：WHITE，其他为：OTHER。
        reason字段：20个字，简要描述一下给出该判断的原因。 "#;

    let system_prompt = format!("{}{}", prefix_prompt, output_prompt);
    let video_info = format!(
        r#"
        视频信息如下：
        标题：{};
        描述: {};
        封面文字:{};
        标签：{};
        分区:{};
    "#,
        video.title.clone().unwrap_or(String::from("")),
        video.desc_field.clone().unwrap_or(String::from("")),
        "", // 暂时没有完成封面ocr或者摘要模型的配置，封面文字留空
        video.tag.clone().unwrap_or(String::from("")),
        video.tname.clone().unwrap_or(String::from("")),
    );

    info!("{}", video_info);
    let rule_info = format!(
        r#"黑名单规则如下：{};
        白名单规则如下：{};"#,
        black_prompt, white_prompt
    );

    let user_prompt = format!("{}，{}", video_info, rule_info);

    let glm = &CC.chat.read().await;

    let message = vec![
        Message {
            role: MessageRole::System,
            content: system_prompt,
        },
        Message {
            role: MessageRole::User,
            content: user_prompt,
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

/// 批量AI匹配：将多个视频信息一次性发送给AI，返回每个视频的匹配结果
/// 对于AI返回中缺失的视频id，不会出现在结果HashMap中（调用方应跳过这些视频）
pub async fn get_batch_ai_match_result(
    videos: &[VideoDetail],
    white_prompt: &str,
    black_prompt: &str,
    prefix_prompt: &str,
) -> R<HashMap<u64, AiMatch>> {
    if videos.is_empty() {
        return R::Ok(HashMap::new());
    }

    info!("开始批量AI匹配，共{}个视频", videos.len());

    let output_prompt = r#"**输出格式**  
直接输出完整纯json字符串，不要有markdown相关内容，不要有```json等格式。
输出一个json对象，包含一个"results"字段，值为数组，数组长度必须和输入的视频数量一致，每个元素包含：
- "id"：视频的id（数字）
- "match_type"：匹配结果，黑名单为"BLACK"，白名单为"WHITE"，其他为"OTHER"
- "reason"：20个字以内，简要描述给出该判断的原因

**输出示例**：
{"results":[{"id":123456,"match_type":"BLACK","reason":"包含游戏内容"},{"id":789012,"match_type":"OTHER","reason":"数码评测"}]}"#;

    let system_prompt = format!("{}{}", prefix_prompt, output_prompt);

    // 构建视频信息列表
    let mut video_info_list = String::new();
    for v in videos {
        let info = format!(
            r#"
[视频] (id: {})
标题：{};
描述：{};
封面文字：{};
标签：{};
分区：{}；"#,
            v.id,
            v.title.clone().unwrap_or_default(),
            v.desc_field.clone().unwrap_or_default(),
            "", // 暂时没有完成封面ocr
            v.tag.clone().unwrap_or_default(),
            v.tname.clone().unwrap_or_default(),
        );
        video_info_list.push_str(&info);
    }

    let rule_info = format!(
        r#"
黑名单规则如下：{}；
白名单规则如下：{}；"#,
        black_prompt, white_prompt
    );

    let user_prompt = format!(
        "以下是多个视频的信息，请逐一判断每个视频属于黑名单、白名单还是其他：\n{}{}",
        video_info_list, rule_info
    );

    let glm = &CC.chat.read().await;

    let message = vec![
        Message {
            role: MessageRole::System,
            content: system_prompt,
        },
        Message {
            role: MessageRole::User,
            content: user_prompt,
        },
    ];

    let result_map = match glm.chat_request(message).await {
        Ok(json) => {
            // AI返回格式可能不一致：有时返回 {"results":[...]}, 有时返回 [...]
            let results: Vec<BatchAiMatchItem> = match serde_json::from_str::<Vec<BatchAiMatchItem>>(&json) {
                Ok(items) => items,
                Err(_) => match serde_json::from_str::<BatchAiMatchResponse>(&json) {
                    Ok(resp) => resp.results,
                    Err(e) => {
                        error!("批量AI匹配回答解析失败！原因：{:#?}, {}", e, json);
                        return R::Ok(HashMap::new());
                    }
                }
            };
            let mut map = HashMap::new();
            for item in results {
                map.insert(item.id, AiMatch {
                    match_type: item.match_type,
                    reason: item.reason,
                });
            }
            // 检查是否有缺失的视频id
            for v in videos {
                if !map.contains_key(&v.id) {
                    info!("批量AI匹配中视频id={}未被AI返回，将跳过", v.id);
                }
            }
            map
        }
        Err(e) => {
            error!("批量AI匹配调用失败！原因：{:#?}", e);
            HashMap::new()
        }
    };

    R::Ok(result_map)
}

/// 单一包含匹配
pub fn get_single_match_result(
    v: &VideoDetail,
    black_single_match: &SingleMatchRuleAc,
    white_single_match: &SingleMatchRuleAc,
) -> R<SingleMatch> {
    let black_result = try_single_match(v, black_single_match, AccessType::BLACK)?;
    if black_result.match_count > 0 {
        //直接返回黑名单匹配
        return R::Ok(black_result);
    }
    let mut white_result = try_single_match(v, white_single_match, AccessType::WHITE)?;
    if white_result.match_count > 0 {
        return R::Ok(white_result);
    }
    R::Ok(SingleMatch::default())
}

/// 传入视频和规则，返回匹配结果
pub fn try_single_match(
    v: &VideoDetail,
    rule: &SingleMatchRuleAc,
    access_type: AccessType,
) -> R<SingleMatch> {
    let mut match_count: u32 = 0;
    let tag: Vec<String> = match &v.tag {
        Some(tag_str) => {
            info!("tag single match {:?}", access_type);
            let ac_match = get_ac_match_result(&rule.tag, &tag_str);
            match_count = match_count + ac_match.len() as u32;
            ac_match
        }
        None => vec![],
    };

    let desc: Vec<String> = match &v.desc_field {
        Some(desc_str) => {
            info!("desc single match  {:?}", access_type);
            let ac_match = get_ac_match_result(&rule.desc, &desc_str);
            match_count = match_count + ac_match.len() as u32;
            ac_match
        }
        None => vec![],
    };

    let title: Vec<String> = match &v.title {
        Some(title_str) => {
            info!("title single match  {:?}", access_type);
            let ac_match = get_ac_match_result(&rule.title, &title_str);
            match_count = match_count + ac_match.len() as u32;
            ac_match
        }
        None => vec![],
    };

    // todo 封面数据还未存储进video_detail中
    let cover: Vec<String> = vec![];
    // let cover: Vec<String> = match &v.cover {
    //     Some(cover_str) => {
    //         let ac_match = ac_match(&black_single_match.cover, &cover_str);
    //         match_count = match_count + ac_match.len()as u32;
    //         ac_match
    //     }
    //     None => vec![],
    // };

    let mid: Vec<u64> = match v.owner_id {
        Some(a) => {
            if rule.mid.contains(&a) {
                match_count = match_count + 1;
                vec![a]
            } else {
                vec![]
            }
        }
        None => vec![],
    };

    let tid: Vec<u64> = match v.tid {
        Some(a) => {
            if rule.tid.contains(&a) {
                match_count = match_count + 1;
                vec![a]
            } else {
                vec![]
            }
        }
        None => vec![],
    };

    R::Ok(SingleMatch {
        match_type: match match_count > 0 {
            true => Some(access_type),
            false => None,
        },
        tag,
        desc,
        title,
        cover,
        mid,
        tid,
        match_count,
    })
}

/// 获得视频判断需要的配置
pub async fn get_match_need_config() -> R<(bool, bool, bool, String)> {
    let lock = CC.config_map.read().await;
    let default_prompt = DEFAULT_PROMPT.to_string();
    let mut result = MatchResult::default();
    let prefix_prompt = lock
        .get("ai_system_prompt")
        .map_or(default_prompt, |f| f.to_owned());

    //0. 读取规则配置，看看现在开启了什么规则
    let ai_chat_enable = lock.get("ai_chat_enable").map_or(false, |f| f == "true");
    let single_match_enable = lock.get("single_match").map_or(false, |f| f == "true");
    let complex_match_enable = lock.get("complex_match").map_or(false, |f| f == "true");

    R::Ok((
        ai_chat_enable,
        single_match_enable,
        complex_match_enable,
        prefix_prompt,
    ))
}

/// 构建匹配所需要的规则
pub async fn build_match_config() -> R<(
    // 单一规则
    SingleMatchRuleAc,
    SingleMatchRuleAc,
    // 复合规则
    Vec<AssociateRuleAc>,
    Vec<AssociateRuleAc>,
    // AI提示词
    String,
    String,
    // 开关配置
    bool,
    bool,
    bool,
    String,
)> {
    // 0.1 单一规则
    let black_single_match: SingleMatchRuleAc =
        build_single_match_rule_ac(AccessType::BLACK).await?;
    let white_single_match: SingleMatchRuleAc =
        build_single_match_rule_ac(AccessType::WHITE).await?;

    // 0.2 复合规则
    let black_complex_rule: Vec<AssociateRuleAc> =
        build_complex_rule_list(AccessType::BLACK).await?;
    let white_complex_rule: Vec<AssociateRuleAc> =
        build_complex_rule_list(AccessType::WHITE).await?;

    // 0.3 AI提示词
    let mut prompt_map = Dict::select_by_map(
        &CC.rb,
        value! {
            "dict_type": DictType::AI_JUDGMENT_PROMPT,
            "status": DictStatus::NORMAL
        },
    )
    .await?
    .group_by_full(|f| f.access_type);

    let black_prompt: String =
        prompt_map
            .remove(&AccessType::BLACK)
            .map_or(String::new(), |mut f| {
                if f.is_empty() {
                    String::new()
                } else {
                    f.remove(0).value
                }
            });

    let white_prompt: String =
        prompt_map
            .remove(&AccessType::WHITE)
            .map_or(String::new(), |mut f| {
                if f.is_empty() {
                    String::new()
                } else {
                    f.remove(0).value
                }
            });

    // 0.4 开关配置
    let (ai_chat_enable, single_match_enable, complex_match_enable, prompt) =
        get_match_need_config().await?;

    Ok((
        black_single_match,
        white_single_match,
        black_complex_rule,
        white_complex_rule,
        black_prompt,
        white_prompt,
        ai_chat_enable,
        single_match_enable,
        complex_match_enable,
        prompt,
    ))
}

/// 规则匹配, 返回这是黑名单还是白名单，还是其他，然后把匹配结果也返回
/// 传什么东西过来？规则需要什么，就传什么
/// 按照配置来决定要判断什么
pub async fn total_rule_match(
    v: &VideoDetail,
    ai_chat_enable: bool,
    single_match_enable: bool,
    complex_match_enable: bool,
    prefix_prompt: &String,
    //单一规则
    black_single_match: &SingleMatchRuleAc,
    white_single_match: &SingleMatchRuleAc,

    //复合规则
    black_complex_rule: &Vec<AssociateRuleAc>,
    white_complex_rule: &Vec<AssociateRuleAc>,

    //ai 规则
    black_prompt: &String,
    white_prompt: &String,
) -> R<(AccessType, MatchResult)> {
    let mut result = MatchResult::default();

    // // 0.1 单一规则
    // let black_single_match: SingleMatchRuleAc =
    //     build_single_match_rule_ac(AccessType::BLACK).await?;
    // let white_single_match: SingleMatchRuleAc =
    //     build_single_match_rule_ac(AccessType::WHITE).await?;
    //
    // // 0.2 复合规则
    // let black_complex_rule:Vec<AssociateRuleAc> = build_complex_rule_list(AccessType::BLACK).await?;
    // let white_complex_rule:Vec<AssociateRuleAc> = build_complex_rule_list(AccessType::WHITE).await?;
    //
    // // 0.3 ai提示词
    // let mut prompt_map = Dict::select_by_map(
    //     &CC.rb,
    //     value! {
    //         "dict_type":DictType::AI_JUDGMENT_PROMPT,
    //         "status":DictStatus::NORMAL
    //     },
    // )
    // .await?
    // .group_by_full(|f| f.access_type);
    //
    // let black_prompt:String = prompt_map
    //     .remove(&AccessType::BLACK)
    //     .map_or(String::from(""), |mut f| {
    //         if f.is_empty() {
    //             String::from("")
    //         } else {
    //             f.remove(0).value
    //         }
    //     });
    // let white_prompt:String  = prompt_map
    //     .remove(&AccessType::WHITE)
    //     .map_or(String::from(""), |mut f| {
    //         if f.is_empty() {
    //             String::from("")
    //         } else {
    //             f.remove(0).value
    //         }
    //     });

    //1. 单一包含匹配
    if single_match_enable {
        let single_result = get_single_match_result(v, &black_single_match, &white_single_match)?;
        let access_type = single_result.match_type.clone();
        result.single_match = Some(single_result);
        if let Some(a) = access_type {
            return R::Ok((a, result));
        }
    }

    //2. 复杂规则匹配
    if complex_match_enable {
        let complex_result = get_complex_match_result(v, &black_complex_rule, &white_complex_rule)?;
        let access_type = complex_result.match_type.clone();
        result.complex_match = Some(complex_result);
        if let Some(a) = access_type {
            return R::Ok((a, result));
        }
    }

    //3. ai调用匹配
    if ai_chat_enable {
        let ai_match = get_ai_match_result(v, &white_prompt, &black_prompt, prefix_prompt).await?;

        let access_type = ai_match.match_type.clone();
        result.ai_match = Some(ai_match);
        return R::Ok((access_type, result));
    }

    R::Ok((AccessType::OTHER, result))
}

/// 复杂规则匹配
pub fn get_complex_match_result(
    v: &VideoDetail,
    black_complex_rule: &[AssociateRuleAc],
    white_complex_rule: &[AssociateRuleAc],
) -> R<ComplexMatch> {
    let black_result = try_complex_match(v, black_complex_rule, AccessType::BLACK)?;
    if black_result.match_count >= 3 {
        //直接返回黑名单匹配
        return R::Ok(black_result);
    }
    let mut white_result = try_complex_match(v, white_complex_rule, AccessType::WHITE)?;
    if white_result.match_count >= 3 {
        return R::Ok(white_result);
    }

    if black_result.match_count > white_result.match_count {
        return R::Ok(black_result);
    } else {
        return R::Ok(white_result);
    }

    // R::Ok(ComplexMatch::default())
}

/// 输入视频和规则，返回判断结果
fn try_complex_match(
    v: &VideoDetail,
    rule_list: &[AssociateRuleAc],
    access_type: AccessType,
) -> R<ComplexMatch> {
    info!("{:?} complex_match ,len={}", access_type, rule_list.len());

    let mut max_match: Option<ComplexMatch> = None;
    for f in rule_list {
        info!("规则={} 正在匹配", f.name);
        let mut count: u32 = 0;

        let title_arr = match &v.title {
            Some(title) => {
                let ac_match = get_ac_match_result(&f.title, title);
                count = count + ac_match.len() as u32;
                ac_match
            }
            None => vec![],
        };

        let desc_arr = match &v.desc_field {
            Some(desc) => {
                let ac_match = get_ac_match_result(&f.desc, desc);
                count = count + ac_match.len() as u32;
                ac_match
            }
            None => vec![],
        };

        let tag_arr = match &v.tag {
            Some(tag) => {
                let ac_match = get_ac_match_result(&f.tag, tag);
                count = count + ac_match.len() as u32;
                ac_match
            }
            None => vec![],
        };

        // todo 暂时没有实现封面数据的存储和获取
        let cover_arr: Vec<String> = vec![];
        // let cover_arr = match &v.cover {
        //     Some(cover) => {
        //         let ac_match = ac_match(&f.cover, cover);
        //         count = count + ac_match.len()as u32;
        //         ac_match
        //     }
        //     None => vec![],
        // };

        let tid_arr = match v.tid {
            Some(u) => {
                if f.tid.contains(&u) {
                    count = count + 1;
                    vec![u]
                } else {
                    vec![]
                }
            }
            None => vec![],
        };

        let mid_arr = match v.owner_id {
            Some(u) => {
                if f.mid.contains(&u) {
                    count = count + 1;
                    vec![u]
                } else {
                    vec![]
                }
            }
            None => vec![],
        };

        if count >= 3 {
            info!("{} 匹配成功，匹配count={}", f.name, count);
            return R::Ok(ComplexMatch {
                match_type: Some(access_type),
                rule_name: Some(f.name.clone()),
                tag: tag_arr,
                desc: desc_arr,
                title: title_arr,
                cover: cover_arr,
                mid: mid_arr,
                tid: tid_arr,
                match_count: count,
            });
        } else {
            info!("{} 没有匹配成功，匹配count={}", f.name, count);

            if max_match.as_ref().map_or(true, |f| f.match_count < count) {
                max_match = Some(ComplexMatch {
                    match_type: None,
                    rule_name: Some(f.name.clone()),
                    tag: tag_arr,
                    desc: desc_arr,
                    title: title_arr,
                    cover: cover_arr,
                    mid: mid_arr,
                    tid: tid_arr,
                    match_count: count,
                });
            }
        }
    }

    R::Ok(max_match.unwrap_or(ComplexMatch::default()))
}

#[cfg(test)]
mod tests {
    use aho_corasick::AhoCorasick;
    use jieba_rs::Jieba;
    use rbs::value;

    use crate::domain::dtos::TestRuleDto;
    use crate::service::rule_service::{get_match_need_config, total_rule_match};
    use crate::service::video_detail_service;
    use crate::utils::data_util::bvid_to_aid;
    use crate::{
        app::config::CC,
        domain::{
            dict::Dict,
            enumeration::{AccessType, DictType},
        },
        service::rule_service::{self, build_ac, build_single_match_rule_ac},
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
    async fn test_all_match() {
        //第一句必须是这个
        crate::init().await;

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_ac_match2() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        pub fn get_ac_match_result(ac: &AhoCorasick, haystack: &str) -> Vec<String> {
            // 获取所有匹配的关键词
            let matched_keywords: Vec<String> = ac
                .find_iter(haystack)
                .map(|mat| &haystack[mat.start()..mat.end()])
                .map(|f| f.to_string())
                .collect();

            matched_keywords
        }

        let select_by_map: Vec<String> = Dict::select_by_map(
            &CC.rb,
            value! {
                "access_type":AccessType::BLACK,
                "dict_type":DictType::TITLE

            },
        )
        .await
        .unwrap()
        .into_iter()
        .map(|f| f.value)
        .collect();

        let ac = build_ac(select_by_map);
        let haystack = "邻居至今都不知道我家到底几只猫？";
        let get_ac_match_result = get_ac_match_result(&ac, haystack);
        println!("{:#?}", get_ac_match_result);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[test]
    fn test_ac_match() {
        println!("=== Aho-Corasick 中文测试 ===\n");

        // 测试文本
        let text = "邻居至今都不知道我家到底几只猫？";
        println!("测试文本: {}", text);
        println!("文本长度: {} 字节", text.len());
        println!();

        // 测试1: 基本中文关键词匹配
        println!("--- 测试1: 基本中文关键词 ---");
        let keywords = vec!["邻居", "知道", "猫"];
        println!("关键词: {:?}", keywords);
        let ac = AhoCorasick::new(keywords).unwrap();

        println!("匹配结果:");

        for mat in ac.find_iter(text) {
            let matched = &text[mat.start()..mat.end()];
            println!(
                "  - 位置: {}..{}, 内容: '{}'",
                mat.start(),
                mat.end(),
                matched
            );
        }
        println!();

        // 测试2: 重叠关键词
        println!("--- 测试2: 重叠关键词测试 ---");
        let keywords = vec!["苹果", "苹果手机", "手机"];
        println!("关键词: {:?}", keywords);
        let ac = AhoCorasick::new(keywords).unwrap();
        let text2 = "我买了一部苹果手机";

        println!("文本: {}", text2);
        println!("匹配结果:");

        for mat in ac.find_iter(text2) {
            let matched = &text2[mat.start()..mat.end()];
            println!(
                "  - 位置: {}..{}, 内容: '{}'",
                mat.start(),
                mat.end(),
                matched
            );
        }
        println!();

        // 测试3: 完整的关键词集（你原来的场景）
        println!("--- 测试3: 完整场景测试 ---");
        let all_keywords = vec![
            "邻居",
            "至今",
            "都不知道",
            "知道",
            "我家",
            "到底",
            "几只",
            "猫",
        ];
        let ac = AhoCorasick::new(all_keywords.clone()).unwrap();

        println!("关键词数量: {}", all_keywords.len());
        println!("文本: {}", text);
        println!("所有匹配:");

        let matches: Vec<_> = ac.find_iter(text).collect();
        for mat in &matches {
            let matched = &text[mat.start()..mat.end()];
            println!(
                "  - 位置: {:3}..{:3}, 内容: '{}'",
                mat.start(),
                mat.end(),
                matched
            );
        }

        println!("\n总共匹配到 {} 个关键词", matches.len());
        println!();

        // 测试4: 去重后的结果
        println!("--- 测试4: 去重后的关键词 ---");
        use std::collections::HashSet;

        let unique_matches: HashSet<String> = ac
            .find_iter(text)
            .map(|mat| text[mat.start()..mat.end()].to_string())
            .collect();

        println!("唯一关键词: {:?}", unique_matches);
        println!();

        // 测试5: 验证字节边界问题
        println!("--- 测试5: 字节边界验证 ---");
        println!("文本的字符和字节位置:");
        for (i, (byte_pos, ch)) in text.char_indices().enumerate() {
            println!("  字符[{}]: '{}' 位于字节索引 {}", i, ch, byte_pos);
        }

        println!("\n验证匹配的边界是否在字符边界上:");
        for mat in ac.find_iter(text) {
            let matched = &text[mat.start()..mat.end()];
            let is_char_boundary =
                text.is_char_boundary(mat.start()) && text.is_char_boundary(mat.end());
            println!(
                "  '{}' ({}..{}) 边界正确: {}",
                matched,
                mat.start(),
                mat.end(),
                is_char_boundary
            );
        }
    }

    #[tokio::test]
    async fn test_build_ac_map() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let build_ac_map = build_single_match_rule_ac(AccessType::BLACK).await.unwrap();

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
