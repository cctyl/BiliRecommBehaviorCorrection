package io.github.cctyl.task;

import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.RecommendCard;
import io.github.cctyl.domain.dto.SearchResult;

import io.github.cctyl.domain.po.Task;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.exception.LogOutException;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * bilibili相关的任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BiliTask {


    private final BiliService biliService;

    private final CookieHeaderDataService cookieHeaderDataService;

    private final TaskService taskService;


    /**
     * 每小时查看一下哪些任务需要被执行
     */
    @Scheduled(cron = "0 0 * * * *")
    public void doTask() {
        int hour = LocalDateTime.now().getHour();

        List<Task> enableScheduleTask = taskService.getEnableScheduleTask(hour);
        for (Task task : enableScheduleTask) {
            //定时执行的任务，都不需要参数
            taskService.commonTriggerTask(task.getClassMethodName(), new String[0]);
        }

    }

    @Scheduled(cron = "0 */20 * * * *")
    public void refreshCommomHeaderMap(){
        cookieHeaderDataService. refreshCommonHeaderMap();
    }


    /**
     * 20分钟持久化一次 cookie
     */
//    @Scheduled(cron = "0 */20 * * * *")
//    public void saveRefreshCookie() {
//        log.info("开始持久化 refreshCookie，本次持久化的数量为：{}", cookieHeaderDataService.getRefreshCookieMap().size());
//        if (cookieHeaderDataService.getRefreshCookieMap().isEmpty()) {
//            log.info("cookie 数量为0，不予持久化");
//            return;
//        }
//        cookieHeaderDataService.replaceRefreshCookie(cookieHeaderDataService.getRefreshCookieMap());
//    }


//    /**
//     * 每5小时清理一次待处理视频
//     */
//    @Scheduled(cron = "0 0 0/5 * * ?")
//    public void thirdProcess() {
//        log.info("开始执行第三次处理");
//        biliService.doThirdProcess();
//        log.info("第三次处理执行完成");
//    }


}
