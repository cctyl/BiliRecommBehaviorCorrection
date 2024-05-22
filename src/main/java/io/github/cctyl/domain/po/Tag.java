package io.github.cctyl.domain.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.cctyl.domain.dto.Count;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Tag extends SimpleAuditingEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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
    @TableField(exist = false)
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


    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}
