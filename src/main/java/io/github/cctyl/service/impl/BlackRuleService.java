package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.domain.dto.DescV2;
import io.github.cctyl.domain.dto.DislikeReason;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.domain.po.VideoDetail;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.service.DictService;
import io.github.cctyl.service.ImageGenderDetectService;
import io.github.cctyl.utils.SegmenterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.cctyl.domain.constants.AppConstant.REASON_FORMAT;

/**
 * 黑名单相关的方法
 */
@Service
@Slf4j
public class BlackRuleService {

    @Autowired
    private BiliApi biliApi;

    @Autowired
    private DictService dictService;



    @Autowired
    private ImageGenderDetectService imageGenderDetectService;

    
    
    /**
     * 根据视频列表训练黑名单
     *
     * @param videoList
     */
    public void trainBlacklistByVideoList(
            Collection<VideoDetail> videoList
    ) {

        List<String> titleProcess = new ArrayList<>();
        List<String> descProcess = new ArrayList<>();
        List<String> tagNameProcess = new ArrayList<>();

        for (VideoDetail videoDetail : videoList) {
            // 视频被点踩不要把up主加入黑名单
//            if (videoDetail.getOwner() != null && StrUtil.isNotBlank(videoDetail.getOwner().getMid())) {
//                dictService.addBlackUserId(videoDetail.getOwner().getMid());
//            }
            //1. 标题处理
            String title = videoDetail.getTitle();
            titleProcess.addAll(SegmenterUtil.process(title));

            //2.描述
            String desc = videoDetail.getDesc();
            if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
                List<String> descV2Process = videoDetail.getDescV2()
                        .stream().map(descV2 -> SegmenterUtil.process(descV2.getRawText()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                descProcess.addAll(descV2Process);
            }
            descProcess.addAll(SegmenterUtil.process(desc));
            //3.标签
            if (CollUtil.isNotEmpty(videoDetail.getTags())) {
                List<String> tagNameList = videoDetail.getTags()
                        .stream()
                        .map(Tag::getTagName)
                        .collect(Collectors.toList());
                tagNameProcess.addAll(tagNameList);
            }
        }
        List<String> stopWordList = dictService.getStopWordList();
        List<String> topDescKeyWord = SegmenterUtil.getTopFrequentWord(titleProcess,stopWordList);
        List<String> topTagName = SegmenterUtil.getTopFrequentWord( tagNameProcess,stopWordList);
        List<String> topTitleKeyWord = SegmenterUtil.getTopFrequentWord(descProcess,stopWordList);

        //排除bv开头的无用关键词
        topDescKeyWord = filterStartWithBv(topDescKeyWord);
        topTagName = filterStartWithBv(topTagName);
        topTitleKeyWord = filterStartWithBv(topTitleKeyWord);

        log.info("本次训练结果： desc关键词:{}, 标签:{}, 标题关键词:{}", topDescKeyWord,
                topTagName,
                topTitleKeyWord);


        //拿到需要忽略的黑名单关键词
        Set<String> ignoreKeyWordSet = dictService.getIgnoreBlackKeyWordSet();

        topDescKeyWord.removeAll(dictService. getBlackKeywordSet());
        topDescKeyWord.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topDescKeyWord)) {
            dictService.addBlackCache(topDescKeyWord, DictType.KEYWORD);
        }

