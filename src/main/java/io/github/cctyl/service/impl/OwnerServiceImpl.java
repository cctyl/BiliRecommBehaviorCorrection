package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    /**
     * @param owner
     * @return
     */
    @Override
    public Owner findOrCreateByMid(Owner owner) {

        if (owner == null || owner.getMid() == null){
            throw new IllegalArgumentException("owner or mid is null");
        }
        Owner find = this.getOne(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getMid, owner.getMid()));


        if (find == null){
            this.save(owner);
            return owner;
        }else {
            return find;
        }

    }
}
