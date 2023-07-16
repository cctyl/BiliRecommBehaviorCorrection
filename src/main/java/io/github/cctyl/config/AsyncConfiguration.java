package io.github.cctyl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXI_MUM_POOL_SIZE = 20;
    private static final long KEEP_ALIVE_TIME = 500l;

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    private final TaskExecutionProperties taskExecutionProperties;

    public AsyncConfiguration(TaskExecutionProperties taskExecutionProperties) {
        this.taskExecutionProperties = taskExecutionProperties;
    }

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Bean(name = "asyncPoolExecutor")
    public static ThreadPoolExecutor getPool(){
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXI_MUM_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.MICROSECONDS,//毫秒
            new LinkedBlockingDeque<>(10),
            new ThreadPoolExecutor.AbortPolicy()//标记阻塞队列最靠前的任务先执行
        );
        return pool;
    }
}
