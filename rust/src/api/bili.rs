use std::collections::HashMap;
use std::sync::{LazyLock, Mutex};

use crate::app::response::R;
use crate::app::{constans, error::HttpError};
use crate::utils::http::CLIENT;
use anyhow::Context;
use hex;
use log::{error, info};
use reqwest::header;
use serde_json::json;
use std::collections::BTreeMap;
use urlencoding::encode;
//扫码登陆的code
static TEMP_TV_AUTH_CODE: Mutex<String> = Mutex::new(String::new());

fn check_resp(value: &serde_json::Value) -> R<()> {
    let flag = value["code"]
        .as_i64()
        .ok_or(HttpError::Biz("Failed to get code".to_string()))?
        == 0;

    if !flag {

        if let Some(code) = value["code"].as_i64() {
            match code {
                86090 => info!("已扫码未确认"),
                10003 => info!("稿件不存在"),
                86039 => info!("二维码尚未确认"),
                65007 => info!("视频已踩过"),
                -101 => {
                    //TODO 登陆过期，清除accessKey
                    //configService.updateAccessKey(null);
                    info!("登录过期，清除accessKey");
                    error!("body: {:#?}", value);
                }
                -404 => {
                    info!("请求的资源不存在");
                    error!("body: {:#?}", value);
                }
                _ => {
                    info!("异常的code: {:#?}", code);
                    error!("body: {:#?}", value);
                }
            }
        } else {
            error!("body: {:#?}", value);
            error!("响应异常: code not found");
        }

        R::Err(HttpError::Biz(format!(
            "检查响应失败:{:#?}",
            value["message"].as_str().unwrap_or("")
        )))
    } else {
        R::Ok(())
    }
}

/**
 *  申请二维码（TV端）
 */
pub async fn get_tv_login_qr_code() -> R<String> {
    let url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code";
    let mut params: BTreeMap<&'static str, String> = BTreeMap::new();
    params.insert("appkey", constans::THIRD_PART_APPKEY.to_string());
    params.insert("appsec", constans::THIRD_PART_APPSEC.to_string());
    params.insert("local_id", "0".to_string());
    params.insert("ts", get_ts().to_string());
    let signed_params = get_app_sign(params);
    let res: reqwest::Response = CLIENT.post(url).form(&signed_params).send().await?;
    let resp = res.json().await?;
    check_resp(&resp)?;
    let temp_tv_auth_code = resp["data"]["auth_code"]
        .as_str()
        .ok_or(HttpError::Biz("Failed to get auth_code".to_string()))?;
    let mut auth_code_guard = TEMP_TV_AUTH_CODE.lock().unwrap();
    auth_code_guard.clear();
    auth_code_guard.push_str(temp_tv_auth_code);
   

    let url = resp["data"]["url"]
    .as_str()
    .ok_or(HttpError::Biz("Failed to get auth_code".to_string()))?;
    info!("get_tv_login_qr_code: {}", url);
    R::Ok(url.to_string())
}

#[tokio::test]
async fn test_tv_login_qr_code() {
    crate::utils::log::init_log();
    println!("开始获取二维码");
    let get_tv_login_qr_code = get_tv_login_qr_code().await;
    match get_tv_login_qr_code {
        Ok(_) => println!("获取二维码成功"),
        Err(e) => println!("获取二维码失败: {:?}", e),
    }
}

use std::time::{Duration, SystemTime, UNIX_EPOCH};

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

/**
 * 获得tv端 扫码登陆的结果
 */
pub async fn get_tv_qr_code_scan_result() -> R<serde_json::Value> {
    if TEMP_TV_AUTH_CODE.lock().unwrap().is_empty() {
        return R::Ok(json!("请先扫描二维码再获取结果"));
    }
    let url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/poll";

    let mut params: BTreeMap<&'static str, String> = BTreeMap::new();
    params.insert("appkey", constans::THIRD_PART_APPKEY.to_string());
    params.insert("appsec", constans::THIRD_PART_APPSEC.to_string());
    params.insert("auth_code", TEMP_TV_AUTH_CODE.lock().unwrap().to_string());
    params.insert("local_id", "0".to_string());
    params.insert("ts", get_ts().to_string());
    let response = common_post_form(url, get_app_sign(params)).await?;

    check_resp(&response)?;

    let code = response["code"]
        .as_i64()
        .ok_or(HttpError::Biz("Failed to get code".to_string()))?;

    let result = match code {
        0 => {
            info!("扫码成功");
            let data = &response["data"];
            let mid = data["mid"].as_str().unwrap_or("");
            let access_token = data["access_token"].as_str().unwrap_or("");

            let mut cookie_map = HashMap::new();
            data["cookie_info"]["cookies"].as_array().map(|f| {
                f.iter().for_each(|item| {
                    if let Some(name) = item["name"].as_str() {
                        let value = item["value"].as_str().unwrap_or("");
                        cookie_map.insert(name.to_string(), value);
                    }
                });
            });

            TEMP_TV_AUTH_CODE.lock().unwrap().clear();

            //TODO 存储数据
            // configService.updateMid(mid);
            // configService.updateAccessKey(accessToken);
            //立即持久化一次cookie
            // cookieHeaderDataService.replaceRefreshCookie(cookieMap);

            json!(format!("登陆成功！ accessKey={:#?}", access_token))
        },
        86038=>{
            TEMP_TV_AUTH_CODE.lock().unwrap().clear();
            json!("二维码失效，请重新扫描")
        },
        86039=>{
            json!("二维码尚未确认")
        },
        86090=>{
            json!("请在手机端确认")
        },
        -404=>{
            json!("啥都木有")
        },
        -400=>{
            json!("请求错误")
        },
        -3=>{
            json!("API校验密匙错误")
        },
        _ => response,
    };

    return R::Ok(result);
}

/**
 * 携带header和cookie的通用post表单请求
 */
pub async fn common_post_form(
    url: &str,
    param_map: BTreeMap<&'static str, String>,
) -> R<serde_json::Value> {
    let mut req = CLIENT.post(url);

    //TODO 读取数据库中的header
    let hash_map: HashMap<String, String> = HashMap::new();
    for (k, v) in &hash_map {
        req = req.header(k, v);
    }

    //TODO 读取数据库中的cookie
    let cookie_jar: HashMap<String, String> = HashMap::new();
    let cookie_str: String = cookie_jar
        .iter()
        .map(|(k, v)| format!("{}={}", k, v))
        .collect::<Vec<String>>()
        .join("; ");

    let response = req
        .timeout(Duration::from_secs(10))
        .header(header::COOKIE, cookie_str)
        .form(&param_map)
        .send()
        .await?;

    //TODO 保存cookie
    response.cookies().for_each(|c| {
        info!("cookie: {:#?}", c);
    });

    let json = response.json().await?;

    R::Ok(json)
}
