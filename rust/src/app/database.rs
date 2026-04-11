


use std::str::FromStr;

use log::error;
// use rbatis::{RBatis, dark_std::errors::new};
// use rbdc_sqlite::Driver;
// use rbdc_sqlite::driver::SqliteDriver;
use serde::{Deserialize, Deserializer, de};




pub fn default_false() -> bool {
    false
}


pub fn bool_or_int<'de, D>(deserializer: D) -> Result<bool, D::Error>
where
    D: Deserializer<'de>,
{
    struct BoolOrIntVisitor;

    impl<'de> de::Visitor<'de> for BoolOrIntVisitor {
        type Value = bool;

        fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
            formatter.write_str("a boolean or an integer")
        }

        fn visit_bool<E>(self, value: bool) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(value)
        }

        fn visit_i32<E>(self, value: i32) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(value != 0)
        }

        fn visit_i64<E>(self, value: i64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(value != 0)
        }

        fn visit_u64<E>(self, value: u64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(value != 0)
        }
    }

    deserializer.deserialize_any(BoolOrIntVisitor)
}
pub fn bool_or_int_opt<'de, D>(deserializer: D) -> Result<Option<bool>, D::Error>
where
    D: Deserializer<'de>,
{
    struct BoolOrIntVisitor;

    impl<'de> de::Visitor<'de> for BoolOrIntVisitor {
        type Value = Option<bool>;

        fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
            formatter.write_str("a boolean or an integer")
        }

        fn visit_bool<E>(self, value: bool) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(Some(value))
        }

        fn visit_i32<E>(self, value: i32) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(Some(value != 0))
        }

        fn visit_i64<E>(self, value: i64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(Some(value != 0))
        }

        fn visit_u64<E>(self, value: u64) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            // Map 0 to false, any other value to true
            Ok(Some(value != 0))
        }

        fn visit_none<E>(self) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(None)
        }

        fn visit_some<D>(self, deserializer: D) -> Result<Self::Value, D::Error>
        where
            D: Deserializer<'de>,
        {
            // 递归调用deserialize_any来处理Some中的值
            deserializer.deserialize_any(BoolOrIntVisitor)
        }
        fn visit_unit<E>(self) -> Result<Self::Value, E>
        where
            E: de::Error,
        {
            Ok(None)
        }
    }

    deserializer.deserialize_any(BoolOrIntVisitor)
}


pub fn empty_string_as_none<'de, D, T>(deserializer: D) -> Result<Option<T>, D::Error>
where
    D: Deserializer<'de>,
    T: Deserialize<'de>,
{
    let opt = Option::<String>::deserialize(deserializer)?;

    match opt {
        None => Ok(None),
        Some(s) if s.trim().is_empty() => Ok(None),
        Some(s) => {
            let de = serde::de::value::StringDeserializer::<D::Error>::new(s);
            T::deserialize(de).map(Some)
        }
    }
}

