

#[derive(Debug,Clone,PartialEq,Copy,Serialize,Deserialize)]
pub  enum Classify{
    COOKIE,
    REQUEST_HEADER,
    RESPONSE_HEADER,
}
use std::fmt;

use serde::{Deserialize, Serialize};

// impl fmt::Display for Classify {
//     fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
//         match self {
//             Classify::COOKIE => write!(f, "COOKIE"),
//             Classify::RequestHeader => write!(f, "REQUEST_HEADER"),
//             Classify::ResponseHeader => write!(f, "RESPONSE_HEADER"),
//         }
//     }
// }


#[derive(Debug,Clone,Copy,PartialEq,Serialize,Deserialize)]
pub enum MediaType{
  /**
     * 通用类型
     */
    GENERAL,

    /**
     * 与URL匹配的类型
     */
    URL_MATCHING,

    /**
     * 及时更新的类型
     */
    TIMELY_UPDATE,
}

/**
 * 字典类型
 */
#[derive(Debug,Clone,Copy,Serialize,Deserialize,PartialEq)]
pub enum DictType {


    /**
     * 标签类型
     */
    TAG,

    /**
     * 描述
     */
    DESC,

    /**
     * 标题
     */
    TITLE,

    /**
     * 封面
     */
    COVER,

    /**
     * up主id
     */
    MID,

    /**
     * 分区id
     */
    TID,

    /**
     * 搜索词
     */
    SEARCH_KEYWORD,

    /**
     * 通用的关键词，对于没有细分是标题还是封面还是描述的关键词等使用的类型
     */
    KEYWORD,



    /**
     * 停顿词
     */
    STOP_WORDS,


}

/**
 * 访问类型
 */
#[derive(Debug,Clone, Copy,PartialEq, Serialize,Deserialize)]
pub enum  AccessType  {
    /**
     * 黑名单
     */
    BLACK,
    /**
     * 白名单
     */
    WHITE,

    OTHER,



}

#[derive(Debug,Clone, Copy,PartialEq, Serialize,Deserialize)]
pub enum TaskStatus {
    RUNNING,
    STOPPED,
    WAITING,
}

///视频的处理结果
#[derive(Debug,Clone, Copy,PartialEq, Serialize,Deserialize)]
pub enum  HandleType  {
    /**
     * 点赞
     */
    THUMB_UP,

    /**
     * 点踩
     */
    DISLIKE,

    /**
     * 未点赞也未点踩，暂时不匹配白名单也不匹配黑名单
     */
    OTHER

}
#[derive(Debug,Clone, Copy,PartialEq, Serialize,Deserialize)]
pub enum  DictStatus  {
    /// 缓存状态，还没有被正式使用
    CACHE,
    /// 忽略状态，这个状态的关键词不能被用来判断视频
    IGNORE,
    /// 正常状态，正常用来判断视频信息
    NORMAL
}