package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.core.config.Configuration;
import com.sdu.irpc.framework.core.config.ReferenceConfig;
import com.sdu.irpc.framework.core.config.RegistryConfig;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import com.sdu.irpc.framework.core.handler.inbound.HttpHeadersHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class IRpcBootstrap {

    private static final IRpcBootstrap iRpcBootstrap = new IRpcBootstrap();

    // 维护已经发布且暴露的服务列表 (k, v) -> (接口的全限定名, ServiceConfig)
    public static final Map<String, ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(8);
    // 连接的缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(8);
    // 对外挂起的completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(64);

    private final Configuration configuration;

    private IRpcBootstrap() {
        configuration = new Configuration();
    }

    public static IRpcBootstrap getInstance() {
        return iRpcBootstrap;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 用来定义当前应用的名字
     * 服务提供者或客户端都可以使用
     *
     * @param applicationName 应用的名字
     * @return this当前实例
     */
    public IRpcBootstrap application(String applicationName) {
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 用来配置一个注册中心
     * 服务提供者或客户端都可以使用
     *
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public IRpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * 配置服务使用的序列化协议
     * 服务提供者或客户端都可以使用
     *
     * @param serializeType 协议的封装
     * @return this当前实例
     */
    public IRpcBootstrap serialize(String serializeType) {
        configuration.setSerialization(serializeType);
        log.info("当前工程使用了：{}协议进行序列化", serializeType);
        return this;
    }

    /**
     * 配置服务使用的压缩方式
     * 服务提供者或客户端都可以使用
     *
     * @param compression 传输压缩方式
     * @return this当前实例
     */
    public IRpcBootstrap compression(String compression) {
        configuration.setCompression(compression);
        log.info("当前工程使用了：{}进行压缩", compression);
        return this;
    }

    /**
     * 配置服务使用组名
     * 服务提供者或客户端都可以使用
     *
     * @param groupName 组名
     * @return this当前实例
     */
    public IRpcBootstrap group(String groupName) {
        configuration.setGroupName(groupName);
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        EventLoopGroup masterGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);
        try {
            // 服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3、配置服务器
            serverBootstrap = serverBootstrap.group(masterGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                    .option(ChannelOption.SO_REUSEADDR, true) // 参数表示允许重复使用本地地址和端口
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 保活开关2h没有数据服务端会发送心跳包
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpObjectAggregator(8192))
                                    .addLast(new HttpHeadersHandler())
                                    .addLast(new SimpleChannelInboundHandler<>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
                                            ByteBuf byteBuf = (ByteBuf) msg;
                                            log.info("收到数据: {}", byteBuf.toString(StandardCharsets.UTF_8));
                                            channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("back".getBytes()));
                                        }
                                    })
                            ;
                        }
                    });
            // 4、绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Future<?> future = masterGroup.shutdownGracefully();
            Future<?> future1 = workerGroup.shutdownGracefully();
            future.syncUninterruptibly();
            future1.syncUninterruptibly();
            log.info("Tcp server shutdown gracefully");
        }
    }

    /**
     * 服务提供方发布服务，将接口的实现注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this当前实例
     */
    public IRpcBootstrap publish(ServiceConfig<?> service) {
        configuration.getRegistryConfig().getRegistry().register(service);
        // 维护该接口
        SERVICE_MAP.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 服务提供方批量发布
     *
     * @param services 封装的需要发布的服务集合
     * @return this当前实例
     */
    public IRpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    public IRpcBootstrap reference(ReferenceConfig<?> reference) {
        // 将这个代理设置注册中心
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;

    }
}
