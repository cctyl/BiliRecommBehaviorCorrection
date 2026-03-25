use axum::{Router, debug_handler};
use log::info;
use rbs::value;

use crate::{api::bili, app::{config::CC, response::{OkRespExt, R, RR}}, domain::owner::Owner, extractor::path::MyPath};

pub fn create_router() -> Router {
    Router::new().route(
        "/getUserNameByMid/{mid}",
        axum::routing::get(get_user_name_by_mid),
    )
}



/// 根据mid 获取用户名
pub async fn get_user_name_by_mid(MyPath(mid):MyPath<String>)->RR<String>{


    let mut name ;

    if let Some(owner) = Owner::select_one_by_condition(&CC.rb, value! {"mid":&mid}).await?{
        name = owner.name;
    }else {
        //调用接口查询
        name = bili::get_user_name_by_mid(mid).await?;
    }

    RR::success(name)
}
