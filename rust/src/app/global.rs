use std::{
    collections::HashMap,
    sync::{LazyLock,  MutexGuard, OnceLock},
};
use tokio::time::{sleep, Duration};
use log::error;
use tokio::sync::RwLock;
use crate::app::response::R;

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
