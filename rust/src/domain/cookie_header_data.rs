use rbatis::{PageRequest, html_sql};
use crate::app::database::bool_or_int;
use crate::app::database::bool_or_int_opt;
use crate::domain::enumeration::{AccessType, DictStatus, HandleType};
use crate::domain::enumeration::Classify;
use crate::domain::enumeration::DictType;
use crate::domain::enumeration::MediaType;
use crate::domain::enumeration::TaskStatus;
use crate::impl_select_one_by_condition;
use crate::plus;
use crate::utils::id::generate_id;
use rbatis::rbdc::Timestamp;
use rbatis::RBatis;
use rbatis::crud;
use rbatis::impled;
use rbatis::rbdc::types::DateTime;
use rbatis::sql;
use serde::{Deserialize, Serialize};
use crate::app::database::default_false;

use crate::app::config::CC;




#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CookieHeaderData {
    pub id: String,
    pub url: Option<String>,
    pub ckey: String,
    pub cvalue: String,
    pub classify: Option<Classify>,
    pub media_type: Option<MediaType>,
}
crud!(CookieHeaderData {}, "cookie_header_data");

#[html_sql("src/domain/table/cookie_header_data.html")]
impl CookieHeaderData{
    pub async fn select_page(conn:&dyn rbatis::Executor, page_request: &rbatis::PageRequest)-> rbatis::Result<Page<CookieHeaderData>> { impled!() }
}

#[tokio::test]
async fn test_cookie_header_data() {
    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CC.init().await;

    let select_by_map = CookieHeaderData::select_page(&CC.rb, &PageRequest::new(1, 1))
        .await
        .unwrap();
    println!("{:#?}", select_by_map);
}
