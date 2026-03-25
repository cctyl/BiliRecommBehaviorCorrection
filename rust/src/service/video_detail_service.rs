use crate::app::config::CC;
use crate::app::response::R;
use crate::domain::dtos::VideoDetailDTO;
use crate::domain::enumeration::HandleType;
use crate::domain::{video_detail::MatchResult, video_detail::VideoDetail};
use crate::service::{owner_service, tag_service};
use crate::utils::id;
use anyhow::Context;
use log::info;
use rbatis::executor::Executor;
use rbatis::{Error, impled, sql};
use rbs::value;
use sqlparser::ast::ObjectType::View;

#[cfg(test)]
mod tests {
    use crate::api::bili;
    use crate::app::config::CC;
    use crate::app::response::R;
    use crate::domain::enumeration::HandleType;
    use crate::domain::video_detail::{ComplexMatch, MatchResult, SingleMatch, VideoDetail};
    use crate::service::video_detail_service::exist_by_id;
    use log::info;
    use rbs::value;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }


     #[tokio::test]
    async fn test_count_by_condition() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        let count_by_condition = VideoDetail::count_by_condition(&CC.rb,value!{"id":116181130349580u64} ).await.unwrap();

        print!("结果：{}",count_by_condition);
        //最后一句必须是这个
        log::logger().flush();
    }

    /// 编写一个查出来的测试函数

    #[tokio::test]
    async fn test_get_video_handle_reason() {
        crate::init().await;


        let select_by_id = VideoDetail::select_by_id(&CC.rb, 116181130349580u64).await.unwrap().unwrap();
        

        let reason = select_by_id.handle_reason.unwrap();

        println!("{:#?}",reason);



        //最后一句必须是这个
        log::logger().flush();
    }

    /// 测试handle_reason json 数据的存储
    #[tokio::test]
    async fn test_save_video_handle_reason() {
        //第一句必须是这个
        crate::init().await;

        let mut dto = bili::get_video_detail(116181130349580).await.unwrap();

        // dto.video_detail.aid = 9999999998;

        dto.video_detail.bvid = dto.video_detail.bvid + "aaa";

        let mut newitem: VideoDetail = dto.video_detail.into();
        // 6. 完整匹配结果（所有字段都有值）
        let full_match_result = MatchResult {
            single_match: Some(SingleMatch {
                is_match: true,
                tag: vec!["游戏".to_string()],
                desc: vec!["包含违规描述".to_string()],
                title: vec!["标题违规".to_string()],
                cover: vec!["封面违规".to_string()],
                mid: vec![111],
                tid: vec!["222".to_string()],
                match_count: 4,
            }),
            complex_match: Some(ComplexMatch {
                is_match: true,
                rule_name: Some("复杂规则A".to_string()),
                tag: vec!["动漫".to_string()],
                desc: vec!["描述匹配复杂规则".to_string()],
                title: vec![],
                cover: vec![],
                mid: vec![333],
                tid: vec!["444".to_string()],
                match_count: 1,
            }),
            ai_match: Some(false),
            user_handle_reason: Some("AI误判，人工修正".to_string()),
        };
        newitem.handle_reason = Some(full_match_result);

        VideoDetail::insert(&CC.rb, &newitem).await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_exist_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        let exist_by_id = exist_by_id(&CC.rb, 305988942).await.unwrap();
        println!("{}", exist_by_id);

        //最后一句必须是这个
        log::logger().flush();
    }

    //测试 save_video_detail
    #[tokio::test]
    async fn test_save_video_detail() {
        //第一句必须是这个
        crate::init().await;

        let mut dto = bili::get_video_detail(116181130349580).await.unwrap();

        // dto.video_detail.aid = 9999999998;

        dto.video_detail.bvid = dto.video_detail.bvid + "aaa";

        super::save_video_detail(&mut dto).await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }

    //测试 save_video_detail
    #[tokio::test]
    async fn test_save_video() {
        //第一句必须是这个
        crate::init().await;

        let mut dbdata = VideoDetail::select_by_id(&CC.rb, 305988942)
            .await
            .unwrap()
            .unwrap();

        dbdata.id = 999999999999;
        dbdata.title = Some("aaaaaaa".to_string());
        dbdata.bvid = "aaaaaaa".to_string();

        println!("{:#?}", dbdata);
        let insert = VideoDetail::insert(&CC.rb, &dbdata).await.unwrap();

        println!("{:#?}", insert);

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

        let mut reason = MatchResult::default();
        reason.user_handle_reason = Some("aaaaa".to_string());
        //在这中间编写测试代码
        super::record_handle_video(&mut dto, HandleType::DISLIKE, reason)
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
        let v = super::find_by_aid(114518642397331).await.unwrap();
        // info!("{}", v.unwrap().id.unwrap());

        info!("{:#?}", v);

        log::logger().flush();
    }
}
/// 根据aid查询
pub(crate) async fn find_by_aid(aid: u64) -> R<Option<VideoDetail>> {
    let v = VideoDetail::select_one_by_condition(
        &CC.rb,
        value! {
            "id":aid
        },
    )
    .await?;

    R::Ok(v)
}

