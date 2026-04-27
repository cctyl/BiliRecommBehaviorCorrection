use log::{error, info};
use std::collections::HashMap;
use std::future::Future;

use tokio::runtime::Handle;
use tokio::sync::{mpsc, RwLock};
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
    pub async fn get_running_task_names(&self) -> Vec<String> {
        let map = self.method_name_task_map.read().await;
        map.iter()
            .filter(|(_, handle)| !handle.is_finished())
            .map(|(name, _)| name.clone())
            .collect()
    }

    /// 检查指定方法名的任务是否存在且正在运行
    pub async fn exists_running_task(&self, method_name: &str) -> bool {
        let map = self.method_name_task_map.read().await;
        match map.get(method_name) {
            Some(handle) => !handle.is_finished(),
            None => false,
        }
    }

    /// 如果不存在运行中的任务，则添加新任务
    pub async fn put_if_absent<F, Fut>(&self, method_name: String, task: F) -> bool
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = R<()>> + Send + 'static,
    {
        info!("put_if_absent {}", method_name);
        // 先检查是否存在运行中的任务
        {
            let map = self.method_name_task_map.read().await;
            if let Some(handle) = map.get(&method_name) {
                if !handle.is_finished() {
                    info!("存在相同的未完成的任务:{}",method_name);

                    let keys:Vec<&String> = map.keys().collect();
                    info!("当前map中存在的key为：{:#?}",keys);


                    return false;
                }
            }
        }
        // 添加新任务
        {
            
            let mut map = self.method_name_task_map.write().await;
            let method_name_clone = method_name.clone();
            info!("put_if_absent 添加新任务:{}",method_name);
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

    /// 优雅关闭：等待所有正在运行的任务结束
    pub async fn shutdown(&self) {
        // 1. 获取写锁，防止在关闭过程中有新任务加入，同时拿到所有句柄
        // 使用 drain() 可以取出并移除 HashMap 中的所有内容
        let mut map = self.method_name_task_map.write().await;
        let handles: Vec<_> = map.drain().map(|(_, handle)| handle).collect();

        // 释放锁，允许其他非写操作（如果有）继续，虽然此时 map 已空
        drop(map);

        println!("正在等待 {} 个任务完成...", handles.len());

        // 2. 并发等待所有任务结束
        // 使用 futures 库的 join_all 或者简单的循环 await
        for handle in handles {
            match handle.await {
                Ok(_) => {},
                Err(e) => eprintln!("任务运行出错: {:?}", e),
            }
        }

        println!("所有任务已完成，线程池已安全退出。");
    }
}

use std::sync::{Arc, LazyLock};

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
        }).await;

        assert!(!result);
        assert!(task_pool.exists_running_task(&method_name).await);
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
        }).await;

        assert!(result);
        assert!(task_pool.exists_running_task(&method_name).await);
    }

    #[tokio::test]
    async fn test_exists_running_task() {
        let task_pool = TaskPool::new();
        let method_name = "test_task".to_string();

        // 任务不存在时
        assert!(!task_pool.exists_running_task(&method_name).await);

        // 添加任务后
        task_pool.put_if_absent(method_name.clone(), || async {
            sleep(Duration::from_millis(100)).await;
            R::Ok(())
        });
        assert!(task_pool.exists_running_task(&method_name).await);

        // 等待任务完成后
        sleep(Duration::from_millis(150)).await;
        assert!(!task_pool.exists_running_task(&method_name).await);
    }

    #[tokio::test]
    async fn test_get_running_task_names() {
        let task_pool = TaskPool::new();

        // 初始状态没有任务
        assert_eq!(task_pool.get_running_task_names().await.len(), 0);

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

        let running_tasks = task_pool.get_running_task_names().await;
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
        let running_tasks = task_pool.get_running_task_names().await;
        assert_eq!(running_tasks.len(), 5);

        for name in &task_names {
            assert!(running_tasks.contains(name));
            assert!(task_pool.exists_running_task(name).await);
        }

        // 等待一段时间（150ms），部分任务应该完成
        sleep(Duration::from_millis(180)).await;
        println!("等待了180ms");
        // 检查剩余运行中的任务
        let running_tasks = task_pool.get_running_task_names().await;
        println!("Running tasks: {:?}", running_tasks);
        // 前3个任务应该已经完成了(50ms, 100ms, 150ms)
        assert_eq!(running_tasks.len(), 2);
        assert!(running_tasks.contains(&"concurrent_task_4".to_string()));
        assert!(running_tasks.contains(&"concurrent_task_5".to_string()));

        // 等待所有任务完成
        sleep(Duration::from_millis(150)).await;

        // 验证没有任务在运行
        let running_tasks = task_pool.get_running_task_names().await;
        assert_eq!(running_tasks.len(), 0);

        // 验证所有任务都不在运行状态
        for name in &task_names {
            assert!(!task_pool.exists_running_task(name).await);
        }
    }
}



#[cfg(test)]
mod test2{
    #[cfg(test)]
    mod tests {
        use super::*;
        use std::time::Duration;
        use tokio::time::sleep;
        use crate::app::error::HttpError;
        use crate::app::response::R;
        use crate::app::task_pool::TASK_POOL;
        use crate::service::task_service::do_task;

