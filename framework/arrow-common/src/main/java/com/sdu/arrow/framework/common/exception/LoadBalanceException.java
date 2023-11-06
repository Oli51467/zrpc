package com.sdu.arrow.framework.common.exception;

public class LoadBalanceException extends RuntimeException {

    public LoadBalanceException(String message) {
        super(message);
    }

    public LoadBalanceException() {
    }
}
