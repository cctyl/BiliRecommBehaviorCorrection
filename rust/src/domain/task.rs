
use rbatis::executor::Executor;
use rbatis::rbdc::db::ExecResult;
use rbatis::{impled, sql};
use rbatis::{crud, rbdc::DateTime};
use serde::{Deserialize, Serialize};
use crate::app::database::bool_or_int;

use crate::{domain::enumeration::TaskStatus, plus, utils::id::generate_id};
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Task {
    pub id: String,
    pub last_run_time: Option<DateTime>,
    pub current_run_status: TaskStatus,
    pub total_run_count: Option<u32>,
    pub last_run_duration: Option<u32>,
    pub task_name: Option<String>,
    pub scheduled_hour: i32,
    #[serde(deserialize_with = "bool_or_int")]
    pub is_enabled: bool,
    pub class_method_name: String,
    pub description: Option<String>,
    pub img: Option<String>,
}
crud!(Task {}, "task");
plus!(Task {});

impl Task {
    pub fn default() -> Self {
        Task {
            id: generate_id(),
            last_run_time: None,
            current_run_status: TaskStatus::STOPPED,
            total_run_count: None,
            last_run_duration: None,
            task_name: None,
            scheduled_hour: 0,
            is_enabled: false,
            class_method_name: String::from("default_class_name"),
            description: None,
            img: None,
        }
    }


    #[sql("update task set current_run_status = ? ")]
    pub async fn update_task_state(rb: &dyn Executor,status: TaskStatus) -> ExecResult {
        impled!()
    }



    #[sql("update task set is_enabled = ? ")]
    pub async fn update_task_is_enabled(rb: &dyn Executor,is_enabled: bool) -> ExecResult {
        impled!()
    }
}
