package io.github.cctyl.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 用于静态环境下获取容器中的Bean
 */
@Component
public class BeanProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }


    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
}
