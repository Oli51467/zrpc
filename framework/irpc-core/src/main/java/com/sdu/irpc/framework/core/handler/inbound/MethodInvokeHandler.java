package com.sdu.irpc.framework.core.handler.inbound;

import com.sdu.irpc.framework.common.entity.holder.ShutdownContextHolder;
import com.sdu.irpc.framework.common.entity.rpc.RequestPayload;
import com.sdu.irpc.framework.common.entity.rpc.RpcRequest;
import com.sdu.irpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.irpc.framework.common.enums.RequestType;
import com.sdu.irpc.framework.common.enums.RespCode;
import com.sdu.irpc.framework.core.config.IRpcBootstrap;
import com.sdu.irpc.framework.common.entity.rpc.ServiceConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) {
        log.info("收到数据: {}", request.toString());

        // 封装响应体
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        response.setCompressionType(request.getCompressionType());
        response.setSerializationType(request.getSerializationType());
        // 获得通道
        Channel channel = channelHandlerContext.channel();
        if (ShutdownContextHolder.BAFFLE.get()) {
            response.setCode(RespCode.CLOSING.getCode());
            channel.writeAndFlush(response);
            return;
        }
        // 拿到真正的payload
        if (request.getRequestType() == RequestType.HEART_BEAT.getCode()) {
            response.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());
        } else {
            RequestPayload requestPayload = request.getRequestPayload();
            try {
                Object result = doInvoke(requestPayload);
                log.info("请求【{}】已经在服务端完成方法调用。", request.getRequestId());
                response.setCode(RespCode.SUCCESS.getCode());
                response.setBody(result);
                log.info("服务提供方响应体: {}", response);
            } catch (Exception e) {
                log.error("Id为【{}】的请求在调用过程中发生异常。", response.getRequestId(), e);
                response.setCode(RespCode.FAIL.getCode());
            }
        }
        // 设置响应时间戳 写回响应
        response.setTimeStamp(System.currentTimeMillis());
        channel.writeAndFlush(response);
    }

    private Object doInvoke(RequestPayload requestPayload) {
        String path = requestPayload.getPath();
        String methodName = requestPayload.getMethodName();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 寻找服务的具体实现
        ServiceConfig serviceConfig = IRpcBootstrap.SERVICE_MAP.get(path);
        Object referenceImpl = serviceConfig.getReference();
        Method method = serviceConfig.getMethod();
        // 获取方法对象 通过反射调用invoke方法
        Object returnValue;
        try {
            returnValue = method.invoke(referenceImpl, parametersValue);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常。", path, methodName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
