package com.sdu.irpc.framework.common.transaction.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.sdu.irpc.framework.common.transaction.dao.SecureInvokeRecordDAO;
import com.sdu.irpc.framework.common.transaction.entity.dto.SecureInvokeDTO;
import com.sdu.irpc.framework.common.transaction.entity.po.SecureInvokeRecord;
import com.sdu.irpc.framework.common.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Description: 安全执行处理器
 */
@Slf4j
@AllArgsConstructor
public class SecureInvokeService {

    public static final double RETRY_INTERVAL_MINUTES = 2D;

    private final SecureInvokeRecordDAO secureInvokeRecordDAO;

    private final Executor executor;

//    @Scheduled(cron = "*/100000000 * * * * ?")
//    public void retry() {
//        List<SecureInvokeRecord> secureInvokeRecords = secureInvokeRecordDAO.getWaitRetryRecords();
//        for (SecureInvokeRecord secureInvokeRecord : secureInvokeRecords) {
//            doAsyncInvoke(secureInvokeRecord);
//        }
//    }

    public void save(SecureInvokeRecord record) {
        secureInvokeRecordDAO.save(record);
    }

    private void retryRecord(SecureInvokeRecord record, String errorMsg) {
        Integer retryTimes = record.getRetryTimes() + 1;
        SecureInvokeRecord update = new SecureInvokeRecord();
        update.setId(record.getId());
        update.setFailReason(errorMsg);
        update.setNextRetryTime(getNextRetryTime(retryTimes));
        if (retryTimes > record.getMaxRetryTimes()) {
            update.setStatus(SecureInvokeRecord.STATUS_FAIL);
        } else {
            update.setRetryTimes(retryTimes);
        }
        secureInvokeRecordDAO.updateById(update);
    }

    private Date getNextRetryTime(Integer retryTimes) {//或者可以采用退避算法
        double waitMinutes = Math.pow(RETRY_INTERVAL_MINUTES, retryTimes);//重试时间指数上升 2m 4m 8m 16m
        return DateUtil.offsetMinute(new Date(), (int) waitMinutes);
    }

    private void removeRecord(Long id) {
        secureInvokeRecordDAO.removeById(id);
    }

    public void invoke(SecureInvokeRecord record, boolean async) {
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        // 非事务状态，直接执行，不做任何保证。
        if (!inTransaction) {
            return;
        }
        // 保存执行数据
        save(record);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @SneakyThrows
            @Override
            public void afterCommit() {
                // 事务后执行
                if (async) {
                    doAsyncInvoke(record);
                } else {
                    doInvoke(record);
                }
            }
        });
    }

    public void doAsyncInvoke(SecureInvokeRecord record) {
        executor.execute(() -> {
            System.out.println(Thread.currentThread().getName());
            doInvoke(record);
        });
    }

    public void doInvoke(SecureInvokeRecord record) {
        SecureInvokeDTO secureInvokeDTO = record.getSecureInvokeDTO();
        try {
            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass);
            List<String> parameterStrings = JsonUtil.toList(secureInvokeDTO.getParameterTypes(), String.class);
            List<Class<?>> parameterClasses = getParameters(parameterStrings);
            Method method = ReflectUtil.getMethod(beanClass, secureInvokeDTO.getMethodName(), parameterClasses.toArray(new Class[]{}));
            Object[] args = secureInvokeDTO.getArgs();
            // 执行方法
            method.invoke(bean, args);
            // 执行成功更新状态
            removeRecord(record.getId());
        } catch (Throwable e) {
            log.error("SecureInvokeService invoke fail", e);
            // 执行失败，等待下次执行
            retryRecord(record, e.getMessage());
        }
    }

    private List<Class<?>> getParameters(List<String> parameterStrings) {
        return parameterStrings.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                log.error("SecureInvokeService class not fund", e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
