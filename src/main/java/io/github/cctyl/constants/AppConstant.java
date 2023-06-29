package io.github.cctyl.constants;

/**
 * 常量
 */
public class AppConstant {
    public static final String PREFIX = "bili:";
    /**
     * redis 存储cookie的key
     */
    public static final String COOKIES_KEY = PREFIX+"cookies";
    /**
     * 关键词列表
     */
    public static final String KEY_WORD_KEY = PREFIX+"keywords";

    /**
     * 黑名单用户id集合
     */
    public static final String BLACK_USER_ID_KEY = PREFIX+"black_user_ids";

    /**
     * 黑名单关键词-视频标题、简介 DFA算法过滤
     */
    public static final String BLACK_KEY_WORD_KEY = PREFIX+"black_keywords";

    /**
     * 黑名单标签
     */
    public static final String BLACK_TAG_KEY = PREFIX+"black_tags";

    /**
     * 黑名单分区id集合
     */
    public static final String BLACK_TID_KEY = PREFIX+"black_tids";

    /**
     * 黑名单分区id集合
     */
    public static final String MID_KEY = PREFIX+"mid";


    /**
     * 最大图片大小 3M
     */
    public static final int PIC_MAX_SIZE = 2097152;



}
