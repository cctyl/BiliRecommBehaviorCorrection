package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频的相关数据
 * 弹幕数、硬币数、点赞数
 */
@NoArgsConstructor
@Data
public class Stat {
    @JSONField(name = "aid")
    private Integer aid;
    @JSONField(name = "view")
    private Integer view;
    @JSONField(name = "danmaku")
    private Integer danmaku;
    @JSONField(name = "reply")
    private Integer reply;
    @JSONField(name = "favorite")
    private Integer favorite;
    @JSONField(name = "coin")
    private Integer coin;
    @JSONField(name = "share")
    private Integer share;
    @JSONField(name = "now_rank")
    private Integer nowRank;
    @JSONField(name = "his_rank")
    private Integer hisRank;
    @JSONField(name = "like")
    private Integer like;
    @JSONField(name = "dislike")
    private Integer dislike;
    @JSONField(name = "vt")
    private Integer vt;
    @JSONField(name = "vv")
    private Integer vv;
}
