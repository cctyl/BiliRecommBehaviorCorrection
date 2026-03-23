use crate::{app::{config::CC, response::R}, entity::models::Region};

/// 查询分区列表
pub(crate) async fn list() -> R<Vec<Region>> {
    R::Ok(Region::select_all(&CC.rb).await?)
}
