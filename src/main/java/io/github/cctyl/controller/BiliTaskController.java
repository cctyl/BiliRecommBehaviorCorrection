package io.github.cctyl.controller;

import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.query.PageQuery;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.service.impl.BlackRuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


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
    public R getReady2HandleVideo( PageQuery pageQuery,HandleType  handleType) {
        return R.data(videoDetailService.findWithOwnerAndHandle(false,pageQuery,handleType));
    }


    @Operation(summary = "处理单条处理的数据（二次处理）")
    @PutMapping("/process")
    public R processSingleVideo(
            @RequestParam String id,
            @RequestParam HandleType handleType,
            @RequestParam(defaultValue = "被用户反转了判断") String reason
    ) {
       biliService.secondProcessSingleVideo(id,handleType,reason);
       return R.ok();
    }

}
