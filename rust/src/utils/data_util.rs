use md5;
use regex::Regex;
use std::collections::{HashMap, HashSet};
use std::time::{SystemTime, UNIX_EPOCH};
use url::Url;




pub fn get_ts() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .expect("Time went backwards")
        .as_secs()
}


/// AV号转换为BV号
pub fn aid_to_bvid(aid: i64) -> String {
    AVBVConverter::av2bv(aid)
}

/// BV号转换为AV号
pub fn bvid_to_aid(bvid: &str) -> i64 {
    AVBVConverter::bv2av(bvid) as i64
}

/// 获取指定范围内的随机数集合
///
pub fn get_random_set(size: usize, start: i32, end: i32) -> HashSet<i32> {
    if start >= end || size <= 0 {
        panic!("参数异常");
    }

    let mut rng = rand::thread_rng();
    let mut num_set = HashSet::new();

    while num_set.len() < size {
        let random_number = rand::Rng::gen_range(&mut rng, start..=end);
        num_set.insert(random_number);
    }

    num_set
}

/// 随机访问列表中的元素
pub fn random_access_list<T, F>(source: &[T], size: usize, mut consumer: F)
where
    F: FnMut(&T),
{
    let actual_size = std::cmp::min(size, source.len());
    let indices = get_random_set(actual_size, 0, (actual_size - 1) as i32);

    for &index in &indices {
        consumer(&source[index as usize]);
    }
}

/// 获取指定范围内的随机数
pub fn get_random(start: i32, end: i32) -> i32 {
    let mut rng = rand::thread_rng();
    rand::Rng::gen_range(&mut rng, start..=end)
}

/// 获取URL中的查询参数
pub fn get_url_query_param(url: &str, param_name: &str) -> Option<String> {
    match Url::parse(url) {
        Ok(parsed_url) => parsed_url
            .query_pairs()
            .find(|(key, _)| key == param_name)
            .map(|(_, value)| value.to_string()),
        Err(_) => None,
    }
}

/// 生成MD5哈希值
pub fn generate_md5(input: &str) -> String {
    let digest = md5::compute(input);
    format!("{:x}", digest)
}

/// 将cookie字符串转换为键值对
pub fn split_cookie(cookie_str: &str) -> HashMap<String, String> {
    let mut map = HashMap::new();
    let cookies: Vec<&str> = cookie_str.split(';').collect();

    for cookie in cookies {
        let parts: Vec<&str> = cookie.trim().split('=').collect();
        if parts.len() == 2 {
            map.insert(parts[0].to_string(), parts[1].to_string());
        }
    }

    map
}

/// 获取URL中的主机名
pub fn get_host(url: &str) -> String {
    let no_protocol = url.replace("https://", "").replace("http://", "");
    let parts: Vec<&str> = no_protocol.split('/').collect();
    parts[0].to_string()
}

/// 统计键的出现频率
pub fn count_frequency<K>(frequency_map: &mut HashMap<K, i32>, key: K)
where
    K: std::hash::Hash + Eq,
{
    let count = frequency_map.entry(key).or_insert(0);
    *count += 1;
}

/// 计算两个日期之间的秒数差
pub fn calculate_seconds_difference(date1: SystemTime, date2: SystemTime) -> i32 {
    let duration1 = date1.duration_since(UNIX_EPOCH).unwrap().as_secs();
    let duration2 = date2.duration_since(UNIX_EPOCH).unwrap().as_secs();
    (duration1 as i64 - duration2 as i64).abs() as i32
}

// AVBV转换器实现
pub struct AVBVConverter;

impl AVBVConverter {
    const XOR_CODE: u64 = 23442827791579;
    const MASK_CODE: u64 = 2251799813685247;
    const MAX_AID: u64 = 1 << 51;
    const BASE: u64 = 58;

    const DATA: &'static str = "FcwAPNKTMug3GV5Lj7EJnHpWsx4tb8haYeviqBz6rkCy12mUSDQX9RdoZf";

    pub fn av2bv(aid_param: i64) -> String {
        let aid = aid_param as u64;
        let mut bytes = ['B', 'V', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0'];
        let mut bv_index = bytes.len() - 1;
        let mut tmp = (Self::MAX_AID | aid) ^ Self::XOR_CODE;

        while tmp > 0 {
            let index = (tmp % Self::BASE) as usize;
            bytes[bv_index] = Self::DATA.chars().nth(index).unwrap();
            tmp /= Self::BASE;
            if bv_index > 0 {
                bv_index -= 1;
            }
        }

        Self::swap(&mut bytes, 3, 9);
        Self::swap(&mut bytes, 4, 7);

        bytes.iter().collect()
    }

    pub fn bv2av(bvid: &str) -> u64 {
        let mut bvid_arr: Vec<char> = bvid.chars().collect();
        Self::swap(&mut bvid_arr, 3, 9);
        Self::swap(&mut bvid_arr, 4, 7);

        let adjusted_bvid: String = bvid_arr[3..].iter().collect();
        let mut tmp = 0u64;

        for c in adjusted_bvid.chars() {
            if let Some(index) = Self::DATA.find(c) {
                tmp = tmp * Self::BASE + index as u64;
            }
        }

        let xor = (tmp & Self::MASK_CODE) ^ Self::XOR_CODE;
        xor
    }

    fn swap(array: &mut [char], i: usize, j: usize) {
        array.swap(i, j);
    }
}
