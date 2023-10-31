package com.sdu.arrow.middleware.ratelimiter.guava;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Builder
@Data
public class GuavaRateLimiterParam {

    /**
     * 返回的RateLimiter的速率，意味着每秒有多少个许可变成有效。
     */
    private int permitsPerSecond;

    /**
     * 在这段时间内RateLimiter会增加它的速率，在抵达它的稳定速率或者最大速率之前
     */
    private int warmupPeriod;

    /**
     * 参数warmupPeriod 的时间单位
     */
    private TimeUnit timeUnit;
}
