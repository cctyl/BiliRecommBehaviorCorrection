package io.github.cctyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.domain.dto.CheckResult;
import io.github.cctyl.domain.dto.WhiteListRuleAddUpdateDto;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.po.WhiteListRule;
import io.github.cctyl.exception.ServerException;
import io.github.cctyl.mapper.WhiteListRuleMapper;
import io.github.cctyl.domain.dto.DescV2;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.domain.dto.UserSubmissionVideo;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.service.DictService;
import io.github.cctyl.service.WhiteListRuleService;
import io.github.cctyl.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.cctyl.domain.constants.AppConstant.REASON_FORMAT;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
@Service
@Slf4j
public class WhiteListRuleServiceImpl extends ServiceImpl<WhiteListRuleMapper, WhiteListRule> implements WhiteListRuleService {

    @Override
    public List<WhiteListRule> findAll() {
        return this.list();
    }


    @Autowired
    private BiliApi biliApi;

    @Autowired
    private DictService dictService;

    /**
     * 白名单判断
     *
     * @param videoDetail
     * @return
     */
    @Override
    public CheckResult whiteMatch(VideoDetail videoDetail,
                              List<WhiteListRule> whitelistRuleList,
                              List<String> whiteUserIdList,
                              List<String> whiteTidList,
                              WordTree whiteTitleKeywordTree,
                              WordTree whiteDescKeywordTree) {

        /**
         * 假设，白名单使用一个专门的条件构造器，一个对象。里面包含 关键词 分区 up主id 等多个条件
         * 白名单匹配时，需要在单个对象上，找到两个匹配的条件，则表示该条件匹配
         *
         * 那么此时与黑名单产生了割裂，黑名单是任意一个匹配
         *
         * 而关键词列表，不再作为白名单的判断条件
         *
         *
         * 或者说，白名单的关键词 要 配合分区 或 up主id ，达到两个条件以上
         *
         * 错误案例：
         *      刘三金
         *      本来是搜索猫猫的视频，但是出现了一些标题带有刘三金的视频
         *      也进行了点赞，这样非常的不符合。
         *      起码，这个up主在范围内（直接用up主id不就行了），分区在范围内，封面在范围内
         *      所以关键词部分，至少满足： 标题 描述 关键词匹配，分区匹配，封面包含指定关键词 三个条件中两个条件满足
         */

        try {
            //白名单规则匹配
            boolean listRuleMatch = isWhitelistRuleMatch(videoDetail,whitelistRuleList);

            //up主id匹配
            boolean midMatch = isUserIdMatch(videoDetail,whiteUserIdList);

            //分区id匹配
            boolean tidMatch = isTidMatch(videoDetail,whiteTidList);

            //标题关键词匹配
            boolean titleMatch = isTitleMatch(videoDetail, whiteTitleKeywordTree);

            //描述关键词匹配
            boolean descMatch = isDescMatch(videoDetail, whiteDescKeywordTree);

            //标签匹配
            boolean tagMatch = false;

            //封面匹配
            boolean coverMatch = false;

            boolean total = titleMatch || descMatch || tagMatch || midMatch || tidMatch || coverMatch || listRuleMatch;
            return new CheckResult(
                    total,
                    titleMatch,
                    descMatch,
                    tagMatch,
                    midMatch,
                    tidMatch,
                    coverMatch,
                    listRuleMatch
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return  CheckResult.error();
        }
    }

    private boolean isDescMatch(VideoDetail videoDetail, WordTree whiteKeywordTree) {

        String matchWord = whiteKeywordTree.match(videoDetail.getDesc());
        boolean match = matchWord != null;
        String desc = videoDetail.getDesc() == null ? "" : videoDetail.getDesc();
        if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
            match = match || videoDetail.getDescV2()
                    .stream()
                    .map(DescV2::getRawText)
                    .anyMatch(whiteKeywordTree ::isMatch);
            desc = desc + "," + videoDetail.getDescV2().stream().map(DescV2::getRawText).collect(Collectors.joining(","));
        }
        log.debug("视频:{}-{}的 简介：{}，匹配结果：{},匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                desc,
                match,
                matchWord
        );
        if (match) {
            videoDetail.setThumbUpReason(
                    Opt.ofNullable(videoDetail.getThumbUpReason()).orElse("") +
                    String.format(REASON_FORMAT,
                            "描述",
                            videoDetail.getTitle(),
                            matchWord
                    )
            );
        }
        return match;
    }

