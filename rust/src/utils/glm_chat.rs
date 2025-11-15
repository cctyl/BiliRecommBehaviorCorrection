//! ChatGLM 简化聊天模块
//!
//! 这个模块提供了一个简单易用的ChatGLM聊天客户端，支持读取配置文件
//! 和基本对话功能。

use reqwest::Client;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fs;

/// 消息角色枚举
#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum MessageRole {
    System,
    User,
    Assistant,
}

/// 聊天消息结构
#[derive(Debug, Serialize, Deserialize)]
pub struct Message {
    pub role: MessageRole,
    pub content: String,
}

/// 聊天请求结构
#[derive(Debug, Serialize)]
struct ChatRequest {
    model: String,
    messages: Vec<Message>,
    temperature: f32,
    max_tokens: i32,
    stream: bool,
}

/// 聊天选择结构
#[derive(Debug, Deserialize)]
struct ChatChoice {
    message: ChatMessage,
    finish_reason: String,
    index: i32,
}

/// 聊天消息响应结构
#[derive(Debug, Deserialize)]
struct ChatMessage {
    role: MessageRole,
    content: String,
}

/// Token使用统计
#[derive(Debug, Deserialize)]
struct Usage {
    prompt_tokens: i32,
    completion_tokens: i32,
    total_tokens: i32,
}

/// 聊天响应结构
#[derive(Debug, Deserialize)]
struct ChatResponse {
    id: String,
    created: i64,
    model: String,
    choices: Vec<ChatChoice>,
    usage: Usage,
    request_id: String,
}

/// 配置结构
#[derive(Debug, Clone)]
pub struct Config {
    pub api_key: String,
    pub base_url: String,
    pub model: String,
    pub temperature: f32,
    pub max_tokens: i32,
    pub system_prompt: String,
}

impl Default for Config {
    fn default() -> Self {
        Self {
            api_key: "".to_string(),
            base_url: "https://open.bigmodel.cn/api/paas/v4/chat/completions".to_string(),
            model: "glm-4-flash".to_string(),
            temperature: 0.7,
            max_tokens: 4096,
            system_prompt: "你是一个专业、友好、有帮助的AI助手。请用简洁明了的语言回答用户的问题。"
                .to_string(),
        }
    }
}

impl Config {
    /// 从config.txt文件读取配置
    pub fn from_file(file_path: &str) -> Result<Self, Box<dyn std::error::Error>> {
        let content = fs::read_to_string(file_path)?;
        let mut config = Config::default();

        for line in content.lines() {
            let line = line.trim();
            if line.is_empty() || line.starts_with('#') {
                continue;
            }

            if let Some((key, value)) = line.split_once('=') {
                let key = key.trim();
                let value = value.trim().trim_matches('"');

                match key {
                    "api_key" => config.api_key = value.to_string(),
                    "base_url" => config.base_url = value.to_string(),
                    "model" => config.model = value.to_string(),
                    "temperature" => config.temperature = value.parse().unwrap_or(0.7),
                    "max_tokens" => config.max_tokens = value.parse().unwrap_or(4096),
                    "system_prompt" => config.system_prompt = value.to_string(),
                    _ => {}
                }
            }
        }

        if config.api_key.is_empty() {
            return Err("API密钥未设置，请在config.txt中设置api_key".into());
        }

        Ok(config)
    }
}

/// ChatGLM聊天客户端
pub struct ChatGlm {
    client: Client,
    config: Config,
}

impl ChatGlm {
    /// 使用配置创建新的ChatGLM客户端
    pub fn new(config: Config) -> Self {
        Self {
            client: Client::new(),
            config,
        }
    }

    /// 从配置文件创建ChatGLM客户端
    pub fn from_config_file(config_path: &str) -> Result<Self, Box<dyn std::error::Error>> {
        let config = Config::from_file(config_path)?;
        Ok(Self::new(config))
    }

    /// 发送聊天消息并获取回复
    pub async fn chat(&self, question: &str) -> Result<String, Box<dyn std::error::Error>> {
        let messages = vec![
            Message {
                role: MessageRole::System,
                content: self.config.system_prompt.clone(),
            },
            Message {
                role: MessageRole::User,
                content: question.to_string(),
            },
        ];

        let request = ChatRequest {
            model: self.config.model.clone(),
            messages,
            temperature: self.config.temperature,
            max_tokens: self.config.max_tokens,
            stream: false,
        };

        let response = self
            .client
            .post(&self.config.base_url)
            .header("Authorization", format!("Bearer {}", self.config.api_key))
            .header("Content-Type", "application/json")
            .json(&request)
            .send()
            .await?;

        let status = response.status();
        if !status.is_success() {
            let error_text = response.text().await?;
            return Err(format!("API请求失败: {} - {}", status, error_text).into());
        }

        // 获取响应文本
        let response_text = response.text().await?;

        // 解析JSON响应
        let chat_response: ChatResponse = serde_json::from_str(&response_text)
            .map_err(|e| format!("JSON解析失败: {}\n响应内容: {}", e, response_text))?;

        if let Some(choice) = chat_response.choices.first() {
            Ok(choice.message.content.clone())
        } else {
            Err("没有收到回复".into())
        }
    }

