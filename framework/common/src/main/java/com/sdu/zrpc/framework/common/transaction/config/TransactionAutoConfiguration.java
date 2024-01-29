package com.sdu.zrpc.framework.common.transaction.config;

import com.sdu.zrpc.framework.common.transaction.aspect.SecureInvokeAspect;
import com.sdu.zrpc.framework.common.transaction.dao.SecureInvokeRecordDAO;
import com.sdu.zrpc.framework.common.transaction.mapper.SecureInvokeRecordMapper;
import com.sdu.zrpc.framework.common.transaction.service.SecureInvokeService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
@Import({SecureInvokeAspect.class, SecureInvokeRecordDAO.class})
public class TransactionAutoConfiguration {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDAO secureInvokeRecordDAO) {
        return new SecureInvokeService(secureInvokeRecordDAO, threadPoolTaskExecutor);
    }
}