    public boolean isTitleMatch(VideoDetail videoDetail, WordTree whiteKeywordTree) {
        String matchWord = whiteKeywordTree.match(videoDetail.getTitle());
        boolean match = matchWord != null;
        log.debug("视频:{}-{}的标题：{}，匹配结果：{} ,匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTitle(),
                match,
                matchWord
        );
        if (match) {
            videoDetail.setThumbUpReason(
                    Opt.ofNullable(videoDetail.getThumbUpReason()).orElse("") +
                    String.format(REASON_FORMAT,
                            "标题",
                            videoDetail.getTitle(),
                            matchWord
                    )
            );
        }
        return match;
    }

    /**
     * 用户id是否匹配白名单
     *
     * @param videoDetail
     * @return
     */
    @Override
    public boolean isUserIdMatch(VideoDetail videoDetail, List<String> whiteUserIdList) {
        if (videoDetail.getOwner() == null || videoDetail.getOwner().getMid() == null) {
            log.error("视频:{}缺少up主信息", videoDetail);
            return false;
        }
        boolean match = whiteUserIdList
                .contains(videoDetail.getOwner().getMid());

        log.debug("视频:{}-{}的 up主：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getOwner().getMid(),
                videoDetail.getOwner().getName(),
                match);
        if (match) {
            videoDetail.setThumbUpReason(
                    Opt.ofNullable(videoDetail.getThumbUpReason()).orElse("") +
                            String.format(REASON_FORMAT,
                                    "up主",
                                    videoDetail.getOwner().getName(),
                                    "成功"
                            )

            );


        }

        return match;
    }

    /**
     * tid是否匹配白名单
     *
     * @param videoDetail
     * @return
     */
    @Override
    public boolean isTidMatch(VideoDetail videoDetail, List<String> whiteTidList) {


        boolean match = whiteTidList
                .contains(String.valueOf(videoDetail.getTid()));

        log.debug("视频:{}-{}的 分区：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTid(),
                videoDetail.getTname(),
                match);

        if (match) {
            videoDetail.setThumbUpReason(
                    Opt.ofNullable(videoDetail.getThumbUpReason()).orElse("") +
                    String.format(REASON_FORMAT,
                            "分区id",
                            videoDetail.getTid(),
                            "成功"
                    )
            );
        }
        return match;
    }

