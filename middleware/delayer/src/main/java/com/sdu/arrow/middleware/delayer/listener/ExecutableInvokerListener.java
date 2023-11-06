package com.sdu.arrow.middleware.delayer.listener;

@FunctionalInterface
public interface ExecutableInvokerListener<P> {

    /**
     * 执行方法
     */
    void handle(P param);
}
