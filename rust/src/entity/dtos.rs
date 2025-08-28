use core::str;
use serde::{Deserialize, Serialize};
use std::{
    thread,
    time::{Duration, SystemTime},
};
use validator::Validate;
use rbatis::rbdc::datetime::DateTime;
#[test]
fn testnow() {


    let now = SystemTime::now();
    let secs = now
        .duration_since(SystemTime::UNIX_EPOCH)
        .unwrap()
        .as_millis();

    println!("{secs}");

    thread::sleep(Duration::from_secs(2));

    let duration = SystemTime::now().duration_since(now).unwrap();
    let subsec_millis: u128 = duration.as_millis();
    println!("过去了：{subsec_millis}毫秒");
}
