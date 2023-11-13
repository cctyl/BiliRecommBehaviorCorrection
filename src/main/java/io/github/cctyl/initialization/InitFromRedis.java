package io.github.cctyl.initialization;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.entity.WhiteListRule;
import io.github.cctyl.service.ConfigService;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.service.DictService;
import io.github.cctyl.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.cctyl.pojo.constants.AppConstant.*;

@Slf4j
@Component
@Order(1)
public class InitFromRedis implements ApplicationRunner {

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private CookieHeaderDataService cookieHeaderDataService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private DictService dictService;

    public void runback(ApplicationArguments args) throws Exception {
        log.debug("初始化...从redis中加载数据...");
        //0.加载cookie
        Map<Object, Object> cookiesFromRedis = redisUtil.hGetAll(COOKIES_KEY);
        if (CollUtil.isNotEmpty(cookiesFromRedis)) {
            for (Map.Entry<Object, Object> entry : cookiesFromRedis.entrySet()) {
                GlobalVariables.cookieMap.put((String) entry.getKey(), (String) entry.getValue());
            }
        } else {
            throw new RuntimeException("cookie为空");
        }

        GlobalVariables.mid = (String) redisUtil.get(MID_KEY);
        if (GlobalVariables.mid == null) {
            throw new RuntimeException("mid 为空");
        }

        if (GlobalVariables.cookieMap.get("bili_jct") == null) {
            throw new RuntimeException("csrf 为空");
        }

        //1. 加载关键字数据
        GlobalVariables.setKeywordSet(redisUtil.sMembers(KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //2. 加载黑名单用户id列表
        GlobalVariables.setBlackUserIdSet(redisUtil.sMembers(BLACK_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //3. 加载黑名单关键词列表
        GlobalVariables.setBlackKeywordSet(redisUtil.sMembers(BLACK_KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //4. 加载黑名单分区id列表
        GlobalVariables.setBlackTidSet(redisUtil.sMembers(BLACK_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //5.黑名单标签列表
        GlobalVariables.setBlackTagSet(redisUtil.sMembers(BLACK_TAG_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //6.白名单用户id
        GlobalVariables.setWhiteUserIdSet(redisUtil.sMembers(WHITE_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //7.白名单分区id
        GlobalVariables.setWhiteTidSet(redisUtil.sMembers(WHITE_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet()));

        //8.最小播放时间
        if (applicationProperties.getMinPlaySecond() == null) {
            applicationProperties.setMinPlaySecond(50);
        }

        //9.白名单关键词列表
        GlobalVariables.setWhitelistRules(
                redisUtil.sMembers(WHITE_LIST_RULE_KEY)
                        .stream().map(
                            o -> (WhiteListRule) o
                        ).collect(Collectors.toList())
        );

        //10.加载停顿词
        if (redisUtil.sMembers(STOP_WORDS_KEY).size() == 0) {
            ClassPathResource classPathResource = new ClassPathResource("cn_stopwords.txt");
            redisUtil.sAdd(STOP_WORDS_KEY, Files.lines(Paths.get(classPathResource.getFile().getPath())).toArray());
        }

        //11.加载ApiHeader相关
        for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(API_HEADER_MAP).entrySet()) {
            GlobalVariables.apiHeaderMap.put((String) entry.getKey(), (ApiHeader) entry.getValue());
        }

        for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COMMON_COOKIE_MAP).entrySet()) {
            GlobalVariables.commonCookieMap.put((String) entry.getKey(), (String) entry.getValue());
        }

        for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COMMON_HEADER_MAP).entrySet()) {
            GlobalVariables.commonHeaderMap.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("初始化...从sqlite中加载数据...");

        //0.加载cookie
        Map<String, String> totalCookieMap =   cookieHeaderDataService.findCookieMap();
        GlobalVariables.cookieMap.putAll(totalCookieMap);

        //0.1加载mid
        GlobalVariables.mid = configService.findByName("mid");


        //1. 加载关键字数据
        GlobalVariables.initKeywordSet();


        //2. 加载黑名单用户id列表
        GlobalVariables.initBlackUserIdSet();


        //3. 加载黑名单关键词列表
        GlobalVariables.initBlackKeywordSet();


        //4. 加载黑名单分区id列表
        GlobalVariables.initBlackTidSet();

        //5.黑名单标签列表
        GlobalVariables.initBlackTagSet();

        //6.白名单用户id
        GlobalVariables.initWhiteUserIdSet();

        //7.白名单分区id
        GlobalVariables.initWhiteTidSet();


        //8.最小播放时间
        String minPlaySecond = configService.findByName("minPlaySecond");
        if (minPlaySecond!=null){
            try {
                GlobalVariables.minPlaySecond = Integer.parseInt(minPlaySecond);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }


        //9.白名单关键词列表
        GlobalVariables.initWhitelistRules();

        //10.加载停顿词
        GlobalVariables.initStopWords();


        //11.加载ApiHeader相关
        GlobalVariables.initApiHeaderMap();
        for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(API_HEADER_MAP).entrySet()) {
            GlobalVariables.apiHeaderMap.put((String) entry.getKey(), (ApiHeader) entry.getValue());
        }

        for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COMMON_COOKIE_MAP).entrySet()) {
            GlobalVariables.commonCookieMap.put((String) entry.getKey(), (String) entry.getValue());
        }

        for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COMMON_HEADER_MAP).entrySet()) {
            GlobalVariables.commonHeaderMap.put((String) entry.getKey(), (String) entry.getValue());
        }

    }
}
