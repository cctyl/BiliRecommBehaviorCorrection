package io.github.cctyl.controller;

import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.query.PageQuery;
import io.github.cctyl.domain.vo.VideoVo;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.service.impl.BlackRuleService;
import io.github.cctyl.task.BiliTask;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


/**
 * bili任务
 */
@RestController
@RequestMapping("/bili-task")
@Tag(name = "bili任务模块")
@Slf4j
@RequiredArgsConstructor
public class BiliTaskController {


    private final BiliService biliService;
    private final BlackRuleService blackRuleService;
    private final VideoDetailService videoDetailService;

    @PostMapping("/search-task")
    @Operation(summary = "触发关键词任务")
    public R startSearchTask() {

        if (TaskPool.existsRunningTask()) {
            return R.error().setMessage("searchTask 任务正在进行中");
        }
        TaskPool.putTask(() -> {
            try {
                biliService.searchTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }

    @PostMapping("/hot-rank-task")
    @Operation(summary = "触发热门排行榜任务")
    public R startHotRankTask() {
        if (TaskPool.existsRunningTask()) {
            return R.error().setMessage("hotRankTask 任务正在进行中");
        }
        TaskPool.putTask(() -> {
            try {
                biliService.hotRankTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }

    @PostMapping("/home-recommend-task")
    @Operation(summary = "触发首页推荐任务")
    public R startHomeRecommendTask() {
        if (TaskPool.existsRunningTask()) {
            return R.error().setMessage("hotRankTask 任务正在进行中");
        }
        TaskPool.putTask(() -> {
            try {
                biliService.homeRecommendTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }


    @Operation(summary = "获取等待处理的数据")
    @GetMapping("/ready2handle")
    public R getReady2HandleVideo(@ParameterObject PageQuery pageQuery,HandleType  handleType) {
        return R.data(videoDetailService.findWithOwnerAndHandle(false,pageQuery,handleType));
    }


    @Operation(summary = "处理等待处理的数据")
    @PostMapping("/ready2handle")
    public R processReady2HandleVideo(
            @RequestBody Map<String, List<String>> map
    ) {

        if (map.get("dislikeList") == null &&
                map.get("thumbUpList") == null
        ) {
            return R.error().setMessage("参数错误");
        }

        TaskPool.putTask(() -> {
            biliService.processReady2HandleVideo(map);
        });
        return R.ok();
    }


    @Operation(summary = "处理单条处理的数据")
    @PutMapping("/process")
    public R processSingleVideo(
            @RequestParam Integer aid,
            @RequestParam HandleType handleType
    ) {

        //TODO
        throw new RuntimeException("暂未实现");

    }

}
