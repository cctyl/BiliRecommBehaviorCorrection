package io.github.cctyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
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
            if (videoDetail.getOwner() != null && StrUtil.isNotBlank(videoDetail.getOwner().getMid())) {
                GlobalVariables.INSTANCE.addBlackUserId(videoDetail.getOwner().getMid());
            }
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
        List<String> topDescKeyWord = SegmenterUtil.getTopFrequentWord(titleProcess);
        List<String> topTagName = SegmenterUtil.getTopFrequentWord(descProcess);
        List<String> topTitleKeyWord = SegmenterUtil.getTopFrequentWord(tagNameProcess);

        log.info("本次训练结果： desc关键词:{}, 标签:{}, 标题关键词:{}", topDescKeyWord,
                topTagName,
                topTitleKeyWord);


        //拿到需要忽略的黑名单关键词
        Set<String> ignoreKeyWordSet = GlobalVariables.getIgnoreBlackKeyWordSet();

        topDescKeyWord.removeAll(GlobalVariables. getBlackKeywordSet());
        topDescKeyWord.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topDescKeyWord)) {
            dictService.addBlackCache(topDescKeyWord, DictType.KEYWORD);
        }

        topTitleKeyWord.removeAll(GlobalVariables.getBlackKeywordSet()  );
        topTitleKeyWord.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topTitleKeyWord)) {
            dictService.addBlackCache(topTitleKeyWord,DictType.KEYWORD);
        }

        topTagName.removeAll(GlobalVariables. getBlackTagSet()  );
        topTagName.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topTagName)) {
            dictService.addBlackCache(topTagName,DictType.TAG);
        }


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
                videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+"封面:" + videoDetail.getPic() + " 匹配成功");
                //封面匹配，认为是不喜欢这个up
                videoDetail.setDislikeReason(DislikeReason.up(videoDetail.getOwner().getName()));
            }
            return human;
        } catch (Exception e) {
            log.error("获取图片字节码出错：{}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 标题匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isTitleMatch(VideoDetail videoDetail) {
        String matchWord = GlobalVariables.getBlackKeywordTree() .match(videoDetail.getTitle());
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
                    "标题:" + videoDetail.getTitle() + " 匹配到了关键词：" + matchWord);
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
    public boolean isDescMatch(VideoDetail videoDetail) {
        String matchWord = GlobalVariables.getBlackKeywordTree().match(videoDetail.getDesc());
        boolean match = matchWord != null;
        String desc = videoDetail.getDesc() == null ? "" : videoDetail.getDesc();
        if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
            match = match || videoDetail.getDescV2()
                    .stream()
                    .map(DescV2::getRawText)
                    .anyMatch(GlobalVariables.getBlackKeywordTree() ::isMatch);
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
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+"描述:" + desc + " 匹配到了关键词：" + matchWord);
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
    public boolean isTidMatch(VideoDetail videoDetail) {
        boolean match = GlobalVariables.getBlackTidSet() .contains(String.valueOf(videoDetail.getTid()));

        log.debug("视频:{}-{}的 分区：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTid(),
                videoDetail.getTname(),
                match);

        if (match) {
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+"分区id:" + videoDetail.getTid() + "匹配成功");
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
        boolean match = GlobalVariables.getBlackUserIdSet()
                .contains(videoDetail.getOwner().getMid());

        log.debug("视频:{}-{}的 up主：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getOwner().getMid(),
                videoDetail.getOwner().getName(),
                match);
        if (match) {
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+"up主:" + videoDetail.getOwner().getName() +
                    " id:" + videoDetail.getOwner().getMid() + " 匹配成功");

            videoDetail.setDislikeMid(Integer.parseInt(videoDetail.getOwner().getMid()));
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
    public boolean isTagMatch(VideoDetail videoDetail) {
        Tag matchTag = videoDetail.getTags()
                .stream()
                .filter(tag -> GlobalVariables.getBlackTagTree().isMatch(tag.getTagName()))
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
            videoDetail.setBlackReason(Opt.ofNullable(videoDetail.getBlackReason()).orElse("")+"Tag:" + matchTag.getTagName() + " 匹配到了关键词：" +
                    GlobalVariables.getBlackTagTree().match(matchTag.getTagName()));
            videoDetail.setDislikeReason(DislikeReason.channel());
            videoDetail.setDislikeTagId(matchTag.getTagId());
        }
        return match;
    }

    /**
     * 黑名单判断
     *
     * @param videoDetail
     * @return
     */
    public boolean blackMatch(VideoDetail videoDetail) {
        //1.1 标题是否触发黑名单关键词
        return isTitleMatch(videoDetail)
                ||
                //1.2 简介是否触发黑名单关键词
                isDescMatch(videoDetail)
                ||
                //1.3 标签是否触发关键词,需要先获取标签
                isTagMatch(videoDetail)
                ||
                //1.4 up主id是否在黑名单内
                isMidMatch(videoDetail)
                ||
                //1.5 分区是否触发
                isTidMatch(videoDetail)
                || //1.6 封面是否触发
                isCoverMatch(videoDetail);
    }
}
