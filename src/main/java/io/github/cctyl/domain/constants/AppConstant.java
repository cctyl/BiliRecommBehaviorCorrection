package io.github.cctyl.domain.constants;

/**
 * 常量
 */
public class AppConstant {
    public static final String PREFIX = "bili:";
    /**
     * redis 存储cookie的key
     */
    public static final String COOKIES_KEY = PREFIX + "cookies";

    /**
     * accessKey
     */
    public static final String BILI_ACCESS_KEY = PREFIX + "access_key";
    public static final String REASON_FORMAT= "%s=%s,匹配:%s,  <br/> ";
    /**
     * wbi
     */
    public static final String WBI = PREFIX + "wbi";
    public static final String IMG_KEY = PREFIX + "imgKey";
    public static final String SUB_KEY = PREFIX + "subKey";
    public static final String BILITICKET = "bili_ticket";
    public static final String B_NUT = "b_nut";
    public static final String BUVID3 = "buvid3";
    public static final String BUVID4 = "buvid4";

    /**
     * 每个api对应需要用到的header
     */
    public static final String API_HEADER_MAP = PREFIX + "api_header";

    /**
     * 当找不到对应的header时，使用公共的header
     */
    public static final String COMMON_COOKIE_MAP = PREFIX + "common_cookie";
    /**
     * 找不到header时使用公共的header
     */
    public static final String COMMON_HEADER_MAP = PREFIX + "common_header";


    /**
     * 关键词列表
     */
    public static final String KEY_WORD_KEY = PREFIX + "keywords";

    /**
     * 黑名单用户id集合
     */
    public static final String BLACK_USER_ID_KEY = PREFIX + "black_user_ids";

    /**
     * 白名单用户id集合
     */
    public static final String WHITE_USER_ID_KEY = PREFIX + "white_user_ids";

    /**
     * 黑名单关键词-视频标题、简介 DFA算法过滤
     */
    public static final String BLACK_KEY_WORD_KEY = PREFIX + "black_keywords";

    /**
     * 可疑的cookie
     */
    public static final String SUSPICIOUS_COOKIE_KEY = PREFIX + "suspicious_cookie";

    /**
     * 黑名单标签
     */
    public static final String BLACK_TAG_KEY = PREFIX + "black_tags";

    /**
     * 黑名单关键词缓存
     */
    public static final String BLACK_KEYWORD_CACHE = PREFIX + "black_keyword_cache";

    /**
     * 黑名单分区名缓存
     */
    public static final String BLACK_TAG_NAME_CACHE = PREFIX + "black_tag_name_cache";



    /**
     * 黑名单分区id集合
     */
    public static final String BLACK_TID_KEY = PREFIX + "black_tids";

    /**
     * 白名单分区id集合
     */
    public static final String WHITE_TID_KEY = PREFIX + "white_tids";

    /**
     * 黑名单分区id集合
     */
    public static final String MID_KEY = PREFIX + "mid";
    /**
     * 处理过的视频记录
     */
    public static final String HANDLE_VIDEO_DETAIL_KEY = PREFIX + "handle_video_detail_list";
    public static final String HANDLE_VIDEO_ID_KEY = PREFIX + "handle_video_id_list";

    /**
     * 将要处理的视频的缓存
     */
    public static final String READY_HANDLE_VIDEO = PREFIX + "ready_handle_video";
    public static final String READY_HANDLE_VIDEO_ID = PREFIX + "ready_handle_video_id";

    /**
     * 忽略的黑名单关键词
     */
    public static final String IGNORE_BLACK_KEYWORD = PREFIX + "ignore_black_keyword";

    /**
     * 忽略的白名单关键词
     */
    public static final String IGNORE_WHITE_KEYWORD = PREFIX + "ignore_white_keyword";

    /**
     * 白名单关键词列表
     */
    public static final String WHITE_LIST_RULE_KEY = PREFIX + "white_list_rule";

    /**
     * 百度accessKey
     */
    public static final String BAIDU_ASK_KEY = "baidu_accesskey";

    public static final String STOP_WORDS_KEY = "stop_words";

    /**
     * 最大图片大小 3M
     */
    public static final int PIC_MAX_SIZE = 2097152;

    /**
     * android 粉 appkey
     */
    public static final String ANDROID_PINK_APPKEY = "1d8b6e7d45233436";
    /**
     * android 粉 appsec
     */
    public static final String ANDROID_PINK_APPSEC = "560c52ccd288fed045859ed18bffd973";


    /**
     * 第三方登陆用到的key
     */
    public static final String THIRD_PART_APPKEY = "783bbb7264451d82";
    /**
     * 第三方登陆用到的secret
     */
    public static final String THIRD_PART_APPSEC = "2653583c8873dea268ab9386918b1d65";

    /**
     * 浏览器端的userAgent
     */
    public static final String BROWSER_UA_STR = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0";


    /**
     * 初次启动时间
     */
    public static final String FIRST_START_TIME = "firstStartTime";

    /**
     * 是否初次启动
     */
    public static final String FIRST_USE = "firstUse";

    public static final String BAIDU_CLIENT_ID = "baidu_client_id";
    public static final String BAIDU_CLIENT_SECRET = "baidu_client_secret";
    public static final String MIN_PLAY_SECOND = "minPlaySecond";
    public static final String CRON = "cron";
}
