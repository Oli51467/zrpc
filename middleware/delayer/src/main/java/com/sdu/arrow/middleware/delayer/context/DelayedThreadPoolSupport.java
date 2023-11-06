package com.sdu.arrow.middleware.delayer.context;

import lombok.Getter;

import java.util.concurrent.Executor;


public class DelayedThreadPoolSupport {

    /**
     * 任务执行线程机制
     */
    @Getter
    private static Executor taskExecuteThread;

    /**
     * 任务轮询线程机制
     */
    @Getter
    private static Executor taskRecycleThread;


    /**
     * 操作处理机制
     */
    public DelayedThreadPoolSupport(Executor taskExecuteThread, Executor taskRecycleThread) {
        DelayedThreadPoolSupport.taskExecuteThread = taskExecuteThread;
        DelayedThreadPoolSupport.taskRecycleThread = taskRecycleThread;
    }
}
