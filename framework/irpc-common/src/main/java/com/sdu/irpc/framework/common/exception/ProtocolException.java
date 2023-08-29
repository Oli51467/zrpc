package com.sdu.irpc.framework.common.exception;

public class ProtocolException extends RuntimeException {

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }
}