/// 记录视频的处理结果
pub(crate) async fn record_handle_video(
    video: &mut VideoDetailDTO,
    handle_type: HandleType,
    reason: MatchResult,
) -> R<()> {
    let mut v: VideoDetail = video.video_detail.clone().into();

    v.handle_type = Some(handle_type);
    v.handle_step = 100;
    v.handle_reason = Some(reason);

    if exist_by_id(&CC.rb, v.id).await? == 0 {
        save_video_detail(video).await?;
    } else {
        VideoDetail::update_by_id(&CC.rb, &v).await?;
    }

    R::Ok(())
}

///保存视频详情，包括关联数据
async fn save_video_detail(dto: &mut VideoDetailDTO) -> R<()> {
    let exist = VideoDetail::select_one_by_condition(
        &CC.rb,
        value! {
            "id": dto.video_detail.aid
        },
    )
    .await?;
    if let Some(db) = exist {
        let v: VideoDetail = dto.video_detail.clone().into();
        VideoDetail::update_by_id(&CC.rb, &v).await?;
        info!("aid={}的视频已存在，只更新视频信息", dto.video_detail.aid);
        return R::Ok(());
    }

    info!("开始保存up主信息:{:?}", dto.owner);

    //2.保存关联数据
    //2.1 up主信息
    if let Some(owner) = &dto.owner {
        let o = owner_service::find_or_create_by_mid(owner.clone().into())
            .await
            .context("保存up主信息出错")?;
        dto.video_detail.owner_id = Some(o.id);
    }

    //2.2 标签数据

    info!("开始保存tags信息:{:?}", dto.tags);
    let mut tag_vec: Vec<String> = vec![];
    if let Some(tags) = &dto.tags {
        let nvec: Vec<String> = tags.iter().map(|f| f.tag_name.clone()).collect();
        tag_vec.extend(nvec);

        tag_service::save_if_not_exists(tags.clone())
            .await
            .context("保存标签出错")?;
    }

    //1.保存本体
    info!("开始保存本体信息:{:?}", dto.video_detail);
    if exist_by_id(&CC.rb, dto.video_detail.aid).await? == 0 {
        let mut v: VideoDetail = dto.video_detail.clone().into();
        v.tag = Some(tag_vec.join(","));
        VideoDetail::insert(&CC.rb, &v)
            .await
            .context("保存本体出错")?;
    }

    R::Ok(())
}

/// 根据id判断是否存在
#[sql("SELECT count(1) > 0 FROM video_detail WHERE id = ?")]
async fn exist_by_id(rb: &dyn Executor, id: u64) -> Result<u32, Error> {
    impled!()
}
