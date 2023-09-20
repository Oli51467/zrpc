package com.sdu.arrow.framework.common.entity.holder;

import com.sdu.arrow.framework.common.entity.dto.RequestInfo;

public class UserContextHolder {

    private static final ThreadLocal<RequestInfo> threadLocal = new ThreadLocal<>();

    public static void set(RequestInfo requestInfo) {
        threadLocal.set(requestInfo);
    }

    public static RequestInfo get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
