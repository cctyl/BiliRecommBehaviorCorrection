package io.github.cctyl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfiguration {

    @Bean
    public Docket defaultApi2() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("BiliRecommBehaviorCorrection RESTful APIs")

                        .description("bili推荐纠正API文档")
                        .termsOfServiceUrl("https://github.com/cctyl/BiliRecommBehaviorCorrection/")
                        .version("1.0")

                        .build())
                .groupName("default")
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.github.cctyl.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
}
