package io.github.cctyl.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.github.cctyl.entity.DescV2;
import io.github.cctyl.entity.Tag;
import io.github.cctyl.entity.VideoDetail;
import io.github.cctyl.utils.RedisUtil;
import io.github.cctyl.utils.SegmenterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.*;

/**
 * 黑名单相关的方法
 */
@Service
@Slf4j
public class BlackRuleService {
    @Autowired
    private BiliService biliService;

    @Autowired
    private BiliApi biliApi;

    @Autowired
    private RedisUtil redisUtil;


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
                GlobalVariables.blackUserIdSet.add(videoDetail.getOwner().getMid());
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

        //更新到redis中
        GlobalVariables.setBlackUserIdSet(GlobalVariables.blackUserIdSet);

        //拿到需要忽略的黑名单关键词
        Set<String> ignoreKeyWordSet = getIgnoreKeyWordSet();

        topDescKeyWord.removeAll(GlobalVariables.blackKeywordSet);
        topDescKeyWord.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topDescKeyWord))
        redisUtil.sAdd(BLACK_KEYWORD_CACHE, topDescKeyWord.toArray());

        topTitleKeyWord.removeAll(GlobalVariables.blackKeywordSet);
        topTitleKeyWord.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topTitleKeyWord))
        redisUtil.sAdd(BLACK_KEYWORD_CACHE, topTitleKeyWord.toArray());

        topTagName.removeAll(GlobalVariables.blackTagSet);
        topTagName.removeAll(ignoreKeyWordSet);
        if (CollUtil.isNotEmpty(topTagName))
        redisUtil.sAdd(BLACK_TAG_NAME_CACHE, topTagName.toArray());


    }



    /**
     * 获得忽略的黑名单关键词
     *
     * @return
     */
    public Set<String> getIgnoreKeyWordSet() {
        return redisUtil.sMembers(IGNORE_BLACK_KEYWORD)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
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
            if (human){
                videoDetail.setBlackReason("封面:"+videoDetail.getPic()+" 匹配成功");
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
    public boolean isTitleMatch( VideoDetail videoDetail) {
        String matchWord = GlobalVariables.blackKeywordTree.match(videoDetail.getTitle());
        boolean match = matchWord!=null;
        log.debug("视频:{}-{}的标题：{}，匹配结果：{} ,匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTitle(),
                match,
                matchWord
        );
        if (match){
            videoDetail.setBlackReason("标题:"+videoDetail.getTitle()+" 匹配到了关键词："+matchWord);
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
        String matchWord = GlobalVariables.blackKeywordTree.match(videoDetail.getDesc());
        boolean match = matchWord!=null;
        String desc = videoDetail.getDesc() == null ? "" : videoDetail.getDesc();
        if (CollUtil.isNotEmpty(videoDetail.getDescV2())) {
            match = match || videoDetail.getDescV2()
                    .stream()
                    .map(DescV2::getRawText)
                    .anyMatch(GlobalVariables.blackKeywordTree::isMatch);
            desc = desc + "," + videoDetail.getDescV2().stream().map(DescV2::getRawText).collect(Collectors.joining(","));
        }
        log.debug("视频:{}-{}的 简介：{}，匹配结果：{},匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                desc,
                match,
                matchWord
        );
        if (match){
            videoDetail.setBlackReason("描述:"+desc+" 匹配到了关键词："+matchWord);
        }
        return match;
    }

    /**
     * 分区id匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isTidMatch( VideoDetail videoDetail) {
        boolean match = GlobalVariables.blackTidSet.contains(String.valueOf(videoDetail.getTid()));

        log.debug("视频:{}-{}的 分区：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTid(),
                videoDetail.getTname(),
                match);

        if (match){
            videoDetail.setBlackReason("分区id:"+videoDetail.getTid()+"匹配成功");
        }
        return match;
    }

    /**
     * up主id匹配
     *
     * @param videoDetail
     * @return
     */
    public boolean isMidMatch( VideoDetail videoDetail) {
        if (videoDetail.getOwner() == null || videoDetail.getOwner().getMid() == null) {
            log.error("视频:{}缺少up主信息", videoDetail.toString());
            return false;
        }
        boolean match = GlobalVariables.blackUserIdSet
                .contains(videoDetail.getOwner().getMid());

        log.debug("视频:{}-{}的 up主：{}-{}，匹配结果：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getOwner().getMid(),
                videoDetail.getOwner().getName(),
                match);
        if (match){
            videoDetail.setBlackReason("up主:"+videoDetail.getOwner().getName()+
                    " id:"+ videoDetail.getOwner().getMid() +" 匹配成功");
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
        String matchWord = videoDetail.getTags()
                .stream().map(Tag::getTagName)
                .filter(s -> GlobalVariables.blackTagTree.isMatch(s))
                .findAny().orElse(null);

        boolean match = matchWord!=null;
        log.debug("视频:{}-{}的 tag：{}，匹配结果：{},匹配到的关键词：{}",
                videoDetail.getBvid(),
                videoDetail.getTitle(),
                videoDetail.getTags(),
                match,
                matchWord
        );
        if (match){
            videoDetail.setBlackReason("Tag:"+matchWord+" 匹配到了关键词："+ GlobalVariables.blackTagTree.match(matchWord));
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
        return isTitleMatch( videoDetail)
                ||
                //1.2 简介是否触发黑名单关键词
                isDescMatch( videoDetail)
                ||
                //1.3 标签是否触发关键词,需要先获取标签
                isTagMatch(videoDetail)
                ||
                //1.4 up主id是否在黑名单内
                isMidMatch( videoDetail)
                ||
                //1.5 分区是否触发
                isTidMatch( videoDetail)
                || //1.6 封面是否触发
                isCoverMatch(videoDetail);
    }
}
