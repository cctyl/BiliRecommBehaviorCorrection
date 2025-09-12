use rbs::value;

use crate::{app::{database::CONTEXT, response::R}, entity::{enumeration::{Classify, MediaType}, models::CookieHeaderData}};



/**
 * 根据分类和用途查询数据
 */
pub async fn find_by_classify_and_media_type(classify: Classify, media_type: MediaType) -> R<Vec<CookieHeaderData>> { 



    let cookie_header_datas = CookieHeaderData::select_by_map(&CONTEXT.rb, 
    value!{
        "classify": classify,
        "media_type": media_type
        }
    ).await?;

    R::Ok(cookie_header_datas)
}

#[tokio::test]
async fn test_find_by_classify_and_media_type() {
    crate::init().await;
    let cookie_header_datas = find_by_classify_and_media_type(Classify::COOKIE, MediaType::GENERAL).await;
    println!("{:?}", cookie_header_datas);
}
