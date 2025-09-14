


pub fn init_log(){
    let l = fast_log::init(
        fast_log::Config::new()
            .console()
            .level(log::LevelFilter::Debug),
    ).unwrap();
   
}