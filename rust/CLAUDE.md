# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个用于B站推荐行为校正的Rust项目，主要功能包括：
- B站API接口封装和调用
- 视频信息获取和处理
- 用户行为模拟（点赞、点踩等）
- Cookie和Header管理
- 数据库操作和缓存

## 常用开发命令

### 构建和运行
```bash
cargo build --release
cargo run
```

### 测试
```bash
cargo test
cargo test --package bili-recomm-behavior-correction --lib -- tests::具体测试名称
```

### 代码检查
```bash
cargo clippy
cargo fmt --check
```

## 代码架构

### 核心模块结构

- **api/**: 外部API接口封装，主要是B站API
  - `bili.rs`: B站API核心功能，包括登录、视频操作、WBI签名等

- **service/**: 业务逻辑层
  - `bili_service.rs`: B站相关业务逻辑，视频处理、用户行为操作
  - `config_service.rs`: 配置管理服务
  - `cookie_header_data_service.rs`: Cookie和Header数据管理
  - `dict_service.rs`: 字典数据管理（黑名单等）
  - `task_service.rs`: 任务调度服务

- **handler/**: HTTP请求处理器
  - 对应service层的HTTP接口实现

- **entity/**: 数据模型定义
  - `models.rs`: 数据库模型（Config、CookieHeaderData、VideoDetail等）
  - `dtos.rs`: 数据传输对象
  - `enumeration.rs`: 枚举类型定义

- **app/**: 应用基础设施
  - `database.rs`: 数据库连接和配置
  - `config.rs`: 应用配置
  - `error.rs`: 错误处理
  - `middleware.rs`: 中间件
  - `response.rs`: 统一响应格式
  - `global.rs`: 全局状态管理

- **utils/**: 工具函数
  - `data_util.rs`: 数据处理工具
  - `http.rs`: HTTP客户端
  - `log.rs`: 日志配置
  - `thread_util.rs`: 线程工具

### 关键技术栈

- **Web框架**: Axum
- **数据库**: SQLite + RBatis ORM
- **HTTP客户端**: Reqwest
- **异步运行时**: Tokio
- **序列化**: Serde
- **日志**: Log + fast_log

### 数据库初始化

项目启动时会自动执行数据库迁移：
```rust
start_migration().await.expect("数据库迁移失败");
```

### 全局状态管理

使用`GLOBAL_STATE`管理应用全局状态，包括：
- 配置信息
- Cookie和Header映射
- 任务调度器

### B站API集成

项目实现了完整的B站API集成：
- **登录认证**: TV端扫码登录
- **WBI签名**: B站API签名机制
- **视频操作**: 获取视频详情、点赞、点踩等
- **用户操作**: 获取用户信息、历史记录等

### 线程池和任务调度

使用自定义线程池处理异步任务：
- `task_pool.rs`: 任务线程池管理
- `ThreadUtil`: 线程休眠工具函数

## 开发注意事项

1. **异步编程**: 项目大量使用async/await，注意异步函数的正确使用
2. **错误处理**: 使用统一的`R<T>`返回类型和`HttpError`错误类型
3. **数据库操作**: 使用RBatis ORM，注意SQL注入防护
4. **API限流**: B站API有调用频率限制，使用`ThreadUtil`进行延时控制
5. **配置管理**: 敏感信息通过数据库配置表管理，不是硬编码

## 测试策略

- 单元测试：各个模块的基础功能测试
- 集成测试：API接口和数据库操作测试
- 测试数据库：使用独立的测试数据库实例