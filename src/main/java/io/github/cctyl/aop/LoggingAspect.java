package io.github.cctyl.aop;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.cctyl.entity.VideoDetail;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * 根据注解
     */
    @Pointcut(
            "within(@org.springframework.stereotype.Repository *)" +
                    " || within(@org.springframework.stereotype.Service *)" +
                    " || within(@org.springframework.web.bind.annotation.RestController *)" +
                    " || within(@org.springframework.stereotype.Component *)"
    )
    public void springBeanPointcut() {
    }

    /**
     * 根据包名
     */
    @Pointcut(
            "within(io.github.cctyl.service..*)" +
                    " || within(io.github.cctyl.controller..*)" +
                    " || within(io.github.cctyl.initialization..*)" +
                    " || within(io.github.cctyl.task..*) " +
                    " || within(io.github.cctyl.api..*) "
    )
    public void applicationPackagePointcut() {
    }

    /**
     * 获取类对应的logger
     *
     * @param joinPoint
     * @return
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * 异常通知
     *
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        logger(joinPoint)
                .error(
                        "Exception in {}() with cause = \'{}\' and exception = \'{}\'",
                        joinPoint.getSignature().getName(),
                        e.getCause() != null ? e.getCause() : "NULL",
                        e.getMessage(),
                        e
                );
    }


    /**
     * 环绕通知
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>正在访问方法: {}#{}(),参数 = {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        Object result = joinPoint.proceed();
        if (log.isDebugEnabled()) {

            log.debug("<<<<<<<<<<<<<<<<<<<<<<<<<<<{}#{}() 结束", joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName());
        }
        return result;
    }




}
