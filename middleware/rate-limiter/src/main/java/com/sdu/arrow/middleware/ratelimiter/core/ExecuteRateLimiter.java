package com.sdu.arrow.middleware.ratelimiter.core;

@FunctionalInterface
public interface ExecuteRateLimiter<P, R> {

    /**
     * 执行操作
     *
     * @param param P
     * @return R
     */
    R execute(P param);
}
