package com.sdu.arrow.middleware.delayer.sample;

import com.sdu.arrow.middleware.delayer.annotation.DelayedQueueListener;
import com.sdu.arrow.middleware.delayer.listener.EventExecutableInvokerListener;
import com.sdu.arrow.middleware.delayer.model.ExecuteInvokerEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
@DelayedQueueListener(value = "delayedQueueListener", group = "test_delayed_queue")
public class DefaultDelayedQueueListener implements EventExecutableInvokerListener<Object, Object> {

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void handle(ExecuteInvokerEvent<Object> param) {
        log.info("input the parameter:{}", param);
    }
}
