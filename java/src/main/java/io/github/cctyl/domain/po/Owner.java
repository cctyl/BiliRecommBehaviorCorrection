package io.github.cctyl.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class Owner extends SimpleAuditingEntity implements Serializable {
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

}
