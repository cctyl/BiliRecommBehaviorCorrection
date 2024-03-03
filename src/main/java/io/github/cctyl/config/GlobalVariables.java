package io.github.cctyl.config;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.domain.constants.AppConstant;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.Classify;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.domain.enumeration.MediaType;
import io.github.cctyl.service.*;
import io.github.cctyl.service.impl.BlackRuleService;
import io.github.cctyl.exception.ServerException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 全局变量的存储
 */
@Data
@Component
@Slf4j
public class GlobalVariables {

    /**
     * bilibili的AccessKey
     */
    private static String BILI_ACCESS_KEY;
    /**
     * wbi
     */
    private static String IMG_KEY;
    private static String SUB_KEY;

    /**
     * 百度accessKey
     */
    private static String BAIDU_ASK_KEY;
    /**
     * 百度客户端id
     */
    private static String BAIDU_CLIENT_ID;
    /**
     * 百度客户端密钥
     */
    private static String BAIDU_CLIENT_SECRET;

    /**
     * 第一次启动
     */
    private static boolean FIRST_USE;

    /**
     * 定时任务开关
     */
    private static boolean CRON;
    /**
     * bilibili 账号是否登陆
     */
    private static boolean BILI_LOGIN;

    /**
     * 黑名单up主 id列表
     */
    private static List<String> BLACK_USER_ID_SET = new ArrayList<>();

    /**
     * 白名单up主id列表
     */
    private static List<String> WHITE_USER_ID_SET = new ArrayList<>();

    /**
     * 黑名单关键词列表
     */
    private static List<String> BLACK_KEYWORD_SET = new ArrayList<>();

    /**
     * 黑名单关键词树
     */
    private static WordTree BLACK_KEYWORD_TREE = new WordTree();

    /**
     * 黑名单分区id列表
     */
    private static List<String> BLACK_TID_SET = new ArrayList<>();

    /**
     * 白名单分区id列表
     */
    private static List<String> WHITE_TID_SET = new ArrayList<>();

    /**
     * 黑名单标签列表
     */
    private static List<String> BLACK_TAG_SET = new ArrayList<>();

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
    private static List<String> SEARCH_KEYWORD_SET = new ArrayList<>();

    /**
     * 白名单关键词列表
     */
    private static List<WhiteListRule> WHITELIST_RULE_LIST = new ArrayList<>();


    /**
     * 通用的header，当没有找到匹配的url时使用这个header
     */
    private static Map<String, String> COMMON_HEADER_MAP = new HashMap<>();


    /**
     * 及时刷新类型的cookie 和 header
     */
    private static Map<String, String> REFRESH_COOKIE_MAP = new HashMap<>(20);

    /**
     * 最小播放时间
     */
    private static int MIN_PLAY_SECOND = 50;

    /**
     * 停顿词列表
     */
    private static List<String> STOP_WORD_LIST = new ArrayList<>(0);
    /**
     * 黑白名单忽略关键词列表
     */
    private static Set<String> IGNORE_BLACK_KEY_WORD_SET = new HashSet<>();
    private static Set<String> IGNORE_WHITE_KEY_WORD_SET = new HashSet<>();

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

    public static List<String> getBlackUserIdSet() {
        return BLACK_USER_ID_SET;
    }

    public static List<String> getWhiteUserIdSet() {
        return WHITE_USER_ID_SET;
    }

    public static List<String> getBlackKeywordSet() {
        return BLACK_KEYWORD_SET;
    }

    public static WordTree getBlackKeywordTree() {
        return BLACK_KEYWORD_TREE;
    }

    public static List<String> getBlackTidSet() {
        return BLACK_TID_SET;
    }

    public static List<String> getWhiteTidSet() {
        return WHITE_TID_SET;
    }

    public static List<String> getBlackTagSet() {
        return BLACK_TAG_SET;
    }

    public static WordTree getBlackTagTree() {
        return BLACK_TAG_TREE;
    }

    public static String getMID() {
        return MID;
    }

    public static List<String> getSearchKeywordSet() {
        return SEARCH_KEYWORD_SET;
    }

    public static List<WhiteListRule> getWhitelistRuleList() {
        return WHITELIST_RULE_LIST;
    }




    public static Map<String, String> getCommonHeaderMap() {
        return COMMON_HEADER_MAP;
    }

