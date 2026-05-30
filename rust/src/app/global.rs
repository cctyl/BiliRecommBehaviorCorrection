use std::{
    collections::HashMap,
    sync::{LazyLock,  MutexGuard, OnceLock},
    time::Instant,
};
use tokio::time::{sleep, Duration};
use log::error;
use tokio::sync::RwLock;
use sysinfo::{ProcessesToUpdate, System};
use crate::app::response::R;

/// 程序启动时间点
pub static APP_START_INSTANT: OnceLock<Instant> = OnceLock::new();

/// 全局 sysinfo System 实例（只创建一次，后续复用，避免每次请求都 new System）
pub static GLOBAL_SYSTEM: LazyLock<RwLock<System>> = LazyLock::new(|| {
    let mut sys = System::new_all();
    sys.refresh_processes(ProcessesToUpdate::All, true);
    RwLock::new(sys)
});

#[derive(Debug)]
pub struct GlobalState {
    pub common_header_map: HashMap<String, String>,
}

pub static GLOBAL_STATE: LazyLock<RwLock<GlobalState>> = LazyLock::new(|| {
    RwLock::new(GlobalState {
        common_header_map: HashMap::new(),
    })
});

// 定义处理全局状态的trait
pub trait GlobalStateHandler<Args,Output>{
    // 具体业务处理方法
    async fn handle(&self, state: &mut GlobalState,args:Args)->R<Output>{
        todo!("未实现")
    }

    // 默认方法，封装锁的获取与释放
    async fn processw(&self,args:Args) -> R<Output> {
        let mut guard = GLOBAL_STATE.write().await;
        self.handle(&mut *guard,args).await
    }

    // 具体业务处理方法
    async fn read(&self, state: &GlobalState,args:Args)->R<Output>{
        todo!("未实现")
    }


    // 默认方法，封装锁的获取与释放
    async fn processr(&self,args:Args) -> R<Output> {
        let mut guard = GLOBAL_STATE.write().await;
        self.read(&*guard,args).await
        
    }

}
