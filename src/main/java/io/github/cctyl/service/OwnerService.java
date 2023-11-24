package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.Owner;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface OwnerService extends IService<Owner> {

    Owner findOrCreateByMid(Owner owner);
}
