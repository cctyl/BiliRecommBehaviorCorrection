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

## 2. 理解请求方法

B站API主要有以下几种请求方式：

### GET请求（返回JSON）
```rust
common_get_json_body(url, vec![
    ("param1".to_string(), "value1".to_string()),
])
```

### POST请求（表单）
```rust
common_post_form(url, vec![
    ("param1".to_string(), "value1".to_string()),
])
```

### 带WBI签名的请求
```rust
let params = vec![("mid", "123".to_string())];
let wbi_params = get_wbi(false, params).await?;
common_get_other_header(url, wbi_params, other_map).await?;
```

## 3. 常见API调用示例

### 获取视频详情
```rust
let video_detail = get_video_detail(aid).await?;
```

### 点赞视频
```rust
thumb_up(aid).await?;
```

### 点踩视频
```rust
dislike(aid).await?;
```

### 搜索视频
```rust
let videos = search_keyword("关键词", 1).await?;
```

### 获取热门排行榜
```rust
let videos = hot_rank_video(1, 10).await?;
```

### 获取首页推荐
```rust
let aids = get_home_recommend_video().await?;
```

### 模拟播放
```rust
simulate_play(aid, cid, duration).await?;
```

## 4. 重要参数说明

### 认证参数
- `access_key`: 访问令牌，通过 `get_access_key(false).await?` 获取
- `csrf`: CSRF令牌，通过 `get_csrf().await?` 获取
- `mid`: 用户ID，从配置获取

### 签名参数
- `sign`: APP签名，`get_app_sign()` 自动生成
- `w_rid`:`wts`: WBI签名，`get_wbi()` 自动生成

### 视频参数
- `aid`: 视频ID (数字)
- `bvid`: 视频BV号 (字符串)
- `cid`: 视频片段ID

## 5. 注意事项

1. **测试前必须初始化**: 所有测试需要先调用 `crate::init().await`
2. **登录态检查**: 大部分接口需要access_key，登录过期会抛出异常
3. **API限流**: 使用 `ThreadUtil::s1().await` 进行1秒延时防止限流
4. **Cookie自动管理**: 请求后自动更新Cookie到数据库

## 6. 提供具体代码帮助

根据用户的需求：
1. 找到合适的API函数
2. 展示函数签名
3. 提供完整的调用示例
4. 说明参数的获取方式
5. 提醒可能需要的初始化步骤

始终提供可直接使用的代码示例，并解释每个关键步骤的作用。
