### 通过工厂配置注册中心

IRpcBootstrap是单例模式，所有的配置在Configuration中定义了默认的配置

``` Java
private Integer port = DEFAULT_PORT;
private String applicationName = DEFAULT_APPLICATION_NAME;
private String groupName = DEFAULT_GROUP_NAME;
private String serialization = DEFAULT_SERIALIZATION;
private String compression = DEFAULT_COMPRESSION;
private RegistryConfig registryConfig = new RegistryConfig(DEFAULT_REGISTRY_CONFIG);
```

抽象注册中心：

```java
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取服务列表
     * @param serviceName 服务的名称
     * @return 服务的地址
     */
    List<InetSocketAddress> discover(String serviceName);
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
    public void register(ServiceConfig<?> service) {
        // 服务名称的节点 最后一层是方法的全类名
        String parentNode = getProviderNodePath(service.getInterface().getName());
        // 创建父节点
        if (!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZooKeeperNode node = new ZooKeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        }
        // 创建临时本机节点
        String finalNodePath = getPath(parentNode, NetUtil.getIp(IRpcBootstrap.getInstance().getConfiguration().getPort()));
        if (!ZookeeperUtil.exists(zooKeeper, finalNodePath, null)) {
            ZooKeeperNode node = new ZooKeeperNode(finalNodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.EPHEMERAL);
        }
        log.info("服务{}注册成功", service.getInterface().getName());
    }

    /**
     * 从注册中心拉取合适的服务列表
     * @param serviceName 服务名称
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> discover(String serviceName) {
        // 找到服务对应的节点
        String serviceNode = getProviderNodePath(serviceName);
        // 从zk中获取他的子节点
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNode, null);
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