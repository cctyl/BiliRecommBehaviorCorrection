use std::collections::HashMap;
use std::sync::{LazyLock, Mutex};

use crate::app::database::CONTEXT;
use crate::app::global::{GLOBAL_STATE, GlobalStateHandler};
use crate::app::response::R;
use crate::app::{constans, error::HttpError};
use crate::entity::dtos::{PageBean, UserSubmissionVideo};
use crate::entity::models::{Config, CookieHeaderData};
use crate::service::config_service;
use crate::service::cookie_header_data_service::{self, init_common_header_map, init_header};
use crate::utils::data_util;
use crate::utils::http::CLIENT;
use crate::utils::id::generate_id;
use anyhow::Context;
use hex;
use log::{error, info};
use rbs::value;
use rbs::value::map::ValueMap;
use reqwest::header;
use serde_json::json;
use std::collections::BTreeMap;
use urlencoding::encode;
//扫码登陆的code
static TEMP_TV_AUTH_CODE: Mutex<String> = Mutex::new(String::new());

async fn check_resp(value: &serde_json::Value) -> R<()> {
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
                    // 登陆过期，清除accessKey
                    config_service::del_by_name(constans::BILI_ACCESS_KEY).await?;
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
    let mut params: Vec<(String, String)> = vec![
        (
            "appkey".to_string(),
            constans::THIRD_PART_APPKEY.to_string(),
        ),
        (
            "appsec".to_string(),
            constans::THIRD_PART_APPSEC.to_string(),
        ),
        ("local_id".to_string(), "0".to_string()),
        ("ts".to_string(), get_ts().to_string()),
    ];
    let signed_params = get_app_sign(params);
    let res: reqwest::Response = CLIENT.post(url).form(&signed_params).send().await?;
    let resp = res.json().await?;
    check_resp(&resp).await?;
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

use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

pub fn get_ts() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_else(|_| std::time::Duration::from_secs(0))
        .as_secs()
}

/**
 * 获取签名后的参数，返回一个包含签名的map
 */
pub fn get_app_sign(mut params: Vec<(String, String)>) -> Vec<(String, String)> {
    let input = params.clone();
    let mut params: BTreeMap<String, String> = params.into_iter().collect();
    // 插入默认 appkey
    params
        .entry("appkey".to_string())
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
    params.insert("sign".to_string(), sign);

    params.into_iter().collect()
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

/**
 * 获得tv端 扫码登陆的结果
 */
pub async fn get_tv_qr_code_scan_result() -> R<serde_json::Value> {
    if TEMP_TV_AUTH_CODE.lock().unwrap().is_empty() {
        return R::Ok(json!("请先扫描二维码再获取结果"));
    }
    let url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/poll";

    let mut params = vec![
        (
            "appkey".to_string(),
            constans::THIRD_PART_APPKEY.to_string(),
        ),
        (
            "appsec".to_string(),
            constans::THIRD_PART_APPSEC.to_string(),
        ),
        (
            "auth_code".to_string(),
            TEMP_TV_AUTH_CODE.lock().unwrap().to_string(),
        ),
        ("local_id".to_string(), "0".to_string()),
        ("ts".to_string(), get_ts().to_string()),
    ];

    let response = common_post_form(url, get_app_sign(params)).await?;

    check_resp(&response).await?;

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

            // 存储数据
            config_service::update_mid(mid).await?;
            config_service::update_access_key(access_token).await?;
            //立即持久化一次cookie
            cookie_header_data_service::replace_refresh_cookie(cookie_map).await?;

            json!(format!("登陆成功！ accessKey={:#?}", access_token))
        }
        86038 => {
            TEMP_TV_AUTH_CODE.lock().unwrap().clear();
            json!("二维码失效，请重新扫描")
        }
        86039 => {
            json!("二维码尚未确认")
        }
        86090 => {
            json!("请在手机端确认")
        }
        -404 => {
            json!("啥都木有")
        }
        -400 => {
            json!("请求错误")
        }
        -3 => {
            json!("API校验密匙错误")
        }
        _ => response,
    };

    return R::Ok(result);
}

/**
 * 携带header和cookie的通用post表单请求
 */
pub async fn common_post_form(url: &str, param_map: Vec<(String, String)>) -> R<serde_json::Value> {
    let mut req = CLIENT.post(url);

    // init_common_header_map().await?;
    req = init_header.processr((url, req)).await?;

    // 读取数据库中的cookie
    let mut cookie_jar: HashMap<String, String> =
        cookie_header_data_service::get_refresh_cookie_map().await?;
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

    //保存cookie
    response.cookies().for_each(|c| {
        info!("cookie: {:#?}", c);
        cookie_jar.insert(c.name().to_string(), c.value().to_string());
    });

    cookie_header_data_service::replace_refresh_cookie(cookie_jar).await?;
    let json = response.json().await?;

    R::Ok(json)
}

