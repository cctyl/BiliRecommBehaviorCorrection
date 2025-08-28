package io.github.cctyl.domain.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 视频的相关数据
 * 弹幕数、硬币数、点赞数
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class Stat extends SimpleAuditingEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @JSONField(name = "aid")
    private Long aid;
    /**
     * 播放数
     */
    @JSONField(name = "view")
    private Integer view;

    /**
     * 弹幕数
     */
    @JSONField(name = "danmaku")
    private Integer danmaku;


    /**
     * 评论数
     */
    @JSONField(name = "reply")
    private Integer reply;


    /**
     * 收藏数
     */
    @JSONField(name = "favorite")
    private Integer favorite;

    /**
     *投币数
     */
    @JSONField(name = "coin")
    private Integer coin;

    /**
     * 分享数
     */
    @JSONField(name = "share")
    private Integer share;

    /**
     * 当前排名
     */
    @JSONField(name = "now_rank")
    private Integer nowRank;

    /**
     * 历史最高排名
     */
    @JSONField(name = "his_rank")
    private Integer hisRank;


    /**
     * 点赞数
     */
    @JSONField(name = "like")
    private Integer like;

    /**
     * 点踩数
     */
    @JSONField(name = "dislike")
    private Integer dislike;


    @JSONField(name = "vt")
    private Integer vt;

    @JSONField(name = "vv")
    private Integer vv;

    private String videoId;
}
