package io.github.cctyl.service.impl;

import cn.hutool.dfa.WordTree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.exception.ServerException;
import io.github.cctyl.mapper.DictMapper;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.service.ConfigService;
import io.github.cctyl.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.service.WhiteListRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    private final ConfigService configService;


    /**
     * 根据两大类型查询字典表
     *
     * @param dictType
     * @param accessType
     * @return
     */
    public List<Dict> findByDictTypeAndAccessType(

            DictType dictType,
            AccessType accessType
    ) {
        LambdaQueryWrapper<Dict> eq = new LambdaQueryWrapper<Dict>()
                .eq(Dict::getAccessType, accessType)
                .eq(Dict::getDictType, dictType);
        return this.list(eq);
    }

    /**
     * 根据两大类型查询已经在数据库中的字典
     * @param dictType
     * @param accessType
     * @param valueCol
     * @return
     */
    public List<Dict> findByDictTypeAndAccessTypeAndValueIn(
            DictType dictType,
            AccessType accessType,
            Collection<String> valueCol
    ) {
        LambdaQueryWrapper<Dict> eq = new LambdaQueryWrapper<Dict>()
                .eq(Dict::getAccessType, accessType)
                .eq(Dict::getDictType, dictType)
                .in(Dict::getValue, valueCol);
        return this.list(eq);
    }

    /**
     * 获得搜索关键词
     *
     * @return
     */
    @Override
    public List<Dict> findSearchKeyWord() {

        return this.findByDictTypeAndAccessType(
                DictType.SEARCH_KEYWORD, AccessType.OTHER
        );
    }


    @Override
    public List<Dict> findBlackUserId() {
        return this.findByDictTypeAndAccessType(
                DictType.MID, AccessType.BLACK
        );

    }

    @Override
    public List<Dict> addBlackKeyword(Collection<String> param) {
        return this.addDict(param, AccessType.BLACK, DictType.KEYWORD);
    }

    public List<Dict> addDict(Collection<String> param, AccessType accessType, DictType dictType) {
        List<Dict> newDictList = param.stream().map(
                        s -> {
                            return new Dict()
                                    .setAccessType(accessType)
                                    .setDictType(dictType)
                                    .setValue(s);
                        }
                )
                .collect(Collectors.toList());
        this.saveBatch(newDictList);
        return newDictList;
    }


    @Override
    public List<Dict> findBlackKeyWord() {
        return this.findByDictTypeAndAccessType(DictType.KEYWORD, AccessType.BLACK);
    }

    @Override
    public List<Dict> findBlackTid() {
        return this.findByDictTypeAndAccessType(DictType.TID, AccessType.BLACK);
    }

    @Override
    public List<Dict> findBlackTag() {
        return this.findByDictTypeAndAccessType(DictType.TAG, AccessType.BLACK);
    }

    @Override
    public List<Dict> addBlackTag(Collection<String> param) {
        return this.addDict(param, AccessType.BLACK, DictType.TAG);
    }

    @Override
    public List<Dict> findWhiteUserId() {
        return this.findByDictTypeAndAccessType(DictType.MID, AccessType.WHITE);
    }


    @Override
    public List<Dict> findWhiteTid() {
        return this.findByDictTypeAndAccessType(DictType.TID, AccessType.WHITE);
    }

    @Override
    public List<Dict> findWhiteIgnoreKeyWord() {
        return this.findByDictTypeAndAccessType(DictType.IGNORE_KEYWORD, AccessType.WHITE);
    }

    /**
     * 黑名单的缓存
     *
     * @param param
     * @param dictType
     */
    @Override
    public void addBlackCache(List<String> param, DictType dictType) {

        this.addDict(param, AccessType.BLACK_CACHE, dictType);
    }


    @Override
    public List<Dict> findBlackIgnoreKeyWord() {
        return this.findByDictTypeAndAccessType(DictType.IGNORE_KEYWORD, AccessType.BLACK);
    }

    @Override
    public List<Dict> findBlackCacheKeyWord() {

        return this.findByDictTypeAndAccessType(
                DictType.KEYWORD,
                AccessType.BLACK_CACHE
        );
    }

    @Override
    public List<Dict> findBlackCacheTag() {
        return this.findByDictTypeAndAccessType(
                DictType.TAG,
                AccessType.BLACK_CACHE
        );
    }


    @Override
    public List<Dict> findByIdIn(Collection<String> idList) {
        return this.list(new LambdaQueryWrapper<Dict>()
                .in(Dict::getId, idList)
        );
    }


    @Override
    public void updateAccessTypeByIdIn(AccessType accessType, Collection<String> keywordIdSet) {

        this.update(
                new LambdaUpdateWrapper<Dict>()
                        .in(Dict::getId, keywordIdSet)
                        .set(Dict::getAccessType, accessType)
        );

    }

    /**
     * 根据id更新 accsessType 和 dictType
     * @param accessType
     * @param dictType
     * @param keywordIdSet
     */
    @Override
    public void updateAccessTypeAndDictTypeByIdIn(AccessType accessType, DictType dictType, Collection<String> keywordIdSet) {
        this.lambdaUpdate()
                .in(Dict::getId, keywordIdSet)
                .set(Dict::getAccessType, accessType)
                .set(Dict::getDictType, dictType)
                .update();
    }

    public void addBlackTidSet(Set<String> param) {
        this.removeAndAddDict(
                AccessType.BLACK,
                DictType.TID,
                null,
                param);

    }

    public void addWhiteTidSet(Set<String> whiteTidSet) {
        this.removeAndAddDict(
                AccessType.WHITE,
                DictType.TID,
                null,
                whiteTidSet);
    }

    public List<String> getWhiteTidSet() {

        return this
                .findWhiteTid()
                .stream()
                .map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

    }

    public void addBlackTagFromCache(List<String> tagNameIdList) {

        //将这些标签的类型由CACHE 改为正常类型即可
        this.updateAccessTypeByIdIn(
                AccessType.BLACK,
                tagNameIdList
        );

    }

    public void addSearchKeyword(Collection<String> searchKeywords) {

        this.removeAndAddDict(
                AccessType.OTHER,
                DictType.SEARCH_KEYWORD,
                null,
                searchKeywords);


    }

    public List<String> getStopWordList() {
        List<String> stopWordList;
        if (configService.isFirstUse()) {
            //初次使用时，从文件中加载停顿词

            try (
                    InputStream inputStream = this.getClass().getResourceAsStream("/cn_stopwords.txt");
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {

                stopWordList = bufferedReader.lines()
                        .map(String::trim)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //存入数据库
            this.saveStopWords(stopWordList);
        } else {
            //从数据库中加载停顿词
            stopWordList = this.findStopWords();
        }

        return stopWordList;
    }


    public List<String> getSearchKeywordSet() {


        return this.findSearchKeyWord()
                .stream().map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public WordTree getBlackTagTree() {
        WordTree BLACK_TAG_TREE = new WordTree();
        BLACK_TAG_TREE.addWords(getBlackTagSet());
        return BLACK_TAG_TREE;
    }

    public void removeBlackTag(Set<String> param) {

        this.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.TAG,
                param
        );
    }

    public void addBlackTagSet(Set<String> collect) {

        this.removeAndAddDict(
                AccessType.BLACK,
                DictType.TAG,
                null,
                collect);


    }

    public List<String> getBlackTagSet() {


        return this.findBlackTag()
                .stream()
                .map(Dict::getValue)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getBlackTidSet() {
        return this
                .findBlackTid()
                .stream().map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void removeByAccessTypeAndDictTypeAndValue(AccessType accessType, DictType dictType, Collection<String> valueCol) {

        this.remove(new LambdaQueryWrapper<Dict>()
                .eq(Dict::getAccessType, accessType)
                .eq(Dict::getDictType, dictType)
                .in(Dict::getValue, valueCol)
        );

    }

    public void removeByAccessTypeAndDictType(AccessType accessType, DictType dictType) {

        this.remove(new LambdaQueryWrapper<Dict>()
                .eq(Dict::getAccessType, accessType)
                .eq(Dict::getDictType, dictType)
        );

    }

    public void removeBlackKeyword(Set<String> param) {
        this.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.KEYWORD,
                param
        );
    }

    public void addStopWords(Collection<String> stopWordList) {
        //存入数据库
        this.saveStopWords(stopWordList);
    }


    public Set<String> getIgnoreBlackKeyWordSet() {

        return this.findBlackIgnoreKeyWord().stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    public Set<String> getIgnoreWhiteKeyWordSet() {

        return this.findWhiteIgnoreKeyWord().stream().map(Dict::getValue).collect(Collectors.toSet());
    }

    @Transactional
    public void addBlackIgnoreKeyword(Set<String> collect) {
        this.removeAndAddDict(
                AccessType.BLACK,
                DictType.IGNORE_KEYWORD,
                null,
                collect);


        //黑名单关键词需要删除
        removeBlackKeyword(collect);

        //黑名单Tag需要删除
        removeBlackTag(collect);

    }

    public WordTree getBlackKeywordTree() {
        WordTree wordTree = new WordTree();
        wordTree.addWords(this.getBlackKeywordSet());
        return wordTree;
    }


    /**
     * 删除并新增字典
     * @param accessType
     * @param dictType
     * @param valueCol
     * @param outerId
     */
    @Override
    @Transactional
    public void removeAndAddDict(
            AccessType accessType,
            DictType dictType,

            String outerId,

            Collection<String> valueCol) {

        //先删除之前的
        this.removeByAccessTypeAndDictTypeAndValue(
                accessType,
                dictType,
                valueCol
        );

        //新增到数据库
        List<Dict> dictList = Dict.keyword2Dict(
                valueCol,
                dictType,
                accessType,
                outerId
        );
        this.saveBatch(dictList);

    }


    @Override
    @Transactional
    public void updateByWhiteListRule(WhiteListRule whitelistRule) {

        if (whitelistRule.getId() == null) {
            throw new RuntimeException("缺少白名单主键id");
        }

        //1.删除原本的
        this.removeByOuterId(whitelistRule.getId());

        //2.重新保存
        List<Dict> totalDict = whitelistRule.getAggregationDict();

        this.saveBatch(totalDict);
    }

    @Override
    public void removeByOuterId(String outerId) {
        this.remove(new LambdaQueryWrapper<Dict>()
                .eq(Dict::getOuterId, outerId));
    }

    /**
     * 保存停顿词列表
     * @param stopWordList
     */
    @Override
    public void saveStopWords(Collection<String> stopWordList) {

        //1.查询已经在数据库中的数据
        List<String> existsWord = this.findByDictTypeAndAccessTypeAndValueIn(
                DictType.STOP_WORDS,
                AccessType.OTHER,
                stopWordList
        ).stream().map(Dict::getValue).collect(Collectors.toList());

        //2.去重
        stopWordList.removeAll(existsWord);

        //3.保存
        List<Dict> dictList = Dict.keyword2Dict(stopWordList,
                DictType.STOP_WORDS,
                AccessType.OTHER,
                null
        );
        this.saveBatch(dictList);


    }

    /**
     * 获得关键词列表
     * @return
     */
    @Override
    public List<String> findStopWords() {
        return this.findByDictTypeAndAccessType(
                DictType.STOP_WORDS,
                AccessType.OTHER
        ).stream().map(Dict::getValue).collect(Collectors.toList());
    }


    @Override
    public void removeAllWhiteUserId() {
        this.removeByAccessTypeAndDictType(
                AccessType.WHITE, DictType.MID
        );
    }

    @Override
    public void addWhiteUserId(Collection<String> whiteUserIdSet) {
        this.addDict(whiteUserIdSet, AccessType.WHITE, DictType.MID);
    }

    @Override
    public List<Dict> findByOuterIdIn(Collection<String> idList) {

        return this.list(new LambdaQueryWrapper<Dict>().in(Dict::getOuterId, idList));
    }

    @Override
    public List<Dict> findBlackIgnoreTag() {
        return this.lambdaQuery().eq(Dict::getAccessType, AccessType.BLACK)
                .eq(Dict::getDictType, DictType.IGNORE_TAG)
                .list();
    }


    @Override
    public List<Dict> getListByDictTypeAndAccessType(DictType dictType, AccessType accessType) {
        return this.lambdaQuery()
                .eq(Dict::getDictType, dictType)
                .eq(Dict::getAccessType, accessType)
                .list();
    }


    @Override
    public List<Dict> findEmptyDescMidDict() {

        return this.lambdaQuery()
                .eq(Dict::getDictType, DictType.MID)
                .isNull(Dict::getDesc)
                .list();
    }

    @Override
    public List<Dict> findEmptyDescTidDict() {
        return this.lambdaQuery()
                .eq(Dict::getDictType, DictType.TID)
                .isNull(Dict::getDesc)
                .list();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Dict> batchRemoveAndUpdate(DictType dictType, AccessType accessType, List<Dict> dictSet) {

        this.removeByAccessTypeAndDictType(accessType, dictType);
        dictSet.forEach(dict -> dict.setAccessType(accessType).setDictType(dictType).setId(null));
        this.saveBatch(dictSet);

        return dictSet;
    }


    @Override
    public void fillOverviewInfo(OverviewVo overviewVo) {

        Long blackCount = this.lambdaQuery()
                .eq(Dict::getAccessType, AccessType.BLACK)
                .count();

        Long whiteCount = this.lambdaQuery()
                .eq(Dict::getAccessType, AccessType.WHITE)
                .count();

        Long searchCount = this.lambdaQuery()
                .eq(Dict::getAccessType, AccessType.OTHER)
                .eq(Dict::getDictType, DictType.SEARCH_KEYWORD)
                .count();
        Long blackCacheCount = this.lambdaQuery()
                .eq(Dict::getAccessType, AccessType.BLACK_CACHE)
                .count();

        //统计黑名单dict数量
        overviewVo.setBlackRuleCount(blackCount)
                .setWhiteRuleCount(whiteCount)
                .setSearchKeywordCount(searchCount)
                .setBlackCacheCount(blackCacheCount)
        ;


    }


    /**
     * 添加一个黑名单用户id
     *
     * @param mid
     */
    public void addBlackUserId(String mid) {
        Collection<String> blackUserIdSet = getBlackUserIdSet();
        if (blackUserIdSet.contains(mid)) {
            return;
        }
        Dict dict = new Dict()
                .setDictType(DictType.MID)
                .setAccessType(AccessType.BLACK)
                .setValue(mid);
        this.save(dict);
    }

    public void addBlackUserIdSet(Set<String> blackUserIdSet) {

        this.removeAndAddDict(
                AccessType.BLACK,
                DictType.MID,
                null,
                blackUserIdSet);


    }

    public List<String> getBlackKeywordSet() {
        return this.findBlackKeyWord()
                .stream()
                .map(Dict::getValue)
                .distinct()
                .collect(Collectors.toList())
                ;
    }

    public void addBlackKeyWordFromCache(List<String> keywordIdSet) {

        //过滤掉忽略的关键词(无需，添加关键词时，如果匹配忽略关键词则不允许添加)

        //将这些标签的类型由CACHE 改为正常类型即可
        this.updateAccessTypeByIdIn(
                AccessType.BLACK,
                keywordIdSet
        );

        List<String> valueList = Dict.transferToValue(this.findByIdIn(keywordIdSet));

    }

    public void addBlackKeywordAndRemoveIfExist(Collection<String> keywordCol) {

        this.removeAndAddDict(
                AccessType.BLACK,
                DictType.KEYWORD,
                null,
                keywordCol);
    }

    @Transactional(rollbackFor = ServerException.class)
    public void setWhiteUserIdSet(Collection<String> whiteUserIdSet) {
        //删除以前的
        this.removeAllWhiteUserId();

        //添加全部
        this.addWhiteUserId(whiteUserIdSet);
    }


    public List<String> getWhiteUserIdSet() {
        return this
                .findWhiteUserId()
                .stream()
                .map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public void delBlackUserIdSet(Set<String> blackUserIdSet) {
        this.removeByAccessTypeAndDictTypeAndValue(
                AccessType.BLACK,
                DictType.MID,
                blackUserIdSet);
    }

    @Override
    public Collection<String> getBlackUserIdSet() {
        return this
                .findBlackUserId()
                .stream()
                .map(Dict::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
