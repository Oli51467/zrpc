package com.sdu.arrow.middleware.ratelimiter.guava.aop;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.RateLimiter;
import com.sdu.arrow.middleware.ratelimiter.guava.GuavaExecuteRateLimiterFactory;
import com.sdu.arrow.middleware.ratelimiter.guava.GuavaRateLimiterParam;
import com.sdu.arrow.middleware.ratelimiter.guava.annotation.GuavaExecuteRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class GuavaExecuteRateLimiterAspect {


    @Autowired
    GuavaExecuteRateLimiterFactory executeRateLimiterFactory;
    ConcurrentHashMap<String, RateLimiter> rateLimiterConcurrentHashMap = new ConcurrentHashMap<>();
    Joiner joiner = Joiner.on("-").skipNulls();

    @Pointcut("@annotation(com.sdu.arrow.middleware.ratelimiter.guava.annotation.GuavaExecuteRateLimiter)")
    public void methodPointCut() {
    }

    @Around("methodPointCut()")
    public Object doMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        GuavaExecuteRateLimiter guavaExecuteRateLimiter = method.getAnnotation(GuavaExecuteRateLimiter.class);
        GuavaRateLimiterParam guavaRateLimiterParam = GuavaRateLimiterParam.builder().
                permitsPerSecond(guavaExecuteRateLimiter.permitsPerSecond()).
                timeUnit(guavaExecuteRateLimiter.timeUnit()).
                warmupPeriod(guavaExecuteRateLimiter.warmupPeriod()).build();
        String key = joiner.join(guavaExecuteRateLimiter.permitsPerSecond(),
                guavaExecuteRateLimiter.timeUnit().toString()
                , guavaExecuteRateLimiter.warmupPeriod());
        RateLimiter rateLimiter = rateLimiterConcurrentHashMap.
                computeIfAbsent(key, param -> executeRateLimiterFactory.create(guavaRateLimiterParam));
        try {
            double rateValue = rateLimiter.acquire();
            log.info("执行限流方法操作处理:当前qps:{} delay rate limiter value:{}", guavaExecuteRateLimiter.permitsPerSecond(), rateValue);
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        } catch (Throwable e) {
            log.error("执行限流控制方法失败！", e);
            return null;
        }
    }

}
