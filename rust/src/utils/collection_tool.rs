use std::collections::HashMap;

/// 通用分组函数
pub fn group_by<K, V, F>(items: Vec<V>, key_extractor: F) -> HashMap<K, Vec<V>>
where
    K: std::hash::Hash + Eq + Clone,
    F: Fn(&V) -> K,
{
    items.into_iter().fold(HashMap::new(), |mut map, item| {
        let key = key_extractor(&item);
        map.entry(key).or_insert_with(Vec::new).push(item);
        map
    })
}

#[cfg(test)]
mod tests {
    use crate::app::config::CC;
    use crate::entity::enumeration::AccessType;
    use crate::entity::models::{AssociateRule, Dict};
    use crate::utils::collection_tool::group_by;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_group() {
        //第一句必须是这个
        // crate::init().await;

        //在这中间编写测试代码
        let rules = vec![
            AssociateRule {
                id: "rule_001".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_002".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_001".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_003".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_002".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_004".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_003".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_005".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_004".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
            AssociateRule {
                id: "rule_001".to_string(),
                created_date: None,
                last_modified_date: None,
                access_type: AccessType::BLACK,
            },
        ];


        let group_by = group_by(rules, |f| f.id.clone());
        println!("{:#?}",group_by);

        //最后一句必须是这个
        // log::logger().flush();
    }
}
