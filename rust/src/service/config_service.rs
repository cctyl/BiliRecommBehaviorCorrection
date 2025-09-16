use log::info;
use rbatis::rbdc::{DateTime, Timestamp};
use rbs::value;

use crate::{
    app::{constans, database::CONTEXT, response::R},
    entity::models::Config,
};



/**
 * 修改用户id
 */
pub async fn update_mid(mid:&str)->R<()>{

    add_or_update_config(constans::MID_KEY, mid).await?;

    R::Ok(())

}



pub async fn add_or_update_config(config_name: &str, config_value: &str) -> R<Config> {
    let find_config_by_name = find_config_by_name(config_name).await?;

    let mut r:Config;
    if let Some(config) = find_config_by_name{
        r = config;
        r.value = Some(config_value.to_string());

        Config::update_by_map(&CONTEXT.rb, &r, value!{"id":&r.id}).await?;
    }else {
        r = Config::default();
        r.name= config_name.to_string();
        r.value = Some(config_value.to_string());
        Config::insert(&CONTEXT.rb, &r).await?;
    }
  

    R::Ok(r)

}


//生成add_or_update_config的测试
#[tokio::test]
async fn test_add_or_update_config() {
    crate::init().await;
    let r = add_or_update_config("test", "test2").await.unwrap();
    println!("{:#?}", r);

   // 根据id查询config 11497000394031109
    let select_by_map = Config::select_by_map( &CONTEXT.rb, value! {"id":"7e3c1d95002e5e873ad318eeb9eef70a"}).await.unwrap();
    println!("{:#?}", select_by_map);

}

pub async fn find_config_by_name(name: &str) -> R<Option<Config>> {
    let config = Config::select_by_name(&CONTEXT.rb, "*", name).await?;

    if let Some(config) = config.as_ref() {
        let expire_second = config.expire_second.unwrap_or(-1) as i64;

        if expire_second > 0 {
            if let Some(last_modified_date) = config.last_modified_date.clone() {
                info!("最后修改时间：{}", last_modified_date);
                let r = (DateTime::now() - last_modified_date) .as_secs()as i64  > expire_second;
                //超时，删除这个配置之
                if r {
                    info!("已超时，删除配置：{}", config.name);
                    Config::delete_by_map(&CONTEXT.rb, value! {"id":&config.id}).await?;
                }
            }
        }
    }

    Ok(config)
}

#[tokio::test]
async fn test_find_config_by_name() {
    crate::init().await;
    let r = find_config_by_name("test").await.unwrap();
    println!("{:#?}", r);
}




