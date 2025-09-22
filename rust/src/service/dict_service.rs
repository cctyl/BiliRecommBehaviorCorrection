use std::collections::HashSet;

use anyhow::Context;
use rbatis::{impl_select, rbdc::DateTime};
use rbs::value;

use crate::{
    app::{database::CONTEXT, error::HttpError, response::R},
    entity::{
        enumeration::{AccessType, DictType},
        models::Dict,
    },
    handler::dict_handler::DictDto,
    utils::id::generate_id,
};
use rbatis::rbatis_codegen::IntoSql;

#[cfg(test)]
mod tests{
    use std::collections::HashSet;

    use anyhow::Context;
    use rbatis::{impl_select, rbdc::DateTime};
    use rbs::value;
    
    use crate::{
        app::{database::CONTEXT, error::HttpError, response::R}, entity::{
            enumeration::{AccessType, DictType},
            models::Dict,
        }, handler::dict_handler::DictDto, service::dict_service::{check_is_need_save, exist_by_access_type_and_dict_type_and_value}, utils::id::generate_id
    };
    use rbatis::rbatis_codegen::IntoSql;
    

    #[tokio::test]
    async fn test_exist_by_access_type_and_dict_type_and_value() {
        crate::init().await;
       
        
        let exist_by_access_type_and_dict_type_and_value = exist_by_access_type_and_dict_type_and_value(AccessType::BLACK,DictType::IGNORE_KEYWORD,"泰国").await.unwrap();
        assert_eq!(exist_by_access_type_and_dict_type_and_value,true);





        log::logger().flush();
    }



    // check_is_need_save
    #[tokio::test]
    async fn test_check_is_need_save() {
        crate::init().await;
       
        
        let r = check_is_need_save(AccessType::BLACK,DictType::KEYWORD,"泰国").await.unwrap();
        assert_eq!(r,false);
        let r = check_is_need_save(AccessType::BLACK,DictType::TAG,"泰国").await.unwrap();
        assert_eq!(r,false);





        log::logger().flush();
    }


}
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
        },
    )
    .await?
    .into_iter()
    .map(|f| f.value)
    .collect::<Vec<String>>();

    dicts.extend(v);
    let mut words: HashSet<String> = dicts.into_iter().collect();
    let unique_words: Vec<String> = words.into_iter().collect();
    let keyword_to_dict =
        keyword_to_dict(unique_words, DictType::STOP_WORDS, AccessType::OTHER, None);
    Dict::insert_batch(&CONTEXT.rb, &keyword_to_dict, 100).await?;
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
            access_type: access_type,
            dict_type: dict_type,
            outer_id: outer_id.clone(),
            value: s,
            id: generate_id(),
            last_modified_date: Some(DateTime::now()),
            created_date: Some(DateTime::now()),
            desc_field: None,
        })
        .collect()
}

/// 根据access_type 和 dict_type 删除数据
pub async fn remove_by_access_type_and_dict_type(
    access_type: AccessType,
    dict_type: DictType,
) -> R<()> {
    Dict::delete_by_map(
        &CONTEXT.rb,
        value! {
            "access_type":access_type,
            "dict_type":dict_type,
        },
    )
    .await?;

    R::Ok(())
}

/// 批量删除后新增词典
pub(crate) async fn batch_remove_and_update(
    dict_type: DictType,
    access_type: AccessType,
    mut dto: Vec<DictDto>,
) -> R<Vec<DictDto>> {
    remove_by_access_type_and_dict_type(access_type, dict_type).await?;

    for d in &mut dto {
        d.access_type = access_type;
        d.dict_type = dict_type;
        d.id = Some(generate_id());
    }

    let collect = dto
        .clone()
        .into_iter()
        .map(|mut d| d.into())
        .collect::<Vec<Dict>>();

    Dict::insert_batch(&CONTEXT.rb, &collect, 100).await?;

    R::Ok(dto)
}


/// 根据access_type 和 dict_type 和 value 查询是否存在
pub async fn exist_by_access_type_and_dict_type_and_value(
    access_type: AccessType,
    dict_type: DictType,
    value: &str,
) -> R<bool> {
    let dicts = Dict::select_by_map(&CONTEXT.rb, value!{
        "dict_type":DictType::IGNORE_KEYWORD,
        "access_type":AccessType::BLACK,
        "value":value
    }).await?;

    R::Ok(!dicts.is_empty())
}

/// 如果不在忽略的名单内则保存，否则不报存
pub async fn check_is_need_save(

    access_type: AccessType,
    dict_type: DictType,
    value: &str,
)->R<bool>{

    let result =  
    if access_type== AccessType::BLACK && dict_type==DictType::KEYWORD{
        exist_by_access_type_and_dict_type_and_value(AccessType::BLACK, DictType::IGNORE_KEYWORD, value).await?
    }else if access_type== AccessType::BLACK && dict_type== DictType::TAG {
        exist_by_access_type_and_dict_type_and_value(AccessType::BLACK, DictType::IGNORE_KEYWORD, value).await?
    }
    else if access_type== AccessType::WHITE && dict_type==DictType::KEYWORD{
        exist_by_access_type_and_dict_type_and_value(AccessType::WHITE, DictType::IGNORE_KEYWORD, value).await?
    }else if access_type== AccessType::WHITE && dict_type== DictType::TAG {
        exist_by_access_type_and_dict_type_and_value(AccessType::WHITE, DictType::IGNORE_KEYWORD, value).await?
    }
    else{
       true
    };

    R::Ok(result)

}



/// 新增字典，如果不在忽略名单内则新增
pub(crate) async fn add_dict(table: Dict) -> R<bool> {

    let access_type = table.access_type;
    let dict_type = table.dict_type;
   
    let check_is_need_save = check_is_need_save(access_type,dict_type,&table.value).await?;
    if check_is_need_save {
        Dict::insert(&CONTEXT.rb, &table).await?;
    }
   R::Ok(check_is_need_save)

}


