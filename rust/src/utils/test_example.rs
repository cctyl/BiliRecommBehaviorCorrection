#[cfg(test)]
mod tests{


    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;
       



        //在这中间编写测试代码



        //最后一句必须是这个
        log::logger().flush();
    }


}