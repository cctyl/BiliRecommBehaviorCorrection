package io.github.cctyl.service.impl;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
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
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 从redis迁移数据
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = ServerException.class)
    public void migrationFromRedis() {
        /*
        12) "bili:api_header"
        13) "bili:common_header"
         4) "bili:common_cookie"
         5) "bili:white_user_ids"
         8) "bili:ignore_white_keyword"
         16) "bili:ignore_black_keyword"
         9) "bili:white_list_rule"
        10) "bili:black_tags"
        11) "bili:mid"
        14) "bili:black_keywords"
        15) "bili:handle_video_detail_list"
        18) "bili:black_user_ids"
        19) "bili:cookies"
        20) "bili:black_tids"
        22) "bili:white_tids"
        23) "bili:ready_handle_video"
        24) "bili:keywords"


     */
        long startTime = System.currentTimeMillis();
//
//        runTask(integer -> {
//            Map<String, String> commonCookieMap = new HashMap<>();
//            for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COMMON_COOKIE_MAP).entrySet()) {
//                commonCookieMap.put((String) entry.getKey(), (String) entry.getValue());
//            }
//            GlobalVariables.INSTANCE.replaceCommonCookieMap(commonCookieMap);
//        });
//
//        runTask(integer -> {
//            Map<String, String> commonHeaderMap = new HashMap<>();
//            for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COMMON_HEADER_MAP).entrySet()) {
//                commonHeaderMap.put((String) entry.getKey(), (String) entry.getValue());
//            }
//            GlobalVariables.INSTANCE.replaceCommonHeaderMap(commonHeaderMap);
//        });
//
//        runTask(integer -> {
//            Map<String, String> cookiesFromRedis = new HashMap<>();
//            for (Map.Entry<Object, Object> entry : redisUtil.hGetAll(COOKIES_KEY).entrySet()) {
//                cookiesFromRedis.put((String) entry.getKey(), (String) entry.getValue());
//            }
//            GlobalVariables.updateRefreshCookie(cookiesFromRedis);
//
//        });
//        runTask(integer -> {
//            List<io.github.cctyl.entity.ApiHeader> apiHeaderList = redisUtil.hGetAll(API_HEADER_MAP).values().stream().map(o -> (io.github.cctyl.entity.ApiHeader) o).collect(Collectors.toList());
//            GlobalVariables.INSTANCE.replaceApiHeaderMap(apiHeaderList.stream().map(v -> {
//                ApiHeader apiHeader = new ApiHeader()
//                        .setUrl(v.getUrl())
//                        .setHeaders(v.getHeaders())
//                        .setCookies(v.getCookies());
//
//                return apiHeader;
//            }).collect(Collectors.toList()));
//        });
//
//
//        runTask(integer -> {
//            Set<String> ignoreWhiteKeyword = redisUtil.sMembers(IGNORE_WHITE_KEYWORD)
//                    .stream()
//                    .map(Object::toString)
//                    .collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addWhiteIgnoreKeyword(ignoreWhiteKeyword);
//
//        });
//        runTask(integer -> {
//            Set<String> ignoreBlackKeyword = redisUtil.sMembers(IGNORE_BLACK_KEYWORD)
//                    .stream()
//                    .map(Object::toString)
//                    .collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addBlackIgnoreKeyword(ignoreBlackKeyword);
//
//        });
//        runTask(integer -> {
//
//            Set<String> whiteUserId = redisUtil.sMembers(WHITE_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.setWhiteUserIdSet(whiteUserId);
//
//        });
//        runTask(integer -> {
//
//            Set<String> blackTagSet = redisUtil.sMembers(BLACK_TAG_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addBlackTagSet(blackTagSet);
//        });
//
//        runTask(integer -> {
//            List<io.github.cctyl.entity.WhitelistRule> whiteListRule = redisUtil.sMembers(WHITE_LIST_RULE_KEY)
//                    .stream().map(
//                            o -> (io.github.cctyl.entity.WhitelistRule) o
//                    )
//                    .map(whitelistRule -> {
//                        whitelistRule.setId(null);
//                        return whitelistRule;
//                    })
//                    .collect(Collectors.toList());
//
//            for (io.github.cctyl.entity.WhitelistRule w : whiteListRule) {
//
//                WhiteListRule whitelistRule = new WhiteListRule()
//                        .setCoverKeyword(Dict.keyword2Dict(Collections.singletonList(w.getCoverKeyword()), DictType.COVER, AccessType.WHITE, null))
//                        .setTagNameList(Dict.keyword2Dict(w.getTagNameList(), DictType.COVER, AccessType.WHITE, null))
//                        .setDescKeyWordList(Dict.keyword2Dict(w.getDescKeyWordList(), DictType.COVER, AccessType.WHITE, null))
//                        .setTitleKeyWordList(Dict.keyword2Dict(w.getTitleKeyWordList(), DictType.COVER, AccessType.WHITE, null));
//                GlobalVariables.INSTANCE.addOrUpdateWhitelitRule(whitelistRule);
//            }
//
//        });
//
//        runTask(integer -> {
//            Set<String> blackKeyWordSet = redisUtil.sMembers(BLACK_KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addBlackKeyword(blackKeyWordSet);
//        });
//
//        runTask(integer -> {
//            Set<io.github.cctyl.entity.VideoDetail> handleVideoDetailSet = redisUtil.sMembers(HANDLE_VIDEO_DETAIL_KEY)
//                    .stream()
//                    .map(o -> (io.github.cctyl.entity.VideoDetail) o)
//                    .collect(Collectors.toSet());
//
//            for (io.github.cctyl.entity.VideoDetail v : handleVideoDetailSet) {
//
//                VideoDetail videoDetail = getVideoDetail(v);
//
//                try {
//                    videoDetailService.saveVideoDetail(videoDetail);
//                } catch (Exception e) {
//
//                    e.printStackTrace();
//                }
//            }
//
//        });
//
//        runTask(integer -> {
//
//            Set<String> blackUserIdSet = redisUtil.sMembers(BLACK_USER_ID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addBlackUserIdSet(blackUserIdSet);
//
//        });
//
//        runTask(integer -> {
//            Set<String> blackTidSet = redisUtil.sMembers(BLACK_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addBlackTidSet(blackTidSet);
//        });
//        runTask(integer -> {
//            Set<String> whiteTidSet = redisUtil.sMembers(WHITE_TID_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//
//            GlobalVariables.INSTANCE.addWhiteTidSet(whiteTidSet);
//        });
//
//        runTask(integer -> {
//            Set<String> searchKeywords = redisUtil.sMembers(KEY_WORD_KEY).stream().map(String::valueOf).collect(Collectors.toSet());
//            GlobalVariables.INSTANCE.addSearchKeyword(searchKeywords);
//        });
//        runTask(integer -> {
//            List<io.github.cctyl.entity.VideoDetail> readyHandleVideoList = redisUtil
//                    .sMembers(READY_HANDLE_VIDEO)
//                    .stream()
//                    .map(io.github.cctyl.entity.VideoDetail.class::cast).collect(Collectors.toList());
//            for (io.github.cctyl.entity.VideoDetail v : readyHandleVideoList) {
//                VideoDetail videoDetail = getVideoDetail(v);
//
//                try {
//                    videoDetailService.saveVideoDetail(videoDetail);
//                } catch (Exception e) {
//
//                    e.printStackTrace();
//                }
//            }
//
//        });
//        GlobalVariables.updateMid((String) redisUtil.get(MID_KEY));
//

        log.debug("转换结束,共花费{}秒", (System.currentTimeMillis() - startTime) / 1000);
    }


    public void runTask(Consumer<Integer> consumer) {
        consumer.accept(-1);
    }


    @Override
    public String getQrCodeUrl() {
        return biliApi.getQrCode();
    }

    @Override
    public Object getQrCodeScanResult() {
        return   biliApi.getQrCodeScanResult();
    }
}

