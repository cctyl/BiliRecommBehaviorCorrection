package io.github.cctyl.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.service.WhiteListRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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


    @Autowired
    private BiliApi biliApi;

    @Autowired
    private WhiteListRuleService whiteRuleService;




    @Operation(summary = "指定视频是否符合白名单")
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
        //白名单规则匹配
        boolean whitelistRuleMatch = whiteRuleService.isWhitelistRuleMatch(videoDetail);
        //up主id匹配
        boolean userIdMatch = whiteRuleService.isUserIdMatch(videoDetail);

        //分区id匹配
        boolean tidMatch = whiteRuleService.isTidMatch(videoDetail);


        HashMap<String, Object> map = new HashMap<>();
        map.put("videoDetail", videoDetail);
        map.put("whitelistRuleMatch", whitelistRuleMatch);
        map.put("userIdMatch", userIdMatch);
        map.put("tidMatch", tidMatch);
        map.put("thumbUpReason", videoDetail.getThumbUpReason());

        return R.data(map);

    }





    /**
     * 输入指定的视频训练白名单规则
     *
     * @param id             白名单条件id，为空表示创建新的规则
     * @param trainedAvidList 用于训练的视频avid列表，与mid二选一
     * @param mid            up主id，表示从该up主的投稿视频抽取进行训练，与trainedAvidList 二选一
     * @return 返回结果
     */
    @Operation(summary = "输入指定的视频训练白名单规则")
    @PostMapping("/train")
    public R addTrain(
            @Parameter(name = "id", description = "白名单条件id,为空表示创建新的规则") @RequestParam(required = false) String id,
            @Parameter(name = "trainedAvidList", description = "用于训练的视频avid列表，与mid二选一") @RequestParam(required = false) List<Integer> trainedAvidList,
            @Parameter(name = "mid", description = "up主id，表示从该up主的投稿视频抽取进行训练，与trainedAvidList 二选一") @RequestParam(required = false) String mid
    ) {

        if (CollUtil.isEmpty(trainedAvidList) && StrUtil.isBlank(mid)) {
            return R.error().setMessage("视频来源参数缺失");
        }

        TaskPool.putTask(() -> {
            whiteRuleService.addTrain(id, trainedAvidList, mid);
        });
        return R.ok().setMessage("训练任务已开始");
    }


    @Operation(summary = "添加或修改指定的白名单对象")
    @PostMapping("/")
    public R addOrUpdateWhiteRule(
            @Parameter(name = "toUpdate", description = "将要修改的白名单对象")
            @RequestBody WhiteListRule toUpdate
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
        toUpdate.setDescKeyWordList(whiteRuleService.filterIgnore(toUpdate.getDescKeyWordList()));
        toUpdate.setTagNameList(whiteRuleService.filterIgnore(toUpdate.getTagNameList()));
        toUpdate.setTitleKeyWordList(whiteRuleService.filterIgnore(toUpdate.getTitleKeyWordList()));
        toUpdate.setCoverKeyword(whiteRuleService.filterIgnore(toUpdate.getCoverKeyword()));

        GlobalVariables.INSTANCE.addOrUpdateWhitelitRule(toUpdate);
        return R.ok().setMessage("添加成功").setData(toUpdate);
    }


    @Operation(summary = "删除指定的白名单规则")
    @DeleteMapping("/{id}")
    public R delWhiteRule(
            @Parameter(name = "id", description = "需要删除的白名单的id")
            @PathVariable("id") String id
    ) {

        boolean result = GlobalVariables.INSTANCE.removeWhitelistRules(id);
        return R.ok().setMessage("操作完成").setData("删除结果："+result);
    }


    @Operation(summary = "获得忽略关键词列表")
    @GetMapping("/ignore")
    public R getIgnoreKeyWordSet() {

        Set<String> keyWordSet = GlobalVariables.getIgnoreWhiteKeyWordSet();
        return R.ok().setData(keyWordSet);
    }

    @Operation(summary = "添加到忽略关键词列表")
    @PostMapping("/ignore")
    public R addIgnoreKeyWordSet(@RequestBody Set<String> ignoreKeyWordSet) {
        GlobalVariables.INSTANCE.addWhiteIgnoreKeyword(ignoreKeyWordSet);
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

        WhiteListRule whiteListRule = whiteRuleService.getById(id);
        return R.data("data",whiteListRule);
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
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit) {

        IPage<WhiteListRule> iPage = whiteRuleService.pageSearch(new Page<>(page, limit));
        return R.data("list", iPage.getRecords());
    }


}
