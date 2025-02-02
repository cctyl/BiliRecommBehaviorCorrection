package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.dfa.WordTree;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.*;
import io.github.cctyl.domain.enumeration.TaskStatus;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.Task;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.exception.LogOutException;
import io.github.cctyl.exception.NotFoundException;
import io.github.cctyl.exception.ServerException;
import io.github.cctyl.service.*;
import io.github.cctyl.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpCookie;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.cctyl.domain.constants.AppConstant.*;


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
    private final CookieHeaderDataService cookieHeaderDataService;

    private final VideoDetailService videoDetailService;
    private final PrepareVideoService prepareVideoService;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final DictService dictService;
    private final TaskService taskService;
    private final ConfigService configService;


    /**
     * 检查cookie状态
     * 调用历史记录接口来实现
     *
     * @return true 有效  false 无效
     */
    public boolean checkCookie() {
        //检查cookie
        Map<String, String> refreshCookie = cookieHeaderDataService.findRefreshCookie();
        if (refreshCookie.get(BILITICKET) == null) {
            updateBiliTicket();
        }

        if (refreshCookie.get(B_NUT) == null) {
            updateBNut();
        }
        if (refreshCookie.get(BUVID3) == null || refreshCookie.get(BUVID4) == null) {
            updateBuvid();
        }


        JSONObject history = biliApi.getHistory();
        log.info("检查cookie状态：{}", history.toString());
        return history.getIntValue("code") == 0;
    }

    /**
     * 更新bNut
     */
    public void updateBNut() {


        HttpResponse execute = biliApi.noAuthCookieGet("https://www.bilibili.com/");
        if (execute.getStatus() == 200) {
            Optional.ofNullable(execute.getCookie(B_NUT))
                    .map(HttpCookie::getValue)
                    .ifPresent(s -> cookieHeaderDataService.updateRefreshCookie(B_NUT, s));
            Optional.ofNullable(execute.getCookie(BUVID3))
                    .map(HttpCookie::getValue)
                    .ifPresent(s -> cookieHeaderDataService.updateRefreshCookie(BUVID3, s));
        }

    }


    /**
     * 填充dict表中,DictType 为mid 的这些用户的用户名
     */
    public void fillDictEmptyUserName() {
        List<Dict> emptyDescMidDict = dictService.findEmptyDescMidDict();
        for (Dict dict : emptyDescMidDict) {

            String mid = dict.getValue();
            String userName = biliApi.onlyGetUserNameByMid(mid);
            if (userName != null) {
                dict.setDesc(userName);
                dictService.updateById(dict);
            }

            ThreadUtil.sleep(10);

        }


    }

    /**
     * 填充空分区描述
     */
    public void fillDictEmptyTname() {
        List<Dict> emptyDescMidDict = dictService.findEmptyDescTidDict();
        List<Region> allRegion = biliApi.getAllRegion(false);
        for (Dict dict : emptyDescMidDict) {

            String tid = dict.getValue();
            allRegion.stream().filter(region -> region.getTid().compareTo(Integer.parseInt(tid)) == 0)
                    .findFirst()
                    .ifPresent(region -> {
                        dict.setDesc(region.getName());
                        dictService.updateById(dict);
                    });
        }

    }


    /**
     * 更新bNut
     */
    public void updateBuvid() {
        String url = "https://api.bilibili.com/x/frontend/finger/spi";
        HttpResponse execute = biliApi.noAuthCookieGet(url);
        if (execute.getStatus() == 200) {
            String body = execute.body();
            JSONObject jsonObject = JSONObject.parseObject(body);

            JSONObject data = jsonObject.getJSONObject("data");
            String b3 = data.getString("b_3");
            String b4 = data.getString("b_4");

            cookieHeaderDataService.updateRefreshCookie(BUVID3, b3);
            cookieHeaderDataService.updateRefreshCookie(BUVID4, b4);

        }

    }

    /**
     * 更新一下必要的cookie
     */
    public void updateCookie() {
        log.debug("更新一次cookie");
        biliApi.getHome();
    }

    /**
     * 更新biliticket
     */
    public void updateBiliTicket() {
        log.debug("updateBiliTicket");
        String biliTicket = biliApi.getBiliTicket();
        cookieHeaderDataService.updateRefreshCookie(BILITICKET, biliTicket);
    }

    /**
     * 记录初次 处理过的视频,要求这个视频必须已经存储到了数据库
     *
     * @param videoDetail 被处理的视频
     * @param handleType  处理类型
     */
    public void recordHandleVideo(VideoDetail videoDetail, HandleType handleType) {
        videoDetail.setHandleType(handleType);
        videoDetail.setHandle(true);//TODO 这里也许应该为false
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
    public void firstProcess(List<VideoDetail> thumbUpVideoList, List<VideoDetail> dislikeVideoList, Long avid,
                             List<WhiteListRule> whitelistRuleList,
                             List<String> whiteUserIdList,
                             List<String> whiteTidList,
                             WordTree blackTagTree,
                             WordTree blackKeywordTree,
                             List<String> blackTidSet
    ) {

        log.debug("处理视频avid={}", avid);
        VideoDetail videoDetail = videoDetailService.findWithDetailByAid(avid);
        if (
                videoDetail != null && (
                        videoDetail.isHandle() ||
                                videoDetail.getBlackReason() != null ||
                                videoDetail.getThumbUpReason() != null

                )
        ) {
            log.info("视频：{} 之前已处理过", avid);
            return;
        }

        try {
            //0.获取视频详情
            if (videoDetail == null) {
                videoDetail = biliApi.getVideoDetail(avid);
            }

            //1. 如果是黑名单内的，直接执行点踩操作
            if (blackRuleService.blackMatch(videoDetail,blackTagTree,blackKeywordTree,blackTidSet)) {
                //点踩
                addReadyToHandleVideo(videoDetail, HandleType.DISLIKE);
                //加日志
                dislikeVideoList.add(videoDetail);
            } else if (whiteRuleService.whiteMatch(videoDetail, whitelistRuleList, whiteUserIdList, whiteTidList)) {
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
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 给视频点踩
     *
     * @param aid
     */
    public void dislike(Long aid) {
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
                log.error(e.getMessage(), e);
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
    public void simulatePlay(Long aid, Long cid, int videoDuration) {
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
        if (playTime >= configService.getMinPlaySecond()) {
            playTime = configService.getMinPlaySecond() + DataUtil.getRandom(1, 10);
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
    public int dislikeByTid(Long tid) {
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
     * @param train
     * @return
     */
    public int dislikeByUserId(String userId, boolean train) {
        //该用户会被加入黑名单
       dictService.addBlackUserId(userId);

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

        if (train) {
            //开始训练黑名单
            blackRuleService.trainBlacklistByVideoList(videoDetailList);
        }


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
                                long dislikeTid,
                                long dislikeTagId,
                                long aid
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
            log.error(e.getMessage(), e);
            throw new RuntimeException("accessKey验证不通过，请检查");
        }

        //0.3 更新一下必要的cookie
        this.updateCookie();
    }


    public boolean doSearchTask() {
        return taskService.doTask(ReflectUtil.getCurrentMethodPath(), this::searchTask);
    }


    public boolean doHotRankTask() {
        return taskService.doTask(ReflectUtil.getCurrentMethodPath(), this::hotRankTask);
    }

    public boolean doHomeRecommendTask() {
        return taskService.doTask(ReflectUtil.getCurrentMethodPath(), this::homeRecommendTask);
    }


    /**
     * 关键词搜索任务 中午12点
     */
    private void searchTask() {
        reentrantLock.lock();
        try {

            before();
            //0.初始化部分
            //本次点赞视频列表
            var thumbUpVideoList = new LinkedList<VideoDetail>();

            //本次点踩视频列表
            var dislikeVideoList = new LinkedList<VideoDetail>();
            List<WhiteListRule> whitelistRuleList = whiteRuleService.getWhitelistRuleList();
            List<String> whiteUserIdSet = dictService.getWhiteUserIdSet();
            List<String> whiteTidSet = dictService.getWhiteTidSet();

            WordTree blackTagTree = dictService.getBlackTagTree();
            WordTree blackKeywordTree = dictService.getBlackKeywordTree();
            List<String> blackTidSet = dictService.getBlackTidSet();

            //1.主动搜索，针对搜索视频进行处理
        /*
            一个关键字获取几条？肯定是每个关键字都需要搜索遍历的
            根据关键词搜索后，不能按顺序点击，这是为了模拟用户真实操作
            不能全部分页获取后，再进行点击，这样容易风控
            一个关键词，从两页抽20条
         */
            log.info("==============开始处理关键词==================");
            for (String keyword : dictService.getSearchKeywordSet()) {
                //不能一次获取完再执行操作，要最大限度模拟用户的行为
                for (int i = 0; i < 2; i++) {
                    //执行搜索
                    List<SearchResult> searchRaw;
                    try {
                        searchRaw = biliApi.search(keyword, i);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        continue;
                    }
                    ThreadUtil.sleep(3);
                    //随机挑选10个
                    DataUtil.randomAccessList(searchRaw, 10, searchResult -> {
                        //处理挑选结果
                        try {
                            this.firstProcess(thumbUpVideoList, dislikeVideoList, searchResult.getAid(),
                                    whitelistRuleList, whiteUserIdSet, whiteTidSet, blackTagTree, blackKeywordTree, blackTidSet
                            );
                            ThreadUtil.sleep(5);
                        } catch (LogOutException e) {
                            throw e;
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                }
                ThreadUtil.sleep(3);
            }
            videoLogOutput(thumbUpVideoList, dislikeVideoList);
        } finally {
            reentrantLock.unlock();
        }

    }

    /**
     * 热门排行榜任务
     */
    private void hotRankTask() {


        reentrantLock.lock();
        try {
            before();
            //0.初始化部分
            //本次点赞视频列表
            var thumbUpVideoList = new LinkedList<VideoDetail>();

            //本次点踩视频列表
            var dislikeVideoList = new LinkedList<VideoDetail>();
            List<WhiteListRule> whitelistRuleList = whiteRuleService.getWhitelistRuleList();
            List<String> whiteUserIdSet = dictService.getWhiteUserIdSet();
            List<String> whiteTidSet = dictService.getWhiteTidSet();

            WordTree blackTagTree = dictService.getBlackTagTree();
            WordTree blackKeywordTree = dictService.getBlackKeywordTree();
            List<String> blackTidSet = dictService.getBlackTidSet();

            //2. 对排行榜数据进行处理，处理100条，即5页数据
            log.info("==============开始处理热门排行榜==================");
            for (int i = 1; i <= 10; i++) {
                List<VideoDetail> hotRankVideo;
                try {
                    hotRankVideo = biliApi.getHotRankVideo(i, 20);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                                ,whitelistRuleList, whiteUserIdSet, whiteTidSet, blackTagTree, blackKeywordTree, blackTidSet
                        );
                        ThreadUtil.sleep(5);
                    } catch (LogOutException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
                ThreadUtil.sleep(7);
            }
            videoLogOutput(thumbUpVideoList, dislikeVideoList);
        } finally {
            reentrantLock.unlock();
        }


    }


    /**
     * 首页推荐任务
     */
    private void homeRecommendTask() {

        reentrantLock.lock();

        try {
            before();
            //0.初始化部分
            //本次点赞视频列表
            var thumbUpVideoList = new LinkedList<VideoDetail>();

            //本次点踩视频列表
            var dislikeVideoList = new LinkedList<VideoDetail>();
            List<WhiteListRule> whitelistRuleList = whiteRuleService.getWhitelistRuleList();
            List<String> whiteUserIdSet = dictService.getWhiteUserIdSet();
            List<String> whiteTidSet = dictService.getWhiteTidSet();

            WordTree blackTagTree = dictService.getBlackTagTree();
            WordTree blackKeywordTree = dictService.getBlackKeywordTree();
            List<String> blackTidSet = dictService.getBlackTidSet();

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
                                    ,whitelistRuleList, whiteUserIdSet, whiteTidSet, blackTagTree, blackKeywordTree, blackTidSet
                            );
                            ThreadUtil.sleep(5);
                        }
                    } catch (LogOutException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
                ThreadUtil.sleep(7);
            }
            videoLogOutput(thumbUpVideoList, dislikeVideoList);
        } finally {
            reentrantLock.unlock();
        }

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
    public void secondProcessSingleVideo(String id, HandleType handleType, String reason, boolean reHandle) {
        VideoDetail video = videoDetailService.getById(id);
        if (video == null) {
            throw new ServerException(400, "视频：" + id + "不存在");
        }

        if ( !reHandle &&  video.isHandle()) {
            throw new ServerException(400, "视频：" + id + "已处理过");
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


    public boolean doThirdProcess() {
       return taskService.doTask(ReflectUtil.getCurrentMethodPath(), this::thirdProcess);
    }

    /**
     * 第三次处理：向bilibili执行反馈（点赞/点踩）
     * 从 PrepareVideo 表中找到视频，每次处理20条
     */
    private void thirdProcess() {

        List<String> dislikeIdList = prepareVideoService.pageFindId(1, 40, HandleType.DISLIKE);
        List<String> thumbUpIdList = prepareVideoService.pageFindId(1, 40, HandleType.THUMB_UP);

        if (!dislikeIdList.isEmpty()) {
            log.debug("三次处理{}条黑名单数据", dislikeIdList.size());
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
                        log.error(e.getMessage(), e);
                    } catch (Exception e) {
                        log.error(e.getMessage());
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
            blackTrainVideoList = null;
            dislikeIdList = null;
        }


        if (!thumbUpIdList.isEmpty()) {
            log.debug("三次处理{}条白名单数据", thumbUpIdList.size());
            //执行点赞
            for (String id : thumbUpIdList) {
                VideoDetail videoDetail = videoDetailService.findWithDetailById(id);
                if (videoDetail != null) {
                    try {
                        this.playAndThumbUp(videoDetail);
                    } catch (NotFoundException e) {
                        log.error(e.getMessage(), e);
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
    public boolean doDefaultProcessVideo() {
        return taskService.doTask(ReflectUtil.getCurrentMethodPath(), this::defaultProcessVideo);
    }
    /**
     * 把未处理的视频，全部加入处理队列中，按照默认的状态去处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void defaultProcessVideo() {

        int pageNo = 1;
        while (true) {
            List<VideoDetail> records = videoDetailService.lambdaQuery()
                    .select(VideoDetail::getId, VideoDetail::getHandleType)
                    .eq(VideoDetail::isHandle, false)
                    .isNotNull(VideoDetail::getHandleType)
                    .page(Page.of(pageNo, 100))
                    .getRecords();
            if (CollUtil.isEmpty(records)) {
                //结束循环
                break;
            }


            for (VideoDetail record : records) {
                this.secondProcessSingleVideo(record.getId(), record.getHandleType(), "默认处理",false);
            }
        }

    }
}
