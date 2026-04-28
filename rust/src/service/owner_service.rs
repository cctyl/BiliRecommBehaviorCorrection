use crate::api::bili;
use crate::app::config::CC;
use crate::app::error::HttpError;
use crate::app::response::R;
use crate::domain::owner::Owner;
use crate::domain::video_detail::VideoDetail;
use crate::utils::id;
use log::info;
use rbs::value;
use std::borrow::Cow::Owned;
#[cfg(test)]
mod tests{
    use crate::domain::owner::Owner;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;




        //在这中间编写测试代码



        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_find_or_create_by_mid() {
        //第一句必须是这个
        crate::init().await;




        //在这中间编写测试代码
        let owner: Owner = super::find_or_create_by_mid(Owner {
            id: 123,
            name: "Sss".to_string(),
            face: None
        }).await.unwrap();

        
        
        println!("{:?}",owner.id);

        //最后一句必须是这个
        log::logger().flush();
    }

}
/// 通过mid查找或创建up主信息，不存在则创建一个
pub(crate) async fn find_or_create_by_mid(mut owner:  Owner) -> R<Owner> {
    let option = Owner::select_one_by_condition(&CC.rb, value! {"id":&owner.id}).await?;

    let o = match option {
        None => {
         
            Owner::insert(&CC.rb, &owner).await?;
            owner
        }
        Some(o) => o,
    };

    R::Ok(o)
}

/// 根据mid查询用户信息，先查owner表，没有则调用B站API获取并保存到owner表
pub(crate) async fn get_owner_info_by_mid(mid: u64) -> R<Owner> {
    // 先查owner表
    let option = Owner::select_one_by_condition(&CC.rb, value! {"id": mid}).await?;

    match option {
        Some(owner) => R::Ok(owner),
        None => {
            // 调用B站API获取用户信息
            let data = bili::get_user_info_by_mid(mid).await?;

            let name = data["name"]
                .as_str()
                .ok_or(HttpError::Biz("获取用户名失败".to_string()))?
                .to_string();
            let face = data["face"].as_str().map(|s| s.to_string());

            let owner = Owner {
                id: mid,
                name,
                face,
            };

            // 保存到owner表
            Owner::insert(&CC.rb, &owner).await?;

            R::Ok(owner)
        }
    }
}

/// 根据视频aid查询视频的owner信息
/// 1. 先查VideoDetail表，获取owner_id
/// 2. 如果没有owner_id，调用get_video_detail获取视频详情和owner信息，保存owner并更新VideoDetail的owner_id
/// 3. 如果有owner_id但owner表中没有对应记录，调用get_owner_info_by_mid获取
pub(crate) async fn get_owner_by_video_aid(aid: u64) -> R<Owner> {
    // 查询VideoDetail
    let video = VideoDetail::select_by_id(&CC.rb, aid).await?;

    match video {
        Some(mut v) => {
            match v.owner_id {
                Some(owner_id) => {
                    // 有owner_id，查owner表
                    let owner_opt = Owner::select_one_by_condition(&CC.rb, value! {"id": owner_id}).await?;
                    match owner_opt {
                        Some(owner) => R::Ok(owner),
                        None => {
                            // owner_id存在但owner表中没有记录，调用get_owner_info_by_mid
                            info!("视频aid={}有owner_id={}但owner表中无记录，调用API获取", aid, owner_id);
                            get_owner_info_by_mid(owner_id).await
                        }
                    }
                }
                None => {
                    // 没有owner_id，调用get_video_detail获取
                    info!("视频aid={}没有owner_id，调用get_video_detail获取", aid);
                    let detail = bili::get_video_detail(aid).await?;

                    // 从detail中获取owner信息并保存
                    let owner_json = detail.owner
                        .ok_or(HttpError::Biz(format!("视频aid={}获取不到owner信息", aid)))?;
                    let owner_id = owner_json.mid;
                    let owner = Owner {
                        id: owner_id,
                        name: owner_json.name,
                        face: owner_json.face,
                    };

                    // 保存owner到数据库
                    find_or_create_by_mid(owner.clone()).await?;

                    // 更新VideoDetail的owner_id
                    v.owner_id = Some(owner_id);
                    VideoDetail::update_by_id(&CC.rb, &v).await?;
                    info!("已更新视频aid={}的owner_id={}", aid, owner_id);

                    R::Ok(owner)
                }
            }
        }
        None => {
            // VideoDetail表中没有该视频，调用get_video_detail获取
            info!("视频aid={}不在VideoDetail表中，调用get_video_detail获取", aid);
            let detail = bili::get_video_detail(aid).await?;

            // 从detail中获取owner信息并保存
            let owner_json = detail.owner
                .ok_or(HttpError::Biz(format!("视频aid={}获取不到owner信息", aid)))?;
            let owner_id = owner_json.mid;
            let owner = Owner {
                id: owner_id,
                name: owner_json.name,
                face: owner_json.face,
            };

            // 保存owner到数据库
            find_or_create_by_mid(owner.clone()).await?;

            // 保存VideoDetail到数据库（带owner_id）
            let mut video_detail: VideoDetail = detail.video_detail.into();
            video_detail.owner_id = Some(owner_id);
            VideoDetail::insert(&CC.rb, &video_detail).await?;
            info!("已保存视频aid={}及owner_id={}", aid, owner_id);

            R::Ok(owner)
        }
    }
}
