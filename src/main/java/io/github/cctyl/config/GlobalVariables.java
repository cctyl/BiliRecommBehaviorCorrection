package io.github.cctyl.config;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.entity.Dict;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.entity.WhiteListRule;
import io.github.cctyl.service.*;
import io.github.cctyl.utils.RedisUtil;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.cctyl.pojo.constants.AppConstant.*;

/**
 * 全局变量的存储
 */
@Data
@Component
public class GlobalVariables {

    /**
     * 黑名单up主 id列表
     */
    public static Set<Dict> blackUserIdSet;

    /**
     * 白名单up主id列表
     */
    public static Set<Dict> whiteUserIdSet;

    /**
     * 黑名单关键词列表
     */
    public static Set<Dict> blackKeywordSet;

    /**
     * 黑名单关键词树
     */
    public static WordTree blackKeywordTree = new WordTree();

    /**
     * 黑名单分区id列表
     */
    public static Set<Dict> blackTidSet;

    /**
     * 白名单分区id列表
     */
    public static Set<Dict> whiteTidSet;

    /**
     * 黑名单标签列表
     */
    public static Set<Dict> blackTagSet;

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
    public static Set<Dict> keywordSet;

    /**
     * 白名单关键词列表
     */
    public static List<WhiteListRule> whitelistRules;

    /**
     * ApiHeader
     * url 作为键，cookie 和 httpheader 作为值
     */
    public static Map<String, ApiHeader> apiHeaderMap = new HashMap<>();
    /**
     * 通用的cookie，当没有找到匹配的url时使用这个cookie
     */
    public static Map<String, String> commonCookieMap = new HashMap<>();
    /**
     * 通用的header，当没有找到匹配的url时使用这个header
     */
    public static Map<String, String> commonHeaderMap = new HashMap<>();

    /**
     * 最小播放时间
     */
    public static int minPlaySecond = 50;

    /**
     * 停顿词列表
     */
    public static WordTree stopWordTree = new WordTree();


    private static RedisUtil redisUtil;
    private static RedisTemplate<String, Object> redisTemplate;
    private static BiliService biliService;
    private static BlackRuleService blackRuleService;
    private static WhiteRuleService whiteRuleService;
    private static DictService dictService;
    private static WhiteListRuleService whiteListRuleService;
    private static CookieHeaderDataService cookieHeaderDataService;

    public GlobalVariables(RedisUtil redisUtil,
                           RedisTemplate<String, Object> redisTemplate,
                           BiliService biliService,
                           BlackRuleService blackRuleService,
                           WhiteRuleService whiteRuleService,
                           DictService dictService,
                           WhiteListRuleService whiteListRuleService,
                           CookieHeaderDataService cookieHeaderDataService
                           ) {
        GlobalVariables.redisUtil = redisUtil;
        GlobalVariables.redisTemplate = redisTemplate;
        GlobalVariables.biliService = biliService;
        GlobalVariables.blackRuleService = blackRuleService;
        GlobalVariables.whiteRuleService = whiteRuleService;
        GlobalVariables.dictService = dictService;
        GlobalVariables.whiteListRuleService = whiteListRuleService;
        GlobalVariables.cookieHeaderDataService = cookieHeaderDataService;

    }

    public static void addBlackUserId(Collection<String> param) {
        GlobalVariables.blackUserIdSet.addAll(param);
        redisUtil.delete(BLACK_USER_ID_KEY);
        redisUtil.sAdd(BLACK_USER_ID_KEY, GlobalVariables.blackUserIdSet.toArray());
    }

    /**
     * 更新blackUserIdSet
     *
     */
    public static void initBlackUserIdSet( ) {
        GlobalVariables.blackUserIdSet = new HashSet<>( dictService.findBlackUserId());
    }

    /**
     * 更新blackKeywordSet
     *
     */
    public static void initBlackKeywordSet() {
        //1.加载需要忽略的东西
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();


        //2. 黑名单关键词列表
        GlobalVariables.blackKeywordSet = dictService.findBlackKeyWord()
                .stream()
                .filter(dict -> !ignoreKeyWordSet.contains(dict.getValue())).collect(Collectors.toSet());
        Set<String> wordStrSet = GlobalVariables.blackKeywordSet.stream().map(Dict::getValue).collect(Collectors.toSet());

        //3.构建dfa Tree
        GlobalVariables.blackKeywordTree = new WordTree();
        GlobalVariables.blackKeywordTree.addWords(wordStrSet);
    }

