package io.github.cctyl.initialization;


import cn.hutool.core.collection.CollUtil;
import io.github.cctyl.config.ApplicationProperties;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.WhiteKeyWord;
import io.github.cctyl.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

@Slf4j
@Component
@Order(1)
public class InitFromRedis implements ApplicationRunner {

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //0.加载cookie
        Map<Object, Object> cookiesFromRedis = redisUtil.hGetAll(COOKIES_KEY);
        if (CollUtil.isNotEmpty(cookiesFromRedis)) {
            cookiesFromRedis.keySet().forEach(o -> {
                GlobalVariables.cookieMap.put((String) o, (String) cookiesFromRedis.get(o));
            });
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
        GlobalVariables.keywordSet = redisUtil.sMembers(KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //2. 加载黑名单用户id列表
        GlobalVariables.blackUserIdSet = redisUtil.sMembers(BLACK_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //3. 加载黑名单关键词列表
        GlobalVariables.blackKeywordSet = redisUtil.sMembers(BLACK_KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
        GlobalVariables.blackKeywordTree.addWords(GlobalVariables.blackKeywordSet);

        //4. 加载黑名单分区id列表
        GlobalVariables.blackTidSet = redisUtil.sMembers(BLACK_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //5.黑名单标签列表
        GlobalVariables.blackTagSet = redisUtil.sMembers(BLACK_TAG_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
        GlobalVariables.blackTagTree.addWords(GlobalVariables.blackTagSet);

        //6.白名单用户id
        GlobalVariables.whiteUserIdSet = redisUtil.sMembers(WHITE_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //7.白名单分区id
        GlobalVariables.whiteTidSet = redisUtil.sMembers(WHITE_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());

        //8.最小播放时间
        if (applicationProperties.getMinPlaySecond()==null){
            applicationProperties.setMinPlaySecond(50);
        }

        //9.白名单关键词列表
        GlobalVariables.whiteKeyWordList = redisUtil.sMembers(WHITE_KEY_WORD_KEY).stream().map(o -> (WhiteKeyWord)o).collect(Collectors.toList());

    }

}
