package com.sdu.zrpc.framework.core.spi;

import com.sdu.zrpc.framework.common.entity.ObjectWrapper;
import com.sdu.zrpc.framework.common.exception.SpiException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.sdu.zrpc.framework.common.constant.Constants.SPI_BASE_PATH;

@Slf4j
public class SpiHandler {

    // 缓存保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);
    // 缓存的是每一个接口所对应的实现的实例
    private static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(16);

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> fileURL;
        try {
            fileURL = classLoader.getResources(SPI_BASE_PATH);
            while (fileURL.hasMoreElements()) {
                URL url = fileURL.nextElement();
                String fileName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setUseCaches(false);
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String implClassName = bufferedReader.readLine();
                List<String> value = new ArrayList<>();
                while (null != implClassName) {
                    value.add(implClassName);
                    implClassName = bufferedReader.readLine();
                }
                SPI_CONTENT.put(fileName, value);
            }
        } catch (IOException e) {
            throw new SpiException("spi路径资源不存在");
        }
    }

    /**
     * 获取第一个和当前服务相关的实例
     *
     * @param clazz 一个服务接口的class实例
     * @return 实现类的实例
     */
    public synchronized static <T> ObjectWrapper<T> load(Class<T> clazz) {
        // 尝试从缓存中获取
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (null != objectWrappers && !objectWrappers.isEmpty()) {
            return (ObjectWrapper<T>) objectWrappers.get(0);
        }
        // 构建缓存
        buildCache(clazz);

        List<ObjectWrapper<?>> result = SPI_IMPLEMENT.get(clazz);
        if (result == null || result.isEmpty()) {
            return null;
        }

        // 尝试获取第一个
        return (ObjectWrapper<T>) result.get(0);
    }

    /**
     * 获取所有和当前服务相关的实例
     *
     * @param clazz 一个服务接口的class实例
     * @return 实现类的实例集合
     */
    public synchronized static <T> List<ObjectWrapper<T>> loadAll(Class<T> clazz) {
        // 尝试从缓存中获取
        List<ObjectWrapper<?>> objectWrapperList = SPI_IMPLEMENT.get(clazz);
        if (null != objectWrapperList && !objectWrapperList.isEmpty()) {
            return objectWrapperList.stream().map(wrapper -> (ObjectWrapper<T>) wrapper).collect(Collectors.toList());
        }
        // 构建缓存
        buildCache(clazz);
        // 再次获取
        objectWrapperList = SPI_IMPLEMENT.get(clazz);
        if (objectWrapperList != null && !objectWrapperList.isEmpty()) {
            return objectWrapperList.stream().map(wrapper -> (ObjectWrapper<T>) wrapper).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 构建clazz相关的缓存
     *
     * @param clazz 一个类的class实例
     */
    private static <T> void buildCache(Class<T> clazz) {
        // 通过clazz获取与之匹配的实现名称
        String clazzName = clazz.getName();
        // 获取已经初始已经缓存的实现的全类名
        List<String> implNames = SPI_CONTENT.get(clazzName);
        if (null == implNames || implNames.isEmpty()) {
            return;
        }
        // 实例化所有的实现
        List<ObjectWrapper<?>> impls = new ArrayList<>();
        for (String implName : implNames) {
            try {
                String[] configs = implName.split("-");
                if (configs.length != 3) {
                    throw new SpiException("您配置的spi文件不合法");
                }
                Byte code = Byte.valueOf(configs[0]);
                String type = configs[1];
                String implementName = configs[2];
                Class<?> implementClass = Class.forName(implementName);
                if (!clazz.isAssignableFrom(implementClass)) {
                    throw new SpiException(implementName + "不是" + clazzName + "的实现");
                }
                Object impl = implementClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type, impl);
                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.error("实例化【{}】的实现时发生了异常", implName, e);
            }
        }
        SPI_IMPLEMENT.put(clazz, impls);
    }
}