        // 测试1: 验证任务是否能正常执行完成
        #[tokio::test]
        async fn test_task_normal_execution() {
            crate::init().await;
            println!("\n=== 测试1: 正常任务执行 ===");


            let x = TASK_POOL.put_if_absent("test_task_normal_execution".to_string(), async move || {
                println!("  任务内部: 开始执行");
                sleep(Duration::from_millis(10000)).await;
                println!("  任务内部: 执行完成");
                R::Ok(())
            }).await;


            TASK_POOL.shutdown().await;
            println!("测试1完成\n");
            log::logger().flush();
        }

        // 测试2: 验证长时间运行的任务
        #[tokio::test]
        async fn test_task_long_running() {
            println!("\n=== 测试2: 长时间运行任务 ===");

            let start = std::time::Instant::now();
            let result = do_task("test_task_2".to_string(),  || async move {
                println!("  任务内部: 开始长时间任务");
                sleep(Duration::from_secs(2)).await;
                println!("  任务内部: 长时间任务完成");
                R::Ok(())
            }).await;

            assert!(result.is_ok());
            println!("任务已提交，等待完成...");
            sleep(Duration::from_secs(3)).await;
            println!("测试2耗时: {:?}", start.elapsed());
        }

        // 测试3: 验证重复提交相同任务（应该被去重）
        #[tokio::test]
        async fn test_task_deduplication() {
            println!("\n=== 测试3: 任务去重测试 ===");

            let task_name = "test_task_3".to_string();

            // 第一次提交
            let first_result = do_task(task_name.clone(),  || async move {
                println!("  任务3: 开始执行（第一次）");
                sleep(Duration::from_secs(1)).await;
                println!("  任务3: 执行完成");
                R::Ok(())
            }).await;
            println!("第一次提交结果: {:?}", first_result);

            // 立即第二次提交（应该被拒绝）
            let second_result = do_task(task_name.clone(),  || async move {
                println!("  这行不应该被执行！");
                R::Ok(())
            }).await;
            println!("第二次提交结果: {:?}", second_result);

            // 等待第一次任务完成
            sleep(Duration::from_secs(2)).await;

            // 第三次提交（应该成功）
            let third_result = do_task(task_name,  || async move {
                println!("  任务3: 重新执行（第三次）");
                R::Ok(())
            }).await;
            println!("第三次提交结果: {:?}", third_result);

            sleep(Duration::from_millis(500)).await;
            println!("测试3完成\n");
        }

        // 测试4: 测试任务失败的情况
        #[tokio::test]
        async fn test_task_failure() {
            println!("\n=== 测试4: 任务失败处理 ===");

            let result = do_task("test_task_4".to_string(),  || async move {
                println!("  任务4: 开始执行，即将失败");
                sleep(Duration::from_millis(100)).await;
                R::Err(HttpError::BadRequest("模拟失败".to_string()))
            }).await;

            assert!(result.is_ok()); // 提交应该成功
            sleep(Duration::from_millis(500)).await;
            println!("测试4完成\n");
        }

        // 测试5: 并发提交多个不同任务
        #[tokio::test]
        async fn test_concurrent_tasks() {
            println!("\n=== 测试5: 并发执行多个不同任务 ===");

            let tasks: Vec<_> = (0..5)
                .map(|i| {
                    let name = format!("concurrent_task_{}", i);
                    tokio::spawn(async move {
                        do_task(name.clone(), || async move {
                            println!("  任务 {}: 开始执行", name);
                            sleep(Duration::from_millis(500)).await;
                            println!("  任务 {}: 执行完成", name);
                            R::Ok(())
                        }).await
                    })
                })
                .collect();

            for task in tasks {
                let _ = task.await;
            }

            sleep(Duration::from_secs(2)).await;
            println!("测试5完成\n");
        }

        // 测试6: 快速连续提交相同任务（压力测试）
        #[tokio::test]
        async fn test_rapid_same_task_submission() {
            println!("\n=== 测试6: 快速连续提交相同任务 ===");

            let task_name = "test_task_6".to_string();
            let mut results = Vec::new();

            // 快速提交10次
            for i in 0..10 {
                let result = do_task(task_name.clone(),  move || async move {
                    println!("  任务6执行 (第{}次实际运行)", i);
                    sleep(Duration::from_millis(500)).await;
                    R::Ok(())
                }).await;
                results.push(result.is_ok());
            }

            let success_count = results.iter().filter(|&&x| x).count();
            println!("提交10次，成功添加任务次数: {}/10", success_count);
            assert!(success_count == 1, "应该只有第一次成功添加");

            sleep(Duration::from_secs(1)).await;
            println!("测试6完成\n");
        }

        // 测试7: 验证任务是否真的在异步执行
        #[tokio::test]
        async fn test_async_execution() {
            println!("\n=== 测试7: 验证异步执行 ===");

            let start = std::time::Instant::now();

            // 提交一个耗时任务
            do_task("async_task".to_string(), || async move {
                println!("  异步任务: 开始 (耗时3秒)");
                sleep(Duration::from_secs(3)).await;
                println!("  异步任务: 完成");
                R::Ok(())
            }).await.unwrap();

            // 立即打印，不应该等待任务完成
            println!("主线程: 任务已提交，继续执行 (耗时: {:?})", start.elapsed());
            assert!(start.elapsed() < Duration::from_millis(100));

            // 等待任务完成
            sleep(Duration::from_secs(4)).await;
            println!("测试7完成，总耗时: {:?}\n", start.elapsed());
        }
    }
}