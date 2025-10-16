use log::info;
use rbatis::rbdc::{DateTime, Timestamp};
use rbs::value;

use crate::{
    app::{
        constans::{self, IMG_KEY, SUB_KEY},
        database::CONTEXT,
        response::R,
    },
    entity::{dtos::ConfigAddUpdateDTO, models::Config},
};

/**
 * 删除配置项
 */
pub async fn del_by_name(name: &str) -> R<()> {
    Config::delete_by_map(&CONTEXT.rb, value! {"name":name}).await?;
    R::Ok(())
}

/**
 * 修改access_key
 */
pub async fn update_access_key(access_key: &str) -> R<()> {
    add_or_update_config(constans::BILI_ACCESS_KEY, access_key, Some(2_505_600)).await?;

    R::Ok(())
}

/**
 * 修改用户id
 */
pub async fn update_mid(mid: &str) -> R<()> {
    add_or_update_config(constans::MID_KEY, mid, None).await?;

    R::Ok(())
}

/**
 * 如果不存在则新增配置项
 * 如果存在则更新配置项
 *
 */
pub async fn add_or_update_config(
    config_name: &str,
    config_value: &str,
    expire_second: Option<i32>,
) -> R<Config> {
    let find_config_by_name = find_config_by_name(config_name).await?;

    let mut r: Config;
    if let Some(config) = find_config_by_name {
        r = config;
        r.last_modified_date = Some(DateTime::now());
        r.value = Some(config_value.to_string());
        if let Some(expire_second) = expire_second {
            r.expire_second = Some(expire_second);
        }
        Config::update_by_map(&CONTEXT.rb, &r, value! {"id":&r.id}).await?;
    } else {
        r = Config::default();
        r.name = config_name.to_string();
        r.value = Some(config_value.to_string());
        if let Some(expire_second) = expire_second {
            r.expire_second = Some(expire_second);
        }
        Config::insert(&CONTEXT.rb, &r).await?;
    }

    R::Ok(r)
}

/**
 * 根据名称查询配置项
 * 如果超时则删除配置项
 */
pub async fn find_config_by_name(name: &str) -> R<Option<Config>> {
    let config = Config::select_by_name(&CONTEXT.rb, "*", name).await?;

    if let Some(config) = config.as_ref() {
        let expire_second = config.expire_second.unwrap_or(-1) as i64;

        if expire_second > 0 {
            if let Some(last_modified_date) = config.last_modified_date.clone() {
                info!("最后修改时间：{}", last_modified_date);
                let r = (DateTime::now() - last_modified_date).as_secs() as i64 > expire_second;
                //超时，删除这个配置
                if r {
                    info!("已超时，删除配置：{}", config.name);
                    Config::delete_by_map(&CONTEXT.rb, value! {"id":&config.id}).await?;

                    return R::Ok(None);
                }
            }
        }
    }

    Ok(config)
}
/**
 * 更新配置项列表
 * 此方法用于批量更新配置项，通过接收一个配置对象列表来实现配置的批量修改或添加
 * 主要用途是当有一批新的配置需要应用或者现有配置发生变更时，通过调用此方法来更新系统内的配置信息
 *
 * @param configList 一个包含多个Config对象的列表，用于更新系统配置
 */
pub async fn update_config_list(payload: Vec<ConfigAddUpdateDTO>) -> R<()> {
    for config in payload.into_iter() {
        if config.id.is_none() {
            //新增
            let mut config_db = Config::default();
            config_db.name = config.name;
            config_db.value = Some(config.value);
            Config::insert(&CONTEXT.rb, &config_db).await?;
        } else {
            let config_db = Config {
                id: config.id.unwrap(),
                name: config.name,
                value: Some(config.value),
                expire_second: None,
                last_modified_date: Some(DateTime::now()),
                created_date: None,
            };

            //修改
            Config::update_by_map(&CONTEXT.rb, &config_db, value! {"id":&config_db.id}).await?;
        }
    }

    R::Ok(())
}

//=====================================测试==========================================
#[cfg(test)]
mod tests {
    use rbs::value;

    use crate::app::constans::BILI_ACCESS_KEY;
    use crate::{app::database::CONTEXT, entity::models::Config};

    use crate::service::config_service::*;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //TODO 在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_find_config_by_name() {
        crate::init().await;
        let r = find_config_by_name("test").await.unwrap();
        println!("{:#?}", r);
    }

    //生成add_or_update_config的测试
    #[tokio::test]
    async fn test_add_or_update_config() {
        crate::init().await;
        let r = add_or_update_config(BILI_ACCESS_KEY, "bbbb", None).await.unwrap();
        println!("{:#?}", r);


        log::logger().flush();
    }

    #[tokio::test]
    async fn test_update_access_key() {
        crate::init().await;

        update_access_key("ceshi1213213d1key").await.unwrap();
        log::logger().flush();
    }

    //update_config_list
    #[tokio::test]
    async fn test_update_config_list() {
        crate::init().await;

        update_config_list(vec![ConfigAddUpdateDTO {
            // id: Some("111".to_string()),
            id: None,
            name: "newww".to_string(),
            value: "test2222".to_string(),
        }])
        .await
        .unwrap();

        log::logger().flush();
    }

    #[tokio::test]
    async fn test_update_wbi() {
        // 第一句必须是这个
        crate::init().await;

        // 测试 update_wbi 函数
        update_wbi(
            &Some("test_img_key".to_string()),
            &Some("test_sub_key".to_string()),
        )
        .await
        .unwrap();

        // 验证配置是否正确保存
        let img_key_config = find_config_by_name(IMG_KEY).await.unwrap();
        let sub_key_config = find_config_by_name(SUB_KEY).await.unwrap();

        assert_eq!(
            img_key_config.as_ref().unwrap().value.as_ref().unwrap(),
            "test_img_key"
        );
        assert_eq!(
            sub_key_config.as_ref().unwrap().value.as_ref().unwrap(),
            "test_sub_key"
        );

        // 最后一句必须是这个
        log::logger().flush();
    }
}

/// 获取img_key
pub(crate) async fn get_img_key() -> R<Option<String>> {
    R::Ok(find_config_by_name(IMG_KEY).await?.and_then(|c| c.value))
}

/// 获取sub_key
pub(crate) async fn get_sub_key() -> R<Option<String>> {
    R::Ok(find_config_by_name(SUB_KEY).await?.and_then(|c| c.value))
}

/// 更新wbi
pub(crate) async fn update_wbi(img_key: &Option<String>, sub_key: &Option<String>) -> R<()> {
    if let Some(img_key) = img_key {
        add_or_update_config(IMG_KEY, &img_key, Some(72_000)).await?;
    }

    if let Some(sub_key) = sub_key {
        add_or_update_config(SUB_KEY, &sub_key, Some(72_000)).await?;
    }

    R::Ok(())
}
