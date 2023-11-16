package io.github.cctyl.initialization;

import io.github.cctyl.config.GlobalVariables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@Order(1)
public class InitFromRedis implements ApplicationRunner {


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("初始化...从sqlite中加载数据...");

        //0.加载cookie
        GlobalVariables.initApiHeaderMap();


        //0.1加载mid
        GlobalVariables.initMid();

        //12.忽略关键词加载
        GlobalVariables.initIgnoreKeyWord();

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
        GlobalVariables.initMinPlaySecond();

        //9.白名单关键词列表
        GlobalVariables.initWhitelistRules();

        //10.加载停顿词
        GlobalVariables.initStopWords();

        //11.加载ApiHeader相关
        GlobalVariables.initApiHeaderMap();



        log.debug("初始化...加载完毕...");
    }
}
