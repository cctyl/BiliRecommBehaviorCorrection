package io.github.cctyl.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.service.impl.BlackRuleService;
import io.github.cctyl.service.DictService;
import io.github.cctyl.utils.ReflectUtil;
import io.github.cctyl.utils.ThreadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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


    @Autowired
    private TaskService taskService;


    @Operation(summary = "对指定分区的 排行榜、热门视频进行点踩")
    @PostMapping("/disklike-by-tid")
    public R dislikeByTid(@Parameter(name = "tidList", description = "需要点踩的分区id") @RequestBody List<Long> tidList) {


        boolean b = taskService.doTask(ReflectUtil.getCurrentMethodPath(), () -> {
            int disklikeNum = 0;
            for (Long tid : tidList) {
                try {
                    disklikeNum += biliService.dislikeByTid(tid);
                    log.info("完成对{}分区的点踩任务", tid);
                    ThreadUtil.s5();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info("本次共对{}个分区:{}进行点踩，共点踩{}个视频", tidList.size(), tidList, disklikeNum);

        });
        if (b){
            return R.ok().setMessage("对指定分区点踩任务已开始");
        }else {
            return R.error().setMessage("该任务正在被运行中，请等待上一个任务结束");
        }

    }

    @Operation(summary = "对指定用户的视频进行点踩")
    @PostMapping("/disklike-by-uid")
    public R dislikeByUserId(@Parameter(name = "userIdList", description = "二选一，需要点踩的用户id") @RequestBody List<String> userIdList,
                             @Parameter(name = "train", description = "是否将这些用户的投稿视频加入黑名单训练") @RequestParam boolean train
                             ) {
        boolean b =taskService.doTask(ReflectUtil.getCurrentMethodPath(), () -> {
            int disklikeNum = 0;
            for (String userId : userIdList) {
                try {
                    disklikeNum += biliService.dislikeByUserId(userId,train);
                    log.info("完成对{}分区的点踩任务", userId);
                    ThreadUtil.s20();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info("本次共对{}个用户:{}进行点踩，共点踩{}个视频", userIdList.size(), userIdList, disklikeNum);
        });

        if (b){
            return R.ok().setMessage("对指定用户点踩任务已开始");
        }else {
            return R.error().setMessage("该任务正在被运行中，请等待上一个任务结束");
        }
    }


    @Operation(summary = "获得缓存的训练结果")
    @GetMapping("/cache-train-result/{type}")
    public R getCacheTrainResult(@PathVariable("type") DictType type) {

        List<Dict> list = new ArrayList<>(0);
        if (type.equals(DictType.KEYWORD)) {
            list = dictService.findBlackCacheKeyWord();

        } else if (type.equals(DictType.TAG)) {
            list = dictService.findBlackCacheTag();
        }
        return R.data(list);
    }


    @Operation(summary = "将缓存的结果存入")
    @PutMapping("/cache-train-result/{type}")
    public R getCacheTrainResult(@PathVariable("type") DictType type,
                                 @RequestBody Map<String, List<String>> map

    ) {
        List<String> selectedId = map.getOrDefault("selectedId", new ArrayList<String>());
        List<String> discardedId = map.getOrDefault("discardedId", new ArrayList<String>());


        if (type.equals(DictType.KEYWORD)) {

            if (!selectedId.isEmpty()) {
                //添加黑名单关键词
               dictService.addBlackKeyWordFromCache(selectedId);
            }

            //舍弃的关键词下次不会再出现
            if (!discardedId.isEmpty()) {
                dictService.updateAccessTypeAndDictTypeByIdIn(AccessType.BLACK, DictType.IGNORE_KEYWORD, discardedId);
                //TODO 需要更新Global 里面的缓存，除非去掉Global
            }

        } else if (type.equals(DictType.TAG)) {
            if (!selectedId.isEmpty()) {
                //添加黑名单标签
               dictService.addBlackTagFromCache(selectedId);
            }
            //舍弃的标签下次不会再出现
            if (!discardedId.isEmpty()) {
                dictService.updateAccessTypeAndDictTypeByIdIn(AccessType.BLACK, DictType.IGNORE_TAG, discardedId);
            }

        }

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
        dictService.addBlackTidSet(blackTidSet);
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
        Set<String> collect = keywordList.stream().filter(StrUtil::isNotBlank).map(String::trim).collect(Collectors.toSet());

        //与忽略的关键词进行过滤
        collect.removeAll(dictService.getIgnoreBlackKeyWordSet());
        dictService.addBlackKeyword(collect);

        return R.ok();
    }


    @Operation(summary = "获得黑名单用户id列表")
    @GetMapping("/user-id")
    public R getBlackUserIdSet() {

        List<Dict> blackUserId = dictService.findBlackUserId();
        return R.ok().setData(blackUserId);
    }

    @Operation(summary = "修改黑名单用户id列表")
    @PostMapping("/user-id")
    public R updateBlackUserIdSet(@RequestBody Set<String> blackUserIdSet) {
        dictService.addBlackUserIdSet(blackUserIdSet);

        return R.ok();
    }

    @Operation(summary = "修改黑名单用户id列表")
    @DeleteMapping("/user-id")
    public R deleteBlackUserIdSet(@RequestBody Set<String> blackUserIdSet) {
        dictService.delBlackUserIdSet(blackUserIdSet);

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

        Set<String> collect = blackTagSet.stream().filter(StrUtil::isNotBlank).map(String::trim).collect(Collectors.toSet());
        //与忽略的关键词进行过滤
        collect.removeAll(dictService.getIgnoreBlackKeyWordSet());
        dictService.addBlackTagSet(collect);
        return R.ok();
    }


    @Operation(summary = "获得忽略关键词列表")
    @GetMapping("/ignore")
    public R getIgnoreKeyWordSet() {
        return R.ok().setData(dictService.findBlackIgnoreKeyWord());
    }

    @Operation(summary = "获得忽略Tag列表")
    @GetMapping("/ignoreTag")
    public R getIgnoreTagSet() {
        return R.ok().setData(dictService.findBlackIgnoreTag());
    }

    @Operation(summary = "添加到忽略关键词列表")
    @PostMapping("/ignore")
    public R addIgnoreKeyWordSet(@RequestBody Set<String> ignoreKeyWordSet) {

        Set<String> collect = ignoreKeyWordSet.stream().filter(StrUtil::isNotBlank).map(String::trim).collect(Collectors.toSet());
        dictService.addBlackIgnoreKeyword(collect);

        return R.ok();
    }


}