    public static int getMinPlaySecond() {
        return MIN_PLAY_SECOND;
    }

    public static List<String> getStopWordList() {
        return STOP_WORD_LIST;
    }

    public static Set<String> getIgnoreBlackKeyWordSet() {
        return IGNORE_BLACK_KEY_WORD_SET;
    }

    public static Set<String> getIgnoreWhiteKeyWordSet() {
        return IGNORE_WHITE_KEY_WORD_SET;
    }

    public static String getBiliAccessKey() {
        return BILI_ACCESS_KEY;
    }

    public static String getBaiduClientId() {
        return BAIDU_CLIENT_ID;
    }

    public static String getBaiduClientSecret() {
        return BAIDU_CLIENT_SECRET;
    }


    public static Map<String, String> getRefreshCookieMap() {
        return REFRESH_COOKIE_MAP;
    }



    /**
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
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
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
                .distinct()
                .collect(Collectors.toList())
        ;
        GlobalVariables.BLACK_KEYWORD_SET.removeAll(IGNORE_BLACK_KEY_WORD_SET);

        //3.构建dfa Tree
        initBlackKeywordTree(GlobalVariables.BLACK_KEYWORD_SET);
    }

    public static void initBlackKeywordTree(Collection<String> param ){
        GlobalVariables.BLACK_KEYWORD_TREE = new WordTree();
        GlobalVariables.BLACK_KEYWORD_TREE.addWords(param);
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
                        .distinct()
                        .collect(Collectors.toList());

        GlobalVariables.BLACK_TAG_SET.removeAll(IGNORE_BLACK_KEY_WORD_SET);

        GlobalVariables.BLACK_TAG_TREE = new WordTree();
        GlobalVariables.BLACK_TAG_TREE.addWords(GlobalVariables.BLACK_TAG_SET);
    }

    public static void initWhiteUserIdSet() {
        GlobalVariables.WHITE_USER_ID_SET = dictService
                .findWhiteUserId()
                .stream()
                .map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public static void initBlackTidSet() {
        GlobalVariables.BLACK_TID_SET = dictService
                .findBlackTid()
                .stream().map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public static void initWhiteTidSet() {
        GlobalVariables.WHITE_TID_SET = dictService
                .findWhiteTid()
                .stream()
                .map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

    }

    public static void initMid() {
        GlobalVariables.MID = configService.findByName(AppConstant.MID_KEY);

    }

    public static void initAccessKey() {
        GlobalVariables.BILI_ACCESS_KEY = configService.findByName(AppConstant.BILI_ACCESS_KEY);
    }


    public static void initMinPlaySecond() {

        String minPlaySecond = configService.findByName(AppConstant.MIN_PLAY_SECOND);
        if (minPlaySecond != null) {
            try {
                GlobalVariables.MIN_PLAY_SECOND = Integer.parseInt(minPlaySecond);
            } catch (NumberFormatException e) {
                log.error(e.getMessage(),e);
            }
        }

    }

    public static void initKeywordSet() {
        GlobalVariables.SEARCH_KEYWORD_SET = dictService.findSearchKeyWord()
                .stream().map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
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
        List<String> stopWordList;
        if (FIRST_USE) {
            //初次使用时，从文件中加载停顿词

            try (
                    InputStream inputStream = GlobalVariables.class.getResourceAsStream("/cn_stopwords.txt");
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {

                stopWordList = bufferedReader.lines()
                        .map(String::trim)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //存入数据库
            dictService.saveStopWords(stopWordList);
        } else {
            //从数据库中加载停顿词
            stopWordList = dictService.findStopWords();
        }
        GlobalVariables.STOP_WORD_LIST = new ArrayList<>(stopWordList);
    }


    public static void addStopWords(Collection<String> stopWordList) {
        //存入数据库
        dictService.saveStopWords(stopWordList);

        GlobalVariables.STOP_WORD_LIST.removeAll(stopWordList);
        GlobalVariables.STOP_WORD_LIST.addAll(stopWordList);
    }

    public static void initApiHeaderMap() {
        //通用类型的数据
        GlobalVariables.COMMON_HEADER_MAP = cookieHeaderDataService.findCommonHeaderMap();
        //及时更新类型的数据
        GlobalVariables.REFRESH_COOKIE_MAP = cookieHeaderDataService.findRefreshCookie();
    }

    /**
     * 更新 及时更新的cookie
     *
     * @param cookieMap
     */
    public static void updateRefreshCookie(Map<String, String> cookieMap) {
        REFRESH_COOKIE_MAP.putAll(cookieMap);

    }

