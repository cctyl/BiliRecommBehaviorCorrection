package io.github.cctyl.controller;

import bilibili.app.playeronline.v1.PlayerOnlineGrpc;
import bilibili.app.view.v1.ViewGrpc;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.WhiteListRuleAddUpdateDto;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.service.DictService;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.service.WhiteListRuleService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ReflectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * 白名单规则模块
 */
@RestController
@RequestMapping("/white-rule")
@Tag(name = "白名单规则模块")
@Slf4j
public class WhiteRuleController {
    @GrpcClient("failover")
    private PlayerOnlineGrpc.PlayerOnlineBlockingStub playerOnlineBlockingStub;

    @GrpcClient("failover")
    private ViewGrpc.ViewBlockingStub viewBlockingStub;

    @Autowired
    private BiliApi biliApi;

    @Autowired
    private WhiteListRuleService whiteRuleService;

    @Autowired
    private DictService dictService;
    @Autowired
    private TaskService taskService;




    /**
     * 输入指定的视频训练已存在的白名单规则或根据训练结果创建白名单规则
     *
     * @param id             白名单条件id，为空表示创建新的规则
     * @param trainedBvidList 用于训练的视频bvid列表，与mid二选一
     * @param mid            up主id，表示从该up主的投稿视频抽取进行训练，与trainedAvidList 二选一
     * @return 返回结果
     */
    @Operation(summary = "输入指定的视频训练白名单规则")
    @PostMapping("/train")
    public R addTrain(
            @Parameter(name = "id", description = "白名单条件id,为空表示创建新的规则") @RequestParam(required = false) String id,
            @Parameter(name = "trainedBvidList", description = "用于训练的视频bvid列表，与mid二选一") @RequestBody(required = false) List<String> trainedBvidList,
            @Parameter(name = "mid", description = "up主id，表示从该up主的投稿视频抽取进行训练，与trainedAvidList 二选一") @RequestParam(required = false) String mid
    ) {

        if (CollUtil.isEmpty(trainedBvidList) && StrUtil.isBlank(mid)) {
            return R.error().setMessage("视频来源参数缺失");
        }
        List<Long> trainedAvidList = List.of();
        if (CollUtil.isNotEmpty(trainedBvidList)) {
            trainedAvidList = trainedBvidList.stream().map(DataUtil::bvidToAid).toList();
        }

        List<Long> finalTrainedAvidList = trainedAvidList;
        boolean b = taskService.doTask(ReflectUtil.getCurrentMethodPath(), () -> {
            whiteRuleService.addTrain(id, finalTrainedAvidList, mid);
        });
        if (b) {
            return R.ok().setMessage("训练任务已开始");
        } else {
            return R.error().setMessage("该任务正在被运行中，请等待上一个任务结束");
        }
    }


    @Operation(summary = "添加或修改指定的白名单对象")
    @PostMapping("")
    public R addOrUpdateWhiteRule(
            @Parameter(name = "toUpdate", description = "将要修改的白名单对象")
            @RequestBody WhiteListRuleAddUpdateDto toUpdate
    ) {
        if (
                CollUtil.isEmpty(toUpdate.getDescKeyWordList())
                        &&
                        CollUtil.isEmpty(toUpdate.getTagNameList())
                        &&
                        CollUtil.isEmpty(toUpdate.getTitleKeyWordList())

                        && CollUtil.isEmpty(toUpdate.getCoverKeyword())
        ) {
            return R.error().setMessage("无效数据");
        }

        //添加之前，过滤掉需要忽略的关键词
//        toUpdate.setDescKeyWordList(whiteRuleService.filterIgnoreValue(toUpdate.getDescKeyWordList()));
//        toUpdate.setTagNameList(whiteRuleService.filterIgnoreValue(toUpdate.getTagNameList()));
//        toUpdate.setTitleKeyWordList(whiteRuleService.filterIgnoreValue(toUpdate.getTitleKeyWordList()));
//        toUpdate.setCoverKeyword(whiteRuleService.filterIgnoreValue(toUpdate.getCoverKeyword()));

        WhiteListRule whiteListRule = whiteRuleService.addOrUpdateWhitelitRule(toUpdate.transform());
        return R.ok().setMessage("添加成功").setData(whiteListRule);
    }


    @Operation(summary = "添加搜索关键词")
    @PostMapping("/search")
    public R addSearchKeyword(@RequestParam List<String> newSearchKeyword) {
        dictService.addSearchKeyword(newSearchKeyword);
        return R.ok();
    }


    @Operation(summary = "删除指定的白名单规则")
    @DeleteMapping("/{id}")
    public R delWhiteRule(
            @Parameter(name = "id", description = "需要删除的白名单的id")
            @PathVariable("id") String id
    ) {

        boolean result = whiteRuleService.removeWhitelistRules(id);
        return R.ok().setMessage("操作完成").setData("删除结果：" + result);
    }


    @Operation(summary = "获得忽略关键词列表")
    @GetMapping("/ignore")
    public R getIgnoreKeyWordSet() {

        Set<String> keyWordSet = dictService.getIgnoreWhiteKeyWordSet();
        return R.ok().setData(keyWordSet);
    }

    @Operation(summary = "添加到忽略关键词列表")
    @PostMapping("/ignore")
    public R addIgnoreKeyWordSet(@RequestBody Set<String> ignoreKeyWordSet) {
        whiteRuleService.addWhiteIgnoreKeyword(ignoreKeyWordSet);
        return R.ok();
    }


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Operation(summary = "获取whiteListRule根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        WhiteListRule whiteListRule = whiteRuleService.findWithDetailById(id);
        return R.data(whiteListRule);
    }

    /**
     * 分页查询
     *
     * @param request 请求
     * @param page    页数
     * @param limit   每页限制
     * @return
     */
    @Operation(summary = "获取whiteListRule列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
            HttpServletRequest request,
            @PathVariable("page") long page,
            @PathVariable("limit") long limit) {

        IPage<WhiteListRuleAddUpdateDto> iPage = whiteRuleService.pageSearch(new Page<>(page, limit));
        return R.data("list", iPage.getRecords());
    }


    @Operation(summary = "对该用户的所有视频均进行点赞")
    @PostMapping("/thumb-up-all/{mid}")
    public R thumbUpUserAllVideo(
            @PathVariable("mid") String mid,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "keyword", defaultValue = "") String keyword
    ) {
        boolean b = taskService.doTask(ReflectUtil.getCurrentMethodPath(), () -> {
            whiteRuleService.thumbUpUserAllVideo(mid, page, keyword);
        });
        if (b) {
            return R.ok().setMessage("点赞任务已开始");
        } else {
            return R.error().setMessage("该任务正在被运行中，请等待上一个任务结束");
        }


    }

}
