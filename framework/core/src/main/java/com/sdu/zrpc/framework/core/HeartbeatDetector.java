package com.sdu.zrpc.framework.core;

import com.sdu.zrpc.framework.common.entity.rpc.RpcRequest;
import com.sdu.zrpc.framework.common.enums.RequestType;
import com.sdu.zrpc.framework.common.exception.NetworkException;
import com.sdu.zrpc.framework.core.config.RpcBootstrap;
import com.sdu.zrpc.framework.core.netty.NettyBoostrapInitializer;
import com.sdu.zrpc.framework.core.registry.Registry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartbeatDetector {

    // 定义一个定时任务，每隔3秒去和每一个连接探活并维护每个连接的响应时间
    private final Thread thread = new Thread(() -> new Timer().scheduleAtFixedRate(new HeartbeatTimerTask(), 0, 3000));

    public HeartbeatDetector(String appName, String path) {
        // 获取注册中心
        Registry registry = RpcBootstrap.getInstance().getRegistry();
        // 通过注册中心发现所有服务列表(InetSocketAddress)
        List<InetSocketAddress> serviceList = registry.discover(appName, path);
        // 将每个连接维护在缓存中，如果没有连接，则建立连接
        for (InetSocketAddress socketAddress : serviceList) {
            if (!RpcBootstrap.CHANNEL_CACHE.containsKey(socketAddress)) {
                try {
                    Channel channel = NettyBoostrapInitializer.getBootstrap().connect(socketAddress).sync().channel();
                    RpcBootstrap.CHANNEL_CACHE.put(socketAddress, channel);
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
            RpcBootstrap.RESPONSE_TIME_CACHE.clear();
            Map<InetSocketAddress, Channel> cache = RpcBootstrap.CHANNEL_CACHE;

            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                int retryTimes = 3;
                while (retryTimes > 0) {
                    Channel channel = entry.getValue();
                    long startTime = System.currentTimeMillis();
                    // 构建一个心跳请求
                    RpcRequest request = RpcRequest.builder()
                            .requestId(RpcBootstrap.getInstance().getIdGenerator().getId())
                            .compressionType(RpcBootstrap.getInstance().getCompressor())
                            .requestType(RequestType.HEART_BEAT.getCode())
                            .serializationType(RpcBootstrap.getInstance().getSerializer())
                            .timeStamp(startTime)
                            .build();
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    RpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
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
                        RpcBootstrap.RESPONSE_TIME_CACHE.put(endTime - startTime, channel);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        // 一旦发生问题，需要优先重试
                        retryTimes--;
                        log.error("和地址为【{}】的主机连接发生异常.正在进行第【{}】次重试......",
                                channel.remoteAddress(), 3 - retryTimes);
                        // 将重试的机会用尽，将失效的地址移出服务列表
                        if (retryTimes == 0) {
                            RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        // 尝试等到一段时间后重试
                        try {
                            Thread.sleep(10 * (new Random().nextInt(50)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    log.info("和[{}]服务器的响应时间是[{}].", entry.getKey(), endTime - startTime);
                    break;
                }
            }
        }
    }
}
