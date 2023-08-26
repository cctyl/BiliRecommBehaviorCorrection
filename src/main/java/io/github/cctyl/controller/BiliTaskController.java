package io.github.cctyl.controller;

import io.github.cctyl.config.TaskPool;
import io.github.cctyl.entity.R;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.entity.enumeration.HandleType;
import io.github.cctyl.entity.vo.VideoVo;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.task.BiliTask;
import io.github.cctyl.utils.RedisUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;


/**
 * bili任务
 */
@RestController
@RequestMapping("/bili-task")
@Tag(name="bili任务模块")
@Slf4j
public class BiliTaskController {

    @Autowired
    private BiliTask biliTask;

    @Autowired
    private BiliService biliService;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/search-task")
    @Operation(summary = "触发关键词任务")
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
    @Operation(summary = "触发热门排行榜任务")
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
    @Operation(summary = "触发首页推荐任务")
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



    @Operation(summary ="获取等待处理的数据")
    @GetMapping("/ready2handle")
    public R getReady2HandleVideo(){
        List<VideoVo> thumbUpList = new ArrayList<>();
        List<VideoVo> dislikeList = new ArrayList<>();
        redisUtil
                .sMembers(READY_HANDLE_VIDEO)
                .stream()
                .map(VideoDetail.class::cast)
                .map(v -> new VideoVo(v.getAid(), v.getBvid(), v.getTitle(),
                        v.getBlackReason(),
                        v.getThumbUpReason()
                ))
                .forEach(videoVo -> {
                    if (videoVo.getBlackReason() != null) {
                        dislikeList.add(videoVo);
                    } else {
                        thumbUpList.add(videoVo);
                    }
                });

        return R.data(Map.of(
                "dislikeList",dislikeList,
                "thumbUpList",thumbUpList
        ));
    }


    @Operation(summary ="处理等待处理的数据")
    @PostMapping("/ready2handle")
    public R processReady2HandleVideo(
            @RequestBody Map<String,List<VideoVo>> map
    ){

        if (map.get("dislikeList") == null &&
                map.get("thumbUpList") == null
        ) {
            return R.error().setMessage("参数错误");
        }

        TaskPool.putTask(() -> {
            Map<Integer, VideoDetail> handleVideoMap = redisUtil
                    .sMembers(READY_HANDLE_VIDEO)
                    .stream()
                    .map(VideoDetail.class::cast)
                    .collect(Collectors.toMap(VideoDetail::getAid, v -> v, (o1, o2) -> o1));

            List<VideoVo> dislikeVoList = map.get("dislikeList");
            List<VideoVo> thumbUpVoList = map.get("thumbUpList");

            //执行点踩
            for (VideoVo vo : dislikeVoList) {
                if (redisUtil.sIsMember(HANDLE_VIDEO_ID_KEY,vo.getAid())){
                    log.debug("{}-{}已处理过",vo.getAid(),vo.getTitle());
                    continue;
                }

                VideoDetail videoDetail = handleVideoMap.get(vo.getAid());
                if (videoDetail!=null){
                    biliService.dislike(videoDetail.getAid());
                    biliService.recordHandleVideo(videoDetail, HandleType.DISLIKE);
                }else {
                    log.debug("{} - {} 未找到匹配的视频",vo.getBvid(),vo.getTitle());
                }
            }

            //执行点赞
            for (VideoVo vo : thumbUpVoList) {
                if (redisUtil.sIsMember(HANDLE_VIDEO_ID_KEY,vo.getAid())){
                    log.debug("{}-{}已处理过",vo.getAid(),vo.getTitle());
                    continue;
                }

                VideoDetail videoDetail = handleVideoMap.get(vo.getAid());
                if (videoDetail!=null){
                    biliService.playAndThumbUp(videoDetail);
                    biliService.recordHandleVideo(videoDetail, HandleType.THUMB_UP);
                }else {
                    log.debug("{} - {} 未找到匹配的视频",vo.getBvid(),vo.getTitle());
                }
            }

            //清空待处理数据
            redisUtil.delete(READY_HANDLE_VIDEO);
            redisUtil.delete(READY_HANDLE_VIDEO_ID);

        });
        return R.ok();
    }

}
