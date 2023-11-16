package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.cctyl.entity.Config;
import io.github.cctyl.mapper.ConfigMapper;
import io.github.cctyl.pojo.constants.AppConstant;
import io.github.cctyl.service.ConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.utils.DataUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.Set;

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


    public Config findConfigByName(String name) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>()
                .select(Config::getValue)
                .eq(Config::getName, name);


        Config config = this.getOne(wrapper);


        if (config != null && config.getExpireSecond()>0) {

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
