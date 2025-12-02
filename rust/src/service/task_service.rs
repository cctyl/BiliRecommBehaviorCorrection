use std::time::Instant;

use rbatis::{
    executor::Executor,
    impled, py_sql,
    rbdc::{DateTime, db::ExecResult},
    sql,
};
use rbs::value;

use crate::{
    app::{database::CC, error::HttpError, response::R, task_pool::TASK_POOL},
    entity::{enumeration::TaskStatus, models::Task},
};

/// 不存在则新增该任务
pub async fn add_if_not_exist(name: &String) -> R<()> {
    let tasks = find_by_class_method_name(&name).await?;

    if tasks.is_none() {
        let mut t = Task::default();
        t.class_method_name = Some(name.clone());
        t.is_enabled = Some(true);
        t.current_run_status = Some(TaskStatus::STOPPED);
        t.total_run_count = Some(0);
        t.scheduled_hour = Some(-1);
        Task::insert(&CC.rb, &t).await?;
    }

    R::Ok(())
}

/// 更新任务状态
#[sql("UPDATE task SET current_run_status = ? WHERE class_method_name = ?")]
async fn update_task_status(
    rb: &dyn Executor,
    new_status: TaskStatus,
    class_method_name: &str,
) -> Result<ExecResult, rbatis::Error> {
}

/// 更新任务最后运行时间 updateLastRunTime

#[sql("UPDATE task SET last_run_time = ? WHERE class_method_name = ?")]
async fn update_last_run_time(
    rb: &dyn Executor,
    last_run_time: DateTime,
    class_method_name: &str,
) -> Result<ExecResult, rbatis::Error> {
}

/// 通过任务名称查找任务
pub async fn find_by_class_method_name(name: &str) -> R<Option<Task>> {
    let mut tasks = select_one_by_condition(&CC.rb, value! {"class_method_name":name}).await?;

    R::Ok(tasks)
}

use rbatis::crud_traits::ValueOperatorSql;
#[py_sql(
    "`select * from task`  
     trim end=' where ':  
       ` where `  
       trim ' and ': for key,item in condition:  
                    if item == null:  
                       continue:  
                    if !item.is_array():  
                      ` and ${key.operator_sql()}#{item}`  
                    if item.is_array():  
                      ` and ${key} in (`  
                         trim ',': for _,item_array in item:  
                              #{item_array},  
                      `)`  
     ` limit 1`"
)]
async fn select_one_by_condition(
    rb: &dyn Executor,
    condition: rbs::Value,
) -> Result<Option<Task>, rbatis::Error> {
    impled!()
}



/// 执行任务并记录任务信息
pub async fn do_task<F, Fut>(method_name: String, task: F) -> R<bool>
where
    F: FnOnce() -> Fut + Send + 'static,
    Fut: Future<Output = R<()>> + Send + 'static,
{
    // 需要记录任务的执行情况
    add_if_not_exist(&method_name).await?;
    update_task_status(&CC.rb, TaskStatus::WAITING, &method_name).await?;
    update_last_run_time(&CC.rb, DateTime::now(), &method_name).await?;

    let method_name_move = method_name.clone();
    R::Ok(TASK_POOL.put_if_absent(method_name_move, async move || {
        let start = Instant::now();
        update_task_status(&CC.rb, TaskStatus::RUNNING, &method_name).await?;
        task().await?;

        let end = Instant::now();
        let millis = end.duration_since(start).as_millis();

        let mut t = find_by_class_method_name(&method_name).await?;
        if let Some(mut t) = t {
            t.last_run_time = Some(DateTime::now());
            t.total_run_count = Some(t.total_run_count.unwrap_or(0) + 1);
            t.last_run_duration = Some(millis as u32);
            t.current_run_status = Some(TaskStatus::STOPPED);
            Task::update_by_id(&CC.rb, &t).await?;
        }

        R::Ok(())
    }))
}










#[cfg(test)]
mod tests {
    use rbatis::rbdc::DateTime;
    use rbs::value;

