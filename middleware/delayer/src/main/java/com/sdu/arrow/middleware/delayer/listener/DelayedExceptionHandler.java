package com.sdu.arrow.middleware.delayer.listener;

@FunctionalInterface
public interface DelayedExceptionHandler {

    /**
     * 捕获异常信息
     */
    void catchException(Throwable e, Thread currentThread);

}
