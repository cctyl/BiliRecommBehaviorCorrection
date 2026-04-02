use anyhow::Context;
use log::info;
use rbatis::{
    RBatis, py_sql,
    rbdc::{DateTime, db::ExecResult},
};
use rbs::value;
use std::collections::HashSet;
use std::hash::Hash;
use tokio::sync::mpsc;
use tokio::{fs::File, io::AsyncReadExt};

use crate::domain::enumeration::DictStatus;
use crate::{
    app::{config::CC, error::HttpError, response::R},
    domain::{
        dict::Dict,
        enumeration::{AccessType, DictType},
    },
    handler::dict_handler::DictDto,
    service::{
        config_service, cookie_header_data_service::find_by_classify_and_media_type,
        task_service::find_by_class_method_name,
    },
    utils::id::generate_id,
};
use rbatis::rbatis_codegen::IntoSql;

#[cfg(test)]
mod tests {
    use std::collections::HashSet;

    use anyhow::Context;
    use log::info;
    use rbatis::rbdc::DateTime;
    use rbs::value;

    use crate::domain::enumeration::DictStatus;
    use crate::{
        app::{config::CC, error::HttpError, response::R},
        domain::{
            dict::Dict,
            enumeration::{AccessType, DictType},
        },
        handler::dict_handler::DictDto,
        service::dict_service::{
            add_black_user_id, check_is_need_save, exist_by_access_type_and_dict_type_and_value,
            find_black_user_id, find_by_dict_type_and_access_type, get_black_user_id_set,
            get_stop_word_list, read_stop_word, update_access_type_and_dict_type_by_ids,
            update_dict_access_type_by_ids,
        },
        utils::id::generate_id,
    };
    use rbatis::rbatis_codegen::IntoSql;

    #[tokio::test]
    async fn test_exist_by_access_type_and_dict_type_and_value() {
        crate::init().await;

        let exist_by_access_type_and_dict_type_and_value =
            exist_by_access_type_and_dict_type_and_value(
                AccessType::BLACK,
                DictType::KEYWORD,
                DictStatus::IGNORE,
                "泰国",
            )
            .await
            .unwrap();
        assert_eq!(exist_by_access_type_and_dict_type_and_value, true);

        log::logger().flush();
    }

    // check_is_need_save
    #[tokio::test]
    async fn test_check_is_need_save() {
        crate::init().await;

        let r = check_is_need_save(
            AccessType::BLACK,
            DictType::KEYWORD,
            DictStatus::IGNORE,
            "泰国",
        )
        .await
        .unwrap();
        assert_eq!(r, false);
        let r = check_is_need_save(AccessType::BLACK, DictType::TAG, DictStatus::IGNORE, "泰国")
            .await
            .unwrap();
        assert_eq!(r, false);

        log::logger().flush();
    }

    //测试update_dict_access_type_by_ids
    #[tokio::test]
    async fn test_update_dict_access_type_by_ids() {
        crate::init().await;

        let r = update_dict_access_type_by_ids(
            &CC.rb,
            AccessType::BLACK,
            &vec![String::from("1"), String::from("2")],
        )
        .await
        .unwrap();

        log::logger().flush();
    }

    // 测试 update_access_type_and_dict_type_by_ids
    #[tokio::test]
    async fn test_update_access_type_and_dict_type_by_ids() {
        crate::init().await;

        let r = super::update_status_by_ids(
            &CC.rb,
            DictStatus::IGNORE,
            &vec![
                String::from("1892076683722792962"),
                String::from("1887365665629224962"),
            ],
        )
        .await
        .unwrap();
        log::logger().flush();
    }

    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_find_black_user_id() {
        //第一句必须是这个
        crate::init().await;

        //在这中间编写测试代码
        let find_black_user_id = find_black_user_id().await.unwrap();

        //长度是746
        assert_eq!(find_black_user_id.len(), 746);

        //最后一句必须是这个
        log::logger().flush();
    }

    // 测试get_black_user_id_set
    #[tokio::test]
    async fn test_get_black_user_id_set() {
        //第一句必须是这个
        crate::init().await;

        //获取黑名单用户ID集合
        let black_user_id_set = get_black_user_id_set().await.unwrap();

        //验证返回结果不为空
        assert!(!black_user_id_set.is_empty());

        //验证数量与find_black_user_id一致
        let all_black_users = find_black_user_id().await.unwrap();
        let unique_count = all_black_users
            .iter()
            .map(|d| &d.value)
            .collect::<std::collections::HashSet<_>>()
            .len();
        assert_eq!(black_user_id_set.len(), unique_count);

        //最后一句必须是这个
        log::logger().flush();
    }

