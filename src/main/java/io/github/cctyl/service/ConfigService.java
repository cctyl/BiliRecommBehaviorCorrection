package io.github.cctyl.service;

import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.constants.AppConstant;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.po.Config;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.domain.vo.OverviewVo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
public interface ConfigService extends IService<Config> {
    int getMinPlaySecond();

    String getBiliAccessKey();

    String getBaiduAskKey();

    void setCron(boolean cron);

    boolean isCron();

    String getSubKey();

    /**
     * 加载一些标记信息
     */
    void setInfo();

    void updateBaiduAskKey(String accessToken);

    void updateWbi(String imgKey, String subKey);

    String getBaiduClientId();

    String getBaiduClientSecret();

    void updateBaiduClientInfo(String clientId, String clientSecret);

    String findByName(String mid);

    String getImgKey();

    void updateAccessKey(String newKey);

    void updateMinPlaySecond(Integer minPlaySecond);

    Config addOrUpdateConfig(String configName, String configValue);

    Config addOrUpdateConfig(String configName, String configValue, Integer expireSecond);

    String getMID();

    void updateMid(String mid);

    boolean isFirstUse();

    Map<String, String> updateRefreshCookie(String cookieStr);

    ConfigVo updateStandardConfigInfo(ConfigDTO configDTO);

    ConfigVo getStandardConfigInfo();



    List<Config> getConfigList();

    void updateConfigList(List<Config> configList);

    void fillOverviewInfo(OverviewVo overviewVo);
}
