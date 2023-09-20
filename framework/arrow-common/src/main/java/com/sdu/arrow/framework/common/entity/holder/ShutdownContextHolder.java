package com.sdu.arrow.framework.common.entity.holder;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

public class ShutdownContextHolder {

    // 用来标记请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    // 用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
