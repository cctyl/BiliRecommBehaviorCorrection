package io.github.cctyl.service.impl;

import io.github.cctyl.service.ImageGenderDetectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnExpression("'${common.imgService}'==null || '${common.imgService}'.length()==0 ")
public class ImageGenderDetectServiceDefaultImpl implements ImageGenderDetectService {

    /**
     * 默认的实现
     * @param bytes
     * @return
     */
    @Override
    public boolean isHuman(byte[] bytes) {
        log.error("警告！你未配置图像识别接口，请检查配置文件，并配置一个可用的图像识别接口");
        return false;
    }
}
