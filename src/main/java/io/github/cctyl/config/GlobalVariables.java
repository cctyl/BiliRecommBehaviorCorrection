package io.github.cctyl.config;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.entity.Dict;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.entity.WhiteListRule;
import io.github.cctyl.pojo.constants.AppConstant;
import io.github.cctyl.pojo.enumeration.AccessType;
import io.github.cctyl.pojo.enumeration.DictType;
import io.github.cctyl.service.*;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
     * AccessKey
     */
    private static String ACCESS_KEY;

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
     * 播放者用户id
     */
    private static String MID;

    /**
     * 搜索关键词列表
     */
    private static Set<String> SEARCH_KEYWORD_SET;

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
     * 及时刷新类型的cookie 和 header
     */
    private static Map<String, String> REFRESH_COOKIE_MAP = new HashMap<>(20);
    private static Map<String, String> REFRESH_HEADER_MAP = new HashMap<>(20);


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
    private static DictService dictService;
    private static WhiteListRuleService whiteListRuleService;
    private static CookieHeaderDataService cookieHeaderDataService;
    private static ConfigService configService;

    public static GlobalVariables INSTANCE;

    public GlobalVariables(

                           BlackRuleService blackRuleService,
                           DictService dictService,
                           WhiteListRuleService whiteListRuleService,
                           CookieHeaderDataService cookieHeaderDataService,
                           ConfigService configService
    ) {
        GlobalVariables.blackRuleService = blackRuleService;
        GlobalVariables.dictService = dictService;
        GlobalVariables.whiteListRuleService = whiteListRuleService;
        GlobalVariables.cookieHeaderDataService = cookieHeaderDataService;
        GlobalVariables.configService = configService;
        GlobalVariables.INSTANCE = this;
    }



/*
    public static void addBlackUserId(Collection<String> param) {
        GlobalVariables.blackUserIdSet.addAll(param);
        redisUtil.delete(BLACK_USER_ID_KEY);
        redisUtil.sAdd(BLACK_USER_ID_KEY, GlobalVariables.blackUserIdSet.toArray());
    }
*/



