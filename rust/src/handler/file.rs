use std::any::Any;
use std::fs::FileTimes;
use std::os::windows::fs::FileTimesExt;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use crate::utils::id;
use crate::{
    app::{
        database::CONTEXT,
        error::HttpError,
        response::{FailRespExt, OkRespExt, R, RR},
    },
    dao::file::FileDao,
};
use axum::body::Bytes;
use axum::{
    Router, debug_handler,
    extract::{Json, Multipart},
};
use log::{error, info};
use md5::Context as Md5Context;
use rbatis::plugin::object_id::ObjectId;
use rbs::value;
use serde::{Deserialize, Serialize};
use tokio::fs::File;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
pub fn create_router() -> Router {
    Router::new()
        .route("/compare", axum::routing::post(compare))
        .route("/upload", axum::routing::post(upload))
        .route("/test", axum::routing::get(test))
}
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FileDto {
    pub id: i64,
    pub name: String,
    pub doc_id: String,
    pub relative_path: String,
    pub is_directory: bool,
    pub md5: String,
}

#[debug_handler]
async fn compare(Json(params): Json<Vec<FileDto>>) -> RR<Vec<FileDto>> {
    let collect: Vec<String> = params
        .iter()
        .map(|item: &FileDto| item.relative_path.clone())
        .collect();

    let mut result = vec![];
    let db_list = FileDao::select_by_relative_path_in(collect).await?;
    for item in params.into_iter() {
        let flag = check_file(item.clone(), &db_list).await?;
        info!("check finish: {},flag = {}", item.name,flag);
        if flag {
            result.push(item);
        }
    }

    RR::success(result)
}

pub async fn check_file(item: FileDto, db_list: &Vec<crate::entity::models::File>) -> R<bool> {
    let mut hasher = Md5Context::new();
    if item.is_directory {
        return Ok(false);
    }

    //如果这个文件不存在，那自然是需要上传
    let find = db_list
        .iter()
        .find(|i| i.relative_path == item.relative_path);

    match find {
        Some(i) => {
            //如果已存在，则判定md5是否相同
            //不同，则需要更新

            Ok(i.md5 != item.md5)
        }
        None => {
           
            info!("数据库中不存在: {}", item.name);
            //数据库中不存在，那么真实文件中是否存在？
            let path = format!("./upload/{}", item.relative_path);
            let try_exists = tokio::fs::try_exists(&path).await?;
            if try_exists {
                let mut file = File::open(&path).await?;

                // 定义缓冲区大小
                let mut buffer = [0; 102400];

                loop {
                    // 异步读取数据到缓冲区
                    let bytes_read = file.read(&mut buffer).await?;

                    // 如果读取的字节数为0，表示到达文件末尾
                    if bytes_read == 0 {
                        break;
                    }

                    // 处理读取到的数据（这里只是示例）
                    // 注意：我们只使用前 bytes_read 字节，因为缓冲区可能没有被完全填满
                    let data = &buffer[..bytes_read];
                    // 在这里处理 data...

                    hasher.consume(&data);
                }

                let real_md5 = format!("{:x}", hasher.finalize());
                if real_md5 == item.md5 {

                    info!("{}md5比较相同，不必上传",item.name);

                    let file = crate::entity::models::File {
                        id: id::next_id(),
                        name: item.name,
                        doc_id: item.doc_id,
                        relative_path: item.relative_path,
                        is_directory:item.is_directory,
                        md5: item.md5.clone(),
                    };
                    crate::entity::models::File::insert(&CONTEXT.rb, &file).await?;
                }else{

                    info!("重新上传文件: {}", item.name);
                }
                Ok(real_md5 != item.md5)
            } else {
                info!("重新上传文件: {}", item.name);
                //不存在自然是上传
                Ok(true)
            }
        }
    }
}

