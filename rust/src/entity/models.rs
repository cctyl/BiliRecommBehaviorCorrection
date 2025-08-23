


use std::fmt::Debug;
use rbatis::rbatis_codegen::IntoSql;
use rbatis::rbdc::datetime::DateTime;
use chrono::prelude::*;
use rbatis::{crud, impl_select};
use serde::{Deserialize, Deserializer, Serialize};

pub fn deserde_from_int<'de, D>(deserializer: D) -> Result<bool, D::Error>
where
    D: Deserializer<'de>,
{
    let i = u32::deserialize(deserializer)?;
    Ok(i == 1)
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct File{
    pub id:i64,
    pub name:String,
    pub doc_id:String,
    pub relative_path:String,
     #[serde(deserialize_with = "deserde_from_int")]
    pub is_directory:bool,
    pub md5:String,
}
crud!(File {});
impl_select!(File{select_all_by_relative_path_in(relative_path:&[&str]) -> Vec => "`where relative_path in ${relative_path.sql()}`"});