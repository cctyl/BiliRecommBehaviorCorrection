package io.github.cctyl.controller;

import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.entity.Dict;
import io.github.cctyl.pojo.R;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.service.BlackRuleService;
import io.github.cctyl.service.DictService;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.cctyl.pojo.constants.AppConstant.*;

@RestController
@RequestMapping("/black-rule")
@Tag(name = "黑名单规则模块")
@Slf4j
public class BlackRuleController {



    @Autowired
    private BiliService biliService;

    @Autowired
    private BlackRuleService blackRuleService;

    @Autowired
    private DictService dictService;

    @Autowired
    private BiliApi biliApi;

    @Operation(summary = "指定视频是否符合黑名单")
    @GetMapping("/check-video")
    public R checkVideo(
            @Parameter(name = "aid", description = "avid")
            @RequestParam(required = false) Integer aid,
            @Parameter(name = "bvid", description = "bvid")
            @RequestParam(required = false) String bvid
    ) {

        VideoDetail videoDetail;
        if (aid != null) {

            videoDetail = biliApi.getVideoDetail(aid);
        } else if (StrUtil.isNotBlank(bvid)) {
            videoDetail = biliApi.getVideoDetail(bvid);
        } else {
            return R.error().setMessage("参数缺失");
        }
        boolean titleMatch = blackRuleService.isTitleMatch(videoDetail);
        //1.2 简介是否触发黑名单关键词
        boolean descMatch = blackRuleService.isDescMatch(videoDetail);
        //1.3 标签是否触发关键词,需要先获取标签
        boolean tagMatch = blackRuleService.isTagMatch(videoDetail);
        //1.4 up主id是否在黑名单内
        boolean midMatch = blackRuleService.isMidMatch(videoDetail);
        //1.5 分区是否触发
        boolean tidMatch = blackRuleService.isTidMatch(videoDetail);
        //1.6 封面是否触发
        boolean coverMatch = blackRuleService.isCoverMatch(videoDetail);


        HashMap<String, Object> map = new HashMap<>();
        map.put("videoDetail", videoDetail);
        map.put("titleMatch", titleMatch);
        map.put("descMatch", descMatch);
        map.put("tagMatch", tagMatch);
        map.put("midMatch", midMatch);
        map.put("tidMatch", tidMatch);
        map.put("coverMatch", coverMatch);
        map.put("blackReason", videoDetail.getBlackReason());

        return R.data(map);

    }

