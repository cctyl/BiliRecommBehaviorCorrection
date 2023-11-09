package io.github.cctyl.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.entity.Owner;
import io.github.cctyl.mapper.OwnerMapper;
import io.github.cctyl.service.OwnerService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
@Service
public class OwnerServiceImpl extends ServiceImpl<OwnerMapper, Owner> implements OwnerService {

}
