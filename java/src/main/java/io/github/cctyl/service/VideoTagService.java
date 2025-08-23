package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.VideoTag;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-17
 */
public interface VideoTagService extends IService<VideoTag> {

    void saveRelate(List<Tag> tagList, VideoDetail videoDetail);
}
