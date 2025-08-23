package io.github.cctyl.service.impl;

import io.github.cctyl.domain.po.Stat;
import io.github.cctyl.mapper.StatMapper;
import io.github.cctyl.service.StatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class StatServiceImpl extends ServiceImpl<StatMapper, Stat> implements StatService {

}
