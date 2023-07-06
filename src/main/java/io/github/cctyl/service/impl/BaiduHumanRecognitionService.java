package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.cctyl.api.BaiduApi;
import io.github.cctyl.entity.BaiduImageClassify;
import io.github.cctyl.service.ImageGenderDetectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.List;

/**
 * 百度人体分析api
 */
@ConditionalOnExpression("'${common.imgService}'.contains('baidu')")
@Service
@Slf4j
public class BaiduHumanRecognitionService implements ImageGenderDetectService {

    @Autowired
    private BaiduApi baiduApi;

    /**
     * 判断性别 1 男性 2 女性 3其他表示未识别成功 4表示出现异常
     * @param bytes
     * @return
     */
    public int getGender(byte[] bytes) {
        try {
            BaiduImageClassify baiduImageClassify = baiduApi.getGender(baiduApi.getFileContentAsBase64(bytes));
            List<BaiduImageClassify.PersonInfo> personInfoList = baiduImageClassify.getPersonInfo();
            if (baiduImageClassify.getPersonNum()>0 && CollUtil.isNotEmpty(personInfoList)) {
                boolean match = personInfoList.stream().map(p -> p.getAttributes().getGender().getName())
                        .anyMatch("女性"::equals);
                return match ? 2 : 1;
            } else {
                log.error("识别错误");
                return 3;
            }

        } catch (Exception e) {
            log.error("识别异常！msg={}", e.getMessage());
            e.printStackTrace();
            return 4;
        }
    }

    @Override
    public boolean isHuman(byte[] bytes) {
        return baiduApi.isHuman(baiduApi.getFileContentAsBase64(bytes));
    }
}