/**
 * 获取cookie字符串
 */
pub async fn get_cookie_str() -> R<(HashMap<String, String>, String)> {
    // 读取数据库中的cookie
    let mut cookie_jar: HashMap<String, String> =
        cookie_header_data_service::get_refresh_cookie_map().await?;
    let cookie_str: String = cookie_jar
        .iter()
        .map(|(k, v)| format!("{}={}", k, v))
        .collect::<Vec<String>>()
        .join("; ");

    R::Ok((cookie_jar, cookie_str))
}

/**
 * 携带header和cookie的通用get请求
 */
pub async fn common_get(url: &str, param_map: Vec<(String, String)>) -> R<serde_json::Value> {
    let mut req = CLIENT.get(url);

    // init_common_header_map().await?;
    req = init_header.processr((url, req)).await?;

    //读取cookie
    let (cookie_jar, cookie_str) = get_cookie_str().await?;

    let response: reqwest::Response = req
        .timeout(Duration::from_secs(10))
        .header(header::COOKIE, cookie_str)
        .query(&param_map)
        .send()
        .await?;

    //保存cookie
    update_cookie(&response, cookie_jar).await?;

    let json = response.json().await?;

    R::Ok(json)
}

/// 封装通用的get
/// 携带cookie、ua、参数的url编码
/// 记忆cookie
/// 添加额外的请求头
pub async fn common_get_other_header(
    url: &str,
    param_map: Vec<(&str, String)>,
    other_map: Vec<(&str, String)>,
) -> R<serde_json::Value> {
    let mut req = CLIENT.get(url);

    // init_common_header_map().await?;
    req = init_header.processr((url, req)).await?;

    //额外新增的header
    for (k, v) in other_map.into_iter() {
        req = req.header(k, v);
    }

    //读取cookie
    let (cookie_jar, cookie_str) = get_cookie_str().await?;

    //TODO 记得打开
    let response: reqwest::Response = req
        .timeout(Duration::from_secs(10))
        .header(header::COOKIE, cookie_str)
        .query(&param_map)
        .send()
        .await?;

    //保存cookie
    update_cookie(&response, cookie_jar).await?;

    let json = response.json().await?;

    R::Ok(json)
}

/// 更新cookie
pub async fn update_cookie(
    response: &reqwest::Response,
    mut cookie_jar: HashMap<String, String>,
) -> R<()> {
    //保存cookie
    response.cookies().for_each(|c| {
        info!("cookie: {:#?}", c);
        cookie_jar.insert(c.name().to_string(), c.value().to_string());
    });

    cookie_header_data_service::replace_refresh_cookie(cookie_jar).await
}

/// 获取accessKey
/// 若不存在，则抛出异常，提示重新登陆
pub async fn get_access_key(refresh: bool) -> R<String> {
    // 如果需要刷新，或者缓存中不存在，则更新一次
    // 否则从缓存中取出
    // let mut where_map = ValueMap::new();
    // where_map.insert(rbs::Value::String("name".to_string()), rbs::Value::String(constans::BILI_ACCESS_KEY.to_string()));
    // let config_result = Config::select_by_map(&CONTEXT.rb, rbs::Value::Map(where_map)).await?;
    let mut config_result: Vec<Config> = Config::select_by_map(
        &CONTEXT.rb,
        value! {"name":constans::BILI_ACCESS_KEY.to_string()},
    )
    .await?;

    if refresh {
        // 记录登陆状态
        // 新版本不再支持通过cookie获取
        // 提示让用户扫码登陆
        return Err(HttpError::Biz("登录已过期，请重新扫码登录".to_string()));
    } else {
        let c = config_result
            .pop()
            .ok_or(HttpError::Biz("登录已过期，请重新扫码登录".to_string()))?;
        match c.value {
            Some(access_key) => R::Ok(access_key),
            None => Err(HttpError::Biz("access_key 为空，请重新登录".to_string())),
        }
    }
}

#[tokio::test]
async fn test_get_access_key() {
    crate::init().await;

    let access_key = get_access_key(false).await;
    println!("access_key={:#?}", access_key);
}

/**
 * 获取当前用户信息
 */
