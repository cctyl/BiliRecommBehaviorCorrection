package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.*;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.exception.LogOutException;
import io.github.cctyl.exception.NotFoundException;
import io.github.cctyl.exception.ServerException;
import io.github.cctyl.service.PrepareVideoService;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.service.WhiteListRuleService;
import io.github.cctyl.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 相关任务处理
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BiliService {


    private final BiliApi biliApi;

    private final BlackRuleService blackRuleService;

    private final WhiteListRuleService whiteRuleService;

    private final VideoDetailService videoDetailService;
    private final PrepareVideoService prepareVideoService;


    /**
     * 检查cookie状态
     * 调用历史记录接口来实现
     *
     * @return true 有效  false 无效
     */
    public boolean checkCookie() {
        JSONObject history = biliApi.getHistory();
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
     * 记录初次 处理过的视频,要求这个视频必须已经存储到了数据库
     *
     * @param videoDetail 被处理的视频
     * @param handleType  处理类型
     */
    public void recordHandleVideo(VideoDetail videoDetail, HandleType handleType) {
        videoDetail.setHandleType(handleType);
        videoDetail.setHandle(true);
        if (videoDetail.getId() == null) {
            videoDetailService.saveVideoDetail(videoDetail);
        } else {
            videoDetailService.updateHandleInfoById(videoDetail);
        }


    }


    /**
     * 添加一个需要处理的视频到数据库
     *
     * @param videoDetail
     */
    public void addReadyToHandleVideo(VideoDetail videoDetail, HandleType handleType) {
        videoDetail.setHandle(false)
                .setHandleType(handleType);
        videoDetailService.saveVideoDetail(videoDetail);
    }

    /**
     * 初次处理视频
     * 根据视频信息判断，
     * 最后得出结果，到底是喜欢的视频，还是不喜欢的视频
     * 对于不喜欢的视频，执行点踩操作
     * 对于喜欢视频，执行点赞操作
     *
     * @param thumbUpVideoList
     * @param dislikeVideoList
     * @param avid
     */
    public void firstProcess(List<VideoDetail> thumbUpVideoList,
                             List<VideoDetail> dislikeVideoList,
                             int avid
    ) {

        log.debug("处理视频avid={}", avid);
        VideoDetail videoDetail = videoDetailService.findWithDetailByAid(avid);
        if (
                videoDetail != null && (
                        videoDetail.isHandle() ||
                        videoDetail.getBlackReason()!=null ||
                        videoDetail.getThumbUpReason()!=null

                )
        ) {
            log.info("视频：{} 之前已处理过", avid);
            return;
        }

        try {
            //0.获取视频详情 实际上，信息已经足够，但是为了模拟用户真实操作，还是调用一次
            if (videoDetail == null) {
                videoDetail = biliApi.getVideoDetail(avid);
            }

            //1. 如果是黑名单内的，直接执行点踩操作
            if (blackRuleService.blackMatch(videoDetail)) {
                //点踩
                addReadyToHandleVideo(videoDetail, HandleType.DISLIKE);
                //加日志
                dislikeVideoList.add(videoDetail);
            } else if (whiteRuleService.whiteMatch(videoDetail)) {
                // 3.不是黑名单内的，就一定是我喜欢的吗？ 不一定,比如排行榜的数据，接下来再次判断
                //播放并点赞
                addReadyToHandleVideo(videoDetail, HandleType.THUMB_UP);
                //加日志
                thumbUpVideoList.add(videoDetail);
            } else {
                log.info("视频：{}-{} 不属于黑名单也并非白名单", videoDetail.getBvid(), videoDetail.getTitle());
                recordHandleVideo(videoDetail, HandleType.OTHER);
            }

        } catch (Exception e) {

            //出现任何异常，都进行跳过
            log.error("处理视频：{} 时出现异常，信息如下：", Opt.ofNullable(videoDetail)
                    .map(VideoDetail::getTitle).get());
            log.error(e.getMessage(),e);
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
            VideoDetail byAid = videoDetailService.findByAid(videoDetail.getAid());
            try {
                if (byAid != null && byAid.isHandle()) {
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
                log.error(e.getMessage(),e);
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
        if (playTime >= GlobalVariables.getMinPlaySecond()) {
            playTime = GlobalVariables.getMinPlaySecond() + DataUtil.getRandom(1, 10);
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
        blackRuleService.trainBlacklistByVideoList(allVideo);

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
        GlobalVariables.INSTANCE.addBlackUserId(userId);

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
        blackRuleService.trainBlacklistByVideoList(videoDetailList);

        return videoDetailList.size();
    }

    /**
     * 带理由的不喜欢
     *
     * @param dislikeReason
     * @param dislikeMid
     * @param dislikeTid
     * @param dislikeTagId
     */
    public void dislikeByReason(DislikeReason dislikeReason,
                                String dislikeMid,
                                Integer dislikeTid,
                                Integer dislikeTagId,
                                Integer aid
    ) {

        biliApi.dislikeByReason(dislikeReason,
                dislikeMid,
                dislikeTid,
                dislikeTagId,
                aid
        );
    }


    /**
     * 批量处理等待处理的数据(二次处理)
     * 这些数据已存储到数据库中
     */
    @Deprecated
    public void processReady2HandleVideo(Map<String, List<String>> map) {

        List<String> dislikeIdList = map.getOrDefault("dislikeList", Collections.emptyList());
        List<String> thumbUpIdList = map.getOrDefault("thumbUpList", Collections.emptyList());
        List<String> other = map.getOrDefault("other", Collections.emptyList());

        if (!dislikeIdList.isEmpty()) {
            List<VideoDetail> blackTrainVideoList = new ArrayList<>();
            //执行点踩
            for (String id : dislikeIdList) {
                VideoDetail videoDetail = videoDetailService.findWithDetailById(id);

                if (videoDetail != null) {
                    blackTrainVideoList.add(videoDetail);
                    if (videoDetail.getDislikeReason() != null) {
                        this.dislikeByReason(videoDetail.getDislikeReason(),
                                String.valueOf(videoDetail.getDislikeMid()),
                                videoDetail.getDislikeTid(),
                                videoDetail.getDislikeTagId(),
                                videoDetail.getAid()
                        );
                    }
                    this.dislike(videoDetail.getAid());
                    this.recordHandleVideo(videoDetail, HandleType.DISLIKE);
                } else {
                    log.debug("{} - {} 未找到匹配的视频", videoDetail.getBvid(), videoDetail.getTitle());
                }
            }
            //进行黑名单训练
            blackRuleService.trainBlacklistByVideoList(blackTrainVideoList);
        }


        if (!thumbUpIdList.isEmpty()) {
            //执行点赞
            for (String id : thumbUpIdList) {
                VideoDetail videoDetail = videoDetailService.findWithDetailById(id);
                if (videoDetail != null) {
                    this.playAndThumbUp(videoDetail);
                    this.recordHandleVideo(videoDetail, HandleType.THUMB_UP);
                } else {
                    log.debug("{} - {} 未找到匹配的视频", videoDetail.getBvid(), videoDetail.getTitle());
                }
            }
        }


        if (!other.isEmpty()) {
            //不处理的
            for (String id : other) {
                VideoDetail videoDetail = videoDetailService.findWithDetailById(id);
                this.recordHandleVideo(videoDetail, HandleType.OTHER);
            }
        }


    }


    /**
     * 执行任务前的准备
     */
    public void before() {
        //0.1 检查cookie
        boolean cookieStatus = this.checkCookie();
        if (!cookieStatus) {
            //todo 发送提醒
            throw new RuntimeException("cookie过期，请更新cookie");
        }

        //0.2 检查accessKey
        try {
            JSONObject jsonObject = biliApi.getUserInfo();
            log.info("accessKey验证通过,body={}", jsonObject.toString());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException("accessKey验证不通过，请检查");
        }

        //0.3 更新一下必要的cookie
        this.updateCookie();
    }


    /**
     * 关键词搜索任务 中午12点
     */
    public void searchTask() {

        before();
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new LinkedList<VideoDetail>();

        //本次点踩视频列表
        var dislikeVideoList = new LinkedList<VideoDetail>();

        //1.主动搜索，针对搜索视频进行处理
        /*
            一个关键字获取几条？肯定是每个关键字都需要搜索遍历的
            根据关键词搜索后，不能按顺序点击，这是为了模拟用户真实操作
            不能全部分页获取后，再进行点击，这样容易风控
            一个关键词，从两页抽20条
         */
        log.info("==============开始处理关键词==================");
        for (String keyword : GlobalVariables.getSearchKeywordSet()) {
            //不能一次获取完再执行操作，要最大限度模拟用户的行为
            for (int i = 0; i < 2; i++) {
                //执行搜索
                List<SearchResult> searchRaw;
                try {
                    searchRaw = biliApi.search(keyword, i);
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    continue;
                }
                ThreadUtil.sleep(3);
                //随机挑选10个
                DataUtil.randomAccessList(searchRaw, 10, searchResult -> {
                    //处理挑选结果
                    try {
                        this.firstProcess(thumbUpVideoList, dislikeVideoList, searchResult.getAid());
                        ThreadUtil.sleep(5);
                    } catch (LogOutException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                    }
                });
            }
            ThreadUtil.sleep(3);
        }
        videoLogOutput(thumbUpVideoList, dislikeVideoList);
    }

    /**
     * 热门排行榜任务
     */
    public void hotRankTask() {

        before();
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new LinkedList<VideoDetail>();

        //本次点踩视频列表
        var dislikeVideoList = new LinkedList<VideoDetail>();

        //2. 对排行榜数据进行处理，处理100条，即5页数据
        log.info("==============开始处理热门排行榜==================");
        for (int i = 1; i <= 10; i++) {
            List<VideoDetail> hotRankVideo;
            try {
                hotRankVideo = biliApi.getHotRankVideo(i, 20);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                continue;
            }
            //20条中随机抽10条
            DataUtil.randomAccessList(hotRankVideo, 10, videoDetail -> {
                try {
                    //处理挑选结果
                    this.firstProcess(
                            thumbUpVideoList,
                            dislikeVideoList,
                            videoDetail.getAid()
                    );
                    ThreadUtil.sleep(5);
                } catch (LogOutException e) {
                    throw e;
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            });
            ThreadUtil.sleep(7);
        }
        videoLogOutput(thumbUpVideoList, dislikeVideoList);
    }


    /**
     * 首页推荐任务
     */
    public void homeRecommendTask() {

        before();
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new LinkedList<VideoDetail>();

        //本次点踩视频列表
        var dislikeVideoList = new LinkedList<VideoDetail>();

        //3. 对推荐视频进行处理
        log.info("==============开始处理首页推荐==================");
        for (int i = 0; i < 10; i++) {
            List<RecommendCard> recommendVideo = biliApi.getRecommendVideo();
            DataUtil.randomAccessList(recommendVideo, 10, recommendCard -> {

                try {
                    if ("av".equals(recommendCard.getCardGoto())) {
                        //处理挑选结果
                        this.firstProcess(
                                thumbUpVideoList,
                                dislikeVideoList,
                                recommendCard.getArgs().getAid()
                        );
                        ThreadUtil.sleep(5);
                    }
                } catch (LogOutException e) {
                    throw e;
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            });
            ThreadUtil.sleep(7);
        }
        videoLogOutput(thumbUpVideoList, dislikeVideoList);
    }

    /**
     * 执行完毕后输出日志
     */
    public void videoLogOutput(List<VideoDetail> thumbUpVideoList, List<VideoDetail> dislikeVideoList) {
        log.info("本次点赞的视频：{}", thumbUpVideoList.stream().map(VideoDetail::getTitle).collect(Collectors.toList()));
        log.info("本次点踩的视频：{}", dislikeVideoList.stream().map(VideoDetail::getTitle).collect(Collectors.toList()));
    }


    /**
     * 处理单个视频（二次处理）
     * 本质是先修改处理状态，然后将视频加入队列中，等待定时任务处理
     *
     * @param id
     * @param handleType
     */
    @Transactional(rollbackFor = ServerException.class)
    public void secondProcessSingleVideo(String id, HandleType handleType, String reason) {
        VideoDetail video = videoDetailService.getById(id);
        if (video == null) {
            throw new ServerException(400,"视频："+id+"不存在");
        }

        if (video.isHandle()){
            throw new ServerException(400,"视频："+id+"已处理过");
        }

        //黑名单其实可能变成白名单，存在反转问题，因此这个handleType 也需要进行更新
        if (
                video.getHandleType() == null
                        || !video.getHandleType().equals(handleType)
        ) {
            //原本的处理类型是空，或者HandleType发生变化
            if (HandleType.THUMB_UP.equals(handleType)) {
                video.setThumbUpReason(reason)
                        .setBlackReason("ErrorReason:" + Opt.ofNullable(video.getBlackReason()).orElse(""))
                ;
            } else if (HandleType.DISLIKE.equals(handleType)) {
                video.setBlackReason(reason)
                        .setThumbUpReason("ErrorReason:" + Opt.ofNullable(video.getThumbUpReason()).orElse(""))
                ;
            } else {
                //OTHER 类型，不需要黑白名单理由
                video.setBlackReason(null)
                        .setThumbUpReason(null);
            }
        }

        //此视频已经被处理
        video.setHandle(true)
                .setHandleType(handleType)
        ;
        videoDetailService.updateProcessInfo(video);

        //加入处理队列当中（其他类型不需要向bilibili  反馈，忽略）
        if (!HandleType.OTHER.equals(handleType)) {
            prepareVideoService.saveIfNotExists(id, handleType);
        }
    }


    /**
     * 第三次处理：向bilibili执行反馈（点赞/点踩）
     * 从 PrepareVideo 表中找到视频，每次处理20条
     */
    public void thirdProcess() {

        List<String> dislikeIdList = prepareVideoService.pageFindId(1, 100, HandleType.DISLIKE);
        List<String> thumbUpIdList = prepareVideoService.pageFindId(1, 100, HandleType.THUMB_UP);

        if (dislikeIdList.size() > 40) {
            List<VideoDetail> blackTrainVideoList = new ArrayList<>(dislikeIdList.size());
            //执行点踩
            for (String id : dislikeIdList) {
                VideoDetail videoDetail = videoDetailService.findWithDetailById(id);

                if (videoDetail != null) {
                    blackTrainVideoList.add(videoDetail);
                    try {
                        if (videoDetail.getDislikeReason() != null) {
                            this.dislikeByReason(videoDetail.getDislikeReason(),
                                    String.valueOf(videoDetail.getDislikeMid()),
                                    videoDetail.getDislikeTid(),
                                    videoDetail.getDislikeTagId(),
                                    videoDetail.getAid()
                            );
                        }
                        this.dislike(videoDetail.getAid());
                    } catch (NotFoundException e) {
                        log.error(e.getMessage(),e);
                    }
                    //删除数据库记录
                    prepareVideoService.removeByVideoId(id);

                } else {
                    log.debug("{} - {} 未找到匹配的视频", Opt.ofNullable(videoDetail).map(VideoDetail::getBvid).orElse(null),
                            Opt.ofNullable(videoDetail).map(VideoDetail::getTitle).orElse(null)
                    );
                }
            }
            //进行黑名单训练
            blackRuleService.trainBlacklistByVideoList(blackTrainVideoList);
            blackTrainVideoList =null;
            dislikeIdList = null;
        }


        if (thumbUpIdList.size() > 20) {
            //执行点赞
            for (String id : thumbUpIdList) {
                VideoDetail videoDetail = videoDetailService.findWithDetailById(id);
                if (videoDetail != null) {
                    try {
                        this.playAndThumbUp(videoDetail);

                    } catch (NotFoundException e) {
                        log.error(e.getMessage(),e);
                    }
                    //删除数据库记录
                    prepareVideoService.removeByVideoId(id);
                } else {
                    log.debug("{} - {} 未找到匹配的视频", Opt.ofNullable(videoDetail).map(VideoDetail::getBvid).orElse(null),
                            Opt.ofNullable(videoDetail).map(VideoDetail::getTitle).orElse(null)
                    );
                }
            }
            thumbUpIdList = null;
        }
    }

}
