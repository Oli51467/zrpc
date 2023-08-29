### 远程调用具体流程

#### 服务调用方

- 发送报文 ```writeAndFlush(requestArgs)```
- RpcRequest的内容
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest {

    /**
     * 请求的id
     */
    private Long requestId;

    /**
     * 请求类型
     */
    private Byte requestType;

    /**
     * 压缩类型
     */
    private Byte compressType;

    /**
     * 序列化方式
     */
    private Byte serializeType;

    /**
     * 时间戳
     */
    private Long timeStamp;

    /**
     * 具体的消息体
     */
    private RequestPayload requestPayload;
}
```
具体的消息体：
```java
/**
 * 请求调用方所请求的接口方法的描述
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {

    /**
     * 接口名
     */
    private String interfaceName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型用来确定重载方法，具体的参数用来执行方法调用
     */
    private Class<?>[] parametersType;

    /**
     * 参数列表，参数分为参数类型和具体的参数
     */
    private Object[] parametersValue;

    /**
     * 返回值的封装
     */
    private Class<?> returnType;
}
```
- outHandler转化Object为请求报文、序列化、压缩
- 发送

#### 服务提供方

- 接受报文
- inHandler解压缩、反序列化、转化请求报文为RpcRequest
- 处理Request请求，得到结果
- 发送报文```writeAndFlush(responseArgs)```
- outHandler将Object为请求报文、序列化、压缩
- 返回给服务调用方

