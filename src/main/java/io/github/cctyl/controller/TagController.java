package io.github.cctyl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
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
@io.swagger.v3.oas.annotations.tags.Tag(name = "tagCRUD接口")
@RequestMapping("/cctyl/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取tag根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        Tag tag = tagService.getById(id);
        return R.data("data",tag);
    }

    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取tag列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") long page,
        @PathVariable("limit") long limit) {

        Page<Tag> pageBean = new Page<>(page, limit);
        IPage<Tag> iPage = tagService.page(pageBean, null);
        List<Tag> records = iPage.getRecords();
        return R.data("list", records);
    }


    /**
    * 新增一个
    * @param tag
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody Tag tag,
        HttpServletRequest request) {
        boolean result = tagService.save(tag);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param tag 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody Tag tag, HttpServletRequest request){
        boolean result = tagService.removeById(tag.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  tag 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody Tag tag,  HttpServletRequest request){
        boolean result =  tagService.updateById(tag);
        return result ? R.ok() : R.error();
    }
}
