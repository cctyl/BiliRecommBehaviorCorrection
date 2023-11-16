package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.entity.CookieHeaderData;
import io.github.cctyl.mapper.CookieHeaderDataMapper;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.pojo.AuditingEntity;
import io.github.cctyl.pojo.enumeration.Classify;
import io.github.cctyl.pojo.enumeration.MediaType;
import io.github.cctyl.service.CookieHeaderDataService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                .orderBy(true,false, AuditingEntity::getLastModifiedDate)
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
}
