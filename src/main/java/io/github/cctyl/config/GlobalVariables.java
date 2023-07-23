package io.github.cctyl.config;

import cn.hutool.dfa.WordTree;
import io.github.cctyl.entity.WhitelistRule;
import io.github.cctyl.utils.RedisUtil;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.cctyl.constants.AppConstant.*;

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


    /**
     * 白名单关键词列表
     */
    public static List<WhitelistRule> whiteKeyWordList;

    /**
     * 更新blackUserIdSet
     * @param param
     */
    public static void updateBlackUserId(Set<String> param){
        GlobalVariables.blackUserIdSet = param;
        BeanProvider.getRedisUtil().delete(BLACK_USER_ID_KEY);
        BeanProvider.getRedisUtil().sAdd(BLACK_USER_ID_KEY,GlobalVariables.blackUserIdSet.toArray());
    }

    /**更新blackKeywordSet
     *
     * @param param
     */
    public static void updateBlackKeyword(Set<String> param){
        GlobalVariables.blackKeywordSet = param;
        BeanProvider.getRedisUtil().delete(BLACK_KEY_WORD_KEY);
        BeanProvider.getRedisUtil().sAdd(BLACK_KEY_WORD_KEY,GlobalVariables.blackKeywordSet.toArray());
        GlobalVariables.blackKeywordTree.addWords(GlobalVariables.blackKeywordSet);
    }

    /**更新blackKeywordSet
     *
     * @param param
     */
    public static void updateBlackTagSet(Set<String> param){
        GlobalVariables.blackTagSet = param;
        BeanProvider.getRedisUtil().delete(BLACK_TAG_KEY);
        BeanProvider.getRedisUtil().sAdd(BLACK_TAG_KEY,GlobalVariables.blackTagSet.toArray());
        GlobalVariables.blackTagTree.addWords(GlobalVariables.blackTagSet);
    }

}