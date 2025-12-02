
/// 当前版本
const CURRENT_VERSION: u32 = 2;


#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Migration {
    pub id: Option<u32>,
    pub version: u32,
    pub created_time: DateTime,
}
crud!(Migration {});

#[sql("select max(version) from migration")]
pub async fn get_max_version(rb: &RBatis) -> Result<Option<u32>, rbatis::error::Error> {
    impled!()
}

#[sql( "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'migration'")]
pub async fn migration_exist(rb: &RBatis) ->Result<Option<String>, rbatis::error::Error>{
    impled!()
}

#[sql( "SELECT count(1) FROM sqlite_master WHERE type = 'table' ")]
pub async fn table_num(rb: &RBatis) ->Result<u32, rbatis::error::Error>{
    impled!()
}

#[tokio::test]
async fn test_init() {
    _ = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    );
    CC.init().await;
    start_migration().await.unwrap();
}


/**
 * 执行迁移
 */
pub async fn start_migration() -> R<()> {
    // 首先获取迁移文件路径
    let migration_paths: HashMap<u32, String> = load_migrations()?;

    let migration_exist = migration_exist(&CC.rb).await?;
    let table_num = table_num(&CC.rb).await?;

    let  mut update = true;
    //migration存在
    //已经升级过，正常升级即可
    if migration_exist.is_some() {
        info!("表 migration_exist 存在");
        //表存在
        let max = get_max_version(&CC.rb).await?.unwrap_or(0);
        info!("历史最大版本号={},当前版本号={}", max, CURRENT_VERSION);

        if max < CURRENT_VERSION {
            info!("======需要升级=========");
            for i in max + 1..=CURRENT_VERSION {
                if let Some(p) = migration_paths.get(&i) {
                    exec_sql(p).await?;
                }
            }

            //更新最大版本号
            Migration::insert(
                &CC.rb,
                &Migration {
                    id: None,
                    version: CURRENT_VERSION,
                    created_time: DateTime::now(),
                },
            )
            .await?;
        } else {
            info!("=========无需升级=======");
            update = false;
        }
    } else {
        //migration 不存在
        info!("表 migration_exist 不存在");

        let mut start;
        if table_num >0 {
            //旧的数据库 config一定存在
            //从1.sql开始执行
            start = 1;

            info!("=========从java版本迁移=========");
        } else {
            //全新的数据库 config一定不存在
            //全部执行
            start = 0;
            info!("=========全新环境=========");
        }

        
        for i in start..=CURRENT_VERSION {
            if let Some(p) = migration_paths.get(&i) {
                exec_sql(p).await?;
            }
        }
    

        //更新最大版本号
        Migration::insert(
            &CC.rb,
            &Migration {
                id: None,
                version: CURRENT_VERSION,
                created_time: DateTime::now(),
            },
        )
        .await?;
    }

    if update{
        let end_sql = vec![" VACUUM;", "PRAGMA journal_mode=DELETE;"];

        for sql in end_sql {
            CC.rb.exec(sql, vec![]).await?;
        }
    }
   

    Ok(())
}

/**
 * 将字符串中多个sql拆分为一条条语句并执行
 */
pub async fn exec_sql(path: &str) -> R<()> {
    let content = tokio::fs::read_to_string(path).await?;
    let split_sql_statements = parse_sql_to_str_refs(&content)?;

    {
        // 在事务中执行多个 SQL
        let tx = CC.rb.acquire_begin().await?;

        for sql in split_sql_statements {
            info!("sql={sql}");
            tx.exec(&sql, vec![]).await?;
        }

        tx.commit().await?;
    }

    Ok(())
}
/**
 * 加载迁移文件
 */
fn load_migrations() -> std::io::Result<HashMap<u32, String>> {
    let mut migrations = HashMap::new();
    let migration_dir = "./migration"; // 迁移文件目录

    // 遍历目录中的所有条目
    for entry in std::fs::read_dir(migration_dir)? {
        let entry = entry?;
        let path = entry.path();

        // 确保是文件且扩展名为 .sql
        if path.is_file() {
            if let Some(file_name) = path.file_name().and_then(|f| f.to_str()) {
                if file_name.ends_with(".sql") {
                    // 使用正则表达式提取文件名中的数字
                    let re = regex::Regex::new(r"^(\d+)\.sql$").unwrap();
                    if let Some(captures) = re.captures(file_name) {
                        if let Ok(number) = captures[1].parse::<u32>() {
                            migrations.insert(number, path.to_string_lossy().to_string());
                        }
                    }
                }
            }
        }
    }

    info!("加载的迁移文件：{:?}", migrations);
    Ok(migrations)
}

use std::collections::HashMap;

use log::info;
use rbatis::{crud, impled, sql, RBatis};
use rbatis::rbdc::DateTime;
use serde::{Deserialize, Serialize};
use sqlparser::dialect::SQLiteDialect;
use sqlparser::parser::Parser;

use crate::app::database::CC;
use crate::app::response::R;

/**
 * 解析SQL语句,返回Vec<String>
 */
fn parse_sql_to_str_refs(sql_input: &str) -> Result<Vec<String>, sqlparser::parser::ParserError> {
    let dialect = SQLiteDialect {};
    let statements = Parser::parse_sql(&dialect, sql_input)?;

    Ok(statements
        .into_iter()
        .map(|stmt| stmt.to_string())
        .collect())
}
