use std::collections::HashMap;

use rbatis::{Page, PageRequest};
use rbs::value;

use crate::{
    app::{config::CC, response::R},
    entity::{
        dtos::{AssociateRuleListDto, PageDTO},
        enumeration::{AccessType, DictType},
        models::{AssociateRule, Dict},
    },
    utils::collection_tool::group_by,
};

/// 查询规则列表
pub async fn get_associate_tule_list(
    access_type: AccessType,
    page: u64,
    limit: u64,
) -> R<PageDTO<AssociateRuleListDto>> {
    let result = AssociateRule::select_page_by_access_type(
        &CC.rb,
        &PageRequest::new(page, limit),
        access_type,
    )
    .await?;

    let Page {
        records,
        total,
        page_no,
        page_size,
        ..
    } = result;

    let ids: Vec<&String> = records.iter().map(|a| &a.id).collect();
    let dict_by_outerid: Vec<Dict> = Dict::select_by_map(
        &CC.rb,
        value! {
            "outer_id":&ids
        },
    )
    .await?;

    //按照id分组
    let mut grouped = group_by(dict_by_outerid, |f| {
        f.outer_id.clone().unwrap_or(String::from("0")).clone()
    });

    let mut result: Vec<AssociateRuleListDto> = Vec::new();
    for item in records {
        if let Some(v) = grouped.remove(&item.id) {
            let mut dict_type_map = group_by(v, |f| f.dict_type);

            let newitem = AssociateRuleListDto {
                id: item.id, // 直接转移所有权，无需 clone
                info: item.info,
                access_type: item.access_type,
                title: dict_type_map.remove(&DictType::TITLE).unwrap_or(vec![]),
                desc: dict_type_map.remove(&DictType::DESC).unwrap_or(vec![]),
                tag: dict_type_map.remove(&DictType::TAG).unwrap_or(vec![]),
                cover: dict_type_map.remove(&DictType::COVER).unwrap_or(vec![]),
                tid: dict_type_map.remove(&DictType::TID).unwrap_or(vec![]),
                mid: dict_type_map.remove(&DictType::MID).unwrap_or(vec![]),
            };
            result.push(newitem);
        }
    }

    R::Ok(PageDTO::new(page_no, limit, result, total))
}

#[cfg(test)]
mod tests {
    use crate::app::config::CC;
    use crate::entity::enumeration::AccessType;
    use crate::entity::models::AssociateRule;
    use crate::service::associate_rule_service::get_associate_tule_list;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_select() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let vec = AssociateRule::select_all(&CC.rb).await.unwrap();

        println!("{:#?}", vec);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_get_associate_tule_list() {
        // 第一句必须是这个
        crate::init().await;

        // 测试逻辑编写
        // 1. 定义测试用的分页参数（page从1开始，limit自定义）
        let test_access_type = AccessType::WHITE; // 根据你的AccessType枚举实际值调整，比如AccessType::Admin
        let test_page = 1u64;
        let test_limit = 10u64;

        // 2. 调用待测试的函数
        let result = get_associate_tule_list(test_access_type, test_page, test_limit).await;

        // 3. 验证返回结果是否符合预期
        match result {
            Ok(page_dto) => {
                // 打印返回结果，方便调试查看
                println!("测试返回的分页数据: {:#?}", page_dto);
            }
            Err(e) => {
                // 打印错误信息
                eprintln!("测试失败，调用函数出错: {:#?}", e);
                // 断言失败，标记测试不通过
                panic!("get_associate_tule_list 调用失败: {}", e);
            }
        }

        // 最后一句必须是这个
        log::logger().flush();
    }
}

/// 删除一条规则，同时删除规则关联的字典
pub async fn delete_rule_by_id(id: String) -> R<String> {
    // 1. 开始事务
    let tx = &CC.rb.acquire_begin().await?;

    // 2. 注册defer回调（这里只是注册，不会立即执行）
    let guard = tx.defer_async(|tx| async move {
        if !tx.done() {
            // 检查事务是否已完成
            let _ = tx.rollback().await; // 如果未完成则回滚  
        }
    });

    // 3. 在这里,用guard 执行你的业务逻辑（两个删除操作）
    AssociateRule::delete_by_id(&guard, &id).await?;
    Dict::delete_by_map(&guard, value!{ "outer_id":id }).await?;


    // 4. 正常提交事务
    guard.commit().await?; // 设置done=true，defer回调不会回滚  
    // guard离开作用域，defer回调执行，但发现done=true，不会回滚
    

    R::Ok("删除成功".to_string())
}
