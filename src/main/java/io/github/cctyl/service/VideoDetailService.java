package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.query.PageQuery;

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

    VideoDetail findWithDetailById(String id);

    /**
     * 根据 处理状态查询 视频列表
     * @param isHandle
     * @param pageQuery
     * @return
     */
    List<VideoDetail> findWithOwnerAndHandle(boolean isHandle, PageQuery pageQuery);


    void updateHandleInfoById(VideoDetail videoDetail);

    boolean exists(Integer aid);
}