    /**
     * 在白名单列表中是否找到匹配的
     *
     * @param videoDetail
     * @return
     */
    @Override
    public boolean isWhitelistRuleMatch(VideoDetail videoDetail, List<WhiteListRule> whitelistRuleList) {
        String[] matchWordArr = new String[8];
        WhiteListRule whitelistRule = whitelistRuleList
                .stream()
                .filter(item ->
                        {
                            AtomicBoolean titleMatch = new AtomicBoolean(false);
                            AtomicBoolean descMatch = new AtomicBoolean(false);
                            AtomicBoolean tagMatch = new AtomicBoolean(false);
                            try {

                                //重新初始化
                                Arrays.fill(matchWordArr, null);

                                //标题
                                item.getTitleKeyWordList().stream().filter(keyword -> {
                                            return videoDetail.getTitle().contains(keyword.getValue());
                                        })
                                        .findFirst()
                                        .ifPresent(s -> {
                                            titleMatch.set(true);
                                            matchWordArr[0] = s.getValue();
                                            matchWordArr[1] = videoDetail.getTitle();
                                        });

                                log.debug("标题{} 匹配结果{}, 关键词：{}",
                                        videoDetail.getTitle(),
                                        titleMatch,
                                        matchWordArr[0]
                                );


                                //desc
                                item.getDescKeyWordList().stream()
                                        .filter(s -> videoDetail.getDesc().contains(s.getValue()))
                                        .findFirst()
                                        .ifPresent(s -> {
                                            descMatch.set(true);
                                            matchWordArr[2] = s.getValue();
                                            matchWordArr[3] = videoDetail.getDesc();
                                        });

                                //descV2
                                if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
                                    List<String> descV2TextList = videoDetail.getDescV2()
                                            .stream()
                                            .map(DescV2::getRawText)
                                            .collect(Collectors.toList());

                                    for (Dict keyword : item.getDescKeyWordList()) {
                                        String descV2Found = descV2TextList.stream()
                                                .filter(descV2Text -> descV2Text.contains(keyword.getValue()))
                                                .findFirst().orElse(null);

                                        if (descV2Found != null) {
                                            descMatch.set(true);
                                            matchWordArr[4] = keyword.getValue();
                                            matchWordArr[5] = descV2Found;
                                            break;
                                        }
                                    }

                                }

                                log.debug("desc {},{} 匹配结果{}, 关键词:{}-{}",
                                        videoDetail.getDesc(),
                                        videoDetail.getDescV2(),
                                        descMatch,
                                        matchWordArr[4],
                                        matchWordArr[5]
                                );


                                //tag
                                if (CollUtil.isNotEmpty(videoDetail.getTags())) {
                                    List<String> tagNameList = videoDetail.getTags()
                                            .stream()
                                            .map(Tag::getTagName)
                                            .collect(Collectors.toList());

                                    for (Dict keyword : item.getTagNameList()) {

                                        try {
                                            String tagNameFound = tagNameList.stream()
                                                    .filter(s -> keyword.getValue() != null && keyword.getValue().contains(s))
                                                    .findFirst().orElse(null);

                                            if (tagNameFound != null) {
                                                tagMatch.set(true);
                                                matchWordArr[6] = keyword.getValue();
                                                matchWordArr[7] = tagNameFound;
                                                break;
                                            }
                                        } catch (Exception e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    }


                                }

                                log.debug("tagName:{} 匹配结果{},具体匹配：{}，关键词{}",
                                        videoDetail.getTags()
                                                .stream()
                                                .map(Tag::getTagName)
                                                .collect(Collectors.toList()),
                                        tagMatch,
                                        matchWordArr[7],
                                        matchWordArr[6]
                                );
                            } catch (Exception e) {
                                log.error("出现异常:{},视频信息：{}", e.getMessage(), videoDetail.toString());
                                log.error(e.getMessage(), e);
                            }
                            //两个以上的判断都通过，才表示通过
                            return Stream.of(titleMatch, descMatch, tagMatch)
                                    .filter(atomicBoolean -> Boolean.TRUE.equals(atomicBoolean.get()))
                                    .count() > 1;
                        }

                )
                .findFirst()
                .orElse(null);

        boolean match = whitelistRule != null;
        String matchDetail = "";
        if (match) {
            matchDetail =
                    "  联合匹配 关键词：" + matchWordArr[0] + " 标题：" + matchWordArr[1] + "<br/>" +
                            "  关键词：" + matchWordArr[2] + " desc：" + matchWordArr[3] + "<br/>" +
                            "  关键词：" + matchWordArr[4] + " descV2：" + matchWordArr[5] + "<br/>" +
                            "  关键词：" + matchWordArr[6] + " tagName：" + matchWordArr[7] + "<br/>"
            ;

            videoDetail.setThumbUpReason(

                    Opt.ofNullable(videoDetail.getThumbUpReason()).orElse("") +

                            String.format(REASON_FORMAT,
                                    "规则名",
                                    whitelistRule.getInfo(),
                                    "成功"
                            )

            );
        }

        log.debug("视频:{}-{}，匹配白名单：{}，匹配结果：{} , 具体如下：\n\t{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                whitelistRule,
                match,
                matchDetail
        );

        return match;
    }

    @Transactional(rollbackFor = ServerException.class)
    public boolean removeWhitelistRules(String id) {

        boolean result = this.removeById(id);

        //删除关联的数据
        dictService.removeByOuterId(id);

        return result;
    }

