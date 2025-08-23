package io.github.cctyl.service.impl;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.po.*;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.mapper.ConfigMapper;
import io.github.cctyl.domain.constants.AppConstant;
import io.github.cctyl.service.ConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.service.VideoDetailService;
import io.github.cctyl.utils.DataUtil;
import io.github.cctyl.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

import static io.github.cctyl.domain.constants.AppConstant.FIRST_START_TIME;


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



    private final CookieHeaderDataService cookieHeaderDataService;
    private final TaskService taskService;

    public void updateMinPlaySecond(Integer minPlaySecond) {

        this.addOrUpdateConfig(AppConstant.MIN_PLAY_SECOND, String.valueOf(minPlaySecond));
    }

    public void updateAccessKey(String newKey) {

        this.addOrUpdateConfig(AppConstant.BILI_ACCESS_KEY, newKey, 2_505_600);
    }

    @Override
    public String getBaiduClientId() {
        return this.findByName(AppConstant.BAIDU_CLIENT_ID);
    }
    public  String getBaiduClientSecret() {
       return this.findByName(AppConstant.BAIDU_CLIENT_SECRET);
    }


    /**
     * 加载一些标记信息
     */
    public  void setInfo() {
        //1.是否第一次使用本系统
      boolean  FIRST_USE = this.isFirstUse();


        if (FIRST_USE) {
            //2.第一次使用系统时间
            this.addOrUpdateConfig(AppConstant.FIRST_START_TIME,
                    String.valueOf(Instant.now().toEpochMilli())
            );
            //3.定时任务开关
            setCron(false);
        }


    }
    
    public  void updateBaiduClientInfo(String clientId, String clientSecret) {
      
        this.addOrUpdateConfig(AppConstant.BAIDU_CLIENT_ID, clientId);
        this.addOrUpdateConfig(AppConstant.BAIDU_CLIENT_SECRET, clientSecret);
    }
    
    public void updateWbi(String imgKey, String subKey) {


        this.addOrUpdateConfig(AppConstant.IMG_KEY, imgKey, 72_000);
        this.addOrUpdateConfig(AppConstant.SUB_KEY, subKey, 72_000);
    }

    public void updateBaiduAskKey(String accessToken) {

        this.addOrUpdateConfig(AppConstant.BAIDU_ASK_KEY, accessToken, 2592000);
    }

    public String getSubKey() {
        return this.findByName(AppConstant.SUB_KEY);
    }

    public  boolean isCron() {
        return Boolean.parseBoolean(Opt.ofNullable(this.findByName(AppConstant.CRON)).orElse("false"));
    }
    public  void setCron(boolean cron) {
      
        this.addOrUpdateConfig(AppConstant.CRON,
                String.valueOf(cron)
        );
    }
    public String getBaiduAskKey() {
        return this.findByName(AppConstant.BAIDU_ASK_KEY);
    }

    public String getBiliAccessKey() {
        return this.findByName(AppConstant.BILI_ACCESS_KEY);
    }

    public int getMinPlaySecond() {
        String minPlaySecond = this.findByName(AppConstant.MIN_PLAY_SECOND);
        if (minPlaySecond != null) {
            try {
                return Integer.parseInt(minPlaySecond);
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
            }
        }
        return -1;
    }

    public String getImgKey() {
        return this.findByName(AppConstant.IMG_KEY);
    }

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

    public String getMID() {

        return this.findByName(AppConstant.MID_KEY);
    }

    public void updateMid(String mid) {
        this.addOrUpdateConfig(AppConstant.MID_KEY, mid);
    }

    @Override
    public Config addOrUpdateConfig(String configName, String configValue, Integer expireSecond) {

        if (configValue == null) {
            LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>().eq(
                    Config::getName, configName
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
        //立即持久化
        Map<String, String> refreshCookieMap = cookieHeaderDataService.getRefreshCookieMap();
        refreshCookieMap.putAll(cookieMap);
        cookieHeaderDataService.replaceRefreshCookie(refreshCookieMap);
        return cookieHeaderDataService.getRefreshCookieMap();
    }

    /**
     * @param configDTO
     * @return
     */
    @Override
    public ConfigVo updateStandardConfigInfo(ConfigDTO configDTO) {
        //TODO 改为列表批量修改
        if (configDTO.getBiliAccessKey() != null) {
            this.updateAccessKey(configDTO.getBiliAccessKey());
        }
        if (configDTO.getBaiduClientId() != null &&
                configDTO.getBaiduClientSecret() != null
        ) {
            this.updateBaiduClientInfo(configDTO.getBaiduClientId(), configDTO.getBaiduClientSecret());
        }

        if (configDTO.getBaiduAskKey() != null) {
            this.updateBaiduAskKey(configDTO.getBaiduAskKey());
        }

        if (configDTO.getMinPlaySecond() != null) {
            this.updateMinPlaySecond(configDTO.getMinPlaySecond());
        }

        if (configDTO.getMid() != null) {
            this.updateMid(configDTO.getMid());
        }
        if (configDTO.getCron() != null) {
            this.setCron(configDTO.getCron());
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
                .setMid(this.getMID())
                .setImgKey(this.getImgKey())
                .setSubKey(this.getSubKey())
                .setBaiduClientId(this.getBaiduClientId())
                .setBaiduAskKey(this.getBaiduAskKey())
                .setBaiduClientSecret(this.getBaiduClientSecret())
                .setBiliAccessKey(this.getBiliAccessKey())
                .setMinPlaySecond(this.getMinPlaySecond())
                .setCron(this.isCron())
                ;
    }

    public void runTask(Consumer<Integer> consumer) {
        consumer.accept(-1);
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
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigList(List<Config> configList) {

        configList.stream()
                .filter(config -> AppConstant.CRON.equals(config.getName()))
                .findFirst()
                .ifPresent(config -> {
                    taskService.updateTaskEnable("true".equals(config.getValue()));
                });
        this.saveOrUpdateBatch(configList);

    }

    @Override
    public void fillOverviewInfo(OverviewVo overviewVo) {

        Config configByName = this.findConfigByName(FIRST_START_TIME);
        if (configByName != null) {
            //configByName.getValue() 是一个毫秒值，与当前天数做比较，计算出间隔多少天
            long millis = Long.parseLong(configByName.getValue());
            LocalDate startDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault());
            LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
            long daysBetween = ChronoUnit.DAYS.between(startDate, currentDate);
            overviewVo.setRunDays(daysBetween);
        }

    }
}

