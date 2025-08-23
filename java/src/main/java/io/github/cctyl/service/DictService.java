package io.github.cctyl.service;

import cn.hutool.dfa.WordTree;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.domain.vo.OverviewVo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface DictService extends IService<Dict> {

    List<Dict> findSearchKeyWord();

    List<Dict> findBlackUserId();

    List<Dict> addBlackKeyword(Collection<String> param);

    List<Dict> findBlackKeyWord();

    List<Dict> findBlackTid();

    List<Dict> findBlackTag();

    List<Dict> addBlackTag(Collection<String> param);

    List<Dict> findWhiteUserId();

    List<Dict> findWhiteTid();

    List<Dict> findWhiteIgnoreKeyWord();

    void addBlackCache(List<String> param, DictType dictType);

    List<Dict> findBlackIgnoreKeyWord();

    List<Dict> findBlackCacheKeyWord();

    List<Dict> findBlackCacheTag();

    List<Dict> findByIdIn(Collection<String> idList);

    void updateAccessTypeByIdIn(AccessType blackCache, Collection<String> keywordIdSet);

    void updateAccessTypeAndDictTypeByIdIn(AccessType blackCache, DictType dictType, Collection<String> keywordIdSet);

    void addBlackTagFromCache(List<String> tagNameIdList);

    List<String> getBlackTidSet();

    List<String> getBlackTagSet();

    List<String> getWhiteTidSet();

    void addBlackTagSet(Set<String> collect);

    void addBlackTidSet(Set<String> param);

    void addWhiteTidSet(Set<String> whiteTidSet);

    void removeBlackTag(Set<String> param);

    void removeByAccessTypeAndDictTypeAndValue(AccessType accessType, DictType dictType, Collection<String> valueCol);

    WordTree getBlackTagTree();

    List<String> getSearchKeywordSet();

    void addSearchKeyword(Collection<String> searchKeywords);

    List<String> getStopWordList();

    WordTree getBlackKeywordTree();

    void addStopWords(Collection<String> stopWordList);


    void addBlackIgnoreKeyword(Set<String> collect);

    Set<String> getIgnoreWhiteKeyWordSet();

    Set<String> getIgnoreBlackKeyWordSet();

    void removeAndAddDict(AccessType accessType, DictType dictType, String outerId, Collection<String> valueCol);

    void addBlackKeywordAndRemoveIfExist(Collection<String> keywordCol);

    void updateByWhiteListRule(WhiteListRule whitelistRule);

    void removeBlackKeyword(Set<String> param);

    void removeByOuterId(String id);

    void saveStopWords(Collection<String> stopWordList);

    List<String> findStopWords();

    void removeAllWhiteUserId();

    void addWhiteUserId(Collection<String> whiteUserIdSet);

    List<Dict> findByOuterIdIn(Collection<String> idList);


    List<Dict> findBlackIgnoreTag();

    List<Dict> getListByDictTypeAndAccessType(DictType dictType, AccessType accessType);

    List<Dict> findEmptyDescMidDict();

    List<Dict> findEmptyDescTidDict();

    List<Dict> batchRemoveAndUpdate(DictType dictType, AccessType accessType, List<Dict> dictSet);

    void fillOverviewInfo(OverviewVo overviewVo);

    List<String> getBlackUserIdSet();

    void addBlackUserId(String mid);

    void addBlackUserIdSet(Set<String> blackUserIdSet);

    void delBlackUserIdSet(Set<String> blackUserIdSet);

    List<String> getWhiteUserIdSet();

    List<String> getBlackKeywordSet();

    void setWhiteUserIdSet(Collection<String> whiteUserIdSet);

    void addBlackKeyWordFromCache(List<String> keywordIdSet);

    List<String> findValueByDictTypeAndAccessType(DictType dictType, AccessType accessType);

    WordTree getWhiteTitleKeywordTree();

    WordTree getWhiteDescKeywordTree();
}
