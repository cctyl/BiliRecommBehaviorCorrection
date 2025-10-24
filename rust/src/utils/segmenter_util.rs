#[cfg(test)]
mod tests {
    use std::collections::HashMap;

    use jieba_rs::Jieba;

    use crate::utils::segmenter_util::{
        generate_frequency_map, get_top_frequent_word, get_top_frequent_word_auto_limit,
        get_top_frequent_word_from_list, get_top_frequent_word_from_list_auto_limit, process,
    };

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_jieba() {
        //第一句必须是这个
        crate::init().await;

        let jieba = Jieba::new();
        let words = jieba.cut("我们中出了一个叛徒", false);

        println!("{:?}", words);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_util_process() {
        //第一句必须是这个
        crate::init().await;

        let process = process("今天是个好日子");

        println!("{:?}", process);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[test]
    fn test_get_top_frequent_word() {
        let mut keyword_frequency_map = HashMap::new();
        keyword_frequency_map.insert("测试".to_string(), 5);
        keyword_frequency_map.insert("例子".to_string(), 2);
        keyword_frequency_map.insert("rust".to_string(), 4);

        let top_words = get_top_frequent_word(keyword_frequency_map, 2);
        println!("{:?}", top_words);
        assert_eq!(top_words.len(), 2);
        assert!(top_words.contains(&"测试".to_string()));
        assert!(top_words.contains(&"rust".to_string()));
    }

    #[test]
    fn test_get_top_frequent_word_auto_limit() {
        let mut keyword_frequency_map = HashMap::new();
        keyword_frequency_map.insert("测试".to_string(), 5);
        keyword_frequency_map.insert("例子".to_string(), 2);
        keyword_frequency_map.insert("rust".to_string(), 4);

        let top_words = get_top_frequent_word_auto_limit(keyword_frequency_map);
        println!("{:?}", top_words);
        // limit 应该是 max(3 / 100, 5) = 5, 但由于只有两个满足条件的词，所以返回这两个词
        assert_eq!(top_words.len(), 2);
    }

    #[test]
    fn test_get_top_frequent_word_from_list() {
        let str_process = vec!["测试", "例子", "测试", "测试", "测试"]; // 让"测试"出现至少4次
        let stop_word_list = vec!["例子".to_string()];
        let top_words = get_top_frequent_word_from_list(&str_process, 1, &stop_word_list);
        println!("{:?}", top_words);
        assert_eq!(top_words, vec!["测试"]);
    }

    #[test]
    fn test_get_top_frequent_word_from_list_auto_limit() {
        let str_process = vec!["测试", "例子", "测试", "测试", "测试"]; // 让"测试"出现至少4次
        let stop_word_list = vec!["例子".to_string()];
        let top_words = get_top_frequent_word_from_list_auto_limit(&str_process, &stop_word_list);
        assert_eq!(top_words, vec!["测试"]);
    }

    #[test]
    fn test_generate_frequency_map() {
        let str_process = vec!["测试", "例子", "测试", "!", "a"];
        let stop_word_list = vec!["例子".to_string()];
        let freq_map = generate_frequency_map(&str_process, &stop_word_list);

        assert_eq!(*freq_map.get("测试").unwrap(), 2);
        assert_eq!(freq_map.get("例子"), None);
        assert_eq!(freq_map.get("!"), None); // 标点符号应被过滤掉
        assert_eq!(freq_map.get("a"), None); // 单字符应被过滤掉
    }
}

use jieba_rs::Jieba;
use regex::Regex;
use std::collections::HashMap;
use std::sync::LazyLock;

// 使用 LazyLock 初始化全局 Jieba 实例
pub static JIEBA: LazyLock<Jieba> = LazyLock::new(|| Jieba::new());

// 使用 LazyLock 初始化标点符号正则表达式
pub static PUNCTUATION_PATTERN: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(r##"[=,.?!@#$%^&*()_+:"<>/\[\]\\`~——，。、～《》？；'："【】、{}|·！￥…（）-]"##)
        .expect("Failed to compile punctuation regex")
});
/// 分词函数
pub fn process(text: &str) -> Vec<&str> {
    JIEBA.cut(text, false)
}

/// 获取前 N 个出现频率最高的词
pub fn get_top_frequent_word(
    keyword_frequency_map: HashMap<String, usize>,
    limit: usize,
) -> Vec<String> {
    let mut filtered: Vec<(String, usize)> = keyword_frequency_map
        .into_iter()
        .filter(|(_, count)| *count > 3 as usize)
        .map(|(k, v)| (k.clone(), v))
        .collect();

    filtered.sort_by(|a, b| b.1.cmp(&a.1));

    filtered
        .into_iter()
        .map(|(word, _)| word)
        .filter(|s| !s.is_empty() && s.chars().count() > 1)
        .take(limit)
        .collect()
}

/// 重载版本：自动计算 limit 值
pub fn get_top_frequent_word_auto_limit(
    keyword_frequency_map: HashMap<String, usize>,
) -> Vec<String> {
    let limit = std::cmp::max(keyword_frequency_map.len() / 100, 5);
    get_top_frequent_word(keyword_frequency_map, limit)
}

/// 生成词频统计 map，并获取前 N 个高频词
pub fn get_top_frequent_word_from_list(
    str_process: &[&str],
    limit: usize,
    stop_word_list: &[String],
) -> Vec<String> {
    let frequency_map = generate_frequency_map(str_process, stop_word_list);
    get_top_frequent_word(frequency_map, limit)
}

/// 重载版本：自动计算 limit 值
pub fn get_top_frequent_word_from_list_auto_limit(
    str_process: &[&str],
    stop_word_list: &[String],
) -> Vec<String> {
    let frequency_map = generate_frequency_map(str_process, stop_word_list);
    println!("{:#?}", frequency_map);
    let limit = std::cmp::max(str_process.len() / 100, 5);
    get_top_frequent_word(frequency_map, limit)
}

/// 生成词频统计 HashMap
pub fn generate_frequency_map(
    str_process: &[&str],
    stop_word_list: &[String],
) -> HashMap<String, usize> {
    let mut map: HashMap<String, usize> = HashMap::new();

    for s in str_process {
        if s.is_empty()
            || s.chars().count() < 2
            || stop_word_list.contains(&s.to_string())
            || PUNCTUATION_PATTERN.is_match(s)
        {
            continue;
        }

        *map.entry(s.to_string()).or_insert(0) += 1;
    }

    map
}
