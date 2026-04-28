use axum::{Router, debug_handler};
use log::info;
use rbs::value;

use crate::{api::bili, app::{config::CC, response::{OkRespExt, R, RR}}, domain::owner::Owner, extractor::path::MyPath, service::owner_service};

pub fn create_router() -> Router {
    Router::new()
        .route(
            "/getUserNameByMid/{mid}",
            axum::routing::get(get_user_name_by_mid),
        )
        .route(
            "/getUserInfoByMid/{mid}",
            axum::routing::get(get_user_info_by_mid),
        )
        .route(
            "/getOwnerByAid/{aid}",
            axum::routing::get(get_owner_by_aid),
        )
}



/// 根据mid 获取用户名
pub async fn get_user_name_by_mid(MyPath(mid):MyPath<String>)->RR<String>{


    let mut name ;

    if let Some(owner) = Owner::select_one_by_condition(&CC.rb, value! {"mid":&mid}).await?{
        name = owner.name;
    }else {
        //调用接口查询
        name = bili::get_user_name_by_mid(mid).await?;
    }

    RR::success(name)
}

/// 根据mid获取用户信息
/// 先查owner表，如果没有则调用B站API获取并保存
pub async fn get_user_info_by_mid(MyPath(mid): MyPath<u64>) -> RR<Owner> {
    let owner = owner_service::get_owner_info_by_mid(mid).await?;
    RR::success(owner)
}

/// 根据视频aid获取视频的owner信息
/// 先查VideoDetail表获取owner_id，没有则调用B站API获取视频详情和owner信息
pub async fn get_owner_by_aid(MyPath(aid): MyPath<u64>) -> RR<Owner> {
    let owner = owner_service::get_owner_by_video_aid(aid).await?;
    RR::success(owner)
}

#[cfg(test)]
mod tests {
    use crate::domain::owner::Owner;
    use crate::domain::video_detail::VideoDetail;
    use crate::service::owner_service;

    #[tokio::test]
    async fn test_get_user_info_by_mid() {
        //第一句必须是这个
        crate::init().await;

        let mid: u64 = 3546794552199406;

        // 调用接口查询用户信息
        let owner = owner_service::get_owner_info_by_mid(mid).await.unwrap();
        println!("owner={:?}", owner);

        // 验证返回的数据
        assert_eq!(owner.id, mid);
        assert!(!owner.name.is_empty());

        // 查询owner表确认数据已入库
        let db_owner = Owner::select_one_by_condition(&crate::app::config::CC.rb, rbs::value! {"id": mid})
            .await
            .unwrap();
        assert!(db_owner.is_some(), "owner表中应存在mid={}的记录", mid);
        let db_owner = db_owner.unwrap();
        println!("db_owner={:?}", db_owner);
        assert_eq!(db_owner.id, mid);
        assert_eq!(db_owner.name, owner.name);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_get_owner_by_video_aid() {
        //第一句必须是这个
        crate::init().await;

        let aid: u64 = 305988942;

        // 调用接口查询视频的owner信息
        let owner = owner_service::get_owner_by_video_aid(aid).await.unwrap();
        println!("owner={:?}", owner);

        // 验证返回数据
        assert!(owner.id > 0);
        assert!(!owner.name.is_empty());

        // 查询VideoDetail表确认owner_id已填充
        let video = VideoDetail::select_by_id(&crate::app::config::CC.rb, aid)
            .await
            .unwrap();
        assert!(video.is_some(), "VideoDetail表中应存在aid={}的记录", aid);
        let video = video.unwrap();
        println!("video.owner_id={:?}", video.owner_id);
        assert!(video.owner_id.is_some(), "VideoDetail的owner_id应该已被填充");
        assert_eq!(video.owner_id.unwrap(), owner.id);

        // 查询owner表确认数据已入库
        let db_owner = Owner::select_one_by_condition(&crate::app::config::CC.rb, rbs::value! {"id": owner.id})
            .await
            .unwrap();
        assert!(db_owner.is_some(), "owner表中应存在id={}的记录", owner.id);
        let db_owner = db_owner.unwrap();
        println!("db_owner={:?}", db_owner);
        assert_eq!(db_owner.name, owner.name);

        //最后一句必须是这个
        log::logger().flush();
    }
}
