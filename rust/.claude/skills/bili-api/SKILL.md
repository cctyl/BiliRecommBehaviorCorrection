---
name: bili-api
description: Query and use Bilibili API interfaces. Use when asking about Bilibili API calls, request parameters, signing methods, or how to call specific Bilibili endpoints.
---

当用户询问B站API调用时，按照以下步骤帮助：

## 1. 查找API接口

首先在 `src/api/bili.rs` 中搜索相关的API函数：

- 使用Grep工具搜索关键词（如 "获取视频详情"、"点赞"、"搜索"）
- 查看函数的参数和返回值
- 了解函数的签名和使用方式

## 2. B站特殊参数速查表

项目中用到的所有特殊 key 和常量统一汇总如下，调用任何API前先确认需要哪些认证。

### 硬编码常量（`src/app/constans.rs`）

| 常量名 | 值 | 用途 |
|---|---|---|
| `THIRD_PART_APPKEY` | `783bbb7264451d82` | APP签名参数 `appkey` |
| `THIRD_PART_APPSEC` | `2653583c8873dea268ab9386918b1d65` | APP签名盐值 |
| `MID_KEY` | `bili:mid` | 数据库配置中用户mid的key |
| `CSRF` | `bili_jct` | Cookie中CSRF token的key |
| `BILI_ACCESS_KEY` | `bili:access_key` | 数据库配置中access_key的key |
| `IMG_KEY` | `bili:imgKey` | WBI签名图片key |
| `SUB_KEY` | `bili:subKey` | WBI签名副key |

### APP签名（TV端/APP接口）

通过 `get_app_sign(params)` 自动为参数追加签名，用于扫码登录等接口：

| 参数名 | 来源 | 说明 |
|---|---|---|
| `appkey` | `THIRD_PART_APPKEY` | 固定值 |
| `appsec` | `THIRD_PART_APPSEC` | 固定值，签名盐 |
| `local_id` | `"0"` | 固定 |
| `ts` | `get_ts()` | 当前Unix时间戳（秒） |
| `sign` | `get_app_sign()` 自动追加 | MD5(URL编码参数串 + appsec) |

```rust
// 用法示例（扫码登录）
let params = vec![
    ("appkey".to_string(), THIRD_PART_APPKEY.to_string()),
    ("appsec".to_string(), THIRD_PART_APPSEC.to_string()),
    ("local_id".to_string(), "0".to_string()),
    ("ts".to_string(), get_ts().to_string()),
];
let signed = get_app_sign(params);
// 自动追加 sign 参数
```

### WBI签名（Web端需WBI的接口）

通过 `get_wbi(refresh, params)` 自动签名，用于用户空间等接口：

| 参数名 | 来源 | 说明 |
|---|---|---|
| `w_rid` | `get_wbi()` 自动追加 | MD5(排序后参数 + mixin_key) |
| `wts` | `get_wbi()` 自动追加 | Unix时间戳（秒） |
| `img_key` / `sub_key` | 缓存在数据库config表 | 从 `https://api.bilibili.com/x/web-interface/nav` 获取 |

`get_wbi()` 内部流程：
1. 从数据库取 `img_key` / `sub_key`，若无或 `refresh=true` 则调用 nav 接口刷新
2. 计算 `mixin_key` = 固定置换表(MIXIN_KEY_ENC_TAB)打乱 `img_key + sub_key` 字节序列
3. 参数字典序排序，拼 `key1=val1&key2=val2...`，末尾接 `mixin_key`
4. 取 MD5 得到 `w_rid`，时间戳作为 `wts`

```rust
// 用法示例（查询用户投稿）
let wbi_map = vec![
    ("mid", mid.to_string()),
    ("ps", "30".to_string()),
    ("pn", page_num.to_string()),
    ("order", "pubdate".to_string()),
    ("platform", "web".to_string()),
    ("web_location", "1550101".to_string()),
];
let other_map = vec![
    ("Referer", format!("https://space.bilibili.com/{mid}")),
    ("Origin", "https://space.bilibili.com".to_string()),
];
let signed = get_wbi(false, wbi_map).await?;
let resp = common_get_other_header(url, signed, other_map).await?;
```

### access_key（APP端登录态令牌）

通过 `get_access_key(refresh)` 获取，登录过期时返回错误：

| 参数名 | 来源 | 说明 |
|---|---|---|
| `access_key` | `get_access_key(false).await?` | 从数据库config表读取，需先扫码登录 |

登录过期码：`-101`，此时应提示用户重新扫码。

### csrf（Web端操作令牌）

通过 `cookie_header_data_service::get_csrf().await?` 获取（从Cookie中读取 `bili_jct`）：

| 参数名 | 来源 | 说明 |
|---|---|---|
| `csrf` | `get_csrf().await?` | 从Cookie `bili_jct` 获取，用于点赞/心跳等写操作 |

### mid（用户ID）

通过 `config_service::find_config_by_name(MID_KEY).await?` 获取：

| 参数名 | 来源 | 说明 |
|---|---|---|
| `mid` | 数据库config表 | 用户B站 UID，登录后自动更新 |

### ts（时间戳）

通过 `get_ts()` 获取当前Unix时间戳（秒）。

## 3. 请求方法对照

| 方法 | 函数 | 用途 |
|---|---|---|
| GET + JSON | `common_get_json_body(url, vec![...])` | 通用GET，返回JSON，自动处理Cookie/Header |
| GET + text | `common_get_text_body(url, vec![...])` | 获取HTML等纯文本 |
| GET + 自定义Header | `common_get_other_header(url, params, other_headers)` | WBI签名等需要额外Header的请求 |
| POST 表单 | `common_post_form(url, vec![...])` | 写操作（点赞、点踩、心跳） |
| 无认证GET | `no_auth_cookie_get(url, vec![...])` | 陌生人请求，无需Cookie |

## 4. 常见API调用示例

### 获取视频详情
```rust
let detail = get_video_detail(aid).await?;  // aid为数字ID
```

### 点赞视频（需要csrf）
```rust
thumb_up(aid).await?;  // 内部自动取csrf
```

### 点踩视频（需要access_key）
```rust
dislike(aid).await?;  // 内部自动取access_key
```

### 关键词搜索
```rust
let videos = search_keyword("关键词", 1).await?;
```

### 热门排行榜
```rust
let videos = hot_rank_video(1, 10).await?;  // pn=页码, ps=每页数量
```

### 首页推荐（需要access_key）
```rust
let aids = get_home_recommend_video().await?;  // 返回aid列表
```

### 模拟播放（完整流程）
```rust
// 获取视频URL
let url = get_video_url(bvid, cid).await?;
// 模拟播放（自动发心跳）
simulate_play(aid, cid, duration).await?;
// 播放完成后点赞
thumb_up(aid).await?;
```

## 5. 注意事项

1. **测试前必须初始化**: 所有测试需要先调用 `crate::init().await`，最后 `log::logger().flush()`
2. **登录态检查**: 需 access_key 的接口过期会抛 `HttpError::Biz("登录已过期，请重新扫码登录")`
3. **API限流**: 使用 `ThreadUtil::s1().await`（1秒延时）或 `ThreadUtil::sleep(N)` 防止限流
4. **Cookie自动管理**: 每次请求后响应中的Cookie自动持久化到数据库
5. **WBI Key过期**: `get_wbi()` 会自动检测并刷新，无需手动处理
6. **心跳参数**: `report_heart_beat` 有大量内部状态参数，建议通过 `simulate_play()` 间接调用而非直接使用
