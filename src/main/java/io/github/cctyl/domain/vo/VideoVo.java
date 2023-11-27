package io.github.cctyl.domain.vo;

import io.github.cctyl.domain.dto.DislikeReason;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.VideoDetail;
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

    public static VideoVo from(VideoDetail v) {
       return new VideoVo(
                v.getAid(),
                v.getBvid(),
                v.getTitle(),
                v.getBlackReason(),
                v.getThumbUpReason(),
                v.getDislikeReason(),
                v.getOwner().getName(),
                v.getDesc(),
                v.getHandleType(),
                v.getPic()
        );
    }
}
