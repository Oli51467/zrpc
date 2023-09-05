package com.sdu.irpc.framework.common.entity.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求调用方所请求的接口方法的描述
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {

    /**
     * 接口名
     */
    private String path;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型用来确定重载方法，具体的参数用来执行方法调用
     */
    private Class<?>[] parametersType;

    /**
     * 参数列表，参数分为参数类型和具体的参数
     */
    private Object[] parametersValue;

    /**
     * 返回值的封装
     */
    private Class<?> returnType;
}
