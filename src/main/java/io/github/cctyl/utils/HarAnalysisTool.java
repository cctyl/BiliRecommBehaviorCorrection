package io.github.cctyl.utils;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.HarReaderMode;
import de.sstoehr.harreader.model.*;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.ApiHeader;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.Serializable;
import java.util.*;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * har 分析工具
 */
@Component
@Slf4j
public class HarAnalysisTool {

    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 需要忽略的cookie 或者header
     */
    private static List<String> ignoreString = Arrays.asList(
            "bili_ticket_expires",
            "bili_ticket",
            "b_nut",
            "Referer",
            "Cookie",
            "Host",
            "Content-Length"

    );


    /**
     * 从指定路径加载har
     *
     * @param path
     */
    public void load(String path, boolean refresh) {
        if (path == null) {
            path = "E:\\temp\\www.bilibili.com.har";
        }
        load(new File(path), refresh);
    }

    /**
     * 从指定har文件中重新加载header
     *
     * @param harFile
     * @param refresh
     */
    public void load(File harFile, boolean refresh) {
        HarReader harReader = new HarReader();
        Har har = null;

        Map<String, String> commonCookieMap = new HashMap<>();
        Map<String, String> commonHeaderMap = new HashMap<>();
        HashMap<String, Integer> frequencyMap = new HashMap<>();

        try {
            har = harReader.readFromFile(harFile, HarReaderMode.LAX);

            if (refresh) {
                GlobalVariables.commonHeaderMap = new HashMap<>();
                GlobalVariables.commonCookieMap = new HashMap<>();
                GlobalVariables.apiHeaderMap = new HashMap<>();
            }
            har.getLog().getEntries().forEach(harEntry -> {
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
                    if (!ignoreString.contains(cookie.getName())){
                        DataUtil.countFrequency(frequencyMap,cookie.getName());
                        curCookieMap.put(cookie.getName(), cookie.getValue());
                    }
                }
                commonCookieMap.putAll(curCookieMap);


                HashMap<String, String> curHeaderMap = new HashMap<>();
                for (HarHeader header : request.getHeaders()) {
                    if (!ignoreString.contains(header.getName())){
                        DataUtil.countFrequency(frequencyMap,header.getName());
                        curHeaderMap.put(header.getName(), header.getValue());
                    }
                }
                commonHeaderMap.putAll(curHeaderMap);



                ApiHeader apiHeader = new ApiHeader()
                        .setUrl(extractedUrl)
                        .setCookies(curCookieMap)
                        .setHeaders(curHeaderMap);
                GlobalVariables.apiHeaderMap.put(extractedUrl, apiHeader);

            });

            //只保留出现次数大于3的header和cookie
            for (Map.Entry<String, String> entry : commonCookieMap.entrySet()) {
                if (frequencyMap.getOrDefault( entry.getKey(),0)>2){
                    GlobalVariables.commonCookieMap.put(entry.getKey(),entry.getValue());
                }
            }
            for (Map.Entry<String, String> entry : commonHeaderMap.entrySet()) {
                if (frequencyMap.getOrDefault( entry.getKey(),0)>2){
                    GlobalVariables.commonHeaderMap.put(entry.getKey(),entry.getValue());
                }
            }


            redisUtil.delete(API_HEADER_MAP);
            redisUtil.delete(COMMON_COOKIE_MAP);
            redisUtil.delete(COMMON_HEADER_MAP);

            //这是加载到redis中
            redisTemplate.opsForHash().putAll(API_HEADER_MAP, GlobalVariables.apiHeaderMap);
            redisUtil.hPutAll(COMMON_COOKIE_MAP, GlobalVariables.commonCookieMap);
            redisUtil.hPutAll(COMMON_HEADER_MAP, GlobalVariables.commonHeaderMap);

            log.info("har加载完毕！");
        } catch (HarReaderException e) {
            e.printStackTrace();
        }
    }



}

