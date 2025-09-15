use std::{
    collections::HashMap,
    hash::Hash,
    sync::{Arc, LazyLock, Mutex, RwLock},
    time::Duration,
};

use log::{error, info};
use rbs::value;
use reqwest::header;
use tokio::time::sleep;

use crate::{
    app::{
        database::CONTEXT,
        global::{GlobalState, GlobalStateHandler, GLOBAL_STATE},
        response::R,
    }, entity::{
        enumeration::{Classify, MediaType},
        models::CookieHeaderData,
    }, service::cookie_header_data, utils::{data_util, id::generate_id}
};

pub async fn remove_by_classify_and_media_type(
    classify: &Classify,
    media_type: &MediaType,
) -> R<()> {


    CookieHeaderData::delete_by_map(&CONTEXT.rb, 
        value! {"classify": classify, "media_type": media_type}
    ).await?;


    R::Ok(())
}

//replaceRefreshCookie
pub async fn replace_refresh_cookie(
    map: HashMap<String, String>,
) -> R<()> {


    remove_all_refresh_cookie().await?;
    save_refresh_cookie_map(map).await?;

    R::Ok(())
}

//saveRefreshCookieMap
pub async fn save_refresh_cookie_map(
    map: HashMap<String, String>,
) -> R<()> {

    let list = map_to_list(map,Classify::COOKIE,MediaType::TIMELY_UPDATE);
    CookieHeaderData::insert_batch(&CONTEXT.rb, &list, 100).await?;
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
            get_map_by_classify_and_media_type(&Classify::COOKIE, &MediaType::GENERAL).await?;
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
        //TODO 键值对都有问题
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
        &CONTEXT.rb,
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
    let map = get_map_by_classify_and_media_type(&Classify::COOKIE, &MediaType::TIMELY_UPDATE)
        .await
        .unwrap();
    info!("map length={}", map.len());
    info!("aaa ->>{:#?}", map);

    log::logger().flush();
}
