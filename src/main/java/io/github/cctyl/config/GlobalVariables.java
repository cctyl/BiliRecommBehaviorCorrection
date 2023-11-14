package io.github.cctyl.config;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.entity.Dict;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.entity.WhiteListRule;
import io.github.cctyl.pojo.enumeration.AccessType;
import io.github.cctyl.pojo.enumeration.DictType;
import io.github.cctyl.service.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 全局变量的存储
 */
@Data
@Component
public class GlobalVariables {

    /**
     * 黑名单up主 id列表
     */
    private static Set<String> BLACK_USER_ID_SET;

    /**
     * 白名单up主id列表
     */
    private static Set<String> WHITE_USER_ID_SET;

    /**
     * 黑名单关键词列表
     */
    //TODO 已经有Tree，这个set是否还有必要？
    private static Set<String> BLACK_KEYWORD_SET;

    /**
     * 黑名单关键词树
     */
    private static WordTree BLACK_KEYWORD_TREE = new WordTree();

    /**
     * 黑名单分区id列表
     */
    private static Set<String> BLACK_TID_SET;

    /**
     * 白名单分区id列表
     */
    private static Set<String> WHITE_TID_SET;

    /**
     * 黑名单标签列表
     */
    private static Set<String> BLACK_TAG_SET;

    /**
     * 黑名单标签树
     */
    private static WordTree BLACK_TAG_TREE = new WordTree();

    /**
     * cookie
     */
    private static Map<String, String> COOKIE_MAP = new HashMap<>(20);

    /**
     * 播放者用户id
     */
    private static String MID;

    /**
     * 关键词列表
     */
    private static Set<String> KEYWORD_SET;

    /**
     * 白名单关键词列表
     */
    private static List<WhiteListRule> WHITELIST_RULE_LIST;

    /**
     * ApiHeader
     * url 作为键，cookie 和 httpheader 作为值
     */
    private static Map<String, ApiHeader> API_HEADER_MAP = new HashMap<>();

    /**
     * 通用的cookie，当没有找到匹配的url时使用这个cookie
     */
    private static Map<String, String> COMMON_COOKIE_MAP = new HashMap<>();
    /**
     * 通用的header，当没有找到匹配的url时使用这个header
     */
    private static Map<String, String> COMMON_HEADER_MAP = new HashMap<>();

    /**
     * 最小播放时间
     */
    private static int MIN_PLAY_SECOND = 50;

    /**
     * 停顿词列表
     */
    private static WordTree STOP_WORD_TREE = new WordTree();
    /**
     * 黑白名单忽略关键词列表
     */
    private static Set<String> IGNORE_BLACK_KEY_WORD_SET;
    private static Set<String> IGNORE_WHITE_KEY_WORD_SET;

    private static BlackRuleService blackRuleService;
    private static WhiteRuleService whiteRuleService;
    private static DictService dictService;
    private static WhiteListRuleService whiteListRuleService;
    private static CookieHeaderDataService cookieHeaderDataService;
    private static ConfigService configService;

