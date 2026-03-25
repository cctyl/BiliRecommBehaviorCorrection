use crate::app::database::bool_or_int;
use crate::app::database::bool_or_int_opt;
use crate::app::database::default_false;
use crate::domain::enumeration::AccessType;
use crate::impl_select_one_by_condition;
use crate::plus;
use crate::utils::id::generate_id;
use rbatis::Page;
use rbatis::PageRequest;
use rbatis::RBatis;
use rbatis::crud;
use rbatis::html_sql;
use rbatis::impled;
use rbatis::rbdc::Timestamp;
use rbatis::rbdc::types::DateTime;
use rbatis::sql;
use serde::{Deserialize, Serialize};

use crate::app::config::CC;

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct AssociateRule {
    pub id: String,
    pub info: String,
    pub created_date: Option<DateTime>,
    pub last_modified_date: Option<DateTime>,
    pub access_type: AccessType,
}
plus!(AssociateRule {});
crud!(AssociateRule {});

impl Default for AssociateRule {
    fn default() -> Self {
        Self {
            id: generate_id(),
            info: Default::default(),
            created_date: Default::default(),
            last_modified_date: Default::default(),
            access_type: Default::default(),
        }
    }
}

#[html_sql("src/domain/table/associate_rule.html")]
impl AssociateRule {
    pub async fn select_page_by_access_type(
        conn: &dyn Executor,
          request:&rbatis::PageRequest,
        access_type: AccessType,
    ) -> RB<Page<AssociateRule>> {
        impled!()
    }
}
#[cfg(test)]
mod tests{
    use log::info;
    use rbatis::PageRequest;

    use crate::{app::config::CC, domain::{associate_rule::AssociateRule, enumeration::AccessType}};



    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;
        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }
    #[tokio::test]
    async fn test_select_page_by_access_type() {
        //第一句必须是这个
        crate::init().await;
        //在这中间编写测试代码

        let select_page_by_access_type = AssociateRule::select_page_by_access_type(&CC.rb,
            &PageRequest::new(1, 1),
            AccessType::BLACK).await.unwrap();

        info!("{:#?}",select_page_by_access_type);

        //最后一句必须是这个
        log::logger().flush();
    }


}