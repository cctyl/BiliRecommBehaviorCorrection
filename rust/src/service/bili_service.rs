use crate::{api::bili, app::response::R, entity::{dtos::UserSubmissionVideo, models::VideoDetail}, service::dict_service, utils::{data_util::{self}, thread_util::ThreadUtil}};

use data_util::Consumer;


pub struct VideoHandler;
impl Consumer for VideoHandler  {
    
    type T = UserSubmissionVideo;
    
    async fn accept(t:&Self::T)->R<()> {



        let detail:VideoDetail =   bili::get_video_detail(t.aid).await?;

        todo!()
    }
    
    async fn random_access_list(source: &[Self::T], size: usize) -> R<()>
    {
        let actual_size = std::cmp::min(size, source.len());
        let indices = data_util::get_random_set(actual_size, 0, (actual_size - 1) as i32);
    
        for &index in &indices {
            Self::accept(&source[index as usize]).await?;
        }
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
pub async fn disklike_by_user_id(user_id: &str, train: bool) -> R<i32> {
    

    //该用户会被加入黑名单
    dict_service::add_black_user_id(user_id).await?;

    //视频详情
    let video_detail_list:Vec<VideoDetail> = Vec::new();

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

   
    VideoHandler::random_access_list(
        &all_video, all_video.len()
    ).await?;

    // data_util::random_access_list(&all_video, all_video.len(),
    //     async|v| {


    //         let detail:VideoDetail =   bili::get_video_detail(v.aid).await?;


    //         R::Ok(())
    //     }

    // ).await?;


    todo!()

}
