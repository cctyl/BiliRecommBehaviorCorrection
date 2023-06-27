package io.github.cctyl.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.entity.*;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.net.HttpCookie;
import java.nio.charset.Charset;
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
     * 播放者用户id
     */
    private String mid;

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
        }else {
            throw new RuntimeException("cookie为空");
        }
        mid = (String) redisUtil.get("mid");
        if (mid==null){
            throw new RuntimeException("mid 为空");
        }

        if (cookieMap.get("bili_jct")==null){
            throw new RuntimeException("csrf 为空");
        }
    }

    /**
     * 封装通用的get
     * 携带cookie、ua
     * 记忆cookie
     *
     * @param url
     * @return
     */
    private HttpResponse commonGet(String url) {
        HttpRequest request = HttpRequest.get(url)
                .header("User-Agent", BROWSER_UA_STR)
                .cookie(getCookieStr());
        HttpResponse response = request
                .execute();
        updateCookie(response);
        return response;
    }

    /**
     * 封装通用Post
     * @param url
     * @param paramMap
     * @return
     */
    private HttpResponse commonPost(String url, Map<String, Object> paramMap) {
        HttpRequest request =
                HttpRequest.post(url)
                        .header("Content-Type","application/x-www-form-urlencoded")
                        .header("User-Agent", BROWSER_UA_STR)
                        .form(paramMap)
                        .cookie(getCookieStr());
        HttpResponse response = request
                .execute();
        updateCookie(response);
        return response;
    }
    /**
     * 封装通用的get
     * 携带cookie、ua、参数的url编码
     * 记忆cookie
     *
     * @param url
     * @return
     */
    private HttpResponse commonGet(String url, Map<String, Object> paramMap) {
        HttpRequest request = HttpRequest.get(url)
                .header("User-Agent", BROWSER_UA_STR)
                .form(paramMap)
                .cookie(getCookieStr());
        HttpResponse response = request
                .execute();
        updateCookie(response);
        return response;
    }

    /**
     * 获取热门排行榜数据（非首页推荐）
     *
     * @param pageNum  页码，1开始
     * @param pageSize 每页记录数，页数
     */
    public List<VideoDetail> getHotRankVideo(int pageNum, int pageSize) {
        String url = "https://api.bilibili.com/x/web-interface/popular?pn=" + pageNum + "&ps=" + pageSize;
        String body = commonGet(url).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);
        List<VideoDetail> videoInfoList =
                jsonObject
                        .getJSONObject("data")
                        .getJSONArray("list")
                        .stream()
                        .map(o -> ((JSONObject) o).to(VideoDetail.class) )
                        .collect(Collectors.toList());
        return videoInfoList;
    }


    /**
     * 获取历史观看记录
     *
     * @return
     */
    public JSONObject getHistory() {
        String url = "https://api.bilibili.com/x/web-interface/history/cursor?ps=1&pn=1";
        String body = commonGet(url).body();
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
        return commonGet(url);
    }

    /**
     * 更新cookie
     */
    public void updateCookie(HttpResponse response) {
        List<HttpCookie> cookies = response.getCookies();
        for (HttpCookie cookie : cookies) {
            String name = cookie.getName();
            cookieMap.put(name, cookie.getValue());
        }

        //缓存
        redisUtil.hPutAll(COOKIES_KEY, cookieMap);
    }

    /**
     * 判断响应是否正常
     * {
     * "code": 0,
     * "message": "0",
     * "ttl": 1
     * }
     *
     * @param jsonObject
     * @return
     */
    public boolean checkResp(JSONObject jsonObject) {
        return jsonObject.getIntValue("code") == 0;
    }

    /**
     * 检查响应，如果响应不正确，则抛出异常
     * @param jsonObject
     * @param body
     */
    public void checkRespAndThrow(JSONObject jsonObject,String body){
        if (!checkResp(jsonObject)) {
            log.error("响应异常，message={}", jsonObject.getString("message"));
            log.error("body={}", body);
            throw new RuntimeException("响应异常");
        }
    }

    /**
     * 根据关键词进行综合搜索
     *
     * @param keyword
     */
    public List<SearchResult> search(String keyword, int page) {

        String url = "https://api.bilibili.com/x/web-interface/search/all/v2";
        Map<String, Object> paramMap = Map.of(
                "search_type", "video",
                "keyword", keyword,
                "order", "totalrank",
                "page", page
        );
        String body = commonGet(url, paramMap).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject,body);

        Object videoObj = jsonObject
                .getJSONObject("data")
                .getJSONArray("result")
                .stream()
                .filter(o -> "video".equals(((JSONObject) o).getString("result_type")))
                .findFirst()
                .orElse(null);
        if (videoObj == null) {
            return null;
        }

        List<SearchResult> data = ((JSONObject) videoObj).getJSONArray("data")
                .stream()
                .map(o -> JSONObject.parseObject(o.toString(), SearchResult.class))
                .collect(Collectors.toList());

        return data;
    }

    /**
     * 根据bvid获取视频详情
     * @param bvid
     */
    public VideoDetail getVideoDetail(String bvid) {
        String url = "https://api.bilibili.com/x/web-interface/view";
        String body = commonGet(url, Map.of("bvid", bvid)).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject,body);
        return jsonObject.getJSONObject("data").to(VideoDetail.class);
    }

    /**
     * 获取视频的播放地址
     * curl -G 'https://api.bilibili.com/x/player/playurl' \
     *     --data-urlencode 'bvid=BV1rp4y1e745' \
     *     --data-urlencode 'cid=244954665' \
     *     --data-urlencode 'qn=0' \
     *     --data-urlencode 'fnval=80' \
     *     --data-urlencode 'fnver=0' \
     *     --data-urlencode 'fourk=1' \
     *     -b 'SESSDATA=xxx'
     *
     * @param bvid
     * @param cid
     * @return
     */
    public String getVideoUrl(String bvid,int cid) {
        String url  = "https://api.bilibili.com/x/player/playurl";
        String body = commonGet(url, Map.of(
                "bvid", bvid,
                "cid", cid,
                "qn", 64
        )).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject,body);
        VideoUrl videoUrl = jsonObject.getJSONObject("data").to(VideoUrl.class);
        if (CollUtil.isNotEmpty(videoUrl.getDurl()) ){
            return videoUrl.getDurl().get(0).getUrl();
        }else {
            log.error("body={}",body);
            throw new RuntimeException("url 获取失败");
        }
    }

    /**
     * 上报播放心跳
     * @param start_ts
     * @param aid
     * @param cid
     * @param type 视频类型，通常都是3
     * @param sub_type
     * @param dt
     * @param realtime
     * @param play_type
     *          0：播放中
     *          1：开始播放
     *          2：暂停
     *          3：继续播放
     * @param played_time
     * @param real_played_time
     * @param video_duration
     * @param last_play_progress_time
     * @param max_play_progress_time
     * @return
     */
    public JSONObject reportHeartBeat(
            long start_ts,
            int aid,
            int cid,
            int type,
            int sub_type,
            int dt,
            int realtime,
            int play_type,
            int played_time,
            int real_played_time,
            int video_duration,
            int last_play_progress_time,
            int max_play_progress_time
    ) {
        String url = "https://api.bilibili.com/x/click-interface/web/heartbeat";
        Map<String, Object> paramMap = new HashMap<>(16);

        paramMap.put("start_ts", start_ts);
        //播放视频的用户id
        paramMap.put("mid", mid);
        paramMap.put("aid", aid);
        paramMap.put("cid", cid);
        paramMap.put("type", type);
        paramMap.put("sub_type", sub_type);
        paramMap.put("dt", dt);
        paramMap.put("play_type", play_type);
        paramMap.put("realtime", realtime);
        paramMap.put("played_time", played_time);
        paramMap.put("real_played_time", real_played_time);
        paramMap.put("quality", "80");
        paramMap.put("video_duration", video_duration);
        paramMap.put("last_play_progress_time", last_play_progress_time);
        paramMap.put("max_play_progress_time", max_play_progress_time);
        paramMap.put("extra", "{\"player_version\":\"4.1.18\"}");
        paramMap.put("csrf", cookieMap.get("bili_jct"));


        String body = commonPost(url,paramMap).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject,body);
        return jsonObject;
    }


    /**
     * 完全替换cookie
     * @param cookieStr
     */
    public void replaceCookie(String cookieStr) {
        Map<String, String> map = new HashMap<>(10);
        String[] cookieArr = cookieStr.split(";");
        for (String cookie : cookieArr) {
            String[] split = cookie.split("=");
            map.put(split[0], split[1]);
        }
        this.cookieMap = map;
        //缓存
        redisUtil.hPutAll(COOKIES_KEY, map);
    }

}
