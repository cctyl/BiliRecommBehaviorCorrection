package io.github.cctyl.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@ConfigurationProperties(prefix = "common")
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProperties {

    /**
     * 是否需要初始化
     */
    private Boolean init;
    private Integer minPlaySecond;

    private Baidu baidu;

    private Ws ws;

    private DefaultData defaultData;



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Baidu{
        private Boolean enable;
        private String clientId;
        private String clientSecret;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ws{
        private Boolean enable;
        private String url;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DefaultData{
        private String cookie;
        private String mid;
        private List<String> keyWord;
        private List<String> blackUserId;
        private List<String> blackKeyWord;
        private List<String> blackTag;
        private List<String> blackTid;
        private List<String> whiteTid;
        private List<String> whiteUserId;
    }

}
