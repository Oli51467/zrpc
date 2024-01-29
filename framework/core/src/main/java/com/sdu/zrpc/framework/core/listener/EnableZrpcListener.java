package com.sdu.zrpc.framework.core.listener;

import com.sdu.zrpc.framework.common.annotation.EnableZrpc;
import com.sdu.zrpc.framework.core.config.RpcBootstrap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class EnableZrpcListener implements ApplicationListener<ApplicationReadyEvent> {

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        // 获取所有带有 @EnableZrpc 注解的 bean
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(EnableZrpc.class);
        for (Object bean : beansWithAnnotation.values()) {
            // 获取 bean 的类名，并去掉代理类的后缀
            String clazzName = bean.getClass().getName();
            clazzName = clazzName.substring(0, clazzName.indexOf("$"));
            // 使用反射获取真正的类
            Class<?> clazz = Class.forName(clazzName);
            // 获取类上的 @EnableZrpc 注解
            EnableZrpc annotation = clazz.getAnnotation(EnableZrpc.class);
            if (null == annotation) {
                continue;
            }
            Environment environment = applicationContext.getEnvironment();
            // 获取端口配置
            String port = environment.getProperty("local.server.port");
            // 设置端口并启动 RpcBootstrap
            RpcBootstrap.getInstance().getConfiguration().setPort(Integer.parseInt(Objects.requireNonNull(port)));
            RpcBootstrap.getInstance().scanServices(annotation.basePackages()).start();
            log.info("Rpc framework start!");
            // 只处理第一个带有 @EnableZrpc 注解的 bean
            break;
        }
    }
}
