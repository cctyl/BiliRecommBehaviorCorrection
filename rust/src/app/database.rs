use core::error;
use std::{
    cell::RefCell,
    collections::HashMap,
    sync::{LazyLock, OnceLock},
    time::Duration,
};

use crate::{
    app::{config::ServerConfig, response::R},
    entity::models::Config,
    utils::glm_chat::{ChatConfig, ChatGlm},
};
use log::error;
use rbatis::{RBatis, dark_std::errors::new};
use rbdc_sqlite::Driver;
use rbdc_sqlite::driver::SqliteDriver;
use serde::{Deserialize, Deserializer, de};
use tokio::{runtime::Runtime, sync::RwLock};

pub struct AppContext {
    pub rb: RBatis,
    pub config: ServerConfig,
    pub chat: RwLock<ChatGlm>,
}
pub static CC: LazyLock<AppContext> = LazyLock::new(|| AppContext {
    rb: RBatis::new(),
    config: ServerConfig::new(),
    chat: RwLock::new(ChatGlm::new(ChatConfig::default())),
});

impl AppContext {
    pub async fn init(&self) -> R<()> {
        self.init_db().await;

        self.init_glm_chat().await?;
        R::Ok(())
    }

    /// 初始化glm chat
    pub async fn init_glm_chat(&self) -> R<String> {
        //ai 功能是否开启
        let enable = Config::select_by_name(&CC.rb, "ai_chat_enable")
            .await?
            .map_or(false, |c| c.value.map_or(false, |ss| ss == "true"));

        let mut config = ChatConfig::default();
        config.enable = enable;
        if !enable {
            log::info!("[bili-rust] ai 功能未启用");
        } else {
            match Config::select_by_name(&CC.rb, "ai_api_key")
                .await?
                .map_or(None, |c| c.value)
            {
                Some(api_key) => {
                    config.api_key = api_key;
                }
                None => {
                    config.enable = false;
                    error!("[bili-rust] ai api key 未配置,请配置ai api key");
                    return R::Ok("[bili-rust] ai api key 未配置,请配置ai api key".to_string());
                }
            }

            match Config::select_by_name(&CC.rb, "ai_base_url")
                .await?
                .map_or(None, |c| c.value)
            {
                Some(base_url) => {
                    config.base_url = base_url;
                }
                None => {
                    config.enable = false;
                    error!("[bili-rust] ai base_url 未配置,请配置ai base_url");
                    return R::Ok("[bili-rust] ai base_url 未配置,请配置ai base_url".to_string());
                }
            }

            match Config::select_by_name(&CC.rb, "ai_model")
                .await?
                .map_or(None, |c| c.value)
            {
                Some(model) => {
                    config.model = model;
                }
                None => {
                    config.enable = false;
                    error!("[bili-rust] ai model 未配置,请配置ai model");
                    return R::Ok("[bili-rust] ai model 未配置,请配置ai model".to_string());
                }
            }


            Config::select_by_name(&CC.rb, "ai_temperature").await?.map(|c| {
                if let Some(value) = c.value {
                    config.temperature = value.parse::<f32>().unwrap_or(0.7);
                }
            });

            Config::select_by_name(&CC.rb, "ai_max_tokens").await?.map(|c| {
                if let Some(value) = c.value {
                    config.max_tokens = value.parse::<i32>().unwrap_or(4096);
                }
            });

            Config::select_by_name(&CC.rb, "ai_system_prompt").await?.map(|c| {

                if let Some(value) = c.value {
                    config.system_prompt = value;
                }
            });
        }


        let mut lock = self.chat.write().await;
        lock.replace_config(config);

        R::Ok("[bili-rust] ai chat init success!".to_string())
    }

    /// 初始化数据库
    pub async fn init_db(&self) {
        self.rb
            .link(SqliteDriver {}, &self.config.db_url)
            .await
            .expect("[bili-rust] rbatis pool init fail!");

        let pool = self.rb.get_pool().unwrap();
        //max connections
        pool.set_max_open_conns(1).await;
        //max timeout
        pool.set_timeout(Some(Duration::from_secs(20))).await;
        log::info!(
            "[bili-rust] rbatis pool init success! pool state = {}",
            self.rb.get_pool().expect("pool not init!").state().await
        );
    }
}







pub fn bool_or_int<'de, D>(deserializer: D) -> Result<bool, D::Error>
where
    D: Deserializer<'de>,
{
    struct BoolOrIntVisitor;

    impl<'de> de::Visitor<'de> for BoolOrIntVisitor {
        type Value = bool;

        fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
            formatter.write_str("a boolean or an integer")
        }

        fn visit_bool<E>(self, value: bool) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(value)
        }

        fn visit_i32<E>(self, value: i32) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(value != 0)
        }

        fn visit_i64<E>(self, value: i64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(value != 0)
        }

        fn visit_u64<E>(self, value: u64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(value != 0)
        }
    }

    deserializer.deserialize_any(BoolOrIntVisitor)
}
pub fn bool_or_int_opt<'de, D>(deserializer: D) -> Result<Option<bool>, D::Error>
where
    D: Deserializer<'de>,
{
    struct BoolOrIntVisitor;

    impl<'de> de::Visitor<'de> for BoolOrIntVisitor {
        type Value = Option<bool>;

        fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
            formatter.write_str("a boolean or an integer")
        }

        fn visit_bool<E>(self, value: bool) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(Some(value))
        }

        fn visit_i32<E>(self, value: i32) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(Some(value != 0))
        }

        fn visit_i64<E>(self, value: i64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(Some(value != 0))
        }

        fn visit_u64<E>(self, value: u64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(Some(value != 0))
        }

        fn visit_none<E>(self) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(None)
        }

        fn visit_some<D>(self, deserializer: D) -> Result<Self::Value, D::Error>
        where
            D: Deserializer<'de>,
        {
            // 递归调用deserialize_any来处理Some中的值
            deserializer.deserialize_any(BoolOrIntVisitor)
        }
        fn visit_unit<E>(self) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(None)
        }
    }

    deserializer.deserialize_any(BoolOrIntVisitor)
}



#[cfg(test)]
mod tests{
    use crate::app::database::CC;



    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;
       



        //在这中间编写测试代码
        let chat = &CC.chat;
        let read = chat.read().await;

        let chat = read.chat("rust是什么东西").await.unwrap();
        println!("{}",chat);

        let config = read.config();
        println!("{:#?}",config);


        //最后一句必须是这个
        log::logger().flush();
    }


}