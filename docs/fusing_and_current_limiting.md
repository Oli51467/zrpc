### 一、异常重试

为保证接口的幂等性，提供了以下方案：

1. 手动指定可重试的接口，可以通过注解的形式进行标记，有特定注解的接口才能重试。

2. 设置重试白名单。

为了避免因网络抖动导致的重试风暴，可以采用以下策略：

1. 指数退避算法：在连续的重试中，每次重试之间的等待时间呈指数级增长。这样可以降低在短时间内发起大量重试请求的可能性，从而减轻对系统的压力。
2. 随机抖动：在指数退避算法的基础上，引入随机抖动，使得重试之间的等待时间变得不那么规律。这样可以避免多个客户端在相同时间点发起重试请求，进一步减轻服务器压力。
3. 限制重试次数和超时时间：限制单个请求的最大重试次数，以及整个重试过程的总超时时间，防止无限制地发起重试请求。
4. 请求结果缓存：如果有些请求的结果可以缓存，可以考虑在客户端或服务器端缓存请求结果。当发生重试时，直接从缓存中获取结果，以减轻服务器压力。
5. 服务熔断：在客户端或服务器端实现熔断机制，当连续失败达到一定阈值时，触发熔断，暂时阻止后续请求。熔断器在一段时间后会自动恢复，允许新的请求通过。

下面是一个使用指数退避和随机抖动的重试策略示例:
```java
public class RetryPolicy {
    private int maxRetries; // 最大重试次数
    private int initialInterval; // 初始重试间隔
    private double backoffMultiplier; // 退避系数
    private double jitterFactor; // 抖动因子

    // 构造方法
    public RetryPolicy(int maxRetries, int initialInterval, double backoffMultiplier, double jitterFactor) {
        this.maxRetries = maxRetries;
        this.initialInterval = initialInterval;
        this.backoffMultiplier = backoffMultiplier;
        this.jitterFactor = jitterFactor;
    }

    // 获取最大重试次数
    public int getMaxRetries() {
        return maxRetries;
    }

    // 获取下一次重试的间隔时间
    public long getNextRetryInterval(int retryCount) {
        double backoff = initialInterval * Math.pow(backoffMultiplier, retryCount); // 计算指数退避的时间间隔
        double jitter = backoff * jitterFactor * (Math.random() * 2 - 1); // 计算抖动时间间隔
        return (long) (backoff + jitter); // 返回指数退避和抖动的总和
    }
}
```

### 熔断限流

#### 服务端的自我保护

限流是一个比较通用的功能，我们可以在框架中集成限流的功能，让使用方自己去配置限流阈值；我们还可以在服务端添加限流逻辑，当调用端发送请求过来时，服务端在执行业务逻辑之前先执行限流逻辑，如果发现访问量过大并且超出了限流的阈值，就让服务端直接抛回给调用端一个限流异常，否则就执行正常的业务逻辑。

##### 实现方式

最简单的信号量计数器，基于滑动窗口的限流算法体系，例如：静态滑动、动态滑动窗口。
还有桶容量的限流算法体系，例如漏斗算法以及令牌桶算法等，其中令牌桶算法最为常用。

##### 漏桶算法
请求到达时判断漏桶容量是否已满，如果未满则放行。

##### 令牌桶算法
请求到达时向令牌桶申请令牌，判断令牌数是否超过上限，如果可以领取到则放行，否则短路

##### Guava-RateLimiter限流器

1. 核心算法：令牌桶的流量控制算法
2. 运行模式：平稳地到达阈值的预热期模式
3. 执行模式：阻塞模式（等待窗口）/ 非阻塞模式（执行状态）

基于令牌桶的限流器：
```java
@Slf4j
public class TokenBucketRateLimiter implements Limiter {

    private int tokens;

    private final int capacity;

    private final int interval;

    private Long lastTokenTime;

    public TokenBucketRateLimiter(int capacity, int interval) {
        this.capacity = capacity;
        this.interval = interval;
        this.lastTokenTime = System.currentTimeMillis();
        this.tokens = capacity;
    }

    @Override
    public synchronized boolean allowRequest() {
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        if (timeInterval > interval) {
            tokens = (int) Math.min(capacity, timeInterval * 10 + tokens);
            this.lastTokenTime = System.currentTimeMillis();
        }
        if (tokens > 0) {
            tokens--;
            return true;
        } else {
            log.info("请求被拦截---------------");
            return false;
        }
    }
}
```

