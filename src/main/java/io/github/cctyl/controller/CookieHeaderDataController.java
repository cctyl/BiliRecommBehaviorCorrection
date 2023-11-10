package io.github.cctyl.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.entity.CookieHeaderData;
import io.github.cctyl.pojo.R;
import io.github.cctyl.service.CookieHeaderDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* <p>
*  前端控制器
* </p>
*
* @author tyl
* @date 2023-11-10
*/
@RestController
@Tag(name = "cookieHeaderDataCRUD接口")
@RequestMapping("/cookie-header-data")
public class CookieHeaderDataController {

    @Autowired
    private CookieHeaderDataService cookieHeaderDataService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取cookieHeaderData根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        CookieHeaderData cookieHeaderData = cookieHeaderDataService.getById(id);
        return R.data("data",cookieHeaderData);
    }

    /**
    * 分页查询
    *
    * @param request 请求
    * @param page    页数
    * @param limit   每页限制
    * @return
    */
    @Operation(summary = "获取cookieHeaderData列表")
    @GetMapping("/list/{page}/{limit}")
    public R getList(
        HttpServletRequest request,
        @PathVariable("page") Long page,
        @PathVariable("limit") Long limit) {

        Page<CookieHeaderData> pageBean = new Page<>(page, limit);
        IPage<CookieHeaderData> iPage = cookieHeaderDataService.page(pageBean, null);
        List<CookieHeaderData> records = iPage.getRecords();
        return R.data("list", records);
    }


    /**
    * 新增一个
    * @param cookieHeaderData
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody CookieHeaderData cookieHeaderData,
        HttpServletRequest request) {
        boolean result = cookieHeaderDataService.save(cookieHeaderData);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param cookieHeaderData 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody CookieHeaderData cookieHeaderData, HttpServletRequest request){
        boolean result = cookieHeaderDataService.removeById(cookieHeaderData.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  cookieHeaderData 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody CookieHeaderData cookieHeaderData,  HttpServletRequest request){
        boolean result =  cookieHeaderDataService.updateById(cookieHeaderData);
        return result ? R.ok() : R.error();
    }
}
