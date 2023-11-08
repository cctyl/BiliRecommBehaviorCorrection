package io.github.cctyl.pojo.vo;

import io.github.cctyl.pojo.DislikeReason;
import io.github.cctyl.pojo.enumeration.HandleType;
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
