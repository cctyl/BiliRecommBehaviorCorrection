package io.github.cctyl.controller;


import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.pojo.R;
import io.github.cctyl.service.ConfigService;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.utils.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import static io.github.cctyl.pojo.constants.AppConstant.STOP_WORDS_KEY;


@RestController
@RequestMapping("/config")
@Tag(name = "配置模块")
public class ConfigController {


    @Autowired
    private HarAnalysisTool harAnalysisTool;

    @Autowired
    private ConfigService configService;

    @Autowired
    private CookieHeaderDataService cookieHeaderDataService;

    @PutMapping("/cookie")
    @Operation(summary = "更新cookie")
    public R updateCookie(
            @RequestParam String cookieStr
    ) {
        if (StrUtil.isBlank(cookieStr)) {
            return R.error().setMessage("错误的数据");
        }
        Map<String, String> cookieMap = DataUtil.splitCookie(cookieStr);
        GlobalVariables.updateRefreshCookie(cookieMap);
        return R.ok().setData(cookieMap);
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
