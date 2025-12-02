use crate::app::config::CC;
use crate::app::response::R;
use crate::entity::dtos::VideoDetailDTO;
use crate::entity::enumeration::HandleType;
use crate::entity::models::VideoDetail;
use crate::service::{owner_service, tag_service};
use crate::utils::id;
use anyhow::Context;
use log::info;
use rbs::value;
use sqlparser::ast::ObjectType::View;

#[cfg(test)]
mod tests {
    use crate::api::bili;
    use crate::app::response::R;
    use crate::entity::enumeration::HandleType;
    use log::info;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    //测试 save_video_detail
    #[tokio::test]
    async fn test_save_video_detail() {
        //第一句必须是这个
        crate::init().await;

        let mut dto = bili::get_video_detail(532944355).await.unwrap();

        dto.video_detail.aid = 9999999998;
        dto.video_detail.bvid = dto.video_detail.bvid + "aaa";

        super::save_video_detail(&mut dto).await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }
    #[tokio::test]
    async fn test_record_handle_video() {
        //第一句必须是这个
        crate::init().await;

        let mut dto = bili::get_video_detail(532944355).await.unwrap();

        dto.video_detail.aid = 9999999998;
        dto.video_detail.bvid = dto.video_detail.bvid + "aaa";
        dto.video_detail.id = Some(String::from("11769683978616837"));

        //在这中间编写测试代码
        super::record_handle_video(
            &mut dto,
            HandleType::DISLIKE,
            "测试aaaaaaaaaaasdsadasdasdasds".to_string(),
        )
        .await
        .expect("保存出错");

        //最后一句必须是这个
        log::logger().flush();
    }
    
    
    //测试 find_by_aid
    #[tokio::test]
    async fn test_find_by_aid() { 
        //第一句必须是这个
        crate::init().await;
        let v = super::find_by_aid(643755790).await.unwrap();
        info!("{}", v.unwrap().id.unwrap());
        log::logger().flush();
    }
    
}
/// 根据aid查询
pub(crate) async fn find_by_aid(aid: u64) -> R<Option<VideoDetail>> {
    let v = VideoDetail::select_one_by_condition(
        &CC.rb,
        value! {
            "aid":aid
        },
    )
    .await?;

    R::Ok(v)
}

/// 记录视频的处理结果
pub(crate) async fn record_handle_video(
    video: &mut VideoDetailDTO,
    handle_type: HandleType,
    reason: String,
) -> R<()> {
    video.video_detail.handle_type = Some(handle_type);
    video.video_detail.handle = Some(false);
    video.video_detail.handle_reason = Some(reason);
    if video.video_detail.id.is_none() {
        save_video_detail(video).await?;
    } else {
        VideoDetail::update_by_id(&CC.rb, &video.video_detail).await?;
    }

    R::Ok(())
}

///保存视频详情，包括关联数据
async fn save_video_detail(dto: &mut VideoDetailDTO) -> R<()> {
    let exist = VideoDetail::select_one_by_condition(
        &CC.rb,
        value! {
            "aid": dto.video_detail.aid
        },
    )
    .await?;
    if let Some(db) = exist {
        dto.video_detail.id = db.id;
        VideoDetail::update_by_id(&CC.rb, &dto.video_detail).await?;
        info!("aid={}的视频已存在，只更新视频信息", dto.video_detail.aid);
        return R::Ok(());
    }

    //2.保存关联数据
    //2.1 up主信息
    if let Some(owner) = &dto.owner {
        let o = owner_service::find_or_create_by_mid(owner.clone())
            .await
            .context("保存up主信息出错")?;
        dto.video_detail.owner_id = o.id;
    }

    //2.2 标签数据
    if let Some(tags) = &dto.tags {
        tag_service::save_if_not_exists(tags.clone())
            .await
            .context("保存标签出错")?;

        let string = tags
            .iter()
            .map(|tag| tag.tag_id.to_string())
            .collect::<Vec<String>>()
            .join(",");
        dto.video_detail.tag_ids = Some(string);
    }

    //1.保存本体
    dto.video_detail.id = Some(id::generate_id());
    VideoDetail::insert(&CC.rb, &dto.video_detail)
        .await
        .context("保存本体出错")?;

    R::Ok(())
}
