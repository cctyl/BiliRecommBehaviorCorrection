package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.VideoRelate;
import io.github.cctyl.mapper.VideoRelateMapper;
import io.github.cctyl.service.VideoRelateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-21
 */
@Service
public class VideoRelateServiceImpl extends ServiceImpl<VideoRelateMapper, VideoRelate> implements VideoRelateService {

    /**
     * @param relatedVideoList
     * @param videoDetail
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void saveRelate(List<VideoDetail> relatedVideoList, VideoDetail videoDetail) {

        String masterId = videoDetail.getId();
        if (masterId == null) {
            throw new RuntimeException("videoDetail.getId()=null");
        }

        //删除之前的
        this.remove(new LambdaQueryWrapper<VideoRelate>()
                .eq(VideoRelate::getMasterVideoId, masterId)
        );
        List<VideoRelate> relateList = relatedVideoList.stream()
                .filter(r -> r.getId() != null)
                .map(
                        r -> new VideoRelate()
                                .setMasterVideoId(masterId)
                                .setRelatedVideoId(r.getId())
                ).collect(Collectors.toList());
        this.saveBatch(relateList);

    }
}
