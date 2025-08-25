use std::sync::LazyLock;




//请求客户端
pub static CLIENT: LazyLock<reqwest::Client> = LazyLock::new(|| reqwest::Client::new());

