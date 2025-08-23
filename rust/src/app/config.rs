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
        dotenvy::from_filename("config.txt").expect("配置文件config.txt不存在！");
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
