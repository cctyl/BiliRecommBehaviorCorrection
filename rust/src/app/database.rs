use std::sync::{LazyLock, OnceLock};

use rbatis::RBatis;
use rbdc_sqlite::Driver;
use rbdc_sqlite::driver::SqliteDriver;
use tokio::runtime::Runtime;

use crate::app::config::Config;

pub struct AppContext {
    pub rb: RBatis,
    pub config: Config,
}
pub static CONTEXT: LazyLock<AppContext> = LazyLock::new(|| AppContext {
    rb: RBatis::new(),
    config: Config::new(),
});

impl AppContext {
    pub async fn init(&self) {
        &self
            .rb
            .link(SqliteDriver {}, &self.config.db_url)
            .await
            .unwrap();
    }
}
