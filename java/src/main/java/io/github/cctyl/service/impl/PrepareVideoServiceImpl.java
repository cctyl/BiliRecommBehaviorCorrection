package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.Owner;
import io.github.cctyl.domain.po.PrepareVideo;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.mapper.OwnerMapper;
import io.github.cctyl.mapper.PrepareVideoMapper;
import io.github.cctyl.service.OwnerService;
import io.github.cctyl.service.PrepareVideoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
@Service
public class PrepareVideoServiceImpl extends ServiceImpl<PrepareVideoMapper, PrepareVideo> implements PrepareVideoService {


    @Override
    public void saveIfNotExists(String videoId, HandleType handleType) {

        boolean exists = this.count(new LambdaQueryWrapper<PrepareVideo>()
                .eq(PrepareVideo::getVideoId, videoId)) > 0;

        if (!exists){
            this.save( new PrepareVideo()
                    .setHandleType(handleType)
                    .setVideoId(videoId));
        }
    }

    /**
     * 分页且更加条件查找
     * @param page
     * @param size
     * @param handleType
     * @return
     */
    @Override
    public List<String> pageFindId(int page, int size, HandleType handleType) {

        LambdaQueryWrapper<PrepareVideo> wrapper = new LambdaQueryWrapper<PrepareVideo>()
                .select(PrepareVideo::getVideoId)
                .eq(PrepareVideo::getHandleType, handleType);
        return this.page(Page.of(page, size), wrapper)
                .getRecords().stream().map(PrepareVideo::getVideoId).collect(Collectors.toList());

    }


    @Override
    public void removeByVideoId(String id) {

        this.remove(
                new LambdaQueryWrapper<PrepareVideo>()
                .eq(PrepareVideo::getVideoId,id)
        );

    }

    @Override
    public void fillOverviewInfo(OverviewVo overviewVo) {
        overviewVo.setThirdHandleCount(this.count());
    }
}
