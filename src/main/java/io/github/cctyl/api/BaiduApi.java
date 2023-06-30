package io.github.cctyl.api;


import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.entity.BaiduImageClassify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

/**
 * 百度相关api
 */
@ConditionalOnExpression("${common.baidu.enable}==true")
@Component
@Slf4j
public class BaiduApi {




    @Autowired
    private ApplicationProperties applicationProperties;

    /**
     * 人体检测和属性识别
     * @param imgBase64Str
     * @return
     */
    public BaiduImageClassify getGender(String imgBase64Str) {
        String body = HttpRequest.post("https://aip.baidubce.com/rest/2.0/image-classify/v1/body_attr?access_token=" + getAccessToken())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .body("image="+imgBase64Str)
                .execute()
                .body();
        log.debug("body={}",body);
        return JSONObject.parseObject(body, BaiduImageClassify.class);
    }


    /**
     * 获取文件base64编码
     *
     * @param bytes 文件的byte数组
     * @return base64编码信息，不带文件头
     * @throws IOException IO异常
     */
    public String getFileContentAsBase64(byte[] bytes) {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return URLEncoder.encode(base64, StandardCharsets.UTF_8);
    }


    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     */
    public String getAccessToken()  {
        String body = HttpRequest.post("https://aip.baidubce.com/oauth/2.0/token")

                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .form(Map.of(
                        "grant_type","client_credentials",
                        "client_id",applicationProperties.getBaidu().getClientId(),
                        "client_secret",applicationProperties.getBaidu().getClientSecret()

                ))
                .execute()
                .body();



        log.debug("body={}",body);
        String accessToken = JSONObject.parseObject(body).getString("access_token");
        log.info("当前获得的token={}",accessToken);
        return accessToken;
    }

}
