package io.github.cctyl.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.entity.*;
import io.github.cctyl.entity.enumeration.HandleType;
import io.github.cctyl.entity.vo.VideoVo;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.task.BiliTask;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;


/**
 * bili任务
 */
@RestController
@RequestMapping("/bili-task")
@Api(tags="bili任务模块")
@Slf4j
public class BiliTaskController {

    @Autowired
    private BiliTask biliTask;

    @Autowired
    private BiliService biliService;


    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/search-task")
    @ApiOperation(value = "触发关键词任务")
    public R startSearchTask() {
        if (TaskPool.existsRunningTask()){
            return R.error().setMessage("searchTask 任务正在进行中");
        }
        TaskPool.putTask(() -> {
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
        if (TaskPool.existsRunningTask()){
            return R.error().setMessage("hotRankTask 任务正在进行中");
        }
        TaskPool.putTask(() -> {
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
        if (TaskPool.existsRunningTask()){
            return R.error().setMessage("hotRankTask 任务正在进行中");
        }
        TaskPool.putTask(() -> {
            try {
                biliTask.homeRecommendTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return R.ok();
    }



    @ApiOperation("获取等待处理的数据")
    @GetMapping("/ready2handle")
    public R getReady2HandleVideo(){
        List<String> dislikeList = redisUtil
                .sMembers(READY_HANDLE_DISLIKE_VIDEO)
                .stream()
                .map(VideoDetail.class::cast)
                .map(VideoDetail::getTitle)
                .collect(Collectors.toList());



        List<String> thumbUpList = redisUtil
                .sMembers(READY_HANDLE_THUMB_UP_VIDEO)
                .stream()
                .map(VideoDetail.class::cast)
                .map(VideoDetail::getTitle)
                .collect(Collectors.toList());


        return R.data(Map.of(
                "dislikeList",dislikeList,
                "thumbUpList",thumbUpList
        ));
    }


    @ApiOperation("处理等待处理的数据")
    @PostMapping("/ready2handle")
    public R processReady2HandleVideo(
            @RequestBody Map<String,List<String>> map
    ){

        if (map.get("dislikeList") == null &&
                map.get("thumbUpList") == null
        ) {
            return R.error().setMessage("参数错误");
        }

        TaskPool.putTask(() -> {

            List<VideoDetail> thumbUpList = redisUtil
                    .sMembers(READY_HANDLE_THUMB_UP_VIDEO)
                    .stream()
                    .map(VideoDetail.class::cast)
                    .collect(Collectors.toList());

            List<VideoDetail> dislikeList = redisUtil
                    .sMembers(READY_HANDLE_DISLIKE_VIDEO)
                    .stream()
                    .map(VideoDetail.class::cast)
                    .collect(Collectors.toList());


            List<String> dislikeNameList = map.get("dislikeList");
            List<String> thumbUpNameList = map.get("thumbUpList");

            for (String videoName : dislikeNameList) {
                dislikeList.stream()
                        .filter(videoDetail -> videoDetail.getTitle().equals(videoName))
                        .findFirst()
                        .ifPresentOrElse(videoDetail -> {
                            biliService.dislike(videoDetail.getAid());
                            biliService.recordHandleVideo(videoDetail, HandleType.DISLIKE);
                        },() -> {
                            log.debug("{} 未找到匹配的视频",videoName);
                        });
            }


            for (String videoName : thumbUpNameList) {

                thumbUpList.stream()
                        .filter(videoDetail -> videoDetail.getTitle().equals(videoName))
                        .findFirst()
                        .ifPresentOrElse(videoDetail -> {
                            biliService.playAndThumbUp(videoDetail);
                            biliService.recordHandleVideo(videoDetail, HandleType.THUMB_UP);
                        },() -> {
                            log.debug("{} 未找到匹配的视频",videoName);
                        });
            }


        });
        return R.ok();
    }

}
