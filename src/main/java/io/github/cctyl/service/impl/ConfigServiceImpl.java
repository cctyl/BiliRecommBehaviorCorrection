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
        if (configDTO.getCron()!=null){
            GlobalVariables.setCron(configDTO.getCron());
        }

        //其他配置暂不允许更新

        return getStandardConfigInfo()

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

    /**
     * 从redis迁移数据
     * @return
     */
    @Override
    public ConfigVo migrationFromRedis() {
        /*
         1) "bili:suspicious_cookie"
         2) "bili:handle_video_id_list"
         3) "bili:ready_handle_dislike_video"
         4) "bili:common_cookie"
         5) "bili:white_user_ids"
         6) "stop_words"
         7) "bili:ready_handle_video_id"
         8) "bili:ignore_white_keyword"
         9) "bili:white_list_rule"
        10) "bili:black_tags"
        11) "bili:mid"
        12) "bili:api_header"
        13) "bili:common_header"
        14) "bili:black_keywords"
        15) "bili:handle_video_detail_list"
        16) "bili:ignore_black_keyword"
        17) "bili:ready_handle_thumb_up_video"
        18) "bili:black_user_ids"
        19) "bili:cookies"
        20) "bili:black_tids"
        21) "baidu_accesskey"
        22) "bili:white_tids"
        23) "bili:ready_handle_video"
        24) "bili:keywords"

         */
    }
}
