use std::{collections::HashMap, hash::Hash};

use axum::extract::Path;
use axum::{Json, Router, debug_handler, extract::Query};
use log::info;
use rbatis::Page;
use rbatis::rbdc::{Date, DateTime};
use rbs::value;
use serde::{Deserialize, Serialize};

use crate::app::constans::DISLIKE_BY_USER_ID_TASK;
use crate::app::task_pool::{self, TASK_POOL};
use crate::entity::dtos::{self, PageDTO};
use crate::entity::enumeration::{AccessType, Classify, DictType, MediaType};
use crate::entity::models::{CookieHeaderData, Dict, Task};
use crate::service::{bili_service, dict_service, task_service};
use crate::utils::id::generate_id;
use crate::utils::thread_util::ThreadUtil;
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
        "/cache-train-result/{type}",
        axum::routing::put(put_cache_train_result),
    )
    .route("/disklike-by-uid", axum::routing::post(dislike_by_user_id))
    // .route("/", axum::routing::post(add).put(update))
}

// CacheTrainResultDTO 有两个字段，discardedId，selectedId，类型都是Vec<String>
#[derive(Debug, Deserialize, Serialize)]

pub struct CacheTrainResultDTO {
    pub discarded_id: Vec<String>,
    pub selected_id: Vec<String>,
}

/// 将缓存的结果存入
#[debug_handler]
pub async fn put_cache_train_result(
    Path(dict_type): Path<DictType>,
    Json(CacheTrainResultDTO {
        discarded_id,
        selected_id,
    }): Json<CacheTrainResultDTO>,
) -> RR<()> {
    if dict_type == DictType::KEYWORD {
        if !selected_id.is_empty() {
            //把缓存改为黑名单关键词
            dict_service::add_balck_dict_from_cache_by_id(selected_id).await?;
        }

        if !discarded_id.is_empty() {
            //把缓存改为忽略的关键词
            dict_service::update_access_type_and_dict_type_by_ids(
                &CONTEXT.rb,
                AccessType::BLACK,
                DictType::IGNORE_KEYWORD,
                &discarded_id,
            )
            .await?;
        }
    } else if dict_type == DictType::TAG {
        if !selected_id.is_empty() {
            //把缓存改为黑名单关键词
            dict_service::add_balck_dict_from_cache_by_id(selected_id).await?;
        }

        if !discarded_id.is_empty() {
            //把缓存改为忽略的关键词
            dict_service::update_access_type_and_dict_type_by_ids(
                &CONTEXT.rb,
                AccessType::BLACK,
                DictType::IGNORE_TAG,
                &discarded_id,
            )
            .await?;
        }
    }

    RR::success(())
}

#[derive(Debug, Deserialize)]
pub struct DislikeDTO {
    pub train: bool,
}
#[debug_handler]
pub async fn dislike_by_user_id(
    Query(DislikeDTO { train }): Query<DislikeDTO>,
    Json(user_id_list): Json<Vec<String>>,
) -> RR<String> {

   
   task_service::do_task(DISLIKE_BY_USER_ID_TASK.to_string(),async move || {
        //  具体的点踩代码
        let mut disklike_num = 0;
        for user_id in &user_id_list {
            
            info!("开始对{}用户的点踩",{user_id});
            disklike_num+= bili_service::disklike_by_user_id(user_id,train).await?;

            info!("完成对{}用户的点踩",{user_id});

            //休眠20秒
            ThreadUtil::s20().await;
        }
        info!("本次共对{}个用户{:#?}进行点踩，共点踩：{}个视频",user_id_list.len(),user_id_list,disklike_num);
        R::Ok(())
   }).await?;
   RR::success(String::from("添加任务成功"))
}
