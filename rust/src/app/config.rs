use log::{error, info};
use serde::Serialize;
#[derive(Debug, Clone)]
pub struct Config {
    pub secret: String,
    pub port: u16,
    pub db_url: String,
}

#[derive(Serialize)]
pub struct QrCodeDto {
    pub secret: String,
    pub addr: String,
}

impl Config {
    pub fn new() -> Config {


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
        
      
        Config {
            secret,
            port,
            db_url,
        }
    }
}
