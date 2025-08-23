package io.github.cctyl.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 确保 applicationContext 被注入
     */
    @PostConstruct
    public void init() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext is not injected!");
        }
    }
    public static Object getBeanByClassPath(String classPath) throws ClassNotFoundException {
        // 根据类路径加载 Class 对象
        Class<?> clazz = Class.forName(classPath);
        // 从 Spring 容器中获取 Bean
        return applicationContext.getBean(clazz);
    }
}