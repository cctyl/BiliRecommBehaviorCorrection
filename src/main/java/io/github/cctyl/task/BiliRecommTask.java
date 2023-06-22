package io.github.cctyl.task;

import cn.hutool.core.collection.CollUtil;
import io.github.cctyl.utils.RedisUtil;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * bilibili相关的任务
 */
@Component
public class BiliRecommTask {

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 关键词列表
     */
    private Set<String> keywordSet;

    /**
     * 黑名单id列表
     */
    private Set<String> blackUserIdSet;

    /**
     * 黑名单关键词列表
     */
    private Set<String> blackKeywordSet;

    /**
     * 黑名单分区id列表
     */
    private Set<String> blackTidSet;

    /**
     * 初始化
     */
    public BiliRecommTask() {

        //1. 加载关键字数据
        keywordSet = redisUtil.sMembers(KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //2. 加载黑名单用户id列表
        blackUserIdSet = redisUtil.sMembers(BLACK_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //3. 加载黑名单关键词列表
        blackKeywordSet = redisUtil.sMembers(BLACK_KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //4. 加载黑名单分区id列表
        blackTidSet = redisUtil.sMembers(BLACK_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

    }

    @Autowired
    @Qualifier("vchat")
    private WebSocketClient vchatCliet;

    @Scheduled(cron = "*/4 * * * * *")
    public void test() {
        System.out.println("当前时间为：" + LocalDateTime.now());
        redisUtil.set(LocalDateTime.now().toString(),LocalDateTime.now());

    }
}
