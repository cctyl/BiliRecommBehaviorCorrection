package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cctyl.domain.po.VideoDetail;
import org.apache.ibatis.annotations.Param;

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
}
