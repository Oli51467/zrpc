package com.sdu.irpc.framework.core.proxy;

import com.sdu.irpc.framework.common.exception.DiscoveryException;
import com.sdu.irpc.framework.common.exception.NetworkException;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.core.NettyBoostrapInitializer;
import com.sdu.irpc.framework.core.registration.Registry;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcClientInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final Class<?> targetInterface;

    public RpcClientInvocationHandler(Registry registry, Class<?> targetInterface) {
        this.registry = registry;
        this.targetInterface = targetInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("method -> {}", method.getName());
        log.info("args -> {}", args);

        // 寻找该服务的可用节点
        List<InetSocketAddress> socketAddress = registry.discover(targetInterface.getName());
        log.info("发现服务【{}】的提供者: {}", targetInterface.getName(), socketAddress.get(0));
        Channel channel = getChannel(socketAddress.get(0));
        if (null == channel) {
            log.error("获取或建立与【{}】的通道时发生了异常。", socketAddress.get(0));
            throw new NetworkException("获取通道时发生了异常。");
        }
        // 异步发送报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        // 将该任务挂起
        IRpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);
        // 写出的报文会执行OutHandler处理
        channel.writeAndFlush(Unpooled.copiedBuffer("djndjn".getBytes())).addListener((ChannelFutureListener) promise -> {
            if (!promise.isSuccess()) {
                log.error("Error");
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

        return channel;
    }
}
