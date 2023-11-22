package io.github.cctyl.domain.enumeration;

/**
 * 视频的处理结果
 */
public enum  HandleType  {
    /**
     * 点赞
     */
    THUMB_UP,

    /**
     * 点踩
     */
    DISLIKE,

    /**
     * 未点赞也未点踩，暂时不匹配白名单也不匹配黑名单
     */
    OTHER

}
