package io.github.cctyl.service;

import cn.hutool.dfa.WordTree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.dto.CheckResult;
import io.github.cctyl.domain.dto.WhiteListRuleAddUpdateDto;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.WhiteListRule;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface WhiteListRuleService extends IService<WhiteListRule> {


    /**
     * 添加白名单忽略关键词
     *
     * @param ignoreKeyWordSet
     */
    void addWhiteIgnoreKeyword(Set<String> ignoreKeyWordSet);

    List<WhiteListRule> findAll();



    WhiteListRule trainWhitelistRule(
            WhiteListRule whitelistRule,
            List<Long> whiteAvidList);

    void removeWhiteTitleKeyword(Set<String> ignoreKeyWordSet);

    void removeWhiteTagKeyword(Set<String> ignoreKeyWordSet);

    void removeWhiteDescKeyword(Set<String> ignoreKeyWordSet);

    void removeWhiteCoverKeyword(Set<String> ignoreKeyWordSet);

    CheckResult whiteMatch(VideoDetail videoDetail,
                           List<WhiteListRule> whitelistRuleList,
                           List<String> whiteUserIdList,
                           List<String> whiteTidList,
                           WordTree whiteTitleKeywordTree,
                           WordTree whiteDescKeywordTree);

    boolean isUserIdMatch(VideoDetail videoDetail, List<String> whiteUserIdList);

    boolean isTidMatch(VideoDetail videoDetail, List<String> whiteTidList);

    boolean isWhitelistRuleMatch(VideoDetail videoDetail, List<WhiteListRule> whitelistRuleList);

    boolean removeWhitelistRules(String id);

    /**
     * 添加或更新白名单
     *
     * @param whitelistRule
     */
    WhiteListRule addOrUpdateWhitelitRule(WhiteListRule whitelistRule);

    List<WhiteListRule> getWhitelistRuleList();

    void addTrain(String id, List<Long> trainedAvidList, String mid);

    List<WhiteListRule> findWithDetail();

    List<Dict> filterIgnore(List<Dict> dictList);

    List<String> filterIgnoreValue(List<String> dictList);

    IPage<WhiteListRuleAddUpdateDto> pageSearch(IPage<WhiteListRule> page);

    WhiteListRule findWithDetailById(String id);

    void thumbUpUserAllVideo(String mid, long page, String keyword);
}
