use rbatis::crud;
use serde::{Deserialize, Serialize};

use crate::plus;



#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Owner {
    pub id: u64,
    pub name: String,
    pub face: Option<String>,
}
crud!(Owner {}, "owner");
plus!(Owner {});
