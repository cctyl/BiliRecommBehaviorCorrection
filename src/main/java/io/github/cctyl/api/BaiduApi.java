package io.github.cctyl.api;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.entity.BaiduImageClassify;
import io.github.cctyl.utils.RedisUtil;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.BAIDU_ASK_KEY;

/**
 * 百度相关api
 */
@ConditionalOnExpression("'${common.imgService}'.contains('baidu')")
@Component
@Slf4j
public class BaiduApi {


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ApplicationProperties applicationProperties;

    /**
     * 人体检测和属性识别
     *
     * @param imgBase64Str
     * @return
     */
    public BaiduImageClassify getGender(String imgBase64Str) {
        String body = HttpRequest.post("https://aip.baidubce.com/rest/2.0/image-classify/v1/body_attr?access_token=" + getAccessToken())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .body("image=" + imgBase64Str)
                .execute()
                .body();
        log.debug("body={}", body);
        return JSONObject.parseObject(body, BaiduImageClassify.class);
    }


    /**
     * 检测图片中是否包含人体
     *
     * @param imgBase64Str
     * @return
     */
    public boolean isHuman(String imgBase64Str) {
        try {
            String body = HttpRequest.post("https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=" + getAccessToken())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .body("image=" + imgBase64Str)
                    .execute()
                    .body();
            log.debug("body={}", body);

            JSONObject jsonObject = JSONObject.parseObject(body);
            if (jsonObject.getIntValue("result_num") < 1) {
                return false;
            } else {
                String word = jsonObject.getJSONArray("result")
                        .stream().map(o ->
                                {
                                    var j = (JSONObject) o;
                                    return j.getString("keyword") + j.getString("root");
                                }
                        ).collect(Collectors.joining());
                return word.contains("女");
            }
        } catch (HttpException e) {
            e.printStackTrace();
            return false;
        }


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
    public String getAccessToken() {

        Object o = redisUtil.get(BAIDU_ASK_KEY);
        if (!StrUtil.isBlankIfStr(o)) {
            return (String) o;
        }

        String body = HttpRequest.post("https://aip.baidubce.com/oauth/2.0/token")

                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .form(Map.of(
                        "grant_type", "client_credentials",
                        "client_id", applicationProperties.getBaidu().getClientId(),
                        "client_secret", applicationProperties.getBaidu().getClientSecret()
                ))
                .execute()
                .body();
        log.debug("body={}", body);
        String accessToken = JSONObject.parseObject(body).getString("access_token");
        redisUtil.setEx(BAIDU_ASK_KEY, accessToken, 30, TimeUnit.DAYS);
        return accessToken;
    }

}
