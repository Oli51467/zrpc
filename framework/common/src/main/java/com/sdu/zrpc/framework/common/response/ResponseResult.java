package com.sdu.zrpc.framework.common.response;

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

    public static ResponseResult invokeError(Long requestId, Byte code) {
        String message = String.format("当前id为【%s】的请求，返回错误的结果，响应码【%s】.", requestId.toString(), code.toString());
        return new ResponseResult(ResponseCode.FAIL.getCode(), ResponseCode.FAIL.getMsg(), message);
    }

    public static ResponseResult invokeRejected(Long requestId, Byte code) {
        String message = String.format("当前id为【%s】的请求访问被拒绝，目标服务器正处于关闭中，响应码【%s】.", requestId.toString(), code.toString());
        return new ResponseResult(ResponseCode.FAIL.getCode(), ResponseCode.FAIL.getMsg(), message);
    }

    public static ResponseResult invokeLimited(Long requestId, Byte code) {
        String message = String.format("当前id为【%s】的请求被限流，响应码【%s】.", requestId.toString(), code.toString());
        return new ResponseResult(ResponseCode.FAIL.getCode(), ResponseCode.FAIL.getMsg(), message);
    }

    public static ResponseResult invokeResourceNotFound(Long requestId, Byte code) {
        String message = String.format("当前id为【%s】的请求未找到目标资源，响应码【%s】.", requestId.toString(), code.toString());
        return new ResponseResult(ResponseCode.FAIL.getCode(), ResponseCode.FAIL.getMsg(), message);
    }
}
