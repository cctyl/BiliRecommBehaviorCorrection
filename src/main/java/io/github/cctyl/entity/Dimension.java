package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public  class Dimension {
    @JSONField(name = "width")
    private Integer width;
    @JSONField(name = "height")
    private Integer height;
    @JSONField(name = "rotate")
    private Integer rotate;
}