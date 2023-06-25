package io.github.cctyl.service;

import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 相关任务处理
 */
@Service
@Slf4j
public class BiliService {


    @Autowired
    private BiliApi biliApi;


    /**
     * 检查cookie状态
     * 调用历史记录接口来实现
     * @return true 有效  false 无效
     */
    public boolean checkCookie(){
        JSONObject history = biliApi.getHistory();
        log.info("检查cookie状态：{}",history.toString());
        return history.getIntValue("code")==0;
    }

}
