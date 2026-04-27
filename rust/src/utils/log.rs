use fast_log::{
    FastLogFormat,
    plugin::{
        file_split::{DateType, KeepType, Rolling, RollingType},
        packer::LogPacker,
    },
};
use log::LevelFilter;
use crate::app::config::CC;

pub fn init_log() {
    let log_level = &CC.config.log_level;
    let _ = fast_log::init(
        fast_log::Config::new()
            .format(FastLogFormat::new().set_display_line_level(log_level.to_owned()))
            .console()
            .level(log_level.to_owned())
            .chan_len(None),
    );
}