    public static void initWbi() {
        IMG_KEY = configService.findByName(AppConstant.IMG_KEY);
        SUB_KEY = configService.findByName(AppConstant.SUB_KEY);
    }


    public static void initBaiduConfig() {

        BAIDU_ASK_KEY = configService.findByName(AppConstant.BAIDU_ASK_KEY);
        BAIDU_CLIENT_ID = configService.findByName(AppConstant.BAIDU_CLIENT_ID);
        BAIDU_CLIENT_SECRET = configService.findByName(AppConstant.BAIDU_CLIENT_SECRET);

    }


    public static String getBaiduAskKey() {
        return BAIDU_ASK_KEY;
    }

    /**
     * 加载一些标记信息
     */
    public static void setInfo() {
        //1.是否第一次使用本系统
        FIRST_USE = configService.isFirstUse();


        if (FIRST_USE) {
            //2.第一次使用系统时间
            configService.addOrUpdateConfig(AppConstant.FIRST_START_TIME,
                    String.valueOf(Instant.now().toEpochMilli())
            );
            //3.定时任务开关
            setCron(false);
        }


    }

    public static void updateBaiduAskKey(String accessToken) {
        BAIDU_ASK_KEY = accessToken;
        configService.addOrUpdateConfig(AppConstant.BAIDU_ASK_KEY, accessToken, 2592000);
    }

    public static void updateBaiduClientInfo(String clientId, String clientSecret) {
        BAIDU_CLIENT_ID = clientId;
        BAIDU_CLIENT_SECRET = clientSecret;
        configService.addOrUpdateConfig(AppConstant.BAIDU_CLIENT_ID, clientId);
        configService.addOrUpdateConfig(AppConstant.BAIDU_CLIENT_SECRET, clientSecret);
    }

    public static void updateMinPlaySecond(Integer minPlaySecond) {
        MIN_PLAY_SECOND = minPlaySecond;
        configService.addOrUpdateConfig(AppConstant.MIN_PLAY_SECOND, String.valueOf(minPlaySecond));
    }

    public static void updateMid(String mid) {
        MID = mid;
        configService.addOrUpdateConfig(AppConstant.MID_KEY, mid);

    }

    /**
     * 功能的开关
     */
    public static void initSettings() {
        //定时任务开关
        CRON = Boolean.parseBoolean(Opt.ofNullable(configService.findByName(AppConstant.CRON)).orElse("false"));
    }

    public static boolean isCron() {
        return CRON;
    }

    public static void setCron(boolean cron) {
        GlobalVariables.CRON = cron;
        configService.addOrUpdateConfig(AppConstant.CRON,
                String.valueOf(cron)
        );
    }

    public static void setIsLogin(boolean isLogin) {
        BILI_LOGIN = isLogin;
    }

    /**
     * 删除原本的header 重新存储
     *
     * @param commonHeaderMap
     */
    @Transactional(rollbackFor = ServerException.class)
    public void replaceCommonHeaderMap(Map<String, String> commonHeaderMap) {

        COMMON_HEADER_MAP = commonHeaderMap;
        cookieHeaderDataService.removeAllCommonHeader();

        //重新保存新的数据
        cookieHeaderDataService.saveCommonHeaderMap(commonHeaderMap);
    }



    /**
     * 添加一个黑名单用户id
     *
     * @param mid
     */
    public void addBlackUserId(String mid) {

        if (GlobalVariables.BLACK_USER_ID_SET.contains(mid)) {
            return;
        }
        Dict dict = new Dict()
                .setDictType(DictType.MID)
                .setAccessType(AccessType.BLACK)
                .setValue(mid);
        dictService.save(dict);
        GlobalVariables.BLACK_USER_ID_SET.remove(mid);
        GlobalVariables.BLACK_USER_ID_SET.add(mid);
    }

