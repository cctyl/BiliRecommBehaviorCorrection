use std::sync::Arc;

use crate::{api::bili, app::response::R, entity::{dtos::UserSubmissionVideo, models::VideoDetail}, handler::black_rule_handler, service::{black_rule_service, dict_service}, utils::{data_util::{self}, thread_util::ThreadUtil}};

use data_util::Consumer;
use serde::de;
use tokio::sync::Mutex;


pub struct VideoHandler;
impl Consumer for VideoHandler  {
    
    type T = UserSubmissionVideo;
    type Arg = bool;
    type Output = Vec<VideoDetail>;
    
    async fn accept(v:&Self::T,arg:&mut Self::Arg,  video_detail_list: &mut Self::Output)->R<()> {
        let train= arg;
        //获取视频详情并加入
        let detail:VideoDetail =   bili::get_video_detail(v.aid).await?;
        let aid = detail.aid;
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
    dict_service::add_black_user_id(user_id).await?;

    //视频详情
    let mut video_detail_list:Vec<VideoDetail> = Vec::new();

    //获取该用户的所有投稿视频
    let mut all_video = Vec::new();

    let mut page_num = 1;
    loop {

        //searchUserSubmissionVideo
        let page_bean =    
            bili::search_user_submission_video(user_id, page_num,"").await?;
        let has_more = page_bean.has_more();
        all_video.extend(page_bean.data);
        page_num += 1;
        ThreadUtil::s20().await;
        if has_more {
            break;
        }
    }

   
    video_detail_list = VideoHandler::random_access_list(
    &all_video, 
    all_video.len(),
        train,
        video_detail_list
    ).await?;

    
    if train {
        //保存数据
        black_rule_service::trainBlacklistByVideoList(&video_detail_list).await?;
    }

    R::Ok(video_detail_list.len() as u32)

}
