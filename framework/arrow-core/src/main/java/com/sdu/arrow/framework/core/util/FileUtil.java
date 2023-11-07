package com.sdu.arrow.framework.core.util;

import com.sdu.arrow.framework.common.annotation.ArrowService;
import com.sdu.arrow.framework.core.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharPool.DOT;

@Slf4j
public class FileUtil {

    public static List<String> getAllClassNames(String packageName) {
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        // 获取包所在的绝对路径
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        traverseFiles(absolutePath, classNames, basePath);
        return classNames;
    }

    private static void traverseFiles(String absolutePath, List<String> classNames, String basePath) {
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles(f -> f.isDirectory() || f.getPath().contains(".class"));
            if (null == listFiles || listFiles.length == 0) {
                return;
            }
            for (File listFile : listFiles) {
                // 如果是文件夹则递归调用
                if (listFile.isDirectory()) {
                    traverseFiles(listFile.getAbsolutePath(), classNames, basePath);
                } else {
                    String className = getClassNameByAbsolutePath(listFile.getAbsolutePath(), basePath);
                    classNames.add(className);
                }
            }
        } else {
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(className);
        }
    }

    private static String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fullPath = absolutePath.substring(absolutePath.lastIndexOf(basePath));
        return fullPath.substring(0, fullPath.lastIndexOf(".")).replace("/", ".");
    }

    public static List<Class<?>> filterClassWithServiceAnnotation(List<String> classNames) {
        return classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(clazz -> clazz.getAnnotation(ArrowService.class) != null)
                .collect(Collectors.toList());
    }

    public static List<ServiceConfig> createServiceConfigWithClasses(List<Class<?>> classes) {
        List<ServiceConfig> serviceConfigList = new ArrayList<>();
        for (Class<?> clazz : classes) {
            // 获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ArrowService arrowService = clazz.getAnnotation(ArrowService.class);
            String application = arrowService.application();
            for (Class<?> anInterface : interfaces) {
                Method[] methods = anInterface.getDeclaredMethods();
                for (Method method : methods) {
                    ServiceConfig serviceConfig = new ServiceConfig();
                    serviceConfig.setPath(anInterface.getName() + DOT + method.getName());
                    serviceConfig.setReference(instance);
                    serviceConfig.setMethod(method);
                    serviceConfig.setApplicationName(application);
                    serviceConfigList.add(serviceConfig);
                }
            }
        }
        return serviceConfigList;
    }
}
