use image::Luma;
use log::{error, info};
use qrcode::QrCode;
use qrcode::render::unicode;
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
        
        print_qr(port, &secret);
        Config {
            secret,
            port,
            db_url,
        }
    }
}

pub fn print_qr(port:u16, secret:&String) {


    if let Some(ip) = getIp(){
      
        let addr = format!("{ip}:{port}");
        info!("本机ip：{addr}");
        let qr = QrCodeDto {
            secret: secret.clone(),
            addr,
        };

        let jsonstr = serde_json::to_string(&qr).unwrap();
        let code = QrCode::new(jsonstr).unwrap();
        // Render the bits into an image.
        let image = code.render::<Luma<u8>>().build();

        // Save the image.
        image.save("扫我.png").unwrap();
    }else{

        error!("获取ip失败，请手动设置IP地址");
    }
    
}
use std::net::UdpSocket;


pub fn getIp() -> Option<String> {
    let socket = match UdpSocket::bind("0.0.0.0:0") {
        Ok(s) => s,
        Err(_) => return None,
    };

    match socket.connect("8.8.8.8:80") {
        Ok(()) => (),
        Err(_) => return None,
    };

    match socket.local_addr() {
        Ok(addr) => return Some(addr.ip().to_string()),
        Err(_) => return None,
    };
}