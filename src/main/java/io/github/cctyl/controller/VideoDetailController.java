package io.github.cctyl.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.domain.dto.CheckResult;
import io.github.cctyl.domain.dto.FirstProcessData;
import io.github.cctyl.domain.dto.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.domain.dto.VideoCheckDetail;
import io.github.cctyl.domain.po.WatchedVideo;
import io.github.cctyl.mapper.WatchedVideoMapper;
import io.github.cctyl.service.WhiteListRuleService;
import io.github.cctyl.service.impl.BiliService;
import io.github.cctyl.service.impl.BlackRuleService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.domain.po.VideoDetail;
import jakarta.servlet.http.HttpServletRequest;

/**
* <p>
*  前端控制器
* </p>
*
* @author tyl
* @date 2023-11-09
*/
@RestController
@Tag(name = "视频详情 接口")
@RequestMapping("/video-detail")
public class VideoDetailController {

    @Autowired
    private VideoDetailService videoDetailService;

    @Autowired
    private WatchedVideoMapper watchedVideoMapper;

    @Autowired
    private BiliService biliService;

    @Autowired
    private WhiteListRuleService whiteListRuleService;

    @Autowired
    private BlackRuleService blackRuleService;

    @Autowired
    private BiliApi biliApi;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取videoDetail根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        VideoDetail videoDetail = videoDetailService.getById(id);
        return R.data("data",videoDetail);
    }

    @Operation(summary = "已观看视频")
    @PostMapping("/watch/{aid}")
    public R getById(@PathVariable("aid") Long aid) {
        int insert = watchedVideoMapper.insert(new WatchedVideo().setAid(aid));
        return R.data("data",insert);
    }


    @Operation(summary = "指定视频是否符合白名单")
    @GetMapping("/check-video")
    public R checkVideo(
            @Parameter(name = "aid", description = "avid")
            @RequestParam(required = false) Long aid,
            @Parameter(name = "bvid", description = "bvid")
            @RequestParam(required = false) String bvid
    ) {

        VideoDetail videoDetail ;
        if (aid != null) {
            videoDetail = videoDetailService.findWithDetailByAid(aid);
            if (videoDetail==null){
                videoDetail = biliApi.getVideoDetail(aid);
            }
        } else if (StrUtil.isNotBlank(bvid)) {

            videoDetail = videoDetailService.findWithDetailByBvid(bvid);

            if (videoDetail==null) {
                videoDetail = biliApi.getVideoDetail(bvid);
            }
        } else {
            return R.error().setMessage("参数缺失");
        }
        FirstProcessData result = biliService. getResult();

        CheckResult whiteResult = whiteListRuleService.whiteMatch(
                videoDetail,
                result.whitelistRuleList(),
                result.whiteUserIdSet(),
                result.whiteTidSet(),
                result.whiteTitleKeywordTree(),
                result.whiteDescKeywordTree()
        );

        CheckResult blackResult = blackRuleService.blackMatch(
                videoDetail,
                result.blackTagSet(),
                result.blackKeywordTree(),
                result.blackTidSet(),
                result.blackUserIdSet()
        );


        return R.data(  new VideoCheckDetail(
                        whiteResult,
                        blackResult,
                        videoDetail.getThumbUpReason(),
                        videoDetail.getBlackReason(),
                        videoDetail
                )
        );

    }


    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取videoDetail列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") long page,
        @PathVariable("limit") long limit) {

        Page<VideoDetail> pageBean = new Page<>(page, limit);
        IPage<VideoDetail> iPage = videoDetailService.page(pageBean, null);
        List<VideoDetail> records = iPage.getRecords();
        return R.data("list", records);
    }


    /**
    * 新增一个
    * @param videoDetail
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody VideoDetail videoDetail,
        HttpServletRequest request) {
        boolean result = videoDetailService.save(videoDetail);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param videoDetail 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody VideoDetail videoDetail, HttpServletRequest request){
        boolean result = videoDetailService.removeById(videoDetail.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  videoDetail 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody VideoDetail videoDetail,  HttpServletRequest request){
        boolean result =  videoDetailService.updateById(videoDetail);
        return result ? R.ok() : R.error();
    }
}