    use crate::{
        app::{database::CC, response::R}, entity::{enumeration::TaskStatus, models::Task}, impl_select_by_id, service::task_service::update_task_status
    };

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }
    

    #[tokio::test]
    async fn test_impl_delete_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        Task::delete_by_id(&CC.rb, "11679171886120965").await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }
    


    #[tokio::test]
    async fn test_impl_update_by_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let id = "11679233969684485";
        let mut t: Task = Task::select_by_id(&CC.rb, id).await.unwrap().unwrap();

        t.last_run_time = Some(DateTime::now());
        t.total_run_count = Some( 2001);
        t.class_method_name = Some( String::from("111222测试的类方法名啊") );
        t.task_name = Some( String::from("111222测试的名称啊"));

        Task::update_by_id(&CC.rb,&t).await.unwrap();

        //最后一句必须是这个
        log::logger().flush();
    }
    
    #[tokio::test]
    async fn test_impl_select_by_id() {
        //第一句必须是这个
        crate::init().await;
        
        //在这中间编写测试代码
        let task = Task::select_by_id(&CC.rb, "5")
            .await
            .unwrap();

        println!("task: {:?}", task);


        

        //最后一句必须是这个
        log::logger().flush();
    }



    //测试 impl_select_one_by_condition
    #[tokio::test]
    async fn test_impl_select_one_by_condition() {
        //第一句必须是这个
        crate::init().await;

       let select_one_by_condition = Task::select_one_by_condition(&CC.rb, value!{"id":"1883877101111631873"}).await.unwrap();

        println!("select_one_by_condition: {:?}", select_one_by_condition);


        //最后一句必须是这个
        log::logger().flush();
    }


    //测试 add_if_not_exist
    #[tokio::test]
    async fn test_add_if_not_exist() {
        crate::init().await;

        let name = "test_add_if_not_exist".to_string();

        let r = super::add_if_not_exist(&name).await;

        assert!(r.is_ok());

        let tasks = Task::select_by_map(&CC.rb, value! {"class_method_name":&name})
            .await
            .unwrap();

        assert_eq!(tasks.len(), 1);

        assert_eq!(tasks[0].class_method_name.as_ref().unwrap(), &name);

        assert_eq!(tasks[0].is_enabled.unwrap(), true);

        assert_eq!(tasks[0].current_run_status.unwrap(), TaskStatus::STOPPED);

        assert_eq!(tasks[0].total_run_count.unwrap(), 0);

        assert_eq!(tasks[0].scheduled_hour.unwrap(), -1);
        log::logger().flush();
    }

    // 测试 update_task_status
    #[tokio::test]
    async fn test_update_task_status() {
        crate::init().await;

        let name = "test_update_task_status".to_string();

        let r = super::add_if_not_exist(&name).await;

        assert!(r.is_ok());
        let result = update_task_status(&CC.rb, TaskStatus::WAITING, &name)
            .await
            .unwrap();
        assert_eq!(result.rows_affected, 1);
        let tasks = Task::select_by_map(&CC.rb, value! {"class_method_name":&name})
            .await
            .unwrap();
        assert_eq!(tasks.len(), 1);
        assert_eq!(tasks[0].current_run_status.unwrap(), TaskStatus::WAITING);

        log::logger().flush();
    }

    // 测试 find_by_class_method_name
    #[tokio::test]
    async fn test_find_by_class_method_name() {
        crate::init().await;

        let name = "test_find_by_class_method_name".to_string();

        // 先确保任务存在
        super::add_if_not_exist(&name).await.unwrap();

        // 测试能找到已存在的任务
        let result = super::find_by_class_method_name(&name).await;
        assert!(result.is_ok());
        let task = result.unwrap().unwrap();
        assert_eq!(task.class_method_name.unwrap(), name);

        // 测试找不到不存在的任务
        let not_exist_name = "not_exist_task_name";
        let result = super::find_by_class_method_name(not_exist_name).await;
        assert!(result.is_ok());
        assert!(result.unwrap().is_none());
        log::logger().flush();
    }

    // 测试 do_task
    #[tokio::test]
    async fn test_do_task() {
        crate::init().await;

        let name = "test_do_task".to_string();

        // 定义一个简单的测试任务
        let task_fn = || async {
            // 模拟一些工作
            tokio::time::sleep(tokio::time::Duration::from_millis(10)).await;

            println!("任务执行完毕");
            R::Ok(())
        };

        // 执行任务
        let result = super::do_task(name.clone(), task_fn).await;

        // 验证任务被成功提交
        assert!(result.is_ok());
        assert_eq!(result.unwrap(), true); // TASK_POOL.put_if_absent 应该返回true表示任务被添加

        // 等待一段时间确保任务执行完成
        tokio::time::sleep(tokio::time::Duration::from_millis(50)).await;

        // 验证任务状态已被更新
        let task_result = super::find_by_class_method_name(&name).await;
        assert!(task_result.is_ok());
        let task = task_result.unwrap().unwrap();

        // 验证任务状态是 STOPPED (因为任务已经执行完毕)
        println!("任务状态: {:#?}", task);
        assert_eq!(task.current_run_status.unwrap(), TaskStatus::STOPPED);

        // 验证总运行次数增加
        assert_eq!(task.total_run_count.unwrap(), 1);

        // 验证运行时间已被记录
        assert!(task.last_run_duration.unwrap() > 0);

        log::logger().flush();
    }

    // 测试 select_one_by_condition
    #[tokio::test]
    async fn test_select_one_by_condition() {
        crate::init().await;

        let name = "test_select_one_by_condition".to_string();

        // 先确保任务存在
        super::add_if_not_exist(&name).await.unwrap();

        // 测试通过条件查找任务 - 存在的任务
        let condition = value! {"class_method_name": &name};
        let result = super::select_one_by_condition(&CC.rb, condition).await;
        assert!(result.is_ok());
        let task = result.unwrap();
        assert!(task.is_some());
        println!("任务: {:#?}", task);
        assert_eq!(task.unwrap().class_method_name.unwrap(), name);

        // 测试通过条件查找任务 - 不存在的任务
        let not_exist_name = "not_exist_task_name";
        let condition = value! {"class_method_name": not_exist_name};
        let result = super::select_one_by_condition(&CC.rb, condition).await;
        assert!(result.is_ok());
        assert!(result.unwrap().is_none());

        log::logger().flush();
    }
}
