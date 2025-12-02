use crate::app::database::CC;
use crate::app::response::R;
use crate::entity::models::Owner;
use crate::utils::id;
use rbs::value;
use std::borrow::Cow::Owned;
#[cfg(test)]
mod tests{
    use crate::entity::models::Owner;

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
        let owner = super::find_or_create_by_mid(Owner {
            id: None,
            mid: 1234,
            name: "Sss".to_string(),
            face: None
        }).await.unwrap();

        //id必须存在
        assert!(owner.id.is_some());
        
        
        println!("{:?}",owner.id);

        //最后一句必须是这个
        log::logger().flush();
    }

}
/// 通过mid查找或创建up主信息，不存在则创建一个
pub(crate) async fn find_or_create_by_mid(mut owner:  Owner) -> R<Owner> {
    let option = Owner::select_one_by_condition(&CC.rb, value! {"mid":&owner.mid}).await?;

    let o = match option {
        None => {
            owner.id = Some(id::generate_id());
            Owner::insert(&CC.rb, &owner).await?;
            owner
        }
        Some(o) => o,
    };

    R::Ok(o)
}
