package io.github.cctyl.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class VideoUrl {
    @JSONField(name = "from")
    private String from;
    @JSONField(name = "result")
    private String result;
    @JSONField(name = "message")
    private String message;
    @JSONField(name = "quality")
    private Integer quality;
    @JSONField(name = "format")
    private String format;
    @JSONField(name = "timelength")
    private Integer timelength;
    @JSONField(name = "accept_format")
    private String acceptFormat;
    @JSONField(name = "accept_description")
    private List<String> acceptDescription;
    @JSONField(name = "accept_quality")
    private List<Integer> acceptQuality;
    @JSONField(name = "video_codecid")
    private Integer videoCodecid;
    @JSONField(name = "seek_param")
    private String seekParam;
    @JSONField(name = "seek_type")
    private String seekType;
    @JSONField(name = "durl")
    private List<Durl> durl;
    @JSONField(name = "support_formats")
    private List<SupportFormats> supportFormats;
    @JSONField(name = "high_format")
    private Object highFormat;
    @JSONField(name = "last_play_time")
    private Integer lastPlayTime;
    @JSONField(name = "last_play_cid")
    private Integer lastPlayCid;

    @NoArgsConstructor
    @Data
    public static class Durl {
        @JSONField(name = "order")
        private Integer order;
        @JSONField(name = "length")
        private Integer length;
        @JSONField(name = "size")
        private Integer size;
        @JSONField(name = "ahead")
        private String ahead;
        @JSONField(name = "vhead")
        private String vhead;
        @JSONField(name = "url")
        private String url;
        @JSONField(name = "backup_url")
        private List<String> backupUrl;
    }

    @NoArgsConstructor
    @Data
    public static class SupportFormats {
        @JSONField(name = "quality")
        private Integer quality;
        @JSONField(name = "format")
        private String format;
        @JSONField(name = "new_description")
        private String newDescription;
        @JSONField(name = "display_desc")
        private String displayDesc;
        @JSONField(name = "superscript")
        private String superscript;
        @JSONField(name = "codecs")
        private Object codecs;
    }
}
