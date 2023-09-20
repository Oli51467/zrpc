package com.sdu.arrow.framework.core.netty;

import com.sdu.arrow.framework.common.entity.holder.ShutdownContextHolder;

public class NettyShutdownHook extends Thread {

    @Override
    public void run() {
        ShutdownContextHolder.BAFFLE.set(true);
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (ShutdownContextHolder.REQUEST_COUNTER.sum() == 0L || System.currentTimeMillis() - start > 10000) {
                break;
            }
        }
    }
}
