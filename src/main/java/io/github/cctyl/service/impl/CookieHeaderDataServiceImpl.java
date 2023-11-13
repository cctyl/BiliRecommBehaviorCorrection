package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.entity.CookieHeaderData;
import io.github.cctyl.mapper.CookieHeaderDataMapper;
import io.github.cctyl.pojo.enumeration.CookieHeaderType;
import io.github.cctyl.service.CookieHeaderDataService;
import org.springframework.stereotype.Service;

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
}
