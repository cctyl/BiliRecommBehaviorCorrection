package io.github.cctyl.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.*;
import io.github.cctyl.entity.enumeration.HandleType;
import io.github.cctyl.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * 相关任务处理
 */
@Service
@Slf4j
public class BiliService {


    @Autowired
    private BiliApi biliApi;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private BlackRuleService blackRuleService;

    @Autowired
    private WhiteRuleService whiteRuleService;

    /**
     * 检查cookie状态
     * 调用历史记录接口来实现
     *
     * @return true 有效  false 无效
     */
    public boolean checkCookie() {
        JSONObject history = biliApi.getUserInfo();
        log.info("检查cookie状态：{}", history.toString());
        return history.getIntValue("code") == 0;
    }


    /**
     * 更新一下必要的cookie
     */
    public void updateCookie() {
        log.debug("更新一次cookie");
        biliApi.getHome();
    }

    /**
     * 记录已处理过的视频
     *
     * @param videoDetail 被处理的视频
     * @param handleType  处理类型
     */
    public void recordHandleVideo(VideoDetail videoDetail, HandleType handleType) {
        videoDetail.setHandleType(handleType);
        redisUtil.sAdd(HANDLE_VIDEO_ID_KEY, videoDetail.getAid());
        redisUtil.sAdd(HANDLE_VIDEO_DETAIL_KEY, videoDetail);
    }


    /**
     * 添加一个需要处理的视频到缓存中
     *
     * @param videoDetail
     */
    public void addReadyToHandleVideo(VideoDetail videoDetail) {
        redisUtil.sAdd(READY_HANDLE_VIDEO, videoDetail);
        redisUtil.sAdd(READY_HANDLE_VIDEO_ID, videoDetail.getAid());
    }

    /**
     * 处理搜索结果
     * 根据视频信息判断，
     * 最后得出结果，到底是喜欢的视频，还是不喜欢的视频
     * 对于不喜欢的视频，执行点踩操作
     * 对于喜欢视频，执行点赞操作
     *
     * @param thumbUpVideoList
     * @param dislikeVideoList
     * @param avid
     */
    public void handleVideo(List<VideoDetail> thumbUpVideoList,
                            List<VideoDetail> dislikeVideoList,
                            int avid
    ) {

        log.debug("处理视频avid={}", avid);
        if (redisUtil.sIsMember(HANDLE_VIDEO_ID_KEY, avid)
        ||
                redisUtil.sIsMember(READY_HANDLE_VIDEO_ID,avid)
        ) {
            log.info("视频：{} 之前已处理过", avid);
            return;
        }
        VideoDetail videoDetail = null;
        try {
            //0.获取视频详情 实际上，信息已经足够，但是为了模拟用户真实操作，还是调用一次
            videoDetail = biliApi.getVideoDetail(avid);

            //1. 如果是黑名单内的，直接执行点踩操作
            if (blackRuleService.blackMatch(videoDetail)) {
                //点踩
                addReadyToHandleVideo(videoDetail);
                //加日志
                dislikeVideoList.add(videoDetail);
            } else if (whiteRuleService.whiteMatch(videoDetail)) {
                // 3.不是黑名单内的，就一定是我喜欢的吗？ 不一定,比如排行榜的数据，接下来再次判断
                //播放并点赞
                addReadyToHandleVideo(videoDetail);
                //加日志
                thumbUpVideoList.add(videoDetail);
            } else {
                log.info("视频：{}-{} 不属于黑名单也并非白名单", videoDetail.getBvid(), videoDetail.getTitle());
                recordHandleVideo(videoDetail, HandleType.OTHER);
            }

        } catch (Exception e) {
            if (videoDetail != null) {
                //出现任何异常，都进行跳过
                log.error("处理视频：{} 时出现异常，信息如下：", videoDetail.getTitle());
            }
            e.printStackTrace();
        }
    }


    /**
     * 给视频点踩
     *
     * @param aid
     */
    public void dislike(int aid) {
        biliApi.dislike(aid);
    }

