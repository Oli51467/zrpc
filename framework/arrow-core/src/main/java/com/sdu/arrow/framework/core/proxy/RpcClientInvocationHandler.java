package com.sdu.arrow.framework.core.proxy;

import com.sdu.arrow.framework.common.annotation.RpcMapping;
import com.sdu.arrow.framework.common.entity.rpc.RequestPayload;
import com.sdu.arrow.framework.common.entity.rpc.RpcRequest;
import com.sdu.arrow.framework.common.entity.rpc.RpcRequestHolder;
import com.sdu.arrow.framework.common.enums.RequestType;
import com.sdu.arrow.framework.common.exception.DiscoveryException;
import com.sdu.arrow.framework.common.exception.InvalidRpcMappingException;
import com.sdu.arrow.framework.common.exception.MethodExecutionException;
import com.sdu.arrow.framework.common.exception.NetworkException;
import com.sdu.arrow.framework.core.config.RpcBootstrap;
import com.sdu.arrow.framework.core.netty.NettyBoostrapInitializer;
import com.sdu.arrow.framework.core.protection.Breaker;
import com.sdu.arrow.framework.core.protection.CircuitBreaker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.sdu.arrow.framework.core.util.FileUtil.checkPath;
import static com.sdu.arrow.framework.core.util.FileUtil.processPath;

@Slf4j
public class RpcClientInvocationHandler implements InvocationHandler {

    private final String appName;
    private final String path;

    public RpcClientInvocationHandler(String appName, String path) {
        this.appName = appName;
        this.path = path;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        RpcMapping mappingAnnotation = method.getAnnotation(RpcMapping.class);
        if (null == mappingAnnotation) {
            log.error("远程调用缺少Path字段或缺少映射注解, method: {}", method.getName());
            throw new InvalidRpcMappingException();
        }
        String childPath = mappingAnnotation.path();
        String tempPath = new String(path);
        tempPath = tempPath.concat(childPath);
        if (!checkPath(tempPath)) {
            log.error("远程调用Path字段非法, path: {}", tempPath);
            throw new InvalidRpcMappingException();
        }
        // 对路径的特殊字符做处理
        tempPath = processPath(tempPath);
        // 1.寻找该服务的可用节点，通过客户端负载均衡寻找一个可用的服务。如果找不到会抛出一个异常
        InetSocketAddress address = RpcBootstrap.getInstance().getLoadBalanceType().selectService(appName, tempPath);
        log.info("发现服务【{}】的提供者: {}", tempPath, address);
        // 2.判断服务的熔断器是否是打开状态
        Map<SocketAddress, Breaker> ipBreaker = RpcBootstrap.getInstance().getConfiguration().getIpBreaker();
        Breaker breaker = ipBreaker.get(address);
        if (null == breaker) {
            breaker = new CircuitBreaker();
            ipBreaker.put(address, breaker);
        }
        // 3.尝试是否可以访问，如果不能访问则抛出异常
        breaker.attempt();
        // 4.尝试使用服务发现的地址获取一个Channel连接
        Channel channel = getChannel(address);
        // 5.如果所有连接都拿到了，开始封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .path(tempPath)
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();
        // 6.创建一个请求
        RpcRequest request = RpcRequest.builder()
                .requestId(RpcBootstrap.getInstance().getIdGenerator().getId())
                .compressionType(RpcBootstrap.getInstance().getCompressor())
                .requestType(RequestType.REQUEST.getCode())
                .serializationType(RpcBootstrap.getInstance().getSerializer())
                .timeStamp(System.currentTimeMillis())
                .requestPayload(requestPayload)
                .build();
        // 7.将请求存入本地线程
        RpcRequestHolder.set(request);
        try {
            // 8.异步发送报文
            CompletableFuture<Object> completableFuture = new CompletableFuture<>();
            if (channel.isActive() && channel.isOpen()) {
                // 9.将该任务挂起到挂起队列
                RpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
                channel.writeAndFlush(request).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        // 不保证发送成功
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                Object result = completableFuture.get(10, TimeUnit.SECONDS);
                breaker.recordSuccessRequest();
                RpcRequestHolder.remove();
                return result;
            } else {
                RpcRequestHolder.remove();
                throw new MethodExecutionException("服务调用方处于离线状态");
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            breaker.recordErrorRequest();
            throw new MethodExecutionException("服务调用失败");
        }
    }

    /**
     * 根据地址获取一个通道
     *
     * @param address 地址
     * @return io.netty.channel.Channel
     */
    private Channel getChannel(InetSocketAddress address) {
        // 先从尝试从缓存获取
        Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
        if (null == channel) {
            // 如果获取不到，则新建一个连接放到缓存中
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBoostrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if (promise.isDone()) {
                    log.info("客户端连接成功");
                    channelFuture.complete(promise.channel());
                } else if (!promise.isSuccess()) {
                    log.info("客户端连接失败");
                    channelFuture.completeExceptionally(promise.cause());
                }
            });

            // 阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
                if (null != channel) {
                    RpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道连接时发生异常:", e);
                throw new DiscoveryException(e);
            }
        }
        if (null == channel) {
            log.error("获取或建立与【{}】的通道时发生了异常。", address);
            throw new NetworkException("获取通道时发生了异常。");
        }

        return channel;
    }
}
