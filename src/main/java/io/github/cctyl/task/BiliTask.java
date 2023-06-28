package io.github.cctyl.task;

import cn.hutool.core.collection.CollUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.SearchResult;

import io.github.cctyl.service.BiliService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
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

    /**
     * 关键词列表
     */
    private Set<String> keywordSet;


    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        //1. 加载关键字数据
        keywordSet = redisUtil.sMembers(KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
    }

    @Autowired
    @Qualifier("vchat")
    private WebSocketClient vchatCliet;

    /**
     * bilibili 推荐纠正任务
     */
//    @Scheduled(cron = "*/4 * * * * *")
    public void recommonTask() {
        //0.初始化部分
        //本次点赞视频列表
        var thumbUpVideoList = new ArrayList<String>();

        //本次点踩视频列表
        var dislikeVideoList = new ArrayList<String>();


        //1.检查cookie
        boolean cookieStatus = biliService.checkCookie();
        if (!cookieStatus){
            log.error("cookie过期，请更新cookie");
            //todo 发送提醒
            return;
        }

        //2.更新一下必要的cookie
        biliService.updateCookie();

        //3.主动搜索，针对搜索视频进行处理
        /*
            一个关键字获取几条？肯定是每个关键字都需要搜索遍历的
            根据关键词搜索后，不能按顺序点击，这是为了模拟用户真实操作
            不能全部分页获取后，再进行点击，这样容易风控
            一个关键词，从两页抽20条
         */
        for (String keyword : keywordSet) {
            //不能一次获取完再执行操作，要最大限度模拟用户的行为
            for (int i = 0; i < 2; i++) {
                List<SearchResult> searchRaw = biliApi.search(keyword, i);
                ThreadUtil.sleep(3);

                //随机挑选的结果
                DataUtil
                .getRandom(10, 0, 20)
                .stream().map(searchRaw::get)
                .forEach(searchResult -> {
                    //处理挑选结果
                    biliService.handleVideo(thumbUpVideoList, dislikeVideoList, searchResult,false);
                });
            }
        }


        //4.对排行榜数据进行处理，处理100条，即5页数据

    }
}
