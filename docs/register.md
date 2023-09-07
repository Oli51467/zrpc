### 通过工厂配置注册中心

IRpcBootstrap是单例模式，所有的配置在Configuration中定义了默认的配置

抽象注册中心：

```java
public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig serviceConfig);

    /**
     * 从注册中心拉取服务列表
     * @param path 服务的名称
     * @return 服务的地址
     */
    List<InetSocketAddress> discover(String appName, String path);
}

public abstract class AbstractRegistry implements Registry {
}
```

实现具体的注册中心 以ZooKeeper为例

```java
@Slf4j
public class ZooKeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZooKeeperRegistry() {
        this.zooKeeper = ZookeeperUtil.createZookeeperConnection();
    }

    public ZooKeeperRegistry(String connectionAddr, int timeout) {
        this.zooKeeper = ZookeeperUtil.createZookeeperConnection(connectionAddr, timeout);
    }

    @Override
    public void register(ServiceConfig service) {
        // 服务名称的节点 最后一层是方法的全类名
        // 路径是 /providers/appName/methodName/ip1, ip2,...
        String applicationNode = getProviderNodePath(service.getApplicationName());
        // 创建父节点(应用节点)
        if (!ZookeeperUtil.exists(zooKeeper, applicationNode, null)) {
            ZooKeeperNode node = new ZooKeeperNode(applicationNode, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
            log.info("应用节点创建");
        }
        String pathNode = getPath(applicationNode, service.getPath());
        // 创建父节点(路径节点)
        if (!ZookeeperUtil.exists(zooKeeper, pathNode, null)) {
            ZooKeeperNode node = new ZooKeeperNode(pathNode, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
            log.info("方法节点创建");
        }
        // 创建临时本机节点(ip节点)
        String finalNodePath = getPath(pathNode, NetUtil.getIp(IRpcBootstrap.getInstance().getConfiguration().getPort()));
        if (!ZookeeperUtil.exists(zooKeeper, finalNodePath, null)) {
            ZooKeeperNode node = new ZooKeeperNode(finalNodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.EPHEMERAL);
            log.info("ip节点创建");
        }
        log.info("服务{}注册成功", service.getPath());
    }

    /**
     * 从注册中心拉取合适的服务列表
     * @param path 服务名称
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> discover(String appName, String path) {
        // 找到服务对应的节点
        String serviceNode = getProviderNodePath(appName, path);
        // 从zk中获取他的子节点
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        // 获取了所有的可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(address -> {
            String[] ipAndPort = address.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();

        if (inetSocketAddresses.size() == 0) {
            throw new DiscoveryException("未发现可用的服务主机");
        }
        return inetSocketAddresses;
    }
}
```

服务端发布服务，调用```register()```方法

``` javascript
public IRpcBootstrap publish(ServiceConfig<?> service) {
    configuration.getRegistryConfig().getRegistry().register(service);
    return this;
}
```

通过简单工厂获得注册中心：

``` javascript
public Registry getRegistry() {
    // 获取注册中心的类型
    String registryType = getRegistryType(connectionName,true).toLowerCase().trim();
    if (RegistryType.ZOOKEEPER.getName().equals(registryType)) {
        String host=getRegistryType(connectionName,false);
        return new ZooKeeperRegistry(host,ZooKeeperConstant.TIME_OUT);
    }
    throw new DiscoveryException("未发现注册中心");
}
```