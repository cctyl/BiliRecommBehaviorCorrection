package io.github.cctyl.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.entity.R;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.service.BiliService;
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

@RestController
@RequestMapping("/black-rule")
@Api(tags = "黑名单规则模块")
@Slf4j
public class BlackRuleController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BiliService biliService;

    @Autowired
    private BiliApi biliApi;

    @ApiOperation("指定视频是否符合黑名单")
    @GetMapping("/check-video")
    public R checkVideo(
            @ApiParam(name = "aid", value = "avid")
            @RequestParam(required = false) Integer aid,
            @ApiParam(name = "bvid", value = "bvid")
            @RequestParam(required = false) String bvid
    ) {

        VideoDetail videoDetail;
        if (aid!=null){

            videoDetail = biliApi.getVideoDetail(aid);
        }else if (StrUtil.isNotBlank(bvid)){
            videoDetail = biliApi.getVideoDetail(bvid);
        }else {
            return R.error().setMessage("参数缺失");
        }
        boolean titleMatch = biliService.isTitleMatch(GlobalVariables.blackKeywordTree, videoDetail);
        //1.2 简介是否触发黑名单关键词
        boolean descMatch = biliService.isDescMatch(GlobalVariables.blackKeywordTree, videoDetail);
        //1.3 标签是否触发关键词,需要先获取标签
        boolean tagMatch = biliService.isTagMatch(videoDetail);
        //1.4 up主id是否在黑名单内
        boolean midMatch = biliService.isMidMatch(GlobalVariables.blackUserIdSet, videoDetail);
        //1.5 分区是否触发
        boolean tidMatch = biliService.isTidMatch(GlobalVariables.blackTidSet, videoDetail);
        //1.6 封面是否触发
        boolean coverMatch = biliService.isCoverMatch(videoDetail);


        HashMap<String, Object> map = new HashMap<>();
        map.put("videoDetail",videoDetail);
        map.put("titleMatch",titleMatch);
        map.put("descMatch",descMatch);
        map.put("tagMatch",tagMatch);
        map.put("midMatch",midMatch);
        map.put("tidMatch",tidMatch);
        map.put("coverMatch",coverMatch);

        return R.data(map);

    }

    @ApiOperation("对指定分区的 排行榜、热门视频进行点踩")
    @PostMapping("/disklike-by-tid")
    public R dislikeByTid(
            @ApiParam(name = "tidList", value = "需要点踩的分区id")
            @RequestParam List<Integer> tidList
    ) {


        TaskPool.putTask(() -> {
            int disklikeNum = 0;
            for (Integer tid : tidList) {
                try {
                    disklikeNum += biliService.dislikeByTid(tid);
                    log.info("完成对{}分区的点踩任务", tid);
                    ThreadUtil.sleep5Second();
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

    @ApiOperation("对指定用户的视频进行点踩")
    @PostMapping("/disklike-by-uid")
    public R dislikeByUserId(
            @ApiParam(name = "userIdList", value = "二选一，需要点踩的用户id")
            @RequestParam List<String> userIdList
    ) {
        TaskPool.putTask(() -> {
            int disklikeNum = 0;
            for (String userId : userIdList) {
                try {
                    disklikeNum += biliService.dislikeByUserId(userId);
                    log.info("完成对{}分区的点踩任务", userId);
                    ThreadUtil.sleep20Second();
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
        return R.ok().setMessage("对指定分区点踩任务已开始");
    }



    @ApiOperation("获得缓存的训练结果")
    @GetMapping("/cache-train-result")
    public R getCacheTrainResult(){
        Set<String> keywordSet = redisUtil.sMembers(BLACK_KEYWORD_CACHE).stream().map(Object::toString)
                .collect(Collectors.toSet());
        Set<String> tagNameSet = redisUtil.sMembers(BLACK_TAG_NAME_CACHE).stream().map(Object::toString)
                .collect(Collectors.toSet());

        return R.data(Map.of(
                "keywordSet",keywordSet,
                "tagNameSet",tagNameSet
        ));
    }


    @ApiOperation("将缓存的结果存入")
    @PutMapping("/cache-train-result")
    public R getCacheTrainResult(
            @RequestBody Map<String,Set<String>> map
    ) {
        Set<String> keywordSet = map.getOrDefault("keywordSet", Collections.emptySet());
        Set<String> tagNameSet = map.getOrDefault("tagNameSet", Collections.emptySet());

        GlobalVariables.blackKeywordSet.addAll(keywordSet);
        GlobalVariables.setBlackKeywordSet(GlobalVariables.blackKeywordSet);
        GlobalVariables.blackTagSet.addAll(tagNameSet);
        GlobalVariables.setBlackTagSet(GlobalVariables.blackTagSet);

        //清空缓存结果
        redisUtil.delete(BLACK_KEYWORD_CACHE);
        redisUtil.delete(BLACK_TAG_NAME_CACHE);

        return R.data(Map.of(
                "keywordSet", GlobalVariables.blackKeywordSet,
                "tagNameSet", GlobalVariables.blackTagSet
        ));
    }

    @ApiOperation("获得黑名单分区id")
    @GetMapping("/tid")
    public R getBlackTidSet(){
        return R.ok().setData(GlobalVariables.blackTidSet);
    }

    @ApiOperation("更新黑名单分区id")
    @PutMapping("/tid")
    public R updateBlackTidSet(@RequestBody Set<String> blackTidSet ){
        GlobalVariables.setBlackTidSet(blackTidSet);
        return R.ok().setData(GlobalVariables.blackTidSet);
    }



    @ApiOperation("获得黑名单关键词列表")
    @GetMapping("/keyword")
    public R getBlackKeywordSet(){
        return R.ok().setData(GlobalVariables.blackKeywordSet);
    }


    @ApiOperation("添加/更新黑名单关键词")
    @PostMapping("/keyword")
    public R addOrUpdateBlackKeyWord(@RequestBody List<String> keywordList,
                                     @RequestParam Boolean add
                                     ){
        Set<String> ignoreKeyWordSet = biliService.getIgnoreKeyWordSet();
        Set<String> collect = keywordList.stream().filter(StrUtil::isNotBlank)
                //移除忽略关键词
                .filter(s -> !ignoreKeyWordSet.contains(s))
                .collect(Collectors.toSet());
        if (Boolean.TRUE.equals(add)){
            GlobalVariables.addBlackKeyword(collect);
        }else {
            GlobalVariables.setBlackKeywordSet(collect);
            GlobalVariables.blackKeywordTree = new WordTree();
        }
        GlobalVariables.blackKeywordTree.addWords(collect);
        return R.ok().setData(GlobalVariables.blackKeywordSet);
    }





    @ApiOperation("获得黑名单用户id列表")
    @GetMapping("/user-id")
    public R getBlackUserIdSet(){
        return R.ok().setData(GlobalVariables.blackUserIdSet);
    }

    @ApiOperation("更新黑名单用户id列表")
    @PutMapping("/user-id")
    public R updateBlackUserIdSet(@RequestBody Set<String> blackUserIdSet ){
        GlobalVariables.setBlackUserIdSet(blackUserIdSet);
        return R.ok().setData(GlobalVariables.blackUserIdSet);
    }



    @ApiOperation("获得黑名单分区列表")
    @GetMapping("/tag")
    public R getBlackTagSet(){
        return R.ok().setData(GlobalVariables.blackTagSet);
    }

    @ApiOperation("更新黑名单分区列表")
    @PutMapping("/tag")
    public R updateBlackTagSet(@RequestBody Set<String> blackTagSet) {
        Set<String> ignoreKeyWordSet = biliService.getIgnoreKeyWordSet();
        Set<String> collect = blackTagSet.stream().filter(StrUtil::isNotBlank)
                //移除忽略关键词
                .filter(s -> !ignoreKeyWordSet.contains(s))
                .collect(Collectors.toSet());
        GlobalVariables.setBlackTagSet(collect);
        GlobalVariables.blackTagTree = new WordTree();
        GlobalVariables.blackTagTree.addWords(blackTagSet);
        return R.ok().setData(GlobalVariables.blackTagSet);
    }


    @ApiOperation("获得忽略关键词列表")
    @GetMapping("/ignore")
    public R getIgnoreKeyWordSet(){
        return R.ok().setData(redisUtil.sMembers(IGNORE_BLACK_KEYWORD));
    }

    @ApiOperation("添加到忽略关键词列表")
    @PostMapping("/ignore")
    public R addIgnoreKeyWordSet(@RequestBody Set<String> ignoreKeyWordSet ){
        redisUtil.sAdd(IGNORE_BLACK_KEYWORD,ignoreKeyWordSet.toArray());
        return R.ok().setData(redisUtil.sMembers(IGNORE_BLACK_KEYWORD));
    }
}
