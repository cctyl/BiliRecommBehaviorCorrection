use fast_log::{
    FastLogFormat,
    plugin::{
        file_split::{DateType, KeepType, Rolling, RollingType},
        packer::LogPacker,
    },
};
use log::LevelFilter;



const LOG_LEVEL: LevelFilter = LevelFilter::Info;



pub fn init_log() {
    // 确保日志目录存在
    std::fs::create_dir_all("logs").unwrap();

    // 使用日期作为文件名
    let log_file = format!("logs/app_{}.log", chrono::Local::now().format("%Y%m%d"));
    let l = fast_log::init(
        fast_log::Config::new()
            .format(FastLogFormat::new().set_display_line_level(LOG_LEVEL)) // 这是格式化器的级别
            .console()
            .level(LOG_LEVEL) // 这是日志过滤器的级别
            .chan_len(Some(100000)) // 预分配通道提高性能
            .file_split(
                &log_file,                                        // 日志目录
                Rolling::new(RollingType::ByDate(DateType::Day)), // 按天分割
                KeepType::KeepNum(7),                             // 保留7天的日志
                LogPacker {},                                     // 不压缩，直接保存
            ),
    )
    .unwrap();
}
