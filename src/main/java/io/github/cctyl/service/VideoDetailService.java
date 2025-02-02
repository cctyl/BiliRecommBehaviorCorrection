package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.query.PageQuery;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.domain.vo.VideoVo;

import java.util.List;
import java.util.Map;

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

    VideoDetail findByAid(Long avid);

    VideoDetail findWithDetailByAid(Long avid);

    VideoDetail findWithDetailById(String id);

    /**
     * 根据 处理状态查询 视频列表
     * @param isHandle
     * @param pageQuery
     * @return
     */
    Page<VideoVo> findWithOwnerAndHandle(boolean isHandle, PageQuery pageQuery, HandleType handleType);


    void updateHandleInfoById(VideoDetail videoDetail);

    boolean exists(Long aid);




    /**
     * 保存二次处理的信息
     * @param video
     */
    void updateProcessInfo(VideoDetail video);

    void fillOverviewInfo(OverviewVo overviewVo);

    Page<VideoVo> findWithOwnerAndHandle(boolean isHandle, PageQuery pageQuery, HandleType handleType, String search);
}
