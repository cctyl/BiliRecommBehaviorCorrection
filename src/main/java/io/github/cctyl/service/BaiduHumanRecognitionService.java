package io.github.cctyl.service;

import io.github.cctyl.api.BaiduApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * 百度人体分析api
 */
@ConditionalOnExpression("'${api.service}'.contains('baidu')")
@Service
public class BaiduHumanRecognitionService implements ImageGenderDetectService{

    @Autowired
    private BaiduApi baiduApi;

    @Override
    public int getGender(String url) {
        return 0;
    }
}
