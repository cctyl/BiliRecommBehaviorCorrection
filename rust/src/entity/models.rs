use crate::app::database::bool_or_int;
use crate::app::database::bool_or_int_opt;
use crate::entity::enumeration::AccessType;
use crate::entity::enumeration::Classify;
use crate::entity::enumeration::DictType;
use crate::entity::enumeration::MediaType;
use crate::utils::id::generate_id;
use rbatis::rbdc::Timestamp;
use rbatis::PageRequest;
use rbatis::RBatis;
use rbatis::crud;
use rbatis::impl_select;
use rbatis::impl_select_page;
use rbatis::impl_update;
use rbatis::impled;
use rbatis::rbdc::types::DateTime;
use rbatis::sql;
use serde::{Deserialize, Serialize};

use crate::app::database::CONTEXT;

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Config {
    pub id: String,
    pub name: String,
    pub value: Option<String>,
    pub expire_second: Option<i32>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
}

// crud!(Config {}, "config");
crud!(Config {});  
impl_select!(Config{
    select_by_name(table_column:&str,name:&str) -> Option    =>" ` where name = #{name} limit 1 ` "
});

impl Config{
    pub fn default() -> Self {
        Config {
            id: generate_id(),
            name: String::new(),
            value: Some(String::new()),
            expire_second: None,
            created_date: Some( DateTime::now()),
            last_modified_date: Some( DateTime::now()),
        }
    }
}

