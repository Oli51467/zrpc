### 通过工厂模式设置序列化器 压缩器

抽象序列化器
```java
public interface Compressor {
    /**
     * 对字节数据进行压缩
     *
     * @param bytes 带压缩的字节数组
     * @return 压缩后的字节数据
     */
    byte[] compress(byte[] bytes);

    /**
     * 对字节数据进行解压缩
     *
     * @param bytes 待解压缩的字节数据
     * @return 解压缩后的字节数据
     */
    byte[] decompress(byte[] bytes);
}
```

实现最基本的jdk序列化方式 JdkSerializer

jdk的序列化方式就是使用IO进行序列化，他只支持不同的jvm之间的传输，并不能跨语言。
```java
@Slf4j
public class JdkSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(object);

            byte[] result = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对象【{}】已经完成了序列化操作，序列化后的字节数为【{}】", object, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("序列化对象【{}】时放生异常.", object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object object = objectInputStream.readObject();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】已经完成了反序列化操作.", clazz);
            }
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化对象【{}】时放生异常.", clazz);
            throw new SerializeException(e);
        }
    }
}
```

HessianSerializer

Hessian序列化是一种支持动态类型、跨语言、基于对象传输的网络协议，Java对象序列化的二进制流可以被其他语言（如，c++，python）。特性如下：

- **自描述序列化类型**。不依赖外部描述文件或者接口定义，用一个字节表示常用的基础类型，极大缩短二进制流。
语言无关，支持脚本语言。
- 协议简单，比Java原生序列化高效 相比hessian1，hessian2中增加了压缩编码，其序列化二进制流大小是Java序列化的**50%**，序列化耗时是Java序列化的**30%**，反序列化耗时是Java序列化的**20%**。
```java
@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] result = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对象【{}】已经完成了序列化操作，序列化后的字节数为【{}】", object, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("使用hessian进行序列化对象【{}】时放生异常.", object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
            T t = (T) hessian2Input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】已经使用hessian完成了反序列化操作.", clazz);
            }
            return t;
        } catch (IOException e) {
            log.error("使用hessian进行反序列化对象【{}】时发生异常.", clazz);
            throw new SerializeException(e);
        }
    }
}
```

基于AlibabaFastJSON的JsonSerializer
```java
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }

        byte[] result = JSON.toJSONBytes(object);
        if (log.isDebugEnabled()) {
            log.debug("对象【{}】已经完成了序列化操作，序列化后的字节数为【{}】", object, result.length);
        }
        return result;

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);
        if (log.isDebugEnabled()) {
            log.debug("类【{}】已经完成了反序列化操作.", clazz);
        }
        return t;
    }
}
```

通过创建工厂实现ByteCode加载或StringCode加载

初始化时将三种序列化方式和对应的加载码放到缓存中
```
@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<SerializationType, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Serializer>> SERIALIZER_CODE_CACHE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Serializer> jdk = new ObjectWrapper<>((byte) 1, SerializationType.JDK.name(), new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<>((byte) 2, SerializationType.JSON.name(), new JsonSerializer());
        ObjectWrapper<Serializer> hessian = new ObjectWrapper<>((byte) 3, SerializationType.HESSIAN.name(), new HessianSerializer());
        SERIALIZER_CACHE.put(SerializationType.JDK, jdk);
        SERIALIZER_CACHE.put(SerializationType.JSON, json);
        SERIALIZER_CACHE.put(SerializationType.HESSIAN, hessian);

        SERIALIZER_CODE_CACHE.put((byte) 1, jdk);
        SERIALIZER_CODE_CACHE.put((byte) 2, json);
        SERIALIZER_CODE_CACHE.put((byte) 3, hessian);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     *
     * @param serializeType 序列化的类型
     * @return SerializerWrapper
     */
    public static ObjectWrapper<Serializer> getSerializer(SerializationType serializeType) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null) {
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。", serializeType);
            return SERIALIZER_CACHE.get(SerializationType.JDK);
        }
        return serializerWrapper;
    }

    public static ObjectWrapper<Serializer> getSerializer(Byte serializeCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CODE_CACHE.get(serializeCode);
        if (serializerWrapper == null) {
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。", serializeCode);
            return SERIALIZER_CACHE.get(SerializationType.JDK);
        }
        return serializerWrapper;
    }
}
```

压缩器同理，抽象Compressor
```java
public interface Compressor {
    /**
     * 对字节数据进行压缩
     *
     * @param bytes 带压缩的字节数组
     * @return 压缩后的字节数据
     */
    byte[] compress(byte[] bytes);

    /**
     * 对字节数据进行解压缩
     *
     * @param bytes 待解压缩的字节数据
     * @return 解压缩后的字节数据
     */
    byte[] decompress(byte[] bytes);
}
```
实现最基本的压缩方式 GzipCompressor
```java
@Slf4j
public class GzipCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组进行了压缩长度由【{}】压缩至【{}】.", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }

    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组进行了解压缩长度由【{}】变为【{}】.", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }
}
```

还扩展了Deflate、Bzip2、Lzo等压缩方式