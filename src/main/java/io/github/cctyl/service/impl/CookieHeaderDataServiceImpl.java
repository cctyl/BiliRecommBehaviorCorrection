package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.domain.po.CookieHeaderData;
import io.github.cctyl.mapper.CookieHeaderDataMapper;
import io.github.cctyl.domain.dto.ApiHeader;
import io.github.cctyl.domain.po.AuditingEntity;
import io.github.cctyl.domain.enumeration.Classify;
import io.github.cctyl.domain.enumeration.MediaType;
import io.github.cctyl.service.CookieHeaderDataService;
import io.github.cctyl.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
public class CookieHeaderDataServiceImpl extends ServiceImpl<CookieHeaderDataMapper, CookieHeaderData> implements CookieHeaderDataService {


    /**
     * 根据分类和用途查询数据
     * @param classify
     * @param mediaType
     * @return
     */
    public List<CookieHeaderData> findByClassifyAndMediaType(Classify classify,
                                                             MediaType mediaType
    ) {

        return this.list(
                new LambdaQueryWrapper<CookieHeaderData>()
                        .eq(CookieHeaderData::getClassify, classify)
                        .eq(CookieHeaderData::getMediaType, mediaType)
        );

    }



    @Override
    public Map<String, ApiHeader> findApiHeaderMap() {

        HashMap<String, ApiHeader> result = new HashMap<>();

        List<CookieHeaderData> cookieHeaderData =
                this.list(
                        new LambdaQueryWrapper<CookieHeaderData>()
                                .isNotNull(CookieHeaderData::getUrl)
                                .in(CookieHeaderData::getClassify, Classify.COOKIE, Classify.REQUEST_HEADER)
                                .eq(CookieHeaderData::getMediaType,MediaType.URL_MATCHING)
                );

        // url 区分
        Map<String, List<CookieHeaderData>> urlDataListMap =
                cookieHeaderData.stream()
                .collect(
                        Collectors.groupingBy(
                                CookieHeaderData::getUrl
                        ));

        for (Map.Entry<String, List<CookieHeaderData>> entry : urlDataListMap.entrySet()) {

            //每个apiHeader内部都有 cookieMap 和 headerMap
            HashMap<String, String> cookieMap = new HashMap<>();
            HashMap<String, String> headerMap = new HashMap<>();
            List<CookieHeaderData> value = entry.getValue();
            for (CookieHeaderData headerData : value) {
                if (headerData.getClassify() == Classify.COOKIE) {
                    cookieMap.put(headerData.getCkey(), headerData.getCvalue());
                } else {
                    headerMap.put(headerData.getCkey(), headerData.getCvalue());
                }
            }


            ApiHeader apiHeader = new ApiHeader();
            apiHeader.setUrl(entry.getKey())
                    .setCookies(cookieMap)
                    .setHeaders(headerMap)
            ;

            result.put(entry.getKey(), apiHeader);
        }

        return result;

    }

    @Override
    public Map<String, String> findCommonCookieMap() {
        return getMapByClassifyAndMediaType(Classify.COOKIE, MediaType.GENERAL);
    }

    /**
     *
     * @param classify
     * @param mediaType
     * @return
     */
    public Map<String, String> getMapByClassifyAndMediaType(
            Classify classify, MediaType mediaType
    ) {
        List<CookieHeaderData> dataList = this.findByClassifyAndMediaType(
                classify, mediaType);

        return dataList.stream().collect(Collectors.toMap(
                CookieHeaderData::getCkey,
                CookieHeaderData::getCvalue,
                (c1, c2) -> c1
        ));
    }


    @Override
    public Map<String, String> findCommonHeaderMap() {
        return getMapByClassifyAndMediaType(Classify.REQUEST_HEADER, MediaType.GENERAL);
    }

    @Override
    public Map<String, String> findRefreshCookie() {
        return getMapByClassifyAndMediaType(Classify.COOKIE, MediaType.TIMELY_UPDATE);
    }


    @Override
    public Map<String, String> findRefreshHeader() {
        return getMapByClassifyAndMediaType(Classify.REQUEST_HEADER, MediaType.TIMELY_UPDATE);
    }


    @Override
    public void updateRefresh(Map<String, String> cookieMap) {
        baseMapper.updateRefresh(cookieMap);
    }


