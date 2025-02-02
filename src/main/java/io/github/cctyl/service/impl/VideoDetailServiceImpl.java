package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.SimpleAuditingEntity;
import io.github.cctyl.domain.query.PageQuery;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.domain.vo.VideoVo;
import io.github.cctyl.mapper.VideoDetailMapper;
import io.github.cctyl.domain.po.Owner;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.service.*;
import io.github.cctyl.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
@Slf4j
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
        boolean exists = baseMapper.exists(new LambdaQueryWrapper<VideoDetail>().eq(VideoDetail::getAid, videoDetail.getAid()));
        if (exists){
            this.updateById(videoDetail);
            return;
        }

        //1.保存本体，如果aid重复则会创建失败
        this.save(videoDetail);

        //2.保存关联数据
        //2.1 up主信息
        if (videoDetail.getOwner() != null) {
            Owner owner = ownerService.findOrCreateByMid(videoDetail.getOwner());
            videoDetail.setOwnerId(owner.getId());
        }

        //2.2 播放数据 Stat
//        if(videoDetail.getStat()!=null){
//            videoDetail.getStat().setVideoId(videoDetail.getId());
//            statService.save(videoDetail.getStat());
//        }

        //2.3 标签数据
        if(CollUtil.isNotEmpty(videoDetail.getTags())){
            //2.3.1 这些标签是否存在于数据库？先保存一遍
            List<Tag> tagList = tagService.saveIfNotExists(videoDetail.getTags());

            //2.3.2 创建这个标签和视频的关系
            videoTagService.saveRelate(tagList,videoDetail);
        }


        //2.4 relatedVideoList
        // 关闭相关视频列表的保存
