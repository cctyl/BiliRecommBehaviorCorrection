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
    private Long tagId;

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