    /**
     * 从缓存中读入数据，存储
     * <p>
     * 将这些标签的类型由CACHE 改为正常类型即可
     * 黑名单中加入新出现的标签
     */
    public void addBlackKeyWordFromCache(List<String> keywordIdSet) {

        //过滤掉忽略的关键词(无需，添加关键词时，如果匹配忽略关键词则不允许添加)

        //将这些标签的类型由CACHE 改为正常类型即可
        dictService.updateAccessTypeByIdIn(
                AccessType.BLACK,
                keywordIdSet
        );

        List<String> valueList = Dict.transferToValue(dictService.findByIdIn(keywordIdSet));
        BLACK_KEYWORD_SET.removeAll(valueList);
        BLACK_KEYWORD_SET.addAll(valueList);
        BLACK_KEYWORD_TREE.addWords(valueList);
    }

    /**
     * 将缓存中的Tag 加入正常的Tag列表中
     *
     * @param tagNameIdList
     */
    public void addBlackTagFromCache(List<String> tagNameIdList) {

        //将这些标签的类型由CACHE 改为正常类型即可
        dictService.updateAccessTypeByIdIn(
                AccessType.BLACK,
                tagNameIdList
        );

        List<String> valueList = Dict.transferToValue(dictService.findByIdIn(tagNameIdList));
        BLACK_TAG_SET.removeAll(valueList);
        BLACK_TAG_SET.addAll(valueList);
        BLACK_TAG_TREE.addWords(valueList);
    }

    /**
     * 新增tid
     *
     * @param param
     */
    public void addBlackTidSet(Set<String> param) {
        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.TID,
                null,
                param);