#[debug_handler]
async fn upload(mut multipart: Multipart) -> RR<()> {
    info!("开始上传");
    let mut name = None;
    let mut doc_id = None;
    let mut md5 = None;
    let mut relative_path = None;
    let mut create_time: Option<SystemTime> = None;
    let mut is_directory = false;
    let mut check_md5 = false;

    //务必注意字段的先后顺序
    while let Some(mut field) = multipart.next_field().await.unwrap() {
        let name_str = field.name().unwrap_or("").to_string();

        match name_str.as_str() {
            "name" | "treeUri" | "docId" | "md5" | "relativePath" | "checkMd5" | "isDirectory"| "createTime" => {
                let bytes = field.bytes().await?;
                let value = String::from_utf8_lossy(&bytes).to_string();

                match name_str.as_str() {
                    "name" => name = Some(value),

                    "docId" => doc_id = Some(value),
                    "md5" => md5 = Some(value),
                    "relativePath" => relative_path = Some(value),
                    "checkMd5" => check_md5 = value.parse().unwrap_or(false),
                    "isDirectory" => is_directory = value.parse().unwrap_or(false),
                    "createTime" => {
        
                        info!("创建时间:");
        
                        // 解析时间戳（假设为毫秒）
                        if let Ok(timestamp) = value.parse::<u64>() {
                            create_time = Some(UNIX_EPOCH + Duration::from_millis(timestamp));
        
                            info!(" create_time: {}", timestamp);
                        } else {
                            info!("无法解析创建时间: {}", value);
                        }
                    },
                    e => info!("忽略字段: {}", e),
                }
            },

            "file" => {
                info!("开始处理文件上传 {}",name.clone().unwrap());
                // 处理文件上传的逻辑保持不变
                let (temp_path, real_path) = {
                    let mut rel_path = relative_path
                        .clone()
                        .ok_or(HttpError::BadRequest("必须提供 relativePath！".to_string()))?;

                    if rel_path == "/" {
                        return RR::success(());
                    }

                    if rel_path.starts_with('/') {
                        rel_path = rel_path[1..].to_string();
                    }

                    (
                        std::path::PathBuf::from(format!(
                            "./upload/{}.temp{}",
                            rel_path,
                            SystemTime::now()
                                .duration_since(UNIX_EPOCH)
                                .unwrap()
                                .as_millis()
                        )),
                        std::path::PathBuf::from(format!("./upload/{}", rel_path)),
                    )
                };

                if let Some(parent) = real_path.parent() {
                    tokio::fs::create_dir_all(parent).await?;
                }

                let mut dest_file = tokio::fs::File::create(&temp_path).await?;
                let mut hasher = check_md5.then(Md5Context::new);
                let mut file_size = 0;

                while let Some(chunk) = field.chunk().await? {
                    dest_file.write_all(&chunk).await?;
                    file_size += chunk.len() as u64;

                    if let Some(hasher) = &mut hasher {
                        hasher.consume(&chunk);
                    }
                }
                info!("文件写入完成: {} bytes", file_size);

                dest_file.flush().await?;

                if check_md5 {
                    let computed_md5 = hasher.map(|h| format!("{:x}", h.compute())).unwrap();

                    if md5.as_ref() != Some(&computed_md5) {
                        tokio::fs::remove_file(&temp_path).await.ok();
                        return RR::fail(HttpError::Custom(
                            700,
                            "MD5 校验失败，请重新上传！".to_string(),
                        ));
                    }
                }

                tokio::fs::rename(&temp_path, &real_path).await?;

                let ctime = create_time
                    .clone()
                    .ok_or(HttpError::BadRequest("必须提供创建时间".to_string()))?;
                let real_path_clone = real_path.clone();
                
                let dest =std::fs:: File::options().write(true).open(&real_path_clone)?;
                let times = FileTimes::new()
                    .set_accessed(ctime)
                    .set_modified(ctime)
                    .set_created(ctime)
                    ;
                dest.set_times(times)?;
                

           

                info!(
                    "文件上传成功: 路径={}, 大小={} bytes{}",
                    real_path.display(),
                    file_size,
                    if check_md5 {
                        format!(", MD5={}", md5.as_ref().unwrap_or(&"".to_string()))
                    } else {
                        "".into()
                    }
                );
            },
            _ => {
                error!("出现了异常的匹配！");
            }
        }
    }

    //删除relative_path相同的文件
    let data = crate::entity::models::File::delete_by_map(
        &CONTEXT.rb,
        value! {
            "relative_path":relative_path.clone().unwrap()
        },
    )
    .await?;

    info!("del: {data}");

    //保存新的
    let file = crate::entity::models::File {
        id: id::next_id(),
        name: name.unwrap(),
        doc_id: doc_id.unwrap(),
        relative_path: relative_path.unwrap(),
        is_directory,
        md5: md5.unwrap(),
    };

    crate::entity::models::File::insert(&CONTEXT.rb, &file).await?;

    RR::success(())
}
fn calculate_md5(data: &[u8]) -> String {
    let digest = md5::compute(data);
    format!("{:x}", digest)
}

#[debug_handler]
async fn test() -> RR<String> {
    RR::success("服务端连接成功！".to_string())
}

#[test]
fn test_qr_code() {
    use qrcode::QrCode;
    use qrcode::render::unicode;
    let code = QrCode::new(
        br#"{
"addr":"192.168.31.151:8082",
"secret":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9s"
}"#,
    )
    .unwrap();
    let image = code
        .render::<unicode::Dense1x2>()
        .dark_color(unicode::Dense1x2::Light)
        .light_color(unicode::Dense1x2::Dark)
        .build();
    println!("{}", image);
}
