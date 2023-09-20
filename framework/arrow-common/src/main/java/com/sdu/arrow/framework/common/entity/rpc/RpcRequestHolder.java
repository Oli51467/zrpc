package com.sdu.arrow.framework.common.entity.rpc;

public class RpcRequestHolder {

    private static final ThreadLocal<RpcRequest> requestThreadLocal = new ThreadLocal<>();

    public static void set(RpcRequest request) {
        requestThreadLocal.set(request);
    }

    public static RpcRequest get() {
        return requestThreadLocal.get();
    }

    public static void remove() {
        requestThreadLocal.remove();
    }
}
