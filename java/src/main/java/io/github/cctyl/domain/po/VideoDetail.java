package io.github.cctyl.domain.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.cctyl.domain.dto.*;
import io.github.cctyl.domain.enumeration.HandleType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 视频详情
 * https://socialsisteryi.github.io/bilibili-API-collect/docs/video/info.html#%E8%8E%B7%E5%8F%96%E8%A7%86%E9%A2%91%E8%AF%A6%E7%BB%86%E4%BF%A1%E6%81%AF-web%E7%AB%AF
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class VideoDetail extends SimpleAuditingEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * aid
     */
    @JSONField(name = "aid")
    private Long aid;

    //稿件分P总数
    @JSONField(name = "videos")
    private Integer videos;
    /**
     * 分区id
     */
    @JSONField(name = "tid")
    private Long tid;
    /**
     * 子分区名称
     */
    @JSONField(name = "tname")
    private String tname;

    /**
     * 转载还是原创 	1：原创
     * 2：转载
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

    //用户投稿时间戳-秒
    @JSONField(name = "ctime")
    private Integer ctime;

    /**
     * 视频描述
     */
    @JSONField(name = "desc")
    private String desc;
    /*

    state字段值(稿件状态)
    值	内容	备注
    1	橙色通过
    0	开放浏览
    -1	待审
    -2	被打回
    -3	网警锁定
    -4	被锁定	视频撞车了
    -5	管理员锁定
    -6	修复待审
    -7	暂缓审核
    -8	补档待审
    -9	等待转码
    -10	延迟审核
    -11	视频源待修
    -12	转储失败
    -13	允许评论待审
    -14	临时回收站
    -15	分发中
    -16	转码失败
    -20	创建未提交
    -30	创建已提交
    -40	定时发布
    -50	仅UP主可见
    -100	用户删除
     */
    @JSONField(name = "state")
    private Integer state;
    /**
     * 视频时长-秒
     */
    @JSONField(name = "duration")
    private Integer duration;

    //	稿件参与的活动id
    @JSONField(name = "mission_id")
    private Long missionId;

    /**
     * 视频同步发布的的动态的文字内容
     */
    @JSONField(name = "dynamic")
    private String dynamic;
    //视频1P cid
    @JSONField(name = "cid")
    private Long cid;

    //视频合集中分部所属视频合集id
    @JSONField(name = "season_id")
    private Long seasonId;

    //短链接类似 "https://b23.tv/BV1hJ4m177RN"
    @JSONField(name = "short_link_v2")
    private String shortLinkV2;

    //分P封面
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


    //不明确
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

    /**
     * bp num 是否允许承包
     * elec	num	是否支持充电
     * download	num	是否允许下载
     * movie	num	是否电影
     * pay	num	是否PGC付费
     * hd5	num	是否有高码率
     * no_reprint	num	是否显示“禁止转载”标志
     * autoplay	num	是否自动播放
     * ugc_pay	num	是否UGC付费
     * is_cooperation	num	是否为联合投稿
     */
    @JSONField(name = "rights")
    @TableField(exist = false)
    private Rights rights;


    //前分P分辨率
    @JSONField(name = "dimension")
    @TableField(exist = false)
    private Dimension dimension;


    @JSONField(name = "premiere")
    @TableField(exist = false)
    private Object premiere;

    //用于青少年模式
    @JSONField(name = "teenage_mode")
    private Integer teenageMode;

    //充电相关
    @JSONField(name = "is_chargeable_season")
    private Boolean isChargeableSeason;

    //是否可以在 Story Mode 展示?
    @JSONField(name = "is_story")
    private Boolean isStory;
    //	是否为充电专属视频
    @JSONField(name = "is_upower_exclusive")
    private Boolean isUpowerExclusive;

    // 充电相关
    @JSONField(name = "is_upower_play")
    private Boolean isUpowerPlay;
    //是否不允许缓存?
    @JSONField(name = "no_cache")
    private Boolean noCache;


    //视频分P列表
    @JSONField(name = "pages")
    @TableField(exist = false)
    private List<Pages> pages;

    /**
     * 视频CC字幕信息
     */
    @JSONField(name = "subtitle")
    @TableField(exist = false)
    private Subtitle subtitle;


    @JSONField(name = "is_season_display")
    private Boolean isSeasonDisplay;
    //用户装扮信息
    @JSONField(name = "user_garb")
    @TableField(exist = false)
    private UserGarb userGarb;


    @JSONField(name = "honor_reply")
    @TableField(exist = false)
    private HonorReply honorReply;

    //点赞图标的url
    @JSONField(name = "like_icon")
    private String likeIcon;

    //需要跳转到BV号?
    @JSONField(name = "need_jump_bv")
    private Boolean needJumpBv;

    private HandleType handleType;

    @JSONField(name = "desc_v2")
    @TableField(exist = false)
    private List<DescV2> descV2 = new ArrayList<>();


    @JSONField(name = "enable_vt")
    private Integer enableVt;

    //	禁止展示UP主信息?
    @JSONField(name = "disable_show_up_info")
    private Boolean disableShowUpInfo;

    @JSONField(name = "up_from_v2")
    private Integer upFromV2;

    //推荐理由 ，比如 3w点赞，竖屏
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

    @TableField(value = "version",fill = FieldFill.INSERT)
    @Version
    @JsonIgnore
    private Integer version;





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
