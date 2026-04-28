use crate::api::bili;
use crate::app::config::CC;
use crate::app::error::HttpError;
use crate::app::response::R;
use crate::domain::owner::Owner;
use crate::utils::id;
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
