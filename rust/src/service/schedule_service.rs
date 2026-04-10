use chrono::{Local, Timelike};
use log::{debug, error, info};
use tokio_cron_scheduler::{Job, JobScheduler, job};

use crate::{app::response::R, domain::video_detail::VideoDetail, service::task_service};

/// 创建并启动定时任务
pub async fn init_scheduler() {
    let mut sched = JobScheduler::new().await.expect("定时任务调度器创建失败");

    //1. Add basic cron job  同步任务
    // let job = Job::new("1/10 * * * * *", |_uuid, _l| {
    //     println!("I run every 10 seconds");
    // })
    // .unwrap();
    // sched.add(job).await.unwrap();

    //2. 异步任务
    // let job1 = Job::new_async("1/7 * * * * *", |uuid, mut l| {
    //     Box::pin(async move {
    //         println!("I run async every 7 seconds");

    //         // Query the next execution time for this job
    //         let next_tick = l.next_tick_for_job(uuid).await;
    //         match next_tick {
    //             Ok(Some(ts)) => println!("Next time for 7s job is {:?}", ts),
    //             _ => println!("Could not get next tick for 7s job"),
    //         }
    //     })
    // })
    // .expect("创建定时任务失败");

    let job1 = Job::new_async("0 0 * * * *", |uuid, mut l| {
        Box::pin(async move {
            
            let hour = Local::now().hour();
            info!("hour={} 检查一次任务执行情况",hour);
            
            match task_service::check_and_run_task(hour).await {
                Ok(_) => {
                    info!("hour={}  执行成功",hour);
                },
                Err(e) => error!("hour={}  执行失败！ {:#?}",hour,e),
            }


        })
    })
    .expect("创建定时任务失败");

    sched.add(job1).await.expect("添加定时任务失败");

    sched.start().await.expect("启动定时任务调度器失败");
}
