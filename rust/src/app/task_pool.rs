use std::collections::HashMap;
use std::future::Future;
use std::sync::{Arc, RwLock};
use tokio::runtime::Handle;
use tokio::sync::mpsc;
use tokio::task::JoinHandle;

/// 任务处理器
async fn task_processor<F, Fut>(name: String, task: F)
where
    F: FnOnce() -> Fut + Send + 'static,
    Fut: Future<Output = ()> + Send + 'static,
{
    tokio::spawn(async move {
        log::info!("task processor start: {}", name);
        task().await;
    });
}

/// 任务池
///
/// 用于管理和跟踪异步任务的执行状态
pub struct TaskPool {
    // 方法名与对应任务的映射
    method_name_task_map: Arc<RwLock<HashMap<String, JoinHandle<()>>>>,
    // 单线程执行器
    runtime_handle: Handle,
}

impl TaskPool {
    /// 创建一个新的任务池
    pub fn new() -> Self {
        Self {
            method_name_task_map: Arc::new(RwLock::new(HashMap::new())),
            runtime_handle: Handle::current(),
        }
    }

    /// 获取所有正在运行的任务名称
    pub fn get_running_task_names(&self) -> Vec<String> {
        let map = self.method_name_task_map.read().unwrap();
        map.iter()
            .filter(|(_, handle)| !handle.is_finished())
            .map(|(name, _)| name.clone())
            .collect()
    }

    /// 检查指定方法名的任务是否存在且正在运行
    pub fn exists_running_task(&self, method_name: &str) -> bool {
        let map = self.method_name_task_map.read().unwrap();
        match map.get(method_name) {
            Some(handle) => !handle.is_finished(),
            None => false,
        }
    }

    /// 如果不存在运行中的任务，则添加新任务
    pub fn put_if_absent<F, Fut>(&self, method_name: String, task: F) -> bool
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = ()> + Send + 'static,
    {
        // 先检查是否存在运行中的任务
        {
            let map = self.method_name_task_map.read().unwrap();
            if let Some(handle) = map.get(&method_name) {
                if !handle.is_finished() {
                    return false;
                }
            }
        }

        // 添加新任务
        {
            let mut map = self.method_name_task_map.write().unwrap();
            let handle = self.runtime_handle.spawn(async move {
                task().await;
            });
            map.insert(method_name, handle);
        }

        true
    }
}

use std::sync::LazyLock;

// 全局任务池实例
pub static TASK_POOL: LazyLock<TaskPool> = LazyLock::new(|| TaskPool::new());
