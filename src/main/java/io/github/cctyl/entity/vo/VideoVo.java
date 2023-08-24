package io.github.cctyl.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoVo {

    private Integer aid;
    private String bvid;
    private String title;
    private String blackReason;
    private String thumbUpReason;
}
