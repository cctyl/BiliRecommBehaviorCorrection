use std::{collections::HashMap, hash::Hash};

use log::info;
use rbs::value;
use reqwest::header;

use crate::{
    app::{
        database::CONTEXT,
        global::{GLOBAL_STATE, GlobalState, GlobalStateHandler},
        response::R,
    },
    entity::{
        enumeration::{Classify, MediaType},
        models::CookieHeaderData,
    },
};

pub struct CookieHeaderDataService;

pub static CookieHeaderDataServiceImpl: CookieHeaderDataService = CookieHeaderDataService;

impl GlobalStateHandler for CookieHeaderDataService {
    async fn handle(&self, state: &mut GlobalState) {
        state.common_header_map =
            get_map_by_classify_and_media_type(&Classify::REQUEST_HEADER, &MediaType::GENERAL)
                .await
                .unwrap();
    }
}

pub async fn get_common_header_map() {
    CookieHeaderDataServiceImpl.process().await;
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
