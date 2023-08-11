package io.github.cctyl.utils;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.HarReaderMode;
import de.sstoehr.harreader.model.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * har 分析工具
 */
@Component
public class HarAnalysisTool {

    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    private static Map<String, ApiHeader> apiHeaderMap = new HashMap<>();
    private static Map<String, String> commonCookieMap = new HashMap<>();
    private static Map<String, String> commonHeaderMap = new HashMap<>();


    /**
     * 从har中重新加载header
     */
    public void load() {


        HarReader harReader = new HarReader();
        Har har = null;
        try {
            har = harReader.readFromFile(new File("E:\\temp\\www.bilibili.com.har"), HarReaderMode.LAX);

            har.getLog().getEntries()
                    .stream().forEach(harEntry -> {
                HarRequest request = harEntry.getRequest();

                String url = request.getUrl();
                int i = url.indexOf("?");
                String extractedUrl = url;
                if (i != -1) {
                    extractedUrl = url.substring(0, i);
                }

                if (extractedUrl.contains("https://www.bilibili.com/video/BV")) {
                    extractedUrl = "https://www.bilibili.com/video/";
                }

                HashMap<String, String> curCookieMap = new HashMap<>();
                for (HarCookie cookie : request.getCookies()) {
                    curCookieMap.put(cookie.getName(), cookie.getValue());
                }
                commonCookieMap.putAll(curCookieMap);

                HashMap<String, String> curHeaderMap = new HashMap<>();
                for (HarHeader header : request.getHeaders()) {
                    curCookieMap.put(header.getName(), header.getValue());
                }
                commonHeaderMap.putAll(curCookieMap);
                ApiHeader apiHeader = new ApiHeader()
                        .setUrl(extractedUrl)
                        .setCookies(curCookieMap)
                        .setHeaders(curHeaderMap);

                apiHeaderMap.put(extractedUrl, apiHeader);

            });


            redisUtil.delete(API_HEADER_MAP);
            redisUtil.delete(COMMON_COOKIE_MAP);
            redisUtil.delete(COMMON_HEADER_MAP);

            redisTemplate.opsForHash().putAll(API_HEADER_MAP, apiHeaderMap);
            redisUtil.hPutAll(COMMON_COOKIE_MAP,commonCookieMap);
            redisUtil.hPutAll(COMMON_HEADER_MAP,commonHeaderMap);

            redisUtil.hGet(COMMON_COOKIE_MAP,"bili_jct");

            System.out.println(har.getLog().getCreator().getName());
        } catch (HarReaderException e) {
            e.printStackTrace();
        }
    }
}


/**
 * 各Api 所需要用到的cookie 和 header 的集合
 */
@Data
@Accessors(chain = true)
class ApiHeader implements Serializable {

    /**
     * api地址，不包含参数
     */
    private String url;


    /**
     * 该api将会用到的cookie
     */
    private Map<String, String> cookies;

    /**
     * 该api将会用到的请求头
     */
    private Map<String, String> headers;

}