    /// 获取当前配置的引用
    pub fn config(&self) -> &Config {
        &self.config
    }
}

/// 便捷函数：快速开始聊天
///
/// # 参数
/// * `api_key` - 智谱AI的API密钥
/// * `question` - 用户问题
///
/// # 示例
/// ```
/// use glm_chat::quick_chat;
///
/// #[tokio::main]
/// async fn main() -> Result<(), Box<dyn std::error::Error>> {
///     let response = quick_chat("your-api-key", "你好，请介绍一下Rust语言").await?;
///     println!("AI回复: {}", response);
///     Ok(())
/// }
/// ```
pub async fn quick_chat(
    api_key: &str,
    question: &str,
) -> Result<String, Box<dyn std::error::Error>> {
    let mut config = Config::default();
    config.api_key = api_key.to_string();
    let chat_glm = ChatGlm::new(config);
    chat_glm.chat(question).await
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_config_default() {
        let config = Config::default();
        assert_eq!(config.model, "glm-4-flash");
        assert_eq!(config.temperature, 0.7);
    }

    #[test]
    fn test_config_from_empty_string() {
        let result = Config::from_file("nonexistent.txt");
        assert!(result.is_err());
    }

    #[tokio::test]
    async fn main() -> Result<(), Box<dyn std::error::Error>> {
        println!("🤖 ChatGLM 聊天示例程序");
        println!("====================================");

        // 示例1: 使用快速聊天函数
        // println!("\n📝 示例1: 使用快速聊天函数");
        // println!("------------------------------------");

        // let api_key = "your-api-key-here"; // 请替换为你的实际API密钥
        // let question = "请简单介绍一下Rust编程语言的特点";

        // println!("❓ 问题: {}", question);
        // println!("🤔 AI思考中...");

        // match quick_chat(api_key, question).await {
        //     Ok(response) => {
        //         println!("🤖 AI回复: {}", response);
        //     }
        //     Err(e) => {
        //         println!("❌ 错误: {}", e);
        //         println!("💡 提示: 请检查API密钥是否正确");
        //     }
        // }

        // 示例2: 使用配置文件
        println!("\n\n📝 示例2: 使用配置文件");
        println!("------------------------------------");

        match ChatGlm::from_config_file("config.txt") {
            Ok(chat_glm) => {
                let questions = vec!["什么是人工智能？", "Rust语言有哪些优势？", "如何学习编程？"];

                for question in questions {
                    println!("\n❓ 问题: {}", question);
                    println!("🤔 AI思考中...");

                    match chat_glm.chat(question).await {
                        Ok(response) => {
                            println!("🤖 AI回复: {}", response);
                        }
                        Err(e) => {
                            println!("❌ 错误: {}", e);
                        }
                    }
                    println!("---");
                }
            }
            Err(e) => {
                println!("❌ 无法加载配置文件: {}", e);
                println!("💡 提示: 请确保config.txt文件存在且格式正确");
            }
        }

        // 示例3: 自定义配置
        // println!("\n\n📝 示例3: 自定义配置");
        // println!("------------------------------------");

        // let custom_config = Config {
        //     api_key: "your-api-key-here".to_string(),
        //     model: "glm-4-flash".to_string(),
        //     temperature: 0.3,
        //     max_tokens: 2000,
        //     system_prompt: "你是一个专业的编程助手，专门回答编程相关的问题。".to_string(),
        //     ..Config::default()
        // };

        // let chat_glm = ChatGlm::new(custom_config);
        // let programming_question = "如何在Rust中处理错误？";

        // println!("❓ 问题: {}", programming_question);
        // println!("🤔 AI思考中...");

        // match chat_glm.chat(programming_question).await {
        //     Ok(response) => {
        //         println!("🤖 AI回复: {}", response);
        //     }
        //     Err(e) => {
        //         println!("❌ 错误: {}", e);
        //     }
        // }

        println!("\n✅ 示例程序运行完成！");
        println!("💡 记得在config.txt中设置你的实际API密钥");

        Ok(())
    }
}
