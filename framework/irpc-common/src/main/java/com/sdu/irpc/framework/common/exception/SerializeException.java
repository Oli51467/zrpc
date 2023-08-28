package com.sdu.irpc.framework.common.exception;

public class SerializeException extends RuntimeException {

    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
