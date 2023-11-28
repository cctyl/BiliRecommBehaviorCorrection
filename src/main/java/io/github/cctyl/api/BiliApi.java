package io.github.cctyl.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.*;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.*;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.domain.constants.ErrorCode;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.cctyl.domain.constants.AppConstant.*;

/**
 * bilibili相关api都在这里
 * 不打算使用封装层数过多的 retrofit
 * restTemplate 对于增加请求体等需求又比较复杂
 */
@Component
@Slf4j
@Order(1)
public class BiliApi {


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
     * 获取请求头信息
     *
     * @return
     */
    public Map<String, List<String>> getHeader(String url) {
        HashMap<String, List<String>> result = new HashMap<>();
        ApiHeader apiHeader = GlobalVariables.getApiHeaderMap().get(url);
        if ( apiHeader != null) {
            apiHeader.getHeaders()
                    .forEach((k, v) -> result.put(k, Collections.singletonList(v)));

        }else {
            //没有匹配的，就返回默认的header
            //GlobalVariables.getCommonHeaderMap()
            //        .forEach((k, v) ->result.put(k,Collections.singletonList(v)) );
            //公共header时，需要修改host
            result.put("Host",Collections.singletonList(DataUtil.getHost(url)));
        }



        return result;
    }

    /**
     * 封装通用的get
     * 携带cookie、ua
     * 记忆cookie
     *
     * @param url
     * @return
     */
    public HttpResponse commonGet(String url) {
        HttpRequest request = HttpRequest.get(url)
                .clearHeaders()
                .header(getHeader(url),true)
                .timeout(10000)
                .cookie(getCookieStr(url));
        HttpResponse response = request
                .execute();
        updateCookie(response);
        log.trace("body={}", response.body());
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
                .clearHeaders()
                .header(getHeader(url),true)
                .form(paramMap)
                .timeout(10000)
                .cookie(getCookieStr(url));
        HttpResponse response = request
                .execute();
        log.trace("body={}", response.body());
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
                .clearHeaders()
                .header(getHeader(url),true)
                .form(paramMap)
                .timeout(10000)
                .cookie(getCookieStr(url));

        otherHeader.forEach(request::header);
        HttpResponse response = request
                .execute();
        log.trace("body={}", response.body());
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
        GlobalHeaders.INSTANCE.clearHeaders();
        HttpRequest request =
                HttpRequest.post(url)
                        .clearHeaders()
                        .header(getHeader(url),true)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .form(paramMap)
                        .timeout(10000)
                        .cookie(getCookieStr(url));
        HttpResponse response = request
                .execute();
        log.trace("body={}", response.body());
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
                .header(getHeader(picUrl))
                .cookie(getCookieStr(picUrl))
                .executeAsync();
        InputStream inputStream = response.bodyStream();
        FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream();
        try (inputStream; fastByteArrayOutputStream) {
            byte[] buff = new byte[1024];
            int totalLength = 0;
            int len;
            while ((len = inputStream.read(buff)) != -1) {
                fastByteArrayOutputStream.write(buff, 0, len);
                totalLength += len;
                if (totalLength > PIC_MAX_SIZE) {
                    log.error("图片过大:{}", picUrl);
                    throw new RuntimeException("图片过大，不采用");
                }
            }
            return fastByteArrayOutputStream.toByteArray();
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
    public String getCookieStr(String url) {


        Map<String,String> cookies  = new HashMap<>();
        ApiHeader apiHeader = GlobalVariables.getApiHeaderMap().get(url);
        if (apiHeader==null){
            //使用 通用的 cookie
            cookies.putAll(GlobalVariables.getCommonCookieMap());
        }else {
            cookies.putAll(apiHeader.getCookies());
        }

        //及时更新的cookie覆盖掉同名key
        cookies.putAll(GlobalVariables.getRefreshCookieMap());

        return cookies.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue() + ";")
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
        Map<String,String> cookieMap = new HashMap<>();
        cookies.forEach(httpCookie -> cookieMap.put(httpCookie.getName(),httpCookie.getValue()));

        GlobalVariables.updateRefreshCookie(cookieMap);
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
                log.error("body={}", jsonObject);
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
     * 根据avid获取视频详情
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
     * 根据bvid获取视频详情
     *
     * @param bvid
     */
    public VideoDetail getVideoCommonInfo(String bvid) {
        String url = "https://api.bilibili.com/x/web-interface/view";
        String body = commonGet(url, Map.of("bvid", bvid)).body();
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

        //如果需要刷新，或者缓存中不存在，则更新一次
        //否则从缓存中取出
        if( refresh || StrUtil.isEmpty(GlobalVariables.getBiliAccessKey())){
            String url = "https://passport.bilibili.com/login/app/third?appkey=" + THIRD_PART_APPKEY + "&api=https://www.mcbbs.net/template/mcbbs/image/special_photo_bg.png&sign=04224646d1fea004e79606d3b038c84a";
            String body = commonGet(url).body();
            JSONObject first = JSONObject.parseObject(body);
            if (first.getJSONObject("data").getIntValue("has_login") != 1) {
                log.error("未登陆bilibili，无法获取accessKey. body={}", body);
                throw new RuntimeException("未登陆，无法获取accessKey");
            }
            String confirmUri = first.getJSONObject("data").getString("confirm_uri");
            HttpResponse redirect = HttpRequest.head(confirmUri)
                    .header(getHeader(url))
                    .cookie(getCookieStr(url))
                    .execute();
            String location = redirect.header(Header.LOCATION);
            String accessKey = DataUtil.getUrlQueryParam(location, "access_key");
            log.debug("请求获得的accessKey为：{}", accessKey);

            GlobalVariables.updateAccessKey(accessKey);
            return accessKey;
        }else {
            log.debug("缓存中得到了accessKey={}", GlobalVariables.getBiliAccessKey());
            return GlobalVariables.getBiliAccessKey();
        }

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
        paramMap.put("mid", GlobalVariables.getMID());
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
        return GlobalVariables.getRefreshCookieMap().getOrDefault("bili_jct", "");
    }


    /**
     * 获取SESSDATA
     *
     * @return
     */
    public String getSessData() {
        return GlobalVariables.getRefreshCookieMap().getOrDefault("SESSDATA", "");
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
        String sign = DataUtil.generateMD5(queryBuilder + THIRD_PART_APPSEC);
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

        if (refresh || GlobalVariables.getImgKey() == null || GlobalVariables.getSubKey()==null) {
            String url = "https://api.bilibili.com/x/web-interface/nav";
            String body = commonGet(url).body();
            JSONObject jsonObject = JSONObject.parseObject(body);
            checkRespAndThrow(jsonObject, body);
            JSONObject wbiImg = jsonObject.getJSONObject("data").getJSONObject("wbi_img");

            String imgUrl = wbiImg.getString("img_url");
            String subUrl = wbiImg.getString("sub_url");

            imgKey = imgUrl.substring(imgUrl.lastIndexOf("/") + 1).replace(".png", "");
            subKey = subUrl.substring(subUrl.lastIndexOf("/") + 1).replace(".png", "");


            //20小时刷新一次
            GlobalVariables.updateWbi(imgKey,subKey);


        } else {

            imgKey = GlobalVariables.getImgKey();
            subKey = GlobalVariables.getSubKey();
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
     * @param pageNumber 页码 1开始
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
                                "keyword", keyword, //搜索该用户视频使用的关键词
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

        JSONObject data = jsonObject.getJSONObject("data");
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
    public VideoDetail getVideoDetail(String bvid ) {
        String url = "https://api.bilibili.com/x/web-interface/view/detail";
        String body = commonGet(url, Map.of("bvid", bvid )).body();

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


    /**
     * 获取指定分区内的最新视频
     *
     * @param pageNum 页码
     * @param tid     分区id
     */
    public List<VideoDetail> getRegionLatestVideo(
            int pageNum,
            int tid
    ) {
        String url = "https://api.bilibili.com/x/web-interface/dynamic/region";
        String body = commonGet(url, Map.of(
                "rid", tid,
                "ps", 20,
                "pn", pageNum
        ))
                .body();

        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);

        return jsonObject.getJSONObject("data")
                .getJSONArray("archives")
                .toList(VideoDetail.class);


    }


    /**
     * 获取指定分区的视频排行榜数据
     * fixme: 该接口似乎不支持 主分区下的子分区
     *
     * @param tid
     */
    public List<VideoDetail> getRankByTid(int tid) {

        String url = "https://api.bilibili.com/x/web-interface/ranking/v2";
        String body = commonGet(url, Map.of(
                "rid", tid,
                "type", "all"
        ))
                .body();

        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);
        return jsonObject.getJSONObject("data")
                .getJSONArray("list")
                .toList(VideoDetail.class);
    }

    /**
     * 获取所有的分区名单，并以树结构返回
     *
     * @return
     */
    public List<Region> getAllRegion(boolean tree) {
        ArrayList<Region> regions = new ArrayList<>();
        regions.add(new Region(1, "douga", "动画(主分区)", "", "/v/douga", null));
        regions.add(new Region(24, "mad", "MAD·AMV", "具有一定创作度的动/静画二次创作视频", "/v/douga/mad", 1));
        regions.add(new Region(25, "mmd", "MMD·3D", "使用mmd（mikumikudance）和其他3d建模类软件制作的视频", "/v/douga/mmd", 1));
        regions.add(new Region(47, "voice", "短片·手书·配音", "追求个人特色和创意表达的自制动画短片、手书（绘）及acgn相关配音", "/v/douga/voice", 1));
        regions.add(new Region(210, "garage_kit", "手办·模玩", "手办模玩的测评、改造或其他衍生内容", "/v/douga/garage_kit", 1));
        regions.add(new Region(86, "tokusatsu", "特摄", "特摄相关衍生视频", "/v/douga/tokusatsu", 1));
        regions.add(new Region(253, "acgntalks", "动漫杂谈", "以谈话形式对ACGN文化圈进行的鉴赏、吐槽、评点、解说、推荐、科普等内容", "/v/douga/acgntalks", 1));
        regions.add(new Region(27, "other", "综合", "以动画及动画相关内容为素材，包括但不仅限于音频替换、恶搞改编、排行榜等内容", "/v/douga/other", 1));
        regions.add(new Region(13, "anime", "番剧(主分区)", "", "/anime", null));
        regions.add(new Region(51, "information", "资讯", "以动画/轻小说/漫画/杂志为主的资讯内容，PV/CM/特报/冒头/映像/预告", "/v/anime/information", 13));
        regions.add(new Region(152, "offical", "官方延伸", "以动画番剧及声优为主的EVENT/生放送/DRAMA/RADIO/LIVE/特典/冒头等", "/v/anime/offical", 13));
        regions.add(new Region(32, "finish", "完结动画", "已完结TV/WEB动画及其独立系列，旧剧场版/OVA/SP/未放送", "/v/anime/finish", 13));
        regions.add(new Region(33, "serial", "连载动画", "连载中TV/WEB动画，新剧场版/OVA/SP/未放送/小剧场", "/v/anime/serial", 13));
        regions.add(new Region(167, "guochuang", "国创(主分区)", "", "/guochuang", null));
        regions.add(new Region(153, "chinese", "国产动画", "国产连载动画，国产完结动画", "/v/guochuang/chinese", 167));
        regions.add(new Region(168, "original", "国产原创相关", "以国产动画、漫画、小说为素材的二次创作", "/v/guochuang/original", 167));
        regions.add(new Region(169, "puppetry", "布袋戏", "布袋戏以及相关剪辑节目", "/v/guochuang/puppetry", 167));
        regions.add(new Region(170, "information", "资讯", "原创国产动画、漫画的相关资讯、宣传节目等", "/v/guochuang/information", 167));
        regions.add(new Region(195, "motioncomic", "动态漫·广播剧", "国产动态漫画、有声漫画、广播剧", "/v/guochuang/motioncomic", 167));
        regions.add(new Region(3, "music", "音乐(主分区)", "", "/v/music", null));
        regions.add(new Region(28, "original", "原创音乐", "原创歌曲及纯音乐，包括改编、重编曲及remix", "/v/music/original", 3));
        regions.add(new Region(31, "cover", "翻唱", "对曲目的人声再演绎视频", "/v/music/cover", 3));
        regions.add(new Region(30, "vocaloid", "VOCALOID·UTAU", "以vocaloid等歌声合成引擎为基础，运用各类音源进行的创作", "/v/music/vocaloid", 3));
        regions.add(new Region(59, "perform", "演奏", "乐器和非传统乐器器材的演奏作品", "/v/music/perform", 3));
        regions.add(new Region(193, "mv", "MV", "为音乐作品配合拍摄或制作的音乐录影带（music video），以及自制拍摄、剪辑、翻拍mv", "/v/music/mv", 3));
        regions.add(new Region(29, "live", "音乐现场", "音乐表演的实况视频，包括官方/个人拍摄的综艺节目、音乐剧、音乐节、演唱会等", "/v/music/live", 3));
        regions.add(new Region(130, "other", "音乐综合", "所有无法被收纳到其他音乐二级分区的音乐类视频", "/v/music/other", 3));
        regions.add(new Region(243, "commentary", "乐评盘点", "音乐类新闻、盘点、点评、reaction、榜单、采访、幕后故事、唱片开箱等", "/v/music/commentary", 3));
        regions.add(new Region(244, "tutorial", "音乐教学", "以音乐教学为目的的内容", "/v/music/tutorial", 3));
        regions.add(new Region(194, "electronic", "电音(已下线)", "以电子合成器、音乐软体等产生的电子声响制作的音乐", "/v/music/electronic", 3));
        regions.add(new Region(129, "dance", "舞蹈(主分区)", "", "/v/dance", null));
        regions.add(new Region(20, "otaku", "宅舞", "与acg相关的翻跳、原创舞蹈", "/v/dance/otaku", 129));
        regions.add(new Region(154, "three_d", "舞蹈综合", "收录无法定义到其他舞蹈子分区的舞蹈视频", "/v/dance/three_d", 129));
        regions.add(new Region(156, "demo", "舞蹈教程", "镜面慢速，动作分解，基础教程等具有教学意义的舞蹈视频", "/v/dance/demo", 129));
        regions.add(new Region(198, "hiphop", "街舞", "收录街舞相关内容，包括赛事现场、舞室作品、个人翻跳、freestyle等", "/v/dance/hiphop", 129));
        regions.add(new Region(199, "star", "明星舞蹈", "国内外明星发布的官方舞蹈及其翻跳内容", "/v/dance/star", 129));
        regions.add(new Region(200, "china", "中国舞", "传承中国艺术文化的舞蹈内容，包括古典舞、民族民间舞、汉唐舞、古风舞等", "/v/dance/china", 129));
        regions.add(new Region(4, "game", "游戏(主分区)", "", "/v/game", null));
        regions.add(new Region(17, "stand_alone", "单机游戏", "以所有平台（pc、主机、移动端）的单机或联机游戏为主的视频内容，包括游戏预告、cg、实况解说及相关的评测、杂谈与视频剪辑等", "/v/game/stand_alone", 4));
        regions.add(new Region(171, "esports", "电子竞技", "具有高对抗性的电子竞技游戏项目，其相关的赛事、实况、攻略、解说、短剧等视频。", "/v/game/esports", 4));
        regions.add(new Region(172, "mobile", "手机游戏", "以手机及平板设备为主要平台的游戏，其相关的实况、攻略、解说、短剧、演示等视频。", "/v/game/mobile", 4));
        regions.add(new Region(65, "online", "网络游戏", "由网络运营商运营的多人在线游戏，以及电子竞技的相关游戏内容。包括赛事、攻略、实况、解说等相关视频", "/v/game/online", 4));
        regions.add(new Region(173, "board", "桌游棋牌", "桌游、棋牌、卡牌对战等及其相关电子版游戏的实况、攻略、解说、演示等视频。", "/v/game/board", 4));
        regions.add(new Region(121, "gmv", "GMV", "由游戏素材制作的mv视频。以游戏内容或cg为主制作的，具有一定创作程度的mv类型的视频", "/v/game/gmv", 4));
        regions.add(new Region(136, "music", "音游", "各个平台上，通过配合音乐与节奏而进行的音乐类游戏视频", "/v/game/music", 4));
        regions.add(new Region(19, "mugen", "Mugen", "以mugen引擎为平台制作、或与mugen相关的游戏视频", "/v/game/mugen", 4));
        regions.add(new Region(36, "knowledge", "知识(主分区)", "", "/v/knowledge", null));
        regions.add(new Region(201, "science", "科学科普", "回答你的十万个为什么", "/v/knowledge/science", 36));
        regions.add(new Region(124, "social_science", "社科·法律·心理(原社科人文、原趣味科普人文)", "基于社会科学、法学、心理学展开或个人观点输出的知识视频", "/v/knowledge/social_science", 36));
        regions.add(new Region(228, "humanity_history", "人文历史", "看看古今人物，聊聊历史过往，品品文学典籍", "/v/knowledge/humanity_history", 36));
        regions.add(new Region(207, "business", "财经商业", "说金融市场，谈宏观经济，一起畅聊商业故事", "/v/knowledge/finance", 36));
        regions.add(new Region(208, "campus", "校园学习", "老师很有趣，学生也有才，我们一起搞学习", "/v/knowledge/campus", 36));
        regions.add(new Region(209, "career", "职业职场", "职业分享、升级指南，一起成为最有料的职场人", "/v/knowledge/career", 36));
        regions.add(new Region(229, "design", "设计·创意", "天马行空，创意设计，都在这里", "/v/knowledge/design", 36));
        regions.add(new Region(122, "skill", "野生技术协会", "技能党集合，是时候展示真正的技术了", "/v/knowledge/skill", 36));
        regions.add(new Region(39, "speech_course", "演讲·公开课(已下线)", "涨知识的好地方，给爱学习的你", "/v/technology/speech_course", 36));
        regions.add(new Region(96, "military", "星海(已下线)", "军事类内容的圣地", "/v/technology/military", 36));
        regions.add(new Region(98, "mechanical", "机械(已下线)", "机械设备展示或制作视频", "/v/technology/mechanical", 36));
        regions.add(new Region(188, "tech", "科技(主分区)", "", "/v/tech", null));
        regions.add(new Region(95, "digital", "数码(原手机平板)", "科技数码产品大全，一起来做发烧友", "/v/tech/digital", 188));
        regions.add(new Region(230, "application", "软件应用", "超全软件应用指南", "/v/tech/application", 188));
        regions.add(new Region(231, "computer_tech", "计算机技术", "研究分析、教学演示、经验分享......有关计算机技术的都在这里", "/v/tech/computer_tech", 188));
        regions.add(new Region(232, "industry", "科工机械 (原工业·工程·机械)", "前方高能，机甲重工即将出没", "/v/tech/industry", 188));
        regions.add(new Region(233, "diy", "极客DIY", "炫酷技能，极客文化，硬核技巧，准备好你的惊讶", "/v/tech/diy", 188));
        regions.add(new Region(189, "pc", "电脑装机(已下线)", "电脑、笔记本、装机配件、外设和软件教程等相关视频", "/v/digital/pc", 188));
        regions.add(new Region(190, "photography", "摄影摄像(已下线)", "摄影摄像器材、拍摄剪辑技巧、拍摄作品分享等相关视频", "/v/digital/photography", 188));
        regions.add(new Region(191, "intelligence_av", "影音智能(已下线)", "影音设备、智能产品等相关视频", "/v/digital/intelligence_av", 188));
        regions.add(new Region(234, "sports", "运动(主分区)", "", "/v/sports", null));
        regions.add(new Region(235, "basketball", "篮球", "与篮球相关的视频，包括但不限于篮球赛事、教学、评述、剪辑、剧情等相关内容", "/v/sports/basketball", 234));
        regions.add(new Region(249, "football", "足球", "与足球相关的视频，包括但不限于足球赛事、教学、评述、剪辑、剧情等相关内容", "/v/sports/football", 234));
        regions.add(new Region(164, "aerobics", "健身", "与健身相关的视频，包括但不限于瑜伽、crossfit、健美、力量举、普拉提、街健等相关内容", "/v/sports/aerobics", 234));
        regions.add(new Region(236, "athletic", "竞技体育", "与竞技体育相关的视频，包括但不限于乒乓、羽毛球、排球、赛车等竞技项目的赛事、评述、剪辑、剧情等相关内容", "/v/sports/culture", 234));
        regions.add(new Region(237, "culture", "运动文化", "与运动文化相关的视频，包络但不限于球鞋、球衣、球星卡等运动衍生品的分享、解读，体育产业的分析、科普等相关内容", "/v/sports/culture", 234));
        regions.add(new Region(238, "comprehensive", "运动综合", "与运动综合相关的视频，包括但不限于钓鱼、骑行、滑板等日常运动分享、教学、Vlog等相关内容", "/v/sports/comprehensive", 234));
        regions.add(new Region(223, "car", "汽车(主分区)", "", "/v/car", null));
        regions.add(new Region(245, "racing", "赛车", "f1等汽车运动相关", "/v/car/racing", 223));
        regions.add(new Region(246, "modifiedvehicle", "改装玩车", "汽车文化及改装车相关内容，包括改装车、老车修复介绍、汽车聚会分享等内容", "/v/car/modifiedvehicle", 223));
        regions.add(new Region(247, "newenergyvehicle", "新能源车", "新能源汽车相关内容，包括电动汽车、混合动力汽车等车型种类，包含不限于新车资讯、试驾体验、专业评测、技术解读、知识科普等内容", "/v/car/newenergyvehicle", 223));
        regions.add(new Region(248, "touringcar", "房车", "房车及营地相关内容，包括不限于产品介绍、驾驶体验、房车生活和房车旅行等内容", "/v/car/touringcar", 223));
        regions.add(new Region(240, "motorcycle", "摩托车", "骑士们集合啦", "/v/car/motorcycle", 223));
        regions.add(new Region(227, "strategy", "购车攻略", "丰富详实的购车建议和新车体验", "/v/car/strategy", 223));
        regions.add(new Region(176, "life", "汽车生活", "分享汽车及出行相关的生活体验类视频", "/v/car/life", 223));
        regions.add(new Region(224, "culture", "汽车文化(已下线)", "车迷的精神圣地，包括汽车赛事、品牌历史、汽车改装、经典车型和汽车模型等", "/v/car/culture", 223));
        regions.add(new Region(225, "geek", "汽车极客(已下线)", "汽车硬核达人聚集地，包括DIY造车、专业评测和技术知识分享", "/v/car/geek", 223));
        regions.add(new Region(226, "smart", "智能出行(已下线)", "探索新能源汽车和未来智能出行的前沿阵地", "/v/car/smart", 223));
        regions.add(new Region(160, "life", "生活(主分区)", "", "/v/life", null));
        regions.add(new Region(138, "funny", "搞笑", "各种沙雕有趣的搞笑剪辑，挑战，表演，配音等视频", "/v/life/funny", 160));
        regions.add(new Region(250, "travel", "出行", "为达到观光游览、休闲娱乐为目的的远途旅行、中近途户外生活、本地探店", "/v/life/travel", 160));
        regions.add(new Region(251, "rurallife", "三农", "分享美好农村生活", "/v/life/rurallife", 160));
        regions.add(new Region(239, "home", "家居房产", "与买房、装修、居家生活相关的分享", "/v/life/home", 160));
        regions.add(new Region(161, "handmake", "手工", "手工制品的制作过程或成品展示、教程、测评类视频", "/v/life/handmake", 160));
        regions.add(new Region(162, "painting", "绘画", "绘画过程或绘画教程，以及绘画相关的所有视频", "/v/life/painting", 160));
        regions.add(new Region(21, "daily", "日常", "记录日常生活，分享生活故事", "/v/life/daily", 160));
        regions.add(new Region(76, "food", "美食圈(重定向)", "美食鉴赏&料理制作教程", "/v/life/food", 160));
        regions.add(new Region(75, "animal", "动物圈(重定向)", "萌萌的动物都在这里哦", "/v/life/animal", 160));
        regions.add(new Region(163, "sports", "运动(重定向)", "运动相关的记录、教程、装备评测和精彩瞬间剪辑视频", "/v/life/sports", 160));
        regions.add(new Region(176, "automobile", "汽车(重定向)", "专业汽车资讯，分享车生活", "/v/life/automobile", 160));
        regions.add(new Region(174, "other", "其他(已下线)", "对于分区归属不明的视频进行归纳整合的特定分区", "/v/life/other", 160));
        regions.add(new Region(211, "food", "美食(主分区)", "", "/v/food", null));
        regions.add(new Region(76, "make", "美食制作(原[生活]->[美食圈])", "学做人间美味，展示精湛厨艺", "/v/food/make", 211));
        regions.add(new Region(212, "detective", "美食侦探", "寻找美味餐厅，发现街头美食", "/v/food/detective", 211));
        regions.add(new Region(213, "measurement", "美食测评", "吃货世界，品尝世间美味", "/v/food/measurement", 211));
        regions.add(new Region(214, "rural", "田园美食", "品味乡野美食，寻找山与海的味道", "/v/food/rural", 211));
        regions.add(new Region(215, "record", "美食记录", "记录一日三餐，给生活添一点幸福感", "/v/food/record", 211));
        regions.add(new Region(217, "animal", "动物圈(主分区)", "", "/v/animal", null));
        regions.add(new Region(218, "cat", "喵星人", "喵喵喵喵喵", "/v/animal/cat", 217));
        regions.add(new Region(219, "dog", "汪星人", "汪汪汪汪汪", "/v/animal/dog", 217));
        regions.add(new Region(220, "panda", "大熊猫", "芝麻汤圆营业中", "/v/animal/panda", 217));
        regions.add(new Region(221, "wild_animal", "野生动物", "内有“猛兽”出没", "/v/animal/wild_animal", 217));
        regions.add(new Region(222, "reptiles", "爬宠", "鳞甲有灵", "/v/animal/reptiles", 217));
        regions.add(new Region(75, "animal_composite", "动物综合", "收录除上述子分区外，其余动物相关视频以及非动物主体或多个动物主体的动物相关延伸内容", "/v/animal/animal_composite", 217));
        regions.add(new Region(119, "kichiku", "鬼畜(主分区)", "", "/v/kichiku", null));
        regions.add(new Region(22, "guide", "鬼畜调教", "使用素材在音频、画面上做一定处理，达到与bgm一定的同步感", "/v/kichiku/guide", 119));
        regions.add(new Region(26, "mad", "音MAD", "使用素材音频进行一定的二次创作来达到还原原曲的非商业性质稿件", "/v/kichiku/mad/v/kichiku/mad", 119));
        regions.add(new Region(126, "manual_vocaloid", "人力VOCALOID", "将人物或者角色的无伴奏素材进行人工调音，使其就像VOCALOID一样歌唱的技术", "/v/kichiku/manual_vocaloid", 119));
        regions.add(new Region(216, "theatre", "鬼畜剧场", "使用素材进行人工剪辑编排的有剧情的作品", "/v/kichiku/theatre", 119));
        regions.add(new Region(127, "course", "教程演示", "鬼畜相关的科普和教程演示", "/v/kichiku/course", 119));
        regions.add(new Region(155, "fashion", "时尚(主分区)", "", "/v/fashion", null));
        regions.add(new Region(157, "makeup", "美妆护肤", "彩妆护肤、美甲美发、仿妆、医美相关内容分享或产品测评", "/v/fashion/makeup", 155));
        regions.add(new Region(252, "cos", "仿妆cos", "对二次元、三次元人物角色进行模仿、还原、展示、演绎的内容", "/v/fashion/cos", 155));
        regions.add(new Region(158, "clothing", "穿搭", "穿搭风格、穿搭技巧的展示分享，涵盖衣服、鞋靴、箱包配件、配饰（帽子、钟表、珠宝首饰）等", "/v/fashion/clothing", 155));
        regions.add(new Region(159, "catwalk", "时尚潮流", "时尚街拍、时装周、时尚大片，时尚品牌、潮流等行业相关记录及知识科普", "/v/fashion/catwalk", 155));
        regions.add(new Region(164, "aerobics", "健身(重定向)", "器械、有氧、拉伸运动等，以达到强身健体、减肥瘦身、形体塑造目的", "/v/fashion/aerobics", 155));
        regions.add(new Region(192, "trends", "风尚标(已下线)", "时尚明星专访、街拍、时尚购物相关知识科普", "/v/fashion/trends", 155));
        regions.add(new Region(202, "information", "资讯(主分区)", "", "/v/information", null));
        regions.add(new Region(203, "hotspot", "热点", "全民关注的时政热门资讯", "/v/information/hotspot", 202));
        regions.add(new Region(204, "global", "环球", "全球范围内发生的具有重大影响力的事件动态", "/v/information/global", 202));
        regions.add(new Region(205, "social", "社会", "日常生活的社会事件、社会问题、社会风貌的报道", "/v/information/social", 202));
        regions.add(new Region(206, "multiple", "综合", "除上述领域外其它垂直领域的综合资讯", "/v/information/multiple", 202));
        regions.add(new Region(165, "ad", "广告(主分区)", "", "/v/ad", null));
        regions.add(new Region(166, "ad", "广告(已下线)", "", "/v/ad/ad", 165));
        regions.add(new Region(5, "ent", "娱乐(主分区)", "", "/v/ent", null));
        regions.add(new Region(71, "variety", "综艺", "所有综艺相关，全部一手掌握！", "/v/ent/variety", 5));
        regions.add(new Region(241, "talker", "娱乐杂谈", "娱乐人物解读、娱乐热点点评、娱乐行业分析", "/v/ent/talker", 5));
        regions.add(new Region(242, "fans", "粉丝创作", "粉丝向创作视频", "/v/ent/fans", 5));
        regions.add(new Region(137, "celebrity", "明星综合", "娱乐圈动态、明星资讯相关", "/v/ent/celebrity", 5));
        regions.add(new Region(131, "korea", "Korea相关(已下线)", "Korea相关音乐、舞蹈、综艺等视频", "/v/ent/korea", 5));
        regions.add(new Region(181, "cinephile", "影视(主分区)", "", "/v/cinephile", null));
        regions.add(new Region(182, "cinecism", "影视杂谈", "影视评论、解说、吐槽、科普等", "/v/cinephile/cinecism", 181));
        regions.add(new Region(183, "montage", "影视剪辑", "对影视素材进行剪辑再创作的视频", "/v/cinephile/montage", 181));
        regions.add(new Region(85, "shortfilm", "小剧场", "有场景、有剧情的演绎类内容", "/v/cinephile/shortfilm", 181));
        regions.add(new Region(184, "trailer_info", "预告·资讯", "影视类相关资讯，预告，花絮等视频", "/v/cinephile/trailer_info", 181));
        regions.add(new Region(177, "documentary", "纪录片(主分区)", "", "/documentary", null));
        regions.add(new Region(37, "history", "人文·历史", "除宣传片、影视剪辑外的，人文艺术历史纪录剧集或电影、预告、花絮、二创、5分钟以上纪录短片", "/v/documentary/history", 177));
        regions.add(new Region(178, "science", "科学·探索·自然", "除演讲、网课、教程外的，科学探索自然纪录剧集或电影、预告、花絮、二创、5分钟以上纪录短片", "/v/documentary/science", 177));
        regions.add(new Region(179, "military", "军事", "除时政军事新闻外的，军事纪录剧集或电影、预告、花絮、二创、5分钟以上纪录短片", "/v/documentary/military", 177));
        regions.add(new Region(180, "travel", "社会·美食·旅行", "除VLOG、风光摄影外的，社会美食旅行纪录剧集或电影、预告、花絮、二创、5分钟以上纪录短片", "/v/documentary/travel", 177));
        regions.add(new Region(23, "movie", "电影(主分区)", "", "/movie", null));
        regions.add(new Region(147, "chinese", "华语电影", "", "/v/movie/chinese", 23));
        regions.add(new Region(145, "west", "欧美电影", "", "/v/movie/west", 23));
        regions.add(new Region(146, "japan", "日本电影", "", "/v/movie/japan", 23));
        regions.add(new Region(83, "movie", "其他国家", "", "/v/movie/movie", 23));
        regions.add(new Region(11, "tv", "电视剧(主分区)", "", "/tv", null));
        regions.add(new Region(185, "mainland", "国产剧", "", "/v/tv/mainland", 11));
        regions.add(new Region(187, "overseas", "海外剧", "", "/v/tv/overseas", 11));

        if (tree) {
            return Region.buildTree(regions);
        } else {
            return regions;
        }
    }


    /**
     * 根据短网址获得视频的bvid
     * @param shortUrl
     * @return
     */
    public String getBvidByShortUrl(String shortUrl){
        HttpResponse response = commonGet(shortUrl);
        String pcUrl = response.body().replaceAll("<a href=\"", "")
                .replaceAll("\">Found</a>.", "");
        return getBvidByUrl(pcUrl);
    }


    /**
     * 根据pc端url获得bvid
     * @param url
     * @return
     */
    public String getBvidByUrl(String url){
        String s = url.replaceAll("https://www.bilibili.com/video/", "");
        s = s.substring(0,s.indexOf("?"));
        return s;
    }

    /**
     * 根据pc端url获得avid
     * @param url
     * @return
     */
    public Integer getAvidByUrl(String url){
        return DataUtil.bvidToAid(getBvidByUrl(url));
    }


    /**
     * 根据短链接获得avid
     * @param shortUrl
     * @return
     */
    public Integer getAvidByShortUrl(String shortUrl){
        String bvid = getBvidByShortUrl(shortUrl);
        return DataUtil.bvidToAid(bvid);
    }


    /**
     * 反馈不喜欢原因
     * @param dislikeReason
     * @param dislikeMid
     * @param dislikeTid
     * @param dislikeTagId
     */
    public void dislikeByReason(DislikeReason dislikeReason,
                                String dislikeMid,
                                Integer dislikeTid,
                                Integer dislikeTagId,
                                Integer aid
                                ) {

        String url = "https://app.bilibili.com/x/feed/dislike";
        Map<String,String> map = new HashMap<>();
        map.put( "access_key", getAccessKeyByCookie(false));
        map.put( "goto", "av");
        map.put( "id", String.valueOf(aid));
        map.put( "mid", dislikeMid);
        map.put( "reason_id", String.valueOf(dislikeReason.getId()));
        map.put( "rid", String.valueOf(dislikeTid));
        map.put( "tag_id", String.valueOf(dislikeTagId));
        map.put( "ts", String.valueOf(getTs()));
        map.put( "platform","android");
        map.put( "mobi_app","android");
        map.put( "build","7110300");
        map.put("appkey", ANDROID_PINK_APPKEY);
        Map<String, Object> paramSignMap = getAppSign(map);

        String body = commonGet(url, paramSignMap).body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        checkRespAndThrow(jsonObject, body);


    }
}
