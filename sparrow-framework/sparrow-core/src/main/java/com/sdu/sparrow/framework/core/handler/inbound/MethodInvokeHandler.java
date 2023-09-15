package com.sdu.sparrow.framework.core.handler.inbound;

import com.sdu.sparrow.framework.common.entity.holder.ShutdownContextHolder;
import com.sdu.sparrow.framework.common.entity.rpc.RequestPayload;
import com.sdu.sparrow.framework.common.entity.rpc.RpcRequest;
import com.sdu.sparrow.framework.common.entity.rpc.RpcResponse;
import com.sdu.sparrow.framework.common.enums.RespCode;
import com.sdu.sparrow.framework.common.exception.MethodExecutionException;
import com.sdu.sparrow.framework.common.transaction.annotation.SecureInvoke;
import com.sdu.sparrow.framework.core.config.RpcBootstrap;
import com.sdu.sparrow.framework.core.config.ServiceConfig;
import com.sdu.sparrow.framework.core.protection.Limiter;
import com.sdu.sparrow.framework.core.protection.TokenBucketRateLimiter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

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
        // 查看关闭的挡板是否打开，如果挡板已经打开，返回一个错误的响应
        if (ShutdownContextHolder.BAFFLE.get()) {
            response.setCode(RespCode.CLOSING.getCode());
            channel.writeAndFlush(response);
            return;
        }
        ShutdownContextHolder.REQUEST_COUNTER.increment();
        // 请求限流
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, Limiter> ipRateLimiter = RpcBootstrap.getInstance().getConfiguration().getIpRateLimiter();
        Limiter limiter = ipRateLimiter.get(socketAddress);
        if (null == limiter) {
            limiter = new TokenBucketRateLimiter(10, 1000);
            ipRateLimiter.put(socketAddress, limiter);
        }
        boolean allowRequest = limiter.allowRequest();
        if (!allowRequest) {
            response.setCode(RespCode.RATE_LIMIT.getCode());
        } else {
            // 拿到真正的payload
            RequestPayload requestPayload = request.getRequestPayload();
            try {
                Object result = doInvoke(requestPayload);
                log.info("请求【{}】已经在服务端完成方法调用。", request.getRequestId());
                response.setCode(RespCode.SUCCESS.getCode());
                response.setBody(result);
                log.info("服务提供方响应体: {}", response.getBody());
            } catch (Exception e) {
                log.error("Id为【{}】的请求在调用过程中发生异常。", response.getRequestId(), e);
                response.setCode(RespCode.FAIL.getCode());
            }
        }
        // 设置响应时间戳 写回响应
        response.setTimeStamp(System.currentTimeMillis());
        if (channel.isActive() && channel.isOpen()) {
            writeAndFlushWithTransaction(channel, response);
        }
        ShutdownContextHolder.REQUEST_COUNTER.decrement();
    }

    private Object doInvoke(RequestPayload requestPayload) {
        String path = requestPayload.getPath();
        String methodName = requestPayload.getMethodName();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 寻找服务的具体实现
        ServiceConfig serviceConfig = RpcBootstrap.SERVICE_MAP.get(path);
        if (null == serviceConfig) {
            throw new MethodExecutionException("调用方服务路径不存在");
        }
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = Exception.class)
    @SecureInvoke
    public void writeAndFlushWithTransaction(Channel channel, RpcResponse response) {
        channel.writeAndFlush(response);
    }
}
