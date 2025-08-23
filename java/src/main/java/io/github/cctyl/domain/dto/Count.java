package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public  class Count {
    @JSONField(name = "view")
    private Integer view;
    @JSONField(name = "use")
    private Integer use;
    @JSONField(name = "atten")
    private Integer atten;
}