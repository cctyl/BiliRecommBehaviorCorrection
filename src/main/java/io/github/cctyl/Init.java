package io.github.cctyl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.service.BiliService;
import io.github.cctyl.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import static io.github.cctyl.constants.AppConstant.*;

@Slf4j
@Component
@ConditionalOnExpression("${common.init}==true")
public class Init implements ApplicationRunner {

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
        if (StrUtil.isBlankIfStr(redisUtil.get(COOKIES_KEY)) ){
            if (StrUtil.isNotEmpty(defaultData.getCookie())){
                throw new RuntimeException("未配置初始化cookie！");
            }
            biliApi.replaceCookie(defaultData.getCookie());
        }

        //2.同理，加载黑名单
        if (redisUtil.sMembers(BLACK_TID_KEY).size()==0){
            redisUtil.sAdd(BLACK_TID_KEY,defaultData.getBlackTid().toArray(new String[0]));
        }
        if (redisUtil.sMembers(BLACK_USER_ID_KEY).size()==0){
            redisUtil.sAdd(BLACK_USER_ID_KEY,defaultData.getBlackUserId().toArray(new String[0]));
        }
        if (redisUtil.sMembers(BLACK_TAG_KEY).size()==0){
            redisUtil.sAdd(BLACK_TAG_KEY,defaultData.getBlackTag().toArray(new String[0]));
        }
        if (redisUtil.sMembers(BLACK_KEY_WORD_KEY).size()==0){
            redisUtil.sAdd(BLACK_KEY_WORD_KEY,defaultData.getBlackKeyWord().toArray(new String[0]));
        }
        if (redisUtil.sMembers(KEY_WORD_KEY).size()==0){
            redisUtil.sAdd(KEY_WORD_KEY,defaultData.getKeyWord().toArray(new String[0]));
        }
        if (redisUtil.get(MID_KEY)==null){
            redisUtil.set(MID_KEY,defaultData.getMid());
        }
        if (redisUtil.sMembers(WHITE_TID_KEY).size()==0){
            redisUtil.sAdd(WHITE_TID_KEY,defaultData.getWhiteTid().toArray(new String[0]));
        }
        if (redisUtil.sMembers(WHITE_USER_ID_KEY).size()==0){
            redisUtil.sAdd(WHITE_USER_ID_KEY,defaultData.getWhiteTid().toArray(new String[0]));
        }
    }
}
