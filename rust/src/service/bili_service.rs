use std::sync::Arc;

use crate::{
    api::bili,
    app::response::R,
    domain::{
        dtos::{UserSubmissionVideo, VideoDetailDTO},
        video_detail::{MatchResult, VideoDetail},
    },
    handler::black_rule_handler,
    service::{rule_service, dict_service},
    utils::{
        data_util::{self},
        thread_util::ThreadUtil,
    },
};

use crate::app::error::HttpError;
use crate::domain::enumeration::HandleType;
use crate::service::video_detail_service;
use data_util::RandomAccessListConsumer;
use log::{error, info};
use serde::de;
use tokio::sync::Mutex;

pub struct VideoHandler;
impl RandomAccessListConsumer for VideoHandler {
    type T = UserSubmissionVideo;
    type Input = ();
    type Output = Vec<VideoDetailDTO>;

    async fn accept(
        v: &Self::T,
        arg: &mut Self::Input,
        video_detail_list: &mut Self::Output,
    ) -> R<()> {
        //获取视频详情并加入
        let detail = bili::get_video_detail(v.aid).await?;
        let aid = detail.video_detail.aid;
        video_detail_list.push(detail);
        ThreadUtil::s2().await;

        //点踩
        bili::dislike(aid).await?;
        ThreadUtil::s20().await;

        R::Ok(())
    }
}

/// 根据用户id进行点踩
///
/// 参数:
/// - `user_id`: 用户ID
/// - `train`: 是否训练模式
///
/// 返回点踩的视频数量
pub async fn disklike_by_user_id(user_id: &str, train: bool) -> R<u32> {
    //该用户会被加入黑名单
    dict_service::add_black_user_id(user_id, None).await?;
    info!("用户已经被加入黑名单：{}", user_id);

    //视频详情
    let mut video_detail_list: Vec<VideoDetailDTO> = Vec::new();

    //获取该用户的所有投稿视频
    let mut all_video = Vec::new();

    let mut page_num = 1;
    loop {
        //searchUserSubmissionVideo
        info!("获取用户投稿视频，页码：{}", page_num);
        let page_bean = bili::search_user_submission_video(user_id, page_num, "").await?;
        let has_more = page_bean.has_more();
        //TO DO
        // let has_more =false;
        all_video.extend(page_bean.data);
        page_num += 1;
        ThreadUtil::s20().await;
        if !has_more {
            break;
        }
    }
    info!("获取用户投稿视频完成，共{}个视频", all_video.len());
    video_detail_list =
        VideoHandler::random_access_list(&all_video, all_video.len(), (), video_detail_list)
            .await?;
    info!("全部点踩完成！");
    let len = video_detail_list.len();
    if train {
        info!("开始进行训练...");
        //保存数据
        rule_service::train_blacklist_by_video_list(video_detail_list).await?;
    }

    R::Ok(len as u32)
}

#[cfg(test)]
mod tests {
    use log::info;
    use crate::api::bili;
    use crate::service::bili_service::disklisk_video_list;
    use crate::utils::thread_util::ThreadUtil;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }
    #[tokio::test]
    async fn test_disklike_by_user_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let res = super::disklike_by_user_id("3493076265863553", true)
            .await
            .unwrap();
        println!("res:{}", res);

        //最后一句必须是这个
        log::logger().flush();
    }


    #[tokio::test]
    async fn test_disklisk_video_list() {
        //第一句必须是这个
        crate::init().await;

        let tid = 129;
        //在这中间编写测试代码
        let mut rank_video_list = bili::get_rank_by_tid(tid).await.unwrap();
        ThreadUtil::s1().await;


        let dto = rank_video_list.pop().unwrap();
        info!("dto={:#?}",dto);
        
        let mut newvec = vec![dto];
        //1.1 分步点踩
        disklisk_video_list(&mut newvec, format!("对{}分区的视频进行点踩", tid)).await.unwrap();
        
        
        //最后一句必须是这个
        log::logger().flush();
    }
}
/// 对指定分区的最新视频和排行榜视频进行点踩操作
/// 为减少风控限制，分步执行点踩操作
pub(crate) async fn disklike_by_tid(tid: u32) -> R<u32> {
    info!("----开始对{}分区进行点踩----", tid);

    //1.获取该分区的排行榜视频
    info!("获取{}分区排行榜视频...", tid);
    let mut rank_video_list = bili::get_rank_by_tid(tid).await?;
    ThreadUtil::s5().await;

    //1.1 分步点踩
    disklisk_video_list(&mut rank_video_list, format!("对{}分区的排行榜视频进行点踩", tid)).await?;
    
    //2.获取该分区的最新视频
    info!("获取{}分区最新视频...", tid);
    let mut region_latest_video = vec![];
    for i in 1..=10{
        let mut result:Vec<VideoDetailDTO> = bili::get_region_lastest_video(i,tid).await?;

        info!("获取{}分区最新视频，页码：{},数量：{}",tid, i, result.len());
        
        disklisk_video_list(&mut result, format!("对{}分区的最新视频进行点踩", tid)).await?;
        region_latest_video.extend( result);
    }
    
    
    
    info!("点踩完毕，结束对{}分区的点踩操作，开始训练黑名单", tid);
    let mut all_video = rank_video_list;
    all_video.extend(region_latest_video);

    let size = all_video.len();
    rule_service::train_blacklist_by_video_list(all_video).await?;
    
    R::Ok(size as u32)
}

/// 批量给视频点踩
pub async fn disklisk_video_list(video_list: &mut Vec<VideoDetailDTO>, reason_str: String) -> R<()> {
    for video in video_list.iter_mut() {
        let aid = video.video_detail.aid;
        let error_hanle = {
            let db = video_detail_service::find_by_aid(aid).await?;

            if db.is_some() && db.unwrap().handle_step==100 {
                info!("该视频已经处理过了：{}", aid);
                continue;
            }

            info!(
                "对视频{}-{}进行点踩",
                aid,
                video.video_detail.title.as_ref().unwrap_or(&"".to_string())
            );
            bili::dislike(aid).await?;
            let mut reason = MatchResult::default();
            reason.user_handle_reason = Some(reason_str.clone());
            video_detail_service::record_handle_video(video, HandleType::DISLIKE, reason)
                .await?;

            ThreadUtil::s30().await;

            R::Ok(())
        };
        match error_hanle {
            Ok(_) => {}
            Err(e) => {
                error!("点踩视频:{}出错：{}", aid, e)
            }
        }
    }

    R::Ok(())
}
