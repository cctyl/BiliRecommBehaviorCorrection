
use rbatis::crud;
use serde::{Deserialize, Serialize};

use crate::plus;
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Region{
    pub tid:u32,
    pub code:String,
    pub name:String,
    pub desc:Option<String>,
    pub router:Option<String>,
    pub pid:u32
}
plus!(Region{});
crud!(Region{});
