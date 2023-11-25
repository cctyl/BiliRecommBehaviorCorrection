package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class Tag implements Serializable {
    @JSONField(name = "tag_id")
    private Integer tagId;
    @JSONField(name = "tag_name")
    private String tagName;
    @JSONField(name = "cover")
    private String cover;
    @JSONField(name = "head_cover")
    private String headCover;
    @JSONField(name = "content")
    private String content;
    @JSONField(name = "short_content")
    private String shortContent;
    @JSONField(name = "type")
    private Integer type;
    @JSONField(name = "state")
    private Integer state;
    @JSONField(name = "ctime")
    private Integer ctime;
    @JSONField(name = "count")
    private Count count;
    @JSONField(name = "is_atten")
    private Integer isAtten;
    @JSONField(name = "likes")
    private Integer likes;
    @JSONField(name = "hates")
    private Integer hates;
    @JSONField(name = "attribute")
    private Integer attribute;
    @JSONField(name = "liked")
    private Integer liked;
    @JSONField(name = "hated")
    private Integer hated;
    @JSONField(name = "extra_attr")
    private Integer extraAttr;
    @JSONField(name = "music_id")
    private String musicId;
    @JSONField(name = "tag_type")
    private String tagType;
    @JSONField(name = "is_activity")
    private Boolean isActivity;
    @JSONField(name = "color")
    private String color;
    @JSONField(name = "alpha")
    private Integer alpha;
    @JSONField(name = "is_season")
    private Boolean isSeason;
    @JSONField(name = "subscribed_count")
    private Integer subscribedCount;
    @JSONField(name = "archive_count")
    private String archiveCount;
    @JSONField(name = "featured_count")
    private Integer featuredCount;
    @JSONField(name = "jump_url")
    private String jumpUrl;

    @NoArgsConstructor
    @Data
    public static class Count {
        @JSONField(name = "view")
        private Integer view;
        @JSONField(name = "use")
        private Integer use;
        @JSONField(name = "atten")
        private Integer atten;
    }


    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}
