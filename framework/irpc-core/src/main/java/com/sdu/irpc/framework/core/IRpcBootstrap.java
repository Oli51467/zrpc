package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.common.enums.CompressionType;
import com.sdu.irpc.framework.common.enums.LoadBalancerType;
import com.sdu.irpc.framework.common.enums.SerializationType;
import com.sdu.irpc.framework.core.config.Configuration;
import com.sdu.irpc.framework.core.config.RegistryConfig;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import com.sdu.irpc.framework.core.handler.inbound.HttpHeadersHandler;
import com.sdu.irpc.framework.core.handler.inbound.MethodInvokeHandler;
import com.sdu.irpc.framework.core.handler.inbound.RequestMessageDecoder;
import com.sdu.irpc.framework.core.handler.outbound.ResponseMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    // 维护每个连接的响应时间
    public static final TreeMap<Long, Channel> RESPONSE_TIME_CACHE = new TreeMap<>();

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
     * @param serializationType 协议的封装
     * @return this当前实例
     */
    public IRpcBootstrap serialize(SerializationType serializationType) {
        configuration.setSerializationType(serializationType.name());
        log.info("当前工程使用了：{}协议进行序列化", serializationType.name());
        return this;
    }

    /**
     * 配置服务使用的压缩方式
     * 服务提供者或客户端都可以使用
     *
     * @param compressionType 传输压缩方式
     * @return this当前实例
     */
    public IRpcBootstrap compress(CompressionType compressionType) {
        configuration.setCompressionType(compressionType.name());
        log.info("当前工程使用了【{}】进行压缩", compressionType.name());
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
     * 配置服务的负载均衡器
     * @param loadBalancerType 负载均衡器类型
     * @return this当前实例
     */
    public IRpcBootstrap loadbalancer(LoadBalancerType loadBalancerType) {
        configuration.setLoadBalancer(loadBalancerType.name());
        return this;
    }

    /**
     * 配置服务的端口
     *
     * @param port 端口
     * @return this当前实例
     */
    public IRpcBootstrap port(int port) {
        configuration.setPort(port);
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
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpObjectAggregator(8192))
                                    .addLast(new HttpHeadersHandler())      // 解析http头部
                                    .addLast(new RequestMessageDecoder())   // 消息解码
                                    .addLast(new MethodInvokeHandler())     // 反射调用方法
                                    .addLast(new ResponseMessageEncoder())  // 处理响应编码
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
}
