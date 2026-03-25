use std::{collections::HashMap, hash::Hash};

use axum::{debug_handler, extract::Query, Json, Router};
use axum::extract::Path;
use log::info;
use rbatis::Page;
use rbs::value;
use serde::{Deserialize, Serialize};

use crate::domain::dtos::PageDTO;
use crate::domain::enumeration::{Classify, MediaType};
use crate::domain::cookie_header_data::CookieHeaderData;
use crate::utils::id::generate_id;
use crate::{
    api::bili, app::{
        config::CC,
        response::{OkRespExt, RR, *},
    }, service::{ cookie_header_data_service}
};



pub fn create_router() -> Router {
    Router::new()
        .route("/list/{page}/{limit}", axum::routing::get(get_list))
        .route("/{id}", axum::routing::get(get_by_id).delete(del))
        .route("/", axum::routing::post(add).put(update))

}


#[derive(Clone, Debug, Serialize, Deserialize)]

pub struct CookieHeaderDataAddDTO {
    pub id: Option<String>,
    pub url: Option<String>,
    pub ckey: String,
    pub cvalue: String,
    pub classify: Option<Classify>,
    pub media_type: Option<MediaType>,
}

//CookieHeaderDataAddDTO from CookieHeaderData
impl From<CookieHeaderDataAddDTO> for CookieHeaderData {
    fn from(param: CookieHeaderDataAddDTO) -> Self {
        CookieHeaderData {
            id: param.id.unwrap_or(generate_id()),
            url: param.url,
            ckey: param.ckey,
            cvalue: param.cvalue,
            classify: param.classify,
            media_type: param.media_type,
        }
    }
}

/**
 * 修改数据
 */
#[debug_handler]
pub async fn update(Json(param):Json<CookieHeaderDataAddDTO>)->RR<CookieHeaderData>{
    let table: CookieHeaderData = param.into();

    let result = CookieHeaderData::update_by_map(&CC.rb, &table, value! {"id":table.id.clone()}).await?;
    
    RR::success(table)
}

/**
 * 新增数据
 */
#[debug_handler]
pub async fn add(Json(param):Json<CookieHeaderDataAddDTO>)->RR<CookieHeaderData>{


    let table: CookieHeaderData = param.into();

    let result = CookieHeaderData::insert(&CC.rb, &table).await?;
    
    RR::success(table)
}


/**
 * 根据id删除
 */
#[debug_handler]
pub async fn del(Path(param):Path<String>)->RR<()>{


    if  param.is_empty() {
        RR::fail("id 不能为空")
    }else {
        CookieHeaderData::delete_by_map( &CC.rb, value!{"id":param}).await?;
        RR::success(())
    }

}

#[derive(Clone, Debug, Serialize, Deserialize)]

pub struct CookieHeaderDataListDTO {
    pub id: String,
    pub url: Option<String>,
    pub ckey: String,
    pub cvalue: String,
    pub classify: Option<Classify>,
    pub media_type: Option<MediaType>,
}
//CookieHeaderDataListDTO from CookieHeaderData
impl From<CookieHeaderData> for CookieHeaderDataListDTO {
    fn from(param: CookieHeaderData) -> Self {
        CookieHeaderDataListDTO {
            id: param.id,
            url: param.url,
            ckey: param.ckey,
            cvalue: param.cvalue,
            classify: param.classify,
            media_type: param.media_type,
        }
    }
}
#[debug_handler]
pub async fn get_list(Path((page,limit)):Path<(u64,u64)>)->RR<PageDTO<CookieHeaderDataListDTO>>{

    let page_list = cookie_header_data_service::page_list(page, limit).await?;
    let r:PageDTO<CookieHeaderDataListDTO> = PageDTO::<CookieHeaderDataListDTO>::convert_from(page_list);
    RR::success(r)
}

#[debug_handler]
pub async fn get_by_id(Path(id):Path<String>)->RR<CookieHeaderDataListDTO>{

    let cookie_header_data = CookieHeaderData::select_by_map(&CC.rb, value!{"id":id}).await?.pop();
    if cookie_header_data.is_none(){
        return RR::fail("数据不存在");
    }else {
        RR::success(cookie_header_data.unwrap().into())
    }
}