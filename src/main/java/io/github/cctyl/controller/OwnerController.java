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
import io.github.cctyl.service.OwnerService;

import io.github.cctyl.domain.po.Owner;
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
@Tag(name = "ownerCRUD接口")
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取owner根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        Owner owner = ownerService.getById(id);
        return R.data("data",owner);
    }

    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取owner列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") Long page,
        @PathVariable("limit") Long limit) {

        Page<Owner> pageBean = new Page<>(page, limit);
        IPage<Owner> iPage = ownerService.page(pageBean, null);
        List<Owner> records = iPage.getRecords();
        return R.data("list", records);
    }


    /**
    * 新增一个
    * @param owner
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody Owner owner,
        HttpServletRequest request) {
        boolean result = ownerService.save(owner);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param owner 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody Owner owner, HttpServletRequest request){
        boolean result = ownerService.removeById(owner.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  owner 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody Owner owner,  HttpServletRequest request){
        boolean result =  ownerService.updateById(owner);
        return result ? R.ok() : R.error();
    }
}
