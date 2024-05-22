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

    private String videoId;
}
