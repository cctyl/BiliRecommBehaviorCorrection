package io.github.cctyl.controller;

import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private BiliApi biliApi;

    @PostMapping("/cookie-header")
    public R testCookieAndHeader(
            @RequestBody List<Map<String, String>> paramList
    ) {

        if (paramList.size() < 2) {
            return R.error().setMessage("参数缺失");
        }
        GlobalVariables.commonHeaderMap = paramList.get(0);
        GlobalVariables.commonCookieMap = paramList.get(1);

        JSONObject history = biliApi.getHistory();

        return R.ok().setData(history);
    }

    @GetMapping("/cookie-header")
    public R getCookieAndHeader() {
        return R.ok().setData(
                Arrays.asList(GlobalVariables.cookieMap, GlobalVariables.commonHeaderMap
                ));
    }



}