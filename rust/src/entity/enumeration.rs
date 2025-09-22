

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
     * 需要忽略的关键词
     */
    IGNORE_KEYWORD,

    /**
     *  需要忽略的标签
     */
    IGNORE_TAG,

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

    /**
     * 黑名单未处理缓存
     */
    BLACK_CACHE,
    /**
     * 白名单未处理缓存
     */
    WHITE_CACHE,

}
