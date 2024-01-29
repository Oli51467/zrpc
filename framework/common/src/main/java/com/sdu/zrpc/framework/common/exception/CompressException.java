package com.sdu.zrpc.framework.common.exception;

public class CompressException extends RuntimeException {

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
