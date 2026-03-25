use rbs::value;

use crate::{app::{config::CC, response::R}, domain::region::Region};

/// 查询分区列表
pub(crate) async fn list() -> R<Vec<Region>> {
    R::Ok(Region::select_by_map(&CC.rb,value!{}).await?)
}
