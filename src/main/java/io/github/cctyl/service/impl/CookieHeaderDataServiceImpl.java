package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.Api;
import io.github.cctyl.entity.CookieHeaderData;
import io.github.cctyl.mapper.CookieHeaderDataMapper;
import io.github.cctyl.pojo.ApiHeader;
import io.github.cctyl.pojo.enumeration.CookieHeaderType;
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

    @Override
    public Map<String, String> findCookieMap() {
        return baseMapper.findDataByTypeDistinctByKey(CookieHeaderType.COOKIE);
    }

    @Override
    public Map<String, String> findHeaderMap() {
        return baseMapper.findDataByTypeDistinctByKey(CookieHeaderType.REQUEST_HEADER);
    }

    @Override
    public Map<String, ApiHeader> findApiHeaderMap() {

        HashMap<String, ApiHeader> result = new HashMap<>();

        List<CookieHeaderData> cookieHeaderData =
                this.list(
                        new LambdaQueryWrapper<CookieHeaderData>()
                                .isNotNull(CookieHeaderData::getUrl)
                                .in(CookieHeaderData::getType, CookieHeaderType.COOKIE, CookieHeaderType.REQUEST_HEADER)
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
                if (headerData.getType() == CookieHeaderType.COOKIE) {
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
}
