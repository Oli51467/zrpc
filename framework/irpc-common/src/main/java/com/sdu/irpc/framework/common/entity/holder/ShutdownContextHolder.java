package com.sdu.irpc.framework.common.entity.holder;

import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownContextHolder {

    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);
}