//        if (CollUtil.isNotEmpty(videoDetail.getRelatedVideoList())){
//
//            //查询或保存
//            List<VideoDetail> relatedVideoList = this.saveIfNotExists(videoDetail.getRelatedVideoList());
//            //保存关联关系
//            videoRelateService.saveRelate(relatedVideoList,videoDetail);
//        }

        this.updateById(videoDetail);

    }

    @Override
    public List<VideoDetail> saveIfNotExists(List<VideoDetail> relatedVideoList) {
        //查询已存在的
        List<Long> aidList = relatedVideoList.stream().map(VideoDetail::getAid)
                .collect(Collectors.toList());
        List<VideoDetail> existVideoList = this.list(
                new LambdaQueryWrapper<VideoDetail>()
                        .in(VideoDetail::getAid, aidList)
        );
        List<Long> existsAidList = existVideoList
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

    @Override
    public boolean exists(Long aid) {
        return  baseMapper.exists(new LambdaQueryWrapper<VideoDetail>().eq(VideoDetail::getAid, aid));
    }



    @Override
    public VideoDetail findByAid(Long avid) {
        return this.getOne(new LambdaQueryWrapper<VideoDetail>().eq(VideoDetail::getAid,avid));
    }

    /**
     * @param avid
     * @return
     */
    @Override
    public VideoDetail findWithDetailByAid(Long avid) {

        return   this.findWithOwnerAndTag(new LambdaQueryWrapper<VideoDetail>()
                .eq(VideoDetail::getAid,avid));
    }

    @Override
    public VideoDetail findWithDetailById(String id) {
        return   this.findWithOwnerAndTag(new LambdaQueryWrapper<VideoDetail>()
                .eq(VideoDetail::getId,id));
    }

    /**
     * 根据 处理状态查询 视频列表
     *
     * @param isHandle
     * @param pageQuery
     * @return
     */
    @Override
    public  Page<VideoVo> findWithOwnerAndHandle(boolean isHandle, PageQuery pageQuery, HandleType handleType) {

        Page<VideoDetail> page = this.lambdaQuery()
                .eq(VideoDetail::isHandle, isHandle)
                .eq(handleType!=null,VideoDetail::getHandleType, handleType)
                .isNotNull(VideoDetail::getHandleType)
                .page(Page.of(pageQuery.getPage(), pageQuery.getSize()));
        List<String> idCol = page.getRecords().stream().map(VideoDetail::getId).collect(Collectors.toList());

        List<VideoVo> voWithOwnerByIdIn = baseMapper.findVoWithOwnerByIdIn(idCol);
        voWithOwnerByIdIn.forEach(videoVo -> {
            videoVo.setUpName(Opt.ofNullable(videoVo.getOwner()).map(Owner::getName).orElse(""));
        });

        Page<VideoVo> objectPage = Page.of(page.getCurrent(), page.getSize(), page.getTotal());
        objectPage.setRecords(voWithOwnerByIdIn);

        return objectPage;
    }

    @Override
    public  Page<VideoVo> findWithOwnerAndHandle(boolean isHandle, PageQuery pageQuery, HandleType handleType,
                                                 String search
                                                 ) {

        Page<VideoDetail> page = this.lambdaQuery()
                .like(StrUtil.isNotBlank(search),VideoDetail::getTitle,search)
                .like(StrUtil.isNotBlank(search),VideoDetail::getDesc,search)
                .eq(VideoDetail::isHandle, isHandle)
                .eq(handleType!=null,VideoDetail::getHandleType, handleType)
                .isNotNull(VideoDetail::getHandleType)
                .orderByDesc(SimpleAuditingEntity::getCreatedDate)
                .page(Page.of(pageQuery.getPage(), pageQuery.getSize()));
        List<String> idCol = page.getRecords().stream().map(VideoDetail::getId).collect(Collectors.toList());

        List<VideoVo> voWithOwnerByIdIn = baseMapper.findVoWithOwnerByIdIn(idCol);
        voWithOwnerByIdIn.forEach(videoVo -> {
            videoVo.setUpName(Opt.ofNullable(videoVo.getOwner()).map(Owner::getName).orElse(""));
        });

        Page<VideoVo> objectPage = Page.of(page.getCurrent(), page.getSize(), page.getTotal());
        objectPage.setRecords(voWithOwnerByIdIn);

        return objectPage;
    }


    /**
     * 修改部分信息
     * @param videoDetail
     */
    @Override
    public void updateHandleInfoById(VideoDetail videoDetail) {

        VideoDetail temp =   new VideoDetail()
                .setId(videoDetail.getId())
                .setBlackReason(videoDetail.getBlackReason())
                .setThumbUpReason(videoDetail.getThumbUpReason())
        ;

       this.updateById(temp);

    }

    public VideoDetail findWithOwnerAndTag(LambdaQueryWrapper<VideoDetail> wrapper) {

        //1.查询本体
        VideoDetail videoDetail = this.getOne(wrapper);

        if (videoDetail==null){
            return null;
        }
        //2.查询up主信息
        Owner owner = Opt.ofNullable(videoDetail.getOwnerId())
                .map(ownerService::getById)
                .get();
        videoDetail.setOwner(owner);


        //3.播放数据 TODO 感觉在处理任务时用不到此数据

        //4.标签数据
        List<Tag> tagList = tagService.findByVideoId(videoDetail.getId());
        videoDetail.setTags(tagList);

        //5.关联视频数据 TODO 感觉在处理任务时用不到此数据


        return  videoDetail;
    }

    /**
     * 保存二次处理后的信息
     * 包括： handleType、handle、blackReason、thumbUpReason
     * @param video
     */
    @Override
    public void updateProcessInfo(VideoDetail video) {
        if (video.getId() == null) {
            throw new RuntimeException("video.getId()==null");
        }
        this.update(new LambdaUpdateWrapper<VideoDetail>()
                .eq(VideoDetail::getId, video.getId())
                .set(VideoDetail::getHandleType, Opt.ofNullable(video.getHandleType()).orElse(HandleType.THUMB_UP))
                //二次流程了，我认为此视频一定被处理掉了
                .set(VideoDetail::isHandle, true)
                //这两个原因字段一定会被修改，不管哪种情况
                .set(VideoDetail::getBlackReason, video.getBlackReason())
                .set(VideoDetail::getThumbUpReason, video.getThumbUpReason()));
    }


    @Override
    public void fillOverviewInfo(OverviewVo overviewVo) {

        //统计handle字段是false的数量
        Long count = this.lambdaQuery()
                .eq(VideoDetail::isHandle, false)
                .isNotNull(VideoDetail::getHandleType)
                .count();

        overviewVo.setSecondHandleCount(count);

        int year = overviewVo.getYear();
        //给一个year参数，获取这一年的最开始和最后时间，要Date对象
        Date startDate = DateUtil.parse(year + "-01-01 00:00:00");
        Date endDate = DateUtil.parse(year + "-12-31 23:59:59");


        //历史上的数据
        List<Map<String, Object>> whiteMapList = baseMapper.countByHandTypeAndGroupByLastModifiedDateAndYearBetween(
                HandleType.THUMB_UP,
                startDate,
                endDate
        );
        overviewVo.setWhiteHistory(transfer(whiteMapList));

        List<Map<String, Object>> blackMapList = baseMapper.countByHandTypeAndGroupByLastModifiedDateAndYearBetween(HandleType.DISLIKE,
                startDate,
                endDate
        );
        overviewVo.setBlackHistory(transfer(blackMapList));

        List<Map<String, Object>> otherMapList = baseMapper.countByHandTypeAndGroupByLastModifiedDateAndYearBetween(HandleType.OTHER,
                startDate,
                endDate
        );
        overviewVo.setOtherHistory(transfer(otherMapList));


        overviewVo.setLikeVideoCount(
                        this.lambdaQuery()
                                .eq(VideoDetail::getHandleType, HandleType.THUMB_UP)
                                .eq(VideoDetail::isHandle, true)
                                .count()
                )
                .setHateVideoCount(
                        this.lambdaQuery()
                                .eq(VideoDetail::getHandleType, HandleType.DISLIKE)
                                .eq(VideoDetail::isHandle, true)
                                .count()
                );

    }

    private static List<Map<String, Integer>> transfer(List<Map<String, Object>> blackMapList) {
        return blackMapList.stream().map(item -> {
            Map<String, Integer> map = new HashMap<>();
            map.put((String) item.get("last_modified_date"),Integer.parseInt(item.get("count").toString()));
            return map;
        }).collect(Collectors.toList());

    }
}