        //新增到缓存
        BLACK_TID_SET.removeAll(param);
        BLACK_TID_SET.addAll(param);
    }

    /**
     * 新增黑名单关键词
     *
     * @param keywordCol
     */
    public void addBlackKeyword(Collection<String> keywordCol) {


        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.KEYWORD,
                null,
                keywordCol);

        //新增到缓存
        BLACK_KEYWORD_SET.removeAll(keywordCol);
        BLACK_KEYWORD_SET.addAll(keywordCol);
        BLACK_KEYWORD_TREE.addWords(keywordCol);

    }

    public void addBlackUserIdSet(Set<String> blackUserIdSet) {

        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.MID,
                null,
                blackUserIdSet);

        //新增到缓存
        BLACK_USER_ID_SET.removeAll(blackUserIdSet);
        BLACK_USER_ID_SET.addAll(blackUserIdSet);
    }

    public void addBlackTagSet(Set<String> collect) {

        dictService.removeAndAddDict(
                AccessType.BLACK,
                DictType.TAG,
                null,
                collect);

        //新增到缓存
        BLACK_TAG_SET.removeAll(collect);
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
    public void addBlackIgnoreKeyword(Set<String> collect) {
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

    public void removeBlackTag(Set<String> param) {
        for (String s : param) {
            BLACK_TAG_SET.remove(s);
            //remove没有效果,重新构建
            initBlackTagTree(BLACK_TAG_SET);
        }
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.TAG,
                param
        );
    }

    private void initBlackTagTree(Collection<String> blackTagSet) {
        BLACK_TAG_TREE = new WordTree();
        BLACK_TAG_TREE.addWords(blackTagSet);
    }

    public void removeBlackKeyword(Set<String> param) {

        for (String s : param) {
            BLACK_KEYWORD_SET.remove(s);
            BLACK_KEYWORD_TREE = new WordTree();
            BLACK_KEYWORD_TREE.addWords(BLACK_KEYWORD_SET);
        }
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.KEYWORD,
                param
        );
    }

    /**
     * 添加或更新白名单
     *
     * @param whitelistRule
     */
    @Transactional(rollbackFor = ServerException.class)
    public void addOrUpdateWhitelitRule(WhiteListRule whitelistRule) {

        if (StrUtil.isNotBlank(whitelistRule.getId())) {
            WHITELIST_RULE_LIST.remove(whitelistRule);
        }
        //修改主对象
        whiteListRuleService.saveOrUpdate(whitelistRule);
        //修改关联的数据
        dictService.updateByWhiteListRule(whitelistRule);

        WHITELIST_RULE_LIST.add(whitelistRule);

    }


    /**
     * 根据id删除白名单
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = ServerException.class)
    public boolean removeWhitelistRules(String id) {

        WHITELIST_RULE_LIST = WHITELIST_RULE_LIST.stream()
                .filter(whiteListRule -> !id.equals(whiteListRule.getId()))
                .collect(Collectors.toList());

        boolean result = whiteListRuleService.removeById(id);

        //删除关联的数据
        dictService.removeByOuterId(id);

        return result;
    }

    /**
     * 添加白名单忽略关键词
     *
     * @param ignoreKeyWordSet
     */
    @Transactional
    public void addWhiteIgnoreKeyword(Set<String> ignoreKeyWordSet) {

        if (ignoreKeyWordSet == null) {
            return;
        }
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

    private void removeWhiteCoverKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.COVER,
                ignoreKeyWordSet
        );

        //缓存层面的删除
        for (WhiteListRule whiteListRule : WHITELIST_RULE_LIST) {

            List<Dict> coverKeyword =
                    Opt.ofNullable(whiteListRule.getCoverKeyword())
                            .orElse(Collections.emptyList())
                    .stream()
                    .filter(
                            dict -> !ignoreKeyWordSet.contains(dict.getValue())
                    ).collect(Collectors.toList());

            whiteListRule.setCoverKeyword(coverKeyword);
        }
    }

    private void removeWhiteTitleKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.TITLE,
                ignoreKeyWordSet
        );

        //缓存层面的删除
        for (WhiteListRule whiteListRule : WHITELIST_RULE_LIST) {

            List<Dict> titleKeyWordList =
                    Opt.ofNullable(whiteListRule.getTitleKeyWordList())
                            .orElse(Collections.emptyList())

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

            List<Dict> descKeyWordList =
                    Opt.ofNullable(whiteListRule.getDescKeyWordList())
                            .orElse(Collections.emptyList())
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
            List<Dict> tagNameList = Opt.ofNullable(whiteListRule.getTagNameList())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(
                            dict ->    !ignoreKeyWordSet.contains(dict.getValue())
                    ).collect(Collectors.toList());
            whiteListRule.setTagNameList(tagNameList);
        }

    }

    public static void updateAccessKey(String newKey) {
        BILI_ACCESS_KEY = newKey;
        configService.addOrUpdateConfig(AppConstant.BILI_ACCESS_KEY, newKey, 2_505_600);
    }

    public static void updateWbi(String imgKey, String subKey) {

        IMG_KEY = imgKey;
        SUB_KEY = subKey;

        configService.addOrUpdateConfig(AppConstant.IMG_KEY, imgKey, 72_000);
        configService.addOrUpdateConfig(AppConstant.SUB_KEY, subKey, 72_000);
    }

    public static String getImgKey() {
        return IMG_KEY;
    }

    public static String getSubKey() {
        return SUB_KEY;
    }




    public void updateCommonHeaderMap(Map<String, String> commonHeaderMap) {
        COMMON_HEADER_MAP.putAll(commonHeaderMap);
        //删除同名的
        Set<String> keySet = commonHeaderMap.keySet();
        cookieHeaderDataService.removeByKeyInAndClassifyAndMediaType(keySet, Classify.REQUEST_HEADER, MediaType.GENERAL);
        //重新保存
        cookieHeaderDataService.saveCommonHeaderMap(commonHeaderMap);
    }




    @Transactional(rollbackFor = ServerException.class)
    public void setWhiteUserIdSet(Collection<String> whiteUserIdSet) {
        //删除以前的
        dictService.removeAllWhiteUserId();

        //添加全部
        dictService.addWhiteUserId(whiteUserIdSet);

        GlobalVariables.WHITE_USER_ID_SET = new ArrayList<>(whiteUserIdSet);

    }


    /**
     * 白名单分区
     *
     * @param whiteTidSet
     */
    public void addWhiteTidSet(Set<String> whiteTidSet) {
        dictService.removeAndAddDict(
                AccessType.WHITE,
                DictType.TID,
                null,
                whiteTidSet);

        //新增到缓存
        WHITE_TID_SET.removeAll(whiteTidSet);
        WHITE_TID_SET.addAll(whiteTidSet);
    }

    /**
     * 添加搜索关键词
     *
     * @param searchKeywords
     */
    public void addSearchKeyword(Collection<String> searchKeywords) {

        dictService.removeAndAddDict(
                AccessType.OTHER,
                DictType.SEARCH_KEYWORD,
                null,
                searchKeywords);

        //新增到缓存
        SEARCH_KEYWORD_SET.removeAll(searchKeywords);
        SEARCH_KEYWORD_SET.addAll(searchKeywords);

    }


}