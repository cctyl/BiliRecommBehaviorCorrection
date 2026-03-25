use rbatis::{crud, rbdc::DateTime};
use serde::{Deserialize, Serialize};

use crate::{domain::enumeration::{AccessType, DictStatus, DictType}, plus, utils::id::generate_id};


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Dict {
    pub id: String,
    pub value: String,           //字典值
    pub access_type: AccessType, //黑名单或者白名单类型
    pub dict_type: DictType,     //字典的类型
    pub outer_id: Option<String>,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    #[serde(rename = "desc")]
    pub desc_field: Option<String>,
    pub status: DictStatus, //该字典当前的状态，
}
crud!(Dict {}, "dict");
plus!(Dict {});

impl Dict {
    pub fn new(
        value: String,
        access_type: AccessType,
        dict_type: DictType,
        outer_id: Option<String>,
        desc_field: Option<String>,
        status: DictStatus,
    ) -> Self {
        Dict {
            id: generate_id(),
            value,
            access_type,
            dict_type,
            outer_id,
            desc_field,
            created_date: Some(DateTime::now()),
            last_modified_date: Some(DateTime::now()),
            status,
        }
    }
}
