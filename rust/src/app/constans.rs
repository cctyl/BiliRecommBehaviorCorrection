// 基础前缀常量
const PREFIX: &str = "bili:";

// 字符串常量（硬编码拼接结果）
pub const IMG_KEY: &str = "bili:imgKey";
pub const SUB_KEY: &str = "bili:subKey";
pub const BILI_ACCESS_KEY: &str = "bili:access_key";
pub const MID_KEY: &str = "bili:mid";

// 独立字符串常量
pub const BILITICKET: &str = "bili_ticket";
pub const B_NUT: &str = "b_nut";
pub const BUVID3: &str = "buvid3";
pub const BUVID4: &str = "buvid4";
pub const BAIDU_ASK_KEY: &str = "baidu_accesskey";
pub const FIRST_START_TIME: &str = "firstStartTime";
pub const FIRST_USE: &str = "firstUse";
pub const BAIDU_CLIENT_ID: &str = "baidu_client_id";
pub const BAIDU_CLIENT_SECRET: &str = "baidu_client_secret";
pub const MIN_PLAY_SECOND: &str = "minPlaySecond";
pub const CRON: &str = "cron";

// 第三方认证密钥（需确保值不变）
pub const THIRD_PART_APPKEY: &str = "783bbb7264451d82";
pub const THIRD_PART_APPSEC: &str = "2653583c8873dea268ab9386918b1d65";

// 格式化字符串（需直接展开）
pub const REASON_FORMAT: &str = "%s=%s,匹配:%s, <br/> ";

// 整数常量（显式指定类型）
pub const PIC_MAX_SIZE: usize = 2097152; // 或 u32


// 点踩任务 
pub const DISLIKE_BY_USER_ID_TASK: &str = "io.github.cctyl.controller.BlackRuleController.dislikeByUserId";