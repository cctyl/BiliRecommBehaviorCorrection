package io.github.cctyl.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.constants.ErrorCode;
import io.github.cctyl.entity.*;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * bilibili相关api都在这里
 * 不打算使用封装层数过多的 retrofit
 * restTemplate 对于增加请求体等需求又比较复杂
 */
@Component
@Slf4j
@Order(1)
public class BiliApi {


    @Autowired
    private RedisUtil redisUtil;

    /**
     * urlRegex
     */
    private static Pattern urlRegex = Pattern.compile(".*/([^/]+\\.png)$");

    /**
     * 用于获取wbi签名
     */
    private static final int[] mixinKeyEncTab = new int[]{
            46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
            33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
            61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
            36, 20, 34, 44, 52
    };

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
        log.debug("body={}", response.body());
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
        log.debug("body={}", response.body());
        updateCookie(response);
        return response;
    }

    /**
     * 封装通用的get
     * 携带cookie、ua、参数的url编码
     * 记忆cookie
     * 添加额外的请求头
     *
     * @param url
     * @return
     */
    private HttpResponse commonGet(String url, Map<String, Object> paramMap,
                                   Map<String, String> otherHeader) {
        HttpRequest request = HttpRequest.get(url)
                .header("User-Agent", BROWSER_UA_STR)
                .form(paramMap)
                .cookie(getCookieStr());

        otherHeader.forEach(request::header);
        HttpResponse response = request
                .execute();
        log.debug("body={}", response.body());
        updateCookie(response);
        return response;
    }


    /**
     * 封装通用Post
     *
     * @param url
     * @param paramMap
     * @return
     */
    private HttpResponse commonPost(String url, Map<String, Object> paramMap) {
        HttpRequest request =
                HttpRequest.post(url)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", BROWSER_UA_STR)
                        .form(paramMap)
                        .cookie(getCookieStr());
        HttpResponse response = request
                .execute();
        log.debug("body={}", response.body());
        updateCookie(response);
        return response;
    }


    /**
     * 获取bilibili图片的byte数组
     *
     * @param picUrl
     * @return
     */
    public byte[] getPicByte(String picUrl) throws IOException {
        HttpResponse response = HttpRequest.get(picUrl)
                .header("User-Agent", BROWSER_UA_STR)
                .cookie(getCookieStr())
                .executeAsync();
        InputStream inputStream = response.bodyStream();
        FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream();
        try (inputStream; fastByteArrayOutputStream) {
            byte[] buff = new byte[1024];
            int totalLength = 0;
            int len = 0;
            while ((len = inputStream.read(buff)) != -1) {
                fastByteArrayOutputStream.write(buff, 0, len);
                totalLength += len;
                if (totalLength > PIC_MAX_SIZE) {
                    throw new RuntimeException("图片过大，不采用");
                }
            }
            return fastByteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 获取热门排行榜数据（非首页推荐）
     * curl -G 'https://api.bilibili.com/x/web-interface/popular' \
     * --data-urlencode 'ps=20' \
     * --data-urlencode 'pn=1'
     *
     * @param pageNum  页码，1开始
     * @param pageSize 每页记录数，页数 默认20
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
                        .toList(VideoDetail.class);
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
        return GlobalVariables.cookieMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue() + ";")
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
            GlobalVariables.cookieMap.put(name, cookie.getValue());
        }

        //缓存
        redisUtil.hPutAll(COOKIES_KEY, GlobalVariables.cookieMap);
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
     *
     * @param jsonObject
     * @param body
     */
    public void checkRespAndThrow(JSONObject jsonObject, String body) {
        if (!checkResp(jsonObject)) {
            log.error("响应异常，message={}", jsonObject.getString("message"));
            log.error("body={}", body);
            checkOtherCode(jsonObject);
        }
    }

    /**
     * 判断是否是未登陆
     * true 表示未登陆，false表示其他
     *
     * @param jsonObject
     * @return
     */
    public boolean checkIsNoLogin(JSONObject jsonObject) {
        return jsonObject.getIntValue("code") == -101;
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
        if (checkIsNoLogin(jsonObject)) {
            log.debug("尝试刷新accessToken，并重新发起请求");
            //账号未登陆，强制刷新token，重新发起这次请求
            getAccessKeyByCookie(true);
            ThreadUtil.sleep(1);
            //重试一次
            jsonObject = supplier.get();
            if (checkIsNoLogin(jsonObject)) {
                throw new RuntimeException("刷新token后访问仍然失败，请检查日志");
            }
        }

        if (!checkResp(jsonObject)) {
            log.error("响应异常，message={}", jsonObject.getString("message"));
            log.error("body={}", jsonObject.toJSONString());
            checkOtherCode(jsonObject);
        }
        return jsonObject;
    }

    /**
     * 响应
     *
     * @param jsonObject
     */
    private void checkOtherCode(JSONObject jsonObject) {
        switch (jsonObject.getIntValue("code")) {
            case 65007:
                log.info(ErrorCode.ALREAD_THUMBUP.getMessage());
                break;

            default:
                log.error("body={}", jsonObject.toString());
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
        checkRespAndThrow(jsonObject, body);

        Object videoObj = jsonObject
                .getJSONObject("data")
                .getJSONArray("result")
                .stream()
                .filter(o -> "video".equals(((JSONObject) o).getString("result_type")))
                .findFirst()
                .orElse(null);
        if (videoObj == null) {
            log.error("查询结果为空, body={}", body);
            return null;
        }

        List<SearchResult> data = ((JSONObject) videoObj).getJSONArray("data")
                .toList(SearchResult.class);

        return data;
    }

    /**
     * 根据bvid获取视频详情
     *
     * @param avid
     */
    public VideoDetail getVideoCommonInfo(long avid) {
        String url = "https://api.bilibili.com/x/web-interface/view";
        String body = commonGet(url, Map.of("aid", avid)).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);
        return jsonObject.getJSONObject("data").to(VideoDetail.class);
    }

    /**
     * 获取视频的播放地址
     * curl -G 'https://api.bilibili.com/x/player/playurl' \
     * --data-urlencode 'bvid=BV1rp4y1e745' \
     * --data-urlencode 'cid=244954665' \
     * --data-urlencode 'qn=0' \
     * --data-urlencode 'fnval=80' \
     * --data-urlencode 'fnver=0' \
     * --data-urlencode 'fourk=1' \
     * -b 'SESSDATA=xxx'
     *
     * @param bvid
     * @param cid
     * @return
     */
    public String getVideoUrl(String bvid, int cid) {
        String url = "https://api.bilibili.com/x/player/playurl";
        String body = commonGet(url, Map.of(
                "bvid", bvid,
                "cid", cid,
                "qn", 64
        )).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);
        VideoUrl videoUrl = jsonObject.getJSONObject("data").to(VideoUrl.class);
        if (CollUtil.isNotEmpty(videoUrl.getDurl())) {
            return videoUrl.getDurl().get(0).getUrl();
        } else {
            log.error("body={}", body);
            throw new RuntimeException("url 获取失败");
        }
    }


    /**
     * 对视频进行点赞
     * curl 'https://api.bilibili.com/x/web-interface/archive/like' \
     * --data-urlencode 'aid=79677524' \
     * --data-urlencode 'like=1' \
     * --data-urlencode 'csrf=xxx' \
     * -b 'SESSDATA=xxx'
     *
     * @param aid
     * @return
     */
    public JSONObject thumpUp(int aid) {
        String url = "https://api.bilibili.com/x/web-interface/archive/like";
        String body = commonPost(url, Map.of(
                "aid", aid,
                "like", 1,
                "csrf", getCsrf()
        )).body();

        JSONObject jsonObject = JSONObject.parseObject(body);
        if (jsonObject.getIntValue("code") == 65006) {
            //已赞过
            return jsonObject;
        }
        checkRespAndThrow(jsonObject, body);
        return jsonObject;
    }

    /**
     * 点踩
     * curl -L -X POST 'https://app.biliapi.net/x/v2/view/dislike' \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * --data-urlencode 'access_key=xxx' \
     * --data-urlencode 'aid=xxx' \
     * --data-urlencode 'dislike=0'
     *
     * @param aid
     * @return
     */
    public JSONObject dislike(int aid) {
        String url = "https://app.biliapi.net/x/v2/view/dislike";

        return checkRespAndRetry(() ->
                JSONObject.parseObject(commonPost(url, Map.of(
                        "aid", aid,
                        "access_key", getAccessKeyByCookie(false),
                        "dislike", 0
                )).body()));
    }

    /**
     * 获取推荐数据
     */
    public List<RecommendCard> getRecommendVideo() {
        String url = "https://app.bilibili.com/x/v2/feed/index";
        JSONObject jsonObject = checkRespAndRetry(() -> JSONObject.parseObject(commonGet(url,
                Map.of(
                        "build", "1",
                        "mobi_app", "android",
                        "idx", getIdx(),
                        "appkey", THIRD_PART_APPKEY,
                        "access_key", getAccessKeyByCookie(false)
                )).body()));
        return jsonObject
                .getJSONObject("data")
                .getJSONArray("items")
                .toList(RecommendCard.class);
    }


    /**
     * 通过sessData 获得 accessKey
     * 目前来看不可用
     *
     * @return
     */
    public String getAccessKeyByCookie(boolean refresh) {
        //如果缓存中存在，则直接返回
        if (!refresh && !StrUtil.isBlankIfStr(redisUtil.get(ACCESS_KEY))) {
            log.debug("缓存中得到了accessKey={}", redisUtil.get(ACCESS_KEY));
            return (String) redisUtil.get(ACCESS_KEY);
        }
        String url = "https://passport.bilibili.com/login/app/third?appkey=" + THIRD_PART_APPKEY + "&api=https://www.mcbbs.net/template/mcbbs/image/special_photo_bg.png&sign=04224646d1fea004e79606d3b038c84a";
        String body = commonGet(url).body();
        JSONObject first = JSONObject.parseObject(body);
        if (first.getJSONObject("data").getIntValue("has_login") != 1) {
            log.error("未登陆bilibili，无法获取accessKey. body={}", body);
            throw new RuntimeException("未登陆，无法获取accessKey");
        }
        String confirmUri = first.getJSONObject("data").getString("confirm_uri");
        HttpResponse redirect = HttpRequest.head(confirmUri)
                .header("User-Agent", BROWSER_UA_STR)
                .cookie(getCookieStr())
                .execute();
        String location = redirect.header(Header.LOCATION);
        String accessKey = DataUtil.getUrlQueryParam(location, "access_key");
        log.debug("请求获得的accessKey为：{}", accessKey);
        redisUtil.setEx(ACCESS_KEY, accessKey, 29, TimeUnit.DAYS);
        return accessKey;
    }


    /**
     * 上报播放心跳
     *
     * @param start_ts
     * @param aid
     * @param cid
     * @param type                    视频类型，通常都是3
     * @param sub_type
     * @param dt
     * @param realtime
     * @param play_type               0：播放中
     *                                1：开始播放
     *                                2：暂停
     *                                3：继续播放
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
        paramMap.put("mid", GlobalVariables.mid);
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
        paramMap.put("csrf", getCsrf());


        String body = commonPost(url, paramMap).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);
        return jsonObject;
    }


    /**
     * 参考 <a href="https://github.com/magicdawn/bilibili-app-recommend"> bilibili-app-recommend </a> 该项目的实现
     *
     * @return
     */
    public static String getIdx() {
        return System.currentTimeMillis() / 1000 + "0" + String.format("%03d", new Random().nextInt(1000));
    }

    /**
     * 获取csrf
     *
     * @return
     */
    public String getCsrf() {
        return GlobalVariables.cookieMap.getOrDefault("bili_jct", "");
    }


    /**
     * 获取SESSDATA
     *
     * @return
     */
    public String getSessData() {
        return GlobalVariables.cookieMap.getOrDefault("SESSDATA", "");
    }

    /**
     * 完全替换cookie
     *
     * @param cookieStr
     */
    public void replaceCookie(String cookieStr) {
        redisUtil.delete(COOKIES_KEY);
        Map<String, String> map = new HashMap<>(10);
        String[] cookieArr = cookieStr.split(";");
        for (String cookie : cookieArr) {
            String[] split = cookie.split("=");
            map.put(split[0].trim(), split[1].trim());
        }
        GlobalVariables.cookieMap = map;
        //缓存
        redisUtil.hPutAll(COOKIES_KEY, map);
    }

    /**
     * 获取视频tag
     *
     * @param aid
     */
    public List<Tag> getVideoTag(int aid) {

        String url = "https://api.bilibili.com/x/tag/archive/tags";
        String body = commonGet(url, Map.of("aid", aid)).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);

        List<Tag> data = jsonObject.getJSONArray("data")
                .toList(Tag.class);

        return data;
    }

    /**
     * 时间戳 秒
     *
     * @return
     */
    public static long getTs() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前用户信息
     */
    public JSONObject getUserInfo() {
        String url = "https://app.bilibili.com/x/v2/account/myinfo";
        String body = commonGet(url,
                getAppSign(
                        Map.of(
                                "access_key", getAccessKeyByCookie(false),
                                "appkey", THIRD_PART_APPKEY,
                                "ts", String.valueOf(getTs())
                        )
                )
        ).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        return jsonObject;
    }


    /**
     * 获取签名后的参数，返回一个包含签名的map
     *
     * @param params
     * @return
     */
    public static Map<String, Object> getAppSign(Map<String, String> params) {
        HashMap<String, Object> map = new HashMap<>(params);
        // 为请求参数进行 APP 签名
        map.put("appkey", THIRD_PART_APPKEY);
        // 按照 key 重排参数
        Map<String, String> sortedParams = new TreeMap<>(params);
        // 序列化参数
        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append('&');
            }
            queryBuilder
                    .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        String sign = DataUtil.generateMD5(queryBuilder.toString() + THIRD_PART_APPSEC);
        map.put("sign", sign);
        return map;
    }


    /**
     * 获取wbi签名的字符串，返回一个拼接好的urlQuery: wts=xxxx&w_rid=xxxx
     *
     * @param refresh
     * @return
     */
    public Map<String, Object> getWbi(boolean refresh, final Map<String, Object> paramMap) {
        //如果缓存中存在，则直接返回
        HashMap<String, Object> resultMap = new HashMap<>(paramMap);
        String imgKey;
        String subKey;
        if (refresh || redisUtil.get(WBI) == null) {
            String url = "https://api.bilibili.com/x/web-interface/nav";
            String body = commonGet(url).body();
            JSONObject jsonObject = JSONObject.parseObject(body);
            checkRespAndThrow(jsonObject, body);
            JSONObject wbiImg = jsonObject.getJSONObject("data").getJSONObject("wbi_img");

            String imgUrl = wbiImg.getString("img_url");
            String subUrl = wbiImg.getString("sub_url");

            imgKey = imgUrl.substring(imgUrl.lastIndexOf("/") + 1).replace(".png", "");
            subKey = subUrl.substring(subUrl.lastIndexOf("/") + 1).replace(".png", "");

            HashMap<String, Object> map = new HashMap<>();
            map.put("imgKey", imgKey);
            map.put("subKey", subKey);
            //20小时刷新一次
            redisUtil.setEx(WBI,
                    map,
                    20, TimeUnit.HOURS);

        } else {
            Map<String, String> map = (Map<String, String>) redisUtil.get(WBI);
            imgKey = map.get("imgKey");
            subKey = map.get("subKey");
        }

        String mixinKey = getMixinKey(imgKey, subKey);
        resultMap.put("wts", System.currentTimeMillis() / 1000);
        StringJoiner param = new StringJoiner("&");
        //排序 + 拼接字符串
        resultMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> param.add(entry.getKey() + "=" + URLUtil.encode(entry.getValue().toString())));
        String wbiSign = SecureUtil.md5(param + mixinKey);
        resultMap.put("w_rid", wbiSign);

        return resultMap;
    }

    private static String getMixinKey(String imgKey, String subKey) {
        String s = imgKey + subKey;
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            key.append(s.charAt(mixinKeyEncTab[i]));
        }
        return key.toString();
    }


    /**
     * 查询用户投稿的视频
     *
     * @param mid        用户id
     * @param pageNumber 页码
     * @param keyword    搜索关键词
     */
    public PageBean<UserSubmissionVideo> searchUserSubmissionVideo(String mid,
                                                               long pageNumber,
                                                               String keyword
    ) {
        String url = "https://api.bilibili.com/x/space/wbi/arc/search";
        String body = commonGet(url,
                getWbi(false,
                        Map.of(
                                "mid", mid, //用户id
                                "ps", 30,//每页数据大小
                                "tid", 0, //不知具体用途
                                "pn", pageNumber, //页码
                                "keyword", keyword, //不知具体用途
                                "order", "pubdate", //排序方式
                                "platform", "web",
                                "web_location", 1550101,//不知具体用途
                                "order_avoided", true //不知具体用途
                        )
                ),
                Map.of(
                        "Referer", "https://space.bilibili.com/" + mid + "/video",
                        "Origin", "https://space.bilibili.com"
                )
        ).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);

        JSONObject data = jsonObject .getJSONObject("data");
        List<UserSubmissionVideo> userSubmissionVideoList = data
                .getJSONObject("list")
                .getJSONArray("vlist")
                .toList(UserSubmissionVideo.class);

        return new PageBean<UserSubmissionVideo>().setData(userSubmissionVideoList)
                .setPageNum(pageNumber)
                .setPageSize(30)
                .setTotal(data.getJSONObject("page").getIntValue("count"));
    }


    /**
     * 获取视频非常详细的信息
     * view 视频基本信息
     * Card UP主信息
     * Tags 视频的标签信息
     * Reply 视频热评信息
     * Related 相关视频列表
     *
     * @param avid 视频id
     */
    public VideoDetail getVideoDetail(Integer avid) {
        String url = "https://api.bilibili.com/x/web-interface/view/detail";
        String body = commonGet(url, Map.of("aid", avid)).body();

        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(
                jsonObject, body
        );

        JSONObject data = jsonObject.getJSONObject("data");
        VideoDetail videoDetail = data.getJSONObject("View").to(VideoDetail.class);
        videoDetail.setTags(data.getJSONArray("Tags").toList(Tag.class));
        videoDetail.setRelatedVideoList(data.getJSONArray("Related").toList(VideoDetail.class));

        return videoDetail;
    }
}
