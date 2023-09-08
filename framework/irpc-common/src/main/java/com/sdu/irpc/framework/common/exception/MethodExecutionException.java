package com.sdu.irpc.framework.common.exception;

public class MethodExecutionException extends RuntimeException {

    public MethodExecutionException() {

    }

    public MethodExecutionException(String message) {
        super(message);
    }

    public MethodExecutionException(Throwable cause) {
        super(cause);
    }
}
