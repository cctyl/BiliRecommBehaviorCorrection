package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class DescV2 implements Serializable {


    @JSONField(name = "raw_text")
    private String rawText;
    @JSONField(name = "type")
    private Integer type;
    @JSONField(name = "biz_id")
    private Integer bizId;
}
