package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Tag {
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
}
