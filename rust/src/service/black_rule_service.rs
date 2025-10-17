use crate::{app::response::R, entity::models::VideoDetail};



/// 根据视频列表训练黑名单
pub(crate) async fn trainBlacklistByVideoList(video_detail_list: &[VideoDetail]) -> R<()> {
    todo!()
}