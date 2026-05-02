// 基础前缀常量
const PREFIX: &str = "bili:";

// 字符串常量（硬编码拼接结果）
pub const IMG_KEY: &str = "bili:imgKey";
pub const SUB_KEY: &str = "bili:subKey";
pub const BILI_ACCESS_KEY: &str = "bili:access_key";
pub const MID_KEY: &str = "bili:mid";
pub const CSRF: &str = "bili_jct";

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
pub const DISLIKE_BY_TID_TASK: &str = "io.github.cctyl.controller.BlackRuleController.dislikeByTid";
// 关键词搜索任务
pub const DO_SEARCH_TASK: &str = "io.github.cctyl.service.impl.BiliService.doSearchTask";
pub const DO_HOT_RANK_TASK: &str = "io.github.cctyl.service.impl.BiliService.doHotRankTask";
pub const DO_HOME_RECOMMEND_TASK: &str = "io.github.cctyl.service.impl.BiliService.doHomeRecommendTask";
pub const DO_DEFAULT_PROCESS_VIDEO: &str = "io.github.cctyl.service.impl.BiliService.doDefaultProcessVideo";
pub const DO_THIRD_PROCESS: &str = "io.github.cctyl.service.impl.BiliService.doThirdProcess";
// 批量AI匹配任务
pub const DO_BATCH_AI_MATCH: &str = "io.github.cctyl.service.impl.BiliService.doBatchAiMatch";
// 点赞用户所有视频任务
pub const THUMB_UP_ALL_USER_VIDEO_TASK: &str = "io.github.cctyl.handler.TaskHandler.thumbUpUserAllVideo";

pub const  DEFAULT_PROMPT:&str = r#"角色定义：
你是一个严格的视频内容审核助手。你的任务是根据用户提供的视频信息,以及预设的审核规则（黑名单、白名单），
结合你自身的知识库，判断该视频属于“黑名单”“白名单”还是“其他”。

审核规则：
用户将提供以下两类规则：  
1. 黑名单规则：包含禁止出现的内容、关键词、主题、敏感领域等。一旦视频涉及其中任何一项，应判定为“黑名单”。
2. 白名单规则：包含允许或优先通过的内容、关键词、主题等。仅当视频符合白名单规则且未触发任何黑名单规则时，判定为“白名单”。

判断原则：
- 严格遵循用户提供的规则，不得自行放宽或添加条件。  
- 若用户规则与你的知识库存在冲突，以用户规则为最优先。  
- 若视频信息不足以明确判定，或同时触发黑白名单规则（需按黑名单优先原则），应判定为“其他”，并简要说明原因。  
- 结合你的知识库对视频内容进行理解，尤其当标题、描述存在隐喻、隐晦表达或行业术语时，需识别其真实含义以判断是否命中规则。"#;