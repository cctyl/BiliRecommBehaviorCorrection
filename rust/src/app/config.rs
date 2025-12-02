use log::{error, info};
use serde::Serialize;


use core::error;
use std::{
    cell::RefCell,
    collections::HashMap,
    sync::{LazyLock, OnceLock},
    time::Duration,
};

use crate::{
    app::{ response::R},
    entity::models::Config,
    utils::glm_chat::{ChatConfig, ChatGlm},
};
use rbatis::{RBatis, dark_std::errors::new};
use rbdc_sqlite::Driver;
use rbdc_sqlite::driver::SqliteDriver;
use serde::{Deserialize, Deserializer, de};
use tokio::{runtime::Runtime, sync::RwLock};


#[derive(Debug, Clone)]
pub struct ServerConfig {
    pub secret: String,
    pub port: u16,
    pub db_url: String,
}


impl ServerConfig {
    pub fn new() -> ServerConfig {


        // 先尝试加载开发配置，如果不存在则加载默认配置
        if let Ok(_) = dotenvy::from_filename("config.dev.txt") {
            info!("已加载开发环境配置");
        } else {
            // 如果开发配置不存在，回退到默认配置
            dotenvy::from_filename("config.txt").expect("配置文件config.txt不存在！");
        }

        
        let port = std::env::var("PORT").map_or(8080, |s| s.parse::<u16>().unwrap());
        let secret = std::env::var("SECRET").expect("必须设置密钥");
        let db_url = std::env::var("DB_URL").expect("必须提供数据库链接");
        
      
        ServerConfig {
            secret,
            port,
            db_url,
        }
    }
}



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


#[cfg(test)]
mod tests{
    use crate::app::config::CC;



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