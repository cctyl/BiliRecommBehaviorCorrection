package io.github.cctyl.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static io.github.cctyl.constants.AppConstant.THIRD_PART_APPKEY;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private BiliApi biliApi;

    @PostMapping("/cookie-header")
    public R testCookieAndHeader(
            @RequestBody Map<String, List<String>> paramMap
    ) {
        String url = "https://passport.bilibili.com/login/app/third?appkey=" + THIRD_PART_APPKEY + "&api=https://www.mcbbs.net/template/mcbbs/image/special_photo_bg.png&sign=04224646d1fea004e79606d3b038c84a";
        HttpRequest request = HttpRequest.get(url)
                .clearHeaders()
                .header(paramMap,true)
                .timeout(10000)
                .cookie(biliApi.getCookieStr(url));
        String body = request
                .execute().body();
        return R.ok().setData(body);
    }

    @GetMapping("/cookie-header")
    public R getCookieAndHeader() {
        HashMap<String, Object> result = new HashMap<>();
        GlobalVariables.apiHeaderMap.get("https://passport.bilibili.com/login/app/third")
                .getHeaders().forEach((k, v) -> result.put(k, Collections.singletonList(v)));
        return R.ok().setData(
                result
        );
    }



}