package io.github.cctyl.service.impl;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.po.Config;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.mapper.ConfigMapper;
import io.github.cctyl.domain.constants.AppConstant;
import io.github.cctyl.service.ConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.utils.DataUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

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
        if (configByName==null){
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
        return GlobalVariables.getRefreshCookieMap();
    }

    /**
     * @param configDTO
     * @return
     */
    @Override
    public ConfigVo updateStandardConfigInfo(ConfigDTO configDTO) {

        if (configDTO.getBiliAccessKey()!=null){
            GlobalVariables.updateAccessKey(configDTO.getBiliAccessKey());
        }
        if (configDTO.getBaiduClientId()!=null &&
            configDTO.getBaiduClientSecret()!=null
        ){
            GlobalVariables.updateBaiduClientInfo(configDTO.getBaiduClientId(),configDTO.getBaiduClientSecret());
        }

        if (configDTO.getBaiduAskKey()!=null){
            GlobalVariables.updateBaiduAskKey(configDTO.getBaiduAskKey());
        }

        if (configDTO.getMinPlaySecond()!=null){
            GlobalVariables.updateMinPlaySecond(configDTO.getMinPlaySecond());
        }

        if (configDTO.getMid()!=null){
            GlobalVariables.updateMid(configDTO.getMid());
        }

        //其他配置暂不允许更新

        return new ConfigVo()
                .setMid(GlobalVariables.getMID())
                .setImgKey(GlobalVariables.getImgKey())
                .setSubKey(GlobalVariables.getSubKey())
                .setBaiduClientId(GlobalVariables.getBaiduClientId())
                .setBaiduAskKey(GlobalVariables.getBaiduAskKey())
                .setBaiduClientSecret(GlobalVariables.getBaiduClientSecret())
                .setBiliAccessKey(GlobalVariables.getBiliAccessKey())
                .setMinPlaySecond(GlobalVariables.getMinPlaySecond());

    }


    public Config findConfigByName(String name) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>()
                .select(Config::getValue)
                .eq(Config::getName, name);


        Config config = this.getOne(wrapper);


        if (Opt.ofNullable(config).map(Config::getExpireSecond).orElse(-1) >0) {

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


}
