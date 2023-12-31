package io.github.cctyl.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConfigDTO {

    @Schema(description = "bilibili的accessKey ，一般不要手动改")
    private String biliAccessKey;

    @Schema(description = "bili wbi相关，一般不要手动改")
    private String imgKey;

    @Schema(description = "bili wbi相关，一般不要手动改")
    private String subKey;

    @Schema(description = "百度accessKey")
    private String baiduAskKey;

    @Schema(description = "百度客户端id")
    private String baiduClientId;

    @Schema(description = "百度客户端密钥")
    private String baiduClientSecret;

    @Schema(description = "本人bilibili用户id")
    private String mid;

    @Schema(description = "最小播放时长")
    private Integer minPlaySecond;


    @Schema(description = "定时任务开关")
    private Boolean cron;
}
