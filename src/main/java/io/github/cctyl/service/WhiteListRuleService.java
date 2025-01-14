package io.github.cctyl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.dto.WhiteListRuleAddUpdateDto;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.WhiteListRule;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface WhiteListRuleService extends IService<WhiteListRule> {

    List<WhiteListRule> findAll();

    boolean whiteMatch(VideoDetail videoDetail);

    boolean isUserIdMatch(VideoDetail videoDetail);

    boolean isTidMatch(VideoDetail videoDetail);

    boolean isWhitelistRuleMatch(VideoDetail videoDetail);

    WhiteListRule trainWhitelistRule(
            WhiteListRule whitelistRule,
            List<Long> whiteAvidList);

    void addTrain(String id,List<Long> trainedAvidList,String mid);

    List<WhiteListRule> findWithDetail();

    List<Dict> filterIgnore(List<Dict> dictList);

    List<String> filterIgnoreValue(List<String> dictList);

    IPage<WhiteListRuleAddUpdateDto> pageSearch(IPage<WhiteListRule> page);

    WhiteListRule findWithDetailById(String id);

    void thumbUpUserAllVideo(String mid, long page, String keyword);
}
