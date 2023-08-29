### 客户端如何复用并优雅连接Netty

统一连接配置，并在初始化时创建
```java
public class NettyBoostrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ClientChannelHandler());
    }

    private NettyBoostrapInitializer() {}

    public static Bootstrap getBootstrap() { return bootstrap; }
}
```

ClientChannelHandler中统一定义了连接时需要配置的Handler
```java
public class ClientChannelHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG));
    }
}
```

IRpc维护了客户端的连接
```
public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
```

#### 连接方式【1】阻塞获取连接
await方法会阻塞，等待连接成功后再返回
```
Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
```

#### 连接方式【2】Netty异步获取连接 sync
sync和await都是阻塞当前线程，异步获取返回值，连接的过程和发送数据的过程都是异步的。

区别在于如果发生了异常，sync会主动在子线程抛出异常，await工作在主线程不会抛出异常。

同步策略：
```
ChannelFuture channelFuture = NettyBoostrapInitializer.getBootstrap().connect(address).await();
if (channelFuture.isDone()) {
    Object object = channelFuture.getNow();
} else if (!channelFuture.isSuccess()) {
    Throwable cause = channelFuture.cause();
    throw new RuntimeExeception(cause);
}
```

异步策略：
```
CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
NettyBoostrapInitializer.getBootstrap().connect(address).addListener(
    (ChannelFutureListener) promise -> {
        if (promise.isDone()) {
            log.info("客户端连接成功");
            channelFuture.complete(promise.channel());
        } else if (!promise.isSuccess()) {
            channelFuture.completeExceptionally(promise.cause());
        }
    }
);

// 阻塞获取channel
try {
    channel = channelFuture.get(3, TimeUnit.SECONDS);
} catch (InterruptedException | ExecutionException | TimeoutException e) {
     log.error("获取通道连接时发生异常:", e);
     throw new DiscoveryException(e);
}
```
