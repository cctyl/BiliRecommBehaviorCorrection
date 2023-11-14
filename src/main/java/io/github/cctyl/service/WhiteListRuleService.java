package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.entity.WhiteListRule;

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
            List<Integer> whiteAvidList);
}
