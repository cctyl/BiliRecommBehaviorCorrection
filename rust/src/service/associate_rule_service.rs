#[cfg(test)]
mod tests{
    use crate::app::config::CC;
    use crate::entity::models::AssociateRule;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;




        //在这中间编写测试代码



        //最后一句必须是这个
        log::logger().flush();
    }



    #[tokio::test]
    async fn test_select() {
        //第一句必须是这个
        crate::init().await;




        //在这中间编写测试代码
        let vec = AssociateRule::select_all(&CC.rb).await.unwrap();

        println!("{:#?}", vec);


        //最后一句必须是这个
        log::logger().flush();
    }

}