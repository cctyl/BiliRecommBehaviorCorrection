use std::collections::HashSet;
use rbs::value;
use crate::app::config::CC;
use crate::app::response::R;
use crate::domain::tag::Tag;
use crate::utils::id;

#[cfg(test)]
mod tests{
    use crate::domain::tag::Tag;

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;




        //在这中间编写测试代码



        //最后一句必须是这个
        log::logger().flush();
    }

    //测试 save_if_not_exists
    //测试 save_if_not_exists
    #[tokio::test]
    async fn test_save_if_not_exists() {
        // 初始化
        crate::init().await;

        // 准备测试数据
        let tags = vec![
            Tag {
                id: None,
                tag_id: 99998787,
               
                tag_name:"cessss".to_string(),
                content:Some("aasdasdasd".to_string())
                
            },
            Tag {
                id: None,
                tag_id: 99998786,
                tag_name:"cessss2".to_string(),
                content:Some("aasdasdasd".to_string())
            }
        ];

        // 执行测试
        let result = super::save_if_not_exists(tags).await;

        // 验证结果
        assert!(result.is_ok());

        // 清理测试数据（如果需要）
        // ...

        log::logger().flush();
    }

}
///批量保存标签，去掉已存在的
pub(crate) async fn save_if_not_exists(tags: Vec<Tag>) ->R<()>{
    if tags.is_empty() {
        return R::Ok(())
    }


    let tag_id:Vec<i32> = tags.iter().map(|tag| { tag.tag_id }).collect();

    let db:HashSet<i32> = Tag::select_by_map(&CC.rb, value! {"tag_id":tag_id}).await?
        .iter().map(|tag| { tag.tag_id })
        .collect()
        ;

    let mut filter:Vec<Tag> = tags.into_iter().filter(|x| {
        !db.contains(&x.tag_id)
    }).collect();


    if filter.is_empty() { 
        return R::Ok(())
    }
    
    
    for x in filter.iter_mut() {
        x.id = Some(id::generate_id());
    }
    
    Tag::insert_batch(&CC.rb, &filter,100).await?;
    
    R::Ok(())
}