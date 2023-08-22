package io.github.cctyl.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.entity.*;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.utils.IdGenerator;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import io.github.classgraph.utils.WhiteBlackList;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;


/**
 * 白名单规则模块
 */
@RestController
@RequestMapping("/white-rule")
@Api(tags = "白名单规则模块")
@Slf4j
public class WhiteRuleController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BiliService biliService;

    @Autowired
    private BiliApi biliApi;


    @GetMapping("/list")
    @ApiOperation(value = "查询所有的白名单规则")
    public R getAllWhiteRule() {
        return R.data(redisUtil.sMembers(WHITE_LIST_RULE_KEY));
    }


    /**
     * @param id
     * @param trainedAvidList
     * @param mid
     * @return
     */
    @ApiOperation(value = "输入指定的视频训练白名单规则")
    @PostMapping("/train")
    public R addTrain(
            @ApiParam(name = "id", value = "白名单条件id,为空表示创建新的规则") @RequestParam(required = false) Long id,
            @ApiParam(name = "trainedAvidList", value = "用于训练的视频avid列表，与mid二选一") @RequestParam(required = false) List<Integer> trainedAvidList,
            @ApiParam(name = "mid", value = "up主id，表示从该up主的投稿视频抽取进行训练，与trainedAvidList 二选一") @RequestParam(required = false) String mid
    ) {

        if (CollUtil.isEmpty(trainedAvidList) && StrUtil.isBlank(mid)) {
            return R.error().setMessage("视频来源参数缺失");
        }

        TaskPool.putTask(() -> {
            log.info("开始训练");
            List<WhitelistRule> whitelistRuleList = redisUtil.sMembers(WHITE_LIST_RULE_KEY).stream().map(WhitelistRule.class::cast).collect(Collectors.toList());
            WhitelistRule whitelistRule;
            if (id == null) {
                //创建新规则
                whitelistRule = new WhitelistRule();
            } else {
                //从redis中找
                whitelistRule =
                        whitelistRuleList.stream()
                                .filter(w -> id.equals(w.getId()))
                                .findFirst()
                                .orElse(new WhitelistRule());
            }

            if (CollUtil.isNotEmpty(trainedAvidList)) {
                log.info("根据视频id进行训练");
                //从给定的视频列表进行训练
                whitelistRule = biliService.train(
                        whitelistRule,
                        trainedAvidList
                );

            } else if (StrUtil.isNotBlank(mid)) {
                //从给定的up主的投稿视频进行训练
                log.info("根据up主id进行训练");
                List<UserSubmissionVideo> allVideo = new ArrayList<>();
                PageBean<UserSubmissionVideo> pageBean = biliApi.searchUserSubmissionVideo(mid, 1, "");
                allVideo.addAll(pageBean.getData());
                while (pageBean.hasMore()) {
                    try {
                        ThreadUtil.sleep10Second();
                        pageBean = biliApi.searchUserSubmissionVideo(mid, pageBean.getPageNum() + 1, "");
                        allVideo.addAll(pageBean
                                .getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                whitelistRule = biliService.train(
                        whitelistRule,
                        allVideo.stream().map(UserSubmissionVideo::getAid).collect(Collectors.toList())
                );
            }
            log.info("训练完成，训练结果为:" + whitelistRule);
            whitelistRuleList.remove(whitelistRule);
            whitelistRuleList.add(whitelistRule);
            redisUtil.delete(WHITE_LIST_RULE_KEY);
            redisUtil.sAdd(WHITE_LIST_RULE_KEY, whitelistRuleList.toArray());
        });
        return R.ok().setMessage("训练任务已开始");
    }



    @ApiOperation(value = "添加或修改指定的白名单对象")
    @PostMapping("/")
    public R addOrUpdateWhiteRule(
            @ApiParam(name = "toUpdate", value = "将要修改的白名单对象")
            @RequestBody WhitelistRule toUpdate
    ) {


        if (
                CollUtil.isEmpty(toUpdate.getDescKeyWordList())
                        &&
                        CollUtil.isEmpty(toUpdate.getTagNameList())
                        &&
                        CollUtil.isEmpty(toUpdate.getTitleKeyWordList())

                        &&
                        StrUtil.isEmpty(toUpdate.getCoverKeyword())
        ) {

            return R.error().setMessage("无效数据");

        }

        if (toUpdate.getId() == null) {
            //此时创建一个新的id
            toUpdate.setId(IdGenerator.nextId());
        }
        List<WhitelistRule> whitelistRuleList =
                redisUtil.sMembers(WHITE_LIST_RULE_KEY)
                        .stream()
                        .map(WhitelistRule.class::cast)
                        .collect(Collectors.toList());
        whitelistRuleList.remove(toUpdate);
        whitelistRuleList.add(toUpdate);
        redisUtil.delete(WHITE_LIST_RULE_KEY);
        redisUtil.sAdd(WHITE_LIST_RULE_KEY, whitelistRuleList.toArray());
        return R.ok().setMessage("添加成功").setData(toUpdate);
    }


    @ApiOperation(value = "删除指定的白名单规则")
    @DeleteMapping("/{id}")
    public R delWhiteRule(
            @ApiParam(name = "id", value = "需要删除的白名单的id")
            @PathVariable("id") Long id
    ) {
        List<WhitelistRule> whitelistRuleList =
                redisUtil.sMembers(WHITE_LIST_RULE_KEY)
                        .stream()
                        .map(WhitelistRule.class::cast)
                        .collect(Collectors.toList());
        WhitelistRule toDel = whitelistRuleList.stream().filter(whitelistRule -> whitelistRule.getId().equals(id))
                .findAny()
                .orElse(null);

        if (toDel == null) {
            return R.error().setMessage("id=" + id + "的白名单规则不存在");
        }
        whitelistRuleList.remove(toDel);
        redisUtil.delete(WHITE_LIST_RULE_KEY);
        redisUtil.sAdd(WHITE_LIST_RULE_KEY, whitelistRuleList.toArray());
        return R.ok().setMessage("删除成功");
    }


}
