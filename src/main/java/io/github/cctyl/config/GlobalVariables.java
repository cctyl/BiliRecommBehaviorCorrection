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
    private static Set<String> blackUserIdSet;

    /**
     * 白名单up主id列表
     */
    private static Set<String> whiteUserIdSet;

    /**
     * 黑名单关键词列表
     */
    //TODO 已经有Tree，这个set是否还有必要？
    private static Set<String> blackKeywordSet;

    /**
     * 黑名单关键词树
     */
    private static WordTree blackKeywordTree = new WordTree();

    /**
     * 黑名单分区id列表
     */
    private static Set<String> blackTidSet;

    /**
     * 白名单分区id列表
     */
    private static Set<String> whiteTidSet;

    /**
     * 黑名单标签列表
     */
    private static Set<String> blackTagSet;

    /**
     * 黑名单标签树
     */
    private static WordTree blackTagTree = new WordTree();

    /**
     * cookie
     */
    private static Map<String, String> cookieMap = new HashMap<>(20);

    /**
     * 播放者用户id
     */
    private static String mid;

    /**
     * 关键词列表
     */
    private static Set<String> keywordSet;

    /**
     * 白名单关键词列表
     */
    private static List<WhiteListRule> whitelistRules;

    /**
     * ApiHeader
     * url 作为键，cookie 和 httpheader 作为值
     */
    private static Map<String, ApiHeader> apiHeaderMap = new HashMap<>();

    /**
     * 通用的cookie，当没有找到匹配的url时使用这个cookie
     */
    private static Map<String, String> commonCookieMap = new HashMap<>();
    /**
     * 通用的header，当没有找到匹配的url时使用这个header
     */
    private static Map<String, String> commonHeaderMap = new HashMap<>();

    /**
     * 最小播放时间
     */
    private static int minPlaySecond = 50;

    /**
     * 停顿词列表
     */
    private static WordTree stopWordTree = new WordTree();
    /**
     * 黑白名单忽略关键词列表
     */
    private static Set<String> ignoreBlackKeyWordSet;
    private static Set<String> ignoreWhiteKeyWordSet;

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


    public static Set<String> getBlackUserIdSet() {
        return blackUserIdSet;
    }

    public static Set<String> getWhiteUserIdSet() {
        return whiteUserIdSet;
    }

    public static Set<String> getBlackKeywordSet() {
        return blackKeywordSet;
    }

    public static WordTree getBlackKeywordTree() {
        return blackKeywordTree;
    }

    public static Set<String> getBlackTidSet() {
        return blackTidSet;
    }

    public static Set<String> getWhiteTidSet() {
        return whiteTidSet;
    }

    public static Set<String> getBlackTagSet() {
        return blackTagSet;
    }

    public static WordTree getBlackTagTree() {
        return blackTagTree;
    }

    public static Map<String, String> getCookieMap() {
        return cookieMap;
    }

    public static String getMid() {
        return mid;
    }

    public static Set<String> getKeywordSet() {
        return keywordSet;
    }

    public static List<WhiteListRule> getWhitelistRules() {
        return whitelistRules;
    }

    public static Map<String, ApiHeader> getApiHeaderMap() {
        return apiHeaderMap;
    }

    public static Map<String, String> getCommonCookieMap() {
        return commonCookieMap;
    }

    public static Map<String, String> getCommonHeaderMap() {
        return commonHeaderMap;
    }

    public static int getMinPlaySecond() {
        return minPlaySecond;
    }

    public static WordTree getStopWordTree() {
        return stopWordTree;
    }

    public static Set<String> getIgnoreBlackKeyWordSet() {
        return ignoreBlackKeyWordSet;
    }

    public static Set<String> getIgnoreWhiteKeyWordSet() {
        return ignoreWhiteKeyWordSet;
    }

    /**
     *
     * 黑白名单忽略关键词列表的加载
     */
    public static void initIgnoreKeyWord() {
        ignoreBlackKeyWordSet = dictService.findBlackIgnoreKeyWord().stream().map(Dict::getValue).collect(Collectors.toSet());
        ignoreWhiteKeyWordSet = dictService.findWhiteIgnoreKeyWord().stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    /**
     * 更新blackUserIdSet
     */
    public static void initBlackUserIdSet() {
        GlobalVariables.blackUserIdSet = dictService
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
        GlobalVariables.blackKeywordSet = dictService.findBlackKeyWord()
                .stream()
                .map(Dict::getValue)
                .collect(Collectors.toSet())
        ;
        GlobalVariables.blackKeywordSet.removeAll(ignoreBlackKeyWordSet);

        //3.构建dfa Tree
        GlobalVariables.blackKeywordTree = new WordTree();
        GlobalVariables.blackKeywordTree.addWords( GlobalVariables.blackKeywordSet);
    }

    /**
     * 更新blackKeywordSet
     */
    public static void initBlackTagSet() {
        //1.加载需要忽略的东西

        GlobalVariables.blackTagSet =
               dictService.findBlackTag()
                .stream()
                       .map(Dict::getValue)
                .collect(Collectors.toSet());

        GlobalVariables.blackTagSet.removeAll(ignoreBlackKeyWordSet);

        GlobalVariables.blackTagTree = new WordTree();
        GlobalVariables.blackTagTree.addWords(GlobalVariables.blackTagSet);
    }

    public static void initWhiteUserIdSet() {
    GlobalVariables.whiteUserIdSet = dictService
            .findWhiteUserId()
            .stream().map(Dict::getValue)
            .collect(Collectors.toSet());
}

    public static void initBlackTidSet() {
        GlobalVariables.blackTidSet = dictService
                .findBlackTid()
                .stream().map(Dict::getValue)
                .collect(Collectors.toSet());
    }

    public static void initWhiteTidSet() {
        GlobalVariables.whiteTidSet = dictService
                .findWhiteTid()
                .stream()
                .map(Dict::getValue)
                .collect(Collectors.toSet());

    }

    public static void initMid() {
        GlobalVariables.mid =  configService.findByName("mid");

    }

    public static void initMinPlaySecond() {

        String minPlaySecond = configService.findByName("minPlaySecond");
        if (minPlaySecond!=null){
            try {
                GlobalVariables.minPlaySecond = Integer.parseInt(minPlaySecond);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    public static void initKeywordSet() {
        GlobalVariables.keywordSet = dictService.findSearchKeyWord()
        .stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    public static void initWhitelistRules() {

        List<WhiteListRule> whitelistRules = whiteListRuleService.findAll();

        //需要忽略的词汇不要存入规则中

        for (WhiteListRule whitelistRule : whitelistRules) {
            whitelistRule.getDescKeyWordList().removeAll(ignoreWhiteKeyWordSet);
            whitelistRule.getTitleKeyWordList().removeAll(ignoreWhiteKeyWordSet);
            whitelistRule.getTagNameList().removeAll(ignoreWhiteKeyWordSet);
        }
        GlobalVariables.whitelistRules = whitelistRules;

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

        GlobalVariables.commonCookieMap = cookieHeaderDataService.findCookieMap();
        GlobalVariables.commonHeaderMap = cookieHeaderDataService.findHeaderMap();
        GlobalVariables.apiHeaderMap =  cookieHeaderDataService.findApiHeaderMap();

    }

    public static void initCookieMap() {
        GlobalVariables.cookieMap = cookieHeaderDataService.findCookieMap();
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

        GlobalVariables.blackUserIdSet.add(mid);
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
        blackKeywordSet.addAll( valueList);
        blackKeywordTree.addWords(valueList);
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
        blackTagSet.addAll( valueList);
        blackTagTree.addWords(valueList);
    }

    /**
     * 新增tid
     * @param blackTidSet
     */
    public static void addBlackTidSet(Set<String> blackTidSet) {


        //先删除之前的
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.TID,
                blackTidSet
        );

        //新增到数据库
        List<Dict> dictList = Dict.keyword2Dict(
                blackTidSet,
                DictType.TID,
                AccessType.BLACK,
                null
        );
        dictService.saveBatch(dictList);

        //新增到缓存
        blackTidSet.addAll(blackTidSet);
    }
}