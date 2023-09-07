### 负载均衡算法

为了支持多种负载均衡策略，抽象出了负载均衡器的抽象概念，形成一个接口Loadbalancer
```java
public interface LoadBalancer {

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
public abstract class AbstractLoadBalancer implements LoadBalancer {

    // 一个服务路径会匹配一个selector
    private Map<String, Selector> selectorCache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectService(String appName, String path) {
        // 优先从cache中获取一个选择器
        Selector selector = selectorCache.get(path);
        // 如果没有，就需要为这个service创建一个selector
        if (null == selector) {
            // 注册中心服务发现所有可用的节点
            List<InetSocketAddress> serviceList = IRpcBootstrap.getInstance().getRegistry().discover(appName, path);
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
        if (null == serviceList || serviceList.size() == 0) {
            log.error("进行负载均衡选取节点时发现服务列表为空.");
            throw new LoadBalancerException();
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
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {
    
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList,128);
    }
    
    /**
     * 一致性hash的具体算法实现
     */
    private static class ConsistentHashSelector implements Selector{
        
        // hash环用来存储服务器节点
        private SortedMap<Integer,InetSocketAddress> circle= new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;
    
        public ConsistentHashSelector(List<InetSocketAddress> serviceList,int virtualNodes) {
            // 我们应该尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                // 需要把每一个节点加入到hash环中
                addNodeToCircle(inetSocketAddress);
            }
        }
        
        @Override
        public InetSocketAddress getNext() {
            // 1、hash环已经建立好了，接下来需要对请求的要素做处理我们应该选择什么要素来进行hash运算
            // 有没有办法可以获取，到具体的请求内容  --> threadLocal
            YrpcRequest yrpcRequest = YrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            
            // 我们想根据请求的一些特征来选择服务器  id
            String requestId = Long.toString(yrpcRequest.getRequestId());
            
            // 请求的id做hash，字符串默认的hash不太好
            int hash = hash(requestId);
            
            // 判断该hash值是否能直接落在一个服务器上，和服务器的hash一样
            if( !circle.containsKey(hash)){
                // 寻找离我最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            
            return circle.get(hash);
        }
    
        /**
         * 将每个节点挂载到hash环上
         * @param inetSocketAddress 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.put(hash,inetSocketAddress);
                if(log.isDebugEnabled()){
                    log.debug("hash为[{}]的节点已经挂载到了哈希环上.",hash);
                }
            }
        }
    
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 挂载到hash环上
                circle.remove(hash);
            }
        }
    
        /**
         * 具体的hash算法
         * @param s hashString
         * @return hash
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // md5得到的结果是一个字节数组，但是我们想要int 4个字节

            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if( digest[i] < 0 ){
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }
    
        private String toBinary(int i){
            String s = Integer.toBinaryString(i);
            int index = 32 - s.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < index; j++) {
                sb.append(0);
            }
            sb.append(s);
            return sb.toString();
        }
    }
}
```