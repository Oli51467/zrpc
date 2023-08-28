package com.sdu.irpc.framework.core.registration;

import com.sdu.irpc.framework.core.config.ServiceConfig;

public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

}
