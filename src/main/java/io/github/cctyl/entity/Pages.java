package io.github.cctyl.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public  class Pages {
    @JSONField(name = "cid")
    private Integer cid;
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