    /**
     * 批量给视频点踩
     *
     * @param videoDetailList
     */
    public void dislike(List<VideoDetail> videoDetailList) {
        for (VideoDetail videoDetail : videoDetailList) {
            try {
                if (redisUtil.sIsMember(HANDLE_VIDEO_ID_KEY, videoDetail.getAid())) {
                    log.info("视频：{} 之前已处理过", videoDetail.getAid());
                    continue;
                }

                //为了减少风控，做一些无意义的操作
                if (CollUtil.isEmpty(videoDetail.getTags())) {
                    videoDetail.setTags(biliApi.getVideoTag(videoDetail.getAid()));
                    ThreadUtil.s20();
                }
                log.info("对视频{}-{}进行点踩", videoDetail.getAid(), videoDetail.getTitle());
                recordHandleVideo(videoDetail, HandleType.DISLIKE);
                biliApi.dislike(videoDetail.getAid());
                ThreadUtil.s30();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 播放并点赞
     *
     * @param videoDetail
     */
    public void playAndThumbUp(VideoDetail videoDetail) {
        //模拟播放
        String url = biliApi.getVideoUrl(videoDetail.getBvid(), videoDetail.getCid());
        log.debug("模拟播放，获得的urk={}", url);
        ThreadUtil.sleep(1);
        simulatePlay(videoDetail.getAid(), videoDetail.getCid(), videoDetail.getDuration());
        //点赞
        biliApi.thumpUp(videoDetail.getAid());
    }


    /**
     * 模拟播放，每次播放时间不固定
     * 必须有从开始到结束的几个心跳
     */
    public void simulatePlay(int aid, int cid, int videoDuration) {
        long start_ts = System.currentTimeMillis() / 1000;

        //0.初始播放
        biliApi.reportHeartBeat(
                start_ts,
                aid,
                cid,
                3,
                0,
                2,
                0,
                1,
                0,
                0,
                videoDuration - 1,
                0,
                0
        );

        if (videoDuration <= 15) {
            if (videoDuration >= 7) {
                //时间太短的，则播完
                biliApi.reportHeartBeat(
                        start_ts,
                        aid,
                        cid,
                        3,
                        0,
                        2,
                        videoDuration - 2,
                        1,
                        videoDuration,
                        videoDuration,
                        videoDuration,
                        videoDuration - 1,
                        videoDuration - 1
                );
            } else {
                //7秒以下，不播
                log.error("视频 avid={} 时间={}，时长过短，不播放", aid, videoDuration);
            }
        }
        //本次预计要播放多少秒
        int playTime = DataUtil.getRandom(0, videoDuration);

        //playTime 不能太长,最大值50
        if (playTime >= applicationProperties.getMinPlaySecond()) {
            playTime = applicationProperties.getMinPlaySecond() + DataUtil.getRandom(1, 10);
        }
        //不能太短,最小值 15
        if (playTime <= 15) {
            playTime = playTime + DataUtil.getRandom(1, 10);
        }
        //最终都不能超过videoDuration
        if (playTime >= videoDuration) {
            playTime = videoDuration;
        }

        log.info("视频avid={} 预计观看时间：{}秒", aid, playTime);

        //当前已播放多少秒
        int nowPlayTime = 0;
        while (nowPlayTime + 15 <= playTime) {
            ThreadUtil.sleep(15);
            nowPlayTime += 15;
            biliApi.reportHeartBeat(
                    start_ts,
                    aid,
                    cid,
                    3,
                    0,
                    2,
                    nowPlayTime - 2,
                    1,
                    nowPlayTime,
                    nowPlayTime,
                    videoDuration,
                    nowPlayTime - 1,
                    nowPlayTime - 1
            );
        }
        //收尾操作,如果还差5秒以上没播完，那再播放一次
        int remainingTime = playTime - nowPlayTime;
        ThreadUtil.sleep(remainingTime);
        nowPlayTime += remainingTime;
        biliApi.reportHeartBeat(
                start_ts,
                aid,
                cid,
                3,
                0,
                2,
                nowPlayTime - 2,
                1,
                nowPlayTime,
                nowPlayTime,
                videoDuration,
                nowPlayTime - 1,
                nowPlayTime - 1
        );

    }

    /**
     * 对指定分区的最新视频和排行榜视频进行点踩操作
     * 为减少风控限制，分步执行点踩操作
     *
     * @param tid 分区id
     * @return 本次点踩数量
     */
    public int dislikeByTid(Integer tid) {
        log.debug("----开始对{}分区进行点踩----", tid);

        //1.获取该分区的排行榜视频
        log.info("开始排行榜数据点踩");
        List<VideoDetail> rankVideoList = biliApi.getRankByTid(tid);
        ThreadUtil.s5();
        //分步点踩
        dislike(rankVideoList);

        //2.获取该分区的最新视频
        log.info("开始分区最新视频点踩");
        List<VideoDetail> regionLatestVideo = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            List<VideoDetail> curList = biliApi.getRegionLatestVideo(1, tid);
            regionLatestVideo.addAll(curList);
            dislike(regionLatestVideo);
        }

        log.info("点踩完毕，结束对{}分区的点踩操作，开始训练黑名单", tid);
        ArrayList<VideoDetail> allVideo = new ArrayList<>();
        allVideo.addAll(rankVideoList);
        allVideo.addAll(regionLatestVideo);
        blackRuleService. trainBlacklistByVideoList(allVideo);

        return allVideo.size();
    }

    /**
     * 根据用户id进行点踩
     *
     * @param userId
     * @return
     */
    public int dislikeByUserId(String userId) {
        //该用户会被加入黑名单
        GlobalVariables.addBlackUserId(Collections.singleton(userId));

        //视频详情
        List<VideoDetail> videoDetailList = new ArrayList<>();

        //获取该用户的所有投稿视频
        List<UserSubmissionVideo> allVideo = new ArrayList<>();
        PageBean<UserSubmissionVideo> pageBean;
        int pageNum = 1;
        do {
            pageBean = biliApi.searchUserSubmissionVideo(userId, pageNum, "");
            allVideo.addAll(pageBean.getData());

            ThreadUtil.s20();
            pageNum++;
        } while (pageBean.hasMore());


        //全部进行点踩
        DataUtil.randomAccessList(
                allVideo,
                allVideo.size(),
                video -> {
                    //获取视频详情
                    VideoDetail videoDetail = biliApi.getVideoDetail(video.getAid());
                    videoDetailList.add(videoDetail);
                    ThreadUtil.s2();

                    //点踩
                    dislike(video.getAid());
                    ThreadUtil.s20();
                }
        );

        //开始训练黑名单
       blackRuleService. trainBlacklistByVideoList(videoDetailList);

        return videoDetailList.size();
    }
}
