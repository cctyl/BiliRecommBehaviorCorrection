

#[derive(Debug,Clone,Serialize,Deserialize)]
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


#[derive(Debug,Clone,Serialize,Deserialize)]
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
// impl fmt::Display for MediaType {
//     fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        
//         match self {
//             MediaType::General => write!(f, "GENERAL"),
//             MediaType::UrlMatching => write!(f, "URL_MATCHING"),
//             MediaType::TimelyUpdate => write!(f, "TIMELY_UPDATE"),
//         }
//     }
// }