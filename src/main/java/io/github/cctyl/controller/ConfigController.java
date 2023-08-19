package io.github.cctyl.controller;


import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.R;
import io.github.cctyl.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import static io.github.cctyl.constants.AppConstant.STOP_WORDS_KEY;


@RestController
@RequestMapping("/config")
@Api(tags = "配置模块")
public class ConfigController {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HarAnalysisTool harAnalysisTool;

    @PutMapping("/cookie")
    @ApiOperation(value = "更新cookie")
    public R updateCookie(
            @RequestParam String cookieStr
    ) {
        if (StrUtil.isBlank(cookieStr)) {
            return R.error().setMessage("错误的数据");
        }
        applicationProperties.getDefaultData().setCookie(cookieStr);

        Map<String, String> cookieMap = DataUtil.splitCookie(applicationProperties.getDefaultData().getCookie());
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            GlobalVariables.cookieMap.put(entry.getKey(), entry.getValue());
        }
        //缓存
        GlobalVariables.setCookieMap(GlobalVariables.cookieMap);
        return R.ok().setData(cookieMap);
    }


    @PostMapping("/load-har")
    @ApiOperation(value = "上传har，更新cookie 和 header")
    public R loadHar(MultipartFile multipartFile, @RequestParam Boolean refresh) throws IOException {

        //保存到临时文件夹
        File tempDir = new File(new ApplicationHome().getDir().getPath(),"temp");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        LocalDateTime now = LocalDateTime.now();
        String fileName = IdGenerator.nextId() + "-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth()+".har";
        File harFile = new File(tempDir, fileName);
        multipartFile.transferTo(harFile);
        harAnalysisTool.load(harFile,refresh);

        return R.ok().setData( Map.of("cookieMap",GlobalVariables.cookieMap,
                "headerMap",GlobalVariables.commonHeaderMap
        ));
    }

    @ApiOperation(value = "更新停顿词列表")
    @PostMapping("/reload-stopword")
    public R reloadStopWord(){

        ClassPathResource classPathResource = new ClassPathResource("cn_stopwords.txt");
        redisUtil.delete(STOP_WORDS_KEY);
        try {
            redisUtil.sAdd(STOP_WORDS_KEY, Files.lines(Paths.get(classPathResource.getFile().getPath())).toArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.ok().setMessage("停顿词列表长度为:"+redisUtil.sSize(STOP_WORDS_KEY));
    }



}
