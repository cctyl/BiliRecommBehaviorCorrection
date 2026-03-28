use std::collections::HashMap;
use std::hash::Hash;



pub trait VecGroupByExt<T> {
    /// 保留 None（None 作为一个合法分组 key），但需要显式处理
    fn group_by_full<K, F>(self, f: F) -> HashMap<K, Vec<T>>
    where
        K: Eq + Hash,
        F: Fn(&T) -> K;  // 直接返回 K，不包裹 Option

    /// 忽略 None（过滤掉）
    fn group_by<K, F>(self, f: F) -> HashMap<K, Vec<T>>
    where
        K: Eq + Hash,
        F: Fn(&T) -> Option<K>;  // 返回 Option，None 表示过滤
}

impl<T> VecGroupByExt<T> for Vec<T> {
    fn group_by_full<K, F>(self, f: F) -> HashMap<K, Vec<T>>
    where
        K: Eq + Hash,
        F: Fn(&T) -> K,
    {
        let mut map = HashMap::with_capacity(self.len());
        for item in self {
            let key = f(&item);  // 直接得到 K，没有 Option
            map.entry(key)
                .or_insert_with(Vec::new)
                .push(item);
        }
        map
    }

    fn group_by<K, F>(self, f: F) -> HashMap<K, Vec<T>>
    where
        K: Eq + Hash,
        F: Fn(&T) -> Option<K>,
    {
        let mut map = HashMap::with_capacity(self.len());
        for item in self {
            if let Some(key) = f(&item) {
                map.entry(key)
                    .or_insert_with(Vec::new)
                    .push(item);
            }
        }
        map
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[derive(Clone, Debug, Default)]
    struct TestTable {
        id: Option<String>,
        name: Option<String>,
    }

    fn mock_data() -> Vec<TestTable> {
        vec![
            TestTable { id: Some("1".into()), name: Some("a".into()) },
            TestTable { id: Some("2".into()), name: Some("b".into()) },
            TestTable { id: Some("1".into()), name: Some("c".into()) },
            TestTable { id: None, name: Some("d".into()) },
        ]
    }

    #[test]
    fn test_group_by_full_keep_none() {
        let data = mock_data();

        let grouped = data.group_by_full(|x| x.id.clone());

        assert_eq!(grouped.len(), 3);

        assert_eq!(grouped.get(&Some("1".to_string())).unwrap().len(), 2);
        assert_eq!(grouped.get(&Some("2".to_string())).unwrap().len(), 1);
        assert_eq!(grouped.get(&None).unwrap().len(), 1);
    }

    #[test]
    fn test_group_by_ignore_none() {
        let data = mock_data();

        let grouped = data.group_by(|x| x.id.clone());

        assert_eq!(grouped.len(), 2);

        assert_eq!(grouped.get("1").unwrap().len(), 2);
        assert_eq!(grouped.get("2").unwrap().len(), 1);
    }
}