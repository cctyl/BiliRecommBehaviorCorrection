package io.github.cctyl.controller;


import cn.hutool.core.util.StrUtil;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.R;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.HttpCookie;
import java.util.Map;

import static io.github.cctyl.constants.AppConstant.COOKIES_KEY;
import static io.github.cctyl.constants.AppConstant.SUSPICIOUS_COOKIE_KEY;

@RestController
@RequestMapping("/config")
@Api(tags="配置模块")
public class ConfigController {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private RedisUtil redisUtil;

    @PutMapping("/cookie")
    @ApiOperation(value = "更新cookie")
    public R updateCookie(
            @RequestParam String cookieStr
    ){
        if (StrUtil.isBlank(cookieStr)){
            return R.error().setMessage("错误的数据");
        }
        applicationProperties.getDefaultData().setCookie(cookieStr);

        Map<String, String> cookieMap = DataUtil.splitCookie(applicationProperties.getDefaultData().getCookie());
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            GlobalVariables.cookieMap.put(entry.getKey(), entry.getValue());
        }
        //缓存
        redisUtil.hPutAll(COOKIES_KEY, GlobalVariables.cookieMap);
        return R.ok().setData(cookieMap);
    }
}
