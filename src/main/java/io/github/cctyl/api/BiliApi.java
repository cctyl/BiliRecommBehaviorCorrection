package io.github.cctyl.api;

import cn.hutool.core.collection.CollUtil;
import io.github.cctyl.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * bilibili相关api都在这里
 * 不打算使用封装层数过多的 retrofit
 * restTemplate 对于增加请求体等需求又比较复杂
 */
@Component
public class BiliApi {


    @Autowired
    private RedisUtil redisUtil;

    /**
     * cookie
     */
    private Map<String,String> cookies = new HashMap<>(10);


    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        //0.加载cookie
        Map<Object, Object> cookiesFromRedis = redisUtil.hGetAll(COOKIES_KEY);
        if (CollUtil.isNotEmpty(cookiesFromRedis)){
            cookiesFromRedis.keySet().forEach(o -> {
                cookies.put( (String)o, (String) cookiesFromRedis.get(o));
            });
        }

    }
}
