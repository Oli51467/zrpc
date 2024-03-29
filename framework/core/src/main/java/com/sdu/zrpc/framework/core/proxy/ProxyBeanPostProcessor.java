package com.sdu.zrpc.framework.core.proxy;

import com.sdu.zrpc.framework.common.annotation.ZrpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
public class ProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 给带有特殊注解的bean生成一个Rpc代理对象
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            ZrpcReference annotation = field.getAnnotation(ZrpcReference.class);
            if (null != annotation) {
                // 获取一个代理
                String application = annotation.application();
                int waitTime = annotation.waitTime();
                TimeUnit waitTimeUnit = annotation.waitTimeUnit();
                Class<?> clazz = field.getType();
                // 根据接口类型和应用名称生成一个RPC代理对象
                Object proxy = ProxyFactory.getProxy(clazz, application, waitTime, waitTimeUnit);
                field.setAccessible(true);
                try {
                    // 将生成的RPC代理对象设置到原bean对象的相应字段中
                    // 设置代理 使用时增强
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
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
