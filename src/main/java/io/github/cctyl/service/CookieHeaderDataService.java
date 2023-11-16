package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.CookieHeaderData;
import io.github.cctyl.pojo.ApiHeader;

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
}
