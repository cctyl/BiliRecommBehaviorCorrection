// src/utils/thread_util.rs

use tokio::time::{sleep, Duration};

/// 线程工具类，提供各种休眠方法
pub struct ThreadUtil;

impl ThreadUtil {
    /// 线程休眠指定秒数
    /// 
    /// # Arguments
    /// * `seconds` - 休眠的秒数
    pub async fn sleep(seconds: u64) {
        sleep(Duration::from_secs(seconds)).await;
    }

    /// 休眠2秒
    pub async fn s2() {
        Self::sleep(2).await;
    }

    /// 休眠5秒
    pub async fn s5() {
        Self::sleep(5).await;
    }

    /// 休眠1秒
    pub async fn s1() {
        Self::sleep(1).await;
    }

    /// 休眠10秒
    pub async fn s10() {
        Self::sleep(10).await;
    }

    /// 休眠20秒
    pub async fn s20() {
        Self::sleep(20).await;
    }

    /// 休眠30秒
    pub async fn s30() {
        Self::sleep(30).await;
    }
}