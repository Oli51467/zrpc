package com.sdu.arrow.middleware.ratelimiter.guava;

import com.google.common.util.concurrent.RateLimiter;
import com.sdu.arrow.middleware.ratelimiter.core.ExecuteRateLimiterFactory;
import org.springframework.stereotype.Component;

@Component
public class GuavaExecuteRateLimiterFactory implements ExecuteRateLimiterFactory<GuavaRateLimiterParam, RateLimiter> {

    /**
     * 创建RateLimiter对象
     */
    @Override
    public RateLimiter create(GuavaRateLimiterParam param) {
        return RateLimiter.create(param.getPermitsPerSecond(), param.getWarmupPeriod(), param.getTimeUnit());
    }
}
