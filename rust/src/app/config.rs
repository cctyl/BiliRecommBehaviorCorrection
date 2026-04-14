use log::{LevelFilter, debug, error, info};
use rbs::value;
use serde::Serialize;

use core::error;
use std::{
    cell::RefCell,
    collections::HashMap,
    sync::{Arc, LazyLock, OnceLock},
    time::Duration,
};

use crate::{
    app::{interceptor::SqlOnlyLogInterceptor, response::R}, domain::config::Config, service::{config_service, task_service}, utils::glm_chat::{ChatConfig, ChatGlm}
};
use rbatis::{
    RBatis, dark_std::errors::new, intercept_log::LogInterceptor, intercept_page::PageIntercept,
};
use rbdc_sqlite::Driver;
use rbdc_sqlite::driver::SqliteDriver;
use serde::{Deserialize, Deserializer, de};
use tokio::{runtime::Runtime, sync::RwLock};
use crate::api::bili;

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
            info!("加载正式环境配置");
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
    pub config_map: RwLock<HashMap<String, String>>,
}
pub static CC: LazyLock<AppContext> = LazyLock::new(|| AppContext {
    rb: RBatis::new(),
    config: ServerConfig::new(),
    chat: RwLock::new(ChatGlm::new(ChatConfig::default())),
    config_map: RwLock::new(HashMap::new()),
});

impl AppContext {
    pub async fn init(&self) -> R<()> {
        self.init_db().await;

        // 先初始化配置，后面的初始化需要用到这里的配置
        self.init_config_data().await?;

        self.init_glm_chat().await?;


        //启动时检查和更新一些配置
        Self::check_on_start().await?;

        R::Ok(())
    }



    /// 启动时检查和更新一些配置
    pub async fn check_on_start() ->R<()>{

        // 启动时，所有任务都应该是停止的
        task_service::update_stop_status().await?;


        //检查access_key 是否过期
        config_service::check_accesskey().await;


        // 如果是初次启动，记录启动时间
        config_service::set_info().await?;


        // 获取新的cookie 信息
        bili::get_home().await?;

        R::Ok(())
    }


    /// 初始化glm chat
    pub async fn init_glm_chat(&self) -> R<String> {
        info!("初始化ai客户端");
        let lock = self.config_map.read().await;

        //ai 功能是否开启
        let enable = lock.get("ai_chat_enable").map_or(false, |f| f == "true");

        let mut config = ChatConfig::default();
        config.enable = enable;

        // info!("初始config：{:#?}", config);

        if !enable {
            log::info!("[bili-rust] ai 功能未启用");
        } else {
            match lock.get("ai_api_key") {
                Some(api_key) => {
                    config.api_key = api_key.clone();
                }
                None => {
                    config.enable = false;
                    error!("[bili-rust] ai api key 未配置,请配置ai api key");
                    return R::Ok("[bili-rust] ai api key 未配置,请配置ai api key".to_string());
                }
            }

            match lock.get("ai_base_url") {
                Some(base_url) => {
                    config.base_url = base_url.clone();
                }
                None => {
                    config.enable = false;
                    error!("[bili-rust] ai base_url 未配置,请配置ai base_url");
                    return R::Ok("[bili-rust] ai base_url 未配置,请配置ai base_url".to_string());
                }
            }

            match lock.get("ai_model") {
                Some(model) => {
                    config.model = model.clone();
                }
                None => {
                    config.enable = false;
                    error!("[bili-rust] ai model 未配置,请配置ai model");
                    return R::Ok("[bili-rust] ai model 未配置,请配置ai model".to_string());
                }
            }

            if let Some(value) = lock.get("ai_temperature") {
                config.temperature = value.parse::<f32>().unwrap_or(0.7);
            };

            if let Some(value) = lock.get("ai_max_tokens") {
                config.max_tokens = value.parse::<i32>().unwrap_or(4096);
            }

            if let Some(value) = lock.get("ai_system_prompt") {
                config.system_prompt = value.clone();
            }
        }
        // info!("赋值后config：{:#?}", config);

        let mut lock = self.chat.write().await;
        lock.replace_config(config);

        R::Ok("[bili-rust] ai chat init success!".to_string())
    }

    /// 初始化数据库
    pub async fn init_db(&self) {
        // 清除默认拦截器
        self.rb.intercepts.clear();

        // PageIntercept 要在前面
        self.rb.intercepts.push(Arc::new(PageIntercept::new()));
        // 自定义的SQL日志拦截器
        self.rb
            .intercepts
            .push(Arc::new(SqlOnlyLogInterceptor::default()));
    

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

    /// 初始化config数据
    pub async fn init_config_data(&self) -> R<()> {
        let select_all: Vec<Config> = Config::select_by_map(&CC.rb, value! {}).await?;
        let mut lock = self.config_map.write().await;

        for ele in select_all {
            lock.insert(ele.name, ele.value.unwrap_or("".to_string()));
        }

        debug!("初始化init_config_data：");
        // debug!("{:#?}", *lock);
        R::Ok(())
    }

    /// 更新config数据
    pub async fn update_config_data(&self, v: Vec<(String, String)>) -> R<()> {
        let mut lock = self.config_map.write().await;
        for ele in v {
            lock.insert(ele.0, ele.1);
        }
        info!("更新实时配置：{:#?}", *lock);
        R::Ok(())
    }
}

#[cfg(test)]
mod tests {
    use crate::app::config::CC;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        // //在这中间编写测试代码
        // let chat = &CC.chat;
        // let read = chat.read().await;

        // let chat = read.chat("rust是什么东西").await.unwrap();
        // println!("{}", chat);

        // let config = read.config();
        // println!("{:#?}", config);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_init_config() {
        //第一句必须是这个
        crate::init().await;

        //最后一句必须是这个
        log::logger().flush();
    }
}
