package io.github.cctyl.service;

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

    List<Dict>  findWhiteUserId();

    List<Dict> findWhiteTid();

    List<Dict> findWhiteIgnoreKeyWord();

    void addBlackCache(List<String> param, DictType dictType);

    List<Dict> findBlackIgnoreKeyWord();

    List<Dict> findBlackCacheKeyWord();

    List<Dict> findBlackCacheTag();

    List<Dict> findByIdIn(Collection<String> idList);

    void updateAccessTypeByIdIn(AccessType blackCache, Collection<String> keywordIdSet);
    void updateAccessTypeAndDictTypeByIdIn(AccessType blackCache,DictType dictType, Collection<String> keywordIdSet);

    void removeByAccessTypeAndDictTypeAndValue(AccessType accessType, DictType dictType, Collection<String> valueCol);

    void removeAndAddDict(AccessType accessType, DictType dictType,  String outerId,Collection<String> valueCol);

    void updateByWhiteListRule(WhiteListRule whitelistRule);

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
}
