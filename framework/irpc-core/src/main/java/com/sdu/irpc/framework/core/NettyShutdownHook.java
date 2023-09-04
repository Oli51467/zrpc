package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.common.entity.ShutdownHolder;

public class NettyShutdownHook extends Thread {

    @Override
    public void run() {
        ShutdownHolder.BAFFLE.set(true);
    }
}
