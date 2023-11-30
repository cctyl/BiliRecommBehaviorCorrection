package io.github.cctyl.domain.vo;

import cn.hutool.core.lang.Opt;
import io.github.cctyl.domain.dto.DislikeReason;
import io.github.cctyl.domain.enumeration.HandleType;
import io.github.cctyl.domain.po.Owner;
import io.github.cctyl.domain.po.VideoDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoVo {

    private String id;
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
    private Owner owner;

    public static VideoVo from(VideoDetail v) {
       return new VideoVo(
                v.getId(),
                v.getAid(),
                v.getBvid(),
                v.getTitle(),
                v.getBlackReason(),
                v.getThumbUpReason(),
                v.getDislikeReason(),
                Opt.ofNullable( v.getOwner()).map(Owner::getName).orElse(""),
                v.getDesc(),
                v.getHandleType(),
                v.getPic(),
               null
        );
    }
}
