package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.vo.VideoVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Date;
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
public interface VideoDetailMapper extends BaseMapper<VideoDetail> {

    List<VideoDetail> findWithOwnerAndHandle(@Param("isHandle") boolean isHandle);

    List<VideoDetail> findWithOwnerByIdIn(@Param("idCol")  Collection<String> idCol);
    List<VideoVo> findVoWithOwnerByIdIn(@Param("idCol")  Collection<String> idCol);





    @Select(" SELECT DATE(datetime(last_modified_date / 1000, 'unixepoch'), 'localtime') AS last_modified_date, COUNT(*) AS count" +
            " FROM video_detail" +
            " where handle_type = #{handleType} " +
            " and handle = true " +
            " GROUP BY DATE(datetime(last_modified_date / 1000, 'unixepoch'), 'localtime');")
    List<Map<String, Object>> countByHandTypeAndGroupByLastModifiedDate(@Param( "handleType")HandleType handleType);

    @Select(" SELECT DATE(datetime(last_modified_date / 1000, 'unixepoch'), 'localtime') AS last_modified_date, COUNT(*) AS count" +
            " FROM video_detail" +
            " where handle_type = #{handleType} " +
            " and handle = true " +
            " and last_modified_date between  #{startDate} and #{endDate} " +
            " GROUP BY DATE(datetime(last_modified_date / 1000, 'unixepoch'), 'localtime');")
    List<Map<String, Object>> countByHandTypeAndGroupByLastModifiedDateAndYearBetween(
            @Param( "handleType")HandleType handleType,
            @Param( "startDate")   Date startDate,
            @Param( "endDate")   Date endDate


    );

}
