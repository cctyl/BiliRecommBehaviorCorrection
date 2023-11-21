package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.VideoDetail;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface VideoDetailService extends IService<VideoDetail> {

    /**
     * 保存视频详情，包括关联数据
     * @param videoDetail
     */
    void saveVideoDetail(VideoDetail videoDetail);

    List<VideoDetail> saveIfNotExists(List<VideoDetail> relatedVideoList);

    VideoDetail findByAid(int avid);

    VideoDetail findWithDetailByAid(int avid);
}
