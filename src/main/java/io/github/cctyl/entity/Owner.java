package io.github.cctyl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.cctyl.pojo.AuditingEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class Owner extends AuditingEntity implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 视频up 用户id
     */
    @JsonProperty("mid")
    private String mid;

    @JsonProperty("name")
    private String name;
    @JsonProperty("face")
    private String face;

    private String videoId;
}
