use crate::domain::{
    dict::Dict, enumeration::AccessType, owner::Owner, tag::Tag, video_detail::VideoDetail,
};
use aho_corasick::AhoCorasick;
use core::str;
use rbatis::{Page, rbdc::datetime::DateTime};
use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::{
    collections::HashSet,
    thread,
    time::{Duration, SystemTime},
};
use validator::Validate;
use crate::domain::video_detail::MatchResult;
use crate::app::database::empty_string_as_none;
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
    pub value: String,
}
#[derive(Debug, Clone, Deserialize)]
pub struct VideoRawDto {
    pub aid: u64,
    pub cid: u64,
    pub tid: Option<u64>,
    pub tname: Option<String>,
    pub pic: Option<String>,
    pub title: Option<String>,
    pub pubdate: Option<u64>,
    pub desc: Option<String>,
    pub duration: Option<u32>,
    pub dynamic: Option<String>,
    pub bvid: String,
    pub owner: Option<OwnerDto>,
}

#[derive(Debug, Clone, Deserialize)]
pub struct OwnerDto {
    pub mid: u64,
}

impl From<VideoRawDto> for VideoDetail {
    fn from(raw: VideoRawDto) -> Self {
        VideoDetail {
            id: raw.aid,
            tid: raw.tid,
            tname: raw.tname,
            cid:raw.cid,
            pic: raw.pic,
            title: raw.title,
            pubdate: raw.pubdate,
            desc_field: raw.desc,
            duration: raw.duration,
            dynamic: raw.dynamic,
            bvid: raw.bvid,
            owner_id: raw.owner.map(|f| f.mid),
            handle_step: 0,
            handle_reason: None,
            handle_time: None,
            handle_type: None,
            created_date: Some(DateTime::now()),
            tag: None,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq, Hash)]
pub struct SearchKeywordDto {
    pub aid: u64,
    pub arcurl: String,
    pub area: u32,
    pub author: String,
    pub bvid: String,
    pub desc: String,
    pub description: String,
    pub duration: String,
    pub mid: u64,
    pub pic: String,
    pub tag: String,
    pub title: String,
    pub typeid: String,
    pub typename: String,
}



#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PageDTO<T: Send + Sync> {
    /// 当前页
    pub current: u64,
    /// 总页数
    pub pages: u64,
    /// 数据
    pub records: Vec<T>,
    /// 每页大小
    pub size: u64,
    /// 总记录数
    pub total: u64,
}

