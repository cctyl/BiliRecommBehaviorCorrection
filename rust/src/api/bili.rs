use std::collections::HashMap;

use crate::app::constans;
use crate::app::response::R;
use anyhow::Context;
use hex;
use std::collections::BTreeMap;
use urlencoding::encode;

/**
 *  申请二维码（TV端）
 */
pub async fn get_tv_login_qr_code() -> R<()> {
    let url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code";

    let mut params: BTreeMap<&'static str, String> = BTreeMap::new();

    params.insert("appkey", constans::THIRD_PART_APPKEY.to_string());
    params.insert("appsec", constans::THIRD_PART_APPSEC.to_string());
    params.insert("local_id", "0".to_string());
    params.insert("ts", get_ts().to_string());
    let signed_params = get_app_sign(params);

    let client = reqwest::Client::new();
    let res = client
        .post(url)
        .form(&signed_params)
        .send()
        .await
        .context("Failed to send request")?;

    let echo_json: serde_json::Value = res.json().await.context("Failed to send request")?;

    println!("{echo_json:#?}");
    R::Ok(())
}

#[tokio::test]
async fn test_tv_login_qr_code() {
    get_tv_login_qr_code().await.unwrap();
}


use std::time::{SystemTime, UNIX_EPOCH};

pub fn get_ts() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_else(|_| std::time::Duration::from_secs(0))
        .as_secs()
}

/**
 * 获取签名后的参数，返回一个包含签名的map
 */
pub fn get_app_sign(mut params: BTreeMap<&'static str, String>) -> BTreeMap<&'static str, String> {
    // 插入默认 appkey
    params
        .entry("appkey")
        .or_insert(constans::THIRD_PART_APPKEY.to_string());

    // 序列化参数
    let query_string = params
        .iter()
        .map(|(k, v)| format!("{}={}", encode(k), encode(v)))
        .collect::<Vec<String>>()
        .join("&");

    // 获取 appsec，默认值
    let appsec = params
        .get("appsec")
        .cloned()
        .unwrap_or(constans::THIRD_PART_APPSEC.to_string());

    // 生成签名
    let sign = generate_md5(&(query_string + &appsec));

    // 插入 sign
    params.insert("sign", sign);

    params
}

/**
 * 生成 MD5 哈希值
 */
pub fn generate_md5(input: &str) -> String {
    let digest = md5::compute(input);
    hex::encode(digest.0)
}

#[test]
fn test_md5() {
    let input = "hello";
    let md5_hash = generate_md5(input);
    println!("MD5 hash of '{}': {}", input, md5_hash);
}

#[test]
fn test_appsign() {
    let mut params: BTreeMap<&'static str, String> = BTreeMap::new();

    params.insert("appkey", constans::THIRD_PART_APPKEY.to_string());
    params.insert("appsec", constans::THIRD_PART_APPSEC.to_string());
    params.insert("local_id", "0".to_string());
    params.insert("ts", "1755956745".to_string());
    let signed_params = get_app_sign(params);

    println!("{:?}", signed_params);
}
