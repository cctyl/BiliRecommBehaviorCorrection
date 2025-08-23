package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.po.Dict;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface DictMapper extends BaseMapper<Dict> {

    @Select(" SELECT DATE(datetime(created_date / 1000, 'unixepoch'), 'localtime') AS created_date, COUNT(*) AS count " +
            " FROM dict " +
            " WHERE access_type = #{accessType} " +
            " GROUP BY DATE(datetime(created_date / 1000, 'unixepoch'), 'localtime')")
    List<Map<String, Object>> countByAccessTypeAndGroupByCreatedDate(@Param("accessType") AccessType accessType);

}
