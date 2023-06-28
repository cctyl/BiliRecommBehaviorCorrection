package io.github.cctyl.service;


/**
 * 性别检测服务
 */
public interface ImageGenderDetectService {

    /**
     * 判断性别 1 男性 2 女性 其他表示未识别成功
     * @param url
     * @return
     */
    int getGender(String url);

}