pub async fn get_user_info() -> R<serde_json::Value> {
    let url = "https://app.bilibili.com/x/v2/account/myinfo";

    let access_key = get_access_key(false).await?;

    let mut params = vec![
        ("access_key".to_string(), access_key),
        (
            "appkey".to_string(),
            constans::THIRD_PART_APPKEY.to_string(),
        ),
        ("ts".to_string(), get_ts().to_string()),
    ];

    let response = common_get(url, get_app_sign(params)).await?;

    check_resp(&response).await?;

    // 更新mid到配置中
    if let Some(mid) = response["data"]["mid"].as_str() {
        // 查找是否已存在mid配置
        // let mut where_map = ValueMap::new();
        // where_map.insert(rbs::Value::String("name".to_string()), rbs::Value::String(constans::MID_KEY.to_string()));
        // let existing_config = Config::select_by_map(&CONTEXT.rb, rbs::Value::Map(where_map)).await?;
        let mut existing_config =
            Config::select_by_map(&CONTEXT.rb, value! {"name":constans::MID_KEY.to_string()})
                .await?;

        if existing_config.is_empty() {
            // 创建新的mid配置
            let new_config = Config {
                id: generate_id(),
                name: constans::MID_KEY.to_string(),
                value: Some(mid.to_string()),
                expire_second: None,
                created_date: Some(rbatis::rbdc::types::DateTime::now()),
                last_modified_date: Some(rbatis::rbdc::types::DateTime::now()),
            };
            Config::insert(&CONTEXT.rb, &new_config).await?;
        } else {
            // 更新现有的mid配置
            let mut config = existing_config.pop().unwrap();
            config.value = Some(mid.to_string());
            config.last_modified_date = Some(rbatis::rbdc::types::DateTime::now());

            // 使用 update_by_map 方法
            // let mut update_where = ValueMap::new();
            // update_where.insert(rbs::Value::String("name".to_string()), rbs::Value::String(constans::MID_KEY.to_string()));
            // Config::update_by_map(&CONTEXT.rb, &config, rbs::Value::Map(update_where)).await?;
            Config::update_by_map(&CONTEXT.rb, &config, value! {"id":&config.id}).await?;
            info!("更新mid: {}", mid);
        }
    }

    Ok(response)
}

//测试get_user_info
#[tokio::test]
async fn test_get_user_info() {
    crate::init().await;
    let result = get_user_info().await;
    println!("result={:#?}", result);
}

/**
 *
 * 查询观看历史
 */
pub async fn get_history() -> R<serde_json::Value> {
    let url = "https://api.bilibili.com/x/web-interface/history/cursor?ps=1&pn=1";
    common_get(url, vec![]).await
}

/// 查询用户投稿的视频
///
/// # 参数
/// * `mid` - 用户id
/// * `pageNumber` - 页码 1开始
/// * `keyword` - 搜索关键词
pub(crate) async fn search_user_submission_video(
    user_id: &str,
    page_num: i32,
    keyword: &str,
) -> R<PageBean<UserSubmissionVideo>> {
    info!("开始发请求1");
    let url = "https://api.bilibili.com/x/space/wbi/arc/search";

    let wbi_map = vec![
        ("mid", user_id.to_string()),
        ("ps", "30".to_string()),
        ("tid", "0".to_string()),
        ("pn", page_num.to_string()),
        ("keyword", keyword.to_string()),
        ("order", "pubdate".to_string()),
        ("platform", "web".to_string()),
        ("web_location", "1550101".to_string()),
        ("order_avoided", "true".to_string()),
    ];
    info!("开始发请求2");
    let other_map = vec![
        (
            "Referer",
            format!("https://space.bilibili.com/{user_id}/video"),
        ),
        ("Origin", "https://space.bilibili.com".to_string()),
    ];

    info!("开始发请求3");
    let wbi_result_map = get_wbi(false, wbi_map).await?;
    info!("wbi_result_map={:#?}", wbi_result_map);

    let response = common_get_other_header(url, wbi_result_map, other_map).await?;

    //=====================================================
    info!("response={:#?}", response);
    check_resp(&response).await?;

    let value = &response["data"]["list"]["vlist"];

    // 假设 value 是你的 serde_json::Value
    let video_list: Vec<UserSubmissionVideo> =
        serde_json::from_value(value.clone()).map_err(|e| {
            error!("解析视频列表失败: {}", e);
            HttpError::Biz("解析视频列表失败".to_string())
        })?;

    info!("video_list={:#?}", video_list);

    // 创建 PageBean 实例
    let page_bean = PageBean::with_data(
        video_list,
        response["data"]["page"]["count"].as_u64().unwrap_or(0),
        30, // 每页大小
        page_num as u64,
    );

    info!("page_bean={:#?}", page_bean);
    R::Ok(page_bean)
}

// 为请求参数进行 wbi 签名
fn encode_wbi(
    params: Vec<(&str, String)>,
    (img_key, sub_key): (String, String),
) -> (String, String) {
    let cur_time = match SystemTime::now().duration_since(UNIX_EPOCH) {
        Ok(t) => t.as_secs(),
        Err(_) => panic!("SystemTime before UNIX EPOCH!"),
    };
    _encode_wbi(params, (img_key, sub_key), cur_time)
}

