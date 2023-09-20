package com.sdu.arrow.framework.core.proxy;

import com.sdu.arrow.framework.common.annotation.RpcClient;
import com.sdu.arrow.framework.common.annotation.RpcProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

import static com.sdu.arrow.framework.core.util.FileUtil.checkPath;

@Component
@Order(1)
public class ProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 给带有特殊注解的bean生成一个Rpc代理对象
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            RpcProxy annotation = field.getAnnotation(RpcProxy.class);
            if (null != annotation) {
                // 获取一个代理
                Class<?> clazz = field.getType();
                // 根据这个类获取@RpcClient注解和对应的属性
                RpcClient clientAnnotation = clazz.getAnnotation(RpcClient.class);
                // 代理类必须持有@RpcClient注解
                if (null != clientAnnotation) {
                    // 从注解中获得方法提供方的应用名和路径
                    String application = clientAnnotation.application();
                    String path = clientAnnotation.path();
                    // 检查路径是否合法
                    if (checkPath(path)) {
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

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
