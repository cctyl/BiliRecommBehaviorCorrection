package io.github.cctyl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.domain.po.VideoTag;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.service.VideoTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
* <p>
*  前端控制器
* </p>
*
* @author tyl
* @date 2023-11-17
*/
@RestController
@Tag(name = "videoTagCRUD接口")
@RequestMapping("/cctyl/video-tag")
public class VideoTagController {

    @Autowired
    private VideoTagService videoTagService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取videoTag根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        VideoTag videoTag = videoTagService.getById(id);
        return R.data("data",videoTag);
    }

    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取videoTag列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") long page,
        @PathVariable("limit") long limit) {

        Page<VideoTag> pageBean = new Page<>(page, limit);
        IPage<VideoTag> iPage = videoTagService.page(pageBean, null);
        List<VideoTag> records = iPage.getRecords();
        return R.data("list", records);
    }


    /**
    * 新增一个
    * @param videoTag
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody VideoTag videoTag,
        HttpServletRequest request) {
        boolean result = videoTagService.save(videoTag);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param videoTag 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody VideoTag videoTag, HttpServletRequest request){
        boolean result = videoTagService.removeById(videoTag.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  videoTag 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody VideoTag videoTag,  HttpServletRequest request){
        boolean result =  videoTagService.updateById(videoTag);
        return result ? R.ok() : R.error();
    }
}
