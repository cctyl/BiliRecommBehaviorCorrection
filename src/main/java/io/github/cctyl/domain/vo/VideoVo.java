package io.github.cctyl.domain.vo;

import io.github.cctyl.domain.dto.DislikeReason;
import io.github.cctyl.domain.enumeration.HandleType;
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
    private DislikeReason dislikeReason;
    private String upName;
    private String desc;
    public HandleType handleType;
    private String coverUrl;


}