package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class VideoDetail {


    /**
     * aid
     */
    @JSONField(name = "aid")
    private Integer aid;
    @JSONField(name = "videos")
    private Integer videos;
    /**
     * 分区id
     */
    @JSONField(name = "tid")
    private Integer tid;
    /**
     * 分区名
     */
    @JSONField(name = "tname")
    private String tname;

    /**
     * 转载还是原创
     */
    @JSONField(name = "copyright")
    private Integer copyright;
    /**
     * 封面url
     */
    @JSONField(name = "pic")
    private String pic;

    /**
     * 视频标题
     */
    @JSONField(name = "title")
    private String title;
    /**
     * 发布时间戳-秒
     */
    @JSONField(name = "pubdate")
    private Integer pubdate;
    @JSONField(name = "ctime")
    private Integer ctime;

    /**
     * 视频描述
     */
    @JSONField(name = "desc")
    private String desc;
    @JSONField(name = "state")
    private Integer state;
    /**
     * 视频时长-秒
     */
    @JSONField(name = "duration")
    private Integer duration;
    @JSONField(name = "mission_id")
    private Integer missionId;
    /**
     * 新版本视频简介
     */
    @JSONField(name = "desc_v2")
    private List<String> descV2;

    /**
     * 视频同步发布的的动态的文字内容
     */
    @JSONField(name = "dynamic")
    private String dynamic;
    @JSONField(name = "cid")
    private Integer cid;
    @JSONField(name = "season_id")
    private Integer seasonId;
    @JSONField(name = "short_link_v2")
    private String shortLinkV2;
    @JSONField(name = "first_frame")
    private String firstFrame;
    /**
     * 发布时的ip地址
     */
    @JSONField(name = "pub_location")
    private String pubLocation;
    /**
     * bvid
     */
    @JSONField(name = "bvid")
    private String bvid;
    @JSONField(name = "season_type")
    private Integer seasonType;
    @JSONField(name = "is_ogv")
    private Boolean isOgv;
    @JSONField(name = "ogv_info")
    private Object ogvInfo;

    /**
     * 视频所有者信息
     */
    @JSONField(name = "owner")
    private Owner owner;
    /**
     * 视频的数据
     */
    @JSONField(name = "stat")
    private Stat stat;

    /**
     * 标签列表
     */
    List<Tag> tagList;

    @JSONField(name = "rights")
    private Rights rights;


    @JSONField(name = "dimension")
    private Dimension dimension;
    @JSONField(name = "premiere")
    private Object premiere;
    @JSONField(name = "teenage_mode")
    private Integer teenageMode;
    @JSONField(name = "is_chargeable_season")
    private Boolean isChargeableSeason;
    @JSONField(name = "is_story")
    private Boolean isStory;
    @JSONField(name = "is_upower_exclusive")
    private Boolean isUpowerExclusive;
    @JSONField(name = "is_upower_play")
    private Boolean isUpowerPlay;
    @JSONField(name = "no_cache")
    private Boolean noCache;
    @JSONField(name = "pages")
    private List<Pages> pages;
    @JSONField(name = "subtitle")
    private Subtitle subtitle;
    @JSONField(name = "is_season_display")
    private Boolean isSeasonDisplay;
    @JSONField(name = "user_garb")
    private UserGarb userGarb;
    @JSONField(name = "honor_reply")
    private HonorReply honorReply;
    @JSONField(name = "like_icon")
    private String likeIcon;
    @JSONField(name = "need_jump_bv")
    private Boolean needJumpBv;


    @NoArgsConstructor
    @Data
    public static class UserGarb {
    }

    @NoArgsConstructor
    @Data
    public static class HonorReply {
    }


}