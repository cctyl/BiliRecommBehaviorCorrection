use std::{collections::HashMap, hash::Hash};

use axum::extract::Path;
use axum::{Json, Router, debug_handler, extract::Query};
use log::info;
use rbatis::Page;
use rbatis::rbdc::{Date, DateTime};
use rbs::value;
use serde::{Deserialize, Serialize};

use crate::entity::dtos::{self, PageDTO};
use crate::entity::enumeration::{AccessType, Classify, DictType, MediaType};
use crate::entity::models::{CookieHeaderData, Dict};
use crate::service::dict_service;
use crate::utils::id::generate_id;
use crate::{
    api::bili,
    app::{
        database::CONTEXT,
        response::{OkRespExt, RR, *},
    },
    service::cookie_header_data_service,
};

pub fn create_router() -> Router {
    Router::new()
        .route(
            "/list",
            axum::routing::get(get_list_by_dict_type_and_access_type),
        )
        .route("/{id}", axum::routing::get(get_by_id).delete(del))
        .route("/", axum::routing::post(add).put(update))
        .route("/add-stopword", axum::routing::post(add_stop_word))
        .route(
            "/batchRemoveAndUpdate",
            axum::routing::post(batch_remove_and_update),
        )
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DictDto {
    pub id: Option<String>,
    pub value: String,
    pub access_type: AccessType,
    pub dict_type: DictType,
    pub outer_id: Option<String>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    pub desc: Option<String>,
}
// dictVo from Dict
impl From<Dict> for DictDto {
    fn from(dict: Dict) -> Self {
        DictDto {
            id: Some(dict.id),
            value: dict.value,
            access_type: dict.access_type,
            dict_type: dict.dict_type,
            outer_id: dict.outer_id,
            created_date: dict.created_date,
            last_modified_date: dict.last_modified_date,
            desc: dict.desc_field,
        }
    }
}

impl From<DictDto> for Dict {
    fn from(dto: DictDto) -> Self {
        Dict {
            id: dto.id.unwrap_or_else(|| generate_id()), // 如果没有id，则生成一个新的ID
            value: dto.value,
            access_type: dto.access_type,
            dict_type: dto.dict_type,
            outer_id: dto.outer_id,
            created_date: dto.created_date.or (Some(DateTime::now())),
            last_modified_date: dto.last_modified_date.or (Some(DateTime::now())),
            desc_field: dto.desc,
        }
    }
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BatchRemoveAndUpdateParam {
    access_type: AccessType,
    dict_type: DictType,
}
/// 批量删除后新增词典
#[debug_handler]
pub async fn batch_remove_and_update(
    Query(BatchRemoveAndUpdateParam {
        dict_type,
        access_type,
    }): Query<BatchRemoveAndUpdateParam>,
    Json(dto): Json<Vec<DictDto>>,
) -> RR<Vec<DictDto>> {

    
    let result = dict_service::batch_remove_and_update(dict_type, access_type, dto).await?;

    RR::success(result)
}

///添加停顿词
#[debug_handler]
pub async fn add_stop_word(Json(v): Json<Vec<String>>) -> RR<()> {
    dict_service::add_stop_word(v).await?;

    RR::success(())
}

/// 修改数据
#[debug_handler]
pub async fn update(Json(dto): Json<DictDto>) -> RR<()> {
    if dto.id.is_none() {
        RR::fail("id 不能为空")
    } else {
        let table: Dict = dto.into();
        Dict::update_by_map(&CONTEXT.rb, &table, value! {"id":table.id.clone()}).await?;
        RR::success(())
    }
}

/// 新增数据
#[debug_handler]
pub async fn add(Json(dto): Json<DictDto>) -> RR<bool> {
    let table: Dict = dto.into();

    RR::success(dict_service::add_dict(table).await?)
}

/// 删除数据
#[debug_handler]
pub async fn del(Path(id): Path<String>) -> RR<()> {
    Dict::delete_by_map(&CONTEXT.rb, value! {"id":id}).await?;
    RR::success(())
}

/**
 * 根据id查询
 */
#[debug_handler]
pub async fn get_by_id(Path(id): Path<String>) -> RR<DictDto> {
    let cookie_header_data = Dict::select_by_map(&CONTEXT.rb, value! {"id":id})
        .await?
        .pop();
    if cookie_header_data.is_none() {
        return RR::fail("数据不存在");
    } else {
        RR::success(cookie_header_data.unwrap().into())
    }
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DictListQueryParam {
    pub dict_type: String,
    pub access_type: String,
}

/**
 * 根据DictType 和 AccessType 查询dict列表
 */
#[debug_handler]
pub async fn get_list_by_dict_type_and_access_type(
    Query(DictListQueryParam {
        access_type,
        dict_type,
    }): Query<DictListQueryParam>,
) -> RR<HashMap<String, Vec<DictDto>>> {
    let mut hash_map = HashMap::new();
    let r: Vec<Dict> =
        dict_service::get_list_by_dict_type_and_access_type(access_type, dict_type).await?;
    let r = r
        .into_iter()
        .map(|item| item.into())
        .collect::<Vec<DictDto>>();

    hash_map.insert(String::from("list"), r);
    RR::success(hash_map)
}
