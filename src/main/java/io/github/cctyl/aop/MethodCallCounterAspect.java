package io.github.cctyl.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Profile("dev")
public class MethodCallCounterAspect {

    private final Map<String, Integer> methodCallCountMap = new ConcurrentHashMap<>();

    @Pointcut("execution(* io.github.cctyl.service.impl.BlackRuleService.*(..)) || " +
            "execution(* io.github.cctyl.service.impl.ConfigServiceImpl.*(..)) || " +
            "execution(* io.github.cctyl.service.impl.CookieHeaderDataServiceImpl.*(..)) || " +
            "execution(* io.github.cctyl.service.impl.DictServiceImpl.*(..)) || " +
            "execution(* io.github.cctyl.service.impl.WhiteListRuleServiceImpl.*(..))")
    public void serviceMethods() {}

    @AfterReturning("serviceMethods()")
    public void countMethodCalls(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        methodCallCountMap.put(methodName, methodCallCountMap.getOrDefault(methodName, 0) + 1);
    }

    public Map<String, Integer> getMethodCallCountMap() {
        return methodCallCountMap;
    }
}
