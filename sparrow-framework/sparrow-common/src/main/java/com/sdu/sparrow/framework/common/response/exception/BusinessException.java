package com.sdu.sparrow.framework.common.response.exception;

public class BusinessException extends RuntimeException {

    private ExceptionEnum e;

    public BusinessException(ExceptionEnum e) {
        this.e = e;
    }

    public ExceptionEnum getE() {
        return e;
    }

    public void setE(ExceptionEnum e) {
        this.e = e;
    }

    /**
     * 不写入堆栈信息，提高性能
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
