use idgenerator::{IdGeneratorOptions, IdInstance};

pub fn init() -> anyhow::Result<()> {
    let option = IdGeneratorOptions::new()
        .base_time(
           0,
        )
        .worker_id(1)
        .worker_id_bit_len(4);

    IdInstance::init(option)?;
    Ok(())
}

pub fn next_id() -> i64 {
    IdInstance::next_id()
}

pub fn generate_id() -> String {
    next_id().to_string()
}
