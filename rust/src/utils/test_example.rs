#[cfg(test)]
mod tests{


    #[tokio::test]
    async fn example() {
        crate::init().await;
       






        log::logger().flush();
    }


}