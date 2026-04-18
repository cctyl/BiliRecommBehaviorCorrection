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
    let _ = fast_log::init(
        fast_log::Config::new()
            .format(FastLogFormat::new().set_display_line_level(LOG_LEVEL))
            .console()
            .level(LOG_LEVEL)
            .chan_len(None),
    );
}
