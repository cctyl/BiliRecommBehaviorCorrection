package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户投稿视频实体
 */
@NoArgsConstructor
@Data
public class UserSubmissionVideo {
    @JSONField(name = "comment")
    private Integer comment;
    @JSONField(name = "typeid")
    private Long typeid;
    @JSONField(name = "play")
    private String play;
    @JSONField(name = "pic")
    private String pic;
    @JSONField(name = "subtitle")
    private String subtitle;
    @JSONField(name = "description")
    private String description;
    @JSONField(name = "copyright")
    private String copyright;
    @JSONField(name = "title")
    private String title;
    @JSONField(name = "review")
    private Integer review;
    @JSONField(name = "author")
    private String author;
    @JSONField(name = "mid")
    private Long mid;
    @JSONField(name = "created")
    private Integer created;
    @JSONField(name = "length")
    private String length;
    @JSONField(name = "video_review")
    private Integer videoReview;
    @JSONField(name = "aid")
    private Long aid;
    @JSONField(name = "bvid")
    private String bvid;
    @JSONField(name = "hide_click")
    private Boolean hideClick;
    @JSONField(name = "is_pay")
    private Integer isPay;
    @JSONField(name = "is_union_video")
    private Integer isUnionVideo;
    @JSONField(name = "is_steins_gate")
    private Integer isSteinsGate;
    @JSONField(name = "is_live_playback")
    private Integer isLivePlayback;
    @JSONField(name = "meta")
    private Object meta;
    @JSONField(name = "is_avoided")
    private Integer isAvoided;
    @JSONField(name = "attribute")
    private Integer attribute;
    @JSONField(name = "is_charging_arc")
    private Boolean isChargingArc;
    @JSONField(name = "vt")
    private Integer vt;
    @JSONField(name = "enable_vt")
    private Integer enableVt;
}
