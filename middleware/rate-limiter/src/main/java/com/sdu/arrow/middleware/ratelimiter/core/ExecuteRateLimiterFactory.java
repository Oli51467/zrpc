package com.sdu.arrow.middleware.ratelimiter.core;

@FunctionalInterface
public interface ExecuteRateLimiterFactory<P, R> {

    R create(P param);
}
