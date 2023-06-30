package io.github.cctyl.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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



    private Baidu baidu;

    private Ws ws;

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
}
