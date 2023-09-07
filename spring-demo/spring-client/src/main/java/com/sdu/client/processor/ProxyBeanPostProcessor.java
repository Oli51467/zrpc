package com.sdu.client.processor;

import com.sdu.irpc.framework.common.annotation.IrpcClient;
import com.sdu.irpc.framework.common.annotation.IrpcProxy;
import com.sdu.irpc.framework.core.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

import static com.sdu.irpc.framework.common.constant.Constants.PATH_REGEX;

@Component
public class ProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 给带有特殊注解的bean生成一个Irpc代理对象
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            IrpcProxy annotation = field.getAnnotation(IrpcProxy.class);
            if (null != annotation) {
                // 获取一个代理
                Class<?> clazz = field.getType();
                // 根据这个类获取@IrpcClient注解和对应的属性
                IrpcClient clientAnnotation = clazz.getAnnotation(IrpcClient.class);
                // 代理类必须持有@IrpcClient注解
                if (null != clientAnnotation) {
                    // 从注解中获得方法提供方的应用名和路径
                    String application = clientAnnotation.application();
                    String path = clientAnnotation.path();
                    // 检查路径是否合法
                    if (checkPath(path)) {
                        // 对路径的特殊字符做处理
                        path = processPath(path);
                        // 生成代理
                        Object proxy = ProxyFactory.getProxy(clazz, application, path);
                        field.setAccessible(true);
                        try {
                            field.set(bean, proxy);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return bean;
    }

    /**
     * 检查路径是否合法 只能包含字母、数字和"/"
     * @param path 路径
     * @return 合法 true 非法 false
     */
    protected boolean checkPath(String path) {
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
    protected String processPath(String path) {
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