fn _encode_wbi(
    mut params: Vec<(&str, String)>,
    (img_key, sub_key): (String, String),
    timestamp: u64,
) -> (String, String) {
    let mixin_key = get_mixin_key((img_key + &sub_key).as_bytes());
    // 添加当前时间戳
    params.push(("wts", timestamp.to_string()));
    // 重新排序
    params.sort_by(|a, b| a.0.cmp(b.0));
    // 拼接参数
    let query = params
        .iter()
        .map(|(k, v)| format!("{}={}", get_url_encoded(k), get_url_encoded(v)))
        .collect::<Vec<_>>()
        .join("&");
    // 计算签名
    let web_sign = format!("{:?}", md5::compute(query + &mixin_key));
    (web_sign, timestamp.to_string())
}
fn get_url_encoded(s: &str) -> String {
    s.chars()
        .filter_map(|c| match c.is_ascii_alphanumeric() || "-_.~".contains(c) {
            true => Some(c.to_string()),
            false => {
                // 过滤 value 中的 "!'()*" 字符
                if "!'()*".contains(c) {
                    return None;
                }
                let encoded = c
                    .encode_utf8(&mut [0; 4])
                    .bytes()
                    .fold("".to_string(), |acc, b| acc + &format!("%{:02X}", b));
                Some(encoded)
            }
        })
        .collect::<String>()
}

/// 获取wbi签名的字符串，返回一个拼接好的urlQuery: wts=xxxx&w_rid=xxxx
/// 返回值：BTreeMap<&'static str, String>
async fn get_wbi(refresh: bool, mut params: Vec<(&str, String)>) -> R<Vec<(&str, String)>> {
    let mut img_key: Option<String> = config_service::get_img_key().await?;
    let mut sub_key: Option<String> = config_service::get_sub_key().await?;
    if (refresh || img_key.is_none() || sub_key.is_none()) {
        let url = "https://api.bilibili.com/x/web-interface/nav";
        let response = common_get(url, vec![]).await?;
        check_resp(&response).await?;

        let value = &response["data"]["wbi_img"];

        if let Some(img_url) = value["img_url"].as_str() {
            let img_url = img_url.split("/").last().unwrap_or("").replace(".png", "");
            img_key = Some(img_url);
        } else {
            error!("img_url is none");
        }

        if let Some(sub_url) = value["sub_url"].as_str() {
            let sub_url = sub_url.split("/").last().unwrap_or("").replace(".png", "");
            sub_key = Some(sub_url.to_string());
        } else {
            error!("sub_url is none");
        }

        config_service::update_wbi(&img_key, &sub_key).await?;
    }

    let (w_rid, wts) = encode_wbi(
        params.clone(),
        (
            img_key.expect("img_key is none"),
            sub_key.expect("sub_key is none"),
        ),
    );

    params.push(("w_rid", w_rid));
    params.push(("wts", wts));

    R::Ok(params)
}

const MIXIN_KEY_ENC_TAB: [usize; 64] = [
    46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49, 33, 9, 42, 19, 29,
    28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25,
    54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52,
];

// 对 imgKey 和 subKey 进行字符顺序打乱编码
fn get_mixin_key(orig: &[u8]) -> String {
    MIXIN_KEY_ENC_TAB
        .iter()
        .take(32)
        .map(|&i| orig[i] as char)
        .collect::<String>()
}

#[cfg(test)]
mod tests {
    use std::{
        any::Any,
        collections::{BTreeMap, HashMap},
        hash::Hash,
    };

    use crate::api::bili::{generate_md5, get_mixin_key};

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //TODO 在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_get_mixin_key() {
        //第一句必须是这个
        crate::init().await;

        let img_key = "7cd084941338484aae1ad9425b84077c";
        let sub_key = "4932caff0ff746eab6f01bf08b70ac45";

        let mixin_key = get_mixin_key((img_key.to_string() + sub_key).as_bytes());

        println!("mixin_key={}", mixin_key);
        assert_eq!(mixin_key, "ea1db124af3c7062474693fa704f4ff8");

        let combined = format!("{}{}", "aaa", mixin_key);
        let wbi_sign = generate_md5(&combined);

        println!("wbi_sign={}", wbi_sign);
        //最后一句必须是这个
        log::logger().flush();
    }

    //测试search_user_submission_video
    #[tokio::test]
    async fn test_search_user_submission_video() {
        crate::init().await;

        log::info!("开始测试search_user_submission_video");
        let result = super::search_user_submission_video("414702734", 1, "1").await;

        log::logger().flush();
    }
}
