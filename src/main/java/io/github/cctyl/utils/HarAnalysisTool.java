package io.github.cctyl.utils;

import com.google.protobuf.Api;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.HarReaderMode;
import de.sstoehr.harreader.model.*;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.pojo.ApiHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
            "referer",
            "cookie",
            "host",
            "content-length",
            "accept-encoding",
            "grpc-encoding",
            "grpc-accept-encoding",
            "accept-encoding"

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
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<ApiHeader> apiHeaderList = new ArrayList<>(har.getLog().getEntries().size());

        try {
            har = harReader.readFromFile(harFile, HarReaderMode.LAX);


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
                    if (!ignoreString.contains(cookie.getName().toLowerCase())){
                        DataUtil.countFrequency(frequencyMap,cookie.getName());
                        curCookieMap.put(cookie.getName(), cookie.getValue());
                    }
                }
                commonCookieMap.putAll(curCookieMap);


                HashMap<String, String> curHeaderMap = new HashMap<>();
                for (HarHeader header : request.getHeaders()) {
                    if (!ignoreString.contains(header.getName().toLowerCase())){
                        DataUtil.countFrequency(frequencyMap,header.getName());
                        curHeaderMap.put(header.getName().replaceAll(":",""), header.getValue());
                    }
                }
                commonHeaderMap.putAll(curHeaderMap);



                ApiHeader apiHeader = new ApiHeader()
                        .setUrl(extractedUrl)
                        .setCookies(curCookieMap)
                        .setHeaders(curHeaderMap);
                apiHeaderList.add(apiHeader);

            });

            //只保留出现次数大于合格数的header和cookie
            //保留 在80% 以上请求都出现过的cookie 和 header。最低不能少于2
            double qualifiedNum  = Math.max(har.getLog().getEntries().size() * 0.8,2.0);
            frequencyMap.entrySet()
                    .stream()
                    .filter(e -> e.getValue() <= qualifiedNum)
                    .map(Map.Entry::getKey)
                    .forEach(s -> {
                        commonCookieMap.remove(s);
                        commonHeaderMap.remove(s);
                    });
            //此时 commonCookieMap 和 commonHeaderMap 保留的都是合格的数据

            if (refresh) {
                //重置情况，首先删除原本的数据，然后完整的添加一次 包括  commonCookieMap commonHeaderMap apiHeaderMap 三者
                GlobalVariables.INSTANCE.replaceCommonCookieMap(commonCookieMap);
                GlobalVariables.INSTANCE.replaceCommonHeaderMap(commonHeaderMap);
                GlobalVariables.INSTANCE.replaceApiHeaderMap(apiHeaderList);


            }else {
                //考虑更新情况，需要更新的有 commonCookieMap commonHeaderMap apiHeaderMap 三者
                GlobalVariables.INSTANCE.updateCommonCookieMap(commonCookieMap);
                GlobalVariables.INSTANCE.updateCommonHeaderMap(commonHeaderMap);
                GlobalVariables.INSTANCE.updateApiHeaderMap(apiHeaderList);
            }


            log.info("har加载完毕！");
        } catch (HarReaderException e) {
            e.printStackTrace();
        }
    }



}


