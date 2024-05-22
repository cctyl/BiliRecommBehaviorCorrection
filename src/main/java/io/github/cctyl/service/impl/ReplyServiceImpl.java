package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.domain.po.Owner;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.VideoReply;
import io.github.cctyl.mapper.OwnerMapper;
import io.github.cctyl.mapper.VideoReplyMapper;
import io.github.cctyl.service.ConfigService;
import io.github.cctyl.service.OwnerService;
import io.github.cctyl.service.ReplyService;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyServiceImpl extends ServiceImpl<VideoReplyMapper, VideoReply> implements ReplyService {


    private final BiliApi biliApi;

    private final VideoDetailService videoDetailService;


    @Override
    public Page<VideoReply> getReplyByVideoId(int avid, long page, long limit) {
        return this.lambdaQuery()
                .eq(VideoReply::getOid, avid)
                .page(new Page<>(page, limit));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReply(int avid) {
        log.info("保存视频:{} 评论开始",avid);

        VideoDetail videoDetail = videoDetailService.findByAid(avid);
        if (videoDetail==null){
            videoDetail = biliApi.getVideoDetail(avid);
            videoDetailService.saveVideoDetail(videoDetail);
        }


        VideoDetail finalVideoDetail = videoDetail;
        List<VideoReply> replyList = DataUtil.eachGetPageData(
                1, 20, 5,
                (pageNo, pageSize) -> biliApi.getReply(avid, pageNo, pageSize),
                videoReplies -> {
                    ThreadUtil.s10();
                }
        );
        this.lambdaUpdate().eq(VideoReply::getOid,avid);
        replyList.forEach(videoReply -> videoReply.setVideoId(finalVideoDetail.getId()));
        this.saveBatch(replyList);

        log.info("保存视频:{} 评论结束",avid);
    }
}
