package io.github.cctyl.service;


/**
 * 性别检测服务
 */
public interface ImageGenderDetectService {

    /**
     * 判断性别 1 男性 2 女性 3其他表示未识别成功 4表示出现异常
     *
     * @param bytes
     * @return
     */
    default int getGender(byte[] bytes) {
        throw new RuntimeException("not implement");
    }

}
