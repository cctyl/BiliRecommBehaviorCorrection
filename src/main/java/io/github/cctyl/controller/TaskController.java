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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


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
    private final VideoDetailService videoDetailService;
    private static final String BILI_SERVICE_CLASS_NAME = BiliService.class.getName();


    @GetMapping("/running-task")
    @Operation(summary = "查询正在运行的任务")
    public R getRunningTask() {
        return R.data(TaskPool.getRunningTaskName());
    }


    @GetMapping("common-trigger-task")
    public R commonTriggerTask(@RequestParam String taskName) {
        Class<?> clazz;
        if (taskName.startsWith(BILI_SERVICE_CLASS_NAME)) {
            clazz = BiliService.class;
            taskName = taskName.replace(BILI_SERVICE_CLASS_NAME + ".", "");
        } else {
            return R.error().setMessage("类名不存在");
        }

        try {
            Method taskMethod = clazz.getDeclaredMethod(taskName);
            boolean invoke = (boolean) taskMethod.invoke(biliService);
            if (invoke) {
                return R.ok().setData(taskName + " 任务已启动");
            } else {
                return R.error().setMessage(taskName + " 任务正在进行中");
            }
        } catch (NoSuchMethodException e) {
            return R.error().setMessage("无此任务：" + taskName);
        } catch (IllegalAccessException e) {
            return R.error().setMessage("该任务不允许外部访问：" + taskName);
        } catch (InvocationTargetException e) {
            return R.error().setMessage("调用异常");
        }
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
