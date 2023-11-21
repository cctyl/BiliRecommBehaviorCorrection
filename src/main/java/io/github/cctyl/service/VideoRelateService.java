package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.entity.VideoRelate;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-21
 */
public interface VideoRelateService extends IService<VideoRelate> {

    void saveRelate(List<VideoDetail> relatedVideoList, VideoDetail videoDetail);
}