    public GlobalVariables(

                           BlackRuleService blackRuleService,
                           WhiteRuleService whiteRuleService,
                           DictService dictService,
                           WhiteListRuleService whiteListRuleService,
                           CookieHeaderDataService cookieHeaderDataService,
                           ConfigService configService
    ) {
        GlobalVariables.blackRuleService = blackRuleService;
        GlobalVariables.whiteRuleService = whiteRuleService;
        GlobalVariables.dictService = dictService;
        GlobalVariables.whiteListRuleService = whiteListRuleService;
        GlobalVariables.cookieHeaderDataService = cookieHeaderDataService;
        GlobalVariables.configService = configService;

    }

/*
    public static void addBlackUserId(Collection<String> param) {
        GlobalVariables.blackUserIdSet.addAll(param);
        redisUtil.delete(BLACK_USER_ID_KEY);
        redisUtil.sAdd(BLACK_USER_ID_KEY, GlobalVariables.blackUserIdSet.toArray());
    }
*/



/*    *//**
     * 添加黑名单关键词
     * 去重
     * 添加到缓存
     * 添加到数据库
     *
     *//*
    public static void addBlackKeyword(Collection<String> param) {
        //1.加载需要忽略的东西
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();


        //2. 黑名单关键词列表
        Set<String> wordStrSet = GlobalVariables.blackKeywordSet.stream().map(Dict::getValue).collect(Collectors.toSet());

        //3.去重，得到新产生的数据
        param.removeAll(ignoreKeyWordSet);
        param.removeAll(wordStrSet);

        //4.添加
        List<Dict> newBlackKeyWordSet = dictService.addBlackKeyword(param);
        GlobalVariables.blackKeywordSet.addAll(newBlackKeyWordSet);
        GlobalVariables.blackKeywordTree.addWords(param);
    }*/



/*    public static void addBlackTagSet(Collection<String> param) {

        //1.加载需要忽略的东西
        Set<String> ignoreKeyWordSet = blackRuleService.getIgnoreKeyWordSet();

        //2. 黑名单关键词列表
        Set<String> wordStrSet = GlobalVariables.blackTagSet.stream().map(Dict::getValue).collect(Collectors.toSet());

        //3.去重，得到新产生的数据
        param.removeAll(ignoreKeyWordSet);
        param.removeAll(wordStrSet);

        //4.添加
        List<Dict> newBlackKeyWordSet = dictService.addBlackTag(param);
        GlobalVariables.blackTagSet.addAll(newBlackKeyWordSet);
        GlobalVariables.blackTagTree.addWords(param);

    }*/



/*
    public static void addWhiteUserId(Collection<String> whiteUserIdSet) {
        GlobalVariables.whiteUserIdSet.addAll(whiteUserIdSet);
        redisUtil.delete(WHITE_USER_ID_KEY);
        redisUtil.sAdd(WHITE_USER_ID_KEY, GlobalVariables.whiteUserIdSet.toArray());
    }
*/


/*
    public static void addBlackTid(Collection<String> blackTidSet) {
        GlobalVariables.blackTidSet.addAll(blackTidSet);
        redisUtil.delete(BLACK_TID_KEY);
        redisUtil.sAdd(BLACK_TID_KEY, GlobalVariables.blackTidSet.toArray());
    }*/

/*

    public static void addWhiteTid(Collection<String> whiteTidSet) {
        GlobalVariables.whiteTidSet.addAll(whiteTidSet);
        redisUtil.delete(WHITE_TID_KEY);
        redisUtil.sAdd(WHITE_TID_KEY, GlobalVariables.whiteTidSet.toArray());
    }
*/


/*
    public static void setCookieMap(Map<String, String> cookieMap) {
        GlobalVariables.cookieMap = cookieMap;
        redisUtil.delete(COOKIES_KEY);
        redisUtil.hPutAll(COOKIES_KEY, GlobalVariables.cookieMap);
    }
*/

/*
    public static void addCookieMap(Map<String, String> cookieMap) {
        GlobalVariables.cookieMap.putAll(cookieMap);
        redisUtil.delete(COOKIES_KEY);
        redisUtil.hPutAll(COOKIES_KEY, GlobalVariables.cookieMap);
    }
*/


/*
    public static void addKeywordSet(Collection<Dict> keywordSet) {
        GlobalVariables.keywordSet.addAll(keywordSet);
    }
*/




/*    public static void addWhitelistRules(List<WhiteListRule> whitelistRules) {
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
        GlobalVariables.commonCookieMap.put(key, value);
        redisUtil.delete(COMMON_COOKIE_MAP);
        redisUtil.hPutAll(COMMON_COOKIE_MAP, GlobalVariables.commonCookieMap);
    }

    public static void setCommonHeaderMap(Map<String, String> commonHeaderMap) {
        GlobalVariables.commonHeaderMap = commonHeaderMap;
        redisUtil.delete(COMMON_HEADER_MAP);
        redisUtil.hPutAll(COMMON_HEADER_MAP, GlobalVariables.commonHeaderMap);
    }


    public static void addCommonHeaderMap(String key, String value) {
        GlobalVariables.commonHeaderMap.put(key, value);
        redisUtil.delete(COMMON_HEADER_MAP);
        redisUtil.hPutAll(COMMON_HEADER_MAP, GlobalVariables.commonHeaderMap);
    }*/


    public static Set<String> getBLACK_USER_ID_SET() {
        return BLACK_USER_ID_SET;
    }

    public static Set<String> getWHITE_USER_ID_SET() {
        return WHITE_USER_ID_SET;
    }

    public static Set<String> getBLACK_KEYWORD_SET() {
        return BLACK_KEYWORD_SET;
    }

