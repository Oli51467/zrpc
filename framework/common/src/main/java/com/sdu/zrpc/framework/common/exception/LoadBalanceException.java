package com.sdu.zrpc.framework.common.exception;

public class LoadBalanceException extends RuntimeException {

    public LoadBalanceException(String message) {
        super(message);
    }

    public LoadBalanceException() {
    }
}
