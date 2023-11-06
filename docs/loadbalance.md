### 负载均衡算法

为了支持多种负载均衡策略，抽象出了负载均衡器的抽象概念，形成一个接口LoadBalance
```java
/**
 * 负载均衡器的接口
 */
public interface LoadBalance {

    /**
     * 根据服务名获取一个可用的服务
     *
     * @param path 服务路径
     * @return 服务地址
     */
    InetSocketAddress selectService(String appName, String path);

    /**
     * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
     *
     * @param path 服务路径
     */
    void reload(String path, List<InetSocketAddress> addresses);
}
```

不同的负载均衡器只是实现的算法不同，在执行一些操作时骨架代理逻辑是一样的，使用**模板方法设计模式**，将相同的骨干逻辑封装在抽象类中：
```java
public abstract class AbstractLoadBalance implements LoadBalance {

    // 一个服务路径会匹配一个selector
    private Map<String, Selector> selectorCache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectService(String appName, String path) {
        // 优先从cache中获取一个选择器
        Selector selector = selectorCache.get(path);
        // 如果没有，就需要为这个service创建一个selector
        if (null == selector) {
            // 注册中心服务发现所有可用的节点
            List<InetSocketAddress> serviceList = RpcBootstrap.getInstance().getRegistry().discover(appName, path);
            // 具体的选择逻辑由子类实现
            selector = initSelector(serviceList);
            // 将select放入缓存当中
            selectorCache.put(path, selector);
        }
        // 执行selector的选择逻辑选择一个节点
        return selector.select();
    }

    @Override
    public synchronized void reload(String path, List<InetSocketAddress> serviceList) {
        selectorCache.put(path, initSelector(serviceList));
    }

    protected abstract Selector initSelector(List<InetSocketAddress> serviceList);
}
```

算法独立的选择器进行抽象成接口Selector：
```java
public interface Selector {
    /**
     * 根据服务列表执行一种算法获取一个服务节点
     * @return 具体的服务节点
     */
    InetSocketAddress select();
}
```
每种负载均衡算法只需要实现对应的select()选择算法即可

- 轮询算法的选择器实现：
```java
/**
 * RoundRobinSelector 轮训负载均衡选择器
 * 如果内部类不会引用到外部类，强烈建议使用静态内部类节省资源，减少内部类其中的一个指向外部类的引用。
 */
private static class RoundRobinSelector implements Selector {

    private List<InetSocketAddress> serviceList;
    private AtomicInteger index;

    public RoundRobinSelector(List<InetSocketAddress> serviceList) {
        this.serviceList = serviceList;
        this.index = new AtomicInteger(0);
    }

    @Override
    public InetSocketAddress select() {
        if (null == serviceList || serviceList.isEmpty()) {
            log.error("进行负载均衡选取节点时发现服务列表为空.");
            throw new LoadBalanceException();
        }
        InetSocketAddress address = serviceList.get(index.get());
        if (index.get() == serviceList.size() - 1) {
            index.set(0);
        } else {
            index.incrementAndGet();
        }
        return address;
    }
}
```

一致性哈希算法的选择器实现：
```java
/**
 * 一致性hash的负载均衡策略
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        try {
            return new ConsistentHashSelector(serviceList, 128);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ConsistentHashSelector implements Selector {
        private final TreeMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        private final int virtualNodesCount;
        private final MessageDigest messageDigest;

        private ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodesCount) throws NoSuchAlgorithmException {
            // 尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodesCount = virtualNodesCount;
            this.messageDigest = MessageDigest.getInstance("MD5");
            for (InetSocketAddress socketAddress : serviceList) {
                // 需要把每一个节点加入到hash环中
                addNodeToCircle(socketAddress);
            }
        }

        @Override
        public InetSocketAddress select() {
            // 从threadLocal中获取请求的id
            RpcRequest rpcRequest = RpcRequestHolder.get();
            int hash = calHash(rpcRequest.getRequestId().toString());
            // 判断该hash值是否能直接落在一个服务器上
            Map.Entry<Integer, InetSocketAddress> entry = circle.ceilingEntry(hash);
            if (entry == null) {
                entry = circle.firstEntry();
            }
            return entry.getValue();
        }

        /**
         * // 为每一个节点生成匹配的虚拟节点进行挂载
         *
         * @param socketAddress 实际节点的地址
         */
        private void addNodeToCircle(InetSocketAddress socketAddress) {
            for (int i = 0; i < this.virtualNodesCount; i++) {
                int hash = calHash(socketAddress.toString() + "-" + i);
                circle.put(hash, socketAddress);
            }
        }

        private int calHash(String key) {
            byte[] bytes = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
            return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
    }
}
```