我们提供一个专门的限流服务，让每个节点都依赖一个限流服务，当请求流量打过来时，服务节点触发限流逻辑，调用这个限流服务来判断是否到达了限流阈值。我们甚至可以将限流逻辑放在调用端，调用端在发出请求时先触发限流逻辑，调用限流服务，如果请求量已经到达了限流阈值，请求都不需要发出去，直接返回给动态代理一个限流异常即可。

#### 熔断机制

熔断器的工作机制主要是关闭、打开和半打开这三个状态之间的切换。

在正常情况下，熔断器是关闭的。
当调用端调用下游服务出现异常时，熔断器会收集异常指标信息进行计算，当达到熔断条件时熔断器打开，这时调用端再发起请求是会直接被熔断器拦截，并快速地执行失败逻辑。


当熔断器打开一段时间后，会转为半打开状态，这时熔断器允许调用端发送一个请求给服务端，如果这次请求能够正常地得到服务端的响应，则将状态置为关闭状态，否则设置为打开。

熔断机制主要是保护调用端，调用端在发出请求的时候会先经过熔断器。


在动态代理中加入熔断器。在发出请求时先经过熔断器，如果状态是闭合则正常发出请求，如果状态是打开则执行熔断器的失败策略。

代码如下：
```java
@Slf4j
public class CircuitBreaker implements Breaker {

    // 熔断器状态
    public static volatile CircuitStatus status = CircuitStatus.CLOSE;
    // 异常的请求数
    private final AtomicInteger errorRequestCount = new AtomicInteger(0);
    // 记录是否在半开期，使用ThreadLocal来存储线程状态
    private final ThreadLocal<Boolean> attemptLocal = ThreadLocal.withInitial(() -> false);
    // 异常的阈值
    private final int maxErrorCount = 10;
    // 打开状态持续时间，单位毫秒
    private static final long OPEN_DURATION = 50;
    // 记录熔断器打开的时间
    private long openTime = 0;

    // 每次发生请求，获取发生异常应该进行记录
    public void recordSuccessRequest() {
        if (attemptLocal.get()) {
            attemptLocal.remove();
            reset();
        }
    }

    public void recordErrorRequest() {
        // 说明当前线程进入了半打开状态的熔断器，且执行失败。重新打开熔断器
        if (attemptLocal.get()) {
            attemptLocal.remove();
            status = CircuitStatus.OPEN;
            openTime = System.currentTimeMillis();
            log.info("重试仍失败，熔断器重新打开");
        } else {
            // 普通失败，记录失败次数。判断是否需要打开
            errorRequestCount.incrementAndGet();
            if (status != CircuitStatus.OPEN && errorRequestCount.get() >= maxErrorCount) {
                status = CircuitStatus.OPEN;
                openTime = System.currentTimeMillis();
                log.info("失败次数过多，熔断器打开");
            }
        }
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        status = CircuitStatus.CLOSE;
        errorRequestCount.set(0);
    }

    public synchronized boolean attempt() {
        if (status == CircuitStatus.CLOSE) {
            return true;
        }
        if (status == CircuitStatus.HALF_OPEN) {
            log.info("当前状态为半打开，已经有线程进入");
            return false;
        }
        if (status == CircuitStatus.OPEN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - openTime >= OPEN_DURATION) {
                status = CircuitStatus.HALF_OPEN;
                attemptLocal.set(true);
                log.info("设置为半打开状态");
                return true;
            } else {
                log.info("请求被熔断");
                return false;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        CircuitBreaker circuitBreaker = new CircuitBreaker();
        for (int i = 0; i < 1000; i ++ ) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    try {
                        Thread.sleep(random.nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    int a = random.nextInt(1000);
                    if (circuitBreaker.attempt()) {
                        if (a <= 100) {
                            circuitBreaker.recordErrorRequest();
                            System.out.println("Error");
                        } else {
                            circuitBreaker.recordSuccessRequest();
                            System.out.println("Success");
                        }
                    } else {
                        System.out.println("Failed");
                    }
                }
            });
            thread.start();
        }
    }
}
```