use rbatis::{crud, executor::Executor, impled, rbdc::DateTime, sql};
use serde::{Deserialize, Serialize};

use crate::{
    app::{config::CC, response::RB},
    plus,
    utils::id::generate_id,
};

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Config {
    pub id: String,
    pub name: String,
    pub value: Option<String>,
    pub expire_second: Option<i32>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
}

crud!(Config {});
plus!(Config {});

impl Config {
    pub fn default() -> Self {
        Config {
            id: generate_id(),
            name: String::new(),
            value: Some(String::new()),
            expire_second: None,
            created_date: Some(DateTime::now()),
            last_modified_date: Some(DateTime::now()),
        }
    }

    #[sql("SELECT * FROM config WHERE name = ?")]
    pub async fn select_by_name(rb: &dyn Executor, name: &str) -> Option<Config> {
        impled!()
    }
}

#[cfg(test)]
mod tests {
    use crate::{app::config::CC, domain::config::Config};

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_select_by_name() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let select_by_name = Config::select_by_name(&CC.rb, "bili:subKey").await.unwrap();
        println!("{:#?}", select_by_name);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_config() {
        _ = fast_log::init(
            fast_log::Config::new()
                .console()
                .level(log::LevelFilter::Debug),
        );
        CC.init().await;

        let select_by_map = Config::select_by_map(&CC.rb, rbs::Value::Null)
            .await
            .unwrap();
        println!("{:#?}", select_by_map);
    }
}
