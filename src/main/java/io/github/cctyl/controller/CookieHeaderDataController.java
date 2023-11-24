package io.github.cctyl.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.po.CookieHeaderData;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.utils.HarAnalysisTool;
import io.github.cctyl.utils.IdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
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


    @Autowired
    private HarAnalysisTool harAnalysisTool;

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


    @PostMapping("/load-har")
    @Operation(summary = "上传har，更新cookie 和 header")
    public R loadHar(MultipartFile multipartFile, @RequestParam Boolean refresh) throws IOException {

        //保存到临时文件夹
        File tempDir = new File(new ApplicationHome().getDir().getParentFile().getPath(), "upload");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        LocalDateTime now = LocalDateTime.now();
        String fileName = IdGenerator.nextId() + "-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth() + ".har";
        File harFile = new File(tempDir, fileName);
        multipartFile.transferTo(harFile);
        harAnalysisTool.load(harFile, refresh);

        return R.ok().setData(GlobalVariables.getApiHeaderMap());
    }

}
