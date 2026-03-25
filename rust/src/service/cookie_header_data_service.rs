use std::{
    collections::HashMap,
    hash::Hash,
    sync::{Arc, LazyLock, Mutex, RwLock},
    time::Duration,
};

use log::{error, info};
use rbatis::PageRequest;
use rbs::value;
use reqwest::header;
use tokio::time::sleep;

use crate::{
    app::{
        config::CC,
        global::{GlobalState, GlobalStateHandler, GLOBAL_STATE},
        response::R,
    }, domain::{
        dtos::PageDTO, enumeration::{Classify, MediaType}, cookie_header_data::CookieHeaderData
    }, service::cookie_header_data_service, utils::{data_util, id::generate_id}
};

pub async fn remove_by_classify_and_media_type(
    classify: &Classify,
    media_type: &MediaType,
) -> R<()> {


    CookieHeaderData::delete_by_map(&CC.rb, 
        value! {"classify": classify, "media_type": media_type}
    ).await?;


    R::Ok(())
}

//replaceRefreshCookie
pub async fn replace_refresh_cookie<K, V>(map: HashMap<K, V>) -> R<()>
where
    K: Into<String>,
    V: Into<String>,
{

    let map: HashMap<String, String> = map
    .into_iter()
    .map(|(k, v)| (k.into(), v.into()))
    .collect();
    remove_all_refresh_cookie().await?;
    save_refresh_cookie_map(map).await?;

    R::Ok(())
}

//saveRefreshCookieMap
pub async fn save_refresh_cookie_map(
    map: HashMap<String, String>,
) -> R<()> {

    let list = map_to_list(map,Classify::COOKIE,MediaType::TIMELY_UPDATE);
    CookieHeaderData::insert_batch(&CC.rb, &list, 100).await?;
    Ok(())
}



// 将 HashMap 转换为 CookieHeaderData 列表
fn map_to_list(
    map: HashMap<String, String>,
    classify: Classify,
    media_type: MediaType
) -> Vec<CookieHeaderData> {
    map.into_iter()
        .map(|(key, value)| {
             CookieHeaderData { 
                    id: generate_id(),
                    url:None,
                    ckey: key, 
                    cvalue: value,
                    classify: Some(classify.clone()), 
                    media_type: Some(media_type.clone())
            }
          
        })
        .collect()
}

// removeAllRefreshCookie
pub async fn remove_all_refresh_cookie() -> R<()> {
    remove_by_classify_and_media_type(&Classify::COOKIE, &MediaType::TIMELY_UPDATE).await
}

pub async fn get_refresh_cookie_map() -> R<HashMap<String, String>> {
    get_map_by_classify_and_media_type(&Classify::COOKIE, &MediaType::TIMELY_UPDATE).await
}
//test get_refresh_cookie_map
#[tokio::test]
async fn test_get_refresh_cookie_map() {
    crate::init().await;
    let map = get_refresh_cookie_map().await.unwrap();
    info!("{:?}", map);

    log::logger().flush();
}

pub async fn init_common_header_map() -> R<()> {
    {
        let read_guard = GLOBAL_STATE.read().await;
        if !read_guard.common_header_map.is_empty() {
            return R::Ok(());
        }
    }
    {
        info!("init_common_header_map");
        let mut write = GLOBAL_STATE.write().await;
        write.common_header_map =
            get_map_by_classify_and_media_type(&Classify::REQUEST_HEADER, &MediaType::GENERAL).await?;
    }

    R::Ok(())
}

pub struct init_header;
impl GlobalStateHandler<(&str, reqwest::RequestBuilder), reqwest::RequestBuilder> for init_header {
    async fn read(
        &self,
        state: &GlobalState,
        args: (&str, reqwest::RequestBuilder),
    ) -> R<reqwest::RequestBuilder> {
        let (url, mut req) = args;
        let mut hash_map = state.common_header_map.clone();
        for (k, v) in hash_map {
            req = req.header(k, v);
        }
        req = req.header(String::from("Host"), data_util::get_host(url));
        R::Ok(req)
    }
}

/**
 * 根据分类和用途查询数据
 */
pub async fn find_by_classify_and_media_type(
    classify: &Classify,
    media_type: &MediaType,
) -> R<Vec<CookieHeaderData>> {
    let cookie_header_datas = CookieHeaderData::select_by_map(
        &CC.rb,
        value! {
        "classify": classify,
        "media_type": media_type
        },
    )
    .await?;

    R::Ok(cookie_header_datas)
}
#[tokio::test]
async fn test_find_by_classify_and_media_type() {
    crate::init().await;
    let cookie_header_datas =
        find_by_classify_and_media_type(&Classify::COOKIE, &MediaType::GENERAL).await;
    println!("{:?}", cookie_header_datas);

    log::logger().flush();
}

pub async fn get_map_by_classify_and_media_type(
    classify: &Classify,
    media_type: &MediaType,
) -> R<HashMap<String, String>> {
    let cookie_header_datas = find_by_classify_and_media_type(classify, media_type).await?;

    let map: HashMap<String, String> = cookie_header_datas
        .into_iter()
        .map(|data| (data.ckey, data.cvalue))
        .collect();

    R::Ok(map)
}

//给 get_map_by_classify_and_mediaType 编写test
#[tokio::test]
async fn test_get_map_by_classify_and_media_type() {
    crate::init().await;
    let map = get_map_by_classify_and_media_type(&Classify::REQUEST_HEADER, &MediaType::GENERAL)
        .await
        .unwrap();
    info!("map={:?}", map);
    info!("map length={}", map.len());
    info!("aaa ->>{:#?}", map);

    log::logger().flush();
}

/**
 * 获取刷新cookie

 */
pub async fn get_refresh_cookie() -> R<HashMap<String, String>> {
   get_map_by_classify_and_media_type(&Classify::COOKIE, &MediaType::TIMELY_UPDATE).await
}

/**
 * 更新刷新cookie
 */
pub async fn update_refresh_cookie(cookie_str:String) ->  R<HashMap<String, String>> {
    let split_cookie = data_util::split_cookie(&cookie_str);
    
    let mut hash_map = get_refresh_cookie().await?;
    hash_map.extend(split_cookie);

    replace_refresh_cookie(hash_map.clone()).await?;

    R::Ok(hash_map)
}


/**
 * 分页查询
 */
pub(crate) async fn page_list(page: u64, limit: u64) -> R<PageDTO<CookieHeaderData>> {
    
    let page = CookieHeaderData::select_page(&CC.rb, &PageRequest::new(page, limit)).await?;

    R::Ok(page.into())

}
