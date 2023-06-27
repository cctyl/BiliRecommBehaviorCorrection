package io.github.cctyl.service;

import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.SearchResult;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * 相关任务处理
 */
@Service
@Slf4j
public class BiliService {


    @Autowired
    private BiliApi biliApi;


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
     */
    public void handleVideo(List<String> thumbUpVideoList, List<String> dislikeVideoList, SearchResult searchResult) {

        //0.获取视频详情 实际上，信息已经足够，但是为了模拟用户真实操作，还是调用一次
        VideoDetail videoDetail = biliApi.getVideoDetail(searchResult.getBvid());

        //1.模拟播放
        String url = biliApi.getVideoUrl(videoDetail.getBvid(), videoDetail.getCid());

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
