package io.github.cctyl.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.po.VideoReply;
import io.github.cctyl.service.DictService;
import io.github.cctyl.service.ReplyService;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ReflectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Tag(name = "评论相关接口")
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final TaskService taskService;

    @Operation(summary = "保存指定视频的评论")
    @PostMapping("/save-reply")
    public R saveVideoReplay(
            @RequestParam(required = false) String bvid,
            @RequestParam(required = false) Long avid
    ){
        if (StrUtil.isNotBlank(bvid)){
            avid = DataUtil.bvidToAid(bvid);
        }
        if (avid==null){
            return R.error().setMessage("bvid / avid不能为空");
        }

        Long finalAvid = avid;
        boolean b = taskService.doTask(ReflectUtil.getCurrentMethodPath(), () -> {
            replyService.saveReply(finalAvid);
        });
        if (b){
            return R.ok().setMessage("任务已开始");
        }else {
            return R.error().setMessage("该任务正在被运行中，请等待上一个任务结束");
        }



    }


    /**
     * 获取指定视频的评论
     * @param bvid
     * @param avid
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/video-reply-list/{page}/{limit}")
    @Operation(summary = "获取指定视频的评论")
    public R getVideoReplyListByVideoId(
            @RequestParam(required = false) String bvid,
            @RequestParam(required = false) Long avid,
            @PathVariable("page") long page,
            @PathVariable("limit") long limit
    ){

        if (StrUtil.isNotBlank(bvid)){
            avid = DataUtil.bvidToAid(bvid);
        }

        if (avid==null){
            return R.error().setMessage("bvid / avid不能为空");
        }

        Page<VideoReply> result = replyService.getReplyByVideoId(avid,page,limit);
        return R.data(result);
    }

}
