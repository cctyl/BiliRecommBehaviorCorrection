use core::str;
use serde::{Deserialize, Serialize};
use std::{
    thread,
    time::{Duration, SystemTime},
};
use validator::Validate;
use rbatis::{rbdc::datetime::DateTime, Page};

use crate::entity::{enumeration::AccessType, models::{Dict, Owner, Tag, VideoDetail}};
#[test]
fn testnow() {


    let now = SystemTime::now();
    let secs = now
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap()
        .as_millis();

    println!("{secs}");

    thread::sleep(Duration::from_secs(2));

    let duration = SystemTime::now().duration_since(now).unwrap();
    let subsec_millis: u128 = duration.as_millis();
    println!("过去了：{subsec_millis}毫秒");
}


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ConfigAddUpdateDTO {
    pub id: Option<String>,
    pub name: String,
    pub value: String
    
}




#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PageDTO<T: Send + Sync>{

    pub current: u64,
    pub pages:u64,
    pub records:Vec<T>,
    pub size:u64,
    pub total:u64,

}

impl<T : Send + Sync>  PageDTO<T> 
{
    
    pub fn new(current: u64, size: u64, records: Vec<T>, total: u64) -> Self {
        PageDTO {
            current,
            pages: (total as f64 / size as f64).ceil() as u64,
            records,
            size,
            total,
        }
    }

     /// 将 PageDTO<D> 转换为 PageDTO<T>，其中 T 实现了 From<D>
    pub fn convert_from<D: Send + Sync>(source: PageDTO<D>) -> PageDTO<T>
    where
        T: From<D>,
    {
        PageDTO {
            current: source.current,
            pages: source.pages,
            records: source.records.into_iter().map(T::from).collect(),
            size: source.size,
            total: source.total,
        }
    }

}

impl<T : Send + Sync> From<Page<T>> for PageDTO<T> {
    fn from(value: Page<T>) -> Self {
       
        PageDTO {
            current: value.page_no,
            //总数处于每页大小
            pages:( value.total as f64/value.page_size as f64).ceil() as u64,
            records: value.records,
            size: value.page_size,
            total: value.total,
        }
    }
}


/// 分页数据结构体
#[derive(Debug, Clone)]
pub struct PageBean<T> {
    pub data: Vec<T>,
    pub total: u64,
    pub page_size: u64,
    pub page_num: u64,
}

impl<T> PageBean<T> {
    /// 判断是否还有更多数据
    pub fn has_more(&self) -> bool {
        self.total / self.page_size > self.page_num
    }

    /// 创建一个新的空 PageBean 实例
    pub fn new() -> Self {
        Self {
            data: Vec::new(),
            total: 0,
            page_size: 0,
            page_num: 0,
        }
    }

    /// 创建一个带有初始值的 PageBean 实例
    pub fn with_data(data: Vec<T>, total: u64, page_size: u64, page_num: u64) -> Self {
        Self {
            data,
            total,
            page_size,
            page_num,
        }
    }
}

impl<T> Default for PageBean<T> {
    fn default() -> Self {
        Self::new()
    }
}


#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserSubmissionVideo {
    pub comment: Option<i32>,
    pub typeid: Option<i64>,
    // pub play: Option<String>,
    pub pic: Option<String>,
    pub subtitle: Option<String>,
    pub description: Option<String>,
    pub copyright: Option<String>,
    pub title: Option<String>,
    pub review: Option<i32>,
    pub author: Option<String>,
    pub mid: Option<i64>,
    pub created: Option<i32>,
    pub length: Option<String>,
    pub video_review: Option<i32>,
    pub aid: u64,
    pub bvid: String,
    pub hide_click: Option<bool>,
    pub is_pay: Option<i32>,
    pub is_union_video: Option<i32>,
    pub is_steins_gate: Option<i32>,
    pub is_live_playback: Option<i32>,
    pub meta: Option<serde_json::Value>,
    pub is_avoided: Option<i32>,
    pub attribute: Option<i32>,
    pub is_charging_arc: Option<bool>,
    pub vt: Option<i32>,
    pub enable_vt: Option<i32>,
}





#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoDetailDTO {
    #[serde(flatten)]
    pub video_detail:VideoDetail,
    pub tags:Option<Vec<Tag>>,
    pub owner:Option<Owner>,
    pub desc_v2:Option<Vec<DescV2>>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct DescV2{
   
    pub raw_text:Option<String>,
    pub type_:Option<i32>,
    pub biz_id:Option<i32>,
}



/// 复合规则列表dto
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRuleListDto{
    pub id: String,
    pub info:String,
    pub access_type : AccessType,
    pub title:Vec<Dict>,
    pub desc:Vec<Dict>,
    pub tag:Vec<Dict>,
    pub cover:Vec<Dict>,
    pub tid:Vec<Dict>,
    pub mid:Vec<Dict>,

}


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRuleAddDto {
    pub info: String,
    pub access_type : AccessType
}


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRuleUpdateDto {
    pub id:String,
    pub info: String,
    pub access_type : AccessType
}