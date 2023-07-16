package io.github.cctyl.task;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.RecommendCard;
import io.github.cctyl.entity.SearchResult;

import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * bilibili相关的任务
 */
@Component
@Slf4j
public class BiliTask {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BiliService biliService;


    @Autowired
    private BiliApi biliApi;


    //@Autowired
    //@Qualifier("vchat")
    //private WebSocketClient vchatCliet;


    /**
     * 执行任务前的准备
     */
    public void before() {
        //0.1 检查cookie
        boolean cookieStatus = biliService.checkCookie();
        if (!cookieStatus) {
            log.error("cookie过期，请更新cookie");
            //todo 发送提醒
            return;
        }

        //0.2 检查accessKey
        try {
            JSONObject jsonObject = biliApi.checkRespAndRetry(() -> biliApi.getUserInfo());
            log.info("accessKey验证通过,body={}", jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("accessKey验证不通过，请检查");
        }

        //0.3 更新一下必要的cookie
        biliService.updateCookie();
    }

    /**
     * 关键词搜索任务
     */
    @Scheduled(cron = "0 9 * * * *")
    public void searchTask() {
        before();
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new ArrayList<VideoDetail>();

        //本次点踩视频列表
        var dislikeVideoList = new ArrayList<VideoDetail>();

        //1.主动搜索，针对搜索视频进行处理
        /*
            一个关键字获取几条？肯定是每个关键字都需要搜索遍历的
            根据关键词搜索后，不能按顺序点击，这是为了模拟用户真实操作
            不能全部分页获取后，再进行点击，这样容易风控
            一个关键词，从两页抽20条
         */
        log.info("==============开始处理关键词==================");
        for (String keyword : GlobalVariables.keywordSet) {
            //不能一次获取完再执行操作，要最大限度模拟用户的行为
            for (int i = 0; i < 2; i++) {
                //执行搜索
                List<SearchResult> searchRaw = biliApi.search(keyword, i);
                ThreadUtil.sleep(3);
                //随机挑选10个
                DataUtil.randomAccessList(searchRaw, 10, searchResult -> {
                    //处理挑选结果
                    biliService.handleVideo(thumbUpVideoList, dislikeVideoList, searchResult.getAid());
                    ThreadUtil.sleep(5);
                });
            }
            ThreadUtil.sleep(3);
        }
        videoLogOutput(thumbUpVideoList, dislikeVideoList);
    }

    /**
     * 热门排行榜任务
     */
    @Scheduled(cron = "0 11 * * * *")
    public void hotRankTask() {
        before();
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new ArrayList<VideoDetail>();

        //本次点踩视频列表
        var dislikeVideoList = new ArrayList<VideoDetail>();

        //2. 对排行榜数据进行处理，处理100条，即5页数据
        log.info("==============开始处理热门排行榜==================");
        for (int i = 1; i <= 10; i++) {
            List<VideoDetail> hotRankVideo = biliApi.getHotRankVideo(i, 20);
            //20条中随机抽10条
            DataUtil.randomAccessList(hotRankVideo, 10, videoDetail -> {
                //处理挑选结果
                biliService.handleVideo(
                        thumbUpVideoList,
                        dislikeVideoList,
                        videoDetail.getAid()
                        );
                ThreadUtil.sleep(5);
            });
            ThreadUtil.sleep(7);
        }
        videoLogOutput(thumbUpVideoList, dislikeVideoList);
    }


    /**
     * 首页推荐任务
     */
    @Scheduled(cron = "0 12 * * * *")
    public void homeRecommendTask() {
        before();
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new ArrayList<VideoDetail>();

        //本次点踩视频列表
        var dislikeVideoList = new ArrayList<VideoDetail>();

        //3. 对推荐视频进行处理
        log.info("==============开始处理首页推荐==================");
        for (int i = 0; i < 10; i++) {
            List<RecommendCard> recommendVideo = biliApi.getRecommendVideo();
            DataUtil.randomAccessList(recommendVideo, 10, recommendCard -> {
                if ("av".equals(recommendCard.getCardGoto())) {
                    //处理挑选结果
                    biliService.handleVideo(
                            thumbUpVideoList,
                            dislikeVideoList,
                            recommendCard.getArgs().getAid()
                            );
                    ThreadUtil.sleep(5);
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
}
