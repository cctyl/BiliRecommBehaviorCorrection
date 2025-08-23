package io.github.cctyl.service;


/**
 * 性别检测服务
 */
public interface ImageGenderDetectService {

    /**
     * 识别图片中人像
     *
     * @param bytes
     * @return
     */
    default boolean isHuman(byte[] bytes) {
        throw new RuntimeException("not implement");
    }

}
