package io.github.cctyl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.cctyl.domain.po.WhiteListRule;
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
public interface WhiteListRuleMapper extends BaseMapper<WhiteListRule> {

    List<WhiteListRule> findWithDetail();


    WhiteListRule findWithDetailById(
            @Param("id") String id
    );
}
