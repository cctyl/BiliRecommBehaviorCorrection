package io.github.cctyl.controller;

import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.DictType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Set;

import io.github.cctyl.service.DictService;
import io.github.cctyl.domain.po.Dict;
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
@Tag(name = "字典接口")
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @Autowired
    private BiliApi biliApi;

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
        return R.data("data",dict);
    }

    @Operation(summary = "根据mid获取用户名")
    @GetMapping("/getUserNameByMid/{mid}")
    public R getUserNameByMid(@PathVariable("mid") String mid) {
        return R.data(biliApi.onlyGetUserNameByMid(mid));
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
        @PathVariable("page") long page,
        @PathVariable("limit") long limit) {

        Page<Dict> pageBean = new Page<>(page, limit);
        IPage<Dict> iPage = dictService.page(pageBean, null);
        List<Dict> records = iPage.getRecords();
        return R.data("list", records);
    }
    @Operation(summary = "根据DictType 和 AccessType 查询dict列表")
    @GetMapping("/list")
    public R getListByDictTypeAndAccessType(
            HttpServletRequest request,
            @RequestParam DictType dictType,
            @RequestParam AccessType accessType
            ) {

        return R.data("list", dictService.getListByDictTypeAndAccessType(dictType,accessType));
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
    */
    @DeleteMapping("/{id}")
    public R del(@PathVariable("id") String id, HttpServletRequest request){
        boolean result = dictService.removeById(id);
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


    @Operation(summary = "添加停顿词")
    @PostMapping("/add-stopword")
    public R addStopWord(List<String> stopWordList) {
        GlobalVariables.addStopWords(stopWordList);
        return R.ok().setMessage("停顿词列表长度为:" + GlobalVariables.getStopWordList().size());
    }


    @Operation(summary = "批量删除后新增词典")
    @PostMapping("/batchRemoveAndUpdate")
    public R batchRemoveAndUpdate(
            @RequestParam DictType dictType,
            @RequestParam AccessType accessType,
            @RequestBody List<Dict> dictSet

            ) {


        return R.data(dictService.batchRemoveAndUpdate(dictType,accessType,dictSet));
    }

}
