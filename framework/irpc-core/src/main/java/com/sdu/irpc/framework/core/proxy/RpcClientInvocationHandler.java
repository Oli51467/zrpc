package com.sdu.irpc.framework.core.proxy;

import com.sdu.irpc.framework.common.enums.RequestType;
import com.sdu.irpc.framework.common.exception.DiscoveryException;
import com.sdu.irpc.framework.common.exception.NetworkException;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.core.NettyBoostrapInitializer;
import com.sdu.irpc.framework.core.compressor.CompressorFactory;
import com.sdu.irpc.framework.core.registry.Registry;
import com.sdu.irpc.framework.core.serializer.SerializerFactory;
import com.sdu.irpc.framework.core.transport.RequestPayload;
import com.sdu.irpc.framework.core.transport.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcClientInvocationHandler implements InvocationHandler {

    private final Class<?> targetInterface;

    public RpcClientInvocationHandler(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1.封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(targetInterface.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();
        // 2.创建一个请求
        RpcRequest request = RpcRequest.builder()
                .requestId(1L)
                .compressionType(CompressorFactory.getCompressor(IRpcBootstrap.getInstance().getConfiguration().getCompressionType()).getCode())
                .requestType(RequestType.REQUEST.getCode())
                .serializationType(SerializerFactory.getSerializer(IRpcBootstrap.getInstance().getConfiguration().getSerializationType()).getCode())
                .timeStamp(System.currentTimeMillis())
                .requestPayload(requestPayload)
                .build();

        // 3.寻找该服务的可用节点，通过客户端负载均衡寻找一个可用的服务
        InetSocketAddress address = IRpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectService(targetInterface.getName());
        log.info("发现服务【{}】的提供者: {}", targetInterface.getName(), address);
        Channel channel = getChannel(address);
        // 4.异步发送报文 并将该任务挂起
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        IRpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) promise -> {
            if (!promise.isSuccess()) {
                log.error("远程调用失败");
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        return completableFuture.get(3, TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取一个通道
     *
     * @param address 地址
     * @return io.netty.channel.Channel
     */
    private Channel getChannel(InetSocketAddress address) {
        Channel channel = IRpcBootstrap.CHANNEL_CACHE.get(address);
        if (null == channel) {
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBoostrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if (promise.isDone()) {
                    log.info("客户端连接成功");
                    channelFuture.complete(promise.channel());
                } else if (!promise.isSuccess()) {
                    channelFuture.completeExceptionally(promise.cause());
                }
            });

            // 阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道连接时发生异常:", e);
                throw new DiscoveryException(e);
            }
            IRpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (null == channel) {
            log.error("获取或建立与【{}】的通道时发生了异常。", address);
            throw new NetworkException("获取通道时发生了异常。");
        }

        return channel;
    }
}
