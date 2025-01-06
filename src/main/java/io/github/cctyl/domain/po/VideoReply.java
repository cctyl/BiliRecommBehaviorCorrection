package io.github.cctyl.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视频评论回复表
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class VideoReply extends SimpleAuditingEntity implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;



    /**
     * 视频id
     */
    private String videoId;

    /**
     * 评论id
     */
    private Long rpid;

    /**
     *  评论区对象 id
     *  其实就是视频的avid
     */
    private Integer oid;

    /**
     * 发送者 mid
     */
    private String mid;

    /**
     * 根评论 rpid
     * 若为一级评论则为 0
     * 大于一级评论则为根评论 id
     */
    private Long root;

    /**
     * --	回复父评论 rpid
     * --	若为一级评论则为 0
     * --	若为二级评论则为根评论 rpid
     * --	大于二级评论为上一级评 论 rpid
     */
    private Long parent;

    /**
     * --	回复对方 rpid
     * --	若为一级评论则为 0
     * --	若为二级评论则为该评论 rpid
     * --	大于二级评论为上一级评论 rpid
     */
    private Long dialog;

    /**
     * 评论发送时间
     */
    private Integer ctime;
    /**
     * 发送者等级
     */
    private Integer currentLevel;

    /**
     * 评论者vip状态
     * 0：无
     * 1：月会员
     * 2：年以上会员
     */
    private Integer vipType;

    /**
     * 评论信息
     */
    private String message;

    /**
     * 性别
     */
    private String sex;


}
