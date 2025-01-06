package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public  class Pages implements Serializable {
    @JSONField(name = "cid")
    private Long cid;
    @JSONField(name = "page")
    private Integer page;
    @JSONField(name = "from")
    private String from;
    @JSONField(name = "part")
    private String part;
    @JSONField(name = "duration")
    private Integer duration;
    @JSONField(name = "vid")
    private String vid;
    @JSONField(name = "weblink")
    private String weblink;
    @JSONField(name = "dimension")
    private Dimension dimension;
    @JSONField(name = "first_frame")
    private String firstFrame;

}