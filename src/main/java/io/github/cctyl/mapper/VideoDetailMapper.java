package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.vo.VideoVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

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
}
