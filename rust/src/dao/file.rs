use crate::{
    app::{ database::CONTEXT, response::R},
    entity::models::File,
};

#[derive(Debug)]
pub struct FileDao;

impl FileDao {



    pub async fn select_by_relative_path_in(relative_path_list: Vec<String>) -> R<Vec<File>> {


        let join = relative_path_list
        .iter()
        .map(|s| s.as_str() )
        .collect::<Vec<&str>>();
        let files = File::select_all_by_relative_path_in(&CONTEXT.rb, &join).await?;
        println!("files:{:#?}",files);
        Ok(files)
    }
}
