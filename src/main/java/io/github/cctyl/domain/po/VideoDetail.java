package io.github.cctyl.domain.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.cctyl.domain.dto.*;
import io.github.cctyl.domain.enumeration.HandleType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Data
@Accessors(chain = true)
public class VideoDetail extends AuditingEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * aid
     */
    @JSONField(name = "aid")
    private Long aid;


    @JSONField(name = "videos")
    private Integer videos;
    /**
     * 分区id
     */
    @JSONField(name = "tid")
    private Long tid;
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
    private Long missionId;

    /**
     * 视频同步发布的的动态的文字内容
     */
    @JSONField(name = "dynamic")
    private String dynamic;

    @JSONField(name = "cid")
    private Long cid;

    @JSONField(name = "season_id")
    private Long seasonId;

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

    /**
     * 视频是否处理过
     */
    private boolean handle;


    @JSONField(name = "ogv_info")
    @TableField(exist = false)
    private Object ogvInfo;

    /**
     * 视频所有者信息
     */
    @JSONField(name = "owner")
    @TableField(exist = false)
    private Owner owner;

    private String ownerId;

    /**
     * 视频的数据
     */
    @JSONField(name = "stat")
    @TableField(exist = false)
    private Stat stat;



    /**
     * 标签列表
     */
    @TableField(exist = false)
    List<Tag> tags;

    @JSONField(name = "rights")
    @TableField(exist = false)
    private Rights rights;



    @JSONField(name = "dimension")
    @TableField(exist = false)
    private Dimension dimension;


    @JSONField(name = "premiere")
    @TableField(exist = false)
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
    @TableField(exist = false)
    private List<Pages> pages;

    /**
     * 字幕相关
     */
    @JSONField(name = "subtitle")
    @TableField(exist = false)
    private Subtitle subtitle;


    @JSONField(name = "is_season_display")
    private Boolean isSeasonDisplay;

    @JSONField(name = "user_garb")
    @TableField(exist = false)
    private UserGarb userGarb;


    @JSONField(name = "honor_reply")
    @TableField(exist = false)
    private HonorReply honorReply;


    @JSONField(name = "like_icon")
    private String likeIcon;

    @JSONField(name = "need_jump_bv")
    private Boolean needJumpBv;

    private HandleType handleType;

    @JSONField(name = "desc_v2")
    @TableField(exist = false)
    private List<DescV2> descV2 = new ArrayList<>();


    @JSONField(name = "enable_vt")
    private Integer enableVt;


    @JSONField(name = "disable_show_up_info")
    private Boolean disableShowUpInfo;

    @JSONField(name = "up_from_v2")
    private Integer upFromV2;

    @JSONField(name = "rcmd_reason")
    private String rcmdReason;

    /**
     * 相关推荐视频列表
     */
    @TableField(exist = false)
    private List<VideoDetail> relatedVideoList = new ArrayList<>();

    @JSONField(name = "score")
    private Integer score;

    @JSONField(name = "others")
    @TableField(exist = false)
    private List<VideoDetail> others;


    /**
     * 点踩原因
     */
    private String blackReason;
    /**
     * 点踩原因id
     */
    @TableField(exist = false)
    private DislikeReason dislikeReason;
    /**
     * 点踩的up id
     */
    @TableField(exist = false)
    private Long dislikeMid;
    /**
     * 点踩的对象板块id
     */
    @TableField(exist = false)
    private Long dislikeTid;

    /**
     * 点踩的tag id
     */
    @TableField(exist = false)
    private Long dislikeTagId;


    /**
     * 点赞原因
     */
    private String thumbUpReason;







    /**
     * 返回简洁的信息
     * @return
     */
    @Override
    public String toString() {
        return "VideoDetail{" +
                "aid=" + aid +
                ", tid=" + tid +
                ", tname='" + tname + '\'' +
                ", pic='" + pic + '\'' +
                ", title='" + title + '\'' +
                ", pubdate=" + pubdate +
                ", ctime=" + ctime +
                ", desc='" + desc + '\'' +
                ", duration=" + duration +
                ", bvid='" + bvid + '\'' +
                ", owner=" + owner +
                ", tags=" +
                (tags != null ?
                        tags.stream().map(
                                tag -> "tagId:" + tag.getTagId() + " tagName:" + tag.getTagName()
                        )
                        :
                        "[]"
                )
                +
                ", subtitle=" + subtitle +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoDetail that = (VideoDetail) o;
        return Objects.equals(aid, that.aid) &&
                Objects.equals(bvid, that.bvid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aid, bvid);
    }
}