    #[tokio::test]
    async fn test_add_black_user_id() {
        //第一句必须是这个
        crate::init().await;

        //准备测试数据
        let test_user_id = "test_user_123456";

        //先确保该用户ID不在黑名单中
        let initial_black_list = get_black_user_id_set().await.unwrap();
        let initial_count = initial_black_list.len();

        //添加黑名单用户ID
        let result = add_black_user_id(test_user_id, None).await;
        assert!(result.is_ok());

        //验证添加成功
        let updated_black_list = get_black_user_id_set().await.unwrap();
        assert!(updated_black_list.contains(&test_user_id.to_string()));
        assert_eq!(updated_black_list.len(), initial_count + 1);

        //再次添加相同用户ID，应该不会重复添加
        let result_again = add_black_user_id(test_user_id, None).await;
        assert!(result_again.is_ok());

        let final_black_list = get_black_user_id_set().await.unwrap();
        assert_eq!(final_black_list.len(), updated_black_list.len());

        //最后一句必须是这个
        log::logger().flush();
    }

    //测试read_stop_word
    #[tokio::test]
    async fn test_read_stop_word() {
        //第一句必须是这个
        crate::init().await;

        // 获取停顿词列表
        let stop_words = read_stop_word().await.unwrap();

        // 验证结果
        assert!(!stop_words.is_empty());
        //长度打印
        println!("停顿词数量: {}", stop_words.len());
        println!("停顿词列表: {:?}", stop_words);

        // 最后一句必须是这个
        log::logger().flush();
    }

    //测试 get_stop_word_list
    #[tokio::test]
    async fn test_get_stop_word_list() {
        //第一句必须是这个
        crate::init().await;

        // 获取停顿词列表
        let stop_words = get_stop_word_list().await.unwrap();

        //长度打印
        println!("停顿词数量: {}", stop_words.len());

        // 最后一句必须是这个
        log::logger().flush();
    }

    //测试 find_by_dict_type_and_access_type
    #[tokio::test]
    async fn test_find_by_dict_type_and_access_type() {
        crate::init().await;

        let r = find_by_dict_type_and_access_type(DictType::STOP_WORDS, AccessType::OTHER)
            .await
            .unwrap();

        println!("find_by_dict_type_and_access_type: {:?}", r.len());

        log::logger().flush();
    }

    //测试 find_by_dict_type_and_access_type_and_value
    #[tokio::test]
    async fn test_new() {
        crate::init().await;
        let option = Dict::select_by_id(&CC.rb, "1887365665629224962".to_string())
            .await
            .unwrap();

        println!("new: {:?}", option);

        let string = serde_json::to_string(&option).unwrap();
        println!("new: {:?}", string);

        log::logger().flush();
    }
}
/**
 * 根据DictType 和 AccessType 获取Dict列表
 */
pub(crate) async fn get_list_by_dict_type_access_type_status(
    access_type: AccessType,
    dict_type: DictType,
    status: DictStatus,
) -> R<Vec<Dict>> {
    Dict::select_by_map(
        &CC.rb,
        value! {"dict_type":dict_type,"access_type":access_type,"status":status},
    )
    .await
    .map_err(HttpError::from)
}

/// 添加停顿词
pub(crate) async fn add_stop_word(v: Vec<String>) -> R<()> {
    let mut dicts = Dict::select_by_map(
        &CC.rb,
        value! {
            "value":&v,
             "access_type":AccessType::OTHER,
             "dict_type":DictType::STOP_WORDS,
        },
    )
    .await?
    .into_iter()
    .map(|f| f.value)
    .collect::<Vec<String>>();

    dicts.extend(v);
    let mut words: HashSet<String> = dicts.into_iter().collect();
    let unique_words: Vec<String> = words.into_iter().collect();
    let keyword_to_dict = keyword_to_dict(
        unique_words,
        DictType::STOP_WORDS,
        AccessType::OTHER,
        None,
        DictStatus::NORMAL,
    );
    Dict::insert_batch(&CC.rb, &keyword_to_dict, 100).await?;
    R::Ok(())
}
pub fn keyword_to_dict(
    value_collection: Vec<String>,
    dict_type: DictType,
    access_type: AccessType,
    outer_id: Option<String>,
    status: DictStatus,
) -> Vec<Dict> {
    value_collection
        .into_iter()
        .map(|s| Dict {
            access_type,
            dict_type,
            outer_id: outer_id.clone(),
            value: s,
            id: generate_id(),
            last_modified_date: Some(DateTime::now()),
            created_date: Some(DateTime::now()),
            desc_field: None,
            status,
        })
        .collect()
}

