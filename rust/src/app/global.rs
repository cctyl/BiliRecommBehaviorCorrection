use std::{
    collections::HashMap,
    sync::{LazyLock, Mutex, MutexGuard, OnceLock},
};

#[derive(Debug)]
pub struct GlobalState {
    pub common_header_map: HashMap<String, String>,
}

pub static GLOBAL_STATE: LazyLock<Mutex<GlobalState>> = LazyLock::new(|| {
    Mutex::new(GlobalState {
        common_header_map: HashMap::new(),
    })
});


// 定义处理全局状态的trait
pub trait GlobalStateHandler {
    // 具体业务处理方法
     async   fn handle(&self, state: &mut GlobalState);

    // 默认方法，封装锁的获取与释放
    async fn process(&self) -> Result<(), std::sync::PoisonError<MutexGuard<'_, GlobalState>>> {
        let mut guard = GLOBAL_STATE.lock()?; // 获取锁，处理可能的PoisonError
        self.handle(&mut *guard).await; // 调用具体业务处理
        Ok(()) // 返回结果
    }
}
