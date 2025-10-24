use crate::{app::response::R, entity::{dtos::VideoDetailTagDTO, models::VideoDetail}};
use crate::utils::segmenter_util;

/// 根据视频列表训练黑名单
pub(crate) async fn train_blacklist_by_video_list(video_detail_list: Vec<VideoDetailTagDTO>) -> R<()> {
    



    let mut title_process = vec![];
    let desc_processs = vec![];
    let tag_name_process = vec![];

    for v in video_detail_list {
        let detail = v.video_detail;
        //1.标题处理
        if let Some(title) = detail.title {
            title_process.extend(segmenter_util::process(&title))
        }

        //2.描述处理
        if let Some(desc) = detail.desc_field {


            detail.v

        }

    }



    todo!()




}



#[cfg(test)]
mod tests{
    use jieba_rs::Jieba;



    #[tokio::test]
    async fn example() {
        //第一句必须是这个
        crate::init().await;
       



        //在这中间编写测试代码



        //最后一句必须是这个
        log::logger().flush();
    }


    #[tokio::test]
    async fn test_jieba() {
        //第一句必须是这个
        crate::init().await;
       


        let jieba = Jieba::new();
        let words = jieba.cut("我们中出了一个叛徒", false);
      

        println!("{:?}", words);

        //最后一句必须是这个
        log::logger().flush();
    }


}