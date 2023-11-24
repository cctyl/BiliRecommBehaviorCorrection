package io.github.cctyl.controller;


import cn.hutool.core.util.StrUtil;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.service.ConfigService;
import io.github.cctyl.utils.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/config")
@Tag(name = "配置模块")
public class ConfigController {



    @Autowired
    private ConfigService configService;



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





}
