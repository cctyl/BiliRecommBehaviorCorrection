package io.github.cctyl.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.entity.Owner;
import io.github.cctyl.entity.Stat;
import io.github.cctyl.entity.VideoInfo;
import io.github.cctyl.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * bilibili相关api都在这里
 * 不打算使用封装层数过多的 retrofit
 * restTemplate 对于增加请求体等需求又比较复杂
 */
@Component
@Slf4j
public class BiliApi {


    @Autowired
    private RedisUtil redisUtil;

    /**
     * 浏览器端的userAgent
     */
    private static final String BROWSER_UA_STR = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0";

    /**
     * cookie
     */
    private Map<String, String> cookieMap = new HashMap<>(10);


    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        //0.加载cookie
        Map<Object, Object> cookiesFromRedis = redisUtil.hGetAll(COOKIES_KEY);
        if (CollUtil.isNotEmpty(cookiesFromRedis)) {
            cookiesFromRedis.keySet().forEach(o -> {
                cookieMap.put((String) o, (String) cookiesFromRedis.get(o));
            });
        }
    }


    /**
     * 获取热门排行榜数据（非首页推荐）
     *
     * @param pageNum  页码，1开始
     * @param pageSize 每页记录数，页数
     */
    public List<VideoInfo> getHotRankVideo(int pageNum, int pageSize) {
        String url = "https://api.bilibili.com/x/web-interface/popular?pn=" + pageNum + "&ps=" + pageSize;
        String body = HttpRequest.get(url)
                .header("User-Agent", BROWSER_UA_STR)
                .cookie(getCookieStr())
                .execute()
                .body();

        JSONObject jsonObject = JSONObject.parseObject(body);
        if (jsonObject.getIntValue("code") != 0) {
            log.error("响应异常，message={}", jsonObject.getString("message"));
            log.error("body={}", body);
            throw new RuntimeException("json 解析异常");
        }
        List<VideoInfo> videoInfoList =
                jsonObject
                        .getJSONObject("data")
                        .getJSONArray("list")
                        .stream()
                        .map(o ->
                                {
                                    //fixme 不能反序列化嵌套的对象
                                    var jo = (JSONObject) o;
                                    VideoInfo videoInfo = JSONObject.parseObject(o.toString(), VideoInfo.class)
                                            .setOwner(
                                                    JSONObject.parseObject(jo.get("owner").toString(), Owner.class)
                                            )
                                            .setStat(
                                                    JSONObject.parseObject(jo.get("stat").toString(), Stat.class)
                                            );
                                    return videoInfo;
                                }
                        )
                        .collect(Collectors.toList());

        return videoInfoList;
    }


    /**
     * 获取历史观看记录
     * @return
     */
    public JSONObject getHistory(){
        String url = "https://api.bilibili.com/x/web-interface/history/cursor?ps=1&pn=1";
        String body = HttpRequest.get(url)
                .header("User-Agent", BROWSER_UA_STR)
                .cookie(getCookieStr())
                .execute()
                .body();
        return JSONObject.parseObject(body);
    }

    /**
     * 获得cookie字符串
     *
     * @return
     */
    public String getCookieStr() {
        return cookieMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue() + ";")
                .collect(Collectors.joining());
    }

    /**
     * 获取首页数据
     */
    public HttpResponse getHome() {
        String url = "https://www.bilibili.com/";
        return HttpRequest.get(url)
                .header("User-Agent", BROWSER_UA_STR)
                .cookie(getCookieStr())
                .execute();
    }

    /**
     * 更新cookie
     */
    public void updateCookie() {
        HttpResponse response = getHome();
        List<HttpCookie> cookies = response.getCookies();
        for (HttpCookie cookie : cookies) {
            String name = cookie.getName();
            cookieMap.put(name,cookie.getValue());
        }

        //缓存
        redisUtil.hPutAll(COOKIES_KEY,cookieMap);
    }
}
