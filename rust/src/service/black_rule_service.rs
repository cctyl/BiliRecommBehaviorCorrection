use log::info;

use crate::entity::enumeration::{AccessType, DictType};
use crate::utils::segmenter_util;
use crate::{
    app::response::R,
    entity::{dtos::VideoDetailTagDTO, models::VideoDetail},
    service::dict_service,
};

/// 根据视频列表训练黑名单
pub(crate) async fn train_blacklist_by_video_list(
    video_detail_list: Vec<VideoDetailTagDTO>,
) -> R<()> {
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

    let  top_title_word =
        segmenter_util::get_top_frequent_word_from_list_auto_limit(&title_process, &stop_word_list);
    let  top_desc_word =
        segmenter_util::get_top_frequent_word_from_list_auto_limit(&desc_processs, &stop_word_list);
    let  top_tag_name_word = segmenter_util::get_top_frequent_word_from_list_auto_limit(
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
    let ignore_keyword = dict_service::find_value_by_dict_type_and_access_type(DictType::IGNORE_KEYWORD, AccessType::BLACK).await?;
    let ignore_tag_keyword = dict_service::find_value_by_dict_type_and_access_type(DictType::IGNORE_TAG, AccessType::BLACK).await?;

    //已有的关键词
    let exist_keyword = dict_service::find_value_by_dict_type_and_access_type(DictType::KEYWORD, AccessType::BLACK).await?;
    let exist_tag_keyword = dict_service::find_value_by_dict_type_and_access_type(DictType::TAG, AccessType::BLACK).await?;

    top_title_word.retain(|s|
        !(ignore_keyword.contains(s) 
        ||
        exist_keyword.contains(s))
    );


    if !top_title_word.is_empty(){

        dict_service::batch_add_dict_from_value(
            top_title_word,
            DictType::KEYWORD,
            AccessType::BLACK_CACHE
        ).await?;

    }

    top_desc_word.retain(|s|{
        !(ignore_keyword.contains(s) 
        ||
        exist_keyword.contains(s))
    });


    if !top_desc_word.is_empty(){

        dict_service::batch_add_dict_from_value(
            top_desc_word,
            DictType::KEYWORD,
            AccessType::BLACK_CACHE
        ).await?;

    }


    top_tag_name_word.retain(|s|
        !(ignore_tag_keyword.contains(s) 
        ||
        exist_tag_keyword.contains(s))
    );


    if !top_tag_name_word.is_empty(){
        dict_service::batch_add_dict_from_value(
            top_tag_name_word,
            DictType::TAG,
            AccessType::BLACK_CACHE
        ).await?;
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

#[cfg(test)]
mod tests {
    use jieba_rs::Jieba;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

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

        let result = super::filter_start_with_bv(list);

        println!("{:?}", result);
        //最后一句必须是这个
        log::logger().flush();
    }
}
