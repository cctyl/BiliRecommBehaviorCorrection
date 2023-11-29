package io.github.cctyl.controller;


import cn.hutool.core.util.StrUtil;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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




    @GetMapping("/web-qr-code")
    @Operation(summary = "申请web登陆二维码")
    public R getWebQrCode() {

        String url = configService.getWebLoginQrCode();
        return R.data(url);
    }


    @GetMapping("/web-scan-result")
    @Operation(summary = "获取web登陆扫码结果")
    public R getWebQrCodeScanResult() {
        return R.data(configService.getWebLoginQrCodeScanResult());
    }


    @GetMapping("/tv-qr-code")
    @Operation(summary = "申请Tv登陆二维码")
    public R getTvQrCode() {

        String url = configService.getTvLoginQrCode();
        return R.data(url);
    }


    @GetMapping("/tv-scan-result")
    @Operation(summary = "获取Tv登陆扫码结果")
    public R getTvQrCodeScanResult() {
        return R.data(configService.getTvLoginQrCodeScanResult());
    }
}
