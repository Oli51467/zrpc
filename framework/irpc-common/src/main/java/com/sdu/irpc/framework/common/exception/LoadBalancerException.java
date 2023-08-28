package com.sdu.irpc.framework.common.exception;

public class LoadBalancerException extends RuntimeException {

    public LoadBalancerException(String message) {
        super(message);
    }

    public LoadBalancerException() {
    }
}
