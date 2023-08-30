### 雪花算法

我们需要给请求一个唯一标识，用来标识一个请求和响应的关联关系，我们要求请求的id必须唯一，且不能占用过大的空间，可用的方案如下：
1. 自增id，单机的自增id不能解决不重复的问题，微服务情况下我们需要一个稳定的发号服务才能保证，但是这样做性能偏低。

2. uuid，将uuid作为唯一标识占用空间太大

3. 雪花算法

#### 简介
雪花算法(snowflake)最早是twitter内部使用分布式环境下的唯一ID生成算法，他使用64位long类型的数据存储id，具体如下：

> 0 - 0000000000 0000000000 0000000000 0000000000 0 - 0000000000 - 000000000000   
> 符号位  &emsp; &emsp;&emsp;&emsp;&emsp;时间戳 &emsp;&emsp;&emsp;&emsp;&emsp;机器码&emsp;&emsp;&emsp;&emsp;&emsp; 序列号


最高位表示符号位，其中0代表整数，1代表负数，而id一般都是正数，所以最高位为0。**可以私有化雪花算法**

- 41位存储毫秒级时间戳，**这个时间戳不是存储当前时间的时间戳，而是存储时间戳的差值（当前时间戳 - 开始时间戳) * 得到的值）**，这样我们可以存储一个相对更长的时间。
- 10位存储机器码，最多支持1024台机器，当并发量非常高，同时有多个请求在同一毫秒到达，可以根据机器码进行第二次生成。机器码可以根据实际需求进行二次划分，比如两个机房操作可以一个机房分配5位机器码。
- 12位存储序列号，当同一毫秒有多个请求访问到了同一台机器后，此时序列号就派上了用场，为这些请求进行第三次创建，最多每毫秒每台机器产生2的12次方也就是4096个id，满足了大部分场景的需求。

雪花算法有以下几个优点：
- 能满足高并发分布式系统环境下ID不重复 
- 基于时间戳，可以保证基本有序递增 
- 不依赖第三方的库或者中间件 
- 生成效率极高

代码如下：
```java
public class IdGenerator {
    // 起始时间戳
    public static final long START_STAMP = DateUtil.get("2022-1-1").getTime();
    //
    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;
    
    // 最大值 Math.pow(2,5) -1
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);
    
    
    // 时间戳 （42） 机房号 （5） 机器号 （5） 序列号 （12）
    // 101010101010101010101010101010101010101011 10101 10101 101011010101
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;
    
    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    // 时钟回拨的问题，我们需要去处理
    private long lastTimeStamp = -1L;
    
    public IdGenerator(long dataCenterId, long machineId) {
        // 判断传世的参数是否合法
        if(dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法.");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }
    
    public long getId(){
        // 第一步：处理时间戳的问题
        long currentTime = System.currentTimeMillis();
        
        long timeStamp = currentTime - START_STAMP;
        
        // 判断时钟回拨
        if(timeStamp < lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调.");
        }
        
        // sequenceId需要做一些处理，如果是同一个时间节点，必须自增
        if (timeStamp == lastTimeStamp){
            sequenceId.increment();
            if(sequenceId.sum() >= SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        
        // 执行结束将时间戳赋值给lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT |  dataCenterId << DATA_CENTER_LEFT
            | machineId << MACHINE_LEFT | sequence;
        
    }
    
    private long getNextTimeStamp() {
        // 获取当前的时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        // 如果一样就一直循环，直到下一个时间戳
        while (current == lastTimeStamp){
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }
    
    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1,2);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> System.out.println(idGenerator.getId())).start();
        }
    }
    
}

```