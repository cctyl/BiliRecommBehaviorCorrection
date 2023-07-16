package io.github.cctyl.constants;

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
    public static final String ACCESS_KEY = PREFIX + "access_key";

    /**
     * wbi
     */
    public static final String WBI = PREFIX + "wbi";

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
     * 黑名单标签
     */
    public static final String BLACK_TAG_KEY = PREFIX + "black_tags";

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
    public static final String THIRD_PART_APPKEY = "27eb53fc9058f8c3";
    /**
     * 第三方登陆用到的secret
     */
    public static final String THIRD_PART_APPSEC = "c2ed53a74eeefe3cf99fbd01d8c9c375";

    /**
     * 浏览器端的userAgent
     */
    public static final String BROWSER_UA_STR = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0";



}
