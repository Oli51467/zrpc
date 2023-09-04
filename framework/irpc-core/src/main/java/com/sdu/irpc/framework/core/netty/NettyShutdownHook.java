package com.sdu.irpc.framework.core.netty;

import com.sdu.irpc.framework.common.entity.holder.ShutdownContextHolder;

public class NettyShutdownHook extends Thread {

    @Override
    public void run() {
        ShutdownContextHolder.BAFFLE.set(true);
    }
}
