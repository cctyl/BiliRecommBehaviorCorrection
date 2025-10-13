use rbatis::{executor::Executor, rbdc::{db::ExecResult, DateTime}, sql};
use rbs::value;

use crate::{app::{database::CONTEXT, response::R, task_pool::TASK_POOL}, entity::{enumeration::TaskStatus, models::Task}};




/// 不存在则新增该任务
pub async fn add_if_not_exist(name:&String)->R<()>{

    let tasks = Task::select_by_map(&CONTEXT.rb, value! {"class_method_name":name}).await?;

    if tasks.is_empty() {
        
        let mut t = Task::default();
        t.class_method_name = Some(name.clone());
        t.is_enabled = Some(true);
        t.current_run_status = Some(TaskStatus::STOPPED);
        t.total_run_count = Some(0);
        t.scheduled_hour = Some(-1);
        Task::insert(&CONTEXT.rb, &t).await?;
    }

    R::Ok(())
}

/// 更新任务状态
#[sql("UPDATE task SET current_run_status = ? WHERE class_method_name = ?")]  
async fn update_task_status(  
    rb: &dyn Executor,   
    new_status: TaskStatus,  
    class_method_name: &str  
) -> Result<ExecResult, rbatis::Error> {}  
  
/// 更新任务最后运行时间 updateLastRunTime

#[sql("UPDATE task SET last_run_time = ? WHERE class_method_name = ?")]
async fn update_last_run_time(  
    rb: &dyn Executor,   
    last_run_time: DateTime,  
    class_method_name: &str  
) -> Result<ExecResult, rbatis::Error> {}

pub async fn do_task<F, Fut>( method_name: String, task: F) -> R<bool>
where
    F: FnOnce() -> Fut + Send + 'static,
    Fut: Future<Output = ()> + Send + 'static,
{



    //TODO 需要记录任务的执行情况
    add_if_not_exist(&method_name).await?;
    update_task_status(&CONTEXT.rb, TaskStatus::WAITING, &method_name).await?;
    update_last_run_time(&CONTEXT.rb, DateTime::now(), &method_name).await?;


    let method_name_move = method_name.clone();
    R::Ok(TASK_POOL.put_if_absent(method_name_move,async move ||{

        update_task_status(&CONTEXT.rb, TaskStatus::RUNNING, &method_name).await?;
        task().await;

        R::Ok(())
    }))

}


#[cfg(test)]
mod tests{
    use rbs::value;

    use crate::{app::database::CONTEXT, entity::{enumeration::TaskStatus, models::Task}, service::task_service::update_task_status};



    #[tokio::test]
    async fn example() {
        crate::init().await;
       
        log::logger().flush();
    }


    //测试 add_if_not_exist
    #[tokio::test]
    async fn test_add_if_not_exist() {
        crate::init().await;

        let name = "test_add_if_not_exist".to_string();

        let r = super::add_if_not_exist(&name).await;

        assert!(r.is_ok());

        let tasks = Task::select_by_map(&CONTEXT.rb, value! {"class_method_name":&name}).await.unwrap();

        assert_eq!(tasks.len(), 1);

        assert_eq!(tasks[0].class_method_name.as_ref().unwrap(),& name);

        assert_eq!(tasks[0].is_enabled.unwrap(), true);

        assert_eq!(tasks[0].current_run_status.unwrap(), TaskStatus::STOPPED);

        assert_eq!(tasks[0].total_run_count.unwrap(), 0);

        assert_eq!(tasks[0].scheduled_hour.unwrap(), -1);

        
    }

    // 测试 update_task_status
    #[tokio::test]
    async fn test_update_task_status() {
        crate::init().await;

        let name = "test_update_task_status".to_string();

        let r = super::add_if_not_exist(&name).await;

        assert!(r.is_ok());
        let result = update_task_status(&CONTEXT.rb, TaskStatus::WAITING, &name).await.unwrap();
        assert_eq!(result.rows_affected, 1);
        let tasks = Task::select_by_map(&CONTEXT.rb, value! {"class_method_name":&name}).await.unwrap();
        assert_eq!(tasks.len(), 1);
        assert_eq!(tasks[0].current_run_status.unwrap(), TaskStatus::WAITING);

        
    }
    
}