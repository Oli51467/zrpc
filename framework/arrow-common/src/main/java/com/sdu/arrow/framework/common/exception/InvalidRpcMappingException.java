package com.sdu.arrow.framework.common.exception;

public class InvalidRpcMappingException extends RuntimeException {

    public InvalidRpcMappingException() {
        super("Invalid Rpc mapping, please check the annotation path");
    }

    public InvalidRpcMappingException(Throwable cause) {
        super(cause);
    }
}
