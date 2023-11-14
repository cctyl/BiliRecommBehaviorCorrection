package io.github.cctyl.service;

import cn.hutool.core.collection.CollUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.Dict;
import io.github.cctyl.pojo.DescV2;
import io.github.cctyl.pojo.Tag;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.entity.WhiteListRule;
import io.github.cctyl.pojo.enumeration.AccessType;
import io.github.cctyl.pojo.enumeration.DictType;
import io.github.cctyl.utils.IdGenerator;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.SegmenterUtil;
import io.github.cctyl.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.cctyl.pojo.constants.AppConstant.IGNORE_WHITE_KEYWORD;

/**
 * 白名单相关规则
 */
@Service
@Slf4j
public class WhiteRuleService {

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
    public boolean whiteMatch(VideoDetail videoDetail) {

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
            boolean whitelistRuleMatch = isWhitelistRuleMatch(videoDetail);

            //up主id匹配
            boolean userIdMatch = isUserIdMatch(videoDetail);

            //分区id匹配
            boolean tidMatch = isTidMatch(videoDetail);
            return
                    whitelistRuleMatch
                            ||
                            userIdMatch
                            ||
                            tidMatch
                    ;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用户id是否匹配白名单
     *
     * @param videoDetail
     * @return
     */
    public boolean isUserIdMatch(VideoDetail videoDetail) {
        if (videoDetail.getOwner() == null || videoDetail.getOwner().getMid() == null) {
            log.error("视频:{}缺少up主信息", videoDetail.toString());
            return false;
        }
        boolean match = GlobalVariables.getWhiteUserIdSet()
                .contains(videoDetail.getOwner().getMid());

        log.debug("视频:{}-{}的 up主：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getOwner().getMid(),
                videoDetail.getOwner().getName(),
                match);
        if (match) {
            videoDetail.setThumbUpReason("up主:" + videoDetail.getOwner().getName() +
                    " id:" + videoDetail.getOwner().getMid() + " 匹配成功");
        }

        return match;
    }

    /**
     * tid是否匹配白名单
     *
     * @param videoDetail
     * @return
     */
    public boolean isTidMatch(VideoDetail videoDetail) {


        boolean match = GlobalVariables.getWhiteTidSet()
                .contains(String.valueOf(videoDetail.getTid()))
                ;

        log.debug("视频:{}-{}的 分区：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTid(),
                videoDetail.getTname(),
                match);

        if (match) {
            videoDetail.setThumbUpReason("分区id:" + videoDetail.getTid() + "匹配成功");
        }
        return match;
    }

    /**
     * 在白名单列表中是否找到匹配的
     *
     * @param videoDetail
     * @return
     */
    public boolean isWhitelistRuleMatch(VideoDetail videoDetail) {
        String[] matchWordArr = new String[8];
        WhiteListRule whitelistRule = GlobalVariables.getWhitelistRules()
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

                                log.info("标题{} 匹配结果{}, 关键词：{}",
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

                                log.info("desc {},{} 匹配结果{}, 关键词:{}-{}",
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

                                        String tagNameFound = tagNameList.stream()
                                                .filter(s -> keyword.getValue().contains(s))
                                                .findFirst().orElse(null);

                                        if (tagNameFound != null) {
                                            tagMatch.set(true);
                                            matchWordArr[6] = keyword.getValue();
                                            matchWordArr[7] = tagNameFound;
                                            break;
                                        }
                                    }


                                }

                                log.info("tagName:{} 匹配结果{},具体匹配：{}，关键词{}",
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
                                e.printStackTrace();
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
                    " \t 关键词："+matchWordArr[0]+"\t 标题："+matchWordArr[1]+"\n"+
                    " \t 关键词："+matchWordArr[2]+"\t desc："+matchWordArr[3]+"\n"+
                    " \t 关键词："+matchWordArr[4]+"\t descV2："+matchWordArr[5]+"\n"+
                    " \t 关键词："+matchWordArr[6]+"\t tagName："+matchWordArr[7]+"\n";

            videoDetail.setThumbUpReason("匹配到了白名单：" + whitelistRule.toString()
                    + "， 具体如下：\n" + matchDetail
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




    /**
     * 白名单关键词自动修正补全
     * 传入一个指定的白名单规则对象，
     * 传入你认为应当符合该规则的视频id
     *
     * @param whitelistRule 需要训练的白名单规则
     * @param whiteAvidList 应当符号白名单规则的视频id集合
     */
    public WhiteListRule trainWhitelistRule(
            WhiteListRule whitelistRule,
            List<Integer> whiteAvidList) {
        if (whitelistRule == null) {
            whitelistRule = new WhiteListRule().setId(String.valueOf(IdGenerator.nextId()));
        }

        log.info("开始对:{} 规则进行训练,训练数据：{}", whitelistRule.getId(), whiteAvidList);
        List<String> titleProcess = new ArrayList<>();
        List<String> descProcess = new ArrayList<>();
        List<String> tagNameProcess = new ArrayList<>();
        for (Integer avid : whiteAvidList) {
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
                e.printStackTrace();
            }

            ThreadUtil.sleep(10);
        }

        //统计频次
        Map<String, Integer> descKeywordFrequencyMap = SegmenterUtil.generateFrequencyMap(descProcess);
        Map<String, Integer> tagNameFrequencyMap = SegmenterUtil.generateFrequencyMap(tagNameProcess);
        Map<String, Integer> titleKeywordFrequencyMap = SegmenterUtil.generateFrequencyMap(titleProcess);
        List<String> topDescKeyWord = SegmenterUtil.getTopFrequentWord(descKeywordFrequencyMap);
        List<String> topTagName = SegmenterUtil.getTopFrequentWord(tagNameFrequencyMap);
        List<String> topTitleKeyWord = SegmenterUtil.getTopFrequentWord(titleKeywordFrequencyMap);

        log.info("本次训练结束 \r\n\t前5的标题关键词是:{} \r\n\t 前5的标签名是:{} \r\n\t 前5的描述关键词是:{}",
                topTitleKeyWord,
                topTagName,
                topDescKeyWord
        );
        Set<String> ignoreKeyWordSet = getWhiteIgnoreKeyWord();
        topTagName.removeAll(ignoreKeyWordSet);
        topTitleKeyWord.removeAll(ignoreKeyWordSet);
        topDescKeyWord.removeAll(ignoreKeyWordSet);

        WhiteListRule finalWhitelistRule = whitelistRule;
        whitelistRule.getTagNameList().addAll(
                keyword2Dict(topTagName,DictType.TAG,finalWhitelistRule.getId())
        );
        whitelistRule.getTitleKeyWordList().addAll(
                keyword2Dict(topTitleKeyWord,DictType.TITLE,finalWhitelistRule.getId())
                );
        whitelistRule.getDescKeyWordList().addAll(
                keyword2Dict(topDescKeyWord,DictType.DESC,finalWhitelistRule.getId())
        );

        return whitelistRule;
    }


}
