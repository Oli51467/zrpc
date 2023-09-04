package com.sdu.irpc.framework.common.entity;

import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownHolder {

    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);
}