#[tokio::test]
async fn test_config() {
    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CONTEXT.init().await;

    let select_by_map = Config::select_by_map(&CONTEXT.rb, rbs::Value::Null)
        .await
        .unwrap();
    println!("{:#?}", select_by_map);
}

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
impl_select_page!(CookieHeaderData{select_page() => ""});
#[tokio::test]
async fn test_cookie_header_data() {
    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CONTEXT.init().await;

    let select_by_map = CookieHeaderData::select_page(&CONTEXT.rb, &PageRequest::new(1, 10))
        .await
        .unwrap();
    println!("{:#?}", select_by_map);
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Dict {
    pub id: String,
    pub value: String,
    pub access_type: Option<AccessType>,
    pub dict_type: Option<DictType>,
    pub outer_id: Option<String>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    #[serde(rename = "desc")]
    pub desc_field: Option<String>,
}
crud!(Dict {}, "dict");


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Owner {
    pub id: String,
    pub mid: String,
    pub name: String,
    pub face: Option<String>,
}
crud!(Owner {}, "owner");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PrepareVideo {
    pub id: String,
    pub video_id: String,
    pub handle_type: String,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
}
crud!(PrepareVideo {}, "prepare_video");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Stat {
    pub id: String,
    pub aid: i32,
    pub view: Option<i32>,
    pub danmaku: Option<i32>,
    pub reply: Option<i32>,
    pub favorite: Option<i32>,
    pub coin: Option<i32>,
    pub share: Option<i32>,
    pub now_rank: Option<i32>,
    pub his_rank: Option<i32>,
    pub like: Option<i32>,
    pub dislike: Option<i32>,
    pub video_id: Option<String>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
}
crud!(Stat {}, "stat");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Tag {
    pub id: String,
    pub tag_id: i32,
    pub tag_name: String,
    pub content: Option<String>,
}
crud!(Tag {}, "tag");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Task {
    pub id: String,
    pub last_run_time: Option<DateTime>,
    pub current_run_status: Option<String>,
    pub total_run_count: Option<i32>,
    pub last_run_duration: Option<i32>,
    pub task_name: Option<String>,
    pub scheduled_hour: Option<i32>,
    pub is_enabled: Option<i32>,
    pub class_method_name: Option<String>,
    pub description: Option<String>,
    pub img: Option<String>,
}
crud!(Task {}, "task");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoDetail {
    pub id: String,
    #[serde(default)]
    pub aid: i64,
    pub videos: Option<i32>,
    pub tid: Option<i64>,
    pub tname: Option<String>,
    pub copyright: Option<i32>,
    pub pic: Option<String>,
    pub title: Option<String>,
    pub pubdate: Option<i64>,
    pub ctime: Option<i64>,
    #[serde(rename = "desc")]
    pub desc_field: Option<String>,
    pub state: Option<i32>,
    pub duration: Option<i32>,
    pub dynamic: Option<String>,
    pub cid: Option<i64>,
    pub season_id: Option<i64>,
    pub first_frame: Option<String>,
    pub pub_location: Option<String>,
    pub bvid: String,
    pub owner_id: Option<String>,
    #[serde(deserialize_with = "bool_or_int_opt")]
    pub handle: Option<bool>,
    pub black_reason: Option<String>,
    pub thumb_up_reason: Option<String>,
    #[serde(deserialize_with = "bool_or_int_opt", default)]
    pub no_cache: Option<bool>,
    pub up_from_v2: Option<i32>,
    pub rcmd_reason: Option<String>,
    pub handle_type: Option<String>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    #[serde(deserialize_with = "bool_or_int_opt", default)]
    pub is_deleted: Option<bool>,
}
crud!(VideoDetail {}, "video_detail");
//如果不需要参数，=> 传空
//与名字没有关系
impl_select_page!(VideoDetail{select_page() => "`order by created_date desc`"});
impl_select_page!(VideoDetail{select_page_by_name(tname:&str) =>"
     if tname != null && tname != '':
       `where tname like #{tname}`
"});

impl_select!(VideoDetail{select_by_id(id:i64) => "`where id = #{id} limit 1`"});
impl_select!(VideoDetail{select_id_tname(id:i64,table_column:&str) => "`where id = #{id} limit 1`"});

impl_select!(
    VideoDetail{
    select_by_title_like(name: &str) -> Vec => 
    "`where title like #{name} limit 1`"
});

#[tokio::test]
async fn test_video_detail() {
    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CONTEXT.init().await;

    let page_request = PageRequest::new(1, 10); // 第一页，每页10条
    //let result = VideoDetail::select_page(&CONTEXT.rb, &page_request).await.unwrap();
    // let result = VideoDetail::select_page_by_name(&CONTEXT.rb, &page_request,"单机游戏").await.unwrap();

    // let result = VideoDetail::select_by_id(&CONTEXT.rb, 1729314424889581570).await.unwrap();
    // let result = VideoDetail::select_id_tname(
    //     &CONTEXT.rb,
    //     1729314424889581570,
    //     "id,tname,aid,bvid,handle,no_cache",
    // )
    // .await
    // .unwrap();


    let result = VideoDetail::select_by_title_like(&CONTEXT.rb, "%不要%").await.unwrap();

   

    println!("{:#?}", result);
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoRelate {
    pub id: String,
    pub master_video_id: String,
    pub related_video_id: String,
}
crud!(VideoRelate {}, "video_relate");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoReply {
    pub id: String,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    pub video_id: String,
    pub rpid: i64,
    pub oid: i64,
    pub mid: String,
    pub root: i64,
    pub parent: i64,
    pub dialog: i64,
    pub ctime: i32,
    pub current_level: i32,
    pub vip_type: i32,
    pub message: String,
    pub sex: Option<String>,
}
crud!(VideoReply {}, "video_reply");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoTag {
    pub id: String,
    pub tag_id: String,
    pub video_id: String,
    pub created_date: Option<DateTime>,
}
crud!(VideoTag {}, "video_tag");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct WatchedVideo {
    pub aid: i32,
}
crud!(WatchedVideo {}, "watched_video");

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct WhiteListRule {
    pub id: String,
    pub info: Option<String>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    #[serde(deserialize_with = "bool_or_int_opt")]
    pub is_deleted: Option<bool>,
    pub version: Option<i32>,
}
crud!(WhiteListRule {}, "white_list_rule");
#[tokio::test]
async fn test_white_list_rule() {
    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CONTEXT.init().await;

    let select_by_map = WhiteListRule::select_by_map(&CONTEXT.rb, rbs::Value::Null)
        .await
        .unwrap();
    println!("{:#?}", select_by_map);
}

