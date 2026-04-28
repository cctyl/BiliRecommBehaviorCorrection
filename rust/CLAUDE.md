# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个用于B站推荐行为校正的Rust项目，主要功能包括：
- B站API接口封装和调用
- 视频信息获取和处理
- 用户行为模拟（点赞、点踩、播放等）
- Cookie和Header管理
- 数据库操作和缓存
- 基于规则和AI的视频分类审核
- 定时任务调度

## 常用开发命令

### 构建和运行
```bash
cargo build --release
cargo run
```

### 测试
```bash
# 运行所有测试
cargo test

# 运行单个测试（测试开头必须先调用 crate::init().await）
cargo test test_get_user_info

# 使用bacon工具进行实时开发
bacon run          # 运行应用
bacon test         # 运行测试
bacon clippy-all   # 运行clippy检查
```

> **注意**：`single_test.rs` 中 `pub async fn init() -> u16` 负责初始化日志、数据库连接、配置映射、AI客户端等全局状态，所有测试的开头必须调用它；最后一行必须为 `log::logger().flush()`。

### 代码检查和格式化
```bash
cargo clippy --all-targets
cargo fmt
cargo fmt --check
```

## 代码架构

### 核心模块结构

- **api/**: 外部API接口封装，主要是B站API
  - `bili.rs`: B站API核心功能，包括登录、视频操作、WBI签名、播放模拟、搜索等

- **service/**: 业务逻辑层
  - `bili_service.rs`: B站相关业务逻辑，视频处理、用户行为操作
  - `config_service.rs`: 配置管理服务（存储在数据库config表）
  - `cookie_header_data_service.rs`: Cookie和Header数据管理
  - `dict_service.rs`: 字典数据（黑名单、白名单、关键词等）
  - `task_service.rs`: 任务调度服务
  - `rule_service.rs`: 规则管理和训练
  - `video_detail_service.rs`: 视频详情服务
  - `schedule_service.rs`: 定时任务调度（使用tokio-cron-scheduler）

- **handler/**: HTTP请求处理器（Axum 0.8），所有路由在 `mod.rs:create_router()` 中集中注册
  - 所有路由统一前缀 `/api`，认证中间件层 `auth`
  - `config_handler.rs`: 配置管理接口
  - `cookie_header_data_handler.rs`: Cookie和Header接口
  - `dict_handler.rs`: 字典数据接口
  - `black_rule_handler.rs`: 黑名单规则接口
  - `rule_handler.rs`: 规则管理接口
  - `task_handler.rs`: 任务管理接口
  - `region_handler.rs`: 分区信息接口
  - `associate_rule_handler.rs`: 关联规则接口
  - `bili_api_handler.rs`: B站API接口
  - `ai.rs`: AI功能接口
  - `/` 路径提供 `web/` 目录静态文件（SPA fallback 到 index.html）

- **domain/**: 数据模型定义
  - `video_detail.rs`: 视频详情数据库模型（VideoDetail）
  - `config.rs`: 配置模型（Config）
  - `cookie_header_data.rs`: Cookie和Header模型
  - `task.rs`: 任务模型（Task）
  - `owner.rs`: UP主信息模型
  - `tag.rs`: 标签模型
  - `region.rs`: 分区模型
  - `associate_rule.rs`: 关联规则模型
  - `dict.rs`: 字典模型
  - `dtos.rs`: 数据传输对象（VideoDetailDTO、PageBean等）
  - `enumeration.rs`: 枚举类型定义（AccessType、TaskStatus、DictType、Classify、MediaType等）

- **app/**: 应用基础设施
  - `database.rs`: 数据库连接和配置
  - `config.rs`: 应用配置和全局上下文（CC），使用LazyLock
  - `error.rs`: 错误处理（HttpError）
  - `middleware.rs`: 中间件（认证auth）
  - `response.rs`: 统一响应格式（R<T>、RR<T>）
  - `global.rs`: 全局状态管理（GLOBAL_STATE），使用RwLock
  - `task_pool.rs`: 任务线程池管理（TASK_POOL），防止任务重复执行
  - `constans.rs`: 常量定义（B站API密钥、任务名称等）
  - `interceptor.rs`: SQL日志拦截器

- **utils/**: 工具函数
  - `data_util.rs`: 数据处理工具、随机访问列表消费者（RandomAccessListConsumer）
  - `http.rs`: HTTP客户端配置
  - `log.rs`: 日志配置（使用fast_log）
  - `thread_util.rs`: 线程休眠工具（ThreadUtil），用于API限流
  - `migration.rs`: 数据库迁移管理（支持从Java版本迁移）
  - `segmenter_util.rs`: 中文分词工具（jieba-rs）
  - `glm_chat.rs`: GLM AI聊天集成
  - `collection_tool.rs`: 集合工具
  - `id.rs`: ID生成工具

- **macros/**: 宏定义
  - `rb.rs`: RBatis相关宏，通过`plus!`批量生成常用CRUD方法
  - 用法示例：`plus!(VideoDetail {})` 生成 `select_by_id`、`update_by_id`、`delete_by_id`、`select_one_by_condition`、`count_by_condition`、`select_page_by_condition`
  - 分页查询条件使用 `rbs::value! {}` 构造，支持 `column`、`order_by` 特殊键

- **extractor/**: 自定义提取器
  - `path.rs`: 路径参数提取器

## 可用 Skills

项目在 `.claude/skills/` 中预置了两个 skill，当遇到对应场景时应主动调用：

| Skill | 触发场景 | 说明 |
|---|---|---|
| `bili-api` | 涉及 B站API 调用（登录、点赞、搜索、签名等） | 位于 `.claude/skills/bili-api/SKILL.md`，提供请求方法、参数说明、调用示例 |
| `rbatis` | 涉及 RBatis 数据库操作（crud、py_sql、html_sql、事务、分页等） | 位于 `.claude/skills/rbatis-skill/SKILL.md`，包含语法速查和完整示例 |

> 使用方式：遇到相关场景时，通过 `/skill <name>` 或让 AI 主动加载对应 SKILL.md 文件获取详细指导。

### 关键技术栈

- **Web框架**: Axum 0.8
- **数据库**: SQLite + RBatis 4.8 ORM（主），Sqlx 0.8（辅助，如任务状态持久化）
- **HTTP客户端**: Reqwest 0.12
- **异步运行时**: Tokio 1.39
- **序列化**: Serde + Serde_json
- **日志**: Log + fast_log
- **任务调度**: tokio-cron-scheduler
- **中文分词**: jieba-rs
- **定时任务**: tokio-cron-scheduler

### 配置管理

项目使用环境变量配置，通过`dotenvy`加载：
- `PORT`: 服务器端口（默认8080）
- `SECRET`: JWT密钥
- `DB_URL`: 数据库连接URL
- `LOG_LEVEL`: 日志级别（默认Info）

配置文件：`config.dev.txt`（开发环境，优先加载）或`config.txt`（生产环境）

### 应用启动初始化顺序（main.rs:init）

1. `CC.init()` → 初始化日志 → RBatis数据库连接 → Sqlx连接池 → 加载config表到内存 → 初始化AI客户端
2. `schedule_service::init_scheduler()` → 定时任务调度器
3. `init_common_header_map()` → 通用Header映射

### 数据库初始化

项目启动时自动执行数据库迁移：
- 迁移文件位于`migration/`目录，按版本号命名（0.sql, 1.sql, ...）
- 当前版本：7
- 支持从Java版本数据库迁移
- 使用`migration`表记录当前版本

### 全局状态管理

使用全局变量管理应用状态：
- `CC`: 全局应用上下文（AppContext），包含数据库连接、配置、AI客户端、配置映射
- `GLOBAL_STATE`: 运行时状态（GlobalState），包含通用Header映射
- `TASK_POOL`: 任务线程池，管理异步任务执行

### B站API集成

项目实现了完整的B站API集成：
- **登录认证**: TV端扫码登录（get_tv_login_qr_code、get_tv_qr_code_scan_result）
- **WBI签名**: B站API签名机制（get_wbi）
- **Cookie管理**: 自动保存和更新Cookie
- **视频操作**: 获取视频详情、点赞、点踩、获取播放URL
- **用户操作**: 获取用户信息、历史记录、观看历史
- **视频搜索**: 综合搜索、用户投稿搜索
- **播放模拟**: 模拟视频播放（report_heart_beat、simulate_play）
- **排行榜**: 热门视频排行榜、分区排行榜
- **推荐**: 首页推荐视频

### 任务系统

使用自定义任务池管理系统任务：
- 防止同名任务重复执行
- 支持查询运行中的任务
- 支持优雅关闭
- 任务类型：关键词搜索、热门排行榜、首页推荐、默认处理、三次处理

## 开发注意事项

1. **异步编程**: 项目大量使用async/await，注意异步函数的正确使用
2. **错误处理**: 使用统一的`R<T>`返回类型和`HttpError`错误类型
3. **数据库操作**: 使用RBatis ORM和自定义宏，注意SQL注入防护
4. **API限流**: B站API有调用频率限制，使用`ThreadUtil`进行延时控制
5. **配置管理**: 敏感信息通过数据库config表管理，不是硬编码
6. **测试**: 所有测试需要先调用`crate::init().await`初始化环境
7. **Rust Edition**: 项目使用 Rust 2024 Edition（`Cargo.toml`），请确保本地工具链支持（Rust 1.85+）

## 函数的响应格式

在 handler 层，返回值用 `RR<T>` 包裹（`type RR<T> = R<Resp<T>>`，即 `Result<Resp<T>, HttpError>`）：
- `RR::success(data)` 返回成功，`RR::fail(err)` 返回错误
- `RR::msg(msg)` 用于无数据的成功响应
- `Resp<T>` 的 HTTP body 格式为 `{ code: 200, message: "...", data: T }`

普通函数（非 handler）返回值用 `R<T>`（`type R<T> = Result<T, HttpError>`）：
- `R::Ok(val)` 返回成功，`R::Err(err)` 返回错误

`HttpError` 实现 `IntoResponse`，自动转换为 JSON 错误响应。

## handler 注意事项
每个 handler 必须加上 `#[debug_handler]` 注解（来自 `axum` crate）


## 错误
凡是错误相关的都在 src/app/error.rs 中寻找


## 视频处理流程

1. 获取视频信息（搜索、排行榜、推荐等）
2. 分析视频（匹配黑白名单规则、AI审核）
3. 存储视频信息到数据库
4. 根据匹配结果执行操作（点赞、点踩等）
5. 更新推荐算法

### 处理步骤（handle_step）

- 0: 未处理
- 1: 机器第一次处理
- 2: 用户处理
- 100: 处理完毕

### 访问类型（AccessType）

- BLACK: 黑名单（点踩）
- WHITE: 白名单（点赞、播放）
- OTHER: 其他（不处理）
