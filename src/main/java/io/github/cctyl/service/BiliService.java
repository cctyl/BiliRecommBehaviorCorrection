package io.github.cctyl.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.*;
import io.github.cctyl.entity.enumeration.HandleType;
import io.github.cctyl.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * 相关任务处理
 */
@Service
@Slf4j
public class BiliService {


    @Autowired
    private ImageGenderDetectService imageGenderDetectService;

    @Autowired
    private BiliApi biliApi;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ApplicationProperties applicationProperties;


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
        if (redisUtil.sIsMember(HANDLE_VIDEO_ID_KEY, avid)) {
            log.info("视频：{} 之前已处理过", avid);
            return;
        }
        VideoDetail videoDetail = null;
        try {
            //0.获取视频详情 实际上，信息已经足够，但是为了模拟用户真实操作，还是调用一次
            videoDetail = biliApi.getVideoDetail(avid);

            //1. 如果是黑名单内的，直接执行点踩操作
            if (blackMatch(videoDetail)) {
                //点踩
                dislike(videoDetail.getAid());
                //加日志
                dislikeVideoList.add(videoDetail);
                recordHandleVideo(videoDetail, HandleType.DISLIKE);
            } else if (whiteMatch(videoDetail)) {
                // 3.不是黑名单内的，就一定是我喜欢的吗？ 不一定,比如排行榜的数据，接下来再次判断
                //播放并点赞
                playAndThumbUp(videoDetail);
                //加日志
                thumbUpVideoList.add(videoDetail);
                recordHandleVideo(videoDetail, HandleType.THUMB_UP);
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
     * 白名单判断
     *
     * @param videoDetail
     * @return
     */
    public boolean whiteMatch(VideoDetail videoDetail) {

        /**
         * 假设，白名单使用一个专门的条件构造器，一个对象。里面包含 关键词 分区 up主id 等多个条件
         * 白名单匹配时，需要在单个对象上，找到两个匹配的条件，则表示该条件匹配
         *
         * 那么此时与黑名单产生了割裂，黑名单是任意一个匹配
         *
         * 而关键词列表，不再作为白名单的判断条件
         *
         *
         * 或者说，白名单的关键词 要 配合分区 或 up主id ，达到两个条件以上
         *
         * 错误案例：
         *      刘三金
         *      本来是搜索猫猫的视频，但是出现了一些标题带有刘三金的视频
         *      也进行了点赞，这样非常的不符合。
         *      起码，这个up主在范围内（直接用up主id不就行了），分区在范围内，封面在范围内
         *      所以关键词部分，至少满足： 标题 描述 关键词匹配，分区匹配，封面包含指定关键词 三个条件中两个条件满足
         */
        boolean keyWordFlag = GlobalVariables.whiteKeyWordList
                .stream()
                .anyMatch(item ->
                        {
                            boolean titleMatch = false;
                            boolean descMatch =
                                    false;
                            boolean tagMatch = false;
                            try {
                                titleMatch = item.titleMatch(videoDetail.getTitle());
                                log.info("标题{}匹配结果{}", videoDetail.getTitle(), titleMatch);
                                descMatch = item.descMatch(videoDetail.getDesc())
                                        ||
                                        CollUtil.isNotEmpty(videoDetail.getDescV2()) && videoDetail.getDescV2().stream().anyMatch(
                                                desc -> item.descMatch(desc.getRawText())
                                        );
                                log.info("desc {},{}匹配结果{}", videoDetail.getDesc(), videoDetail.getDescV2(), descMatch);
                                tagMatch = CollUtil.isNotEmpty(videoDetail.getTags()) &&
                                        item.tagNameMatch(
                                                videoDetail.getTags()
                                                        .stream()
                                                        .map(Tag::getTagName)
                                                        .collect(Collectors.toList())
                                        );
                                log.info("tag {}匹配结果{}", videoDetail.getTags()
                                        .stream()
                                        .map(Tag::getTagName)
                                        .collect(Collectors.toList()), tagMatch);
                            } catch (Exception e) {

                                log.error("出现异常:{},视频信息：{}", e.getMessage(), videoDetail.toString());

                                e.printStackTrace();
                            }
                            //两个以上的判断都通过，才表示
                            return Stream.of(titleMatch, descMatch, tagMatch).filter(Boolean.TRUE::equals).count() > 1;
                        }

                );
        try {
            return
                    //up主id处于白名单
                    GlobalVariables.whiteUserIdSet.contains(videoDetail.getOwner().getMid())
                            ||
                            //分区id处于白名单
                            GlobalVariables.whiteTidSet.contains(String.valueOf(videoDetail.getTid()))
                            ||
                            keyWordFlag
                    ;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 黑名单判断
     *
     * @param videoDetail
     * @return
     */
    public boolean blackMatch(VideoDetail videoDetail) {
        //1.1 标题是否触发黑名单关键词
        return isTitleMatch(GlobalVariables.blackKeywordTree, videoDetail)
                ||
                //1.2 简介是否触发黑名单关键词
                isDescMatch(GlobalVariables.blackKeywordTree, videoDetail)
                ||
                //1.3 标签是否触发关键词,需要先获取标签
                isTagMatch(videoDetail)
                ||
                //1.4 up主id是否在黑名单内
                isMidMatch(GlobalVariables.blackUserIdSet, videoDetail)
                ||
                //1.5 分区是否触发
                isTidMatch(GlobalVariables.blackTidSet, videoDetail)
                || //1.6 封面是否触发
                isCoverMatch(videoDetail);
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
                    ThreadUtil.sleep20Second();
                }
                log.info("对视频{}-{}进行点踩",videoDetail.getAid(),videoDetail.getTitle());
                recordHandleVideo(videoDetail,HandleType.DISLIKE);
                biliApi.dislike(videoDetail.getAid());
                ThreadUtil.sleep30Second();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 封面是否匹配
     *
     * @param videoDetail
     * @return
     */
    private boolean isCoverMatch(VideoDetail videoDetail) {
        try {
            byte[] picByte = biliApi.getPicByte(videoDetail.getPic());
            boolean human = imageGenderDetectService.isHuman(picByte);
            log.debug("视频:{}-{}的封面：{}，匹配结果：{}", videoDetail.getBvid(), videoDetail.getTitle(), videoDetail.getPic(), human);
            return human;
        } catch (IOException e) {
            log.error("获取图片字节码出错：{}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 标题匹配
     *
     * @param blackKeywordTree
     * @param videoDetail
     * @return
     */
    private boolean isTitleMatch(WordTree blackKeywordTree, VideoDetail videoDetail) {
        boolean match = blackKeywordTree.isMatch(videoDetail.getTitle());
        log.debug("视频:{}-{}的标题：{}，匹配结果：{}", videoDetail.getBvid(), videoDetail.getTitle(), videoDetail.getTitle(), match);
        return match;
    }

    /**
     * 简介匹配
     *
     * @param blackKeywordTree
     * @param videoDetail
     * @return
     */
    private boolean isDescMatch(WordTree blackKeywordTree, VideoDetail videoDetail) {
        boolean result = blackKeywordTree.isMatch(videoDetail.getDesc());
        String desc = videoDetail.getDesc() == null ? "" : videoDetail.getDesc();
        if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
            result = result || videoDetail.getDescV2().stream().map(DescV2::getRawText).anyMatch(blackKeywordTree::isMatch);
            desc = desc + "," + videoDetail.getDescV2().stream().map(DescV2::getRawText).collect(Collectors.joining(","));
        }
        log.debug("视频:{}-{}的 简介：{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                desc,
                result);
        return result;
    }

    /**
     * 分区id匹配
     *
     * @param blackTidSet
     * @param videoDetail
     * @return
     */
    private boolean isTidMatch(Set<String> blackTidSet, VideoDetail videoDetail) {
        boolean match = blackTidSet.contains(String.valueOf(videoDetail.getTid()));

        log.debug("视频:{}-{}的 分区：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTid(),
                videoDetail.getTname(),
                match);
        return match;
    }

    /**
     * up主id匹配
     *
     * @param blackUserIdSet
     * @param videoDetail
     * @return
     */
    private boolean isMidMatch(Set<String> blackUserIdSet, VideoDetail videoDetail) {
        if (videoDetail.getOwner() == null || videoDetail.getOwner().getMid() == null) {
            log.error("视频:{}缺少up主信息", videoDetail.toString());
            return false;
        }
        boolean match = blackUserIdSet
                .contains(videoDetail.getOwner().getMid());

        log.debug("视频:{}-{}的 up主：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getOwner().getMid(),
                videoDetail.getOwner().getName(),
                match);
        return match;
    }

    /**
     * 标签匹配
     *
     * @param videoDetail
     * @return
     */
    private boolean isTagMatch(VideoDetail videoDetail) {
        boolean match = videoDetail.getTags()
                .stream().map(Tag::getTagName)
                .anyMatch(s -> GlobalVariables.blackTagTree.isMatch(s));

        log.debug("视频:{}-{}的 tag：{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTags(),
                match);

        return match;
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
     * 白名单关键词自动修正补全
     * 传入一个指定的白名单规则对象，
     * 传入你认为应当符合该规则的视频id
     *
     * @param whitelistRule 需要训练的白名单规则
     * @param whiteAvidList 应当符号白名单规则的视频id集合
     */
    public WhitelistRule train(
            WhitelistRule whitelistRule,
            List<Integer> whiteAvidList) {
        if (whitelistRule == null) {
            whitelistRule = new WhitelistRule().setId(IdGenerator.nextId());
        }
        log.info("开始对:{} 规则进行训练,训练数据：{}", whitelistRule.getId(), whiteAvidList);
        List<String> titleProcess = new ArrayList<>();
        List<String> descProcess = new ArrayList<>();
        List<String> tagNameProcess = new ArrayList<>();
        for (Integer avid : whiteAvidList) {
            try {
                VideoDetail videoDetail = biliApi.getVideoDetail(avid);
                //1. 标题处理
                String title = videoDetail.getTitle();
                titleProcess.addAll(SegmenterUtil.process(title));

                //2.描述
                String desc = videoDetail.getDesc();
                if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
                    List<String> descV2Process = videoDetail.getDescV2().stream().map(descV2 -> SegmenterUtil.process(descV2.getRawText()))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    descProcess.addAll(descV2Process);
                }

                descProcess.addAll(SegmenterUtil.process(desc));


                //3.标签
                List<String> tagNameList = videoDetail.getTags().stream().map(Tag::getTagName).collect(Collectors.toList());
                tagNameProcess.addAll(tagNameList);
                log.info("获得视频信息:{}", videoDetail);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ThreadUtil.sleep(10);
        }

        //统计频次
        Map<String, Integer> descKeywordFrequencyMap = SegmenterUtil.generateFrequencyMap(descProcess);
        Map<String, Integer> tagNameFrequencyMap = SegmenterUtil.generateFrequencyMap(tagNameProcess);
        Map<String, Integer> titleKeywordFrequencyMap = SegmenterUtil.generateFrequencyMap(titleProcess);
        List<String> topDescKeyWord = SegmenterUtil.getTop5FrequentWord(descKeywordFrequencyMap);
        List<String> topTagName = SegmenterUtil.getTop5FrequentWord(tagNameFrequencyMap);
        List<String> topTitleKeyWord = SegmenterUtil.getTop5FrequentWord(titleKeywordFrequencyMap);

        log.info("本次训练结束 \r\n\t前5的标题关键词是:{} \r\n\t 前5的标签名是:{} \r\n\t 前5的描述关键词是:{}",
                topTitleKeyWord,
                topTagName,
                topDescKeyWord
        );
        whitelistRule.getTagNameList().addAll(topTagName);
        whitelistRule.getTitleKeyWordList().addAll(topTitleKeyWord);
        whitelistRule.getDescKeyWordList().addAll(topDescKeyWord);

        return whitelistRule;
    }


    /**
     * 对指定分区的最新视频和排行榜视频进行点踩操作
     * 为减少风控限制，分步执行点踩操作
     * @param tid 分区id
     * @return 本次点踩数量
     */
    public int dislikeByTid(Integer tid) {
        log.debug("----开始对{}分区进行点踩----", tid);

        //1.获取该分区的排行榜视频
        log.info("开始排行榜数据点踩");
        List<VideoDetail> rankVideoList = biliApi.getRankByTid(tid);
        ThreadUtil.sleep5Second();
        //分步点踩
        dislike(rankVideoList);


        //2.获取该分区的最新视频
        log.info("开始分区最新视频点踩");
        List<VideoDetail> regionLatestVideo = new ArrayList<>();
        for (int i = 1 ; i <=10; i++) {
            List<VideoDetail> curList = biliApi.getRegionLatestVideo(1, tid);
            regionLatestVideo.addAll(curList);
            dislike(regionLatestVideo);

        }

        log.info("点踩完毕，结束对{}分区的点踩操作，开始训练黑名单",tid);


        ArrayList<VideoDetail> allVideo = new ArrayList<>();
        allVideo.addAll(rankVideoList);
        allVideo.addAll(regionLatestVideo);

        List<String> titleProcess = new ArrayList<>();
        List<String> descProcess = new ArrayList<>();
        List<String> tagNameProcess = new ArrayList<>();

        for (VideoDetail videoDetail : allVideo) {
            if (videoDetail.getOwner()!=null && StrUtil.isNotBlank(videoDetail.getOwner().getMid())){
                GlobalVariables.blackUserIdSet.add(videoDetail.getOwner().getMid());
            }
            //1. 标题处理
            String title = videoDetail.getTitle();
            titleProcess.addAll(SegmenterUtil.process(title));

            //2.描述
            String desc = videoDetail.getDesc();
            if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
                List<String> descV2Process = videoDetail.getDescV2()
                        .stream().map(descV2 -> SegmenterUtil.process(descV2.getRawText()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                descProcess.addAll(descV2Process);
            }
            descProcess.addAll(SegmenterUtil.process(desc));
            //3.标签
            if(CollUtil.isNotEmpty(videoDetail.getTags())){
                List<String> tagNameList = videoDetail.getTags()
                        .stream()
                        .map(Tag::getTagName)
                        .collect(Collectors.toList());
                tagNameProcess.addAll(tagNameList);
            }
        }
        List<String> topDescKeyWord = SegmenterUtil.getTop5FrequentWord(titleProcess);
        List<String> topTagName = SegmenterUtil.getTop5FrequentWord(descProcess);
        List<String> topTitleKeyWord = SegmenterUtil.getTop5FrequentWord(tagNameProcess);

        log.info("本次训练结果： desc关键词:{}, 标签:{}, 标题关键词:{}",topDescKeyWord,
                topTagName,
                topTitleKeyWord);

        //更新到redis中
        GlobalVariables.updateBlackUserId(GlobalVariables.blackUserIdSet);

        GlobalVariables.blackKeywordSet.addAll(topDescKeyWord);
        GlobalVariables.blackKeywordSet.addAll(topTitleKeyWord);
        GlobalVariables.updateBlackKeyword(GlobalVariables.blackKeywordSet);

        GlobalVariables.blackTagSet.addAll(topTagName);
        GlobalVariables.updateBlackTagSet( GlobalVariables.blackTagSet);


        return allVideo.size();
    }
}