    @Override
    public void removeAllCommonCookie() {
        this.removeByClassifyAndMediaType(Classify.COOKIE,MediaType.GENERAL);
    }


    /**
     * 根据两个类型删除数据
     * @param classify
     * @param mediaType
     */
    public void removeByClassifyAndMediaType(
            Classify classify,
            MediaType mediaType
    ){
        this.remove(
                new LambdaQueryWrapper<CookieHeaderData>()
                        .eq(CookieHeaderData::getClassify,classify)
                        .eq(CookieHeaderData::getMediaType,mediaType)
        );

    }


    @Override
    public void saveCommonCookieMap(Map<String, String> commonCookieMap) {

        List<CookieHeaderData> dataList =
                map2List(commonCookieMap, Classify.COOKIE,MediaType.GENERAL);

        this.saveBatch(dataList);

    }


    @Override
    public void saveCommonHeaderMap(Map<String, String> commonHeaderMap) {

        List<CookieHeaderData> dataList =
                map2List(commonHeaderMap, Classify.REQUEST_HEADER,MediaType.GENERAL);


        this.saveBatch(dataList);

    }

    @Override
    public void saveRefreshCookieMap(Map<String, String> refreshCookieMap) {

        List<CookieHeaderData> dataList =
                map2List(refreshCookieMap, Classify.COOKIE,MediaType.TIMELY_UPDATE);


        this.saveBatch(dataList);

        log.debug("保存{}个数据",dataList.size());
    }

    /**
     * 移除所有API头部信息
     */
    @Override
    public void removeAllApiHeader() {

        this.remove(
            new LambdaQueryWrapper<CookieHeaderData>()
                .in(CookieHeaderData::getClassify,
                    Classify.REQUEST_HEADER, Classify.RESPONSE_HEADER, Classify.COOKIE
                )
                .eq(CookieHeaderData::getMediaType, MediaType.URL_MATCHING)
        );
    }


    @Override
    public void saveApiHeader(List<ApiHeader> apiHeaderList) {
        List<CookieHeaderData> data = new LinkedList<>();
        for (ApiHeader apiHeader : apiHeaderList) {
            List<CookieHeaderData> cookieDataList =
                    map2List(apiHeader.getHeaders(),
                            Classify.COOKIE,
                            MediaType.URL_MATCHING);


            List<CookieHeaderData> headerDataList =
                    map2List(apiHeader.getHeaders(),
                    Classify.REQUEST_HEADER,
                    MediaType.URL_MATCHING);


            data.addAll(cookieDataList);
            data.addAll(headerDataList);
        }

        this.saveBatch(data);

    }

    private List<CookieHeaderData> map2List(Map<String, String> map, Classify classify, MediaType mediaType) {

        return map.entrySet().stream().map(entry -> {
            return new CookieHeaderData()
                   .setCkey(entry.getKey())
                   .setCvalue(entry.getValue())
                   .setClassify(classify)
                   .setMediaType(mediaType);
        }).collect(Collectors.toList());
    }


    @Override
    public void removeAllCommonHeader() {
        this.removeByClassifyAndMediaType(Classify.REQUEST_HEADER,MediaType.GENERAL);
    }
    @Override
    public void removeAllRefreshCookie() {
        this.removeByClassifyAndMediaType(Classify.COOKIE,MediaType.TIMELY_UPDATE);
    }


    @Override
    public void removeByKeyInAndClassifyAndMediaType(Collection<String> keyCol, Classify classify, MediaType mediaType) {

        this.remove(
                new LambdaQueryWrapper<CookieHeaderData>()
                       .in(CookieHeaderData::getCkey,keyCol)
                       .eq(CookieHeaderData::getClassify,classify)
                       .eq(CookieHeaderData::getMediaType,mediaType)
        );

    }

    @Override
    public void removeByUrlAndMediaType(List<String> collect, MediaType mediaType) {
        this.remove(
                new LambdaQueryWrapper<CookieHeaderData>()
                       .in(CookieHeaderData::getUrl,collect)
                       .eq(CookieHeaderData::getMediaType,mediaType)
        );
    }

    /**
     * @param cookieMap
     */
    @Override
    @Transactional(rollbackFor = ServerException.class)
    public void replaceRefreshCookie(Map<String, String> cookieMap) {
        removeAllRefreshCookie();
        saveRefreshCookieMap(cookieMap);
    }
}
