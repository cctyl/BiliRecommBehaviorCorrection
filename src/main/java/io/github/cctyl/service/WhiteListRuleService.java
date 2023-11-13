package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.WhiteListRule;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface WhiteListRuleService extends IService<WhiteListRule> {

    List<WhiteListRule> findAll();
}
