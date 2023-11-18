package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.entity.Owner;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.mapper.VideoDetailMapper;
import io.github.cctyl.service.OwnerService;
import io.github.cctyl.service.StatService;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.utils.ServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
@Service
public class VideoDetailServiceImpl extends ServiceImpl<VideoDetailMapper, VideoDetail> implements VideoDetailService {

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private StatService statService;


    /**
     * 保存视频详情，包括关联数据
     *
     * @param videoDetail
     */
    @Override
    @Transactional(rollbackFor= ServerException.class)
    public void saveVideoDetail(VideoDetail videoDetail) {


        //1.保存本体
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
        if(videoDetail.getTags()!=null){

        }



    }
}
