package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public  class Dimension implements Serializable {
    @JSONField(name = "width")
    private Integer width;
    @JSONField(name = "height")
    private Integer height;
    @JSONField(name = "rotate")
    private Integer rotate;
}