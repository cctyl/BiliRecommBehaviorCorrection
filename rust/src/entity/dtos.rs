use core::str;
use serde::{Deserialize, Serialize};
use std::{
    thread,
    time::{Duration, SystemTime},
};
use validator::Validate;
use rbatis::{rbdc::datetime::DateTime, Page};
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


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ConfigAddUpdateDTO {
    pub id: Option<String>,
    pub name: String,
    pub value: String
    
}




#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PageDTO<T: Send + Sync>{

    pub current: u64,
    pub pages:u64,
    pub records:Vec<T>,
    pub size:u64,
    pub total:u64,

}

impl<T : Send + Sync>  PageDTO<T> 
{
    
    pub fn new(current: u64, size: u64, records: Vec<T>, total: u64) -> Self {
        PageDTO {
            current,
            pages: (total as f64 / size as f64).ceil() as u64,
            records,
            size,
            total,
        }
    }

     /// 将 PageDTO<D> 转换为 PageDTO<T>，其中 T 实现了 From<D>
    pub fn convert_from<D: Send + Sync>(source: PageDTO<D>) -> PageDTO<T>
    where
        T: From<D>,
    {
        PageDTO {
            current: source.current,
            pages: source.pages,
            records: source.records.into_iter().map(T::from).collect(),
            size: source.size,
            total: source.total,
        }
    }

}

impl<T : Send + Sync> From<Page<T>> for PageDTO<T> {
    fn from(value: Page<T>) -> Self {
       
        PageDTO {
            current: value.page_no,
            //总数处于每页大小
            pages:( value.total as f64/value.page_size as f64).ceil() as u64,
            records: value.records,
            size: value.page_size,
            total: value.total,
        }
    }
}