        topTitleKeyWord.removeAll(dictService.getBlackKeywordSet()  );
        topTitleKeyWord.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topTitleKeyWord)) {
            dictService.addBlackCache(topTitleKeyWord,DictType.KEYWORD);
        }

        topTagName.removeAll(dictService. getBlackTagSet()  );
        //TODO tag的忽略名单不要套用黑名单关键词的，单独一份，这里需要修改
        topTagName.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topTagName)) {
            dictService.addBlackCache(topTagName,DictType.TAG);
        }


    }

    private static List<String> filterStartWithBv(List<String> topDescKeyWord) {
        return topDescKeyWord.stream().filter(s -> !(s.startsWith("bv") || s.startsWith("BV") || s.startsWith("Bv"))).collect(Collectors.toList());
    }


    /**
     * 封面是否匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isCoverMatch(VideoDetail videoDetail) {
        try {
            byte[] picByte = biliApi.getPicByte(videoDetail.getPic());
            boolean human = imageGenderDetectService.isHuman(picByte);
            log.debug("视频:{}-{}的封面：{}，匹配结果：{}", videoDetail.getBvid(), videoDetail.getTitle(), videoDetail.getPic(), human);
            if (human) {
                videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+
                        String.format(REASON_FORMAT,
                                "封面",
                                videoDetail.getTid(),
                                "成功"
                        )
                );
                //封面匹配，认为是不喜欢这个up
                videoDetail.setDislikeReason(DislikeReason.up(videoDetail.getOwner().getName()));
            }
            return human;
        } catch (Exception e) {
            log.error("获取图片字节码出错：{}", e.getMessage());
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 标题匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isTitleMatch(VideoDetail videoDetail,WordTree blackKeywordTree) {
        String matchWord = blackKeywordTree.match(videoDetail.getTitle());
        boolean match = matchWord != null;
        log.debug("视频:{}-{}的标题：{}，匹配结果：{} ,匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTitle(),
                match,
                matchWord
        );
        if (match) {
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+
                    String.format(REASON_FORMAT,"标题",videoDetail.getTitle(),matchWord)
            );
            //标题匹配到关键字，认为不感兴趣
            videoDetail.setDislikeReason(DislikeReason.notInteresting());

        }
        return match;
    }

    /**
     * 简介匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isDescMatch(VideoDetail videoDetail,WordTree blackKeywordTree) {
        String matchWord = blackKeywordTree.match(videoDetail.getDesc());
        boolean match = matchWord != null;
        String desc = videoDetail.getDesc() == null ? "" : videoDetail.getDesc();
        if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
            match = match || videoDetail.getDescV2()
                    .stream()
                    .map(DescV2::getRawText)
                    .anyMatch(blackKeywordTree ::isMatch);
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
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+
                    String.format(REASON_FORMAT,"描述",desc,matchWord)
            );
            //描述匹配，则认为是不感兴趣。因为描述的准确度不是很高
            videoDetail.setDislikeReason(DislikeReason.notInteresting());
        }
        return match;
    }

    /**
     * 分区id匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isTidMatch(VideoDetail videoDetail,List<String> blackTidSet) {
        boolean match = blackTidSet .contains(String.valueOf(videoDetail.getTid()));

        log.debug("视频:{}-{}的 分区：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTid(),
                videoDetail.getTname(),
                match);

        if (match) {
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+"分区id:" + videoDetail.getTid() + "匹配成功");

            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+
                    String.format(REASON_FORMAT,
                            "分区id",
                            videoDetail.getTid(),
                            "成功"
                    )
            );
            videoDetail.setDislikeReason(DislikeReason.tid(videoDetail.getTname()));
            videoDetail.setDislikeTid(videoDetail.getTid());
        }
        return match;
    }

    /**
     * up主id匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isMidMatch(VideoDetail videoDetail) {
        if (videoDetail.getOwner() == null || videoDetail.getOwner().getMid() == null) {
            log.error("视频:{}缺少up主信息", videoDetail);
            return false;
        }
        boolean match = dictService.getBlackUserIdSet()
                .contains(videoDetail.getOwner().getMid());

        log.debug("视频:{}-{}的 up主：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getOwner().getMid(),
                videoDetail.getOwner().getName(),
                match);
        if (match) {


            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+
                    String.format(REASON_FORMAT,
                            "up主",
                            videoDetail.getOwner().getMid(),
                            "成功"
                    )
            );
            
            videoDetail.setDislikeMid(Long.parseLong(videoDetail.getOwner().getMid()));
            videoDetail.setDislikeReason(DislikeReason.up(videoDetail.getOwner().getName()));
        }
        return match;
    }

    /**
     * 标签匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isTagMatch(VideoDetail videoDetail, Set<String> blackTagSet) {
        Tag matchTag = videoDetail.getTags()
                .stream()
                .filter(tag -> blackTagSet.contains(tag.getTagName()))
                .findAny().orElse(null);

        boolean match = matchTag != null;
        log.debug("视频:{}-{}的 tag：{}，匹配结果：{},匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTags(),
                match,
                match?matchTag.getTagName():""
        );
        if (match) {


            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+
                    String.format(REASON_FORMAT,
                            "Tag",
                            matchTag.getTagName(),
                            "true"
                    )
            );
            
            videoDetail.setDislikeReason(DislikeReason.channel());
            videoDetail.setDislikeTagId(matchTag.getTagId());
        }
        return match;
    }

    /**
     * 黑名单判断
     *
     * @param videoDetail
     * @param blackTagSet
     * @param blackKeywordTree
     * @return
     */
    public boolean blackMatch(VideoDetail videoDetail,
                              Set<String> blackTagSet,
                              WordTree blackKeywordTree,
                              List<String> blackTidSet
                              ) {
        //1.1 标题是否触发黑名单关键词
        return isTitleMatch(videoDetail, blackKeywordTree)
                ||
                //1.2 简介是否触发黑名单关键词
                isDescMatch(videoDetail,blackKeywordTree)
                ||
                //1.3 标签是否触发关键词,需要先获取标签
                isTagMatch(videoDetail,blackTagSet)
                ||
                //1.4 up主id是否在黑名单内
                isMidMatch(videoDetail)
                ||
                //1.5 分区是否触发
                isTidMatch(videoDetail,blackTidSet)
                || //1.6 封面是否触发
                isCoverMatch(videoDetail);
    }
}
