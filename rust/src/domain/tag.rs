use rbatis::crud;
use serde::{Deserialize, Serialize};

use crate::plus;

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Tag {
    pub id: Option<String>,
    pub tag_id: i32,
    pub tag_name: String,
    pub content: Option<String>,
}
crud!(Tag {}, "tag");
