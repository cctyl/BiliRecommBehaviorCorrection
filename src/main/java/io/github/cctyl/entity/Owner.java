package io.github.cctyl.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Owner {
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