    public static WordTree getBLACK_KEYWORD_TREE() {
        return BLACK_KEYWORD_TREE;
    }

    public static Set<String> getBLACK_TID_SET() {
        return BLACK_TID_SET;
    }

    public static Set<String> getWHITE_TID_SET() {
        return WHITE_TID_SET;
    }

    public static Set<String> getBLACK_TAG_SET() {
        return BLACK_TAG_SET;
    }

    public static WordTree getBLACK_TAG_TREE() {
        return BLACK_TAG_TREE;
    }

    public static Map<String, String> getCOOKIE_MAP() {
        return COOKIE_MAP;
    }

    public static String getMID() {
        return MID;
    }

    public static Set<String> getKEYWORD_SET() {
        return KEYWORD_SET;
    }

    public static List<WhiteListRule> getWHITELIST_RULE_LIST() {
        return WHITELIST_RULE_LIST;
    }

    public static Map<String, ApiHeader> getAPI_HEADER_MAP() {
        return API_HEADER_MAP;
    }

    public static Map<String, String> getCOMMON_COOKIE_MAP() {
        return COMMON_COOKIE_MAP;
    }

    public static Map<String, String> getCOMMON_HEADER_MAP() {
        return COMMON_HEADER_MAP;
    }

    public static int getMIN_PLAY_SECOND() {
        return MIN_PLAY_SECOND;
    }

    public static WordTree getSTOP_WORD_TREE() {
        return STOP_WORD_TREE;
    }

    public static Set<String> getIGNORE_BLACK_KEY_WORD_SET() {
        return IGNORE_BLACK_KEY_WORD_SET;
    }

    public static Set<String> getIGNORE_WHITE_KEY_WORD_SET() {
        return IGNORE_WHITE_KEY_WORD_SET;
    }

