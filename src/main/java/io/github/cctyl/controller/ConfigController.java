package io.github.cctyl.controller;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.server.HttpServerResponse;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.po.Config;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.service.ConfigService;
import io.github.cctyl.service.CookieHeaderDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/config")
@Tag(name = "配置模块")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final BiliApi biliApi;
    private final CookieHeaderDataService cookieHeaderDataService;



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

        Map<String, String> refreshCookieMap = cookieHeaderDataService.getRefreshCookieMap();
        return R.data(refreshCookieMap);
    }

    @PostMapping("/standard")
    @Operation(summary = "更新基本配置信息")
    public R updateStandardConfigInfo(@RequestBody List<Config>  configList){
        configService.updateConfigList(configList);
        return R.ok();
    }

    @GetMapping("/standard")
    @Operation(summary = "查询基本配置信息")
    public R<List<Config>> getStandardConfigInfo(){
        return R.data(configService.getConfigList());
    }



    @GetMapping("/check-cookie")
    @Operation(summary ="检查cookie")
    public R checkCookie(){
        JSONObject history = biliApi.getHistory();
        return R.data(history);
    }


    @GetMapping("/check-accesskey")
    @Operation(summary ="检查accesskey")
    public R checkAccesskey(){
        try {
            JSONObject info = biliApi.getUserInfo();
            return R.data(info);
        } catch (Exception e) {

            return R.error().setMessage(e.getMessage());
        }
    }
    @GetMapping("/getPic")
    @Operation(summary ="获取图片")
    public void getPic(
            @RequestParam("url") String url,
            HttpServletResponse response
    ) throws IOException {
        // 使用 Hutool 从 URL 下载图片
        InputStream inputStream = HttpUtil.createGet(url).execute().bodyStream();

        // 设置响应内容类型
        response.setContentType("image/jpeg"); // 根据实际情况设置内容类型

        // 将图片流写入响应
        IoUtil.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        IoUtil.close(inputStream);

    }


    @GetMapping("/web-qr-code")
    @Operation(summary = "申请web登陆二维码（该接口拿不到accessKey）")
    public R getWebQrCode() {

        String url = biliApi.getWebLoginQrCode();
        return R.data(url);
    }


    @GetMapping("/web-scan-result")
    @Operation(summary = "获取web登陆扫码结果（该接口拿不到accessKey）")
    public R getWebQrCodeScanResult() {
        return R.data(biliApi.getWebLoginQrCodeScanResult());
    }


    @GetMapping("/tv-qr-code")
    @Operation(summary = "申请Tv登陆二维码")
    public R getTvQrCode() {

        String url = biliApi.getTvLoginQrCode();
        return R.data(url);
    }


    @GetMapping("/tv-scan-result")
    @Operation(summary = "获取Tv登陆扫码结果")
    public R getTvQrCodeScanResult() {
        return R.data(biliApi.getTvQrCodeScanResult());
    }
}
