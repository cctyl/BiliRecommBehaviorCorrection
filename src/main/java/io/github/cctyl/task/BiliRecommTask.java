package io.github.cctyl.task;

import io.github.cctyl.utils.RedisUtil;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * bilibili相关的任务
 */
@Component
public class BiliRecommTask {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("vchat")
    private WebSocketClient vchatCliet;

    @Scheduled(cron = "*/4 * * * * *")
    public void test() {
        System.out.println("当前时间为：" + LocalDateTime.now());
        redisUtil.set(LocalDateTime.now().toString(),LocalDateTime.now());

    }
}
