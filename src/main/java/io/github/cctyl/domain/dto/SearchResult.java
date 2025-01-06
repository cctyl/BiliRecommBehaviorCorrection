package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class SearchResult {
    /**
     * 搜索结果类型，因为我们目标是视频，这里通常是 video
     */
    @JSONField(name = "type")
    private String type;

    /**
     * avid，没有前面的av两个字母
     */
    @JSONField(name = "id")
    private String avid;

    /**
     * UP主昵称
     */
    @JSONField(name = "author")
    private String author;

    /**
     * UP主mid
     */
    @JSONField(name = "mid")
    private Long mid;

    /**
     * 视频分区tid
     */
    @JSONField(name = "typeid")
    private String typeId;

    /**
     * 视频子分区名
     */
    @JSONField(name = "typename")
    private String typeName;

    /**
     * 视频重定向url
     */
    @JSONField(name = "arcurl")
    private String arcUrl;

    /**
     * 稿件avid
     */
    @JSONField(name = "aid")
    private Long aid;
    /**
     * bvid
     */
    @JSONField(name = "bvid")
    private String bvid;


    /**
     * 视频标题 	关键字用xml标签<em class="keyword">标注
     * 需要做一定处理
     */
    @JSONField(name = "title")
    private String title;

    /**
     * 视频简介
     */
    @JSONField(name = "description")
    private String description;

    /**
     * 视频封面url
     */
    @JSONField(name = "pic")
    private String pic;

    /**
     * 视频播放量
     */
    @JSONField(name = "play")
    private Integer play;
    @JSONField(name = "video_review")
    private Integer videoReview;
    @JSONField(name = "favorites")
    private Integer favorites;

    /**
     * 视频TAG 每项TAG用,分隔
     */
    @JSONField(name = "tag")
    private String tag;

    @JSONField(name = "review")
    private Integer review;

    @JSONField(name = "pubdate")
    private Integer pubdate;

    @JSONField(name = "senddate")
    private Integer senddate;

    @JSONField(name = "duration")
    private String duration;

    @JSONField(name = "badgepay")
    private Boolean badgepay;

    /**
     * 关键字匹配类型
     */
    @JSONField(name = "hit_columns")
    private List<String> hitColumns;

    @JSONField(name = "view_type")
    private String viewType;

    @JSONField(name = "is_pay")
    private Integer isPay;

    @JSONField(name = "is_union_video")
    private Integer isUnionVideo;

    @JSONField(name = "rec_tags")
    private Object recTags;

    @JSONField(name = "new_rec_tags")
    private List<?> newRecTags;

    @JSONField(name = "rank_score")
    private Integer rankScore;

    /**
     * 获赞数
     */
    @JSONField(name = "like")
    private Integer like;

    /**
     * up主头像url
     */
    @JSONField(name = "upic")
    private String upic;

    /**
     * 角标有无
     */
    @JSONField(name = "corner")
    private String corner;

    /**
     *  	分集封面url
     */
    @JSONField(name = "cover")
    private String cover;





    @JSONField(name = "danmaku")
    private Integer danmaku;

    @JSONField(name = "biz_data")
    private Object bizData;

    @JSONField(name = "is_charge_video")
    private Integer isChargeVideo;
}
