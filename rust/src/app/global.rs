use std::{
    collections::HashMap,
    sync::{LazyLock, Mutex, OnceLock},
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
