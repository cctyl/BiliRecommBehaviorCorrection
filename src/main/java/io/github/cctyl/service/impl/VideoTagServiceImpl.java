package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.entity.Tag;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.entity.VideoRelate;
import io.github.cctyl.entity.VideoTag;
import io.github.cctyl.mapper.VideoTagMapper;
import io.github.cctyl.service.VideoTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-17
 */
@Service
public class VideoTagServiceImpl extends ServiceImpl<VideoTagMapper, VideoTag> implements VideoTagService {

    /**
     * @param tagList
     * @param videoDetail
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void saveRelate(List<Tag> tagList, VideoDetail videoDetail) {
        String masterId = videoDetail.getId();
        if (masterId == null) {
            throw new RuntimeException("videoDetail.getId()=null");
        }

        //删除之前的
        this.remove(new LambdaQueryWrapper<VideoTag>()
                .eq(VideoTag::getVideoId, masterId)
        );

        List<VideoTag> videoTagList = tagList
                .stream()
                .map(tag -> new VideoTag()
                        .setTagId(tag.getId())
                        .setVideoId(videoDetail.getId()))
                .collect(Collectors.toList());
        this.saveBatch(videoTagList);
    }
}
