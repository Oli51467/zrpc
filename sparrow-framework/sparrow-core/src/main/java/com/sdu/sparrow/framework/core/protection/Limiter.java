package com.sdu.sparrow.framework.core.protection;

public interface Limiter {

    /**
     * 是否允许新的请求进入
     * @return true 可以进入 false 拦截
     */
    boolean allowRequest();
}