    public void removeWhiteCoverKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.COVER,
                ignoreKeyWordSet
        );

    }

    public void removeWhiteTitleKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.TITLE,
                ignoreKeyWordSet
        );

    }

    public void removeWhiteDescKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.DESC,
                ignoreKeyWordSet
        );


    }

    public void removeWhiteTagKeyword(Set<String> ignoreKeyWordSet) {

        //数据库层面的删除
        dictService.removeByAccessTypeAndDictTypeAndValue(
                AccessType.WHITE,
                DictType.TAG,
                ignoreKeyWordSet
        );


    }

    /**
     * 白名单关键词自动修正补全
     * 传入一个指定的白名单规则对象，
     * 传入你认为应当符合该规则的视频id
     *
     * @param whitelistRule 需要训练的白名单规则
     * @param whiteAvidList 应当符号白名单规则的视频id集合
     */
    @Override
    public WhiteListRule trainWhitelistRule(
            WhiteListRule whitelistRule,
            List<Long> whiteAvidList) {

        log.info("开始对:{} 规则进行训练,训练数据：{}", whitelistRule.getId(), whiteAvidList);
        List<String> titleProcess = new ArrayList<>();
        List<String> descProcess = new ArrayList<>();
        List<String> tagNameProcess = new ArrayList<>();
        for (Long avid : whiteAvidList) {
            try {
                VideoDetail videoDetail = biliApi.getVideoDetail(avid);
                //1. 标题处理
                String title = videoDetail.getTitle();
                titleProcess.addAll(SegmenterUtil.process(title));

                //2.描述
                String desc = videoDetail.getDesc();
                if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
                    List<String> descV2Process = videoDetail.getDescV2().stream().map(descV2 -> SegmenterUtil.process(descV2.getRawText()))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    descProcess.addAll(descV2Process);
                }
                descProcess.addAll(SegmenterUtil.process(desc));

                //3.标签
                List<String> tagNameList = videoDetail.getTags().stream().map(Tag::getTagName).collect(Collectors.toList());
                tagNameProcess.addAll(tagNameList);
                log.info("获得视频信息:{}", videoDetail);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            ThreadUtil.sleep(10);
        }
        List<String> stopWordList = dictService.getStopWordList();
        //统计频次
        Map<String, Integer> descKeywordFrequencyMap = SegmenterUtil.generateFrequencyMap(descProcess,stopWordList);
        Map<String, Integer> tagNameFrequencyMap = SegmenterUtil.generateFrequencyMap(tagNameProcess,stopWordList);
        Map<String, Integer> titleKeywordFrequencyMap = SegmenterUtil.generateFrequencyMap(titleProcess,stopWordList);
        List<String> topDescKeyWord = SegmenterUtil.getTopFrequentWord(descKeywordFrequencyMap);
        List<String> topTagName = SegmenterUtil.getTopFrequentWord(tagNameFrequencyMap);
        List<String> topTitleKeyWord = SegmenterUtil.getTopFrequentWord(titleKeywordFrequencyMap);

        log.info("本次训练结束 \r\n\t前5的标题关键词是:{} \r\n\t 前5的标签名是:{} \r\n\t 前5的描述关键词是:{}",
                topTitleKeyWord,
                topTagName,
                topDescKeyWord
        );
        Set<String> ignoreKeyWordSet = dictService.getIgnoreWhiteKeyWordSet();
        topTagName.removeAll(ignoreKeyWordSet);
        topTitleKeyWord.removeAll(ignoreKeyWordSet);
        topDescKeyWord.removeAll(ignoreKeyWordSet);


        if (topTagName.isEmpty() && topTitleKeyWord.isEmpty() && topDescKeyWord.isEmpty()) {
            log.info("本次训练没有得到任何结果，将删除该规则");
            return null;
        }

        WhiteListRule finalWhitelistRule = whitelistRule;
        whitelistRule.getTagNameList().addAll(
                Dict.keyword2Dict(topTagName, DictType.TAG, AccessType.WHITE, finalWhitelistRule.getId())
        );
        whitelistRule.getTitleKeyWordList().addAll(
                Dict.keyword2Dict(topTitleKeyWord, DictType.TITLE, AccessType.WHITE, finalWhitelistRule.getId())
        );
        whitelistRule.getDescKeyWordList().addAll(
                Dict.keyword2Dict(topDescKeyWord, DictType.DESC, AccessType.WHITE, finalWhitelistRule.getId())
        );

        return whitelistRule;
    }


    public List<WhiteListRule> getWhitelistRuleList() {
        return this.findWithDetail();

    }

    /**
     * 添加或更新白名单
     *
     * @param whitelistRule
     */
    @Transactional(rollbackFor = ServerException.class)
    public WhiteListRule addOrUpdateWhitelitRule(WhiteListRule whitelistRule) {

        //修改主对象
        this.saveOrUpdate(whitelistRule);
        //修改关联的数据
        dictService.updateByWhiteListRule(whitelistRule);

        return whitelistRule;
    }

    @Override
    public void addTrain(String id, List<Long> trainedAvidList, String mid) {
        log.info("开始训练");
        List<WhiteListRule> whitelistRuleList = this.getWhitelistRuleList();
        WhiteListRule whitelistRule;
        if (StrUtil.isBlank(id)) {
            //创建新规则
            whitelistRule = new WhiteListRule()
                    .setInfo(String.valueOf(IdGenerator.nextId()))
            ;
        } else {
            //从redis中找
            whitelistRule =
                    whitelistRuleList.stream()
                            .filter(w -> id.equals(w.getId()))
                            .findFirst()
                            .orElse(new WhiteListRule());
        }

        if (CollUtil.isNotEmpty(trainedAvidList)) {
            log.debug("根据视频id进行训练");
            //从给定的视频列表进行训练
            whitelistRule = this.trainWhitelistRule(
                    whitelistRule,
                    trainedAvidList
            );

        } else if (StrUtil.isNotBlank(mid)) {
            //从给定的up主的投稿视频进行训练
            log.debug("根据up主id进行训练");
            List<UserSubmissionVideo> allVideo = biliApi.searchUserAllSubmissionVideo(mid, 1, "");
            whitelistRule = this.trainWhitelistRule(
                    whitelistRule,
                    allVideo.stream().map(UserSubmissionVideo::getAid).collect(Collectors.toList())
            );
        }
        log.info("训练完成，训练结果为:" + whitelistRule);

        if (whitelistRule != null) {
            //更新白名单
            this.addOrUpdateWhitelitRule(whitelistRule);
        }


    }

    @Override
    public List<WhiteListRule> findWithDetail() {

        List<WhiteListRule> list = baseMapper.findWithDetail();

        for (WhiteListRule item : list) {
            groupDict(item, item.getTotalDict());
        }

        return list;
    }


    @Override
    public List<Dict> filterIgnore(List<Dict> dictList) {

        Set<String> ignoreSet = dictService.getIgnoreWhiteKeyWordSet();
        return dictList.stream().filter(
                dict -> !ignoreSet.contains(dict.getValue())
        ).collect(Collectors.toList());
    }

    @Override
    public List<String> filterIgnoreValue(List<String> dictList) {
        Set<String> ignoreSet = dictService.getIgnoreWhiteKeyWordSet();
        return dictList.stream().filter(
                dict -> !ignoreSet.contains(dict)
        ).collect(Collectors.toList());
    }

    /**
     * 添加白名单忽略关键词
     *
     * @param ignoreKeyWordSet
     */
    @Transactional
    public void addWhiteIgnoreKeyword(Set<String> ignoreKeyWordSet) {

        if (ignoreKeyWordSet == null) {
            return;
        }
        dictService.removeAndAddDict(
                AccessType.WHITE,
                DictType.IGNORE_KEYWORD,
                null,
                ignoreKeyWordSet
        );


        //删除白名单对应的关键词,不区分具体是哪个白名单对象的
        //并且需要同时删除缓存内对应的数据
        removeWhiteTagKeyword(ignoreKeyWordSet);
        removeWhiteDescKeyword(ignoreKeyWordSet);
        removeWhiteTitleKeyword(ignoreKeyWordSet);
        removeWhiteCoverKeyword(ignoreKeyWordSet);


    }


    @Override
    public IPage<WhiteListRuleAddUpdateDto> pageSearch(IPage<WhiteListRule> page) {

        List<WhiteListRule> records = this.page(page).getRecords();
        List<String> idList = records.stream().map(WhiteListRule::getId).collect(Collectors.toList());
        List<Dict> dictList = dictService.findByOuterIdIn(idList);
        Map<String, List<Dict>> outerIdDictListMap = dictList.stream().collect(Collectors.groupingBy(Dict::getOuterId));


        List<WhiteListRuleAddUpdateDto> collect = records.stream().map(item -> {


            WhiteListRuleAddUpdateDto whiteListRuleAddUpdateDto = BeanUtil.copyProperties(item, WhiteListRuleAddUpdateDto.class);
            whiteListRuleAddUpdateDto.setId(item.getId());
            groupDict(
                    whiteListRuleAddUpdateDto,
                    outerIdDictListMap.getOrDefault(item.getId(), Collections.emptyList())
            );
            return whiteListRuleAddUpdateDto;
        }).toList();

        return new Page<WhiteListRuleAddUpdateDto>(page.getCurrent(), page.getSize(), page.getTotal())
                .setRecords(collect);
    }

    public void groupDict(WhiteListRule whiteListRule, Collection<Dict> dictCollection) {

        Map<DictType, List<Dict>> dictTypeListMap = dictCollection
                .stream()
                .collect(Collectors.groupingBy(Dict::getDictType));
        whiteListRule
                .setTagNameList(dictTypeListMap.get(DictType.TAG))
                .setDescKeyWordList(dictTypeListMap.get(DictType.DESC))
                .setTitleKeyWordList(dictTypeListMap.get(DictType.TITLE))
                .setCoverKeyword(dictTypeListMap.get(DictType.COVER))
        ;
    }

    public void groupDict(WhiteListRuleAddUpdateDto whiteListRule, Collection<Dict> dictCollection) {
        Map<DictType, List<Dict>> dictTypeListMap =
                dictCollection
                        .stream()
                        .collect(Collectors.groupingBy(Dict::getDictType));
        whiteListRule
                .setTagNameList(Optional.ofNullable(dictTypeListMap.get(DictType.TAG)).orElse(Collections.emptyList()).stream().map(Dict::getValue).toList())
                .setCoverKeyword(Optional.ofNullable(dictTypeListMap.get(DictType.COVER)).orElse(Collections.emptyList()).stream().map(Dict::getValue).toList())
                .setTitleKeyWordList(Optional.ofNullable(dictTypeListMap.get(DictType.TITLE)).orElse(Collections.emptyList()).stream().map(Dict::getValue).toList())
                .setDescKeyWordList(Optional.ofNullable(dictTypeListMap.get(DictType.DESC)).orElse(Collections.emptyList()).stream().map(Dict::getValue).toList());
    }

    @Override
    public WhiteListRule findWithDetailById(String id) {
        WhiteListRule whiteListRule = baseMapper.findWithDetailById(id);

        if (whiteListRule != null) {

            groupDict(whiteListRule, whiteListRule.getTotalDict());
            return whiteListRule;
        } else {
            return null;
        }

    }

    @Override
    public void thumbUpUserAllVideo(String mid, long page, String keyword) {

        List<UserSubmissionVideo> userSubmissionVideos = biliApi.searchUserAllSubmissionVideo(mid, page, keyword);

        if (CollUtil.isEmpty(userSubmissionVideos)) {
            log.info("该用户{}投稿视频为空", mid);
            return;
        }

        DataUtil.randomAccessList(
                userSubmissionVideos,
                userSubmissionVideos.size(),
                userSubmissionVideo -> {
                    JSONObject jsonObject = biliApi.thumpUp(userSubmissionVideo.getAid());
                    log.debug("点赞：{} 结果为：{}", userSubmissionVideo.getTitle(), jsonObject.getString("message"));
                    ThreadUtil.sleep(DataUtil.getRandom(10, 23));
                }
        );

        log.info("共点赞{}用户{}条视频", mid, userSubmissionVideos.size());
    }
}
