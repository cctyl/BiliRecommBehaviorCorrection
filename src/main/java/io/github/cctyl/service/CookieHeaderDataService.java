package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.CookieHeaderData;
import io.github.cctyl.domain.dto.ApiHeader;
import io.github.cctyl.domain.enumeration.Classify;
import io.github.cctyl.domain.enumeration.MediaType;
import io.github.cctyl.exception.ServerException;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    void removeAllRefreshCookie();

    void removeAllCommonHeader();

    void saveCommonHeaderMap(Map<String, String> commonHeaderMap);

    void saveRefreshCookieMap(Map<String, String> refreshCookieMap);

    void removeAllApiHeader();

    void saveApiHeader(List<ApiHeader> apiHeaderList);

    void removeByKeyInAndClassifyAndMediaType(Collection<String> keyCol,
                                              Classify classify,
                                              MediaType mediaType);

    void removeByUrlAndMediaType(List<String> collect, MediaType mediaType);

    void replaceRefreshCookie(Map<String, String> cookieMap);

    Map<String, String> getCommonHeaderMap();

    void refreshCommonHeaderMap();

    void updateRefreshCookie(String key, String value);

    void updateCommonHeaderMap(Map<String, String> commonHeaderMap);

    Map<String, String> getRefreshCookieMap();

    void replaceCommonHeaderMap(Map<String, String> commonHeaderMap);

    String getByName(String key);
}
