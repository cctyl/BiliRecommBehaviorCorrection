package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cctyl.entity.CookieHeaderData;
import io.github.cctyl.pojo.enumeration.CookieHeaderType;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
public interface CookieHeaderDataMapper extends BaseMapper<CookieHeaderData> {


    List<CookieHeaderData> findByType(@Param("dataType") CookieHeaderType dataType);

    @MapKey("ckey")
    Map<String, String> findDataByTypeDistinctByKey(@Param("dataType") CookieHeaderType dataType);
}
