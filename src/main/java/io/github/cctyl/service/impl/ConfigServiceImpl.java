package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cctyl.entity.Config;
import io.github.cctyl.mapper.ConfigMapper;
import io.github.cctyl.service.ConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Override
    public String findByName(String name) {

        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>()
                .select(Config::getValue)
                .eq(Config::getName, name);
        Config one = this.getOne(wrapper);
        return one.getValue();
    }
}
