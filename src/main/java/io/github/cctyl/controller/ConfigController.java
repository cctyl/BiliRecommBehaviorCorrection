package io.github.cctyl.controller;


import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/config")
@Tag(name = "配置模块")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;



    @PutMapping("/refresh-cookie")
    @Operation(summary = "更新 及时更新的cookie")
    public R updateCookie(
            @RequestParam String cookieStr
    ) {
        if (StrUtil.isBlank(cookieStr)) {
            return R.error().setMessage("错误的数据");
        }
        return R.ok().setData(configService.updateRefreshCookie(cookieStr));
    }

    @GetMapping("/refresh-cookie")
    @Operation(summary = "获取 及时更新的cookie")
    public R getRefreshCookie(
    ) {

        Map<String, String> refreshCookieMap = GlobalVariables.getRefreshCookieMap();
        return R.data(refreshCookieMap);
    }

    @PutMapping("/standard")
    @Operation(summary = "更新基本配置信息")
    public R updateStandardConfigInfo(@RequestBody ConfigDTO configDTO){
        ConfigVo configVo = configService.updateStandardConfigInfo(configDTO);
        return R.data(configVo);
    }

    @GetMapping("/standard")
    @Operation(summary = "查询基本配置信息")
    public R getStandardConfigInfo(){
        ConfigVo configVo = configService.getStandardConfigInfo();
        return R.data(configVo);
    }

    @PostMapping("/migration-from-redis")
    @Operation(summary = "从redis迁移数据")
    public R migrationFromRedis(){
         configService.migrationFromRedis();
        return R.ok();
    }


    @GetMapping("/qr-code")
    @Operation(summary = "申请二维码")
    public R getQrCode() {

        String url = configService.getQrCodeUrl();
        return R.data(url);
    }


    @GetMapping("/scan-result")
    @Operation(summary = "获取扫码结果")
    public R getQrCodeScanResult() {

        return R.data(configService.getQrCodeScanResult());
    }
}
