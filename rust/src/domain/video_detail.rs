use crate::app::database::default_false;
use crate::domain::dtos::VideoVo;
use crate::domain::enumeration::AccessType;
use crate::impl_select_page_by_condition;
use crate::{
    app::{
        config::CC,
        response::{R, RB},
    },
    plus,
};

use rbatis::{Page, PageRequest, crud, executor::Executor, html_sql, impled, rbdc::DateTime};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct SingleMatch {
    pub match_type: Option<AccessType>,

    #[serde(default)]
    pub tag: Vec<String>,

    #[serde(default)]
    pub desc: Vec<String>,

    #[serde(default)]
    pub title: Vec<String>,

    #[serde(default)]
    pub cover: Vec<String>,

    #[serde(default)]
    pub mid: Vec<u64>,

    #[serde(default)]
    pub tid: Vec<u64>,

    #[serde(default)]
    pub match_count: u32,
}

#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct ComplexMatch {
    pub match_type: Option<AccessType>,

    pub rule_name: Option<String>,

    #[serde(default)]
    pub tag: Vec<String>,

    #[serde(default)]
    pub desc: Vec<String>,

    #[serde(default)]
    pub title: Vec<String>,

    #[serde(default)]
    pub cover: Vec<String>,

    #[serde(default)]
    pub mid: Vec<u64>,

    #[serde(default)]
    pub tid: Vec<u64>,

    #[serde(default)]
    pub match_count: u32,
}

#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct AiMatch {
    pub match_type: AccessType,
    pub reason: String,
}

#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct MatchResult {
    pub single_match: Option<SingleMatch>,
    pub complex_match: Option<ComplexMatch>,
    pub ai_match: Option<AiMatch>,
    pub user_handle_reason: Option<String>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct VideoDetail {
    pub id: u64,
    pub tid: Option<u64>,
    pub tname: Option<String>,
    pub pic: Option<String>,
    /// 视频1p的id，模拟播放需要
    pub cid: u64,
    pub title: Option<String>,
    pub pubdate: Option<u64>,
    #[serde(rename = "desc")]
    pub desc_field: Option<String>,
    pub duration: Option<u32>,
    pub dynamic: Option<String>,
    pub bvid: String,
    pub owner_id: Option<u64>,
    /// 数字类型，未处理时是0，第一次机器处理是1，第二次用户处理是2， 机器处理。。。  以此类推，处理完毕是100
    pub handle_step: u64,
    pub handle_reason: Option<MatchResult>,
    /// 处理时间
    pub handle_time: Option<DateTime>,
    /// 点赞或者点踩还是其他
    pub handle_type: Option<AccessType>,
    pub created_date: Option<DateTime>,
    /// 标签，逗号分隔
    pub tag: Option<String>,
}
crud!(VideoDetail {}, "video_detail");
plus!(VideoDetail {});

#[html_sql("src/domain/table/video_detail.html")]
impl VideoDetail {
    pub async fn select_by_title_like(conn: &dyn Executor, name: &str) -> VideoDetail {
        impled!()
    }

    pub async fn select_page(conn: &dyn Executor) -> RB<Page<VideoDetail>> {
        impled!()
    }

    pub async fn select_page_by_tname_like(
        conn: &dyn Executor,
        tname: &str,
    ) -> RB<Page<VideoDetail>> {
        impled!()
    }

    pub async fn select_page_by_handle_reason_not_null(
        conn: &dyn Executor,
    ) -> RB<Page<VideoDetail>> {
        impled!()
    }


    pub async fn search_handle_videos(
        rb: &dyn Executor,
        request: &rbatis::PageRequest,
        title: Option<String>,
        desc: Option<String>,
        handle_step: u64,
        handle_type: Option<AccessType>,
    ) -> RB<Page<VideoDetail>> {
        impled!()
    }


     /// 批量更新视频的处理步骤和处理时间  
    pub async fn update_handle_step_by_ids(  
        rb: &dyn Executor,  
        ids: &[u64],  
        handle_step: u64,  
        handle_time: DateTime,  
    ) -> rbatis::Result<rbatis::rbdc::db::ExecResult> {  
        impled!()  
    }  
}

