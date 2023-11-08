package io.github.cctyl.config;

import cn.hutool.dfa.WordTree;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.pojo.WhitelistRule;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.service.BlackRuleService;
import io.github.cctyl.service.WhiteRuleService;
import io.github.cctyl.utils.RedisUtil;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * 全局变量的存储
 */
@Data
@Component
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
    public static List<WhitelistRule> whitelistRules;

    /**
     * ApiHeader 相关
     */
    public static Map<String, ApiHeader> apiHeaderMap = new HashMap<>();
    public static Map<String, String> commonCookieMap = new HashMap<>();
    public static Map<String, String> commonHeaderMap = new HashMap<>();

    private static RedisUtil redisUtil;
    private static RedisTemplate<String, Object> redisTemplate;
    private static BiliService biliService;
    private static BlackRuleService blackRuleService;
    private static WhiteRuleService whiteRuleService;

    public GlobalVariables(RedisUtil redisUtil,
                           RedisTemplate<String, Object> redisTemplate,
                           BiliService biliService,
                           BlackRuleService blackRuleService,
                           WhiteRuleService whiteRuleService
                           ) {
        GlobalVariables.redisUtil = redisUtil;
        GlobalVariables.redisTemplate = redisTemplate;
        GlobalVariables.biliService = biliService;
        GlobalVariables.blackRuleService = blackRuleService;
        GlobalVariables.whiteRuleService = whiteRuleService;
    }

    public static void addBlackUserId(Collection<String> param) {
        GlobalVariables.blackUserIdSet.addAll(param);
        redisUtil.delete(BLACK_USER_ID_KEY);
        redisUtil.sAdd(BLACK_USER_ID_KEY, GlobalVariables.blackUserIdSet.toArray());
    }

    /**
     * 更新blackUserIdSet
     *
     * @param param
     */
    public static void setBlackUserIdSet(Set<String> param) {
        GlobalVariables.blackUserIdSet = param;
        redisUtil.delete(BLACK_USER_ID_KEY);
        redisUtil.sAdd(BLACK_USER_ID_KEY, GlobalVariables.blackUserIdSet.toArray());
    }

    /**
     * 更新blackKeywordSet
     *
     * @param param
     */
    public static void setBlackKeywordSet(Set<String> param) {
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();
        param.removeAll(ignoreKeyWordSet);

        GlobalVariables.blackKeywordSet = param;
        redisUtil.delete(BLACK_KEY_WORD_KEY);
        redisUtil.sAdd(BLACK_KEY_WORD_KEY, GlobalVariables.blackKeywordSet.toArray());

        GlobalVariables.blackKeywordTree = new WordTree();
        GlobalVariables.blackKeywordTree.addWords(GlobalVariables.blackKeywordSet);
    }

    public static void addBlackKeyword(Collection<String> param) {
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();
        param.removeAll(ignoreKeyWordSet);

        GlobalVariables.blackKeywordSet.addAll(param);
        redisUtil.delete(BLACK_KEY_WORD_KEY);
        redisUtil.sAdd(BLACK_KEY_WORD_KEY, GlobalVariables.blackKeywordSet.toArray());

        GlobalVariables.blackKeywordTree.addWords(param);
    }

    /**
     * 更新blackKeywordSet
     *
     * @param param
     */
    public static void setBlackTagSet(Set<String> param) {
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();
        param.removeAll(ignoreKeyWordSet);

        GlobalVariables.blackTagSet = param;
        redisUtil.delete(BLACK_TAG_KEY);
        redisUtil.sAdd(BLACK_TAG_KEY, GlobalVariables.blackTagSet.toArray());

        GlobalVariables.blackTagTree = new WordTree();
        GlobalVariables.blackTagTree.addWords(GlobalVariables.blackTagSet);
    }

    public static void addBlackTagSet(Collection<String> param) {
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();
        param.removeAll(ignoreKeyWordSet);

        GlobalVariables.blackTagSet.addAll(param);
        redisUtil.delete(BLACK_TAG_KEY);
        redisUtil.sAdd(BLACK_TAG_KEY, GlobalVariables.blackTagSet.toArray());

        GlobalVariables.blackTagTree.addWords(param);
    }

    public static void setWhiteUserIdSet(Set<String> whiteUserIdSet) {
        GlobalVariables.whiteUserIdSet = whiteUserIdSet;
        redisUtil.delete(WHITE_USER_ID_KEY);
        redisUtil.sAdd(WHITE_USER_ID_KEY, GlobalVariables.whiteUserIdSet.toArray());
    }

    public static void addWhiteUserId(Collection<String> whiteUserIdSet) {
        GlobalVariables.whiteUserIdSet.addAll(whiteUserIdSet);
        redisUtil.delete(WHITE_USER_ID_KEY);
        redisUtil.sAdd(WHITE_USER_ID_KEY, GlobalVariables.whiteUserIdSet.toArray());
    }

    public static void setBlackTidSet(Set<String> blackTidSet) {
        GlobalVariables.blackTidSet = blackTidSet;
        redisUtil.delete(BLACK_TID_KEY);
        redisUtil.sAdd(BLACK_TID_KEY, GlobalVariables.blackTidSet.toArray());
    }

    public static void addBlackTid(Collection<String> blackTidSet) {
        GlobalVariables.blackTidSet.addAll(blackTidSet);
        redisUtil.delete(BLACK_TID_KEY);
        redisUtil.sAdd(BLACK_TID_KEY, GlobalVariables.blackTidSet.toArray());
    }

    public static void setWhiteTidSet(Set<String> whiteTidSet) {
        GlobalVariables.whiteTidSet = whiteTidSet;
        redisUtil.delete(WHITE_TID_KEY);
        redisUtil.sAdd(WHITE_TID_KEY, GlobalVariables.whiteTidSet.toArray());
    }

    public static void addWhiteTid(Collection<String> whiteTidSet) {
        GlobalVariables.whiteTidSet.addAll(whiteTidSet);
        redisUtil.delete(WHITE_TID_KEY);
        redisUtil.sAdd(WHITE_TID_KEY, GlobalVariables.whiteTidSet.toArray());
    }


    public static void setCookieMap(Map<String, String> cookieMap) {
        GlobalVariables.cookieMap = cookieMap;
        redisUtil.delete(COOKIES_KEY);
        redisUtil.hPutAll(COOKIES_KEY, GlobalVariables.cookieMap);
    }

    public static void addCookieMap(Map<String, String> cookieMap) {
        GlobalVariables.cookieMap.putAll(cookieMap);
        redisUtil.delete(COOKIES_KEY);
        redisUtil.hPutAll(COOKIES_KEY, GlobalVariables.cookieMap);
    }

    public static void setMid(String mid) {
        GlobalVariables.mid = mid;
    }

    public static void setKeywordSet(Set<String> keywordSet) {
        GlobalVariables.keywordSet = keywordSet;
        redisUtil.delete(KEY_WORD_KEY);
        redisUtil.sAdd(KEY_WORD_KEY, GlobalVariables.keywordSet.toArray());
    }

    public static void addKeywordSet(Collection<String> keywordSet) {
        GlobalVariables.keywordSet.addAll(keywordSet);
        redisUtil.delete(KEY_WORD_KEY);
        redisUtil.sAdd(KEY_WORD_KEY, GlobalVariables.keywordSet.toArray());
    }


    public static void setWhitelistRules(List<WhitelistRule> whitelistRules) {

        //需要忽略的词汇不要存入规则中
        Set<String> ignoreKeyWordSet = whiteRuleService.getWhiteIgnoreKeyWord();
        for (WhitelistRule whitelistRule : whitelistRules) {
            whitelistRule.getDescKeyWordList().removeAll(ignoreKeyWordSet);
            whitelistRule.getTitleKeyWordList().removeAll(ignoreKeyWordSet);
            whitelistRule.getTagNameList().removeAll(ignoreKeyWordSet);
        }
        GlobalVariables.whitelistRules = whitelistRules;
        redisUtil.delete(WHITE_LIST_RULE_KEY);
        redisUtil.sAdd(WHITE_LIST_RULE_KEY, whitelistRules.toArray());
    }

    public static void addWhitelistRules(List<WhitelistRule> whitelistRules) {
        //需要忽略的词汇不要存入规则中
        Set<String> ignoreKeyWordSet = whiteRuleService.getWhiteIgnoreKeyWord();
        for (WhitelistRule whitelistRule : whitelistRules) {
            whitelistRule.getDescKeyWordList().removeAll(ignoreKeyWordSet);
            whitelistRule.getTitleKeyWordList().removeAll(ignoreKeyWordSet);
            whitelistRule.getTagNameList().removeAll(ignoreKeyWordSet);
        }

        GlobalVariables.whitelistRules.addAll(whitelistRules);
        redisUtil.delete(WHITE_LIST_RULE_KEY);
        redisUtil.sAdd(WHITE_LIST_RULE_KEY, whitelistRules.toArray());
    }

    public static void setApiHeaderMap(Map<String, ApiHeader> apiHeaderMap) {
        GlobalVariables.apiHeaderMap = apiHeaderMap;
        redisUtil.delete(API_HEADER_MAP);
        redisTemplate.opsForHash().putAll(API_HEADER_MAP, GlobalVariables.apiHeaderMap);
    }

    public static void addApiHeader(String key, ApiHeader value) {
        GlobalVariables.apiHeaderMap.put(key, value);
        redisUtil.delete(API_HEADER_MAP);
        redisTemplate.opsForHash().putAll(API_HEADER_MAP, GlobalVariables.apiHeaderMap);
    }


    public static void setCommonCookieMap(Map<String, String> commonCookieMap) {
        GlobalVariables.commonCookieMap = commonCookieMap;
        redisUtil.delete(COMMON_COOKIE_MAP);
        redisUtil.hPutAll(COMMON_COOKIE_MAP, GlobalVariables.commonCookieMap);
    }

    public static void addCommonCookieMap(String key, String value) {
        GlobalVariables.commonCookieMap.put(key,value);
        redisUtil.delete(COMMON_COOKIE_MAP);
        redisUtil.hPutAll(COMMON_COOKIE_MAP, GlobalVariables.commonCookieMap);
    }

    public static void setCommonHeaderMap(Map<String, String> commonHeaderMap) {
        GlobalVariables.commonHeaderMap = commonHeaderMap;
        redisUtil.delete(COMMON_HEADER_MAP);
        redisUtil.hPutAll(COMMON_HEADER_MAP, GlobalVariables.commonHeaderMap);
    }


    public static void addCommonHeaderMap(String key, String value) {
        GlobalVariables.commonHeaderMap.put(key,value);
        redisUtil.delete(COMMON_HEADER_MAP);
        redisUtil.hPutAll(COMMON_HEADER_MAP, GlobalVariables.commonHeaderMap);
    }
}