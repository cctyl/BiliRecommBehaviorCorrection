use log::{error, info};
use std::collections::HashMap;
use std::future::Future;
use std::sync::{Arc, RwLock};
use tokio::runtime::Handle;
use tokio::sync::mpsc;
use tokio::task::JoinHandle;

/// 任务处理器
// async fn task_processor<F, Fut>(name: String, task: F)
// where
//     F: FnOnce() -> Fut + Send + 'static,
//     Fut: Future<Output = ()> + Send + 'static,
// {
//     tokio::spawn(async move {
//         log::info!("task processor start: {}", name);
//         task().await;
//     });
// }

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
        Fut: Future<Output = R<()>> + Send + 'static,
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
            let method_name_clone = method_name.clone();
            let handle = self.runtime_handle.spawn(async move {
                match task().await {
                    Ok(_) => info!("任务执行成功: {}", method_name_clone),
                    Err(e) => error!("任务执行失败: {},错误:{:#?}", method_name_clone,e),
                }
            });
            map.insert(method_name, handle);
        }

        true
    }
}

use std::sync::LazyLock;

use crate::app::response::R;

// 全局任务池实例
pub static TASK_POOL: LazyLock<TaskPool> = LazyLock::new(|| TaskPool::new());

// src/app/task_pool.rs (在文件末尾添加以下测试模块)

#[cfg(test)]
mod tests {
    use super::*;
    use tokio::time::{Duration, sleep};

    #[tokio::test]
    async fn test_put_if_absent_new_task() {
        let task_pool = TaskPool::new();
        let method_name = "test_task".to_string();

        // 测试添加新任务
        let result = task_pool.put_if_absent(method_name.clone(), || async {
            sleep(Duration::from_millis(100)).await;
            R::Ok(())
        });

        assert!(result);
        assert!(task_pool.exists_running_task(&method_name));
    }

    #[tokio::test]
    async fn test_put_if_absent_existing_running_task() {
        let task_pool = TaskPool::new();
        let method_name = "test_task".to_string();

        // 添加第一个任务
        task_pool.put_if_absent(method_name.clone(), || async {
            sleep(Duration::from_millis(100)).await;
            R::Ok(())
        });

        // 尝试添加同名任务（应该失败）
        let result = task_pool.put_if_absent(method_name.clone(), || async {
            sleep(Duration::from_millis(50)).await;
            R::Ok(())
        });

        assert!(!result);
        assert!(task_pool.exists_running_task(&method_name));
    }

    #[tokio::test]
    async fn test_put_if_absent_finished_task() {
        let task_pool = TaskPool::new();
        let method_name = "test_task".to_string();

        // 添加一个会很快完成的任务
        task_pool.put_if_absent(method_name.clone(), || async {
            // 不等待，立即完成
            R::Ok(())
        });

        // 等待任务完成
        sleep(Duration::from_millis(10)).await;

        // 现在应该可以添加同名任务
        let result = task_pool.put_if_absent(method_name.clone(), || async {
            sleep(Duration::from_millis(50)).await;
            R::Ok(())
        });

        assert!(result);
        assert!(task_pool.exists_running_task(&method_name));
    }

    #[tokio::test]
    async fn test_exists_running_task() {
        let task_pool = TaskPool::new();
        let method_name = "test_task".to_string();

        // 任务不存在时
        assert!(!task_pool.exists_running_task(&method_name));

        // 添加任务后
        task_pool.put_if_absent(method_name.clone(), || async {
            sleep(Duration::from_millis(100)).await;
            R::Ok(())
        });
        assert!(task_pool.exists_running_task(&method_name));

        // 等待任务完成后
        sleep(Duration::from_millis(150)).await;
        assert!(!task_pool.exists_running_task(&method_name));
    }

    #[tokio::test]
    async fn test_get_running_task_names() {
        let task_pool = TaskPool::new();

        // 初始状态没有任务
        assert_eq!(task_pool.get_running_task_names().len(), 0);

        // 添加几个任务
        task_pool.put_if_absent("task1".to_string(), || async {
            sleep(Duration::from_millis(100)).await;
            R::Ok(())
        });

        task_pool.put_if_absent("task2".to_string(), || async {
            sleep(Duration::from_millis(200)).await;
            R::Ok(())
        });

        task_pool.put_if_absent("task3".to_string(), || async {
            // 立即完成的任务
            R::Ok(())
        });

        // 等待第三个任务完成
        sleep(Duration::from_millis(10)).await;

        let running_tasks = task_pool.get_running_task_names();
        assert_eq!(running_tasks.len(), 2);
        assert!(running_tasks.contains(&"task1".to_string()));
        assert!(running_tasks.contains(&"task2".to_string()));
        assert!(!running_tasks.contains(&"task3".to_string()));
    }

    #[tokio::test]
    async fn test_concurrent_multiple_tasks() {
        let task_pool = TaskPool::new();

        // 添加多个不同名称的任务
        let task_names = vec![
            "concurrent_task_1".to_string(),
            "concurrent_task_2".to_string(),
            "concurrent_task_3".to_string(),
            "concurrent_task_4".to_string(),
            "concurrent_task_5".to_string(),
        ];

        // 启动多个并发任务，每个任务运行不同时长
        for (i, name) in task_names.iter().enumerate() {
            let duration = Duration::from_millis(((i + 1) * 50).try_into().unwrap()); // 不同时长: 50ms, 100ms, 150ms, 200ms, 250ms

            println!("Starting task: {}, time:{}", name, duration.as_millis());

            let name = name.clone();
            task_pool.put_if_absent(name.clone(), move || async move {
                sleep(duration).await;
                println!("Task completed: {}", name);
                R::Ok(())
            });
        }

        // 验证所有任务都已启动并在运行中
        let running_tasks = task_pool.get_running_task_names();
        assert_eq!(running_tasks.len(), 5);

        for name in &task_names {
            assert!(running_tasks.contains(name));
            assert!(task_pool.exists_running_task(name));
        }

        // 等待一段时间（150ms），部分任务应该完成
        sleep(Duration::from_millis(180)).await;
        println!("等待了180ms");
        // 检查剩余运行中的任务
        let running_tasks = task_pool.get_running_task_names();
        println!("Running tasks: {:?}", running_tasks);
        // 前3个任务应该已经完成了(50ms, 100ms, 150ms)
        assert_eq!(running_tasks.len(), 2);
        assert!(running_tasks.contains(&"concurrent_task_4".to_string()));
        assert!(running_tasks.contains(&"concurrent_task_5".to_string()));

        // 等待所有任务完成
        sleep(Duration::from_millis(150)).await;

        // 验证没有任务在运行
        let running_tasks = task_pool.get_running_task_names();
        assert_eq!(running_tasks.len(), 0);

        // 验证所有任务都不在运行状态
        for name in &task_names {
            assert!(!task_pool.exists_running_task(name));
        }
    }
}