/*    */

    public static Set<String> getBlackUserIdSet() {
        return BLACK_USER_ID_SET;
    }

    public static Set<String> getWhiteUserIdSet() {
        return WHITE_USER_ID_SET;
    }

    public static Set<String> getBlackKeywordSet() {
        return BLACK_KEYWORD_SET;
    }

    public static WordTree getBlackKeywordTree() {
        return BLACK_KEYWORD_TREE;
    }

    public static Set<String> getBlackTidSet() {
        return BLACK_TID_SET;
    }

    public static Set<String> getWhiteTidSet() {
        return WHITE_TID_SET;
    }

    public static Set<String> getBlackTagSet() {
        return BLACK_TAG_SET;
    }

    public static WordTree getBlackTagTree() {
        return BLACK_TAG_TREE;
    }

    public static String getMID() {
        return MID;
    }

    public static Set<String> getSearchKeywordSet() {
        return SEARCH_KEYWORD_SET;
    }

    public static List<WhiteListRule> getWhitelistRuleList() {
        return WHITELIST_RULE_LIST;
    }

    public static Map<String, ApiHeader> getApiHeaderMap() {
        return API_HEADER_MAP;
    }

    public static Map<String, String> getCommonCookieMap() {
        return COMMON_COOKIE_MAP;
    }

    public static Map<String, String> getCommonHeaderMap() {
        return COMMON_HEADER_MAP;
    }

    public static int getMinPlaySecond() {
        return MIN_PLAY_SECOND;
    }

    public static WordTree getStopWordTree() {
        return STOP_WORD_TREE;
    }

    public static Set<String> getIgnoreBlackKeyWordSet() {
        return IGNORE_BLACK_KEY_WORD_SET;
    }

    public static Set<String> getIgnoreWhiteKeyWordSet() {
        return IGNORE_WHITE_KEY_WORD_SET;
    }

    public static String getAccessKey() {
        return ACCESS_KEY;
    }

    /**
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




    public static Map<String, String> getRefreshCookieMap() {
        return REFRESH_COOKIE_MAP;
    }

    public static Map<String, String> getRefreshHeaderMap() {
        return REFRESH_HEADER_MAP;
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
        GlobalVariables.SEARCH_KEYWORD_SET = dictService.findSearchKeyWord()
        .stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    public static void initWhitelistRules() {

        //此方法应该查询出关联的相关规则
        List<WhiteListRule> whitelistRules = whiteListRuleService.findWithDetail();

        //需要忽略的词汇不要存入规则中
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
        //通用类型的数据
        GlobalVariables.COMMON_COOKIE_MAP = cookieHeaderDataService.findCommonCookieMap();
        GlobalVariables.COMMON_HEADER_MAP = cookieHeaderDataService.findCommonHeaderMap();

        //匹配类型的数据
        GlobalVariables.API_HEADER_MAP =  cookieHeaderDataService.findApiHeaderMap();

        //及时更新类型的数据
        GlobalVariables.REFRESH_COOKIE_MAP = cookieHeaderDataService.findRefreshCookie();
        GlobalVariables.REFRESH_HEADER_MAP = cookieHeaderDataService.findRefreshHeader();

    }

    /**
     * 更新 及时更新的cookie
     * @param cookieMap
     */
    public static void updateRefreshCookie(Map<String, String> cookieMap) {
        REFRESH_COOKIE_MAP.putAll(cookieMap);
        cookieHeaderDataService.updateRefresh(cookieMap);
    }


    /**
     * 添加一个黑名单用户id
     * @param mid
     */
    public  void addBlackUserId(String mid) {

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
    public  void addBlackKeyWordFromCache(List<String> keywordIdSet) {

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
    public  void addBlackTagFromCache(List<String> tagNameIdList) {

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
    public  void addBlackTidSet(Set<String> param) {
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
    public  void addBlackKeyword(Collection<String> keywordCol) {


        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.KEYWORD,
                null,
                keywordCol);

        //新增到缓存
        BLACK_KEYWORD_SET.addAll(keywordCol);

    }

    public  void addBlackUserIdSet(Set<String> blackUserIdSet) {

        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.MID,
                null,
                blackUserIdSet);

        //新增到缓存
        BLACK_USER_ID_SET.addAll(blackUserIdSet);
    }

    public  void addBlackTagSet(Set<String> collect) {

        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.TAG,
                null,
                collect);

        //新增到缓存
        BLACK_TAG_SET.addAll(collect);
        BLACK_TAG_TREE.addWords(collect);
    }

    /**
     * 添加忽略的黑名单关键词
     * 并更新 黑名单关键词和黑名单TAG
     *
     * @param collect
     */
    @Transactional
    public  void addBlackIgnoreKeyword(Set<String> collect) {
        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.IGNORE_KEYWORD,
                null,
                collect);

        //新增到缓存
        IGNORE_BLACK_KEY_WORD_SET.addAll(collect);

        //黑名单关键词需要删除
        removeBlackKeyword(collect);

        //黑名单Tag需要删除
        removeBlackTag(collect);

    }

    public  void removeBlackTag(Set<String> param) {
        for (String s : param) {
            BLACK_TAG_SET.remove(s);
            BLACK_TAG_TREE.remove(s);
        }
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.TAG,
                param
        );
    }

    public  void removeBlackKeyword(Set<String> param) {

        for (String s : param) {
            BLACK_KEYWORD_SET.remove(s);
            BLACK_KEYWORD_TREE.remove(s);
        }
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.KEYWORD,
                param
        );
    }

    /**
     * 添加或更新白名单
     * @param whitelistRule
     */
    @Transactional
    public  void addOrUpdateWhitelitRule(WhiteListRule whitelistRule) {

        if (whitelistRule.getId()!=null){
            WHITELIST_RULE_LIST.remove(whitelistRule);
        }
        WHITELIST_RULE_LIST.add(whitelistRule);
        //修改主对象
        whiteListRuleService.saveOrUpdate(whitelistRule);
        //修改关联的数据
        dictService.updateByWhiteListRule(whitelistRule);
    }

    /**
     * 根据id删除白名单
     * @param id
     * @return
     */
    public  boolean removeWhitelistRules(Long id) {

        WHITELIST_RULE_LIST =  WHITELIST_RULE_LIST.stream()
                .filter(whiteListRule -> !id.equals(whiteListRule.getId()))
                .collect(Collectors.toList());

        return whiteListRuleService.removeById(id);
    }

    /**
     * 添加白名单忽略关键词
     * @param ignoreKeyWordSet
     */
    @Transactional
    public  void addWhiteIgnoreKeyword(Set<String> ignoreKeyWordSet) {

        dictService.removeAndAddDict(
                AccessType.WHITE,
                DictType.IGNORE_KEYWORD,
                null,
                ignoreKeyWordSet
        );
        IGNORE_WHITE_KEY_WORD_SET.addAll(ignoreKeyWordSet);


        //删除白名单对应的关键词,不区分具体是哪个白名单对象的
        //并且需要同时删除缓存内对应的数据
        removeWhiteTagKeyword(ignoreKeyWordSet);
        removeWhiteDescKeyword(ignoreKeyWordSet);
        removeWhiteTitleKeyword(ignoreKeyWordSet);
        removeWhiteCoverKeyword(ignoreKeyWordSet);


    }

    private  void removeWhiteCoverKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.COVER,
                ignoreKeyWordSet
        );

        //缓存层面的删除
        for (WhiteListRule whiteListRule : WHITELIST_RULE_LIST) {

            List<Dict> coverKeyword = whiteListRule.getCoverKeyword()
                    .stream()
                    .filter(
                            dict -> !ignoreKeyWordSet.contains(dict.getValue())
                    ).collect(Collectors.toList());

            whiteListRule.setCoverKeyword(coverKeyword);
        }
    }

    private  void removeWhiteTitleKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.TITLE,
                ignoreKeyWordSet
        );

        //缓存层面的删除
        for (WhiteListRule whiteListRule : WHITELIST_RULE_LIST) {

            List<Dict> titleKeyWordList = whiteListRule.getTitleKeyWordList()
                    .stream()
                    .filter(
                            dict -> !ignoreKeyWordSet.contains(dict.getValue())
                    ).collect(Collectors.toList());

            whiteListRule.setTitleKeyWordList(titleKeyWordList);
        }

    }

    void removeWhiteDescKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.DESC,
                ignoreKeyWordSet
        );

        //缓存层面的删除
        for (WhiteListRule whiteListRule : WHITELIST_RULE_LIST) {

            List<Dict> descKeyWordList = whiteListRule.getDescKeyWordList()
                    .stream()
                    .filter(
                            dict -> !ignoreKeyWordSet.contains(dict.getValue())
                    ).collect(Collectors.toList());

            whiteListRule.setDescKeyWordList(descKeyWordList);
        }
    }

    public void removeWhiteTagKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
       dictService.removeByAccessTypeAndDictTypeAndValue(
               AccessType.WHITE,
               DictType.TAG,
               ignoreKeyWordSet
       );

       //缓存层面的删除
        for (WhiteListRule whiteListRule : WHITELIST_RULE_LIST) {
            List<Dict> tagNameList = whiteListRule.getTagNameList()
                    .stream()
                    .filter(
                            dict -> !ignoreKeyWordSet.contains(dict.getValue())
                    ).collect(Collectors.toList());
            whiteListRule.setTagNameList(tagNameList);
        }

    }

    public static void updateAccessKey(String newKey){
        ACCESS_KEY = newKey;
        configService.addOrUpdateConfig(AppConstant.ACCESS_KEY,newKey);
    }
}