/// 批量添加词典，输入的是字符串
pub async fn batch_add_dict_from_value(
    value_collection: Vec<String>,
    dict_type: DictType,
    access_type: AccessType,
    status: DictStatus,
) -> R<()> {
    let keyword_to_dict = keyword_to_dict(value_collection, dict_type, access_type, None, status);
    Dict::insert_batch(&CC.rb, &keyword_to_dict, 100).await?;

    R::Ok(())
}

/// 根据access_type 和 dict_type 删除数据
pub async fn remove_by_access_type_and_dict_type(
    access_type: AccessType,
    dict_type: DictType,
) -> R<()> {
    Dict::delete_by_map(
        &CC.rb,
        value! {
            "access_type":access_type,
            "dict_type":dict_type,
        },
    )
    .await?;

    R::Ok(())
}

/// 批量删除后新增词典
pub(crate) async fn batch_remove_and_update(
    dict_type: DictType,
    access_type: AccessType,
    mut dto: Vec<DictDto>,
) -> R<Vec<DictDto>> {
    remove_by_access_type_and_dict_type(access_type, dict_type).await?;

    for d in &mut dto {
        d.access_type = access_type;
        d.dict_type = dict_type;
        d.id = Some(generate_id());
    }

    let collect = dto
        .clone()
        .into_iter()
        .map(|mut d| d.into())
        .collect::<Vec<Dict>>();

    Dict::insert_batch(&CC.rb, &collect, 100).await?;

    R::Ok(dto)
}

/// 根据access_type 和 dict_type 和 value 查询是否存在
pub async fn exist_by_access_type_and_dict_type_and_value(
    access_type: AccessType,
    dict_type: DictType,
    status: DictStatus,
    value: &str,
) -> R<bool> {
    let dicts = Dict::select_by_map(
        &CC.rb,
        value! {
            "dict_type":dict_type,
            "access_type":access_type,
            "value":value,
            "status":status
        },
    )
    .await?;

    R::Ok(!dicts.is_empty())
}

/// 如果不在忽略的名单内则保存，否则不保存
/// 返回true表示需要保存
/// 返回false表示不需要保存
pub async fn check_is_need_save(
    access_type: AccessType,
    dict_type: DictType,
    status: DictStatus,
    value: &str,
) -> R<bool> {
    R::Ok(
        !exist_by_access_type_and_dict_type_and_value(access_type, dict_type, status, value)
            .await?,
    )
}

/// 新增字典，如果不在忽略名单内则新增
pub(crate) async fn add_dict(table: Dict) -> R<String> {
    let access_type = table.access_type;
    let dict_type = table.dict_type;
    let check_is_need_save =
        check_is_need_save(access_type, dict_type, table.status, &table.value).await?;
    if check_is_need_save {
        Dict::insert(&CC.rb, &table).await?;
    }
    R::Ok(table.id)
}

#[py_sql(
    "`update dict set access_type=#{access_type} where id in (`
    trim ',': for _,item in ids:
        #{item},
    `)`"
)]
async fn update_dict_access_type_by_ids(
    rb: &RBatis,
    access_type: AccessType,
    ids: &[String],
) -> Result<ExecResult, rbatis::Error> {
    impled!()
}
#[py_sql(
    "`update dict set access_type=#{access_type} , dict_type= #{dict_type}   where id in (`
    trim ',': for _,item in ids:
        #{item},
    `)`"
)]
async fn update_access_type_and_dict_type_by_ids(
    rb: &RBatis,
    access_type: AccessType,
    dict_type: DictType,
    ids: &[String],
) -> Result<ExecResult, rbatis::Error> {
    impled!()
}

#[py_sql(
    "`update dict set status=#{status}  where id in (`
    trim ',': for _,item in ids:
        #{item},
    `)`"
)]
async fn update_status_by_ids(
    rb: &RBatis,
    status: DictStatus,
    ids: &[String],
) -> Result<ExecResult, rbatis::Error> {
    impled!()
}

/// 从训练的缓存中添加黑名单关键词
pub(crate) async fn add_balck_dict_from_cache_by_id(selected_id: Vec<String>) -> R<()> {
    update_dict_access_type_by_ids(&CC.rb, AccessType::BLACK, &selected_id).await?;
    R::Ok(())
}

/// 添加一个黑名单用户id
pub(crate) async fn add_black_user_id(user_id: &str, desc: Option<String>) -> R<()> {
    let balck_user_id_set = get_black_user_id_set().await?;

    let mid = user_id.to_string();
    if !balck_user_id_set.contains(&mid) {
        let dict = Dict::new(
            mid,
            AccessType::BLACK,
            DictType::MID,
            None,
            desc,
            DictStatus::NORMAL,
        );
        Dict::insert(&CC.rb, &dict).await?;
    } else {
        info!("用户{}已经在黑名单中", user_id);
    }

    R::Ok(())
}

