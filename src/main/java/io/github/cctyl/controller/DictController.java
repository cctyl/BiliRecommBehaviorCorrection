package io.github.cctyl.controller;

import io.github.cctyl.pojo.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import io.github.cctyl.service.DictService;
import io.github.cctyl.entity.Dict;
import javax.servlet.http.HttpServletRequest;

/**
* <p>
*  前端控制器
* </p>
*
* @author tyl
* @date 2023-11-09
*/
@RestController
@Tag(name = "dictCRUD接口")
@RequestMapping("/cctyl/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取dict根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        Dict dict = dictService.getById(id);
        return R.ok().data("data",dict);
    }

    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取dict列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") Long page,
        @PathVariable("limit") Long limit) {

        Page<Dict> pageBean = new Page<>(page, limit);
        IPage<Dict> iPage = dictService.page(pageBean, null);
        List<Dict> records = iPage.getRecords();
        return R.ok().data("list", records);
    }


    /**
    * 新增一个
    * @param dict
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody Dict dict,
        HttpServletRequest request) {
        boolean result = dictService.save(dict);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param dict 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody Dict dict, HttpServletRequest request){
        boolean result = dictService.removeById(dict.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  dict 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody Dict dict,  HttpServletRequest request){
        boolean result =  dictService.updateById(dict);
        return result ? R.ok() : R.error();
    }
}