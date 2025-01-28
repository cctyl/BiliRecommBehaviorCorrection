package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.dto.ConfigDTO;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.Config;
import io.github.cctyl.domain.po.PrepareVideo;
import io.github.cctyl.domain.vo.ConfigVo;
import io.github.cctyl.domain.vo.OverviewVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
public interface PrepareVideoService extends IService<PrepareVideo> {


    void saveIfNotExists(String videoId, HandleType handleType);

    List<String> pageFindId(int page, int size, HandleType handleType);

    void removeByVideoId(String id);

    void fillOverviewInfo(OverviewVo overviewVo);
}
