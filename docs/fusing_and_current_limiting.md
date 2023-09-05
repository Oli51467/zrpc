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

如何实现？

最简单的计数器，还有可以做到平滑限流的滑动窗口、漏斗算法以及令牌桶算法等等。其中令牌桶算法最为常用。

基于令牌桶的限流器：
```java
public class TokenBuketRateLimiter implements RateLimiter {
    
    // 代表令牌的数量，>0 说明有令牌，能放行，放行就减一，==0,无令牌  阻拦
    private int tokens;
    
    // 限流的本质就是，令牌数
    private final int capacity;
    
    private final int rate;
    
    // 上一次放令牌的时间
    private Long lastTokenTime;
    
    public TokenBuketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }
    
    /**
     * 判断请求是否可以放行
     * @return true 放行  false  拦截
     */
    public synchronized boolean allowRequest() {
        // 1、给令牌桶添加令牌
        // 计算从现在到上一次的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        // 如果间隔时间超过一秒，放令牌
        if(timeInterval >= 1000/rate){
            int needAddTokens = (int)(timeInterval * rate / 1000);
            System.out.println("needAddTokens = " + needAddTokens);
            // 给令牌桶添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            System.out.println("tokens = " + tokens);
    
            // 标记最后一个放入令牌的时间
            this.lastTokenTime = System.currentTimeMillis();
        }
        
        // 2、自己获取令牌,如果令牌桶中有令牌则放行，否则拦截
        if(tokens > 0){
            tokens --;
            System.out.println("请求被放行---------------");
            return true;
        } else {
            System.out.println("请求被拦截---------------");
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
public class CircuitBreaker {

    // 理论上：标准的断路器应该有三种状态  open close half_open，我们为了简单只选取两种
    private volatile boolean isOpen = false;

    // 需要搜集指标  异常的数量   比例
    // 总的请求数
    private AtomicInteger requestCount = new AtomicInteger(0);

    // 异常的请求数
    private AtomicInteger errorRequest = new AtomicInteger(0);

    // 异常的阈值
    private int maxErrorRequest;
    private float maxErrorRate;

    public CircuitBreaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }


    // 断路器的核心方法，判断是否开启
    public boolean isBreak() {
        // 优先返回，如果已经打开了，就直接返回true
        if (isOpen) {
            return true;
        }

        // 需要判断数据指标，是否满足当前的阈值
        if (errorRequest.get() > maxErrorRequest) {
            this.isOpen = true;
            return true;
        }

        if (errorRequest.get() > 0 && requestCount.get() > 0 &&
                errorRequest.get() / (float) requestCount.get() > maxErrorRate
        ) {
            this.isOpen = true;
            return true;
        }

        return false;
    }

    // 每次发生请求，获取发生异常应该进行记录
    public void recordRequest() {
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest() {
        this.errorRequest.getAndIncrement();
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequest.set(0);
    }


    public static void main(String[] args) {

        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 1.1F);

        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequest();
                int num = new Random().nextInt(100);
                if (num > 70) {
                    circuitBreaker.recordErrorRequest();
                }

                boolean aBreak = circuitBreaker.isBreak();

                String result = aBreak ? "断路器阻塞了请求" : "断路器放行了请求";

                System.out.println(result);

            }
        }).start();


        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("-----------------------------------------");
                circuitBreaker.reset();
            }
        }).start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```