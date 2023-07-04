package io.github.cctyl.config;

import cn.hutool.dfa.WordTree;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局变量的存储
 */
@Data
public class GlobalVariables {

    /**
     * 黑名单up主 id列表
     */
    public static Set<String> blackUserIdSet;

    /**
     * 白名单up主id列表
     */
    public static Set<String> whiteUserIdSet;

    /**
     * 黑名单关键词列表
     */
    public static Set<String> blackKeywordSet;

    /**
     * 黑名单关键词树
     */
    public static WordTree blackKeywordTree = new WordTree();

    /**
     * 黑名单分区id列表
     */
    public static Set<String> blackTidSet;

    /**
     * 白名单关键词列表
     */
    public static Set<String> whiteKeyword;

    /**
     * 黑名单关键词树
     */
    public static WordTree whiteKeywordTree = new WordTree();


    /**
     * 白名单分区id列表
     */
    public static Set<String> whiteTidSet;

    /**
     * 黑名单标签列表
     */
    public static Set<String> blackTagSet;

    /**
     * 黑名单标签树
     */
    public static WordTree blackTagTree = new WordTree();




    /**
     * cookie
     */
    public static Map<String, String> cookieMap = new HashMap<>(20);

    /**
     * 播放者用户id
     */
    public static String mid;

    /**
     * 关键词列表
     */
    public static Set<String> keywordSet;


}