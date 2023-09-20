package com.sdu.arrow.framework.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一的公共响应体
 */
@Data
@AllArgsConstructor
public class ResponseResult implements Serializable {
    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 数据
     */
    private Object data;

    public static ResponseResult ok() {
        return new ResponseResult(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), null);
    }

    public static ResponseResult ok(Object o) {
        return new ResponseResult(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMsg(), o);
    }

    public static ResponseResult fail() {
        return new ResponseResult(ResponseCode.FAIL.getCode(), ResponseCode.FAIL.getMsg(), "");
    }

    public static ResponseResult fail(Object errorMessage) {
        return new ResponseResult(ResponseCode.FAIL.getCode(), ResponseCode.FAIL.getMsg(), errorMessage);
    }
}
