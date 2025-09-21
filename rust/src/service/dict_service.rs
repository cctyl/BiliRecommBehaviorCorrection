use std::collections::HashSet;

use anyhow::Context;
use rbatis::{impl_select, rbdc::DateTime};
use rbs::value;

use crate::{
    app::{database::CONTEXT, error::HttpError, response::R},
    entity::{
        enumeration::{AccessType, DictType},
        models::Dict,
    }, utils::id::generate_id,
};
use rbatis::rbatis_codegen::IntoSql;
// impl_select!(Dict{find_by_dict_type_and_access_type_and_value_in(access_type: AccessType, dict_type: DictType, values:&[String]) => r#"
//         ` where access_type = #{access_type} and dict_type = #{dict_type} and value in ` ${values.sql()}
//     "#
// });
/**
 * 根据DictType 和 AccessType 获取Dict列表
 */
pub(crate) async fn get_list_by_dict_type_and_access_type(
    access_type: String,
    dict_type: String,
) -> R<Vec<Dict>> {
    Dict::select_by_map(
        &CONTEXT.rb,
        value! {"dict_type":dict_type,"access_type":access_type},
    )
    .await
    .map_err(HttpError::from)
}

/// 添加停顿词
pub(crate) async fn add_stop_word(v: Vec<String>) -> R<()> {
    let mut dicts = Dict::select_by_map(
        &CONTEXT.rb,
        value! {
            "value":&v,
             "access_type":AccessType::OTHER,
             "dict_type":DictType::STOP_WORDS,
        }
    )
    .await?
    .into_iter()
    .map(|f| f.value)
    .collect::<Vec<String>>();


    dicts.extend(v);
    let mut words:HashSet<String> = dicts.into_iter().collect();
    let unique_words: Vec<String> = words.into_iter().collect();
    let keyword_to_dict = keyword_to_dict(unique_words,  DictType::STOP_WORDS, AccessType::OTHER,None);
    Dict::insert_batch(
        &CONTEXT.rb,
        &keyword_to_dict,
        100
    ).await?;
    R::Ok(())
 
}
pub fn keyword_to_dict(
    value_collection: Vec<String>,
    dict_type: DictType,
    access_type: AccessType,
    outer_id: Option<String>,
) -> Vec<Dict> {
    value_collection
        .into_iter()
        .map(|s| Dict {
            access_type: Some(access_type.clone()),
            dict_type: Some(dict_type.clone()),
            outer_id: outer_id.clone(),
            value: s,
            id:generate_id(),
            last_modified_date:Some(DateTime::now()),
            created_date:Some(DateTime::now()),
            desc_field:None,
            
        })
        .collect()
}