impl<T: Send + Sync> PageDTO<T> {
    pub fn new(current: u64, size: u64, records: Vec<T>, total: u64) -> Self {

        let pages = if total == 0 {
            0
        } else {
            (total + size - 1) / size
        };
        PageDTO {
            current,
            pages: pages,
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


    // pub fn convert<D: Send + Sync>(&self,  records:Vec<T>)->PageDTO<T>{
    //       PageDTO {
    //         current: self.current,
    //         pages: self.pages,
    //         records: records,
    //         size: self.size,
    //         total: self.total,
    //     }
    // }


}

pub trait PageConvert<T: Send + Sync> {
    fn convert<D: Send + Sync, F>(self, f: F) -> PageDTO<D>
    where
        F: FnMut(T) -> D; // 👈 这里
}
impl<T: Send + Sync> PageConvert<T> for Page<T> {
    fn convert<D: Send + Sync, F>(self, mut f: F) -> PageDTO<D>
    where
        F: FnMut(T) -> D, // 👈 这里
    {
        let pages = if self.page_size == 0 {
            0
        } else {
            (self.total + self.page_size - 1) / self.page_size
        };

        PageDTO {
            current: self.page_no,
            pages,
            records: self.records.into_iter().map(|x| f(x)).collect(),
            size: self.page_size,
            total: self.total,
        }
    }
}
impl<T: Send + Sync> From<Page<T>> for PageDTO<T> {
    fn from(value: Page<T>) -> Self {
        PageDTO {
            current: value.page_no,
            //总数处于每页大小
            pages: (value.total as f64 / value.page_size as f64).ceil() as u64,
            records: value.records,
            size: value.page_size,
            total: value.total,
        }
    }
}




/// bilibili的分页数据结构体
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
pub struct VideoDetailJsonDto {
    pub aid: u64,
    pub tid: Option<u64>,
    pub tname: Option<String>,
    pub pic: Option<String>,
    pub title: Option<String>,
    pub pubdate: Option<u64>,
    #[serde(rename = "desc")]
    pub desc_field: Option<String>,
    pub duration: Option<u32>,
    pub dynamic: Option<String>,
    pub bvid: String,
    pub cid:u64,
    pub owner_id: Option<u64>,
}
impl From<VideoDetail> for VideoDetailJsonDto {
    fn from(detail: VideoDetail) -> Self {
        VideoDetailJsonDto {
            aid: detail.id,
            tid: detail.tid,
            cid:detail.cid,
            tname: detail.tname,
            pic: detail.pic,
            title: detail.title,
            pubdate: detail.pubdate,
            desc_field: detail.desc_field,
            duration: detail.duration,
            dynamic: detail.dynamic,
            bvid: detail.bvid,
            owner_id: detail.owner_id,
        }
    }
}
impl From<VideoDetailJsonDto> for VideoDetail {
    fn from(dto: VideoDetailJsonDto) -> Self {
        VideoDetail {
            id: dto.aid,
            tid: dto.tid,
            tname: dto.tname,
            cid:dto.cid,
            pic: dto.pic,
            title: dto.title,
            pubdate: dto.pubdate,
            desc_field: dto.desc_field,
            duration: dto.duration,
            dynamic: dto.dynamic,
            bvid: dto.bvid,
            owner_id: dto.owner_id,
            // 以下字段留空，需要手动填充
            handle_step: 0,
            handle_reason: None,
            handle_time: None,
            handle_type: None,
            created_date: Some(DateTime::now()),
            tag: None,
        }
    }
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct SecondHandleDto {
    pub id: u64,
    pub handle_type: AccessType,
    pub user_handle_reason: Option<String>,
    pub re_handle: bool
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoVo{
    pub id: u64,
    pub tid: Option<u64>,
    pub tname: Option<String>,
    pub pic: Option<String>,
    pub title: Option<String>,
    pub pubdate: Option<u64>,
    #[serde(rename = "desc")]
    pub desc_field: Option<String>,
    pub duration: Option<u32>,
    pub dynamic: Option<String>,
    pub bvid: String,
    pub owner_id: Option<u64>,
    pub handle_step: u64,
    pub handle_reason: Option<MatchResult>,
    pub handle_time: Option<DateTime>,
    pub handle_type: Option<AccessType>,
    pub created_date: Option<DateTime>,
    pub tag: Option<String>,
    pub owner:Option<Owner>,
}
// 实现基础转换（不包含 owner 字段）
impl From<VideoDetail> for VideoVo {
    fn from(detail: VideoDetail) -> Self {
        Self {
            id: detail.id,
            tid: detail.tid,
            tname: detail.tname,
            pic: detail.pic,
            title: detail.title,
            pubdate: detail.pubdate,
            desc_field: detail.desc_field,
            duration: detail.duration,
            dynamic: detail.dynamic,
            bvid: detail.bvid,
            owner_id: detail.owner_id,
            handle_step: detail.handle_step,
            handle_reason: detail.handle_reason,
            handle_time: detail.handle_time,
            handle_type: detail.handle_type,
            created_date: detail.created_date,
            tag: detail.tag,
            owner: None, // 临时设为 None
        }
    }
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CommonTriggerTaskRequest{

  pub  name:String
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct SearchHandleVideoRequest {
    pub page: u64,
    pub limit: u64,
    pub handle_step: u64,

    pub search: Option<String>,

    #[serde(deserialize_with = "empty_string_as_none")]
    pub handle_type: Option<AccessType>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct OwnerJsonDto {
    pub mid: u64,
    pub name: String,
    pub face: Option<String>,
}

impl From<OwnerJsonDto> for Owner {
    fn from(dto: OwnerJsonDto) -> Self {
        Owner {
            id: dto.mid,
            name: dto.name,
            face: dto.face,
        }
    }
}

impl From<Owner> for OwnerJsonDto {
    fn from(owner: Owner) -> Self {
        OwnerJsonDto {
            mid: owner.id,
            name: owner.name,
            face: owner.face,
        }
    }
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoDetailDTO {
    #[serde(flatten)]
    pub video_detail: VideoDetailJsonDto,
    pub tags: Option<Vec<Tag>>,
    pub owner: Option<OwnerJsonDto>,
    pub desc_v2: Option<Vec<DescV2>>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct DescV2 {
    pub raw_text: Option<String>,
    pub type_: Option<u64>,
    pub biz_id: Option<u64>,
}

/// 复合规则列表dto
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRuleListDto {
    pub id: String,
    pub info: String,
    pub access_type: AccessType,
    pub title: Vec<Dict>,
    pub desc: Vec<Dict>,
    pub tag: Vec<Dict>,
    pub cover: Vec<Dict>,
    pub tid: Vec<Dict>,
    pub mid: Vec<Dict>,
}

/// 复合规则列表dto
#[derive(Clone, Debug)]
pub struct AssociateRuleAc {
    pub id: String,
    pub name: String,
    pub title: AhoCorasick,
    pub desc: AhoCorasick,
    pub tag: AhoCorasick,
    pub cover: AhoCorasick,
    pub tid: HashSet<u64>,
    pub mid: HashSet<u64>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct TestRuleDto {
    pub bvid: String,
    pub ai_chat_enable: bool,
    pub single_match_enable: bool,
    pub complex_match_enable: bool,
}

#[derive(Clone, Debug)]
pub struct SingleMatchRuleAc {
    pub title: AhoCorasick,
    pub desc: AhoCorasick,
    pub tag: AhoCorasick,
    pub cover: AhoCorasick,
    pub tid: HashSet<u64>,
    pub mid: HashSet<u64>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRuleAddDto {
    pub info: String,
    pub access_type: AccessType,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRuleUpdateDto {
    pub id: String,
    pub info: String,
    pub access_type: AccessType,
}

/// 点赞用户所有视频请求参数
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ThumbUpUserAllVideoRequest {
    pub mid: u64,
    #[serde(default = "default_page")]
    pub page: i64,
    #[serde(default)]
    pub keyword: String,
}

fn default_page() -> i64 {
    1
}
