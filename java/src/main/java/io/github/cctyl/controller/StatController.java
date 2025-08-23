package io.github.cctyl.controller;

import io.github.cctyl.domain.dto.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import io.github.cctyl.service.StatService;
import io.github.cctyl.domain.po.Stat;
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
@Tag(name = "stat 接口")
@RequestMapping("/stat")
public class StatController {

    @Autowired
    private StatService statService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取stat根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        Stat stat = statService.getById(id);
        return R.data("data",stat);
    }

    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取stat列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") long page,
        @PathVariable("limit") long limit) {

        Page<Stat> pageBean = new Page<>(page, limit);
        IPage<Stat> iPage = statService.page(pageBean, null);
        List<Stat> records = iPage.getRecords();
        return R.data("list", records);
    }


    /**
    * 新增一个
    * @param stat
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody Stat stat,
        HttpServletRequest request) {
        boolean result = statService.save(stat);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param stat 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody Stat stat, HttpServletRequest request){
        boolean result = statService.removeById(stat.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  stat 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody Stat stat,  HttpServletRequest request){
        boolean result =  statService.updateById(stat);
        return result ? R.ok() : R.error();
    }
}