    /**
     *
     * 黑白名单忽略关键词列表的加载
     */
    public static void initIgnoreKeyWord() {
        IGNORE_BLACK_KEY_WORD_SET = dictService.findBlackIgnoreKeyWord().stream().map(Dict::getValue).collect(Collectors.toSet());
        IGNORE_WHITE_KEY_WORD_SET = dictService.findWhiteIgnoreKeyWord().stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    /**
     * 更新blackUserIdSet
     */
    public static void initBlackUserIdSet() {
        GlobalVariables.BLACK_USER_ID_SET = dictService
                .findBlackUserId()
                .stream()
                .map(Dict::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * 更新blackKeywordSet
     */
    public static void initBlackKeywordSet() {
        //1.加载需要忽略的东西

        //2. 黑名单关键词列表
        GlobalVariables.BLACK_KEYWORD_SET = dictService.findBlackKeyWord()
                .stream()
                .map(Dict::getValue)
                .collect(Collectors.toSet())
        ;
        GlobalVariables.BLACK_KEYWORD_SET.removeAll(IGNORE_BLACK_KEY_WORD_SET);

        //3.构建dfa Tree
        GlobalVariables.BLACK_KEYWORD_TREE = new WordTree();
        GlobalVariables.BLACK_KEYWORD_TREE.addWords( GlobalVariables.BLACK_KEYWORD_SET);
    }

    /**
     * 更新blackKeywordSet
     */
    public static void initBlackTagSet() {
        //1.加载需要忽略的东西

        GlobalVariables.BLACK_TAG_SET =
               dictService.findBlackTag()
                .stream()
                       .map(Dict::getValue)
                .collect(Collectors.toSet());

        GlobalVariables.BLACK_TAG_SET.removeAll(IGNORE_BLACK_KEY_WORD_SET);

        GlobalVariables.BLACK_TAG_TREE = new WordTree();
        GlobalVariables.BLACK_TAG_TREE.addWords(GlobalVariables.BLACK_TAG_SET);
    }

    public static void initWhiteUserIdSet() {
    GlobalVariables.WHITE_USER_ID_SET = dictService
            .findWhiteUserId()
            .stream().map(Dict::getValue)
            .collect(Collectors.toSet());
}

    public static void initBlackTidSet() {
        GlobalVariables.BLACK_TID_SET = dictService
                .findBlackTid()
                .stream().map(Dict::getValue)
                .collect(Collectors.toSet());
    }

    public static void initWhiteTidSet() {
        GlobalVariables.WHITE_TID_SET = dictService
                .findWhiteTid()
                .stream()
                .map(Dict::getValue)
                .collect(Collectors.toSet());

    }

    public static void initMid() {
        GlobalVariables.MID =  configService.findByName("mid");

    }

    public static void initMinPlaySecond() {

        String minPlaySecond = configService.findByName("minPlaySecond");
        if (minPlaySecond!=null){
            try {
                GlobalVariables.MIN_PLAY_SECOND = Integer.parseInt(minPlaySecond);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    public static void initKeywordSet() {
        GlobalVariables.KEYWORD_SET = dictService.findSearchKeyWord()
        .stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    public static void initWhitelistRules() {

        List<WhiteListRule> whitelistRules = whiteListRuleService.findAll();

        //需要忽略的词汇不要存入规则中

        for (WhiteListRule whitelistRule : whitelistRules) {
            whitelistRule.getDescKeyWordList().removeAll(IGNORE_WHITE_KEY_WORD_SET);
            whitelistRule.getTitleKeyWordList().removeAll(IGNORE_WHITE_KEY_WORD_SET);
            whitelistRule.getTagNameList().removeAll(IGNORE_WHITE_KEY_WORD_SET);
        }
        GlobalVariables.WHITELIST_RULE_LIST = whitelistRules;

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
        GlobalVariables.STOP_WORD_TREE.addWords(stopWordList);
    }

    public static void initApiHeaderMap() {

        GlobalVariables.COMMON_COOKIE_MAP = cookieHeaderDataService.findCookieMap();
        GlobalVariables.COMMON_HEADER_MAP = cookieHeaderDataService.findHeaderMap();
        GlobalVariables.API_HEADER_MAP =  cookieHeaderDataService.findApiHeaderMap();

    }

    public static void initCookieMap() {
        GlobalVariables.COOKIE_MAP = cookieHeaderDataService.findCookieMap();
    }

    /**
     * 添加一个黑名单用户id
     * @param mid
     */
    public static void addBlackUserId(String mid) {

        Dict dict = new Dict()
                .setDictType(DictType.MID)
                .setAccessType(AccessType.BLACK)
                .setValue(mid);
        dictService.save(dict);

        GlobalVariables.BLACK_USER_ID_SET.add(mid);
    }

    /**
     * 从缓存中读入数据，存储
     *
     * 将这些标签的类型由CACHE 改为正常类型即可
     * 黑名单中加入新出现的标签
     */
    public static void addBlackKeyWordFromCache(List<String> keywordIdSet) {

        //过滤掉忽略的关键词(无需，添加关键词时，如果匹配忽略关键词则不允许添加)

        //将这些标签的类型由CACHE 改为正常类型即可
        dictService.updateAccessTypeByIdIn(
                AccessType.BLACK_CACHE,
                keywordIdSet
        );

        List<String> valueList = Dict.transferToValue( dictService.findByIdIn(keywordIdSet));
        BLACK_KEYWORD_SET.addAll( valueList);
        BLACK_KEYWORD_TREE.addWords(valueList);
    }

    /**
     * 将缓存中的Tag 加入正常的Tag列表中
     * @param tagNameIdList
     */
    public static void addBlackTagFromCache(List<String> tagNameIdList) {

        //将这些标签的类型由CACHE 改为正常类型即可
        dictService.updateAccessTypeByIdIn(
                AccessType.BLACK_CACHE,
                tagNameIdList
        );

        List<String> valueList = Dict.transferToValue( dictService.findByIdIn(tagNameIdList));
        BLACK_TAG_SET.addAll( valueList);
        BLACK_TAG_TREE.addWords(valueList);
    }

    /**
     * 新增tid
     * @param param
     */
    public static void addBlackTidSet(Set<String> param) {


        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.TID,
                null,
                param);

        //新增到缓存
         BLACK_TID_SET.addAll(param);
    }

    /**
     * 新增黑名单关键词
     * @param keywordCol
     */
    public static void addBlackKeyword(Collection<String> keywordCol) {


        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.KEYWORD,
                null,
                keywordCol);

        //新增到缓存
        BLACK_KEYWORD_SET.addAll(keywordCol);

    }

    public static void addBlackUserIdSet(Set<String> blackUserIdSet) {

        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.MID,
                null,
                blackUserIdSet);

        //新增到缓存
        BLACK_USER_ID_SET.addAll(blackUserIdSet);
    }
}