use crate::{api::bili, app::response::R, service::dict_service};



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


    // //视频详情
    // let video_detail_list = Vec::new();

    // //获取该用户的所有投稿视频
    // let all_video = Vec::new();

    // let page_num = 1;
    // while(true){

    //     //searchUserSubmissionVideo
    //     let page_bean =    
    //     bili::search_user_submission_video(user_id, page_num,"").await?;
        

    // }


    todo!()

}
