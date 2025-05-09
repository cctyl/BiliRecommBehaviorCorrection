package io.github.cctyl.api;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.anno.NoLog;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.BaiduImageClassify;
import io.github.cctyl.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 百度相关api
 */
@ConditionalOnExpression("'${common.imgService}'.contains('baidu')")
@Component
@Slf4j
public class BaiduApi {


    @Autowired
    private ConfigService configService;


    /**
     * 人体检测和属性识别
     *
     * @param imgBase64Str
     * @return
     */
    @NoLog
    public BaiduImageClassify getGender(String imgBase64Str) {


        JSONObject jsonObject =
                checkRespAndRetry(
                        () -> JSONObject.parseObject(
                                HttpRequest.post("https://aip.baidubce.com/rest/2.0/image-classify/v1/body_attr?access_token=" + getAccessToken(false))
                                        .header("Content-Type", "application/x-www-form-urlencoded")
                                        .header("Accept", "application/json")
                                        .body("image=" + imgBase64Str)
                                        .execute()
                                        .body()
                        )
                );
        log.debug(jsonObject.toString());
        return jsonObject.to(BaiduImageClassify.class);
    }


    /**
     * 检测图片中是否包含人体
     *
     * @param imgBase64Str
     * @return
     */
    @NoLog
    public boolean isHuman(String imgBase64Str) {
        try {

            JSONObject jsonObject = checkRespAndRetry(() -> JSONObject.parseObject(HttpRequest.post("https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=" + getAccessToken(false))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .body("image=" + imgBase64Str)
                    .execute()
                    .body()));

            log.debug(jsonObject.toString());
            if (jsonObject.getIntValue("result_num") < 1) {
                return false;
            } else {

                return  jsonObject.getJSONArray("result")
                        .stream().anyMatch(o ->
                                {
                                    var j = (JSONObject) o;
                                    return  (j.getString("keyword") + j.getString("root")).contains("女")
                                            && j.getDouble("score")>0.5;
                                }
                        );
            }
        } catch (HttpException e) {
            log.error(e.getMessage(),e);
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
    @NoLog
    public String getFileContentAsBase64(byte[] bytes) {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return URLEncoder.encode(base64, StandardCharsets.UTF_8);
    }


    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     */
    public String getAccessToken(boolean refresh) {

        String baiduAskKey = configService.getBaiduAskKey();

        if (!(StrUtil.isBlankIfStr(baiduAskKey) || refresh)) {
            return baiduAskKey;
        }

        String body = HttpRequest.post("https://aip.baidubce.com/oauth/2.0/token")

                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .form(Map.of(
                        "grant_type", "client_credentials",
                        "client_id", configService.getBaiduClientId(),
                        "client_secret", configService.getBaiduClientSecret()
                ))
                .execute()
                .body();
        log.debug("body={}", body);
        String accessToken = JSONObject.parseObject(body).getString("access_token");


        configService.updateBaiduAskKey(accessToken);

        return accessToken;
    }


    /**
     * accessKey 接口专用
     * 检查响应，如果响应是未登陆，则刷新accessKey并重试
     * 如果还是无法获取正确响应，则抛出异常
     *
     * @param supplier 重试的方法，需要返回一个响应
     */
    public JSONObject checkRespAndRetry(Supplier<JSONObject> supplier) {

        JSONObject jsonObject = supplier.get();
        if (jsonObject.get("error_code") != null

        ) {
            if (jsonObject.getIntValue("error_code") == 110 || jsonObject.getIntValue("error_code") == 6) {
                getAccessToken(true);
                log.debug("刷新token并重试一次");
                jsonObject = supplier.get();
            }else {
                log.error("出现意料之外的异常:{}",jsonObject);
            }
        }
        return jsonObject;
    }

}
