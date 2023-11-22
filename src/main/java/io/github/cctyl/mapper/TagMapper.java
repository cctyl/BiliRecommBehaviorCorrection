package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.cctyl.domain.po.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author tyl
 * @since 2023-11-17
 */
public interface TagMapper extends BaseMapper<Tag> {

    List<Tag> findByVideoId(@Param("id") String id);
}
