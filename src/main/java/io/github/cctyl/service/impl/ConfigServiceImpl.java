package io.github.cctyl.service.impl;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.po.*;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.mapper.ConfigMapper;
import io.github.cctyl.domain.constants.AppConstant;
import io.github.cctyl.service.ConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Consumer;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    private final BiliApi biliApi;

    private final VideoDetailService videoDetailService;

    private final CookieHeaderDataService cookieHeaderDataService;



    @Override
    public String findByName(String name) {
        Config config = findConfigByName(name);

        return config == null ? null : config.getValue();
    }

    @Override
    public Config addOrUpdateConfig(String configName, String configValue) {
        Config config = findConfigByName(configName);

        if (config != null) {
            config.setValue(configValue);
        } else {
            config = new Config()
                    .setName(configName)
                    .setValue(configValue);
        }
        this.saveOrUpdate(config);
        return config;
    }

    @Override
    public Config addOrUpdateConfig(String configName, String configValue, Integer expireSecond) {

        if (configValue==null){
            LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>().eq(
                Config::getName,configName
            );
            this.remove(wrapper);
            return null;
        }

        Config config = findConfigByName(configName);
        if (config != null) {
            config.setValue(configValue)
                    .setExpireSecond(expireSecond)
            ;
        } else {
            config = new Config()
                    .setName(configName)
                    .setValue(configValue)
                    .setExpireSecond(expireSecond)
            ;
        }
        this.saveOrUpdate(config);
        return config;
    }

    /**
     * 是否第一次使用
     * 如果数据库中不存在该记录，则返回true，并且创建一个记录，设置为false
     *
     * @return
     */
    @Override
    public boolean isFirstUse() {
        Config configByName = findConfigByName(AppConstant.FIRST_USE);
        if (configByName == null) {
            configByName = new Config()
                    .setName(AppConstant.FIRST_USE)
                    .setValue("false");
            this.save(configByName);
            return true;
        }

        return Boolean.parseBoolean(configByName.getValue());
    }

    /**
     * @param cookieStr
     * @return
     */
    @Override
    public Map<String, String> updateRefreshCookie(String cookieStr) {
        Map<String, String> cookieMap = DataUtil.splitCookie(cookieStr);
        GlobalVariables.updateRefreshCookie(cookieMap);
        //立即持久化
        cookieHeaderDataService.replaceRefreshCookie(GlobalVariables.getRefreshCookieMap());
        return GlobalVariables.getRefreshCookieMap();
    }

    /**
     * @param configDTO
     * @return
     */
    @Override
    public ConfigVo updateStandardConfigInfo(ConfigDTO configDTO) {
//TODO 改为列表批量修改
        if (configDTO.getBiliAccessKey() != null) {
            GlobalVariables.updateAccessKey(configDTO.getBiliAccessKey());
        }
        if (configDTO.getBaiduClientId() != null &&
                configDTO.getBaiduClientSecret() != null
        ) {
            GlobalVariables.updateBaiduClientInfo(configDTO.getBaiduClientId(), configDTO.getBaiduClientSecret());
        }

        if (configDTO.getBaiduAskKey() != null) {
            GlobalVariables.updateBaiduAskKey(configDTO.getBaiduAskKey());
        }

        if (configDTO.getMinPlaySecond() != null) {
            GlobalVariables.updateMinPlaySecond(configDTO.getMinPlaySecond());
        }

        if (configDTO.getMid() != null) {
            GlobalVariables.updateMid(configDTO.getMid());
        }
        if (configDTO.getCron() != null) {
            GlobalVariables.setCron(configDTO.getCron());
        }

        //其他配置暂不允许更新

        return getStandardConfigInfo();

    }


    public Config findConfigByName(String name) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>()
                .select(Config::getId, Config::getValue)
                .eq(Config::getName, name);


        Config config = this.getOne(wrapper);


        if (Opt.ofNullable(config).map(Config::getExpireSecond).orElse(-1) > 0) {

            Integer differenceSecond =
                    DataUtil.calculateSecondsDifference(
                            new Date(),
                            config.getLastModifiedDate()
                    );
            if (differenceSecond > config.getExpireSecond()) {
                return null;
            }
        }

        return config;

    }

    @Override
    public ConfigVo getStandardConfigInfo() {
        return new ConfigVo()
                .setMid(GlobalVariables.getMID())
                .setImgKey(GlobalVariables.getImgKey())
                .setSubKey(GlobalVariables.getSubKey())
                .setBaiduClientId(GlobalVariables.getBaiduClientId())
                .setBaiduAskKey(GlobalVariables.getBaiduAskKey())
                .setBaiduClientSecret(GlobalVariables.getBaiduClientSecret())
                .setBiliAccessKey(GlobalVariables.getBiliAccessKey())
                .setMinPlaySecond(GlobalVariables.getMinPlaySecond())
                .setCron(GlobalVariables.isCron())
                ;
    }

    public void runTask(Consumer<Integer> consumer) {
        consumer.accept(-1);
    }

    @Override
    public String getWebLoginQrCode() {
        return biliApi.getWebLoginQrCode();
    }

    @Override
    public Object getWebLoginQrCodeScanResult() {
        return   biliApi.getWebLoginQrCodeScanResult();
    }


    @Override
    public String getTvLoginQrCode() {
        return biliApi.getTvLoginQrCode();
    }

    @Override
    public Object getTvLoginQrCodeScanResult() {
        return biliApi.getTvQrCodeScanResult();
    }

    @Override
    public List<Config> getConfigList() {
        return this.list();
    }

    /**
     * 更新配置项列表
     * 此方法用于批量更新配置项，通过接收一个配置对象列表来实现配置的批量修改或添加
     * 主要用途是当有一批新的配置需要应用或者现有配置发生变更时，通过调用此方法来更新系统内的配置信息
     *
     * @param configList 一个包含多个Config对象的列表，用于更新系统配置
     */
    @Override
    public void updateConfigList(List<Config> configList) {

        this.saveOrUpdateBatch(configList);

    }
}

