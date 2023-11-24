package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.CookieHeaderData;
import io.github.cctyl.domain.dto.ApiHeader;
import io.github.cctyl.domain.enumeration.Classify;
import io.github.cctyl.domain.enumeration.MediaType;


import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
public interface CookieHeaderDataService extends IService<CookieHeaderData> {

    Map<String, ApiHeader> findApiHeaderMap();

    Map<String, String> findCommonCookieMap();

    Map<String, String> findCommonHeaderMap();

    Map<String, String> findRefreshCookie();

    Map<String, String> findRefreshHeader();

    void updateRefresh(Map<String, String> cookieMap);

    void removeAllCommonCookie();

    void saveCommonCookieMap(Map<String, String> commonCookieMap);

    void removeAllCommonHeader();
    void saveCommonHeaderMap(Map<String, String> commonHeaderMap);

    void removeAllApiHeader();

    void saveApiHeader(List<ApiHeader> apiHeaderList);

    void removeByKeyInAndClassifyAndMediaType(Collection<String> keyCol,
                                              Classify classify,
                                              MediaType mediaType);

    void removeByUrlAndMediaType(List<String> collect, MediaType mediaType);
}
