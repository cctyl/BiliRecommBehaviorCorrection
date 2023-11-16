package io.github.cctyl.service;

import io.github.cctyl.entity.Config;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
public interface ConfigService extends IService<Config> {

    String findByName(String mid);

    Config addOrUpdateConfig(String configName,String configValue);

    Config addOrUpdateConfig(String configName,String configValue,Integer expireSecond);
}
