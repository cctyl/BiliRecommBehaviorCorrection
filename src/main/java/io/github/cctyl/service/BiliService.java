package io.github.cctyl.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.dfa.WordTree;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.SearchResult;
import io.github.cctyl.entity.Tag;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * 黑名单id列表
     */
    private Set<String> blackUserIdSet;

    /**
     * 黑名单关键词列表
     */
    private Set<String> blackKeywordSet;

    /**
     * 黑名单关键词树
     */
    private WordTree blackKeywordTree = new WordTree();

    /**
     * 黑名单分区id列表
     */
    private Set<String> blackTidSet;

    /**
     * 黑名单标签列表
     */
    private Set<String> blackTagSet;

    /**
     * 黑名单标签树
     */
    private WordTree blackTagTree = new WordTree();

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {


        //2. 加载黑名单用户id列表
        blackUserIdSet = redisUtil.sMembers(BLACK_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //3. 加载黑名单关键词列表
        blackKeywordSet = redisUtil.sMembers(BLACK_KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
        //3.1 初始化关键词树
        blackKeywordTree.addWords(blackKeywordSet);


        //4. 加载黑名单分区id列表
        blackTidSet = redisUtil.sMembers(BLACK_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //5.黑名单标签列表
        blackTagSet = redisUtil.sMembers(BLACK_TAG_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
        blackTagTree.addWords(blackTagSet);

    }
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
        biliApi.getHome();
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
     * @param searchResult
     * @param isRank 如果是搜索模式，那么不再黑名单内的都进行点踩。如果是排行榜模式，那么不在黑名单内，还需要判断一次是否在白名单内
     */
    public void handleVideo(List<String> thumbUpVideoList,
                            List<String> dislikeVideoList,
                            SearchResult searchResult,
                            boolean isRank
                            ) {

        //0.获取视频详情 实际上，信息已经足够，但是为了模拟用户真实操作，还是调用一次
        VideoDetail videoDetail = biliApi.getVideoDetail(searchResult.getBvid());

        //1.判断并分类
        //1.1 标题是否触发黑名单关键词
        boolean titleMatch = blackKeywordTree.isMatch(videoDetail.getTitle());

        //1.2 简介是否触发黑名单关键词
        boolean descMatch = blackKeywordTree.isMatch(videoDetail.getDesc());
        if (CollUtil.isNotEmpty(videoDetail.getDescV2())){
            descMatch = descMatch || videoDetail.getDescV2().stream().anyMatch(blackKeywordTree::isMatch);
        }

        //1.3 标签是否触发关键词,需要先获取标签
        boolean tagMatch = biliApi.getVideoTag(videoDetail.getAid()).stream().map(Tag::getTagName).anyMatch(s -> blackTagTree.isMatch(s));

        //1.4 up主id是否在黑名单内
        boolean midMatch = blackUserIdSet.contains(videoDetail.getOwner().getMid());

        //1.5 分区是否触发
        boolean tidMatch = blackTidSet.contains(String.valueOf(videoDetail.getTid()));

        //1.6 todo 封面是否触发
        boolean coverMatch = false;


        //2. 如果是黑名单内的，直接执行点踩操作
        if (titleMatch || descMatch || tagMatch || midMatch || tidMatch || coverMatch) {
            //todo 点踩 加日志

        }else if (isRank){
            // 3. 不是黑名单内的，就一定是我喜欢的吗？ 不一定，接下来再次判断

        }else {

            //4. 搜索模式，那么不是黑名单内的就是喜欢的，执行点赞播放操作

        }

    }


    /**
     * 播放并点赞 todo 点赞未完成
     * @param videoDetail
     */
    public void playAndThumbUp(VideoDetail videoDetail ){

        String url = biliApi.getVideoUrl(videoDetail.getBvid(), videoDetail.getCid());
        simulatePlay(videoDetail.getAid(),videoDetail.getCid(),videoDetail.getDuration());
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

        if (videoDuration<=15 ){
            if ( videoDuration>=7){
                //时间太短的，则播完
                biliApi.reportHeartBeat(
                        start_ts,
                        aid,
                        cid,
                        3,
                        0,
                        2,
                        videoDuration-2,
                        1,
                        videoDuration,
                        videoDuration,
                        videoDuration,
                        videoDuration-1,
                        videoDuration-1
                );
            }else {
                //7秒以下，不播
                log.error("视频 avid={} 时间={}，时长过短，不播放",aid,videoDuration);
            }

        }
        //本次预计要播放多少秒
        int playTime = DataUtil.getRandom(0, videoDuration);
        if (playTime<=15){
            if (playTime+15 <videoDuration){
                playTime = playTime+15;
            }else {
                playTime = videoDuration;
            }
        }

        //playTime 不能太长
        if (playTime>=300){
            playTime = 300;
        }

        log.info("视频avid={} 预计观看时间：{}秒",aid,playTime);

        //当前已播放多少秒
        int nowPlayTime = 0;
        while (nowPlayTime +15 <=playTime){
            ThreadUtil.sleep(15);
            nowPlayTime+=15;
            biliApi.reportHeartBeat(
                    start_ts,
                    aid,
                    cid,
                    3,
                    0,
                    2,
                    nowPlayTime-2,
                    1,
                    nowPlayTime,
                    nowPlayTime,
                    videoDuration,
                    nowPlayTime-1,
                    nowPlayTime-1
            );
        }
        //收尾操作,如果还差5秒以上没播完，那再播放一次
        int remainingTime = playTime-nowPlayTime;
        ThreadUtil.sleep(remainingTime);
        nowPlayTime+=remainingTime;
        biliApi.reportHeartBeat(
                start_ts,
                aid,
                cid,
                3,
                0,
                2,
                nowPlayTime-2,
                1,
                nowPlayTime,
                nowPlayTime,
                videoDuration,
                nowPlayTime-1,
                nowPlayTime-1
        );

    }
}
