### 实现动态上下线
当有服务提供方动态上下线，我们如何进行感知呢？服务上线，首先会在注册中心进行注册，调用方是无法实时感知的，合理的方式只有两种：

1. 调用方定时的去主动的拉。

2. 注册中心主动的推送。

在我们当前的项目中zookeeper提供了watcher机制，我们正好可以利用他来实现动态上下线，具体步骤如下：

1. 调用方拉取服务列表时，注册一个watcher关注该服务节点的变化。

2. 当服务提供方上线或线下时会触发watcher机制（节点发生了变化）。

3. 通知调用方，执行动态上下线的操作。

服务上线，下线均可以依赖watcher机制，但是对于下线而言也可以通过心跳探活来实现。

一旦节点发生了变化，UpAndDownWatcher就会被触发，会触发reloadBalance（重新进行负载均衡），代码如下：
```java
@Slf4j
public class UpAndDownWatcher implements Watcher {

    @SneakyThrows
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            log.info("检测到服务【{}】下有节点变化", event.getPath());
            Thread.sleep(500);
            String[] pathArgs = event.getPath().split("/");
            String pathName = pathArgs[pathArgs.length - 1];
            String appName = pathArgs[pathArgs.length - 2];
            Registry registry = RpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addressList = registry.discover(appName, pathName);
            // 处理新增的节点
            for (InetSocketAddress address : addressList) {
                if (!RpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel;
                    try {
                        channel = NettyBoostrapInitializer.getBootstrap().connect(address).sync().channel();
                        RpcBootstrap.CHANNEL_CACHE.put(address, channel);
                    } catch (InterruptedException e) {
                        throw new NetworkException("获取通道连接时发生了异常。");
                    }
                }
            }
            // 处理下线的节点
            for (Map.Entry<InetSocketAddress, Channel> entry : RpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if (!addressList.contains(entry.getKey())) {
                    RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }
            // 重新负载均衡
            RpcBootstrap.getInstance().getLoadBalancer().reload(pathName, addressList);
        }
    }
}
```

### 实现包扫描发布服务

扫描包，进行批量发布的思路和逻辑也很简单，具体步骤如下：

1. 将包名转化为文件夹

2. 遍历文件夹下的所有class文件。

3. 获取class文件的完整路径，转化为全限定名。

4. 通过反射进行加载，封装成ServiceConfig对象，调用发布接口进行发布即可。

代码如下：
``` java
public RpcBootstrap scanServices(String packageName) {
    // 1、需要通过packageName获取其下的所有的类的权限定名称
    List<String> classNames = FileUtil.getAllClassNames(packageName);
    List<Class<?>> classes = FileUtil.filterClassWithServiceAnnotation(classNames);
    List<ServiceConfig> serviceConfigList = FileUtil.createServiceConfigWithClasses(classes);
    return publish(serviceConfigList);
}
```