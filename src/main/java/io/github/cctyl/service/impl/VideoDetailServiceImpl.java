package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.entity.*;
import io.github.cctyl.mapper.VideoDetailMapper;
import io.github.cctyl.service.*;
import io.github.cctyl.utils.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @since 2023-11-09
 */
@Service
@RequiredArgsConstructor
public class VideoDetailServiceImpl extends ServiceImpl<VideoDetailMapper, VideoDetail> implements VideoDetailService {

    private final OwnerService ownerService;

    private final StatService statService;

    private final TagService tagService;

    private final VideoTagService videoTagService;

    private final VideoRelateService videoRelateService;

    /**
     * 保存视频详情，包括关联数据
     *
     * @param videoDetail
     */
    @Override
    @Transactional(rollbackFor= ServerException.class)
    public void saveVideoDetail(VideoDetail videoDetail) {


        //1.保存本体，如果aid重复则会创建失败
        this.save(videoDetail);

        //2.保存关联数据
        //2.1 up主信息
        if (videoDetail.getOwner() != null) {
            Owner owner = ownerService.findOrCreateByMid(videoDetail.getOwner());
            videoDetail.setOwnerId(owner.getId());
        }

        //2.2 播放数据 Stat
        if(videoDetail.getStat()!=null){
            videoDetail.getStat().setVideoId(videoDetail.getId());
            statService.save(videoDetail.getStat());
        }

        //2.3 标签数据
        if(CollUtil.isNotEmpty(videoDetail.getTags())){
            //2.3.1 这些标签是否存在于数据库？先保存一遍
            List<Tag> tagList = tagService.saveIfNotExists(videoDetail.getTags());

            //2.3.2 创建这个标签和视频的关系
            videoTagService.saveRelate(tagList,videoDetail);
        }


        //2.4 relatedVideoList
        if (CollUtil.isNotEmpty(videoDetail.getRelatedVideoList())){

            //查询或保存
            List<VideoDetail> relatedVideoList = this.saveIfNotExists(videoDetail.getRelatedVideoList());
            //保存关联关系
            videoRelateService.saveRelate(relatedVideoList,videoDetail);
        }

        //2.5 点赞点踩原因 TODO
    }

    @Override
    public List<VideoDetail> saveIfNotExists(List<VideoDetail> relatedVideoList) {
        //查询已存在的
        List<Integer> aidList = relatedVideoList.stream().map(VideoDetail::getAid)
                .collect(Collectors.toList());
        List<VideoDetail> existVideoList = this.list(
                new LambdaQueryWrapper<VideoDetail>()
                        .in(VideoDetail::getAid, aidList)
        );
        List<Integer> existsAidList = existVideoList
                .stream()
                .map(VideoDetail::getAid)
                .collect(Collectors.toList());

        //过滤得到新的tag
        List<VideoDetail> newVideoList = relatedVideoList
                .stream()
                .filter(v -> !existsAidList.contains(v.getAid()))
                .collect(Collectors.toList());

        this.saveBatch(newVideoList);

        newVideoList.addAll(existVideoList);

        return newVideoList;
    }
}
