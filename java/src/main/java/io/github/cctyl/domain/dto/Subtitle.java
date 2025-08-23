package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public  class Subtitle {
    @JSONField(name = "allow_submit")
    private Boolean allowSubmit;
    @JSONField(name = "list")
    private List<?> list;
}