/// 获取黑名单用户id
/// 返回值：Vec<String>
async fn get_black_user_id_set() -> R<Vec<String>> {
    let v: Vec<String> = find_black_user_id()
        .await?
        .into_iter()
        .map(|d| d.value)
        .collect::<std::collections::HashSet<String>>()
        .into_iter()
        .collect();

    R::Ok(v)
}

/// 查找黑名单用户id Dict
/// 返回值：Vec<Dict>
async fn find_black_user_id() -> R<Vec<Dict>> {
    get_list_by_dict_type_access_type_status(AccessType::BLACK, DictType::MID, DictStatus::NORMAL)
        .await
}

/// 根据dict_type 和 access_type 查询
pub async fn find_by_dict_type_and_access_type(
    dict_type: DictType,
    access_type: AccessType,
) -> R<Vec<Dict>> {
    R::Ok(
        Dict::select_by_map(
            &CC.rb,
            value! {
                    "dict_type":dict_type,
                    "access_type":access_type
            },
        )
        .await?,
    )
}

/// 根据dict_type 和 access_type 和 status 查询字典值
pub async fn find_value_by_dict_type_and_access_type_and_status(
    dict_type: DictType,
    access_type: AccessType,
    status: DictStatus,
) -> R<Vec<String>> {
    R::Ok(
        Dict::select_by_map(
            &CC.rb,
            value! {
                    "dict_type":dict_type,
                    "access_type":access_type,
                    "status":status
            },
        )
        .await?
        .into_iter()
        .map(|d| d.value)
        .collect(),
    )
}

/// 根据dict_type 和 access_type 查询字典值
pub async fn find_normal_value_by_dict_type_and_access_type(
    dict_type: DictType,
    access_type: AccessType,
) -> R<Vec<String>> {
    R::Ok(
        find_value_by_dict_type_and_access_type_and_status(
            dict_type,
            access_type,
            DictStatus::NORMAL,
        )
        .await?,
    )
}

/// 根据dict_type 和 access_type 查询忽略字典值
pub async fn find_ignore_value_by_dict_type_and_access_type(
    dict_type: DictType,
    access_type: AccessType,
) -> R<Vec<String>> {
    R::Ok(
        find_value_by_dict_type_and_access_type_and_status(
            dict_type,
            access_type,
            DictStatus::NORMAL,
        )
        .await?,
    )
}

/// 查询停顿词列表
pub(crate) async fn get_stop_word_list() -> R<Vec<String>> {
    let first = config_service::is_first_use().await?;
    let mut items: Vec<String>;
    if first {
        items = read_stop_word().await?;
        save_stop_word(items.clone()).await?;
    } else {
        items = find_by_dict_type_and_access_type(DictType::STOP_WORDS, AccessType::OTHER)
            .await?
            .into_iter()
            .map(|d| d.value)
            .collect();
    }

    R::Ok(items)
}

/// 保存停顿词,去重
pub async fn save_stop_word(items: Vec<String>) -> R<()> {
    let exists_word = Dict::select_by_map(
        &CC.rb,
        value! {
            "dict_type":DictType::STOP_WORDS,
            "access_type":AccessType::OTHER,
            "value":&items
        },
    )
    .await?
    .into_iter()
    .map(|d| d.value)
    .collect::<HashSet<String>>();

    let collect: Vec<String> = items
        .into_iter()
        .filter(|i| !exists_word.contains(i))
        .collect::<HashSet<String>>()
        .into_iter()
        .collect();

    println!("过滤后的：{}", collect.len());

    if !collect.is_empty() {
        let keyword_to_dict = keyword_to_dict(
            collect,
            DictType::STOP_WORDS,
            AccessType::OTHER,
            None,
            DictStatus::NORMAL,
        );
        Dict::insert_batch(&CC.rb, &keyword_to_dict, 100).await?;
    }

    R::Ok(())
}

/// 加载停顿词
pub async fn read_stop_word() -> R<Vec<String>> {
    let mut file = File::open("cn_stopwords.txt").await?;

    let mut contents = String::new();
    file.read_to_string(&mut contents).await?;

    let collect = contents
        .lines()
        .map(|l| l.trim().to_string())
        .collect::<Vec<String>>();

    R::Ok(collect)
}

/// 查询搜索关键词列表
pub(crate) async fn get_search_keyword_set() -> R<HashSet<String>> {
    let vec: HashSet<String> = Dict::select_by_map(
        &CC.rb,
        value! {
            "dict_type":DictType::SEARCH_KEYWORD,
            "status":DictStatus::NORMAL,
        },
    )
    .await?
    .into_iter()
    .map(|d| d.value)
    .collect();

    R::Ok(vec)
}