#[cfg(test)]
mod tests {
    use log::info;
    use rbatis::utils::table_util;
    use rbatis::{PageRequest, table_field_vec};
    use rbs::value;

    use crate::domain::dtos::VideoVo;
    use crate::domain::enumeration::AccessType;
    use crate::domain::owner::Owner;
    use crate::utils::collection_tool::VecGroupByExt;
    use crate::{app::config::CC, domain::video_detail::VideoDetail};

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_search_handle_videos() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        let search = Some("红色沙漠".to_string());

        let search_handle_videos = VideoDetail::search_handle_videos(&CC.rb, &PageRequest::new(1, 10), search.clone(), search, 1, Some(AccessType::WHITE)).await.unwrap();
        println!("{:#?}",search_handle_videos);
        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_find_vo_with_owner_by_id_in() {
        //第一句必须是这个
        crate::init().await;

        // table_util

        let id_arr = vec![305988942];
        let v_arr = VideoDetail::select_by_map(
            &CC.rb,
            value! {
                "id": id_arr,
            },
        )
        .await
        .unwrap();

        let owner_id: Vec<u64> = v_arr.iter().filter_map(|f| f.owner_id).collect();
        let mut id_owner_map = Owner::select_by_map(
            &CC.rb,
            value! {
                "id":owner_id
            },
        )
        .await
        .unwrap()
        .group_by(|f| Some(f.id));
        let collect: Vec<VideoVo> = v_arr
            .into_iter()
            .map(|f| {
                let mut v: VideoVo = f.into();

                v.owner_id.map(|f| {
                    id_owner_map.remove(&f).map(|mut a| {
                        a.pop().map(|s| {
                            v.owner = Some(s);
                        })
                    })
                });
                v
            })
            .collect();

        println!("{:#?}", collect);
        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_video_detail() {
        _ = fast_log::init(
            fast_log::Config::new()
                .console()
                .level(log::LevelFilter::Debug),
        );
        CC.init().await;

        let page_request = PageRequest::new(1, 10); // 第一页，每页10条
        //let result = VideoDetail::select_page(&CONTEXT.rb, &page_request).await.unwrap();
        // let result = VideoDetail::select_page_by_name(&CONTEXT.rb, &page_request,"单机游戏").await.unwrap();

        // let result = VideoDetail::select_by_id(&CONTEXT.rb, 1729314424889581570).await.unwrap();
        // let result = VideoDetail::select_id_tname(
        //     &CONTEXT.rb,
        //     1729314424889581570,
        //     "id,tname,aid,bvid,handle,no_cache",
        // )
        // .await
        // .unwrap();

        // let result = VideoDetail::select_by_title_like(&CC.rb, "%不要%")
        //     .await
        //     .unwrap();

        // println!("{:#?}", result);
    }

   

    #[tokio::test]
    async fn test_select_handle_reason() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        // let mut page_no = 1;
        // loop {
        //     info!("第{page_no}页循环");
        //
        //     match VideoDetail::select_page_by_handle_reason_not_null(
        //         &CC.rb,
        //         &PageRequest::new(page_no, 2),
        //     )
        //     .await
        //     {
        //         Ok(page) => {
        //             if page.records.is_empty() {
        //                 break;
        //             }
        //         }
        //         Err(e) => {
        //             info!("第{page_no}页出错");
        //             info!("{:#?}", e);
        //         }
        //     }
        //
        //     page_no = page_no + 1;
        // }
        //
        // info!("共有{page_no}页");

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_find_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        let select_by_id = VideoDetail::select_by_id(&CC.rb, 961025315u64).await;
        println!("{:#?}", select_by_id);
        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_imp_page() {
        //第一句必须是这个
        crate::init().await;

        let p = PageRequest::new(1, 2);

        // 基本条件查询
        let condition = value! {
            "handle_type": AccessType::WHITE,
            // "column": ["*"],
            // "order_by": "created_date desc"
        };
        //在这中间编写测试代码
        let select_page_by_condition = VideoDetail::select_page_by_condition(&CC.rb, &p, condition)
            .await
            .unwrap();
        println!("{:#?}", select_page_by_condition);

        //最后一句必须是这个
        log::logger().flush();
    }
}
