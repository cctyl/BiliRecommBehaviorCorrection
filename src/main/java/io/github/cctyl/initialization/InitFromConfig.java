package io.github.cctyl.initialization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static io.github.cctyl.pojo.constants.AppConstant.*;

@Slf4j
@Component
@ConditionalOnExpression("${common.init}==true")
@Order(0)
public class InitFromConfig implements ApplicationRunner {

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private BiliApi biliApi;

    @Autowired
    private ApplicationProperties applicationProperties;



    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===========开始执行初始化任务========");

        ApplicationProperties.DefaultData defaultData = applicationProperties.getDefaultData();
        //1.如果redis中没有cookie，从配置文件中读取Cookie
        if (CollUtil.isEmpty(redisUtil.hGetAll(COOKIES_KEY))){
            log.debug("从配置文件中加载cookie...");
            if (StrUtil.isEmpty(defaultData.getCookie())){
                throw new RuntimeException("未配置初始化cookie！");
            }
            biliApi.replaceCookie(defaultData.getCookie());
        }

        //2.同理，加载黑名单
        if (redisUtil.sMembers(BLACK_TID_KEY).size()==0){
            log.debug("从配置文件中加载BLACK_TID_KEY...");
            redisUtil.sAdd(BLACK_TID_KEY,defaultData.getBlackTid().toArray(new String[0]));
        }
        if (redisUtil.sMembers(BLACK_USER_ID_KEY).size()==0){
            log.debug("从配置文件中加载BLACK_USER_ID_KEY...");
            redisUtil.sAdd(BLACK_USER_ID_KEY,defaultData.getBlackUserId().toArray(new String[0]));
        }
        if (redisUtil.sMembers(BLACK_TAG_KEY).size()==0){
            log.debug("从配置文件中加载 BLACK_TAG_KEY...");
            redisUtil.sAdd(BLACK_TAG_KEY,defaultData.getBlackTag().toArray(new String[0]));
        }
        if (redisUtil.sMembers(BLACK_KEY_WORD_KEY).size()==0){
            log.debug("从配置文件中加载 BLACK_KEY_WORD_KEY...");
            redisUtil.sAdd(BLACK_KEY_WORD_KEY,defaultData.getBlackKeyWord().toArray(new String[0]));
        }
        if (redisUtil.sMembers(KEY_WORD_KEY).size()==0){
            log.debug("从配置文件中加载 KEY_WORD_KEY...");
            redisUtil.sAdd(KEY_WORD_KEY,defaultData.getKeyWord().toArray(new String[0]));
        }
        if (redisUtil.get(MID_KEY)==null){
            log.debug("从配置文件中加载 MID_KEY...");
            redisUtil.set(MID_KEY,defaultData.getMid());
        }
        if (redisUtil.sMembers(WHITE_TID_KEY).size()==0){
            log.debug("从配置文件中加载 WHITE_TID_KEY...");
            redisUtil.sAdd(WHITE_TID_KEY,defaultData.getWhiteTid().toArray(new String[0]));
        }
        if (redisUtil.sMembers(WHITE_USER_ID_KEY).size()==0){
            log.debug("从配置文件中加载 WHITE_USER_ID_KEY...");
            redisUtil.sAdd(WHITE_USER_ID_KEY,defaultData.getWhiteTid().toArray(new String[0]));
        }
        if (redisUtil.sMembers(WHITE_LIST_RULE_KEY).size()==0){
            log.debug("从配置文件中加载 WHITE_LIST_RULE_KEY...");
            redisUtil.sAdd(WHITE_LIST_RULE_KEY,defaultData.getWhitleRuleList().toArray());
        }
    }
}
