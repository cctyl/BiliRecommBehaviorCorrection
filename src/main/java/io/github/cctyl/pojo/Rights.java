package io.github.cctyl.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public  class Rights implements Serializable {
    @JSONField(name = "bp")
    private Integer bp;
    @JSONField(name = "elec")
    private Integer elec;
    @JSONField(name = "download")
    private Integer download;
    @JSONField(name = "movie")
    private Integer movie;
    @JSONField(name = "pay")
    private Integer pay;
    @JSONField(name = "hd5")
    private Integer hd5;
    @JSONField(name = "no_reprint")
    private Integer noReprint;
    @JSONField(name = "autoplay")
    private Integer autoplay;
    @JSONField(name = "ugc_pay")
    private Integer ugcPay;
    @JSONField(name = "is_cooperation")
    private Integer isCooperation;
    @JSONField(name = "ugc_pay_preview")
    private Integer ugcPayPreview;
    @JSONField(name = "no_background")
    private Integer noBackground;
    @JSONField(name = "clean_mode")
    private Integer cleanMode;
    @JSONField(name = "is_stein_gate")
    private Integer isSteinGate;
    @JSONField(name = "is_360")
    private Integer is360;
    @JSONField(name = "no_share")
    private Integer noShare;
    @JSONField(name = "arc_pay")
    private Integer arcPay;
    @JSONField(name = "free_watch")
    private Integer freeWatch;
}