    /**
     * 添加黑名单关键词
     * 去重
     * 添加到缓存
     * 添加到数据库
     * @param param
     */
    public static void addBlackKeyword(Collection<String> param) {
        //1.加载需要忽略的东西
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();


        //2. 黑名单关键词列表
        Set<String> wordStrSet = GlobalVariables.blackKeywordSet.stream().map(Dict::getValue).collect(Collectors.toSet());

        //3.去重，得到新产生的数据
        param.removeAll(ignoreKeyWordSet);
        param.removeAll(wordStrSet);

        //4.添加
        List<Dict>  newBlackKeyWordSet =  dictService.addBlackKeyword(param);
        GlobalVariables.blackKeywordSet.addAll(newBlackKeyWordSet);
        GlobalVariables.blackKeywordTree.addWords(param);
    }

    /**
     * 更新blackKeywordSet
     *
     */
    public static void initBlackTagSet() {
        //1.加载需要忽略的东西
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();
        GlobalVariables.blackTagSet = new HashSet<>(dictService.findBlackTag())
                .stream()
                .filter(dict -> !ignoreKeyWordSet.contains(dict.getValue())).collect(Collectors.toSet());

        Set<String> blackTagStrSet = GlobalVariables.blackTagSet.stream().map(Dict::getValue).collect(Collectors.toSet());


        GlobalVariables.blackTagTree = new WordTree();
        GlobalVariables.blackTagTree.addWords(blackTagStrSet);
    }

    public static void addBlackTagSet(Collection<String> param) {

        //1.加载需要忽略的东西
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();

        //2. 黑名单关键词列表
        Set<String> wordStrSet = GlobalVariables.blackTagSet.stream().map(Dict::getValue).collect(Collectors.toSet());

        //3.去重，得到新产生的数据
        param.removeAll(ignoreKeyWordSet);
        param.removeAll(wordStrSet);

        //4.添加
        List<Dict>  newBlackKeyWordSet =  dictService.addBlackTag(param);
        GlobalVariables.blackTagSet.addAll(newBlackKeyWordSet);
        GlobalVariables.blackTagTree.addWords(param);

    }

    public static void initWhiteUserIdSet() {
        GlobalVariables.whiteUserIdSet = new HashSet<>(dictService.findWhiteUserId());
    }

    public static void addWhiteUserId(Collection<String> whiteUserIdSet) {
        GlobalVariables.whiteUserIdSet.addAll(whiteUserIdSet);
        redisUtil.delete(WHITE_USER_ID_KEY);
        redisUtil.sAdd(WHITE_USER_ID_KEY, GlobalVariables.whiteUserIdSet.toArray());
    }

    public static void initBlackTidSet() {
        GlobalVariables.blackTidSet = new HashSet<>(dictService.findBlackTid());
    }

    public static void addBlackTid(Collection<String> blackTidSet) {
        GlobalVariables.blackTidSet.addAll(blackTidSet);
        redisUtil.delete(BLACK_TID_KEY);
        redisUtil.sAdd(BLACK_TID_KEY, GlobalVariables.blackTidSet.toArray());
    }

    public static void initWhiteTidSet() {
        GlobalVariables.whiteTidSet =  new HashSet<>(dictService.findWhiteTid());

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

    public static void initKeywordSet() {
        GlobalVariables.keywordSet = new HashSet<>(dictService.findSearchKeyWord());
    }
    public static void addKeywordSet(Collection<Dict> keywordSet) {
        GlobalVariables.keywordSet.addAll(keywordSet);
    }


    public static void initWhitelistRules() {

        List<WhiteListRule> whitelistRules = whiteListRuleService.findAll();

        //需要忽略的词汇不要存入规则中
        Set<String> ignoreKeyWordSet = whiteRuleService.getWhiteIgnoreKeyWord();
        for (WhiteListRule whitelistRule : whitelistRules) {
            whitelistRule.getDescKeyWordList().removeAll(ignoreKeyWordSet);
            whitelistRule.getTitleKeyWordList().removeAll(ignoreKeyWordSet);
            whitelistRule.getTagNameList().removeAll(ignoreKeyWordSet);
        }
        GlobalVariables.whitelistRules = whitelistRules;

    }

    public static void addWhitelistRules(List<WhiteListRule> whitelistRules) {
        //需要忽略的词汇不要存入规则中
        Set<String> ignoreKeyWordSet = whiteRuleService.getWhiteIgnoreKeyWord();
        for (WhiteListRule whitelistRule : whitelistRules) {
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

    /**
     * 加载停顿词列表
     */
    public static void initStopWords() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("cn_stopwords.txt");
        List<String> stopWordList =
                Files.lines(Paths.get(classPathResource.getFile().getPath()))
                        .map(String::trim)
                        .collect(Collectors.toList());
        GlobalVariables.stopWordTree.addWords(stopWordList);
    }

    public static void initApiHeaderMap() {


        cookieHeaderDataService.findCookieMap();

    }
}