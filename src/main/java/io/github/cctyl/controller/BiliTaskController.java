package io.github.cctyl.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.PageBean;
import io.github.cctyl.entity.R;
import io.github.cctyl.entity.UserSubmissionVideo;
import io.github.cctyl.entity.WhitelistRule;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.task.BiliTask;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.WHITE_LIST_RULE_KEY;


/**
 * bili任务
 */
@RestController
@RequestMapping("/bili-task")
@Api(tags="bili任务模块")
public class BiliTaskController {




    @Autowired
    private BiliTask biliTask;


    @PostMapping("/search-task")
    @ApiOperation(value = "触发关键词任务")
    public R startSearchTask() {
        CompletableFuture.runAsync(() -> {
            try {
                biliTask.searchTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }
    @PostMapping("/hot-rank-task")
    @ApiOperation(value = "触发热门排行榜任务")
    public R startHotRankTask() {
        CompletableFuture.runAsync(() -> {
            try {
                biliTask.hotRankTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }

    @PostMapping("/home-recommend-task")
    @ApiOperation(value = "触发首页推荐任务")
    public R startHomeRecommendTask() {
        CompletableFuture.runAsync(() -> {
            try {
                biliTask.homeRecommendTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }





}
