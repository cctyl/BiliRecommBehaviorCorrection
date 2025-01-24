package io.github.cctyl.controller;

import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.Task;
import io.github.cctyl.domain.query.PageQuery;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.service.impl.BlackRuleService;

import io.github.cctyl.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


/**
 * bili任务
 */
@RestController
@RequestMapping("/task")
@Tag(name = "bili任务模块")
@Slf4j
@RequiredArgsConstructor
public class TaskController {


    private final BiliService biliService;
    private final BlackRuleService blackRuleService;
    private final TaskService taskService;
    private final VideoDetailService videoDetailService;
    private static final String BILI_SERVICE_CLASS_NAME = BiliService.class.getName();


    @GetMapping("/task-list")
    @Operation(summary = "查询任务列表")
    public R<List<Task>> getTaskList() {
        return R.data(taskService.getTaskList());
    }

    @PutMapping("")
    @Operation(summary = "更新任务")
    public R updateTask(@RequestBody Task task) {
        taskService.updateById(task);
        return R.ok();
    }

    @GetMapping("/common-trigger-task")
    public R commonTriggerTask(@RequestParam String classAndMethodName) throws ClassNotFoundException {

        return taskService.commonTriggerTask(classAndMethodName);
    }


    @Operation(summary = "获取等待处理的数据")
    @GetMapping("/ready2handle")
    public R getReady2HandleVideo(PageQuery pageQuery, HandleType handleType) {
        return R.data(videoDetailService.findWithOwnerAndHandle(false, pageQuery, handleType));
    }


    @Operation(summary = "处理单条处理的数据（二次处理）")
    @PutMapping("/process")
    public R processSingleVideo(
            @RequestParam String id,
            @RequestParam HandleType handleType,
            @RequestParam(defaultValue = "被用户反转了判断") String reason
    ) {
        biliService.secondProcessSingleVideo(id, handleType, reason);
        return R.ok();
    }


    @Operation(summary = "按照默认状态处理所有未处理视频")
    @PutMapping("/default-process")
    public R defaultProcessVideo() {
        biliService.defaultProcessVideo();
        return R.ok();
    }

    @Operation(summary = "主动触发三次处理")
    @PutMapping("/third-process")
    public R thirdProcess() {
        TaskPool.putTask(biliService::thirdProcess);
        return R.ok();
    }
}
