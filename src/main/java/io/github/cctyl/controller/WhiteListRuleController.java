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
import io.github.cctyl.service.WhiteListRuleService;
import io.github.cctyl.entity.WhiteListRule;
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
@Tag(name = "whiteListRuleCRUD接口")
@RequestMapping("/cctyl/white-list-rule")
public class WhiteListRuleController {

    @Autowired
    private WhiteListRuleService whiteListRuleService;

    /**
    * 根据id查询
    *
    * @param id
    * @return
    */
    @Operation(summary = "获取whiteListRule根据id")
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") String id) {

        WhiteListRule whiteListRule = whiteListRuleService.getById(id);
        return R.ok().data("data",whiteListRule);
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

        Page<WhiteListRule> pageBean = new Page<>(page, limit);
        IPage<WhiteListRule> iPage = whiteListRuleService.page(pageBean, null);
        List<WhiteListRule> records = iPage.getRecords();
        return R.ok().data("list", records);
    }


    /**
    * 新增一个
    * @param whiteListRule
    * @param request
    * @return
    */
    @PostMapping("")
    public R add(
        @RequestBody WhiteListRule whiteListRule,
        HttpServletRequest request) {
        boolean result = whiteListRuleService.save(whiteListRule);
        return result ? R.ok() : R.error();
    }


    /**
    * 删除一个
    * @param whiteListRule 预留接口，用于后续根据条件删除
    * @param request
    * @return
    */
    @DeleteMapping("")
    public R del(@RequestBody WhiteListRule whiteListRule, HttpServletRequest request){
        boolean result = whiteListRuleService.removeById(whiteListRule.getId());
        return result ? R.ok() : R.error();
    }


    /**
    * 修改一个
    * @param  whiteListRule 预留接口，用于后续根据条件修改
    * @param request
    * @return
    */
    @PutMapping("")
    public R update(@RequestBody WhiteListRule whiteListRule,  HttpServletRequest request){
        boolean result =  whiteListRuleService.updateById(whiteListRule);
        return result ? R.ok() : R.error();
    }
}
