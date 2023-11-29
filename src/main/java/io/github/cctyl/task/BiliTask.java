package io.github.cctyl.task;

import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.RecommendCard;
import io.github.cctyl.domain.dto.SearchResult;

import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.exception.LogOutException;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    //@Autowired
    //@Qualifier("vchat")
    //private WebSocketClient vchatCliet;


    /**
     * 关键词搜索任务 中午12点
     */
    @Scheduled(cron = "* 0 12 * * *")
    public void searchTask() {
        if (!GlobalVariables.isCron()) {
            return;
        }
        biliService.searchTask();
    }

    /**
     * 热门排行榜任务
     */
    @Scheduled(cron = "* 3 18 * * *")
    public void hotRankTask() {
        if (!GlobalVariables.isCron()) {
            return;
        }
        biliService.hotRankTask();
    }


    /**
     * 首页推荐任务
     */
    @Scheduled(cron = "0 1 19 * * *")
    public void homeRecommendTask() {
        if (!GlobalVariables.isCron()) {
            return;
        }
        biliService.homeRecommendTask();
    }


    /**
     * 20分钟持久化一次 cookie
     */
    @Scheduled(cron = "0 */20 * * * *")
    public void saveRefreshCookie() {
        log.info("开始持久化 refreshCookie，本次持久化的数量为：{}", GlobalVariables.getRefreshCookieMap().size());
        if (GlobalVariables.getRefreshCookieMap().size() == 0) {
            log.info("cookie 数量为0，不予持久化");
            return;
        }
        cookieHeaderDataService.replaceRefreshCookie(GlobalVariables.getRefreshCookieMap());
    }

}
