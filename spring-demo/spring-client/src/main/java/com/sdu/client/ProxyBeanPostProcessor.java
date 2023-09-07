package com.sdu.client;

import com.sdu.irpc.framework.common.annotation.IrpcProxy;
import com.sdu.irpc.framework.core.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

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
                Class<?> type = field.getType();
                Object proxy = ProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