    @Operation(summary = "对指定分区的 排行榜、热门视频进行点踩")
    @PostMapping("/disklike-by-tid")
    public R dislikeByTid(
            @Parameter(name = "tidList", description = "需要点踩的分区id")
            @RequestParam List<Integer> tidList
    ) {


        TaskPool.putTask(() -> {
            int disklikeNum = 0;
            for (Integer tid : tidList) {
                try {
                    disklikeNum += biliService.dislikeByTid(tid);
                    log.info("完成对{}分区的点踩任务", tid);
                    ThreadUtil.s5();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("本次共对{}个分区:{}进行点踩，共点踩{}个视频",
                    tidList.size(),
                    tidList,
                    disklikeNum
            );
        });
        return R.ok().setMessage("对指定分区点踩任务已开始");
    }

    @Operation(summary = "对指定用户的视频进行点踩")
    @PostMapping("/disklike-by-uid")
    public R dislikeByUserId(
            @Parameter(name = "userIdList", description = "二选一，需要点踩的用户id")
            @RequestParam List<String> userIdList
    ) {
        TaskPool.putTask(() -> {
            int disklikeNum = 0;
            for (String userId : userIdList) {
                try {
                    disklikeNum += biliService.dislikeByUserId(userId);
                    log.info("完成对{}分区的点踩任务", userId);
                    ThreadUtil.s20();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("本次共对{}个用户:{}进行点踩，共点踩{}个视频",
                    userIdList.size(),
                    userIdList,
                    disklikeNum
            );
        });
        return R.ok().setMessage("对指定用户点踩任务已开始");
    }


    @Operation(summary = "获得缓存的训练结果")
    @GetMapping("/cache-train-result")
    public R getCacheTrainResult() {
        List<Dict> keywordList = dictService.findBlackCacheKeyWord();
        List<Dict> tagNameList = dictService.findBlackCacheTag();
        return R.data(Map.of(
                "keywordSet", keywordList,
                "tagNameSet", tagNameList
        ));
    }


    @Operation(summary = "将缓存的结果存入")
    @PutMapping("/cache-train-result")
    public R getCacheTrainResult(
            @RequestBody Map<String, List<String>> map
    ) {
        List<String> keywordIdList = map.getOrDefault("keywordIdList", Collections.emptyList());
        List<String> tagNameIdList = map.getOrDefault("tagNameIdList", Collections.emptyList());

        //添加黑名单关键词
        GlobalVariables.addBlackKeyWordFromCache(keywordIdList);

        //添加黑名单标签
        GlobalVariables.addBlackTagFromCache(tagNameIdList);

        return R.ok();
    }

    @Operation(summary = "获得黑名单分区id")
    @GetMapping("/tid")
    public R getBlackTidSet() {
        List<Dict> blackTid = dictService.findBlackTid();
        return R.ok().setData(blackTid);
    }

    @Operation(summary = "新增黑名单分区id")
    @PostMapping("/tid")
    public R updateBlackTidSet(@RequestBody Set<String> blackTidSet) {
        GlobalVariables.addBlackTidSet(blackTidSet);
        return R.ok();
    }


    @Operation(summary = "获得黑名单关键词列表")
    @GetMapping("/keyword")
    public R getBlackKeywordSet() {
        List<Dict> blackKeyWord = dictService.findBlackKeyWord();
        return R.ok().setData(blackKeyWord);
    }


    @Operation(summary = "添加黑名单关键词")
    @PostMapping("/keyword")
    public R addOrUpdateBlackKeyWord(@RequestBody List<String> keywordList) {
        Set<String> collect = keywordList.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());

        //与忽略的关键词进行过滤
        collect.removeAll(GlobalVariables.getIGNORE_BLACK_KEY_WORD_SET());
        GlobalVariables.addBlackKeyword(collect);

        return R.ok();
    }


    @Operation(summary = "获得黑名单用户id列表")
    @GetMapping("/user-id")
    public R getBlackUserIdSet() {

        List<Dict> blackUserId = dictService.findBlackUserId();
        return R.ok().setData(blackUserId);
    }

    @Operation(summary = "增加黑名单用户id列表")
    @PostMapping("/user-id")
    public R updateBlackUserIdSet(@RequestBody Set<String> blackUserIdSet) {
        GlobalVariables.addBlackUserIdSet(blackUserIdSet);

        return R.ok();
    }


    @Operation(summary = "获得黑名单分区列表")
    @GetMapping("/tag")
    public R getBlackTagSet() {
        List<Dict> blackTag = dictService.findBlackTag();
        return R.ok().setData(blackTag);
    }

    @Operation(summary = "新增黑名单分区名")
    @PostMapping("/tag")
    public R updateBlackTagSet(@RequestBody Set<String> blackTagSet) {

        Set<String> collect = blackTagSet
                .stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
        //与忽略的关键词进行过滤
        collect.removeAll(GlobalVariables.getIGNORE_BLACK_KEY_WORD_SET());
        GlobalVariables.addBlackTagSet(collect);
        return R.ok();
    }


    @Operation(summary = "获得忽略关键词列表")
    @GetMapping("/ignore")
    public R getIgnoreKeyWordSet() {
        List<Dict> blackIgnoreKeyWord = dictService.findBlackIgnoreKeyWord();
        return R.ok().setData(blackIgnoreKeyWord);
    }

    @Operation(summary = "添加到忽略关键词列表")
    @PostMapping("/ignore")
    public R addIgnoreKeyWordSet(@RequestBody Set<String> ignoreKeyWordSet) {

        Set<String> collect = ignoreKeyWordSet
                .stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
        GlobalVariables.addBlackIgnoreKeyword(collect);

        return R.ok();
    }


}
