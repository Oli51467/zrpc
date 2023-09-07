package com.sdu.irpc.framework.core.util;

import com.sdu.irpc.framework.common.annotation.IrpcMapping;
import com.sdu.irpc.framework.common.annotation.IrpcService;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sdu.irpc.framework.common.constant.Constants.PATH_REGEX;

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
                }).filter(clazz -> {
                    if (clazz.getAnnotation(IrpcService.class) != null) {
                        String path = clazz.getAnnotation(IrpcService.class).path();
                        return checkPath(path);
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<ServiceConfig> createServiceConfigWithClasses(List<Class<?>> classes) {
        List<ServiceConfig> serviceConfigList = new ArrayList<>();
        for (Class<?> clazz : classes) {
            // 获取接口
            IrpcService serviceAnnotation = clazz.getAnnotation(IrpcService.class);
            String parentPath = serviceAnnotation.path();
            String applicationName = serviceAnnotation.application();
            Object instance;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getAnnotation(IrpcMapping.class) != null) {
                    IrpcMapping mappingAnnotation = method.getAnnotation(IrpcMapping.class);
                    String path = parentPath + mappingAnnotation.path();
                    if (!checkPath(path)) {
                        continue;
                    }
                    path = processPath(path);
                    ServiceConfig serviceConfig = new ServiceConfig();
                    serviceConfig.setPath(path);
                    serviceConfig.setReference(instance);
                    serviceConfig.setMethod(method);
                    serviceConfig.setApplicationName(applicationName);
                    serviceConfigList.add(serviceConfig);
                }
            }
        }
        return serviceConfigList;
    }

    /**
     * 检查路径是否合法 只能包含字母、数字和"/"
     * @param path 路径
     * @return 合法 true 非法 false
     */
    public static boolean checkPath(String path) {
        if (!path.matches(PATH_REGEX)) {
            return false;
        }
        // 检查字符串的任意连续两个字符是否都不是 "/"
        for (int i = 0; i < path.length() - 1; i++) {
            if (path.charAt(i) == '/' && path.charAt(i + 1) == '/') {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理路径 将/换成.，将前导.和后缀.去除
     * @param path 路径
     * @return 处理后的路径
     */
    public static String processPath(String path) {
        path = path.replace("/", ".");
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        if (path.endsWith(".")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
