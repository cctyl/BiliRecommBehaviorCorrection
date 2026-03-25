
use rbatis::{crud, rbdc::DateTime};
use serde::{Deserialize, Serialize};
use crate::app::database::bool_or_int_opt;

use crate::{domain::enumeration::TaskStatus, plus, utils::id::generate_id};
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Task {
    pub id: String,
    pub last_run_time: Option<DateTime>,
    pub current_run_status: Option<TaskStatus>,
    pub total_run_count: Option<i32>,
    pub last_run_duration: Option<u32>,
    pub task_name: Option<String>,
    pub scheduled_hour: Option<i32>,
    #[serde(deserialize_with = "bool_or_int_opt")]
    pub is_enabled: Option<bool>,
    pub class_method_name: Option<String>,
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
            current_run_status: None,
            total_run_count: None,
            last_run_duration: None,
            task_name: None,
            scheduled_hour: None,
            is_enabled: None,
            class_method_name: None,
            description: None,
            img: None,
        }
    }
}
