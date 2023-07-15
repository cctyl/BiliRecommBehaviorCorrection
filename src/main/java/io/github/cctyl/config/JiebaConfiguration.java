package io.github.cctyl.config;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiebaConfiguration {

    @Bean
    public JiebaSegmenter jiebaSegmenter(){
       return new JiebaSegmenter();
    }


}
