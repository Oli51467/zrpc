package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.common.entity.rpc.RpcRequest;
import com.sdu.irpc.framework.common.enums.RequestType;
import com.sdu.irpc.framework.common.exception.NetworkException;
import com.sdu.irpc.framework.core.compressor.CompressorFactory;
import com.sdu.irpc.framework.core.registry.Registry;
import com.sdu.irpc.framework.core.serializer.SerializerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartbeatDetector {

    // 定义一个定时任务，每隔3秒去和每一个连接探活并维护每个连接的响应时间
    private final Thread thread = new Thread(() -> new Timer().scheduleAtFixedRate(new HeartbeatTimerTask(), 0, 3000));

    public HeartbeatDetector(String serviceName) {
        // 获取注册中心
        Registry registry = IRpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        // 通过注册中心发现所有服务列表(InetSocketAddress)
        List<InetSocketAddress> serviceList = registry.discover(serviceName);
        // 将每个连接维护在缓存中，如果没有连接，则建立连接
        for (InetSocketAddress socketAddress : serviceList) {
            if (!IRpcBootstrap.CHANNEL_CACHE.containsKey(socketAddress)) {
                try {
                    Channel channel = NettyBoostrapInitializer.getBootstrap().connect(socketAddress).sync().channel();
                    IRpcBootstrap.CHANNEL_CACHE.put(socketAddress, channel);
                } catch (InterruptedException e) {
                    throw new NetworkException("获取通道连接时发生了异常。");
                }
            }
        }
        // 设置线程为守护线程
        thread.setDaemon(true);
    }

    public void startDetect() {
        thread.start();
    }

    private static class HeartbeatTimerTask extends TimerTask {
        @Override
        public void run() {
            // 1.先将响应时间缓存清空
            IRpcBootstrap.RESPONSE_TIME_CACHE.clear();
            Map<InetSocketAddress, Channel> cache = IRpcBootstrap.CHANNEL_CACHE;

            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                Channel channel = entry.getValue();
                long startTime = System.currentTimeMillis();
                // 构建一个心跳请求
                RpcRequest request = RpcRequest.builder()
                        .requestId(IRpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                        .compressionType(CompressorFactory.getCompressor(IRpcBootstrap.getInstance().getConfiguration().getCompressionType()).getCode())
                        .requestType(RequestType.HEART_BEAT.getCode())
                        .serializationType(SerializerFactory.getSerializer(IRpcBootstrap.getInstance().getConfiguration().getSerializationType()).getCode())
                        .timeStamp(startTime)
                        .build();
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                IRpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
                // 发送心跳包信息
                channel.writeAndFlush(request).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });

                long endTime;
                try {
                    completableFuture.get(2, TimeUnit.SECONDS);
                    endTime = System.currentTimeMillis();
                    // 使用treemap缓存响应时间
                    IRpcBootstrap.RESPONSE_TIME_CACHE.put(endTime - startTime, channel);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                log.info("和[{}]服务器的响应时间是[{}].", entry.getKey(), endTime - startTime);
            }
        }
    }
}
